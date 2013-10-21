package com.example.piet_droid.widget;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.example.jpiet.CodelColor;
import com.example.piet_droid.MemoryUtils;
import com.example.piet_droid.R;
import com.example.piet_droid.R.styleable;

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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

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

    private class Cell implements Serializable {

        private static final long serialVersionUID = 1L;
        int color;
        Drawable drawable;
        Rect bounds;
        public int x;
        public int y;

        Cell() {
            this.color = 0;
            this.bounds = new Rect();
            this.drawable = null;
        }

        public void init(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Rect createBounds(int width, int height, Rect padding,
                Rect margin) {
            bounds.left = x * (width + padding.left) + margin.left;
            bounds.top = y * (height + padding.top) + margin.top;

            bounds.right = bounds.left + mCellWidth;
            bounds.bottom = bounds.top + mCellHeight;

            return bounds;
        }

        public void draw(Canvas canvas, Paint paint, Paint paintBounds) {
            paint.setColor(color);
            canvas.drawRect(bounds, paint);

            if (drawable != null) {
                drawable.setBounds(bounds);
                drawable.draw(canvas);
            }

            int strokeWidth = (int) paintBounds.getStrokeWidth() / 2;

            canvas.drawRect(bounds.left + strokeWidth,
                    bounds.top + strokeWidth, bounds.right - strokeWidth,
                    bounds.bottom - strokeWidth, paintBounds);
        }

        public boolean contains(float x2, float y2) {
            return bounds.contains((int) x2, (int) y2);
        }
    }

    private class Cells {
        Cell[] mCells;
        private int mWidth;
        private int mHeight;
        private int mCellMemorySize;
        
        Cells(int width, int height) {
            createCells(width, height);
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public Cell getCell(int x, int y) {
            int index = getIndex(x, y);
            return mCells[index];
        }

        private int getIndex(int x, int y) {
            int index = x + y * mWidth;

            return index;
        }

        public Cell[] getRow(int y) {
            int first = getIndex(0, y);
            int last = first + mWidth;
            Cell[] row = new Cell[mWidth];
            for (int i = first, index = 0; i < last; i++, index++) {
                row[index] = mCells[i];
            }

            return row;
        }

        private void createCells(int width, int height) {
            mWidth = width;
            mHeight = height;
            mCells = new Cell[mWidth * mHeight];
            /*int last = getIndex(0, mHeight);
            for (int i = 0; i < last; i++) {
                mCells[i] = new Cell(mDefaultCellColor);
            }*/
            
            for (int y = 0; y < mHeight; y++) {
                for (int x = 0; x < mWidth; x++) {
                        int index = getIndex(x,y);
                        Cell cell = new Cell();
                        cell.init(x, y);
                        mCells[index] = cell;
                }
            }
        }

        public boolean resize(int width, int height) {
            int oldSize = mCells.length;
            int newSize = width * height;
            int delta = newSize - oldSize;
            
            if (delta <= 0) {
                mWidth = width;
                mHeight = height;
                
                invalidateCellCoords();
                return true;
            } else {
                int first = oldSize - 1;
                Cell[] newCells = new Cell[newSize];
                System.arraycopy(mCells, 0, newCells, 0, mCells.length);

                for (int i = first; i < newSize; i++) {
                    newCells[i] = new Cell();
                }
                
                mCells = newCells;
                
                mWidth = width;
                mHeight = height;
                
                invalidateCellCoords();
                
                return true;
            }
        }
        
        public void invalidateCellCoords() {
            for (int y = 0; y < mHeight; y++) {
                for (int x = 0; x < mWidth; x++) {
                        int index = getIndex(x,y);
                        Cell cell = mCells[index];
                        cell.init(x, y);
                }
            }
        }
        // Brutal way to determine memory for one cell
        private void makeCellMemorySize() {
            long freeMemoryBefore = MemoryUtils.getFreeMemory();
            new Cell();
            long freeMemoryAfter = MemoryUtils.getFreeMemory();
            mCellMemorySize = (int) (freeMemoryBefore - freeMemoryAfter);
        }

        public long getAmountOfMemory(int width, int height) {
            if (mCellMemorySize == 0) {
                return -1;
            }
            
            int oldSize = mWidth * mHeight;
            int newSize = width * height;
            int delta = newSize - oldSize;
            if(delta <= 0) {
                return -1;
            }
            
            return delta * mCellMemorySize;
        }

        public void drawCells(Canvas canvas) {
            for(Cell cell : mCells) {
                cell.createBounds(mCellWidth, mCellHeight,
                        mCellPadding, mCellMargin);
                cell.draw(canvas, mCellPaint, mCellBoundsPaint);
            }
        }
    }

    View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        private Cell mPreviousCell;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
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
                if (mPreviousCell != null && mPreviousCell.contains(x, y)) {
                    return;
                }

            case MotionEvent.ACTION_DOWN:
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
            Cell[] findedRow = null;

            for (int y = 0; y < mCellCountY; y++) {
                float boundTop = y * (mCellHeight + mCellPadding.top)
                        + mCellMargin.top;

                float boundBottom = boundTop + mCellHeight;

                if (eventY >= boundTop && eventY < boundBottom) {
                    findedRow = mCells.getRow(y);
                    break;
                }
            }

            if (findedRow == null) {
                return null;
            }

            Cell findedCell = null;
            for (int x = 0; x < mCellCountX; x++) {
                float boundLeft = x * (mCellWidth + mCellPadding.left)
                        + mCellMargin.left;
                float boundRight = boundLeft + mCellWidth;

                if (eventX >= boundLeft && eventX < boundRight) {
                    findedCell = findedRow[x];
                }
            }

            return findedCell;
        }
    };

    private Paint mLinePaint;
    private Paint mCellPaint;
    private Paint mCellBoundsPaint;

    private Rect mCellPadding;
    private Rect mCellMargin;

    private int mCellMarginSide;
    private int mCellWidth = 1;
    private int mCellHeight = 1;

    private int mCellCountX;
    private int mCellCountY;

    private int mDefaultCellColor;
    private boolean mNormaliseForLowestEdge;

    private int mLineColor;

    private int mStrokeWidth;

    private CellClickListener mOnCellClickListener;

    private boolean mForceDraw;
    private Cell mCellToRedraw;

    private int mMinCellSide;
    private int mMaxCellSide;
  

    private final static int DEFAULT_MIN_CELL_SIDE = 10;
    private final static int DEFAULT_MAX_CELL_SIDE = 100;

    public void setOnCellClickListener(CellClickListener onCellClickListener) {
        mOnCellClickListener = onCellClickListener;
    }

    Cells mCells;

    public ColorFieldView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorFieldView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ColorFieldView);

        mNormaliseForLowestEdge = a.getBoolean(
                R.styleable.ColorFieldView_normaliseForLowestEdge, false);
        mDefaultCellColor = a.getColor(
                R.styleable.ColorFieldView_defaultCellColor, Color.BLACK);

        mCellCountX = a.getInt(R.styleable.ColorFieldView_countX, 0);
        mCellCountY = a.getInt(R.styleable.ColorFieldView_countY, 0);

        mMinCellSide = a.getDimensionPixelSize(
                R.styleable.ColorFieldView_minCellSide, DEFAULT_MIN_CELL_SIDE);

        mMaxCellSide = a.getDimensionPixelSize(
                R.styleable.ColorFieldView_maxCellSide, DEFAULT_MAX_CELL_SIDE);

        mCellWidth = a.getDimensionPixelSize(
                R.styleable.ColorFieldView_cellWidth, mMinCellSide);
        mCellHeight = a.getDimensionPixelSize(
                R.styleable.ColorFieldView_cellHeight, mCellWidth);

        mLineColor = a.getColor(R.styleable.ColorFieldView_lineColor,
                Color.BLACK);

        // Now only square padding supported but Rect used for future
        int paddingSide = a.getDimensionPixelSize(
                R.styleable.ColorFieldView_cellPadding, 0);
        mCellPadding = new Rect(paddingSide, paddingSide, paddingSide,
                paddingSide);

        // Now only square margin supported but Rect used for future
        mCellMarginSide = a.getDimensionPixelSize(
                R.styleable.ColorFieldView_cellMargin, mStrokeWidth);
        mCellMargin = new Rect(mCellMarginSide, mCellMarginSide,
                mCellMarginSide, mCellMarginSide);

        mStrokeWidth = a.getInt(R.styleable.ColorFieldView_strokeWidth, 1);

        createCells();

        

        int colorsResourceId = a.getResourceId(
                R.styleable.ColorFieldView_palette, -1);
        if (colorsResourceId != -1) {
            loadCellColorsFromResource(colorsResourceId);
        }
        a.recycle();

        this.setOnTouchListener(mOnTouchListener);

        init();
    }

    protected void resetNewCellMargin() {
        mCellMargin.set(mCellMarginSide, mCellMarginSide, mCellMarginSide,
                mCellMarginSide);
    }

    public int getCellCountX() {
        return mCellCountX;
    }

    public int getCellCountY() {
        return mCellCountY;
    }

    public int getCellWidth() {
        return mCellWidth;
    }

    public int getCellHeight() {
        return mCellHeight;
    }

    public void setCellSide(int side) {
        if (mCellWidth == side && mCellHeight == side) {
            return;
        }

        mCellWidth = side;
        mCellHeight = side;

        invalidateSize();
    }

    public int getCellMarginLeft() {
        return mCellMargin.left;
    }

    public int getCellMarginTop() {
        return mCellMargin.top;
    }

    public void setCellMarginLeft(int left) {
        mCellMargin.left = left;
    }

    public void setCellMarginTop(int top) {
        mCellMargin.top = top;
    }

    public boolean isNormaliseForLowestEdge() {
        return mNormaliseForLowestEdge;
    }

    public int getCellStrokeWidth() {
        return mStrokeWidth;
    }

    protected void init() {
        // int x = 1 / 0;
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

    public int getCellCount() {
        return mCellCountX * mCellCountY;
    }

    public void setCellPadding(int paddingSide) {
        if (mCellPadding.left == paddingSide && mCellPadding.top == paddingSide) {
            return;
        }

        mCellPadding.set(paddingSide, paddingSide, paddingSide, paddingSide);
        invalidateSize();
    }

    // Load colors from array resource in order which it have in xml
    private void loadCellColorsFromResource(int resourceId) {
        Resources resources = getResources();
        TypedArray colors = resources.obtainTypedArray(resourceId);
        int size = colors.length();
        if (size < getCellCount()) {
            throw new IllegalArgumentException("Array for fill to small");
        }

        int index = 0;
        for (int y = 0; y < mCellCountY; ++y) {
            for (int x = 0; x < mCellCountX; ++x) {
                int color = colors.getColor(index, 0);
                setCellColor(x, y, color);
                index++;
            }
        }

        colors.recycle();
    }

    private void createCells() {
        mCells = new Cells(mCellCountX, mCellCountY);   
    }

    public void setCellColor(int x, int y, int color) {
        mCells.getCell(x, y).color = color;
    }

    public int getCellColor(int x, int y) {
        return mCells.getCell(x, y).color;
    }

    public void setCellToRedraw(int x, int y) {
        mCellToRedraw = mCells.getCell(x, y);
        Rect bounds = mCellToRedraw.createBounds(mCellWidth, mCellHeight,
                mCellPadding, mCellMargin);

        invalidate(bounds);
        // invalidate();
    }

    public void setCellDrawable(int x, int y, Drawable drawable) {
        mCells.getCell(x, y).drawable = drawable;
        setCellToRedraw(x, y);
    }

    public void setDrawableForColor(int color, Drawable drawable) {
        for (int y = 0; y < mCellCountY; y++) {
            for (int x = 0; x < mCellCountX; x++) {
                if (mCells.getCell(x, y).color == color) {
                    setCellDrawable(x, y, drawable);
                }
            }
        }
    }

    public void clearDrawables() {
        for (int y = 0; y < mCellCountY; y++) {
            for (int x = 0; x < mCellCountX; x++) {
                mCells.getCell(x, y).drawable = null;
            }
        }

        invalidate();
    }

    private void drawGrid(Canvas canvas) {
        int realWidth = mCellCountX * (mCellWidth + mCellPadding.left);
        int realHeight = mCellCountY * (mCellHeight + mCellPadding.top);

        for (int y = 0; y <= mCellCountY; y++) {
            canvas.drawLine(0, y * mCellHeight, realWidth, y * mCellHeight,
                    mLinePaint);
            canvas.save();
        }
        for (int x = 0; x <= mCellCountX; x++) {
            canvas.drawLine(x * mCellWidth, 0, x * mCellWidth, realHeight,
                    mLinePaint);
            canvas.save();
        }
    }

    private void drawFull(Canvas canvas) {
        mCells.drawCells(canvas);
    }

    private void drawDirty(Canvas canvas) {
        Rect boundsToRedraw = canvas.getClipBounds();
        Rect boundsCell = mCellToRedraw.createBounds(mCellWidth, mCellHeight, mCellPadding, mCellMargin);

        if (boundsCell.contains(boundsToRedraw) == false) {
            drawFull(canvas);
            return;
        }

        mCellToRedraw.draw(canvas, mCellPaint, mCellBoundsPaint);
        canvas.save();
    }

    protected int getTotalPaddingLeft() {
        return mCellPadding.left * mCellCountX;
    }

    protected int getTotalPaddingTop() {
        return mCellPadding.top * mCellCountY;
    }

    protected int makeMinimalCanvasWidth() {
        ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int marginLeft = getCellMarginLeft();

        int minMeasureWidth = getCellCountX()
                * mCellWidth
                + (getTotalPaddingLeft() + (getCellStrokeWidth() * 2)
                        + vlp.leftMargin + vlp.rightMargin + (marginLeft * 2));

        return minMeasureWidth;
    }

    protected int makeMinimalCanvasHeight() {
        ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int marginTop = getCellMarginTop();

        int minMeasureHeight = getCellCountY()
                * mCellHeight
                + (getTotalPaddingTop() + (getCellStrokeWidth() * 2)
                        + vlp.bottomMargin + vlp.topMargin + (marginTop * 2));
        return minMeasureHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // paint.setStrokeWidth(mStrokeWidth);
        // paint.setStyle(Paint.Style.FILL_AND_STROKE);
        // paint.setColor(Color.BLUE);
        // canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        // canvas.save();

        if (mCellToRedraw == null) {
            drawFull(canvas);
        } else if (mForceDraw == true) {
            drawFull(canvas);
            mForceDraw = false;
        } else {
            drawDirty(canvas);
            mCellToRedraw = null;
        }
    }

    public void resize(int countX, int countY) {
        mCells.resize(countX, countY);

        mCellCountX = countX;
        mCellCountY = countY;
        invalidateSize();
    }

    protected void invalidateSize() {
        // this.setMinimumWidth(500);
        // this.setMinimumHeight(500);
        // this.setMinimumWidth(width);
        // this.setMinimumHeight(height);
        int width = makeMinimalCanvasWidth();
        int height = makeMinimalCanvasHeight();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this
                .getLayoutParams();
        params.width = width;
        params.height = height;
        this.setLayoutParams(params);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(makeMinimalCanvasWidth(),
                makeMinimalCanvasHeight());
    }

    public void clearAll() {
        createCells();
        invalidate();
    }

    public int getMinCellSide() {
        return mMinCellSide;
    }

    public int getMaxCellSide() {
        return mMaxCellSide;
    }

    public long getAmountOfMemory(int width, int height) {
        return mCells.getAmountOfMemory(width, height);
    }
}
