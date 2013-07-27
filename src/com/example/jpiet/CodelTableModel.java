package com.example.jpiet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodelTableModel {
/*
 * Model of interpreter
 * Use x,y coords or codel arguments for access to data
 */
   
    protected CodelColor mData[];
    protected int mWidth;
    protected int mHeight;
     
    //FIXME delete underscores
    static public CodelTableModel createEmptyCodelTableModel(int _width, int _height, CodelColor defaultColor) {
        CodelTableModel model = new CodelTableModel(_width, _height, defaultColor);
        return model;
    }
    
    static public CodelTableModel createCodelTableModelFromSerializedData(CodelTableModelSerializedData data) {
        CodelTableModel model = new CodelTableModel(data);
        return model;
    }
    
    private CodelTableModel(int _width, int _height, CodelColor defaultColor) {
        mWidth = _width;
        mHeight = _height;
        
        mData = new CodelColor[mWidth *  mHeight];
        fillWithColor(defaultColor);
    }
    
    private CodelTableModel(CodelTableModelSerializedData data) {
        // TODO Auto-generated constructor stub
        mWidth = data.width;
        mHeight = data.height;
        
        mData = Arrays.copyOf(data.values, data.values.length);
    }
    
    public CodelTableModelSerializedData getSerializeData() {
        CodelTableModelSerializedData data = new CodelTableModelSerializedData();
        data.width = mWidth;
        data.height = mHeight;
        data.values = Arrays.copyOf(mData, mData.length);
        
        return data;
    }

    public void fillWithColor(CodelColor color) {
        for( int i = 0; i < mData.length; i++ ){
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
        if (_index >= mData.length) {
            throw new IndexOutOfBoundsException();
        }

        mData[_index] = _item;
    }

    public CodelColor getValue(int x, int y) {
        int index = getIndex(x, y);
        CodelColor result = null;
        try{
            result =  mData[index];
            return result;
        }
        catch(ArrayIndexOutOfBoundsException e) {
            PolicyStorage.getInstance().getLogger().error(e.toString());
        }
        
        return null;
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
        for(int i = 0; i < mData.length; ++i) {
            colors[i] = mData[i].getARGB();
        }
    }

    public List<CodelColor> getRow(int index) {
        ArrayList<CodelColor> row = new ArrayList<CodelColor>();
        //int first = mWidth*index;
        //int last = first + mWidth;
        for(int i = 0; i < mWidth; ++i) {
            CodelColor color = getValue(i, index);
            row.add(color);
        }
        
        return row;
    }
}
