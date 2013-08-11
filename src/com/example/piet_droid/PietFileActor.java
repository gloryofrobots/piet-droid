package com.example.piet_droid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.jpiet.CodelColor;
import com.example.jpiet.CodelTableModel;
import com.example.jpiet.CodelTableModelSerializedData;
import com.example.jpiet.Piet;

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
    
    public void save(String path) {
        doSave(path);
    }
    
    public void save() {
        if(mPietFile.hasPath() == false) {
            //TODO THROW!!!!!
        }
        
        String path = mPietFile.getPath();
        save(path);
    }
    
    AsyncTaskWriteBitmap mSaveTask;
    
    private void doSave(String path) {
        final String filePath = path;
        
        mSaveTask = new AsyncTaskWriteBitmap(mPiet,
                new AsyncTaskWriteBitmap.SaveProcessListener() {

                    @Override
                    public void onSaveBitmapError() {
                        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        mView.resize(countX, countY);
        
        mPiet.createModel(countX, countY);
        mPietFile.untouch();
    }
    
}
