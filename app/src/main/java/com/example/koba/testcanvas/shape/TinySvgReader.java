package com.example.koba.testcanvas.shape;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * SVG読み込み 簡易版
 *
 * TinySvgWriterで出力したもののみ受け付ける
 */
class TinySvgReader implements ISvgReader {
    private static final String TAG_SVG = "svg";
    private static final String TAG_CIRCLE = "circle";
    private static final String TAG_ELLIPSE = "ellipse";
    private static final String TAG_LINE = "line";
    private static final String TAG_PATH = "path";
    private static final String TAG_POLYGON = "polygon";
    private static final String TAG_POLYLINE = "polyline";
    private static final String TAG_RECT = "rect";
    private static final String TAG_TEXT = "text";
    private static final String ATTR_WIDTH = "width";
    private static final String ATTR_HEIGHT = "height";

    private OnCircleListener onCircleListener = null;
    private OnEllipseListener onEllipseListener = null;
    private OnLineListener onLineListener = null;
    private OnPathArcListener onPathArcListener = null;
    private OnPolygonListener onPolygonListener = null;
    private OnPolylineListener onPolylineListener = null;
    private OnRectListener onRectListener = null;
    private OnTextListener onTextListener = null;

    private Element svg = null;

    private double width = 0;
    private double height = 0;
    private int strokeColor = 0xff000000;
    private float strokeWidth = 1;
    private float fontSize = 1;
    private String fill = "";
    private int fillColor = 0xff000000;

    @Override
    public boolean read(InputStream stream) {
        final Document document;
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(stream);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        }

        // ルートノード取得
        svg = document.getDocumentElement();
        if (!svg.getTagName().equals(TAG_SVG))
            return false;

        // 画像サイズ取得
        if (svg.hasAttribute(ATTR_WIDTH))
            width = Double.parseDouble(svg.getAttribute(ATTR_WIDTH));
        if (svg.hasAttribute(ATTR_HEIGHT))
            height = Double.parseDouble(svg.getAttribute(ATTR_HEIGHT));

