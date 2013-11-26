package com.example.jpiet;
import java.util.ArrayList;

public abstract class Logger {
    
    public interface EventListener {
        public void onError(String error);
        public void onWarning(String error);
        public void onInfo(String error);
    }
    
    ArrayList<EventListener> mListeners;
    
    public Logger() {
       mListeners = new ArrayList<EventListener>();
    }
    
    public void addListener(EventListener listener) {
        mListeners.add(listener);
    }
    
	public synchronized void error(String msg, Object... args){
	    String error = prepare(msg, args);
	    _onError(error);
	    notifyError(error);
	}
	public synchronized void info(String msg, Object... args){
	    String error = prepare(msg, args);
	    _onInfo(error);
	    notifyInfo(error);
	}
	
	public synchronized void warning(String msg, Object... args){
	    String error = prepare(msg, args);
	    _onWarning(error);
	    notifyWarning(error);
	}
	
	public String prepare(String _msg, Object... args) {
        String str = String.format(_msg, args);
        return str;
    }
	
	public void notifyError(String msg) {
	    for(EventListener listener : mListeners) {
	        listener.onError(msg);
	    }
	}
	
	public void notifyWarning(String msg) {
        for(EventListener listener : mListeners) {
            listener.onWarning(msg);
        }
    }
	
	
	public void notifyInfo(String msg) {
        for(EventListener listener : mListeners) {
            listener.onInfo(msg);
        }
    }
	
	abstract public void _onError(String _msg);
    abstract public void _onInfo(String msg);
    abstract public void _onWarning(String msg);
}
