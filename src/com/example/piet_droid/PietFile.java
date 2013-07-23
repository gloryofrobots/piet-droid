package com.example.piet_droid;


public class PietFile {
    private String mPath;
    private PietFileActor mActor;
    
    public PietFile(PietFileActor actor) {
        mActor = actor;
        actor.setPietFile(this);
    }
    
    public void finalise() {
        mActor.finalise();
        mActor = null;
    }
    
    public void setPath(String path) {
        mPath = path;
    }
    
    public boolean hasPath() {
        return mPath != null;
    }
    
    public String getPath() {
        return mPath;
    }
    
    public PietFileActor getActor() {
        return mActor;
    }

}