        return true;
    }

    @Override
    public boolean parse() {
        if (svg == null)
            return false;

        // 子ノード取得
        // svgタグ直下の図形のみ処理する
        final NodeList nodeList = svg.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;
            final Element elem = (Element)node;
            final String name = elem.getTagName();
            switch (name) {
                case TAG_CIRCLE:
                    parseCircle(elem);
                    break;
                case TAG_ELLIPSE:
                    parseEllipse(elem);
                    break;
                case TAG_LINE:
                    parseLine(elem);
                    break;
                case TAG_PATH:
                    parsePath(elem);
                    break;
                case TAG_POLYGON:
                    parsePolygon(elem);
                    break;
                case TAG_POLYLINE:
                    parsePolyline(elem);
                    break;
                case TAG_RECT:
                    parseRect(elem);
                    break;
                case TAG_TEXT:
                    parseText(elem);
                    break;
                default:
                    // 上記以外の場合、無視する
                    break;
            }
        }

        return true;
    }

    /**
     * 円のタグの読み込み
     * @param elem 対象のタグ
     */
    private void parseCircle(Element elem) {
        if (onCircleListener == null)
            return;
        parseAttr(elem);
        final double cx = Double.parseDouble(elem.getAttribute("cx"));
        final double cy = Double.parseDouble(elem.getAttribute("cy"));
        final double r = Double.parseDouble(elem.getAttribute("r"));
        onCircleListener.onCircle(this, cx, cy, r);
    }

    /**
     * 楕円のタグの読み込み
     * @param elem 対象のタグ
     */
    private void parseEllipse(Element elem) {
        if (onEllipseListener == null)
            return;
        parseAttr(elem);
        final double cx = Double.parseDouble(elem.getAttribute("cx"));
        final double cy = Double.parseDouble(elem.getAttribute("cy"));
        final double rx = Double.parseDouble(elem.getAttribute("rx"));
        final double ry = Double.parseDouble(elem.getAttribute("ry"));
        onEllipseListener.onEllipse(this, cx, cy, rx, ry);
    }

    /**
     * 直線のタグの読み込み
     * @param elem 対象のタグ
     */
    private void parseLine(Element elem) {
        if (onLineListener == null)
            return;
        parseAttr(elem);
        fill = "none"; // 塗りつぶしなし扱いをする
        final double x1 = Double.parseDouble(elem.getAttribute("x1"));
        final double y1 = Double.parseDouble(elem.getAttribute("y1"));
        final double x2 = Double.parseDouble(elem.getAttribute("x2"));
        final double y2 = Double.parseDouble(elem.getAttribute("y2"));
        onLineListener.onLine(this, x1, y1, x2, y2);
    }

    /**
     * Pathのタグの読み込み （円弧単体のみ）
     * @param elem 対象のタグ
     */
    private void parsePath(Element elem) {
        if (onPathArcListener == null)
            return;
        parseAttr(elem);

        final String d = elem.getAttribute("d");
        final LinkedList<String> dList = new LinkedList<>(Arrays.asList(d.split(" ")));

        // 円弧のみのPATHでない場合、何もしない
        if (dList.size() != 5)
            return;

        // 最初の指定位置を取得
        final String m = dList.removeFirst();
        if (!m.startsWith("M"))
            return;
        final Double[] mxmy = getXYArray(m.substring(1));
        if (mxmy == null)
            return;
        final double mx = mxmy[0];
        final double my = mxmy[1];

        // 円弧にする円の半径を取得する
        final String a = dList.removeFirst();
        if (!a.startsWith("A"))
            return;
        final Double[] rxry = getXYArray(a.substring(1));
        if (rxry == null)
            return;
        final double rx = rxry[0];
        final double ry = rxry[1];

        // 円弧の回転角度を取得する
        final String xAR = dList.removeFirst();
        final double xAxisRotation = Double.parseDouble(xAR);

        // flagを取得する
        final String flags = dList.removeFirst();
        final String[] flagsArray = flags.split(",");
        final boolean largeArcFlag = Integer.parseInt(flagsArray[0]) != 0;
        final boolean sweepFlag = Integer.parseInt(flagsArray[1]) != 0;

        // 末端の座標を取得する
        final String point = dList.removeFirst();
        final Double[] xy = getXYArray(point);
        if (xy == null)
            return;
        final double x = xy[0];
        final double y = xy[1];

        onPathArcListener.onPathArc(this, mx, my, rx, ry,
                xAxisRotation, largeArcFlag, sweepFlag, x, y);
    }

    /**
     * "x,y"の形式の文字列を解析してDoubleの配列を返す
     * @param xy "x,y"の形式の文字列
     * @return Doubleに変換した値の配列
     */
    private Double[] getXYArray(String xy) {
        final String[] xyArray = xy.split(",");
        if (xyArray.length < 2)
            return null;
        final Double[] xyDoubleArray = new Double[2];
        for (int i = 0; i < xyDoubleArray.length; i++)
            xyDoubleArray[i] = Double.parseDouble(xyArray[i]);
        return xyDoubleArray;
    }

    /**
     * 多角形のタグの読み込み
     * @param elem 対象のタグ
     */
    private void parsePolygon(Element elem) {
        if (onPolygonListener == null)
            return;
        parseAttr(elem);
        final String pointsStr = elem.getAttribute("points");
        final List<Double> points = getPointList(pointsStr);
        onPolygonListener.onPolygon(this, points);
    }

    /**
     * 連続直線のタグの読み込み
     * @param elem 対象のタグ
     */
    private void parsePolyline(Element elem) {
        if (onPolylineListener == null)
            return;
        parseAttr(elem);
        final String pointsStr = elem.getAttribute("points");
        final List<Double> points = getPointList(pointsStr);
        onPolylineListener.onPolyline(this, points);
    }

    /**
     * 長方形のタグの読み込み
     * @param elem 対象のタグ
     */
    private void parseRect(Element elem) {
        if (onRectListener == null)
            return;
        parseAttr(elem);
        final double x = Double.parseDouble(elem.getAttribute("x"));
        final double y = Double.parseDouble(elem.getAttribute("y"));
        final double width = Double.parseDouble(elem.getAttribute("width"));
        final double height = Double.parseDouble(elem.getAttribute("height"));
        onRectListener.onRect(this, x, y, width, height);
    }

    /**
     * 文字列のタグの読み込み
     * @param elem 対象のタグ
     */
    private void parseText(Element elem) {
        if (onTextListener == null)
            return;
        parseAttr(elem);
        final double x = Double.parseDouble(elem.getAttribute("x"));
        final double y = Double.parseDouble(elem.getAttribute("y"));
        final String str = elem.getTextContent();
        onTextListener.onText(this, x, y, str);
    }

    /**
     * 属性を読み込む
     * @param elem 対象のタグ
     */
    private void parseAttr(Element elem) {
        // 色(色と透過値)の取得
        double opacity = 1;  // 0 <= opacity <= 1
        if (elem.hasAttribute("stroke-opacity"))
            opacity = Double.parseDouble(elem.getAttribute("stroke-opacity"));
        int alpha = (int)(0xff * opacity);
        if (alpha > 0xff)
            alpha = 0xff;
        int strokeColorOnly = 0;
        if (elem.hasAttribute("stroke")) {
            final String stroke = elem.getAttribute("stroke");
            if (stroke.startsWith("#"))
                strokeColorOnly = Integer.parseInt(stroke.substring(1), 16);
        }
        if (strokeColorOnly > 0xffffff)
            strokeColorOnly = 0xffffff;
        strokeColor = (alpha << 24) | strokeColorOnly;
        // 線の幅の取得
        if (elem.hasAttribute("stroke-width"))
            strokeWidth = (float) Double.parseDouble(elem.getAttribute("stroke-width"));
        // 文字のサイズの取得
        if (elem.hasAttribute("font-size"))
            fontSize = (float) Double.parseDouble(elem.getAttribute("font-size"));
        // fill属性
        if (elem.hasAttribute("fill"))
            fill = elem.getAttribute("fill");
        int fillColorOnly = 0;
        if (fill.startsWith("#"))
            fillColorOnly = Integer.parseInt(fill.substring(1), 16);
        if (fillColorOnly > 0xffffff)
            fillColorOnly = 0xffffff;
        fillColor = (alpha << 24) | fillColorOnly;
    }

    /**
     * 空白で区切られた(x,y)をListにして返す
     * @param points 空白区切りの数字列
     * @return Doubleのリスト
     */
    private List<Double> getPointList(String points) {
        // まず空白で分割されたxyの配列を作成
        final String[] pointsStrArray = points.split(" ");
        final List<Double> pointList = new LinkedList<>();
        for (String xy : pointsStrArray) {
            // コンマで区切られたxyを分割してリストに追加
            final String[] xyArray = xy.split(",");
            pointList.add(Double.parseDouble(xyArray[0]));
            pointList.add(Double.parseDouble(xyArray[1]));
        }
        return pointList;
    }

    @Override
    public double getSvgSizeWidth() {
        return width;
    }

    @Override
    public double getSvgSizeHeight() {
        return height;
    }

    @Override
    public int getStrokeColor() {
        return strokeColor;
    }

    @Override
    public float getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    public float getFontSize() {
        return fontSize;
    }

    @Override
    public String getFill() {
        return fill;
    }

    @Override
    public int getFillColor() {
        return fillColor;
    }

    @Override
    public void setOnCircleListener(OnCircleListener listener) {
        onCircleListener = listener;
    }

    @Override
    public void setOnEllipseListener(OnEllipseListener listener) {
        onEllipseListener = listener;
    }

    @Override
    public void setOnLineListener(OnLineListener listener) {
        onLineListener = listener;
    }

    @Override
    public void setOnPathArcListener(OnPathArcListener listener) {
        onPathArcListener = listener;
    }

    @Override
    public void setOnPolygonListener(OnPolygonListener listener) {
        onPolygonListener = listener;
    }

    @Override
    public void setOnPolylineListener(OnPolylineListener listener) {
        onPolylineListener = listener;
    }

    @Override
    public void setOnRectListener(OnRectListener listener) {
        onRectListener = listener;
    }

    @Override
    public void setOnTextListener(OnTextListener listener) {
        onTextListener = listener;
    }
}
