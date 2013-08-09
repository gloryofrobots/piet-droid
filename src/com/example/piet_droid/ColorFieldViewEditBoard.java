package com.example.piet_droid;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class ColorFieldViewEditBoard extends ColorFieldView {
    int mPreferredCellWidth;
    int mPreferredCellHeight;
    boolean mFitOnViewPort;

    public ColorFieldViewEditBoard(Context context) {
        super(context);
    }

    public ColorFieldViewEditBoard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorFieldViewEditBoard(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ColorFieldViewEditBoard);

        mPreferredCellWidth = a.getInteger(
                R.styleable.ColorFieldViewEditBoard_preferedCellWidth, 1);

        mPreferredCellHeight = a.getInteger(
                R.styleable.ColorFieldViewEditBoard_preferedCellWidth, 1);

        a.recycle();
        // this.setLayoutParams(new LinearLayout.LayoutParams(1000, 1000));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mFitOnViewPort = true;
        int measuredWidth = measureWidth(widthMeasureSpec);
        int measuredHeight = measureHeight(heightMeasureSpec);

        int minMeasureWidth = getCountX() * mPreferredCellWidth
                + getTotalPaddingLeft() + (getCellStrokeWidth() * 2);

//        if (measuredWidth < minMeasureWidth) {
//            measuredWidth = minMeasureWidth;
//            mFitOnViewPort = false;
//        }

        int minMeasureHeight = getCountY() * mPreferredCellHeight
                + getTotalPaddingTop() + (getCellStrokeWidth() * 2);

//        if (measuredHeight < minMeasureHeight) {
//            measuredHeight = minMeasureHeight;
//            mFitOnViewPort = false;
//        }
/*
        if (isNormaliseForLowestEdge() == true) {
            int minSide = Math.min(measuredWidth, measuredHeight);
            measuredWidth = minSide;
            measuredHeight = minSide;
        }*/
        measuredWidth = minMeasureWidth;
        measuredHeight = minMeasureHeight;
        
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    protected void makeCellDimensions() {
        // Do nothing, because cell side set in xml
        setCellSides(mPreferredCellWidth, mPreferredCellHeight);
    }
}
