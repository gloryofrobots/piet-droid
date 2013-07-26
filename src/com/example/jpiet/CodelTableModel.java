package com.example.jpiet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CodelTableModel {
/*
 * Model of interpreter
 * Use x,y coords or codel arguments for access to data
 */
   
    public class SerializedData  implements Serializable{
        /**
         * 
         */
        private static final long serialVersionUID = 4325689890282796352L;
        protected int size;
        protected CodelColor data[];
        protected int width;
        protected int height;
    }
    
    protected int mSize;
    protected CodelColor mData[];
    protected int mWidth;
    protected int mHeight;
    
    
    //FIXME delete underscores
    static public CodelTableModel createEmptyCodelTableModel(int _width, int _height, CodelColor defaultColor) {
        CodelTableModel model = new CodelTableModel(_width, _height, defaultColor);
        return model;
    }
    
    static public CodelTableModel createCodelTableModelFromSerializedData(SerializedData data) {
        CodelTableModel model = new CodelTableModel(data);
        return model;
    }
    
    private CodelTableModel(int _width, int _height, CodelColor defaultColor) {
        // TODO Auto-generated constructor stub
        mSize = _width * _height;
        
        mData = new CodelColor[mSize];
        fillWithColor(defaultColor);
        
        mWidth = _width;
        mHeight = _height;
    }
    
    private CodelTableModel(SerializedData data) {
        // TODO Auto-generated constructor stub
        mWidth = data.width;
        mHeight = data.height;
        
        mSize = mWidth * mHeight;
        
        mData = data.data;
    }
    
    public SerializedData getSerializeData() {
        SerializedData data = new SerializedData();
        data.size = mSize;
        data.width = mWidth;
        data.height = mHeight;
        data.data = mData;
        
        return data;
    }

    
    public void fillWithColor(CodelColor color) {
        for( int i = 0; i < mSize; i++ ){
            mData[i] = color;
        }
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
        if(index == 196){
            int bdsm = 1;
        }
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

    public void fillArray(int[] colors) throws IndexOutOfBoundsException {
        for(int i = 0; i < mSize; ++i) {
            colors[i] = mData[i].getARGB();
        }
    }

    public List<CodelColor> getRow(int index) {
        ArrayList<CodelColor> row = new ArrayList<CodelColor>();
        int first = mWidth*index;
        int last = first + mWidth;
        for(int i = 0; i < mWidth; ++i) {
            CodelColor color = getValue(i, index);
            row.add(color);
        }
        
        return row;
    }
}
