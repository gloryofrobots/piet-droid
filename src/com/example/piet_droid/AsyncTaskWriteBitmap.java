package com.example.piet_droid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.example.jpiet.Piet;

class AsyncTaskWriteBitmap extends AsyncTask<String, Void, Void> {
    public interface SaveProcessListener {
        public void onSaveBitmapCancel();

        public void onSaveBitmapComplete();

        public void onSaveBitmapError();
    }

    boolean mErrorDectected;
    private Context mContext;
    private SaveProcessListener mListener;
    private ProgressDialog mProgressDialog;
    //String mFormatTag;
    private BitmapWriter mWriter;
    public AsyncTaskWriteBitmap(Piet piet,
            SaveProcessListener listener, Context context) {
        mListener = listener;
        mWriter = new BitmapWriter(piet.getModel());
        mContext = context;
        mErrorDectected = false;
        //mFormatTag = format;
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
        mProgressDialog.dismiss();
        
        if(mErrorDectected) {
            mListener.onSaveBitmapError();
        } else {
            mListener.onSaveBitmapComplete();
        }
    }
    
    @Override
    protected Void doInBackground(String... params) {
        String fileName = params[0];
        mErrorDectected = !(mWriter.write(fileName));
        return null;
    }
}