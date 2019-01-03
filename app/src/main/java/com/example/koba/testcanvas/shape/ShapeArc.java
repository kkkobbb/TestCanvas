package com.example.koba.testcanvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;


class ShapeArc extends ShapeBase {
    // 2つの状態を持ち、各状態で操作が変わる
    // 1. 端点2つを決定する （最初のみ）
    // 2. 弧のサイズを変更する　（2回目以降）

    // canvas描画用
    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private float startAngle;  // 度: 0 - 360
    private float sweepAngle;  // 度: 0 - 360
    private State state;
    // svg出力用
    private float startX;  // 弧の末端1 x座標
    private float startY;  // 弧の末端1 y座標
    private float endX;  // 弧の末端2 x座標
    private float endY;  // 弧の末端2 y座標
    private boolean largeArcFlag;
    private boolean sweepFlag;

    ShapeArc(float x, float y, Paint paint) {
        super(paint);

        startX = x;
        startY = y;
        changePoint(x, y);
        startAngle = 90;
        sweepAngle = 180;
        state = State.CHANGE_POINT;

        largeArcFlag = true;
        sweepFlag = false;
    }

    /**
     * 複製用 （privateメンバの指定）
     * @param x1 同名メンバ変数
     * @param y1 同名メンバ変数
     * @param x2 同名メンバ変数
     * @param y2 同名メンバ変数
     * @param startAngle 同名メンバ変数
     * @param sweepAngle 同名メンバ変数
     * @param startX 同名メンバ変数
     * @param startY 同名メンバ変数
     * @param endX 同名メンバ変数
     * @param endY 同名メンバ変数
     * @param largeArcFlag 同名メンバ変数
     * @param sweepFlag 同名メンバ変数
     * @param paint 同名メンバ変数
     */
    private ShapeArc(float x1, float y1, float x2, float y2, float startAngle, float sweepAngle,
                     float startX, float startY, float endX, float endY,
                     boolean largeArcFlag, boolean sweepFlag, Paint paint) {
        super(new Paint(paint));

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.startAngle = startAngle;
        this.sweepAngle = sweepAngle;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.largeArcFlag = largeArcFlag;
        this.sweepFlag = sweepFlag;
        state = State.CHANGE_CIRCLE;
    }

    /**
     * SVGの属性用
     * @param mx startX
     * @param my startY
     * @param x endX
     * @param y endY
     * @param r 半径
     * @param largeArcFlag 同名メンバ変数
     * @param sweepFlag 同名メンバ変数
     * @param paint 同名メンバ変数
     */
    private ShapeArc(double mx, double my, double r,
                     boolean largeArcFlag, boolean sweepFlag,
                     double x, double y, Paint paint) {
        super(paint);
        // 点P(mx, my) 点R(x, y)とする
        // 線分PQの中点を点Iとする
        // 点Iを通る線分PQの垂線を垂線Sとする
        // 円C(半径r、点P、Rを通る)上の点を点Qとする (円Cの中心を点Cとする)
        // 点Qを求めて3点目を指定することでcanvas描画用の変数を設定する

        startX = (float)mx;
        startY = (float)my;
        changePoint((float)x, (float)y);

        final double a = getPerpendicular(mx, my, x, y).v1;  // 垂線Sの傾き
        final double ix = (mx + x) / 2;  // 点Iのx座標
        final double iy = (my + y) / 2;  // 点Iのy座標
        final double pi2 = getDistance2(mx, my, ix, iy);  // 線分PIの長さの2乗
        final double ci = Math.sqrt(Math.pow(r, 2) - pi2);  // 線分CIの長さ

        final double qi;  // 線分QIの長さ
        if (largeArcFlag)
            qi = r + ci;
        else
            qi = r - ci;

        final double qx;  // 点Qのx座標
        final double qy;  // 点Qのy座標
        if (a == 0) {  // 垂線Sがx軸と平行
            // 弧の向きによる符号反転
            final double signDiff = ((mx >= x && sweepFlag) || (mx < x && !sweepFlag)) ? 1 : -1;
            qx = ix;
            qy = iy + signDiff * qi;
        } else {
            // 弧の向きによる符号反転
            final double signDiff = ((my >= y && sweepFlag) || (my < y && !sweepFlag)) ? -1 : 1;
            if (Double.isInfinite(a)) {  // 垂線Sがy軸と平行
                qx = ix + signDiff * qi;
                qy = iy;
            } else {  // 垂線Sが y=ax+b (a!=0) で表せる
                final double cos2A = 1 / (Math.pow(a, 2) + 1);
                final double cosA = Math.sqrt(cos2A);  // 垂線とx軸のなす角
                final double sinA = Math.sqrt(1 - cos2A);  // 垂線とx軸のなす角
                final double dqx = qi * cosA;  // 点Qと点Iの差分 (x座標)
                final double dqy = qi * sinA;  // 点Qと点Iの差分 (y座標)
                final double signXY = Math.signum(a);  // qx qy での差分の符号反転
                qx = ix + signDiff * dqx;
                qy = iy + signDiff * signXY * dqy;
            }
        }

        addPoint((float)qx, (float)qy);
    }

