package com.example.koba.testcanvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

class ShapeRect extends ShapeBase {
    private float x1;
    private float y1;
    private float x2;
    private float y2;

    ShapeRect(float x, float y, Paint paint) {
        super(paint);

        this.x1 = x;
        this.y1 = y;
        this.x2 = x;
        this.y2 = y;
    }

    /**
     * 複製用 （privateメンバの指定）
     * @param x1 同名メンバ変数
     * @param y1 同名メンバ変数
     * @param x2 同名メンバ変数
     * @param y2 同名メンバ変数
     * @param paint 同名メンバ変数
     */
    private ShapeRect(float x1, float y1, float x2, float y2, Paint paint) {
        super(new Paint(paint));

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }


    @Override
    protected float getX() {
        return x1;
    }

    @Override
    protected float getY() {
        return y1;
    }

    @Override
    void makeSvg(ISvgWriter svg) {
        svg.setStrokeColor(getPaint().getColor());
        svg.setStrokeWidth(getPaint().getStrokeWidth());
        final double x = Math.min(x1, x2);
        final double y = Math.min(y1, y2);
        final double width = Math.abs(x2 - x1);
        final double height = Math.abs(y2 - y1);
        svg.addRect(x, y, width, height);
    }

    @Override
    void draw(Canvas canvas) {
        canvas.drawRect(x1, y1, x2, y2, getPaint());
    }

    @Override
    void setPoint(float x, float y) {
        this.x2 = x;
        this.y2 = y;
    }

    @Override
    void transferRelative(float dx, float dy) {
        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
    }

    @Override
    ShapeBase copyShape() {
        return new ShapeRect(x1, y1, x2, y2, getPaint());
    }
}
