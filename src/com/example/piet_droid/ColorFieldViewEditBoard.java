package com.example.piet_droid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class ColorFieldViewEditBoard extends ColorFieldView {
    int mPreferredCellWidth;
    int mPreferredCellHeight;
    boolean mFitOnViewPort;
    boolean mAlignCellsToCenter;

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

        mPreferredCellWidth = a.getDimensionPixelSize(
                R.styleable.ColorFieldViewEditBoard_preferedCellWidth, 1);

        mPreferredCellHeight = a.getDimensionPixelSize(
                R.styleable.ColorFieldViewEditBoard_preferedCellWidth, 1);

        mAlignCellsToCenter = a.getBoolean(
                R.styleable.ColorFieldViewEditBoard_alignCellsToCenter, true);

        a.recycle();
        // this.setLayoutParams(new LinearLayout.LayoutParams(1000, 1000));
    }

    public boolean getAlignCellsToCenter() {
        return mAlignCellsToCenter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        if(mFitOnViewPort == true) {
//            Rect margin = getCellMargin();
//            int w = getMeasuredWidth();
//            int h = getMeasuredHeight();
//            setMeasuredDimension(w, h);
//            return;
//        }
        
        int measuredWidth = measureWidth(widthMeasureSpec);
        int measuredHeight = measureHeight(heightMeasureSpec);

        if (measuredHeight == 0 && measuredWidth == 0) {
            setMeasuredDimension(0, 0);
            return;
        } else if (measuredHeight != 0 && measuredWidth != 0) {
            mFitOnViewPort = true;
            int bdsm = 1;
            int l = bdsm;
        }
        
        
        ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        //resetNewCellMargin();
        int marginLeft = getCellMarginLeft();
        int marginTop = getCellMarginTop();
        
        int minMeasureWidth = getCellCountX()
                * mPreferredCellWidth
                + (getTotalPaddingLeft() + (getCellStrokeWidth() * 2)
                        + vlp.leftMargin + vlp.rightMargin + (marginLeft * 2));

        int minMeasureHeight = getCellCountY()
                * mPreferredCellHeight
                + (getTotalPaddingTop() + (getCellStrokeWidth() * 2)
                        + vlp.bottomMargin + vlp.topMargin + (marginTop * 2));

        setMeasuredDimension(minMeasureWidth, minMeasureHeight);
        
//        if (getAlignCellsToCenter() == true) {
//            if (minMeasureWidth < measuredWidth) {
//                int delta = measuredWidth - minMeasureWidth;
//                marginLeft = delta / 2;
//            } else {
//                measuredWidth = minMeasureWidth;
//            }
//            if (minMeasureHeight < measuredHeight) {
//                int delta = measuredHeight - minMeasureHeight;
//                marginTop = delta / 2;
//            } else {
//                measuredHeight = minMeasureHeight;
//            }
//
//        } else {
//            measuredWidth = minMeasureWidth;
//            measuredHeight = minMeasureHeight;
//        }
//        setCellMarginLeft(marginLeft);
//        setCellMarginTop(marginTop);
//        
//        if(getCellCountX() == 2 && getCellCountY() == 2) {
//            int bdsm = 1;
//            int l = bdsm;
//        }
//        
//        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    protected void makeCellDimensions() {
        // Do nothing, because cell side set in xml
        setCellSides(mPreferredCellWidth, mPreferredCellHeight);
    }
}
