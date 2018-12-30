package com.example.koba.testcanvas.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.example.koba.testcanvas.R;

import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * ShapeBase継承クラスの管理 (生成、削除等)
 */
public class ShapeManager {
    private List<ShapeCreator> shapeCreatorList;
    private int selectedShape;
    @ColorInt private final static int DEFAULT_COLOR = 0xffff00ff;
    @ColorInt private final static int UNDO_COLOR = 0x20000000;
    @ColorInt private final static int HIGHLIGHT_COLOR = DEFAULT_COLOR;
    @ColorInt private final static int NO_HIGHLIGHT_COLOR = 0x30ff00ff;
    private final static float DEFAULT_STROKE_WIDTH = 20;
    private final static float DEFAULT_TEXT_SIZE = 60;
    private Paint paint;
    private Paint textPaint;

    private double width = -1;
    private double height = -1;
    private LinkedList<ShapeBase> shapeList;  // 書いた順に格納する
    private LinkedList<ShapeBase> undoList;  // 戻した順に格納する
    /**
     * 図形の描画を継続中の場合、真
     */
    private boolean drawing = false;

    private float baseX = 0;  // 移動量の基準位置 x座標
    private float baseY = 0;  // 移動量の基準位置 y座標

    private static final String BUNDLE_KEY_SHAPELIST = "ShapeManagerShapeList";
    private static final String BUNDLE_KEY_UNDOLIST = "ShapeManagerUndoList";
    private static final String BUNDLE_KEY_DRAWING = "ShapeManagerDrawing";

    // 文字列設定用イベントリスナー
    private OnSetTextListener onSetTextListener;

    public ShapeManager() {
        shapeCreatorList = new ArrayList<>();
        // ボタン表示順
        shapeCreatorList.add(new ShapeCreator(R.string.shape_line, ShapeLine.class, false));
        shapeCreatorList.add(new ShapeCreator(R.string.shape_rect, ShapeRect.class, false));
        shapeCreatorList.add(new ShapeCreator(R.string.shape_circle, ShapeCircle.class, false));
        shapeCreatorList.add(new ShapeCreator(R.string.shape_arc, ShapeArc.class, true));
        shapeCreatorList.add(new ShapeCreator(R.string.shape_ellipse, ShapeEllipse.class, false));
        shapeCreatorList.add(new ShapeCreator(R.string.shape_polyline, ShapePolyline.class, true));
        shapeCreatorList.add(new ShapeCreator(R.string.shape_polygon, ShapePolygon.class, true));
        shapeCreatorList.add(new ShapeCreator(R.string.shape_text, ShapeText.class, false,
                new ShapeCreator.OnCreatedListener() {
                    @Override
                    public void onCreated(final ShapeBase shape) {
                        shape.setPaint(textPaint);  // 文字列表示用のPaintに変更する
                        // テキスト取得イベント呼び出し
                        if (onSetTextListener != null)
                            onSetTextListener.onSetText();
                    }
                }));
        selectedShape = 0;

        paint = new Paint();
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setColor(DEFAULT_COLOR);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(DEFAULT_COLOR);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(DEFAULT_TEXT_SIZE);

        shapeList = new LinkedList<>();
        undoList = new LinkedList<>();
    }

