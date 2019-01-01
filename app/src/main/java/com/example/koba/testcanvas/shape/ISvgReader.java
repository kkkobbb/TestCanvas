package com.example.koba.testcanvas.shape;

import java.io.Reader;
import java.util.EventListener;
import java.util.List;

interface ISvgReader {
    /**
     * SVG読み込み
     * @param reader 入力元
     * @return 成否
     */
    boolean read(Reader reader);

    /**
     * @return 画像全体のサイズ 幅
     */
    double getSvgSizeWidth();

    /**
     * @return 画像全体のサイズ 高さ
     */
    double getSvgSizeHeight();

    /**
     * @return 線の色
     */
    int getStrokeColor();

    /**
     * @return 線の太さ
     */
    float getStrokeWidth();

    /**
     * @return 文字の大きさ
     */
    float getFontSize();

    /**
     * 円読み込み時のイベントを設定する
     * @param listener 円読み込み時のイベント
     */
    void setOnCircleListener(OnCircleListener listener);

    /**
     * 楕円読み込み時のイベントを設定する
     * @param listener 楕円読み込み時のイベント
     */
    void setOnEllipseListener(OnEllipseListener listener);

    /**
     * 直線読み込み時のイベントを設定する
     * @param listener 直線読み込み時のイベント
     */
    void setOnLineListener(OnLineListener listener);

    /**
     * パス（円弧1つ分）読み込み時のイベントを設定する
     * @param listener パス（円弧1つ分）読み込み時のイベント
     */
    void setOnPathArcListener(OnPathArcListener listener);

    /**
     * 多角形読み込み時のイベントを設定する
     * @param listener 多角形読み込み時のイベント
     */
    void setOnPolygonListener(OnPolygonListener listener);

    /**
     * 連続直線読み込み時のイベントを設定する
     * @param listener 連続直線読み込み時のイベント
     */
    void setOnPolylineListener(OnPolylineListener listener);

    /**
     * 長方形読み込み時のイベントを設定する
     * @param listener 長方形読み込み時のイベント
     */
    void setOnRectListener(OnRectListener listener);

    /**
     * 文字列読み込み時のイベントを設定する
     * @param listener 文字列読み込み時のイベント
     */
    void setOnTextListener(OnTextListener listener);

    interface OnCircleListener extends EventListener {
        /**
         * 円の読み込み
         * @param svg svgデータ
         * @param cx 中心のx座標
         * @param cy 中心のy座標
         * @param r 半径
         */
        void onCircle(ISvgReader svg, double cx, double cy, double r);
    }

    interface OnEllipseListener extends EventListener {
        /**
         * 楕円の読み込み
         * @param svg svgデータ
         * @param cx 中心のx座標
         * @param cy 中心のy座標
         * @param rx x軸方向の半径
         * @param ry y軸方向の半径
         */
        void onEllipse(ISvgReader svg, double cx, double cy, double rx, double ry);
    }

    interface OnLineListener extends EventListener {
        /**
         * 直線の読み込み
         * @param svg svgデータ
         * @param x1 始点のx座標
         * @param y1 始点のy座標
         * @param x2 終点のx座標
         * @param y2 終点のy座標
         */
        void onLine(ISvgReader svg, double x1, double y1, double x2, double y2);
    }

    interface OnPathArcListener extends EventListener {
        /**
         * パス（円弧1つ分）の読み込み
         * @param svg svgデータ
         * @param x1 弧の始点のx座標
         * @param y1 弧の始点のy座標
         * @param x2 弧の終点のx座標
         * @param y2 弧の終点のy座標
         * @param rx 弧のx軸方向の半径
         * @param ry 弧のy軸方向の半径
         * @param xAxisRotation 楕円の回転角度
         * @param largeArcFlag 弧の選択 180度以上の弧か
         * @param sweepFlag 弧の選択 始点から時計回りか
         */
        void onPathArc(ISvgReader svg, double x1, double y1, double x2, double y2,
                       double rx, double ry, double xAxisRotation,
                       boolean largeArcFlag, boolean sweepFlag);
    }

    interface OnPolygonListener extends EventListener {
        /**
         * 多角形の読み込み
         * @param svg svgデータ
         * @param x 始点のx座標
         * @param y 始点のy座標
         * @param pointList 以降の点の座標 x座標, y座標, x座標 ... の繰り返し
         */
        void onPolygon(ISvgReader svg, double x, double y, List<Double> pointList);

    }

    interface OnPolylineListener extends EventListener {
        /**
         * 連続直線の読み込み
         * @param svg svgデータ
         * @param x 始点のx座標
         * @param y 始点のy座標
         * @param pointList 以降の点の座標 x座標, y座標, x座標 ... の繰り返し
         */
        void onPolyline(ISvgReader svg, double x, double y, List<Double> pointList);

    }

    interface OnRectListener extends EventListener {
        /**
         * 長方形の読み込み
         * @param svg svgデータ
         * @param x 始点のx座標
         * @param y 始点のy座標
         * @param width 幅
         * @param height 高さ
         */
        void onRect(ISvgReader svg, double x, double y, double width, double height);

    }

    interface OnTextListener extends EventListener {
        /**
         * 文字列の読み込み
         * @param x 始点のx座標
         * @param y 始点のy座標
         * @param str 表示する文字列
         */
        void onText(ISvgReader svg, double x, double y, String str);
    }
}
