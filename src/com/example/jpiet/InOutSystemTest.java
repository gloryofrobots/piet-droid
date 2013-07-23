package com.example.jpiet;

import java.io.IOException;

public class InOutSystemTest implements InOutSystem {
    
    int[] input;
    int cursor;
    String output;
    boolean mVerbose;
    
    public InOutSystemTest(int[] input, boolean verbose) {
        ///mVerbose = true;
        this.output = new String();
        this.cursor = 0;
        this.input = input;
    }
    
    /*public InOutSystemTest(char[] chars, boolean verbose) {
        
        this.input = new int[chars.length];
        int i = 0;
        for(char c : chars) {
            int x = (int) c;
            int f = Character.getNumericValue(c);
            int l =  c - 48;
            
            this.input[i] = f;
            i++;
        }
   }*/

    @Override
    public int read() throws IOException {
        int symbol = input[cursor];
        cursor++;
        return symbol;
    }

    @Override
    public void write(int symbol) {
        if (mVerbose) {
            System.out.printf("OUT %s\n",String.valueOf(symbol));
        }
        output += String.valueOf(symbol);
    }

    @Override
    public void write(String str) {
        if (mVerbose) {
            System.out.printf("OUT %s\n", str);
        }
        output += str;
    }

}
