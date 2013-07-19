package com.example.piet_droid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.example.jpiet.Codel;
import com.example.jpiet.Piet;

class AsyncTaskRunPiet extends AsyncTask<Piet, Void, Void> {
    public interface ExecutionProcessListener {
        public void onRunStart();

        public void onRunCancel();

        public void onRunUpdate(Codel codel);
        public void onRunComplete();
    }

    private enum State {
        RUN, ONE_STEP, WAIT/*, TERMINATED*/;        
    }

    private ExecutionProcessListener mListener;
    private long mDelay;
    private State mState;
    private boolean mWaitForFlush;
    Codel mCurrentCodel;
    
    public AsyncTaskRunPiet(ExecutionProcessListener listener, long delay) {
        mListener = listener;
        mDelay = delay;
        mState = State.RUN;
    }
    public boolean isWaiting() {
        return isOnState(State.WAIT);
    }
    
    public boolean isRunning() {
        return isOnState(State.RUN);
    }
    
    public void setWait() {
        setState(State.WAIT);
    }

    public void allowRun() {
        setState(State.RUN);
    }

    public void allowOneStepOnly() {
        setState(State.ONE_STEP);
    }
    
    private void waitForFlush(){
        mWaitForFlush = true;
    }
    
    public void endFlush() {
        mWaitForFlush = false;
    }
    
    public boolean isFlushed() {
        return mWaitForFlush == false;
    }
    
    private void setState(State state) {
        mState = state;
        //Log.e("AsyncTaskRunPiet", mState.toString());
    }
    
    private boolean isOnState(State state) {
        return mState == state;
    }
    
    public void endStep() {
        if (mState == State.ONE_STEP) {
            mState = State.WAIT;
        }
    }
    
    /*public void terminate(){
        setState(State.TERMINATED);
    }
    
    public boolean isTerminated() {
        return isOnState(State.TERMINATED);
    }*/
    
    @Override
    protected void onCancelled() {
        mListener.onRunCancel();
    }

    @Override
    protected void onPreExecute() {
        mListener.onRunStart();
        mWaitForFlush = false;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mListener.onRunComplete();
    }

    @Override
    protected Void doInBackground(Piet... params) {
        Piet piet = params[0];
       
        while (true) {
            if (isCancelled() == true) {
                break;
            }
             
            if(isFlushed() == false){
                continue;
            }
            
            if(isWaiting()) {
                continue;
            }
            
            if(piet.step() == false) {
                break;
            }
            mCurrentCodel = piet.getCurrentCodel();
                        
            endStep();
            waitForFlush();
            
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
        mListener.onRunUpdate(mCurrentCodel);
        endFlush();
    }
    
    public void setStepDelay(long delay) {
        mDelay = delay;
    }
}