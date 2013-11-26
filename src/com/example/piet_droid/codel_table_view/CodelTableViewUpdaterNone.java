package com.example.piet_droid.codel_table_view;

import android.content.res.Resources;

import com.example.jpiet.Codel;
import com.example.piet_droid.widget.ColorFieldView;

public class CodelTableViewUpdaterNone extends CodelTableViewUpdater {

    public CodelTableViewUpdaterNone(ColorFieldView view, Resources resources) {
        super(view, resources);
    }

    @Override
    protected void onUpdate(Codel codel) {
    }
    
    @Override
    protected void onStart() {
        mView.clearDrawables();
    }

    @Override
    protected void onComplete() {
    }

    @Override
    protected void onCancel() {
        mView.clearDrawables();
    }
}
