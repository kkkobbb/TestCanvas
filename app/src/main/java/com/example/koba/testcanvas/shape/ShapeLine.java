package com.example.koba.testcanvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

class ShapeLine extends ShapeBase {
    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private String attrId = null;

    ShapeLine(float x, float y, Paint paint) {
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
    private ShapeLine(float x1, float y1, float x2, float y2, Paint paint) {
        super(new Paint(paint));

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * SVGの属性用
     * @param x1 同名メンバ変数
     * @param y1 同名メンバ変数
     * @param x2 同名メンバ変数
     * @param y2 同名メンバ変数
     * @param paint 同名メンバ変数
     * @return 新しいインスタンス
     */
    static ShapeLine newFromSvg(double x1, double y1, double x2, double y2, Paint paint) {
        if (paint == null)
            return null;

        return new ShapeLine((float)x1, (float)y1, (float)x2, (float)y2, paint);
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
        svg.addLine(x1, y1, x2, y2);
        svg.setAttrId(attrId);
    }

    @Override
    void draw(Canvas canvas) {
        canvas.drawLine(x1, y1, x2, y2, getPaint());
    }

    @Override
    void setAttrId(String attrId) {
        this.attrId = attrId;
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
    void transferAbsolute(float x, float y) {
        final float dx = x - this.x1;
        final float dy = y - this.y1;

        transferRelative(dx, dy);
    }

    @Override
    ShapeBase copyShape() {
        return new ShapeLine(x1, y1, x2, y2, getPaint());
    }
}