    /**
     * 状態保存する
     * @param outState 状態保存先
     */
    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(BUNDLE_KEY_SHAPELIST, shapeList);
        outState.putSerializable(BUNDLE_KEY_UNDOLIST, undoList);
        outState.putBoolean(BUNDLE_KEY_DRAWING, drawing);
    }

    /**
     * 状態保存した値を読み込む
     * @param savedInstanceState 状態保存先
     */
    @SuppressWarnings("unchecked")  // ジェネリック型(LinkedList<>)へのキャスト (回避不可)
    public void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            return;

        shapeList = (LinkedList<ShapeBase>) savedInstanceState.getSerializable(BUNDLE_KEY_SHAPELIST);
        undoList = (LinkedList<ShapeBase>) savedInstanceState.getSerializable(BUNDLE_KEY_UNDOLIST);
        drawing = savedInstanceState.getBoolean(BUNDLE_KEY_DRAWING);
    }

    public void setShape(int n) {
        if (n < 0 || shapeCreatorList.size() <= n)
            return;
        selectedShape = n;
    }

    public List<Integer> getShapeIdList() {
        ArrayList<Integer> shapeIdList = new ArrayList<>();
        for (ShapeCreator sc : shapeCreatorList)
            shapeIdList.add(sc.id);

        return shapeIdList;
    }

    /**
     * 文字列取得イベント設定
     * @param onSetTextListener 文字列を設定するイベントリスナー
     */
    public void setOnSetTextListener(OnSetTextListener onSetTextListener) {
        this.onSetTextListener = onSetTextListener;
    }

    /**
     * 図形に文字列を設定する <br>
     * 適切な図形でない場合は無視する
     * @param text 図形に設定する文字列
     */
    public void setText(String text) {
        if (shapeList.isEmpty())
            return;
        shapeList.getLast().setData(text);
    }

    /**
     * 図形の描画開始
     * @param x 描画する位置のx座標
     * @param y 描画する位置のy座標
     */
    public void start(float x, float y) {
        final ShapeCreator shapeCreator = shapeCreatorList.get(selectedShape);
        final Class<? extends ShapeBase> clazz = shapeCreator.clazz;
        if (drawing && !shapeList.isEmpty() && shapeList.getLast().getClass() == clazz) {
            // 現在の図形の描画を続ける
            shapeList.getLast().addPoint(x, y);
            return;
        }

        final ShapeBase shape = shapeCreator.create(x, y, paint);
        if (shape == null)
            return;  // 生成に失敗した場合、何もしない

        drawing = shapeCreator.drawing;  // 次回のstart()時も描画を続行する場合、真

        shapeList.addLast(shape);
        undoList.clear();  // 履歴を削除
    }

    /**
     * 図形の変更
     * @param x 変更先の位置のx座標
     * @param y 変更先の位置のy座標
     */
    public void move(float x, float y) {
        if (shapeList.isEmpty())
            return;
        shapeList.getLast().setPoint(x, y);
    }

    /**
     * 図形描画完了の明示的指定
     */
    public void fix() {
        drawing = false;
    }

    /**
     * 図形の移動 基準位置を決める
     * @param x 移動量の基準位置 x座標
     * @param y 移動量の基準位置 y座標
     */
    public void preTransfer(float x, float y) {
        baseX = x;
        baseY = y;
    }

    /**
     * 図形の移動 基準位置からの移動量分を移動する
     * @param x 移動量を決めるためのx座標
     * @param y 移動量を決めるためのy座標
     */
    public void transfer(float x, float y) {
        if (shapeList.isEmpty())
            return;
        final float dx = x - baseX;
        final float dy = y - baseY;
        shapeList.getLast().transferRelative(dx, dy);

        baseX = x;
        baseY = y;
    }

    /**
     * 最新の図形と同じ図形を指定の位置に複製する
     * @param x 複製先のx座標
     * @param y 複製先のx座標
     */
    public void copy(float x, float y) {
        if (shapeList.isEmpty())
            return;

        final ShapeBase cpShape = shapeList.getLast().copyShape();
        cpShape.transferAbsolute(x, y);
        shapeList.addLast(cpShape);
        // 履歴は削除しない
    }

    /**
     * 作成した図形を描画する
     * @param canvas 描画先
     */
    public void drawShapes(Canvas canvas) {
        for (ShapeBase shape : shapeList)
            shape.draw(canvas);
    }

    /**
     * 一時的な色で図形を描画する
     * @param canvas 描画先
     * @param shape 描画する図形
     * @param tempColor 一時的な色
     */
    private void drawShapeTempColor(Canvas canvas, ShapeBase shape, @ColorInt int tempColor) {
        @ColorInt final int color = shape.getPaint().getColor();
        shape.getPaint().setColor(tempColor);
        shape.draw(canvas);
        shape.getPaint().setColor(color);
    }

    /**
     * 最新の図形を強調する
     * （最新の図形以外は薄く表示）
     * @param canvas 描画先
     */
    public void drawShapesLastHighlight(Canvas canvas) {
        if (shapeList.isEmpty())
            return;
        final ShapeBase last = shapeList.getLast();
        drawShapeTempColor(canvas, last, HIGHLIGHT_COLOR);

        final int lastNum = shapeList.size() - 1;  // 最後の要素を除く
        if (lastNum <= 0)
            return;
        final List<ShapeBase> otherList = shapeList.subList(0, lastNum);
        for (ShapeBase shape : otherList)
            drawShapeTempColor(canvas, shape, NO_HIGHLIGHT_COLOR);
    }

    /**
     * undoした図形を透過値を変更して描画する
     * @param canvas 描画先
     */
    public void drawUndo(Canvas canvas) {
        // 各図形の透過値を変更して描画後元に戻す
        for (ShapeBase shape : undoList)
            drawShapeTempColor(canvas, shape, UNDO_COLOR);
    }

    public boolean undo() {
        fix();

        if (shapeList.isEmpty())
            return false;

        final ShapeBase last = shapeList.removeLast();
        undoList.addLast(last);
        return true;
    }

    public boolean redo() {
        fix();

        if (undoList.isEmpty())
            return false;

        final ShapeBase last = undoList.removeLast();
        shapeList.addLast(last);
        return true;
    }

    public boolean canUndo() {
        return !shapeList.isEmpty();
    }

    public boolean canRedo() {
        return !undoList.isEmpty();
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * SVGとして出力する
     * @param writer 出力先
     * @return 出力に成功した場合、真
     */
    public boolean writeTo(Writer writer) {
        ISvgWriter svg;
        try {
            svg = new TinySvg();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        }

        if (width >= 0 && height >= 0)
            svg.setSvgSize(width, height);

        for (ShapeBase shape : shapeList)
            shape.makeSvg(svg);

        return svg.writeTo(writer);
    }

    /**
     * ShapeBase系クラスのインスタンス生成補助
     */
    private static class ShapeCreator {
        final int id;
        final Class<? extends ShapeBase> clazz;
        final boolean drawing;
        private final OnCreatedListener onCreatedListener;

        ShapeCreator(int id, Class<? extends ShapeBase> clazz, boolean drawing) {
            this(id, clazz, drawing, null);
        }

        ShapeCreator(int id, Class<? extends ShapeBase> clazz, boolean drawing, OnCreatedListener onCreatedListener) {
            this.id = id;
            this.clazz = clazz;
            this.drawing = drawing;
            this.onCreatedListener = onCreatedListener;
        }

        ShapeBase create(float x, float y, Paint paint) {
            ShapeBase shape;
            try {
                // コンストラクタ呼び出し
                final Constructor<? extends ShapeBase> c = clazz.getDeclaredConstructor(float.class, float.class, Paint.class);
                shape = c.newInstance(x, y, paint);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }

            if (onCreatedListener != null)
                onCreatedListener.onCreated(shape);

            return shape;
        }

        interface OnCreatedListener extends EventListener {
            void onCreated(ShapeBase shape);
        }
    }

    /**
     * テキスト取得時のイベント
     * テキストの設定はsetText()を呼び出して行うこと
     */
    public interface OnSetTextListener extends EventListener {
        void onSetText();
    }
}
