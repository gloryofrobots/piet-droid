package com.example.piet_droid.codel_table_view;

import java.util.HashMap;

import android.content.res.Resources;

import com.example.piet_droid.GuiTraceMode;
import com.example.piet_droid.widget.ColorFieldView;

public class CodelTableViewUpdaterPool {
    private HashMap<GuiTraceMode,CodelTableViewUpdater> mUpdaters;
    
    public CodelTableViewUpdaterPool(ColorFieldView view, Resources resources) {
        mUpdaters = new HashMap<GuiTraceMode,CodelTableViewUpdater>();
        mUpdaters.put(GuiTraceMode.All, new CodelTableViewUpdaterAllTrace(view, resources));
        mUpdaters.put(GuiTraceMode.Current, new CodelTableViewUpdaterCurrentCodel(view, resources));
        mUpdaters.put(GuiTraceMode.None, new CodelTableViewUpdaterNone(view, resources));
    }
    
    public CodelTableViewUpdater getUpdater(GuiTraceMode mode) {
        return mUpdaters.get(mode);
    }

}