    /**
     * SVGの属性用
     * @param mx startX
     * @param my startY
     * @param x endX
     * @param y endY
     * @param rx メンバ変数に対応なし
     * @param ry メンバ変数に対応なし
     * @param xAxisRotation 値に関係なく0とみなす
     * @param largeArcFlag 同名メンバ変数
     * @param sweepFlag 同名メンバ変数
     * @param paint 同名メンバ変数
     */
    static ShapeArc newFromSvg(double mx, double my, double rx, double ry, double xAxisRotation,
                               boolean largeArcFlag, boolean sweepFlag,
                               double x, double y, Paint paint) {
        // 誤差の許容範囲は適当 (もっと厳しくしてもいいかも)
        if (paint == null)
            return null;
        if (xAxisRotation != 0)  // 回転に未対応
            return null;
        if (Math.abs(rx - ry) > 0.01)  // 真円以外に未対応 (誤差の許容)
            return null;

        // 半円の場合、(float, float, paint)コンストラクタを使う
        final double pointsDistance2 = Math.pow(mx - x, 2) + Math.pow(my - y, 2);
        final double dia = Math.pow(rx * 2, 2);  // 直径の2乗
        if (Math.abs(pointsDistance2 - dia) < 0.01) {  // 誤差の許容
            final ShapeArc shape = new ShapeArc((float)mx, (float)my, paint);
            shape.changePoint((float)x, (float)y);
            return shape;
        }

        return new ShapeArc(mx, my, rx, largeArcFlag, sweepFlag, x, y, paint);
    }

    @Override
    protected float getX() {
        // 円の中心を返す
        return (x1 + x2) / 2;
    }

    @Override
    protected float getY() {
        // 円の中心を返す
        return (y1 + y2) / 2;
    }

    @Override
    void makeSvg(ISvgWriter svg) {
        svg.setStrokeColor(getPaint().getColor());
        svg.setStrokeWidth(getPaint().getStrokeWidth());

        final double rx = Math.abs(x1 - x2) / 2;
        final double ry = Math.abs(y1 - y2) / 2;
        svg.addPathArc(startX, startY, rx, ry, 0, largeArcFlag, sweepFlag, endX, endY);
    }

    @Override
    void draw(Canvas canvas) {
        canvas.drawArc(x1, y1, x2, y2, startAngle, sweepAngle, false, getPaint());
    }

    @Override
    void setPoint(float x, float y) {
        switch (state) {
            case CHANGE_POINT:
                changePoint(x, y);
                break;
            case CHANGE_CIRCLE:
                changeCircle(x, y);
                break;
        }
    }

    /**
     * 端点を変更し、合わせて他のパラメータも変更する
     * @param x 変更後の端点のx座標
     * @param y 変更後の端点のy座標
     */
    private void changePoint(float x, float y) {
        // (startX, startY) (x, y)間を直径とする円を円Aとする

        // r: 円Aの半径
        final double distanceSquare = getDistance2(startX, startY, x, y);
        final double r = Math.sqrt(distanceSquare) / 2;

        // 円Aの中心の座標
        final float cx = (startX + x) / 2;
        final float cy = (startY + y) / 2;

        // 円Aが内接円となる長方形
        x1 = (float)(cx - r);
        y1 = (float)(cy - r);
        x2 = (float)(cx + r);
        y2 = (float)(cy + r);

        // startAngle: 円Aの中心から(x, y)に伸びる直線のx軸に対する角度
        if (r == 0)
            return;
        double rad = Math.acos((x - cx) / r);
        if (cy > y)
            rad = 2 * Math.PI - rad;
        startAngle = (float)Math.toDegrees(rad);

        // svg出力用に端点を保存する
        endX = x;
        endY = y;
    }

