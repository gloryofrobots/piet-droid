package com.example.piet_droid;

import java.util.HashMap;

import android.graphics.Paint;

public class PaintPool {
	HashMap<Integer, Paint> mPaints;
	
	public PaintPool() {
		// TODO Auto-generated constructor stub
		mPaints = new HashMap<Integer, Paint>();
	}
	
	public void add(int color) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		paint.setColor(color);
		paint.setStrokeWidth(1);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaints.put(color, paint);
	}
}
