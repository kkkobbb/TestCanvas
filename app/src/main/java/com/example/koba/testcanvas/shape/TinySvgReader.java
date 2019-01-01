package com.example.koba.testcanvas.shape;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * SVG読み込み 簡易版
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
        final Element svg = document.getDocumentElement();
        if (!svg.getTagName().equals(TAG_SVG))
            return false;

        // 画像サイズ取得
        if (svg.hasAttribute(ATTR_WIDTH))
            width = Double.parseDouble(svg.getAttribute(ATTR_WIDTH));
        if (svg.hasAttribute(ATTR_HEIGHT))
            height = Double.parseDouble(svg.getAttribute(ATTR_HEIGHT));

        // 子ノード取得
        final NodeList nodeList = svg.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;
            final Element elem = (Element)node;
            final String name = elem.getTagName();
            switch (name) {
                case TAG_CIRCLE:
                    readCircle(elem);
                    break;
                case TAG_ELLIPSE:
                    readEllipse(elem);
                    break;
                case TAG_LINE:
                    readLine(elem);
                    break;
                case TAG_PATH:
                    readPath(elem);
                    break;
                case TAG_POLYGON:
                    readPolygon(elem);
                    break;
                case TAG_POLYLINE:
                    readPolyline(elem);
                    break;
                case TAG_RECT:
                    readRect(elem);
                    break;
                case TAG_TEXT:
                    readText(elem);
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
    private void readCircle(Element elem) {
        if (onCircleListener == null)
            return;
        readAttr(elem);
        final double cx = Double.parseDouble(elem.getAttribute("cx"));
        final double cy = Double.parseDouble(elem.getAttribute("cy"));
        final double r = Double.parseDouble(elem.getAttribute("r"));
        onCircleListener.onCircle(this, cx, cy, r);
    }

    /**
     * 楕円のタグの読み込み
     * @param elem 対象のタグ
     */
    private void readEllipse(Element elem) {
        if (onEllipseListener == null)
            return;
        readAttr(elem);
        final double cx = Double.parseDouble(elem.getAttribute("cx"));
        final double cy = Double.parseDouble(elem.getAttribute("cy"));
        final double rx = Double.parseDouble(elem.getAttribute("rx"));
        final double ry = Double.parseDouble(elem.getAttribute("ry"));
        onEllipseListener.onEllipse(this, cx, cy, rx, ry);
    }

    private void readLine(Element elem) {
        if (onLineListener == null)
            return;
        readAttr(elem);
        fill = "none"; // 塗りつぶしなし扱いをする
        final double x1 = Double.parseDouble(elem.getAttribute("x1"));
        final double y1 = Double.parseDouble(elem.getAttribute("y1"));
        final double x2 = Double.parseDouble(elem.getAttribute("x2"));
        final double y2 = Double.parseDouble(elem.getAttribute("y2"));
        onLineListener.onLine(this, x1, y1, x2, y2);
    }

    private void readPath(Element elem) {
        if (onPathArcListener == null)
            return;
        readAttr(elem);
        // TODO
    }

    private void readPolygon(Element elem) {
        if (onPolygonListener == null)
            return;
        readAttr(elem);
        // TODO
    }

    private void readPolyline(Element elem) {
        if (onPolylineListener == null)
            return;
        readAttr(elem);
        // TODO
    }

    private void readRect(Element elem) {
        if (onRectListener == null)
            return;
        readAttr(elem);
        final double x = Double.parseDouble(elem.getAttribute("x"));
        final double y = Double.parseDouble(elem.getAttribute("y"));
        final double width = Double.parseDouble(elem.getAttribute("width"));
        final double height = Double.parseDouble(elem.getAttribute("height"));
        onRectListener.onRect(this, x, y, width, height);
    }

    private void readText(Element elem) {
        if (onTextListener == null)
            return;
        readAttr(elem);
        final double x = Double.parseDouble(elem.getAttribute("x"));
        final double y = Double.parseDouble(elem.getAttribute("y"));
        final String str = elem.getTextContent();
        onTextListener.onText(this, x, y, str);
    }

    /**
     * 属性を読み込む
     * @param elem 対象のタグ
     */
    private void readAttr(Element elem) {
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
