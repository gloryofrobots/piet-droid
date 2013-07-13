package com.example.piet_droid;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import android.view.MotionEvent;
import android.view.View;

/*
 * var argb  : int = (alpha<<24)|rgb;
 var rgb   : int = 0xFFFFFF & argb;
 var alpha : int = (argb>>24)&0xFF;

 /*rgb = (rgb << 8) + red;
 rgb = (rgb << 8) + green;
 rgb = (rgb << 8) + blue;
 int rgb = 0xffff00;
 int argb  = (alpha << 24) | rgb;
 * */

public class ColorFieldView extends View {
	public interface CellClickListener{
		public void onCellClick(int x, int y);
	}
	
	private class Cell {
		public int x;
		public int y;
		int color;
		Drawable drawable;
		
		Cell(int x, int y, int color) {
			this.x = x;
			this.y = y;
			this.color = color;
			drawable = null;
		}
	}

	private final int UNKNOWN_MEASURE = -1;
	private final int DEFAULT_WIDTH = 500;
	private final int DEFAULT_HEIGHT = 500;

	private Paint mLinePaint;
	private Paint mCellPaint;


	private int mCellWidth = 1;
	private int mCellHeight = 1;

	private int mCountX;
	private int mCountY;
	
	private int mDefaultColor;

	private CellClickListener mOnCellClickListener;
	
	private boolean mForceDraw;
	private Cell mCellToRedraw;
	
	/**
	 * @param mOnCellClickListener the mOnCellClickListener to set
	 */
	public void setOnCellClickListener(CellClickListener onCellClickListener) {
		mOnCellClickListener = onCellClickListener;
	}

	Cell[][] mCells;

