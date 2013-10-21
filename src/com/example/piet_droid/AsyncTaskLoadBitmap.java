package com.example.piet_droid;


import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import android.os.AsyncTask;
import android.util.Log;

class AsyncTaskLoadBitmap extends AsyncTask<Bitmap, Integer, Void> {
    public interface LoadProcessListener {
        public void onLoadBitmapCancel();
        public void onLoadBitmapComplete();
        public void onLoadBitmapError();
        public void onLoadBitmapPixel(int x, int y, int color);
    }
    
    public class Pixel{
        Pixel(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
        
        public int x;
        public int y;
        public int color;
    }

    private static final String LOG_TAG = "ASYNC_TASK_LOAD_BITMAP";
    
    private Context mContext;
    private LoadProcessListener mListener;
    private ProgressDialog mProgressDialog;
    boolean mErrorDetected;
    
    public AsyncTaskLoadBitmap(LoadProcessListener listener, Context context) {
        mListener = listener;
        mContext = context;
    }
    
    @Override
    protected void  onCancelled(){
        mListener.onLoadBitmapCancel();
        mProgressDialog.dismiss();
    }
    
    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        Resources resources = mContext.getResources();
        
        String title = resources.getString(R.string.load_bitmap_process_dialog_title);
        String message = resources.getString(R.string.load_bitmap_process_dialog_message);
        
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        
        mProgressDialog.dismiss();
        
        if(mErrorDetected) {
            mListener.onLoadBitmapError();
        } else {
            mListener.onLoadBitmapComplete();
        }
    }

    @Override
    protected Void doInBackground(Bitmap... params) {
        try {
            Bitmap bitmap = params[0];
            
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            
            // TODO CODEL SIZE HERE
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = bitmap.getPixel(x, y);
                    mListener.onLoadBitmapPixel(x, y, pixel);
                }
            }
            
            bitmap.recycle();
        } catch(Exception e) {
            Log.e(LOG_TAG,e.toString());
            mErrorDetected = true;
        }
        
        return null;
    }
}