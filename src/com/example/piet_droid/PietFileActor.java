package com.example.piet_droid;


import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.jpiet.CodelColor;
import com.example.jpiet.CodelTableModel;
import com.example.jpiet.CodelTableModelSerializedData;
import com.example.jpiet.Piet;
import com.example.piet_droid.widget.ColorFieldView;

public class PietFileActor {
    PietFile mPietFile;
    Piet mPiet;
    ColorFieldView mView;
    Activity mActivity;

    private final String SAVE_KEY_MODEL_INSTANCE_STATE = "PietCodelTableModelInstanceState";
    private final String SAVE_KEY_CURRENT_FILENAME_INSTANCE_STATE = "PietCurrentFileNameInstanceState";
    private final String SAVE_KEY_IS_TEMPORARY_FILE = "PietCurrentFileIsTemporary";
    
    public PietFileActor(PietFile pietFile) {
        mPietFile = pietFile;
        mPiet = mPietFile.getPiet();
        mView = mPietFile.getView();
        mActivity = mPietFile.getActivity();
    }

    public void redrawCell(int x, int y) {
        mView.setCellToRedraw(x, y);
    }

    public void clear() {
        mView.clearAll();
        mPiet.clear();
    }

    public void setCell(int x, int y, int color) {
        mView.setCellColor(x, y, color);
        mPiet.setColor(x, y, color);
        mPietFile.touch();
    }

    public void finalise() {
        mPietFile = null;
    }

    public void saveInstanceState(Bundle savedInstanceState) {
        CodelTableModel model = mPiet.getModel();
        CodelTableModelSerializedData data = model.getSerializeData();
        savedInstanceState.putSerializable(SAVE_KEY_MODEL_INSTANCE_STATE, data);
        savedInstanceState.putBoolean(SAVE_KEY_IS_TEMPORARY_FILE, mPietFile.isTemporary());
        
        savedInstanceState.putString(SAVE_KEY_CURRENT_FILENAME_INSTANCE_STATE,
                mPietFile.getPath());
    }

    public void restoreFromSavedState(Bundle savedInstanceState) {
        CodelTableModelSerializedData data = (CodelTableModelSerializedData) savedInstanceState
                .getSerializable(SAVE_KEY_MODEL_INSTANCE_STATE);

        CodelTableModel model = CodelTableModel
                .createCodelTableModelFromSerializedData(data);

        attachModel(model);
        invalidateView();
        
        boolean isTemporary = savedInstanceState.getBoolean(SAVE_KEY_IS_TEMPORARY_FILE);
        mPietFile.setTemporary(isTemporary);
        
        String fileName = savedInstanceState
                .getString(SAVE_KEY_CURRENT_FILENAME_INSTANCE_STATE);
        
        mPietFile.setPath(fileName);
    }

    public void attachModel(CodelTableModel model) {
        int countX = model.getWidth();
        int countY = model.getHeight();
        mView.resize(countX, countY);

        for (int y = 0; y < countY; ++y) {
            for (int x = 0; x < countX; ++x) {
                CodelColor color = model.getValue(x, y);
                try {
                    mView.setCellColor(x, y, color.getARGB());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        mPiet.setModel(model);
        mPietFile.touch();
    }

    public void invalidateView() {
        mView.invalidate();
    }

    public void resize(int countX, int countY) {
        mView.resize(countX, countY);
        mPiet.setNewModel(countX, countY);
        
        //TODO MOVE AWAY
        mPietFile.untouch();
    }
}
