package com.example.koba.testcanvas.shape;

import java.io.Writer;
import java.util.List;

/**
 * SVG構築用のインターフェース
 */
interface ISvgWriter {
    /**
     * SVG出力
     * @param writer 出力先
     * @return 成否
     */
    boolean writeTo(Writer writer);

    /**
     * 画像全体のサイズを設定
     * @param width 幅
     * @param height 高さ
     */
    void setSvgSize(double width, double height);

    /**
     * 線の色
     * @param color 色 0xAARRGGBB  (AA:alpha RR:red GG:green BB:blue)
     */
    void setStrokeColor(int color);

    /**
     * 線の太さ
     * @param strokeWidth 太さ
     */
    void setStrokeWidth(float strokeWidth);

    /**
     * 文字の大きさ
     * @param fontSize 文字サイズ
     */
    void setFontSize(float fontSize);

    /**
     * 円の追加
     * @param cx 中心のx座標
     * @param cy 中心のy座標
     * @param r 半径
     */
    void addCircle(double cx, double cy, double r);

    /**
     * 楕円の追加
     * @param cx 中心のx座標
     * @param cy 中心のy座標
     * @param rx x軸方向の半径
     * @param ry y軸方向の半径
     */
    void addEllipse(double cx, double cy, double rx, double ry);

    /**
     * 直線の追加
     * @param x1 始点のx座標
     * @param y1 始点のy座標
     * @param x2 終点のx座標
     * @param y2 終点のy座標
     */
    void addLine(double x1, double y1, double x2, double y2);

    /**
     * パス（円弧1つ分）の追加
     * @param mx 弧の始点のx座標
     * @param my 弧の始点のy座標
     * @param rx 弧のx軸方向の半径
     * @param ry 弧のy軸方向の半径
     * @param xAxisRotation 楕円の回転角度
     * @param largeArcFlag 弧の選択 180度以上の弧か
     * @param sweepFlag 弧の選択 始点から時計回りか
     * @param x 弧の終点のx座標
     * @param y 弧の終点のy座標
     */
    void addPathArc(double mx, double my, double rx, double ry, double xAxisRotation,
                    boolean largeArcFlag, boolean sweepFlag, double x, double y);

    /**
     * 多角形の追加
     * @param x 始点のx座標
     * @param y 始点のy座標
     * @param pointList 以降の点の座標 x座標, y座標, x座標 ... の繰り返し
     */
    void addPolygon(double x, double y, List<Double> pointList);

    /**
     * 連続直線の追加
     * @param x 始点のx座標
     * @param y 始点のy座標
     * @param pointList 以降の点の座標 x座標, y座標, x座標 ... の繰り返し
     */
    void addPolyline(double x, double y, List<Double> pointList);

    /**
     * 長方形の追加
     * @param x 始点のx座標
     * @param y 始点のy座標
     * @param width 幅
     * @param height 高さ
     */
    void addRect(double x, double y, double width, double height);

    /**
     * 文字列の追加
     * @param x 始点のx座標
     * @param y 始点のy座標
     * @param str 表示する文字列
     */
    void addText(double x, double y, String str);
}
