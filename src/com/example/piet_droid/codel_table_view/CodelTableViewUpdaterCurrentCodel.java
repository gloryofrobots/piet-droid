package com.example.piet_droid.codel_table_view;

import com.example.jpiet.Codel;
import com.example.piet_droid.R;
import com.example.piet_droid.widget.ColorFieldView;
import com.example.piet_droid.widget.DrawableFilledCircle;

import android.content.res.Resources;

public class CodelTableViewUpdaterCurrentCodel extends CodelTableViewUpdater {
    private Codel mPreviousCodel;
    private DrawableFilledCircle mCurrentCellDrawable;

    public CodelTableViewUpdaterCurrentCodel(ColorFieldView view,
            Resources resources) {
        super(view, resources);

        mCurrentCellDrawable = new DrawableFilledCircle();
        mCurrentCellDrawable.setColor(resources
                .getColor(R.color.debug_cell_highlight));
    }

    @Override
    protected void onUpdate(Codel codel) {
        mView.setCellDrawable(codel.x, codel.y, mCurrentCellDrawable);
        mView.clearCellDrawable(mPreviousCodel.x, mPreviousCodel.y);
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
    }

    @Override
    protected void onCancel() {
        mView.clearDrawables();
    }
}
