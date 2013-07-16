package com.example.piet_droid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import android.os.AsyncTask;

class AsyncTaskLoadBitmap extends AsyncTask<Bitmap, Integer, Void> {
    public interface LoadProcessListener {
        //public void onLoadBitmapStart();
        public void onLoadBitmapCancel();
        //public void onLoadBitmapUpdate(List<Pixel> pixels);
        public void onLoadBitmapComplete();
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
    
    private Context mContext;
    private LoadProcessListener mListener;
    //private List<Pixel> mQueue;
    //private String mFilename;
    private ProgressDialog mProgressDialog;
    
    public AsyncTaskLoadBitmap(LoadProcessListener listener, Context context) {
        mListener = listener;
        mContext = context;
    }
    
    
    @Override
    protected void  onCancelled(){
        mListener.onLoadBitmapCancel();
    }
    
    @Override
    protected void onPreExecute() {
        //mQueue = Collections.synchronizedList(new ArrayList<Pixel>());
        mProgressDialog = new ProgressDialog(mContext);
        Resources resources = mContext.getResources();
        
        String title = resources.getString(R.string.load_bitmap_process_dialog_title);
        String message = resources.getString(R.string.load_bitmap_process_dialog_message);
        
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        
        //mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //mProgressDialog.setMax(100);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        
        //mListener.onLoadBitmapStart();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mListener.onLoadBitmapComplete();
        mProgressDialog.dismiss();
    }

    @Override
    protected Void doInBackground(Bitmap... params) {
        Bitmap bitmap = params[0];
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        int size = width * height;
        int count = 0;
        // TODO CODEL SIZE HERE
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                count++;
                int pixel = bitmap.getPixel(x, y);
                
                mListener.onLoadBitmapPixel(x, y, pixel);
                
                //publishProgress((count * 100) / size);
            }
        }
        
        bitmap.recycle();
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        //int percentage = progress[0];
        //mProgressDialog.setProgress(percentage);
        /*
        mListener.onLoadBitmapUpdate(mQueue);
        synchronized (mQueue) {
            mQueue.clear();
        }*/
        
    }
}