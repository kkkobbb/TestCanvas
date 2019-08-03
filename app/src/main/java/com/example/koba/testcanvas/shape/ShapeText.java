package com.example.koba.testcanvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

// フォントを指定していないため、アプリ上の表示とSVGでの表示が異なる
class ShapeText extends ShapeBase {
    private float x;
    private float y;
    private String str;
    private String attrId = null;

    ShapeText(float x, float y, Paint paint) {
        super(paint);

        this.x = x;
        this.y = y;
        str = "text";
    }

    /**
     * 複製用 （privateメンバの指定）
     * @param x 同名メンバ変数
     * @param y 同名メンバ変数
     * @param str 同名メンバ変数
     * @param paint 同名メンバ変数
     */
    private ShapeText(float x, float y, String str, Paint paint) {
        super(paint);

        this.x = x;
        this.y = y;
        this.str = str;
    }

    /**
     * SVGの属性用
     * @param x 同名メンバ変数
     * @param y 同名メンバ変数
     * @param str 同名メンバ変数
     * @param paint 同名メンバ変数
     * @return 新しいインスタンス
     */
    static ShapeText newFromSvg(double x, double y, String str, Paint paint) {
        if (paint == null)
            return null;

        return new ShapeText((float)x, (float)y, str, paint);
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
        svg.setFontSize(getPaint().getTextSize());
        svg.addText(x, y, str);
        svg.setAttrId(attrId);
    }

    @Override
    void draw(Canvas canvas) {
        canvas.drawText(str, x, y, getPaint());
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
        // 何もしない
    }

    @Override
    void transferRelative(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    ShapeBase copyShape() {
        return new ShapeText(x, y, str, getPaint());
    }

    @Override
    void setData(Object data) {
        if (!(data instanceof String))
            return;

        str = (String)data;
    }
}
