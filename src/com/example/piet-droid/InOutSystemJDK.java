/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.pieteditor;

import java.io.IOException;

/**
 *
 * @author виктор
 */
public class InOutSystemJDK implements InOutSystem{

    public int read() throws IOException {
        return System.in.read();
    }

    public void write(int output) {
        System.out.print(output);
    }

    public void write(String output) {
        System.out.print(output);
    }
}
