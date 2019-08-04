package com.example.koba.testcanvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

class ShapePolygon extends ShapeBase {
    private float x;
    private float y;
    private ArrayList<Float> pointList;

    @SuppressWarnings("unused")  // ShapeManagerでリフレクションによってのみ呼び出される
    ShapePolygon(float x, float y, Paint paint) {
        super(paint);

        this.x = x;
        this.y = y;
        pointList = new ArrayList<>();
        pointList.add(x);
        pointList.add(y);
    }

    /**
     * 複製用 （privateメンバの指定）
     * @param x 同名メンバ変数
     * @param y 同名メンバ変数
     * @param pointList 同名メンバ変数
     * @param paint 同名メンバ変数
     */
    private ShapePolygon(float x, float y, List<Float> pointList, Paint paint) {
        super(new Paint(paint));

        this.x = x;
        this.y = y;
        this.pointList = new ArrayList<>(pointList);
    }

    /**
     * SVGの属性用
     * @param points 座標のリスト
     * @param paint 同名メンバ変数
     * @return 新しいインスタンス
     */
    static ShapePolygon newFromSvg(List<Double> points, Paint paint) {
        if (paint == null)
            return null;
        // 線が描けないpointsの場合、何もしない
        final int pointsSize = points.size();
        if (pointsSize < 2 || pointsSize % 2 != 0)
            return null;

        final ArrayList<Float> pointList = new ArrayList<>();
        final float x = points.remove(0).floatValue();
        final float y = points.remove(0).floatValue();
        for (double p : points)
            pointList.add((float)p);
        return new ShapePolygon(x, y, pointList, paint);
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
        // cast List<Float> to List<Double>
        final List<Double> dList = new ArrayList<>();
        for(double point : pointList)
            dList.add(point);
        svg.addPolygon(x, y, dList);
        svg.setAttrId(getAttrId());
    }

    @Override
    void draw(Canvas canvas) {
        final Path path = new Path();
        path.moveTo(x, y);
        for(int i = 0; i < pointList.size(); i += 2)
            path.lineTo(pointList.get(i), pointList.get(i + 1));
        path.close();
        canvas.drawPath(path, getPaint());
    }

    @Override
    void setPoint(float x, float y) {
        final int lastIndex = pointList.size() - 1;
        pointList.set(lastIndex - 1, x);
        pointList.set(lastIndex, y);
    }

    @Override
    void addPoint(float x, float y) {
        pointList.add(x);
        pointList.add(y);
    }

    @Override
    void transferRelative(float dx, float dy) {
        x += dx;
        y += dy;
        for (int i = 0; i < pointList.size(); i += 2)
        {
            pointList.set(i, pointList.get(i) + dx);
            pointList.set(i + 1, pointList.get(i + 1) + dy);
        }
    }

    @Override
    ShapeBase copyShape() {
        return new ShapePolygon(x, y, pointList, getPaint());
    }
}
