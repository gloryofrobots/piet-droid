/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.jpiet;

/**
 * Result of scan for codels with same color value as current codel in iterpreter
 * [min,max][Y,X]Codel is public properties for codels on edge of area
 * size - count of elements and at fact integer data of current codel block
 */
class CodelArea {
    
    private static final int MAX_X_MAX_Y = 0;
    private static final int MAX_X_MIN_Y = 1;
    private static final int MIN_X_MAX_Y = 2;
    private static final int MIN_X_MIN_Y = 3;
    
    private static final int MAX_Y_MAX_X = 4;
    private static final int MAX_Y_MIN_X = 5;
    private static final int MIN_Y_MAX_X = 6;
    private static final int MIN_Y_MIN_X = 7;
    
    private boolean[][] mCheck;
    
    public Codel[] mCodels;

    public CodelColor color;
    public int size;

    CodelArea() {
        mCodels = new Codel[8];
        for(int i = 0; i < 8; i++){
            mCodels[i] = new Codel();
        }
        size = 0;
    }

    /*
     * Add codel to block and resize bounds if necessary
     */
    public void add(int x, int y) {
        //System.out.printf("AREA %d,%d\n",x,y);
        /*
        if(x == 82 && y == 1) {
            int bdsm = 1;
        }*/
        //FIXME
        
        if(mCheck != null) {
            if(mCheck[y][x] != false) {
                System.out.printf("AREA DUBLICATED %d,%d\n",x,y);
            }
            mCheck[y][x] = true;
        }
        
        Codel codel = mCodels[MAX_Y_MIN_X];
        if(y >= codel.y)
        if( y > codel.y || x < codel.x) {
            codel.set(x,y);
        }
        
        codel = mCodels[MAX_Y_MAX_X];
        if(y >= codel.y)
        if( y > codel.y || x > codel.x) {
            codel.set(x,y);
        }
        
        
        codel = mCodels[MIN_Y_MAX_X];
        if(y <= codel.y)
        if( y < codel.y || x > codel.x) {
            codel.set(x,y);
        }
        
        
        codel = mCodels[MIN_Y_MIN_X];
        if(y <= codel.y)
        if( y < codel.y || x < codel.x) {
            codel.set(x,y);
        }
        
        codel = mCodels[MAX_X_MAX_Y];
        if(x >= codel.x)
        if( x > codel.x || y > codel.y) {
            codel.set(x,y);
        }
        
        
        codel = mCodels[MAX_X_MIN_Y];
        if(x >= codel.x)
        if( x > codel.x || y < codel.y) {
            codel.set(x,y);
        }
        
        codel = mCodels[MIN_X_MAX_Y];
        if(x <= codel.x)
        if( x < codel.x || y > codel.y) {
            codel.set(x,y);
        }
        
        codel = mCodels[MIN_X_MIN_Y];
        if(x <= codel.x)
        if( x < codel.x || y < codel.y) {
            codel.set(x,y);
        }
        size++;
    }
    
    public Codel maxYMinX() {
        return mCodels[MAX_Y_MIN_X];
    }
    
    public Codel maxYMaxX() {
        return mCodels[MAX_Y_MAX_X];
    }
    
    public Codel minYMaxX() {
        return mCodels[MIN_Y_MAX_X];
    }
    
    public Codel minYMinX() {
        return mCodels[MIN_Y_MIN_X];
    }
    
    public Codel minXMinY() {
        return mCodels[MIN_X_MIN_Y];
    }
    
    public Codel minXMaxY() {
        return mCodels[MIN_X_MAX_Y];
    }
    
    public Codel maxXMaxY() {
        return mCodels[MAX_X_MAX_Y];
    }
    
    public Codel maxXMinY() {
        return mCodels[MAX_X_MIN_Y];
    }
    
    /**
     * init first codel of codel area. if search will unsucces this codel
     * will be the one in block;
     * @param x
     * @param y
     * @param codelColor
     */
    public void setDebugRestriction(int width, int height) {
        mCheck = new boolean[height][width];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                mCheck[y][x] = false;
            }
        }
        
    }
    
    public void init(int x, int y, CodelColor codelColor) {
        mCheck = null;
        for(Codel codel : mCodels) {
            codel.set(x, y);
        }
        //FIXME
        color = codelColor;
        size = 0;
    }
    
    public String toString() {
        String repr = "";
        for(Codel codel : mCodels) {
            repr += codel;
        }
        
        return repr;
    }
}
