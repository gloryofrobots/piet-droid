package com.example.piet_droid;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class FilledCircleDrawable extends Drawable {
	Paint mPaint;
	
	public FilledCircleDrawable() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStrokeWidth(1);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	}
	
	@Override
	public void draw(Canvas canvas) {
		Rect bounds = getBounds();
		int px = bounds.left + (bounds.width() / 2);
		int py = bounds.top + (bounds.height() / 2);
		
		int diam = Math.min(bounds.width(), bounds.height());
		int radius = diam / 4;
		
		canvas.drawCircle(px, py, radius, mPaint);
	}
	
	public void setColor(int color) {
		mPaint.setColor(color);
	}
	
	@Override
	public void setAlpha(int alpha) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		throw new UnsupportedOperationException();

	}

	@Override
	public int getOpacity() {
		
		return 0;
	}

}
