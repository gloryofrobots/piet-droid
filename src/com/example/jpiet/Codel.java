/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.jpiet;

/**
 * Simple representation of x,y 2D coord data
 */
public class Codel {

    public int x;
    public int y;

    @Override
    public String toString() {
        String res = String.format("(%d,%d)", x, y);
        return res;
    }

    public Codel() {
        this(0, 0);
    }
    
    public Codel(Codel codel) {
        this(codel.x, codel.y);
    }

    public Codel(int _x, int _y) {
        x = _x;
        y = _y;
    }

    public void set(int _x, int _y) {
        x = _x;
        y = _y;
    }

    public void set(Codel codel) {
        x = codel.x;
        y = codel.y;
    }
}
