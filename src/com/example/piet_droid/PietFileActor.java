package com.example.piet_droid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import com.example.jpiet.Piet;

public class PietFileActor {
    PietFile mPietFile;
    Piet mPiet;
    ColorFieldView mView;
    Context mContext;

    public PietFileActor(ColorFieldView view, Piet piet, Context context) {
        mPiet = piet;
        mView = view;
        mContext = context;
    }

    public void setPietFile(PietFile file) {
        mPietFile = file;
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
    }

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
        AsyncTaskLoadBitmap loadTask = new AsyncTaskLoadBitmap(
                new AsyncTaskLoadBitmap.LoadProcessListener() {
                    @Override
                    public void onLoadBitmapCancel() {
                        // TODO Auto-generated method stub
                    }

                    public void onLoadBitmapPixel(int x, int y, int color) {
                        setCell(x, y, color);
                    }

                    @Override
                    public void onLoadBitmapComplete() {
                        mView.setVisibility(View.VISIBLE);
                        mView.invalidate();
                        mPietFile.setPath(filePath);
                    }
                }, mContext);
        loadTask.execute(bitmap);
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
    
    private void doSave(String path) {
        final String filePath = path;
        
        AsyncTaskWriteBitmap saveTask = new AsyncTaskWriteBitmap(mPiet,
                new AsyncTaskWriteBitmap.SaveProcessListener() {

                    @Override
                    public void onSaveBitmapError() {
                        // TODO Auto-generated method stub
                        showMessage("Error occurred during saving bitmap");
                    }

                    @Override
                    public void onSaveBitmapComplete() {
                        mPietFile.setPath(filePath);
                        
                        showMessage("Bitmap saved");
                    }

                    @Override
                    public void onSaveBitmapCancel() {
                    }
                }, mContext);

        saveTask.execute(path);
    }

    public void showMessage(String msg) {
        Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public void finalise() {
        mPietFile = null;
        // IF RUN TASK AND BLA BLA
    }

    public void setCellDrawable(int x, int y,
            Drawable drawable) {
        mView.setCellDrawable(x, y, drawable);
    }
}
