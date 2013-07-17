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

        public void onRunUpdate(List<Codel> codelsToUpdate);
        public void onRunUpdate(Codel codel);
        public void onRunComplete();
    }

    private enum State {
        RUN, ONE_STEP, WAIT, TERMINATED;
        
        public State parentState = null;
    }

    private ExecutionProcessListener mListener;
    private List<Codel> mQueue;
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
        state.parentState = mState;
        mState = state;
        Log.e("AsyncTaskRunPiet", mState.toString());
    }
    
    private boolean isOnState(State state) {
        return mState == state;
    }
    
    public void endStep() {
        if (mState == State.ONE_STEP) {
            mState = State.WAIT;
        }
    }
    
    public void terminate(){
        setState(State.TERMINATED);
    }
    
    public boolean isTerminated() {
        return isOnState(State.TERMINATED);
    }
    
    @Override
    protected void onCancelled() {
        mListener.onRunCancel();
    }

    @Override
    protected void onPreExecute() {
        mQueue = Collections.synchronizedList(new ArrayList<Codel>());
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
       
        while (piet.step() == true) {
            if (isCancelled() == true) {
                return null;
            }
            
            while(isFlushed() == false){
                if(isTerminated()){
                    return null;
                }
            }
            
            if(isTerminated()){
                return null;
            }
            
            // wait while resumed
            while (isWaiting()) {
                if(isTerminated()){
                    return null;
                }
            }

            mCurrentCodel = piet.getCurrentCodel();
            
            //mQueue.add(new Codel(currentCodel));
            
            endStep();
            publishProgress();
            
            waitForFlush();
            

            //publishProgress();

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
        /*mListener.onRunUpdate(mQueue);
        synchronized (mQueue) {
            mQueue.clear();
        }*/
    }
}