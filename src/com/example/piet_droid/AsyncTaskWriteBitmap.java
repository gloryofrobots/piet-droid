package com.example.piet_droid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.jpiet.CodelTableModel;
import com.example.jpiet.Piet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import android.os.AsyncTask;
import android.util.Log;

class AsyncTaskWriteBitmap extends AsyncTask<String, Void, Void> {
    public interface SaveProcessListener {
        public void onSaveBitmapCancel();

        public void onSaveBitmapComplete();

        public void onSaveBitmapError();
    }

    int[] mColors;

    private Context mContext;
    private SaveProcessListener mListener;
    private CodelTableModel mModel;
    private ProgressDialog mProgressDialog;
    //String mFormatTag;
    
    public AsyncTaskWriteBitmap(Piet piet,
            SaveProcessListener listener, Context context) {
        mListener = listener;
        mModel = piet.getModel();
        mContext = context;
        //mFormatTag = format;
    }
    
    private Bitmap.CompressFormat getBitmapDecodeFormat() {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        /*if(mFormatTag.equals("PNG")) {
            format = Bitmap.CompressFormat.PNG;
        } else if (mFormatTag.equals("JPEG")) {
            format = Bitmap.CompressFormat.JPEG;
        }*/
        
        return format;
    }
    
    @Override
    protected void onCancelled() {
        mListener.onSaveBitmapCancel();
        mProgressDialog.dismiss();
    }
    
    
    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        Resources resources = mContext.getResources();

        String title = resources
                .getString(R.string.save_bitmap_process_dialog_title);
        String message = resources
                .getString(R.string.save_bitmap_process_dialog_message);

        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);

        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mListener.onSaveBitmapComplete();
        mProgressDialog.dismiss();
    }

    @Override
    protected Void doInBackground(String... params) {
        String fileName = params[0];

        int width = mModel.getWidth();
        int height = mModel.getHeight();

        int size = width * height;
        int[] colors = new int[size];

        Bitmap bitmap = null;
        FileOutputStream fileOStream = null;

        try {
            mModel.fillArray(colors);
            bitmap = Bitmap.createBitmap(colors, width, height,
                    Bitmap.Config.ARGB_8888);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            
            Bitmap.CompressFormat format = getBitmapDecodeFormat();
            bitmap.compress(format, 100, bytes);

            File file = new File(fileName);

            fileOStream = new FileOutputStream(file);
            fileOStream.write(bytes.toByteArray());
            fileOStream.close();
            
        } catch (IndexOutOfBoundsException e) {
            Log.e("LOAD_IMAGE", "IndexOutOfBoundsException while saving "
                    + fileName);
            mListener.onSaveBitmapError();
        } catch (IOException e) {
            Log.e("LOAD_IMAGE", "IOException while saving " + fileName + "->" + e.toString());
            mListener.onSaveBitmapError();
        } finally {
            if (bitmap != null && bitmap.isRecycled() == false) {
                bitmap.recycle();
            }
        }

        return null;
    }

}