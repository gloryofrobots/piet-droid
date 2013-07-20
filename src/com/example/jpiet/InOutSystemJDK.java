/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.jpiet;

import java.io.IOException;

/**
 *
 * @author 
 */
public class InOutSystemJDK implements InOutSystem{

    public int read() throws IOException {
        int sym = System.in.read();
        System.out.println(sym);
        return sym;
    }

    public void write(int output) {
        System.out.print(output);
    }

    public void write(String output) {
        System.out.print(output);
    }
}
