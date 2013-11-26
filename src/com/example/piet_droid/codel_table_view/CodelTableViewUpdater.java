package com.example.piet_droid.codel_table_view;

import android.content.res.Resources;

import com.example.jpiet.Codel;
import com.example.piet_droid.widget.ColorFieldView;

public abstract class CodelTableViewUpdater {
    ColorFieldView mView;
    Resources  mResources;
    
    public CodelTableViewUpdater(ColorFieldView view, Resources resources) {
        mView = view;
        mResources = resources;
    }
    
    public void start() {
        onStart();
    }
    
    protected abstract void onStart();

    public void complete() {
        onComplete();
    }
    
    protected abstract void onComplete();

    public void update(Codel codel) {
        onUpdate(codel);
    }

    protected abstract void onUpdate(Codel codel);
    
    public void cancel() {
        onCancel();
    }

    protected abstract void onCancel();
}
