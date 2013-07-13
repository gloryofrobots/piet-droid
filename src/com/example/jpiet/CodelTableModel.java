package com.example.jpiet;

public class CodelTableModel {
/*
 * Model of interpreter
 * Use x,y coords or codel arguments for access to data
 */
   
    protected int mSize;
    protected CodelColor mData[];
    protected int mWidth;
    protected int mHeight;

    public CodelTableModel(int _width, int _height) {
        // TODO Auto-generated constructor stub
        mSize = _width * _height;
        
        mData = new CodelColor[mSize];
        for( int i = 0; i < mSize; i++ ){
        	mData[i] = CodelColor.BLACK;
        }
        
        mWidth = _width;
        mHeight = _height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void set(int _x, int _y, CodelColor _item) {
        int index = getIndex(_x, _y);
        set(index, _item);
    }

    public void set(int _index, CodelColor _item) {
        if (_index >= mSize) {
            throw new IndexOutOfBoundsException();
        }

        mData[_index] = _item;
    }

    public CodelColor getValue(int x, int y) {
        int index = getIndex(x, y);
        return mData[index];
    }

    public CodelColor getValue(Codel _cursor) {
        return getValue(_cursor.x, _cursor.y);
    }

    public boolean isValid(int x, int y) {
        if (x < 0 || x >= mWidth || y < 0 || y >= mHeight) {
            return false;
        }

        return true;
    }

    public boolean isValid(Codel codel) {
        return isValid(codel.x, codel.y);
    }

    private int getIndex(int x, int y) {
        int index = x + y * mWidth;

        return index;
    }

    /*
   

    public int getIndex(Codel _cursor) {
    return getIndex(_cursor.x, _cursor.y);
    }

    public Codel getCodelCursor(int x, int y) {

    if (x < 0 || x >= mWidth || y < 0 || y >= mHeight) {
    return null;
    }

    //return new Codel(x, y);
    }*/
}
