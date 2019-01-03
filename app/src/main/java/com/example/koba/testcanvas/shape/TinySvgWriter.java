package com.example.koba.testcanvas.shape;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Writer;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * SVGのXML構築 簡易版
 *
 * 基本図形の追加等
 */
class TinySvgWriter implements ISvgWriter {
    private static final String NS_SVG = "http://www.w3.org/2000/svg";
    private static final String NS_XLINK = "http://www.w3.org/1999/xlink";

    private Document document;

    private int strokeColor = 0xff000000;
    private float strokeWidth = 1;
    private float fontSize = 20;

    TinySvgWriter() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        // ルートノード生成
        final Element svg = document.createElementNS(NS_SVG, "svg");

        // 属性追加
        svg.setAttribute("version", "1.1");
        svg.setAttribute("xmlns:xlink", NS_XLINK);

        document.appendChild(svg);
    }

    @Override
    public boolean writeTo(Writer writer) {
        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final Transformer t = tf.newTransformer();
            // xml整形の設定
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
            // 文字列として出力
            t.transform(new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void setSvgSize(double width, double height) {
        final Element svg = document.getDocumentElement();
        svg.setAttribute("width", String.valueOf(width));
        svg.setAttribute("height", String.valueOf(height));
    }

    @Override
    public void setStrokeColor(int color) {
        this.strokeColor = color;
    }

    @Override
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Override
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * 線の見た目を設定する
     * @param shape 設定する図形
     */
    private void setStroke(Element shape) {
        shape.setAttribute("stroke", getColorCode(strokeColor));
        shape.setAttribute("stroke-opacity", getOpacity(strokeColor));
        shape.setAttribute("stroke-width", String.valueOf(strokeWidth));
    }

    /**
     * colorからRGBを取り出して、SVG用の文字列を返す
     * @param color 色
     * @return 色を表す文字列
     */
    private String getColorCode(int color) {
        final int r = (color >> 16) & 0xff;
        final int g = (color >>  8) & 0xff;
        final int b = (color      ) & 0xff;

        return String.format("#%02x%02x%02x", r, g, b);
    }

    /**
     * colorからalpha値を取り出して、SVG用の文字列を返す
     * @param color 色
     * @return 透過率を表す文字列
     */
    private String getOpacity(int color) {
        final int a = (color >> 24) & 0xff;
        return String.valueOf(a / (float)0xff);
    }

    @Override
    public void addCircle(double cx, double cy, double r) {
        final Element circle = document.createElement("circle");
        circle.setAttribute("cx", String.valueOf(cx));
        circle.setAttribute("cy", String.valueOf(cy));
        circle.setAttribute("r", String.valueOf(r));
        setStroke(circle);
        circle.setAttribute("fill", "none");
        document.getDocumentElement().appendChild(circle);
    }

    @Override
    public void addEllipse(double cx, double cy, double rx, double ry) {
        final Element ellipse = document.createElement("ellipse");
        ellipse.setAttribute("cx", String.valueOf(cx));
        ellipse.setAttribute("cy", String.valueOf(cy));
        ellipse.setAttribute("rx", String.valueOf(rx));
        ellipse.setAttribute("ry", String.valueOf(ry));
        setStroke(ellipse);
        ellipse.setAttribute("fill", "none");
        document.getDocumentElement().appendChild(ellipse);
    }

    @Override
    public void addLine(double x1, double y1, double x2, double y2) {
        final Element line = document.createElement("line");
        line.setAttribute("x1", String.valueOf(x1));
        line.setAttribute("y1", String.valueOf(y1));
        line.setAttribute("x2", String.valueOf(x2));
        line.setAttribute("y2", String.valueOf(y2));
        setStroke(line);
        document.getDocumentElement().appendChild(line);
    }

    @Override
    public void addPathArc(double mx, double my, double rx, double ry, double xAxisRotation,
                           boolean largeArcFlag, boolean sweepFlag, double x, double y) {
        final Element pathArc = document.createElement("path");
        final String d = "M" + mx + "," + my +
                " A" + rx + "," + ry +
                " " + xAxisRotation +
                " " + (largeArcFlag ? 1 : 0) + "," + (sweepFlag ? 1 : 0) +
                " " + x + "," + y;
        pathArc.setAttribute("d", d);
        setStroke(pathArc);
        pathArc.setAttribute("fill", "none");
        document.getDocumentElement().appendChild(pathArc);
    }

    @Override
    public void addPolygon(double x, double y, List<Double> pointList) {
        // 線が描けないpointListの場合、何もしない
        final int pointListSize = pointList.size();
        if (pointListSize < 4 || pointListSize % 2 != 0)
            return;

        final Element polygon = document.createElement("polygon");
        final StringBuilder points = new StringBuilder();
        points.append(x).append(",").append(y);
        for (int i = 0; i < pointList.size(); i += 2)
            points.append(" ").append(pointList.get(i)).append(",").append(pointList.get(i + 1));
        polygon.setAttribute("points", points.toString());
        setStroke(polygon);
        polygon.setAttribute("fill", "none");
        document.getDocumentElement().appendChild(polygon);
    }

    @Override
    public void addPolyline(double x, double y, List<Double> pointList) {
        // 線が描けないpointListの場合、何もしない
        final int pointListSize = pointList.size();
        if (pointListSize < 4 || pointListSize % 2 != 0)
            return;

        final Element polyline = document.createElement("polyline");
        final StringBuilder points = new StringBuilder();
        points.append(x).append(",").append(y);
        for (int i = 0; i < pointList.size(); i += 2)
            points.append(" ").append(pointList.get(i)).append(",").append(pointList.get(i + 1));
        polyline.setAttribute("points", points.toString());
        setStroke(polyline);
        polyline.setAttribute("fill", "none");
        document.getDocumentElement().appendChild(polyline);
    }

    @Override
    public void addRect(double x, double y, double width, double height) {
        final Element rect = document.createElement("rect");
        rect.setAttribute("x", String.valueOf(x));
        rect.setAttribute("y", String.valueOf(y));
        rect.setAttribute("width", String.valueOf(width));
        rect.setAttribute("height", String.valueOf(height));
        setStroke(rect);
        rect.setAttribute("fill", "none");
        document.getDocumentElement().appendChild(rect);
    }

    @Override
    public void addText(double x, double y, String str) {
        final Element text = document.createElement("text");
        text.setAttribute("x", String.valueOf(x));
        text.setAttribute("y", String.valueOf(y));
        text.setAttribute("fill", getColorCode(strokeColor));
        text.setAttribute("font-size", String.valueOf(fontSize));
        text.setAttribute("xml:space", "preserve");  // 連続した空白を表示する
        text.setTextContent(str);
        document.getDocumentElement().appendChild(text);
    }
}
