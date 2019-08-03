package com.example.koba.testcanvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

class ShapeCircle extends ShapeBase {
    private float x;
    private float y;
    private float r;
    private String attrId = null;

    ShapeCircle(float x, float y, Paint paint) {
        super(paint);

        this.x = x;
        this.y = y;
        this.r = 1;
    }

    /**
     * 複製用 （privateメンバの指定）
     * @param x 同名メンバ変数
     * @param y 同名メンバ変数
     * @param r 同名メンバ変数
     * @param paint 同名メンバ変数
     */
    private ShapeCircle(float x, float y, float r, Paint paint) {
        super(new Paint(paint));

        this.x = x;
        this.y = y;
        this.r = r;
    }

    /**
     * SVGの属性用
     * @param x 同名メンバ変数
     * @param y 同名メンバ変数
     * @param r 同名メンバ変数
     * @param paint 同名メンバ変数
     * @return 新しいインスタンス
     */
    static ShapeCircle newFromSvg(double x, double y, double r, Paint paint) {
        if (paint == null)
            return null;

        return  new ShapeCircle((float)x, (float)y, (float)r, paint);
    }

    @Override
    protected float getX() {
        return x;
    }

    @Override
    protected float getY() {
        return y;
    }

    @Override
    void makeSvg(ISvgWriter svg) {
        svg.setStrokeColor(getPaint().getColor());
        svg.setStrokeWidth(getPaint().getStrokeWidth());
        svg.addCircle(x, y, r);
        svg.setAttrId(attrId);
    }

    @Override
    void draw(Canvas canvas) {
        canvas.drawCircle(x, y, r, getPaint());
    }

    @Override
    void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    @Override
    String getAttrId() {
        return attrId;
    }

    @Override
    void setPoint(float x, float y) {
        // (x, y)と中心との距離がrとなる
        final float distanceSquare = (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y);
        this.r = (float)Math.sqrt(distanceSquare);
    }

    @Override
    void transferRelative(float dx, float dy) {
        x += dx;
        y += dy;
    }

    @Override
    ShapeBase copyShape() {
        return new ShapeCircle(x, y, r, getPaint());
    }
}
