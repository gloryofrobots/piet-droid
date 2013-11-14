package com.example.piet_droid;

import android.os.AsyncTask;
import android.util.Log;

import com.example.jpiet.Codel;
import com.example.jpiet.Piet;

class AsyncTaskRunPiet extends AsyncTask<Piet, Void, Void> {
    public interface ExecutionProcessListener {
        public void onRunStart();

        public void onRunCancel();

        public void onRunError();

        public void onRunUpdate(Codel codel);

        public void onRunComplete();
    }

    private enum State {
        RUN, ONE_STEP, WAIT;
    }

    private static final String LOG_TAG = "ASYNC_TASK_RUN_PIET";

    private ExecutionProcessListener mListener;
    private long mDelay;
    private State mState;
    private boolean mWaitForFlush;
    Codel mCurrentCodel;

    private boolean mErrorDetected;

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

    private void waitForFlush() {
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
        // Log.e("AsyncTaskRunPiet", mState.toString());
    }

    private boolean isOnState(State state) {
        return mState == state;
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
        mListener.onRunStart();
        mWaitForFlush = false;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if(mErrorDetected) {
            mListener.onRunError();
        }
        else {
            mListener.onRunComplete();
        }
    }

    @Override
    protected Void doInBackground(Piet... params) {
        Piet piet = params[0];
        while (true) {
            try {
                // Task cancel
                if (isCancelled() == true) {
                    break;
                }
                // We need to flush codel
                if (isFlushed() == false) {
                    continue;
                }
                // Task on pause
                if (isWaiting()) {
                    continue;
                }

                // perform step and publish
                if (piet.step() == false) {
                    break;
                }
                
                mCurrentCodel = piet.getCurrentCodel();
                endStep();
                waitForFlush();
                publishProgress();
                Thread.sleep(mDelay);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
                mErrorDetected = true;
                return null;
            }
        }
        return null;
    }

    protected void onProgressUpdate(Void... progress) {
        if(mErrorDetected) {
            return;
        }
        
        mListener.onRunUpdate(mCurrentCodel);
        endFlush();
    }

    public void setStepDelay(long delay) {
        mDelay = delay;
    }
}