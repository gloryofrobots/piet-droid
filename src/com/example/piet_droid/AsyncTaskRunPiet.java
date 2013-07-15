package com.example.piet_droid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;

import com.example.jpiet.Codel;
import com.example.jpiet.Piet;

class AsyncTaskRunPiet extends AsyncTask<Piet, Void, Void> {
    public interface ExecutionProcessListener {
        public void onRunStart();
        public void onRunCancel();
        public void onRunUpdate(List<Codel> codelsToUpdate);
        public void onRunComplete();
    }
    

    private ExecutionProcessListener mListener;
    private List<Codel> mQueue;
    private long mDelay;
    boolean mLock;
    boolean mPause;
    
    public AsyncTaskRunPiet(ExecutionProcessListener listener, long delay) {
        mListener = listener;
        mDelay = delay;
        mPause = false;
    }
    
    public boolean isOnPause() {
        return mPause;
    }
    
    public void pause() {
        mPause = true;
    }
    
    public void resume() {
        mPause = false;
    }
    
    @Override
    protected void  onCancelled(){
        mListener.onRunCancel();
    }
    
    @Override
    protected void onPreExecute() {
        mQueue = Collections.synchronizedList(new ArrayList<Codel>());
        mListener.onRunStart();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mListener.onRunComplete();
    }

    @Override
    protected Void doInBackground(Piet... params) {
        Piet piet = params[0];
        int count = 0;
        
        while (piet.step() == true) {
            if (isCancelled() == true){
                return null;
            }
            
            //wait while resumed
            while(isOnPause()){
                int x =1;
            }
            
            Codel currentCodel = piet.getCurrentCodel();
            
            mQueue.add(new Codel(currentCodel));
            count++;
            
            publishProgress();
            
            try {
                Thread.sleep(mDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void onProgressUpdate(Void... progress) {
        mListener.onRunUpdate(mQueue);
        synchronized (mQueue) {
            mQueue.clear();
        }
    }
}