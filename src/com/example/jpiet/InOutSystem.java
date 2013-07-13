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
public interface InOutSystem {
    public int read() throws IOException;
    public void write(int output);
    public void write(String output);
}
