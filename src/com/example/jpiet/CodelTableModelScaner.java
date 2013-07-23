package com.example.jpiet;

public interface CodelTableModelScaner {
    public CodelArea getCodelArea();
    public void setModel(CodelTableModel _model);
    public void scanForCodelNeighbors(int x, int y);
}
