package com.example.koba.testcanvas.shape;

import java.io.Reader;

/**
 * SVG読み込み 簡易版
 */
class TinySvgReader implements ISvgReader {
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

    @Override
    public boolean read(Reader reader) {
        // TODO 未実装
        return false;
    }

    @Override
    public double getSvgSizeWidth() {
        // TODO 未実装
        return 0;
    }

    @Override
    public double getSvgSizeHeight() {
        // TODO 未実装
        return 0;
    }

    @Override
    public int getStrokeColor() {
        // TODO 未実装
        return 0;
    }

    @Override
    public float getStrokeWidth() {
        // TODO 未実装
        return 0;
    }

    @Override
    public float getFontSize() {
        // TODO 未実装
        return 0;
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
