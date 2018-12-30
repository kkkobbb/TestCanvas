package com.example.koba.testcanvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.Serializable;

/**
 * 図形情報を表す基底クラス <br>
 * 1. (float, float, Paint)のコンストラクタを作成すること <br>
 * 2. シリアライズ化可能であること
 */
abstract class ShapeBase implements Serializable {
    private transient Paint paint;  // TODO とりあえずPaintはシリアライズ化しない

    ShapeBase(Paint paint) {
        this.paint = new Paint(paint);
    }

    /**
     * 描画時の線の見た目を取得
     * @return 見た目の設定
     */
    final Paint getPaint() {
        return paint;
    }

    /**
     * 描画時の線の見た目を設定
     * @param paint 見た目の設定
     */
    final void setPaint(Paint paint) {
        this.paint = paint;
    }

    /**
     * 図形の代表となる位置のx座標を返す
     * @return x座標
     */
    // どこを代表となる位置とするかは図形ごとに決める
    abstract protected float getX();

    /**
     * 図形の代表となる位置のy座標を返す
     * @return y座標
     */
    // どこを代表となる位置とするかは図形ごとに決める
    abstract protected float getY();

    /**
     * svgの構築
     * @param svg SVG構築先
     */
    abstract void makeSvg(ISvgWriter svg);

    /**
     * 図形の描画
     * @param canvas 描画先
     */
    abstract void draw(Canvas canvas);

    /**
     * 末端の座標の変更
     * @param x 新しい末端のx座標
     * @param y 新しい末端のy座標
     */
    abstract void setPoint(float x, float y);

    /**
     * 新たな点の追加
     * @param x 新しい点のx座標
     * @param y 新しい点のy座標
     */
    void addPoint(float x, float y) {}

    /**
     * 図形の移動 相対位置指定
     * @param dx x軸方向の移動量
     * @param dy y軸方向の移動量
     */
    abstract void transferRelative(float dx, float dy);

    /**
     * 図形の移動 絶対座標指定
     * @param x 移動後のx座標
     * @param y 移動後のy座標
     */
    void transferAbsolute(float x, float y) {
        final float dx = x - getX();
        final float dy = y - getY();

        transferRelative(dx, dy);
    }

    /**
     * 同じ図形を作成する
     * @return 同じ図形のもの
     */
    // 戻り値は同じ見た目の図形を表していれば良く、clone()互換である必要はない（Cloneableではない）
    abstract ShapeBase copyShape();

    /**
     * 追加のデータを設定する
     * @param data 設定するデータ
     */
    void setData(Object data) {}
}
