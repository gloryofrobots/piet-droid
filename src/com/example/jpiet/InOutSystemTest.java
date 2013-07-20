package com.example.jpiet;

import java.io.IOException;

public class InOutSystemTest implements InOutSystem {
    
    int[] input;
    int cursor;
    String output;
    
    public InOutSystemTest(String input) {
         char [] chars =  input.toCharArray();
         this.input = new int[chars.length];
         int i = 0;
         for(char c : chars) {
             this.input[i] = c;
             i++;
         }
         
        this.output = new String();
        this.cursor = 0;
    }

    @Override
    public int read() throws IOException {
        int symbol = input[cursor];
        cursor++;
        return symbol;
    }

    @Override
    public void write(int symbol) {
        // TODO Auto-generated method stub
        output += (char) symbol;
    }

    @Override
    public void write(String str) {
        // TODO Auto-generated method stub
        output += str;
    }

}
