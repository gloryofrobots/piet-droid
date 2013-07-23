package com.example.jpiet;

public class CodelTableModelScanerRecursive implements CodelTableModelScaner{
    /*
     * Search neighbours of input codel with same color value in model
     *
     * Usage: first call scanForCodelNeighbors and then
     * getCodelArea. In CodelArea object your can`t find all important data.
     *
     * After all calls to scanForCodelNeighbors CodelArea area rewrites
     * Unfortunately now it uses non efficient all-cells-in-table recursive algorhitm
     */

    private CodelTableModel mModel;
    protected CodelArea mArea;

    CodelTableModelScanerRecursive() {
        mArea = new CodelArea();
    }

    public CodelArea getCodelArea() {
        return mArea;
    }

    public void setModel(CodelTableModel _model) {
        mModel = _model;
    }

    /*
     * Search neighbors for codel with coords x,y and fill CodelArea object
     * Call getCodelArea() after
     */
    public void scanForCodelNeighbors(int x, int y) {
        int width = mModel.getWidth();
        int height = mModel.getHeight();
        boolean[][] checked = new boolean[height][width];

        CodelColor value = mModel.getValue(x, y);

        mArea.init(x, y, value);
        mArea.setDebugRestriction(width, height);
        checked[y][x] = true;

        addNeighbours(x, y, checked);
    }

    private void addNeighbour(int x, int y, boolean[][] checked) {
        if (mModel.isValid(x, y) == false) {
            return;
        }

        if (checked[y][x] == true) {
            return;
        }

        CodelColor color = mModel.getValue(x, y);
        if (color != mArea.color) {
            return;
        }

        checked[y][x] = true;

        mArea.add(x, y);

        addNeighbours(x, y, checked);
    }

    private void addNeighbours(int x, int y, boolean[][] checked) {
        int left = x - 1;
        int top = y - 1;
        int bottom = y + 1;
        int right = x + 1;
        addNeighbour(left, y, checked);
        addNeighbour(right, y, checked);
        addNeighbour(x, bottom, checked);
        addNeighbour(x, top, checked);
    }
}
