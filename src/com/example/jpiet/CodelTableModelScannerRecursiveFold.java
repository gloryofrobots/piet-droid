package com.example.jpiet;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class CodelTableModelScannerRecursiveFold implements
        CodelTableModelScanner {
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

    CodelTableModelScannerRecursiveFold() {
        mArea = new CodelArea();
    }

    public CodelArea getCodelArea() {
        return mArea;
    }

    public void setModel(CodelTableModel _model) {
        mModel = _model;
    }
    
    interface ScanChank {
        public void exec(ScanQueue queue, boolean[][] checked);
    }
    
    private class ScanQueue extends LinkedList<ScanChank> implements List<ScanChank>{
        
        
        private static final long serialVersionUID = 1L;

        public void addScan(final int x, final int y) {
            this.add(new ScanChank() {

                @Override
                public void exec(ScanQueue queue, boolean[][] checked){
                    addNeighbour(x,y, checked, queue);
                }
                
            });
        }
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
        //checked[y][x] = true;
        
        ScanQueue queueCurrent = new ScanQueue();
        ScanQueue queueNext = new ScanQueue();
        ScanQueue temp;
        addNeighbour(x, y, checked, queueCurrent);
        while(true) {
            if(queueCurrent.size() == 0) {
                break;
            }
            
            queueNext.clear();
            for(ScanChank chank : queueCurrent) {
                chank.exec(queueNext, checked);
            }
            
            temp = queueCurrent;
            queueCurrent = queueNext;
            queueNext = temp;
        }
    }
    
    private void addNeighbour(int x, int y, boolean[][] checked, ScanQueue queue ) {
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
        
        addNeighbours(x, y, checked, queue);
    }
    
    private void addNeighbours(final int x, final int y, boolean[][] checked, ScanQueue queue) {
        final int left = x - 1;
        final int top = y - 1;
        final int bottom = y + 1;
        final int right = x + 1;
        
        queue.addScan(right, y);
        queue.addScan(x, bottom);
        queue.addScan(x, top);
        queue.addScan(left, y);
    }

}


