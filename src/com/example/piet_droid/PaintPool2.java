package com.example.piet_droid;

import android.graphics.Paint;

import com.example.jpiet.CodelColor;

public class PaintPool2 implements PaletteProvider{
	Paint [] mPaint;
	CodelColor [] mColors;
	
	
	public PaintPool2() {
		init();
	}
	
	private void init() {
		int size = CodelColor.values().length;
		mPaint = new Paint[size];
		mColors = new CodelColor[size];
		
		int index = 0;
		
		for(final CodelColor codelColor : CodelColor.values()) {
			int color = (255 << 24) | codelColor.value;
			
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			
			paint.setColor(color);
			paint.setStrokeWidth(1);
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			mPaint[index] = paint;
			mColors[index] = codelColor;
			
			++index;
		}
	}
	
	@Override
	public Paint getPaint(int id) throws PaletteProvider.UnknownColorIdException {
		try{
			return mPaint[id];
		}
		catch(IndexOutOfBoundsException e){
			throw new PaletteProvider.UnknownColorIdException();
		}
		
	}
	
	
}
