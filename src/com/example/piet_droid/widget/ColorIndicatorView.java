package com.example.piet_droid.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class ColorIndicatorView extends View {
    Paint mPaint;
    Paint mLinePaint;
    Rect mBounds;
    
   
    public ColorIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorIndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mBounds = new Rect();
        mPaint = new Paint();
        mPaint.setColor(0);
        mPaint.setStyle(Paint.Style.FILL);
        
        
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mLinePaint.setStrokeWidth(2);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(mBounds);
        canvas.drawRect(mBounds, mPaint);
        float x = (float) (mBounds.width() / 3) * 2; 
        canvas.drawLine(x, 0.f, x, (float) mBounds.bottom, mLinePaint);
        float y = (float) (mBounds.height() / 3) * 2; 
        canvas.drawLine(0.f, y, (float) mBounds.right, y, mLinePaint);
    }

    /*
     * @Override protected void onMeasure(int widthMeasureSpec, int
     * heightMeasureSpec) { setMeasuredDimension(); }
     */
}
