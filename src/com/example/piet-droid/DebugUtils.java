package com.example.pieteditor;

/**
 * Created with IntelliJ IDEA.
 * User: gloryofrobots
 * Date: 05/07/13
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */

import java.util.ArrayList;


public class DebugUtils {
    static public <T> void printArrayList( ArrayList<T> _array){
        System.out.print("[");
        for(int i = 0; i < _array.size(); i++) {
            String str = _array.get(i).toString();
            System.out.print(str);
        }
        System.out.println("]");
    }

    static public void printf(String _format, Object... args){
        System.out.printf(_format + "\n" , args);
    }
}
