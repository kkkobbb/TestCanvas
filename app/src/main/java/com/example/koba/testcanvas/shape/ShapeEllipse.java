package com.example.koba.testcanvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

class ShapeEllipse extends ShapeBase {
    private float cx;
    private float cy;
    private float rx;
    private float ry;

    ShapeEllipse(float x, float y, Paint paint) {
        super(paint);

        this.cx = x;
        this.cy = y;
        this.rx = 1;
        this.ry = 1;
    }

    /**
     * 複製用 （privateメンバの指定）
     * @param cx 同名メンバ変数
     * @param cy 同名メンバ変数
     * @param rx 同名メンバ変数
     * @param ry 同名メンバ変数
     * @param paint 同名メンバ変数
     */
    private ShapeEllipse(float cx, float cy, float rx, float ry, Paint paint) {
        super(new Paint(paint));

        this.cx = cx;
        this.cy = cy;
        this.rx = rx;
        this.ry = ry;
    }

    @Override
    protected float getX() {
        return cx;
    }

    @Override
    protected float getY() {
        return cy;
    }

    @Override
    void makeSvg(ISvgWriter svg) {
        svg.setStrokeColor(getPaint().getColor());
        svg.setStrokeWidth(getPaint().getStrokeWidth());
        svg.addEllipse(cx, cy, rx, ry);
    }

    @Override
    void draw(Canvas canvas) {
        final float x1 = cx - rx;
        final float y1 = cy - ry;
        final float x2 = cx + rx;
        final float y2 = cy + ry;
        canvas.drawOval(x1, y1, x2, y2, getPaint());
    }

    @Override
    void setPoint(float x, float y) {
        rx = Math.abs(this.cx - x);
        ry = Math.abs(this.cy - y);
    }

    @Override
    void transferRelative(float dx, float dy) {
        cx += dx;
        cy += dy;
    }

    @Override
    ShapeBase copyShape() {
        return new ShapeEllipse(cx, cy, rx, ry, getPaint());
    }
}
