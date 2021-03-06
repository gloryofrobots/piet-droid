package com.example.piet_droid.widget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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

        public boolean isNeedToHandleMoveAsClick();
    }

    private class Cells {
        private class CellColors {
            protected int[] mData;
            protected int mSize;

            public CellColors(int size) {
                mData = new int[size];
                mSize = size;
            }

            public void resize(int newSize) {
                if (mSize > newSize) {
                    mSize = newSize;
                } else {
                    growTo(newSize);
                }
            }

            public int size() {
                return mSize;
            }

            public int getDataSize() {
                return mData.length;
            }

            public int getColor(int index) {
                return mData[index];
            }

            public void setColor(int index, int value) {
                if (index >= mSize || index < 0) {
                    throw new IndexOutOfBoundsException();
                }

                mData[index] = value;
            }

            private void growTo(int newSize) {
                int[] newData = new int[newSize];
                System.arraycopy(mData, 0, newData, 0, mSize);
                mData = null;
                mData = newData;
                mSize = newSize;
            }
        }

        private class CellDrawables {
            private ArrayList<Drawable> mDrawables = new ArrayList<Drawable>();
            private HashMap<Integer, Integer> mFindMap = new HashMap<Integer, Integer>();

            private Drawable getDrawable(int index) {
                Integer drawableIndex = mFindMap.get(index);
                if (drawableIndex == null) {
                    return null;
                }
                try {
                    Drawable drawable = mDrawables.get(drawableIndex);
                    return drawable;
                } catch (IndexOutOfBoundsException e) {
                    return null;
                }
            }

            private boolean hasDrawable(int index) {
                return mFindMap.containsKey(index);
            }

            private void putDrawable(int index, Drawable drawable) {
                if (mFindMap.containsKey(index)) {
                    Integer drawableIndex = mFindMap.get(index);
                    mDrawables.set(drawableIndex, drawable);
                } else {
                    mDrawables.add(drawable);
                    int findIndex = mDrawables.size() - 1;
                    mFindMap.put(index, findIndex);
                }
            }

            public void clear() {
                mFindMap.clear();
                mDrawables.clear();
            }
        }

        CellColors mCellColors;
        CellDrawables mCellDrawables;

        private int mWidth;
        private int mHeight;
        Rect mDrawCellBounds = new Rect();

        Cells(int width, int height) {

            mWidth = width;
            mHeight = height;
            int size = mWidth * mHeight;
            mCellColors = new CellColors(size);
            mCellDrawables = new CellDrawables();
            invalidateCellColors();
        }

        private int getIndex(int x, int y) {
            int index = x + y * mWidth;
            return index;
        }

        public void resize(int width, int height) {
            int newSize = width * height;
            mCellColors.resize(newSize);
            mCellDrawables.clear();
            mWidth = width;
            mHeight = height;
            invalidateCellColors();
        }

        public void invalidateCellColors() {
            for (int y = 0; y < mHeight; y++) {
                for (int x = 0; x < mWidth; x++) {
                    setCellColor(x, y, mDefaultCellColor);
                }
            }
        }

        public long getAmountOfMemory(int width, int height) {
            return width * height * 4;
        }

        public void createBoundsForCell(int x, int y, Rect bounds,
                int layoutLeftMargin, int layoutRightMargin) {

            bounds.left = x * (mCellWidth + mCellPadding.left)
                    + mCellMargin.left + layoutLeftMargin;
            bounds.top = y * (mCellHeight + mCellPadding.top) + mCellMargin.top + layoutRightMargin;

            bounds.right = bounds.left + mCellWidth;
            bounds.bottom = bounds.top + mCellHeight;
        }

        public void createBoundsForCell(int x, int y, Rect bounds) {
            createBoundsForCell(x, y, bounds, 0, 0);
        }

        public void setCellColor(int x, int y, int color) {
            int index = getIndex(x, y);
            mCellColors.setColor(index, color);
        }

        public int getCellColor(int x, int y) {
            int index = getIndex(x, y);
            return mCellColors.getColor(index);
        }

        public void setCellDrawable(int x, int y, Drawable drawable) {
            mCellDrawables.putDrawable(getIndex(x, y), drawable);
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
            mCellDrawables.clear();
        }

        public void drawCell(int x, int y, Canvas canvas, Paint cellPaint,
                Paint cellBoundsPaint) {

            int color = getCellColor(x, y);
            
            createBoundsForCell(x, y, mDrawCellBounds);

            cellPaint.setColor(color);
            canvas.drawRect(mDrawCellBounds, cellPaint);

            Drawable drawable = mCellDrawables.getDrawable(getIndex(x, y));
            if (drawable != null) {
                drawable.setBounds(mDrawCellBounds);
                drawable.draw(canvas);
            }

            int strokeWidth = (int) cellBoundsPaint.getStrokeWidth() / 2;

            canvas.drawRect(mDrawCellBounds.left + strokeWidth,
                    mDrawCellBounds.top + strokeWidth, mDrawCellBounds.right
                            - strokeWidth,
                    mDrawCellBounds.bottom - strokeWidth, cellBoundsPaint);
        }

        public void drawCells(Canvas canvas) {
            for (int y = 0; y < mCellCountY; y++) {
                for (int x = 0; x < mCellCountX; x++) {
                    drawCell(x, y, canvas, mCellPaint, mCellBoundsPaint);
                }
            }
        }

        public void clear() {
            mCellDrawables.clear();
            invalidateCellColors();
        }
    }

    View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        private Point mCellCoord = new Point();
        private Rect mPreviousCellBounds = new Rect(-1, -1, -1, -1);

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
                if (mPreviousCellBounds.contains((int) x, (int) y)) {
                    return;
                }

                if (mOnCellClickListener.isNeedToHandleMoveAsClick() == false) {
                    return;
                }
            case MotionEvent.ACTION_UP:
                if (findClickedCell(x, y) == false) {
                    return;
                }
                mCells.createBoundsForCell(mCellCoord.x, mCellCoord.y,
                        mPreviousCellBounds);
                mOnCellClickListener.onCellClick(mCellCoord.x, mCellCoord.y);
                break;
            }
        }

        private boolean findClickedCell(float eventX, float eventY) {
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
                return false;
            }

            for (int x = 0; x < mCellCountX; x++) {
                float boundLeft = x * (mCellWidth + mCellPadding.left)
                        + mCellMargin.left;
                float boundRight = boundLeft + mCellWidth;

                if (eventX >= boundLeft && eventX < boundRight) {
                    mCellCoord.set(x, rowY);
                    return true;
                }
            }

            return false;
        }
    };

    private Paint mLinePaint;
    private Paint mCellPaint;
    private Paint mCellBoundsPaint;
    //TODO REMOVE THIS
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

    public void setOnCellClickListener(CellClickListener onCellClickListener) {
        mOnCellClickListener = onCellClickListener;
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

    private Rect mCellToRedrawBounds = new Rect();

    public void setCellToRedraw(int x, int y) {
        mCellToRedrawCoords.set(x, y);
        mPartialRedrawFlag = true;
        mCells.createBoundsForCell(x, y, mCellToRedrawBounds);

        invalidate(mCellToRedrawBounds);
        // invalidate();
    }

    public void setCellDrawable(int x, int y, Drawable drawable) {
        mCells.setCellDrawable(x, y, drawable);
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
        mCells.drawCells(canvas);
    }

    public void invalidate() {
        super.invalidate();
    }

    private void drawDirty(Canvas canvas) {
        Rect boundsToRedraw = canvas.getClipBounds();
        mCells.createBoundsForCell(mCellToRedrawCoords.x,
                mCellToRedrawCoords.y, mCellToRedrawBounds);

        if (mCellToRedrawBounds.contains(boundsToRedraw) == false) {
            drawFull(canvas);
            return;
        }
        mCells.drawCell(mCellToRedrawCoords.x, mCellToRedrawCoords.y, canvas,
                mCellPaint, mCellBoundsPaint);

        canvas.save();
    }

    protected int getTotalPaddingLeft() {
        return mCellPadding.left * mCellCountX;
    }

    protected int getTotalPaddingTop() {
        return mCellPadding.top * mCellCountY;
    }

    protected int makeMinimalCanvasWidth() {
        int marginLeft = getCellMarginLeft();

        int minMeasureWidth = getCellCountX()
                * mCellWidth
                + (getTotalPaddingLeft() + (getCellStrokeWidth() * 2)
                         + (marginLeft * 2));

        return minMeasureWidth;
    }

    protected int makeMinimalCanvasHeight() {
        int marginTop = getCellMarginTop();

        int minMeasureHeight = getCellCountY()
                * mCellHeight
                + (getTotalPaddingTop() + (getCellStrokeWidth() * 2)
                        + (marginTop * 2));
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
        mCells.clear();
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
