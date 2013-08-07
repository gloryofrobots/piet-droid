package com.example.piet_droid;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

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
    public interface CellClickListener {
        public void onCellClick(int x, int y);
        public boolean isProcessClickWanted();
    }

    private class Cell {
        public int x;
        public int y;
        int color;
        Drawable drawable;

        Rect bounds;
        Rect padding;
        int margin;
        
        Cell(int x, int y, int color, Rect padding, int margin) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.bounds = new Rect();
            this.padding = padding;
            this.margin = margin;
            drawable = null;
        }

        public Rect createBounds(int width, int height) {
            bounds.left = x * (width + padding.left) + margin;
            bounds.top = y * (height + padding.top) + margin;
            
            bounds.right = bounds.left + mCellWidth;
            bounds.bottom = bounds.top + mCellHeight;

            return bounds;
        }
        
        public void draw(Canvas canvas, Paint paint, Paint paintBounds){
            paint.setColor(color);
            canvas.drawRect(bounds, paint);

            if (drawable != null) {
                drawable.setBounds(bounds);
                drawable.draw(canvas);
            }
            
            int strokeWidth = (int) paintBounds.getStrokeWidth() / 2;
            
            canvas.drawRect(bounds.left + strokeWidth,
                    bounds.top + strokeWidth,
                    bounds.right - strokeWidth,
                    bounds.bottom - strokeWidth
                    , paintBounds);
            }

        public boolean contains(float x2, float y2) {
            // TODO Auto-generated method stub
            return bounds.contains((int)x2, (int)y2);
        }
    }
    
    View.OnTouchListener mOnTouchListener = new View.OnTouchListener(){
        private Cell mPreviousCell;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (mOnCellClickListener != null 
                    && mOnCellClickListener.isProcessClickWanted()) {
                processCellClick(event);
                return true;
            }
            return false;
        }
        
        private void processCellClick(MotionEvent event) {
            // Get the type of action this event represents
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            
            switch (action) {
            case MotionEvent.ACTION_MOVE:
                if(mPreviousCell != null && mPreviousCell.contains(x,y)) {
                    return;
                }
                
            case MotionEvent.ACTION_DOWN:
                // Touch screen pressed
                Cell cell = findClickedCell(x, y);
                if (cell == null) {
                    // TODO throw something here
                    return;
                }
                mPreviousCell = cell;
                mOnCellClickListener.onCellClick(cell.x, cell.y);
                // invalidate();
                break;
            }
        }

        private Cell findClickedCell(float eventX, float eventY) {
            // TODO Auto-generated method stub
            Cell[] findedRow = null;

            for (int y = 0; y < mCountY; y++) {
                Cell[] row = mCells[y];

                float boundTop = y * (mCellHeight + mPadding.top) + mMargin;
                
                float boundBottom = boundTop + mCellHeight;

                if (eventY >= boundTop && eventY < boundBottom) {
                    findedRow = row;
                    break;
                }
            }
            
            if(findedRow == null) {
                return null;
            }
            
            Cell findedCell = null;
            for (int x = 0; x < mCountX; x++) {
                float boundLeft = x * (mCellWidth + mPadding.left) + mMargin;
                float boundRight = boundLeft + mCellWidth;

                if (eventX >= boundLeft && eventX < boundRight) {
                    findedCell = findedRow[x];
                }
            }
            
            return findedCell;
        }
        
    };
    
    
    
    private final int UNKNOWN_MEASURE = -1;
    private final int DEFAULT_WIDTH = 100;
    private final int DEFAULT_HEIGHT = 100;
    
    private Paint mLinePaint;
    private Paint mCellPaint;
    private Paint mCellBoundsPaint;
    private Rect mPadding;
    
    private int mMargin;
    
    private int mCellWidth = 1;
    private int mCellHeight = 1;

    private int mCountX;
    private int mCountY;

    private int mDefaultCellColor;
    private boolean mNormaliseForLowestEdge;
     
    private int mLineColor;
    
    private int mStrokeWidth;
    
    private CellClickListener mOnCellClickListener;

    private boolean mForceDraw;
    private Cell mCellToRedraw;
    
    /**
     * @param mOnCellClickListener
     *            the CellClickListener to set
     */
    public void setOnCellClickListener(CellClickListener onCellClickListener) {
        mOnCellClickListener = onCellClickListener;
    }

    Cell[][] mCells;
    
    public ColorFieldView(Context context) {
        super(context);
        
        mPadding = new Rect();
        mNormaliseForLowestEdge = false;
        mDefaultCellColor =Color.BLACK;
        
        mCountX = 0;
        mCountY = 0;
        
        mLineColor = Color.BLACK;
        mStrokeWidth = 0;
        
        this.setOnTouchListener(mOnTouchListener);
    }
    
    public ColorFieldView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorFieldView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ColorFieldView);
        
        mNormaliseForLowestEdge = a.getBoolean(R.styleable.ColorFieldView_normaliseForLowestEdge, false);
        mDefaultCellColor = a.getColor(R.styleable.ColorFieldView_defaultCellColor, Color.BLACK);
        
        mCountX = a.getInt(R.styleable.ColorFieldView_countX, 0);
        mCountY = a.getInt(R.styleable.ColorFieldView_countY, 0);
        
        mLineColor = a.getColor(R.styleable.ColorFieldView_lineColor, Color.BLACK);
        
        //Now only square padding supported but Rect used for future
        int paddingSide = a.getInt(R.styleable.ColorFieldView_cellPadding, 0);
        mPadding = new Rect(paddingSide, paddingSide, paddingSide, paddingSide);
        
        mStrokeWidth = a.getInt(R.styleable.ColorFieldView_strokeWidth, 1);
        mMargin = mStrokeWidth;
        createCells();

        int colorsResourceId = a.getResourceId(
                R.styleable.ColorFieldView_palette, -1);
        if (colorsResourceId != -1) {
            loadCellColorsFromResources(colorsResourceId);
        }
        a.recycle();
        
        this.setOnTouchListener(mOnTouchListener);
        
        init();
    }
    
    protected void init() {
        
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(mStrokeWidth);
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        
        mCellBoundsPaint = new Paint();
        mCellBoundsPaint.setColor(mLineColor);
        mCellBoundsPaint.setStrokeWidth(mStrokeWidth);
        mCellBoundsPaint.setStyle(Style.STROKE);
        
        mCellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCellPaint.setStrokeWidth(mStrokeWidth);
        mCellPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setDefaultCellColor(int color) {
        mDefaultCellColor = color;
    }
    
    public void setNormaliseForLowestEdge(boolean value) {
        mNormaliseForLowestEdge = value;
    }
    
    public void setLineColor(int color) {
        mLineColor = color;
    }
    
    public void setStrokeWidth(int width) {
        mStrokeWidth = width;
    }
    
    // Load colors from array resource in order which it have in xml
    private void loadCellColorsFromResources(int resourceId) {
        Resources resources = getResources();
        TypedArray colors = resources.obtainTypedArray(resourceId);
        int size = colors.length();
        int y = 0;
        int x = 0;
        for (int i = 0; i < size; i++) {
            int color = colors.getColor(i, 0);

            setCellColor(x, y, color);
            x++;
            //FIXME
            if (((i + 1) % mCountX) == 0) {
                y++;
                x = 0;
            }

        }
        colors.recycle();
    }

    private void createCells() {
        mCells = new Cell[mCountY][mCountX];

        for (int y = 0; y < mCountY; y++) {
            for (int x = 0; x < mCountX; x++) {
                mCells[y][x] = new Cell(x, y, mDefaultCellColor, mPadding, mMargin);
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
        Rect bounds = mCellToRedraw.createBounds(mCellWidth, mCellHeight);

        invalidate(bounds);
        //invalidate();
    }

    public void setCellDrawable(int x, int y, Drawable drawable) {
        mCells[y][x].drawable = drawable;
        setCellToRedraw(x, y);
    }

    public void setDrawableForColor(int color, Drawable drawable) {
        for (int y = 0; y < mCountY; y++) {
            for (int x = 0; x < mCountX; x++) {
                if (mCells[y][x].color == color) {
                    setCellDrawable(x, y, drawable);
                }
            }
        }
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("onTouchEvent", String.format("acion %d", event.getAction()));
        if (mOnCellClickListener != null 
                && mOnCellClickListener.isProcessClickWanted()) {
            processCellClick(event);
        }
        super.onTouchEvent(event);
        //return super.onTouchEvent(event);
        return true;
    }*/
    
    public void clearDrawables() {
        for (int y = 0; y < mCountY; y++) {
            for (int x = 0; x < mCountX; x++) {
                mCells[y][x].drawable = null;
            }
        }

        invalidate();
    }

    private void drawGrid(Canvas canvas) {
        int realWidth = mCountX * (mCellWidth + mPadding.left);
        int realHeight = mCountY * (mCellHeight + mPadding.top);

        for (int y = 0; y <= mCountY; y++) {
            canvas.drawLine(0, y * mCellHeight, realWidth, y * mCellHeight,
                    mLinePaint);
            canvas.save();
        }
        for (int x = 0; x <= mCountX; x++) {
            canvas.drawLine(x * mCellWidth, 0, x * mCellWidth, realHeight,
                    mLinePaint);
            canvas.save();
        }
    }

    private void drawFull(Canvas canvas) {

        for (int y = 0; y < mCountY; y++) {
            for (int x = 0; x < mCountX; x++) {
                try {
                    Cell cell = mCells[y][x];
                    cell.createBounds(mCellWidth, mCellHeight);
                    cell.draw(canvas, mCellPaint, mCellBoundsPaint);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void drawDirty(Canvas canvas) {
        Rect boundsToRedraw = canvas.getClipBounds();
        Rect boundsCell = mCellToRedraw.createBounds(mCellWidth, mCellHeight);

        if (boundsCell.contains(boundsToRedraw) == false) {
            drawFull(canvas);
            return;
        }

        mCellToRedraw.draw(canvas, mCellPaint, mCellBoundsPaint);
        canvas.save();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        //Subtract stroke width for removing artifacts on canvas edge
        int width = getMeasuredWidth() - mStrokeWidth;
        int height = getMeasuredHeight() - mStrokeWidth;
        
        int totalPaddingLeft = mPadding.left * mCountX;
        int totalPaddingTop = mPadding.top * mCountY;
        
        /*Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(), paint);
        canvas.save();*/
        
        mCellWidth = Math.round((width - totalPaddingLeft) / mCountX);
        mCellHeight = Math.round((height - totalPaddingTop) / mCountY);
        
        if(mNormaliseForLowestEdge == true) {
            int lowestSide = Math.min(mCellWidth, mCellHeight);
            mCellWidth = lowestSide;
            mCellHeight = lowestSide;
        }
        
        
        if (mCellToRedraw == null) {
            drawFull(canvas);
        } else if (mForceDraw == true) {
            drawFull(canvas);
            mForceDraw = false;
        } else {
            drawDirty(canvas);
            mCellToRedraw = null;
        }
        
        //drawGrid(canvas);
    }

    // //////////////////////////////////////////////////////////////////////

    public void resize(int countX, int countY) {
        mCountX = countX;
        mCountY = countY;

        createCells();

        invalidate();
    }

    // //////////////////////////////////////////////////////////////////
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measureWidth(widthMeasureSpec);
        int measuredHeight = measureHeight(heightMeasureSpec);
        
        if(mNormaliseForLowestEdge) {
            int minSide = Math.min(measuredWidth, measuredHeight);
            measuredWidth = minSide;
            measuredHeight = minSide;
        }
        
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

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

    public void clearAll() {
        createCells();
        invalidate();
    }
}
