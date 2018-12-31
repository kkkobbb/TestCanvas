package com.example.koba.testcanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import java.util.EventListener;

/**
 * onDraw()時のイベントリスナーを追加したView
 */
public class DrawingView extends View {
    OnDrawListener onDrawListener;

    public DrawingView(Context context) {
        super(context);
        init();
    }
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        onDrawListener = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (onDrawListener != null)
            onDrawListener.onDraw(canvas);
    }

    void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    interface OnDrawListener extends EventListener {
        void onDraw(Canvas canvas);
    }
}


