package com.example.piet_droid.codel_table_view;

import android.content.res.Resources;

import com.example.jpiet.Codel;
import com.example.piet_droid.R;
import com.example.piet_droid.widget.ColorFieldView;
import com.example.piet_droid.widget.DrawableFilledCircle;

public class CodelTableViewUpdaterAllTrace extends CodelTableViewUpdater {
    private Codel mPreviousCodel;
    private DrawableFilledCircle mCurrentCellDrawable;
    private DrawableFilledCircle mPreviousCellDrawable;

    public CodelTableViewUpdaterAllTrace(ColorFieldView view,
            Resources resources) {
        super(view, resources);
        mCurrentCellDrawable = new DrawableFilledCircle();
        int currentDrawableColor = resources
                .getColor(R.color.debug_cell_highlight);
        mCurrentCellDrawable.setColor(currentDrawableColor);

        mPreviousCellDrawable = new DrawableFilledCircle();
        int prevDrawableColor = resources
                .getColor(R.color.debug_previous_cell_highlight);

        mPreviousCellDrawable.setColor(prevDrawableColor);
    }

    @Override
    protected void onUpdate(Codel codel) {
        
        mView.setCellDrawable(mPreviousCodel.x, mPreviousCodel.y,
                mPreviousCellDrawable);

        mView.setCellDrawable(codel.x, codel.y, mCurrentCellDrawable);
        mPreviousCodel.set(codel);
    }

    @Override
    protected void onStart() {
        mPreviousCodel = new Codel(0, 0);
        mView.clearDrawables();
        mView.setCellDrawable(0, 0, mCurrentCellDrawable);
    }

    @Override
    protected void onComplete() {
        mView.setCellDrawable(mPreviousCodel.x, mPreviousCodel.y,
                mPreviousCellDrawable);
    }

    @Override
    protected void onCancel() {
        mView.clearDrawables();
    }
}
