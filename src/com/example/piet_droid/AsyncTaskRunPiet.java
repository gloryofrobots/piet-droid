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

    private enum State {
        RUN, ONE_STEP, WAIT;
    }

    private ExecutionProcessListener mListener;
    private List<Codel> mQueue;
    private long mDelay;
    private State mState;

    public AsyncTaskRunPiet(ExecutionProcessListener listener, long delay) {
        mListener = listener;
        mDelay = delay;
        mState = State.RUN;
    }

    public boolean isWaiting() {
        return mState == State.WAIT;
    }
    
    public boolean isRunning() {
        return mState == State.RUN;
    }
    
    public void setWait() {
        mState = State.WAIT;
    }

    public void allowRun() {
        mState = State.RUN;
    }

    public void allowOneStepOnly() {
        mState = State.ONE_STEP;
    }

    public void endStep() {
        if (mState == State.ONE_STEP) {
            mState = State.WAIT;
        }
    }

    @Override
    protected void onCancelled() {
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
       
        while (piet.step() == true) {
            if (isCancelled() == true) {
                return null;
            }

            // wait while resumed
            while (isWaiting()) {
            }

            Codel currentCodel = piet.getCurrentCodel();
            
            publishProgress();
            mQueue.add(new Codel(currentCodel));
            endStep();
            
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
        mListener.onRunUpdate(mQueue);
        synchronized (mQueue) {
            mQueue.clear();
        }
    }
}