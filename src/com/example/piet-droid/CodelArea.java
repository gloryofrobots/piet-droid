/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.pieteditor;

/**
 * Result of scan for codels with same color value as current codel in iterpreter
 * [min,max][Y,X]Codel is public properties for codels on edge of area
 * size - count of elements and at fact integer data of current codel block
 */
class CodelArea {

    public Codel minYCodel;
    public Codel maxYCodel;
    public Codel minXCodel;
    public Codel maxXCodel;
    public CodelColor color;
    public int size;

    CodelArea() {
        minXCodel = new Codel();
        maxXCodel = new Codel();
        minYCodel = new Codel();
        maxYCodel = new Codel();
        size = 0;
    }

    /*
     * Add codel to block and resize bounds if necessary
     */
    public void add(int x, int y) {
        if (x < minXCodel.x) {
            minXCodel.set(x, y);
        } else if (x > maxXCodel.x) {
            maxXCodel.set(x, y);
        }

        if (y < minYCodel.y) {
            minYCodel.set(x, y);
        } else if (y > maxYCodel.y) {
            maxYCodel.set(x, y);
        }

        size++;
    }

    /**
     * init first codel of codel area. if search will unsucces this codel
     * will be the one in block;
     * @param x
     * @param y
     * @param codelColor
     */
    public void init(int x, int y, CodelColor codelColor) {
        minXCodel.set(x, y);
        maxXCodel.set(x, y);
        minYCodel.set(x, y);
        maxYCodel.set(x, y);
        color = codelColor;
        size = 1;
    }
}