	public ColorFieldView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ColorFieldView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ColorFieldView);
		
		mDefaultColor = a.getColor(R.styleable.ColorFieldView_defaultColor, 0);
		
		mCountX = a.getInt(R.styleable.ColorFieldView_countX, 30);
		mCountY = a.getInt(R.styleable.ColorFieldView_countY, 30);
		
		createCells();
		
		a.recycle();

		init();
	}
	
	private void createCells() {
		mCells = new Cell[mCountY][mCountX];

		for (int y = 0; y < mCountY; y++) {
			for (int x = 0; x < mCountX; x++) {
				mCells[y][x] = new Cell(x, y, mDefaultColor);
			}
		}
	}
	
	public void setCellColor(int x, int y, int color) {
		mCells[y][x].color = color;
	}
	
	public int getCellColor(int x, int y) {
		return mCells[y][x].color;
	}
	
	public void setCellToRedraw(int x, int y) {
		mCellToRedraw = mCells[y][x];
		int left = mCellToRedraw.x * mCellWidth;
		int top =  mCellToRedraw.y * mCellHeight;
		int right = left + mCellWidth;
		int bottom = top + mCellHeight;
		
		invalidate(left, top, right, bottom);
	}
	
	public void setCellDrawable(int x, int y, Drawable drawable){
		mCells[y][x].drawable = drawable;
		setCellToRedraw(x, y);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredHeight = measureHeight(heightMeasureSpec);
		int measuredWidth = measureWidth(widthMeasureSpec);

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	private void processCellClick(MotionEvent event){
		// Get the type of action this event represents
		int action = event.getAction();

		switch (action) {
		/*case (MotionEvent.ACTION_DOWN):
			// Touch screen pressed
			break;*/

		case (MotionEvent.ACTION_DOWN):
			// Touch screen pressed
			float x = event.getX();
			float y = event.getY();
			
			Cell cell = findClickedCell(x, y);
			if (cell == null) {
				//TODO throw something here
				return;
			}
			
			mOnCellClickListener.onCellClick(cell.x, cell.y);
			//invalidate();
			break;
		}
	}
	
	private Cell findClickedCell(float eventX, float eventY) {
		// TODO Auto-generated method stub
		Cell[] findedRow = null;
		
		for (int y = 0; y < mCountY; y++){
			Cell[] row = mCells[y];
			
			float boundTop = y * mCellHeight * 1.0f;
			float boundBottom = boundTop + mCellHeight;
			
			if (eventY >= boundTop && eventY < boundBottom) {
				findedRow =row; 
				break;
			}
		}
		
		Cell findedCell = null;
		for (int x = 0; x < mCountX; x++){
			float boundLeft = x * mCellWidth * 1.0f;
			float boundRight = boundLeft + mCellWidth;
			
			if (eventX >= boundLeft && eventX < boundRight) {
				findedCell = findedRow[x]; 
			}
		}
		
		
		return findedCell;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mOnCellClickListener != null){
			processCellClick(event);
		}
		
		return super.onTouchEvent(event);
	}

	protected void init() {
		Resources res = getResources();
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		mLinePaint.setColor(res.getColor(R.color.codel_field_line_color));
		mLinePaint.setStrokeWidth(1);
		mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mCellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCellPaint.setStrokeWidth(1);
		mCellPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	}
	
	private void drawFull(Canvas canvas) {

		for (int y = 0; y < mCountY; y++) {
			for (int x = 0; x < mCountX; x++) {

				try {
					Cell cell = mCells[y][x];
					float left = cell.x * mCellWidth;
					float top =  cell.y * mCellHeight;
					float right = left + mCellWidth;
					float bottom = top + mCellHeight;
					
					mCellPaint.setColor(cell.color);
					
					canvas.drawRect(left, top, right, bottom, mCellPaint);
					if(cell.drawable != null) {
						cell.drawable.setBounds((int)left, (int)top, (int)right, (int)bottom);
						cell.drawable.draw(canvas);
					}
					
					canvas.save();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
	
	private void drawGrid(Canvas canvas) {
		int realWidth = mCountX * mCellWidth;
		int realHeight = mCountY * mCellHeight;
		
		for (int y = 0; y <= mCountY; y++) {
			canvas.drawLine(0, y * mCellHeight, realWidth, y
					* mCellHeight, mLinePaint);
			canvas.save();
		}
		for (int x = 0; x <= mCountX; x++) {
			canvas.drawLine(x * mCellWidth , 0, x * mCellWidth
					, realHeight , mLinePaint);
			canvas.save();
		}
	}
	
	private void drawDirty(Canvas canvas) {
		int left = mCellToRedraw.x * mCellWidth;
		int top =  mCellToRedraw.y * mCellHeight;
		int right = left + mCellWidth;
		int bottom = top + mCellHeight;
		
		Rect bounds = canvas.getClipBounds();
		Rect check = new Rect(left, top, right, bottom);
		
		if (check.contains(bounds) == false){
			drawFull(canvas);
			return;
		}
		
		mCellPaint.setColor(mCellToRedraw.color);
		
		canvas.drawRect(left, top, right, bottom, mCellPaint);
		
		if(mCellToRedraw.drawable != null) {
			mCellToRedraw.drawable.setBounds((int)left, (int)top, (int)right, (int)bottom);
			mCellToRedraw.drawable.draw(canvas);
		}
		
		canvas.save();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		mCellWidth = Math.round(width / mCountX);
		mCellHeight = Math.round(height / mCountY);
		
		if (mCellToRedraw == null) {
			drawFull(canvas);
		}
		else if (mForceDraw == true) {
			drawFull(canvas);
			mForceDraw = false;
		}
		else {
			drawDirty(canvas);
			mCellToRedraw = null;
		}
		
		drawGrid(canvas);
	}

	// //////////////////////////////////////////////////////////////////////
	
	public void resize(int countX, int countY) {
		mCountX = countX;
		mCountY = countY;
		
		createCells();
		
		invalidate();
	}
	
	////////////////////////////////////////////////////////////////////
	
	private int measureHeight(int measureSpec) {
		int result = getSpecifiedMeasure(measureSpec);
		if (result == UNKNOWN_MEASURE) {
			result = DEFAULT_HEIGHT;
		}
		return result;
	}

	private int measureWidth(int measureSpec) {
		int result = getSpecifiedMeasure(measureSpec);
		if (result == UNKNOWN_MEASURE) {
			result = DEFAULT_WIDTH;
		}
		return result;
	}

	private int getSpecifiedMeasure(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		// Default size if no limits are specified.
		int result;

		if (specMode == MeasureSpec.AT_MOST) {
			// Calculate the ideal size of your
			// control within this maximum size.
			// If your control fills the available
			// space return the outer bound.
			result = specSize;
		} else if (specMode == MeasureSpec.EXACTLY) {
			// If your control can fit within these bounds return that value.
			result = specSize;
		} else {
			result = UNKNOWN_MEASURE;
		}

		return result;
	}
}
