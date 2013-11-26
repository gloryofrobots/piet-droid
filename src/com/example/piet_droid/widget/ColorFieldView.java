package com.example.piet_droid.widget;

import java.io.Serializable;
import java.util.Iterator;

import com.example.piet_droid.MemoryUtils;
import com.example.piet_droid.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ColorFieldView extends View {
    public interface CellClickListener {
        public void onCellClick(int x, int y);

        public boolean isProcessClickWanted();
    }

    private class Cell implements Serializable {

        private static final long serialVersionUID = 1L;
        private int color;
        private Drawable drawable;
        private Rect bounds;
        private int x;
        private int y;

        Cell() {
            this.bounds = new Rect();
        }

        // clear all cell state
        public void init(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.drawable = null;
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

    private class Cells implements Iterable<Cell> {
        public class CellIterator implements Iterator<Cell> {
            private int mLastIndex;
            private int mCurrentIndex;

            CellIterator(int first, int last) {
                mLastIndex = last;
                mCurrentIndex = first;
            }

            @Override
            public boolean hasNext() {
                return mCurrentIndex < mLastIndex;
            }

            @Override
            public Cell next() {
                Cell cell = mCells[mCurrentIndex];
                mCurrentIndex++;
                return cell;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        Cell[] mCells;
        private int mWidth;
        private int mHeight;
        private int mCellMemorySize;
        private int mLastIndex;

        Cells(int width, int height) {

            mWidth = width;
            mHeight = height;
            int size = mWidth * mHeight;
            mCells = new Cell[size];
            createCells(0, size);
            makeCellMemorySize();
        }

        public Cell getCell(int x, int y) {
            int index = getIndex(x, y);
            return mCells[index];
        }

        private int getIndex(int x, int y) {
            int index = x + y * mWidth;

            return index;
        }

        private void createCells(int first, int last) {
            for (int i = first; i < last; i++) {
                mCells[i] = new Cell();
            }

            invalidateCellCoords();
        }

        public void resize(int width, int height) {
            int oldSize = mCells.length;
            int newSize = width * height;
            int delta = newSize - oldSize;

            if (delta <= 0) {
                mWidth = width;
                mHeight = height;

                invalidateCellCoords();
            } else {
                int first = oldSize - 1;
                Cell[] newCells = new Cell[newSize];
                System.arraycopy(mCells, 0, newCells, 0, mCells.length);

                mCells = newCells;
                mWidth = width;
                mHeight = height;

                createCells(first, newSize);
            }
        }

        public void invalidateCellCoords() {
            // create last index for iterator
            mLastIndex = 0;
            for (int y = 0; y < mHeight; y++) {
                for (int x = 0; x < mWidth; x++) {
                    Cell cell = mCells[mLastIndex];
                    cell.init(x, y, mDefaultCellColor);
                    mLastIndex++;
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

            int oldSize = mCells.length;
            int newSize = width * height;
            int delta = newSize - oldSize;
            if (delta <= 0) {
                return -1;
            }

            return delta * mCellMemorySize;
        }

        @Override
        public Iterator<Cell> iterator() {
            return new CellIterator(0, mLastIndex);
        }

        public Rect createBoundsForCell(int x, int y) {
            return getCell(x, y).createBounds(mCellWidth, mCellHeight, mCellPadding, mCellMargin);
        }

        public void setCellColor(int x, int y, int color) {
            getCell(x, y).color = color;
        }

        public int getCellColor(int x, int y) {
            return getCell(x, y).color;
        }

        public void setCellDrawable(int x, int y, Drawable drawable) {
            getCell(x, y).drawable = drawable;
        }

        public void setDrawableForColor(int color, Drawable drawable) {
            for (int y = 0; y < mCellCountY; y++) {
                for (int x = 0; x < mCellCountX; x++) {
                    if (getCellColor(x, y) == color) {
                        setCellDrawable(x, y, drawable);
                    }
                }
            }
        }

        public void clearDrawables() {
            for (int y = 0; y < mCellCountY; y++) {
                for (int x = 0; x < mCellCountX; x++) {
                    getCell(x, y).drawable = null;
                }
            }
        }

        public void drawCell(int x, int y, Canvas canvas, Paint cellPaint,
                Paint cellBoundsPaint) {
            getCell(x,y).draw(canvas, cellPaint, cellBoundsPaint);
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
                    return;
                }

                mPreviousCell = cell;
                mOnCellClickListener.onCellClick(cell.x, cell.y);
                break;
            }
        }

        private Cell findClickedCell(float eventX, float eventY) {
            int rowY = -1;
            for (int y = 0; y < mCellCountY; y++) {
                float boundTop = y * (mCellHeight + mCellPadding.top)
                        + mCellMargin.top;

                float boundBottom = boundTop + mCellHeight;

                if (eventY >= boundTop && eventY < boundBottom) {
                    rowY = y;
                    break;
                }
            }

            if (rowY == -1) {
                return null;
            }

            for (int x = 0; x < mCellCountX; x++) {
                float boundLeft = x * (mCellWidth + mCellPadding.left)
                        + mCellMargin.left;
                float boundRight = boundLeft + mCellWidth;

                if (eventX >= boundLeft && eventX < boundRight) {
                    return mCells.getCell(x, rowY);
                }
            }

            return null;
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
    
    Point mCellToRedrawCoords = new Point();
    boolean mPartialRedrawFlag;
    
    private int mMinCellSide;
    private int mMaxCellSide;

    private final static int DEFAULT_MIN_CELL_SIDE = 10;
    private final static int DEFAULT_MAX_CELL_SIDE = 100;

    Cells mCells;
    
    public void setOnCellClickListener(CellClickListener onCellClickListener) {
        mOnCellClickListener = onCellClickListener;
    }

    

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

        mCells = new Cells(mCellCountX, mCellCountY);

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

    public void setCellColor(int x, int y, int color) {
        mCells.setCellColor(x, y, color);
        
    }

    public int getCellColor(int x, int y) {
        return mCells.getCellColor(x, y);
    }
    
    public void setCellToRedraw(int x, int y) {
        mCellToRedrawCoords.set(x, y);
        mPartialRedrawFlag = true;
        
        Rect bounds = mCells.createBoundsForCell(x, y);

        invalidate(bounds);
        // invalidate();
    }

    public void setCellDrawable(int x, int y, Drawable drawable) {
        mCells.setCellDrawable(x,y,drawable);
        setCellToRedraw(x, y);
    }

    public void clearCellDrawable(int x, int y) {
        mCells.setCellDrawable(x, y, null);
        setCellToRedraw(x, y);
    }
    
    public void setDrawableForColor(int color, Drawable drawable) {
        mCells.setDrawableForColor(color, drawable);
        
    }

    public void clearDrawables() {
        mCells.clearDrawables();
       

        invalidate();
    }

    private void drawFull(Canvas canvas) {
        for (Cell cell : mCells) {
            cell.createBounds(mCellWidth, mCellHeight, mCellPadding,
                    mCellMargin);
            cell.draw(canvas, mCellPaint, mCellBoundsPaint);
        }

    }

    public void invalidate() {
        super.invalidate();
    }

    private void drawDirty(Canvas canvas) {
        Rect boundsToRedraw = canvas.getClipBounds();
        Rect boundsCell = mCells.createBoundsForCell(mCellToRedrawCoords.x, mCellToRedrawCoords.y);
              
        if (boundsCell.contains(boundsToRedraw) == false) {
            drawFull(canvas);
            return;
        }
        mCells.drawCell(mCellToRedrawCoords.x, mCellToRedrawCoords.y, 
                canvas, mCellPaint, mCellBoundsPaint);
        
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
        if (mPartialRedrawFlag == false) {
            drawFull(canvas);
        } else if (mForceDraw == true) {
            drawFull(canvas);
            mForceDraw = false;
        } else {
            drawDirty(canvas);
            mPartialRedrawFlag = false;
        }
    }

    public void resize(int countX, int countY) {
        mCells.resize(countX, countY);

        mCellCountX = countX;
        mCellCountY = countY;
        invalidateSize();
    }

    protected void invalidateSize() {
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
        mCells.invalidateCellCoords();
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
