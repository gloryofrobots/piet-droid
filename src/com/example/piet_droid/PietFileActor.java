package com.example.piet_droid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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
    Context mContext;
    
    public PietFileActor(PietFile pietFile) {
        mPietFile = pietFile;
        mPiet = mPietFile.getPiet();
        mView = mPietFile.getView();
        mContext = mPietFile.getContext();
    }
    
    /*
    public void setPietFile(PietFile file) {
        mPietFile = file;
    }*/
    
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
        //TODO CHECK ERRORS!!!!!
        // TODO FADE OUT FADE IN
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        // TODO CODEL SIZE HERE
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

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
                }, mContext);
        mLoadTask.execute(bitmap);
    }
    public  static  void lockOrientation(Activity activity) {
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int tempOrientation = activity.getResources().getConfiguration().orientation;
        int orientation = 0;
        switch(tempOrientation)
        {
        case Configuration.ORIENTATION_LANDSCAPE:
            if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            else
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            break;
        case Configuration.ORIENTATION_PORTRAIT:
            if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            else
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
        activity.setRequestedOrientation(orientation);
    }
    
    public void unlockOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
//    public void save() {
//        String fileName = params[0];
//        
//        int width = mModel.getWidth();
//        int height = mModel.getHeight();
//
//        int size = width * height;
//        int[] colors = new int[size];
//
//        Bitmap bitmap = null;
//        FileOutputStream fileOStream = null;
//
//        try {
//            mModel.fillArray(colors);
//            bitmap = Bitmap.createBitmap(colors, width, height,
//                    Bitmap.Config.ARGB_8888);
//
//            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//            
//            Bitmap.CompressFormat format = getBitmapDecodeFormat();
//            bitmap.compress(format, 100, bytes);
//
//            File file = new File(fileName);
//
//            fileOStream = new FileOutputStream(file);
//            fileOStream.write(bytes.toByteArray());
//            fileOStream.close();
//            
//        } catch (IndexOutOfBoundsException e) {
//            Log.e("LOAD_IMAGE", "IndexOutOfBoundsException while saving "
//                    + fileName);
//            mListener.onSaveBitmapError();
//        } catch (IOException e) {
//            Log.e("LOAD_IMAGE", "IOException while saving " + fileName);
//            mListener.onSaveBitmapError();
//        } finally {
//            if (bitmap != null && bitmap.isRecycled() == false) {
//                bitmap.recycle();
//            }
//        }
//    }
    
    
    public void saveAsync(String path) {
        doSaveAsync(path);
    }
    
    public void saveAsync() {
        if(mPietFile.hasPath() == false) {
            //TODO THROW!!!!!
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
                        showMessage("Error occurred during saving bitmap");
                    }

                    @Override
                    public void onSaveBitmapComplete() {
                        mPietFile.setPath(filePath);
                        mPietFile.untouch();
                        showMessage("Bitmap saved");
                        mSaveTask = null;
                    }

                    @Override
                    public void onSaveBitmapCancel() {
                        mSaveTask = null;
                    }
                }, mContext);

        mSaveTask.execute(path);
    }

    public void showMessage(String msg) {
        Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
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

    public void setCellDrawable(int x, int y,
            Drawable drawable) {
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
        
        savedInstanceState.putString(SAVE_KEY_CURRENT_FILENAME, mPietFile.getPath());
    }
    
    public void restoreFromSavedState(Bundle savedInstanceState) {
        CodelTableModelSerializedData data = (CodelTableModelSerializedData) savedInstanceState
                .getSerializable(SAVE_KEY_MODEL);
        
        CodelTableModel model = CodelTableModel.createCodelTableModelFromSerializedData(data);

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
        
        for(int y = 0; y < countY; ++y) {
            for (int x = 0; x < countX; ++x) {
                CodelColor color = model.getValue(x, y);
                try{
                    mView.setCellColor(x, y, color.getARGB());
                    
                }
                catch (Exception e) {
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