    /**
     * 2つの端点に対する円を変更する
     * @param x 端点以外の円上の点のx座標
     * @param y 端点以外の円上の点のy座標
     */
    private void changeCircle(float x, float y) {
        // 点P(startX, startY) 点Q(x, y) 点R(endX, endY) とする

        // 点P、点Q、点Rの3点を通る円Aの中心
        final Tuple<Double> center = getCenter(startX, startY, x, y, endX, endY);
        final double cx = center.v1;
        final double cy = center.v2;

        // 円Aの半径
        final double r2 = getDistance2(cx, cy, startX, startY);
        final double r = Math.sqrt(r2);

        // 円Aが内接円となる長方形
        x1 = (float)(cx - r);
        y1 = (float)(cy - r);
        x2 = (float)(cx + r);
        y2 = (float)(cy + r);

        // 円Aの中心から各端点に伸びる直線のx軸に対する角度を求める
        if (r == 0)
            return;
        double radP = Math.acos((startX - cx) / r);
        if (cy > startY)
            radP = 2 * Math.PI - radP;
        double radQ = Math.acos((x - cx) / r);
        if (cy > y)
            radQ = 2 * Math.PI - radQ;
        double radR = Math.acos((endX - cx) / r);
        if (cy > endY)
            radR = 2 * Math.PI - radR;

        // 弧を描画する角度を求める
        if ((radP <= radQ && radQ <= radR) || (radR <= radQ && radQ <= radP)) {
            startAngle = (float) Math.toDegrees(radP);
            sweepAngle = (float) Math.toDegrees(radR - radP);
        } else if (radR > radP) {
            startAngle = (float) Math.toDegrees(radR);
            sweepAngle = (float) Math.toDegrees(2 * Math.PI - (radR - radP));
        } else {
            startAngle = (float) Math.toDegrees(radP);
            sweepAngle = (float) Math.toDegrees(2 * Math.PI - (radP - radR));
        }

        // svg用のフラグを設定する
        final double diffPQ = getRadDiff(radP, radQ);  // 弧PQの角度
        final double diffPR = getRadDiff(radP, radR);  // 弧PRの角度
        sweepFlag = diffPQ <= diffPR;
        largeArcFlag = (diffPR >= Math.PI) ^ !sweepFlag;
    }

    /**
     * 3点PQRが円周上にある円の中心を求める
     * @param px 点Pのx座標
     * @param py 点Pのy座標
     * @param qx 点Qのx座標
     * @param qy 点Qのy座標
     * @param rx 点Rのx座標
     * @param ry 点Rのy座標
     * @return 中心の座標
     */
    private Tuple<Double> getCenter(double px, double py, double qx, double qy, double rx, double ry) {
        final Tuple<Double> pPQ = getPerpendicular(px, py, qx, qy);  // 直線PQの垂線の傾き、切片
        final Tuple<Double> pQR = getPerpendicular(qx, qy, rx, ry);  // 直線QRの垂線の傾き、切片

        if (py == qy) {
            // 一方の垂線がy軸に平行な直線の場合、そのまま代入して中心を求める
            final double x = (px + qx) / 2;
            final double y = pQR.v1 * x + pQR.v2;
            return new Tuple<>(x, y);
        } else if (qy == ry) {
            // 一方の垂線がy軸に平行な直線の場合、そのまま代入して中心を求める
            final double x = (qx + rx) / 2;
            final double y = pPQ.v1 * x + pPQ.v2;
            return new Tuple<>(x, y);
        }

        // 2つの垂線の交点を求める
        return getIntersection(pPQ.v1, pPQ.v2, pQR.v1, pQR.v2);
    }

    /**
     * 線分(px, py) (qx, qy)と中点で交わる垂線の傾きと切片を返す
     * @param px x座標
     * @param py y座標
     * @param qx x座標
     * @param qy y座標
     * @return (傾き, 切片)
     */
    private Tuple<Double> getPerpendicular(double px, double py, double qx, double qy) {
        final double a = (px - qx) / (qy - py);
        final double b = ((py + qy) - (px - qx) * (px + qx) / (qy - py)) / 2;

        return new Tuple<>(a, b);
    }

    /**
     * 直線y=ax+b と直線y=cx+d の交点の座標を返す
     * @param a 傾き
     * @param b 切片
     * @param c 傾き
     * @param d 切片
     * @return 交点の座標
     */
    private Tuple<Double> getIntersection(double a, double b, double c, double d) {
        final double x =  (d - b) / (a - c);
        final double y = (a * d - b * c) / (a - c);

        return new Tuple<>(x, y);
    }

    // 2点間の距離の2乗を返す
    private double getDistance2(double x1, double y1, double x2, double y2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
    }

    /**
     * 2つの角度の差を求める 戻り値は 0以上、2PI以下になる
     * @param rad1 角度1 (radian) 0 <= rad1 <= 2PI
     * @param rad2 角度2 (radian) 0 <= rad2 <= 2PI
     * @return 角度の差
     */
    private double getRadDiff(double rad1, double rad2) {
        double diff = rad2 - rad1;
        if (diff < 0)
            diff += 2 * Math.PI;
        else if (diff > 2 * Math.PI)
            diff -= 2 * Math.PI;
        return diff;
    }

    @Override
    void addPoint(float x, float y) {
        state = State.CHANGE_CIRCLE;
        changeCircle(x, y);
    }

    @Override
    void transferRelative(float dx, float dy) {
        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
        startX += dx;
        startY += dy;
        endX += dx;
        endY += dy;
    }

    @Override
    public ShapeBase copyShape() {
        return new ShapeArc(x1, y1, x2, y2, startAngle, sweepAngle,
                startX, startY, endX, endY, largeArcFlag, sweepFlag, getPaint());
    }

    private enum State {
        /**
         * 端点変更
         */
        CHANGE_POINT,
        /**
         * 弧のサイズ変更
         */
        CHANGE_CIRCLE
    }

    private class Tuple<T> {
        final T v1;
        final T v2;
        Tuple(T v1, T v2) {
            this.v1 = v1;
            this.v2 = v2;
        }
    }
}
