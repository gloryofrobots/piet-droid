package com.example.piet_droid;

import java.util.LinkedList;

import com.example.jpiet.Codel;
import com.example.jpiet.Piet;

public class PietFileRunner implements AsyncTaskRunPiet.ExecutionProcessListener {
    public interface RunEventListener {
        public void onRunStart();
        public void onRunCancel();
        public void onRunUpdate(Codel codel);
        public void onRunComplete();
    }
    
    PietFile mPietFile;
    
    AsyncTaskRunPiet mCurrentRunTask;
    
    LinkedList<RunEventListener> mListeners;
    
    public PietFileRunner(PietFile file) {
        mPietFile = file;
        mListeners = new LinkedList<RunEventListener>();
    }
    
    public void setStepDelay(long delay) {
        mCurrentRunTask.setStepDelay(delay);
    }
    
    public void addExecutionListener(RunEventListener listener) {
        mListeners.add(listener);
    }
    
    public void run(long delay) {
        if (mCurrentRunTask != null) {
            if (mCurrentRunTask.isWaiting()) {
                mCurrentRunTask.allowRun();
            }
        } else {
            Piet piet = mPietFile.getPiet();
            mCurrentRunTask = new AsyncTaskRunPiet(this, delay);
            mCurrentRunTask.execute(piet);
        }
    }
    
    public void step(long delay) {
        if (mCurrentRunTask != null) {
            mCurrentRunTask.allowOneStepOnly();
        } else {
            Piet piet = mPietFile.getPiet();
            mCurrentRunTask = new AsyncTaskRunPiet(this, delay);
            mCurrentRunTask.allowOneStepOnly();
            mCurrentRunTask.execute(piet);
        }
    }
    
    public void pause() {
        mCurrentRunTask.setWait();
    }
    
    public void stop() {
        mCurrentRunTask.cancel(true);
    }
    
    public void finalise() {
        mPietFile = null;
        if(isOnRunMode()) {
            stop();
        }
    }
    
    @Override
    public void onRunStart() {
        for(RunEventListener listener : mListeners) {
            listener.onRunStart();
        }
        
    }

    @Override
    public void onRunCancel() {
        for(RunEventListener listener : mListeners) {
            listener.onRunCancel();
        }
        
        mCurrentRunTask = null;
    }

    @Override
    public void onRunUpdate(Codel codel) {
        if (mCurrentRunTask == null || mCurrentRunTask.isCancelled()) {
            return;
        }
        
        for(RunEventListener listener : mListeners) {
            listener.onRunUpdate(codel);
        }
    }

    @Override
    public void onRunComplete() {
        for(RunEventListener listener : mListeners) {
            listener.onRunComplete();
        }
        
        mCurrentRunTask = null;
    }
    
    public boolean isOnRunMode() {
        return mCurrentRunTask != null;
    }
}
