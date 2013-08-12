package com.example.jpiet;

public interface CodelTableModelScanner {
    public CodelArea getCodelArea();
    public void setModel(CodelTableModel _model);
    public void scanForCodelNeighbors(int x, int y);
}
