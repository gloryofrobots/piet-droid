package com.example.jpiet;

public class PolicyStorage {
    Class mScaner;
    Logger mLogger;
    boolean mDebugMode;
    
    private PolicyStorage() {
    }
    
    private static PolicyStorage instance;
    
    public static synchronized PolicyStorage getInstance() {
        if (instance == null) {
            instance = new PolicyStorage();
        }
        
        return instance;
    }
    
    public void setModelScaner(Class<? extends CodelTableModelScanner> scaner) {
        mScaner = scaner;
    }
    
    public CodelTableModelScanner createModelScaner() {
        try {
            return (CodelTableModelScanner) mScaner.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
    
    public void setLogger(Logger logger) {
        mLogger = logger;
    }
    
    public Logger getLogger() {
        return mLogger;
    }
    
    public void setDebugMode(boolean mode) {
        mDebugMode = mode;
    }
    
    public boolean isOnDebugMode() {
        return mDebugMode;
    }
}
