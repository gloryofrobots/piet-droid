package com.example.piet_droid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.View;
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

    public PietFileActor(PietFile pietFile) {
        mPietFile = pietFile;
        mPiet = mPietFile.getPiet();
        mView = mPietFile.getView();
        mActivity = mPietFile.getActivity();
    }

    public void redrawCell(int x, int y) {
        mView.setCellToRedraw(x, y);
    }

    public void clearViewDrawables() {
        mView.clearDrawables();
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

    AsyncTaskLoadBitmap mLoadTask;

    public void load(String path) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeFile(path, options);

        if (bitmap == null) {
            showMessage("Error decoding file %s", path);
            return;
        }

        // TODO CODEL SIZE HERE
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        long amountOfMemory = mView.getAmountOfMemory(width, height);
        //if amountOfMemory == -1, view can`t determine memory size we just try to load program. 
        //if it would fall it would fall
        if(amountOfMemory != -1) {
            long freeMemory = MemoryUtils.getFreeMemory();
            if(freeMemory < (amountOfMemory + 1024)) {
                showMessage("Error! image to large to create piet program in your device");
                return;
            }
        }
       
        mPiet.createModel(width, height);

        mView.setVisibility(View.INVISIBLE);
        mView.resize(width, height);

        final String filePath = path;
        mLoadTask = new AsyncTaskLoadBitmap(
                new AsyncTaskLoadBitmap.LoadProcessListener() {
                    @Override
                    public void onLoadBitmapCancel() {
                        mLoadTask = null;
                    }

                    public void onLoadBitmapPixel(int x, int y, int color) {
                        setCell(x, y, color);
                    }

                    @Override
                    public void onLoadBitmapComplete() {
                        mView.setVisibility(View.VISIBLE);
                        mView.invalidate();
                        mPietFile.setPath(filePath);
                        mPietFile.untouch();
                        mLoadTask = null;
                    }

                    @Override
                    public void onLoadBitmapError() {
                        mView.setVisibility(View.VISIBLE);
                        mView.invalidate();

                        String message = mActivity.getResources().getString(
                                R.string.runtime_load_bitmap_error);
                        showMessage(message);
                    }
                }, mActivity);
        mLoadTask.execute(bitmap);
    }

    public void lockOrientation() {
        Display display = ((WindowManager) mActivity
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int tempOrientation = mActivity.getResources().getConfiguration().orientation;
        int orientation = 0;
        switch (tempOrientation) {
        case Configuration.ORIENTATION_LANDSCAPE:
            if (rotation == Surface.ROTATION_0
                    || rotation == Surface.ROTATION_90) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            } else {
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
            break;
        case Configuration.ORIENTATION_PORTRAIT:
            if (rotation == Surface.ROTATION_0
                    || rotation == Surface.ROTATION_270) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else {
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
        }
        mActivity.setRequestedOrientation(orientation);
    }

    public void unlockOrientation() {
        mActivity
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public void saveAsync(String path) {
        doSaveAsync(path);
    }

    public void saveAsync() {
        if (mPietFile.hasPath() == false) {
            // TODO THROW!!!!!
        }

        String path = mPietFile.getPath();
        saveAsync(path);
    }

    AsyncTaskWriteBitmap mSaveTask;

    private void doSaveAsync(String path) {
        final String filePath = path;

        mSaveTask = new AsyncTaskWriteBitmap(mPiet,
                new AsyncTaskWriteBitmap.SaveProcessListener() {
                    @Override
                    public void onSaveBitmapError() {
                        String message = mActivity.getResources().getString(
                                R.string.runtime_save_bitmap_error);
                        showMessage(message);
                        mSaveTask = null;
                    }

                    @Override
                    public void onSaveBitmapComplete() {
                        mPietFile.setPath(filePath);
                        mPietFile.untouch();
                        String message = mActivity.getResources().getString(
                                R.string.runtime_bitmap_saved);
                        showMessage(message);
                        mSaveTask = null;
                    }

                    @Override
                    public void onSaveBitmapCancel() {
                        mSaveTask = null;
                    }
                }, mActivity);

        mSaveTask.execute(path);
    }

    public void showMessage(String format, Object... args) {
        String msg = String.format(format, args);
        showMessage(msg);
    }

    public void showMessage(String msg) {
        Toast toast = Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void finalise() {
        mPietFile = null;
        if (mSaveTask != null) {
            mSaveTask.cancel(true);
            mSaveTask = null;
        }
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
            mLoadTask = null;
        }
    }

    public void setCellDrawable(int x, int y, Drawable drawable) {
        mView.setCellDrawable(x, y, drawable);
    }

    private final String SAVE_KEY_MODEL = "PietCodelTableModel";
    private final String SAVE_KEY_CURRENT_FILENAME = "PietCurrentFileName";

    public void saveInstanceState(Bundle savedInstanceState) {
        CodelTableModel model = mPiet.getModel();
        CodelTableModelSerializedData data = model.getSerializeData();
        savedInstanceState.putSerializable(SAVE_KEY_MODEL, data);

        if (mPietFile.hasPath() == false) {
            return;
        }

        savedInstanceState.putString(SAVE_KEY_CURRENT_FILENAME,
                mPietFile.getPath());
    }

    public void restoreFromSavedState(Bundle savedInstanceState) {
        CodelTableModelSerializedData data = (CodelTableModelSerializedData) savedInstanceState
                .getSerializable(SAVE_KEY_MODEL);

        CodelTableModel model = CodelTableModel
                .createCodelTableModelFromSerializedData(data);

        attachModel(model);
        invalidateView();

        String fileName = savedInstanceState
                .getString(SAVE_KEY_CURRENT_FILENAME);

        if (fileName == null) {
            return;
        }

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

        mPiet.createModel(countX, countY);
        mPietFile.untouch();
    }
}
