package com.example.piet_droid;


import java.io.Serializable;

public class IntVector implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int[] mData;
    protected int mMaxIndex;
    private static final int DEFAULT_SIZE = 20;
    public IntVector() {
        this(DEFAULT_SIZE);
    }
    
    public IntVector(int size) {
        mData = new int[size];
    }
    
    public void resize(int newSize) {
        if(mMaxIndex < newSize) {
            mMaxIndex = newSize;
        } else {
            growTo(newSize);
        }
    }

    public int size() {
        return mMaxIndex;
    }
    
    public int getDataSize() {
        return mData.length;
    }

    public int get(int index) {
        return mData[index];
    }

    public void set(int index, int value) {
        while(index >= mData.length) {
            grow();
        }
        
        mData[index] = value;
        
        if(index > mMaxIndex - 1) {
            mMaxIndex = index + 1;
        }
    }
    
    private void grow() {
        int newSize = mMaxIndex + DEFAULT_SIZE;
        growTo(newSize);
    }
    
    private void growTo(int newSize) {
        int [] newData = new int[newSize];
        System.arraycopy(mData, 0, newData, 0, mData.length);
        mData = null;
        mData = newData;
    }
}
