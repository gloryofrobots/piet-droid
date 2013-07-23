
package com.example.jpiet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CodelTableModelScanerIterative implements CodelTableModelScaner {
    
    private final int DELTA_DOWN = 1;
    private final int DELTA_UP = -1;
    
    private class Segment{
        //ArrayList<CodelColor> data;
        ArrayList<Area> parents;
        int x;
        int y;
        int size;
        CodelColor value;
        
        public Segment(int x, int y, CodelColor kind){
            this.x = x;
            this.y = y;
            value = kind;
            size = 1;
            //data = new ArrayList<CodelColor>();
            //data.add(kind);
            parents = new ArrayList<Area>();
        }
        
        public boolean hasValueAt(int index){
            if (index < this.x || index >= this.getLastIndex()) {
                return false;
            }
            
            return true;
        }
        
        public int size(){
            return size;
        }
        
        public int getLastIndex(){
            return this.x + size();
        }
        
        public boolean isValidValue(CodelColor value){
            return this.value == value;
        }
        
        public void add(CodelColor value) {
            size++;
            //this.data.add(value);
        }
        
        public void addParent(Area parent) {
            this.parents.add(parent);
        }
        
        public boolean hasParents() {
            return this.parents.size() != 0;
        }
        
        public int getCountOfParents() {
            return this.parents.size();
        }
        
        public String toString() {
            return String.format("Segment on row %d from %d to %d with data count %d" 
                    , this.y, this.x, this.getLastIndex(), size);
        }

        public List<Area> getParents() {
            return parents;
        }

        public boolean hasManyParents() {
            return parents.size() > 1;
        }

        public boolean contain(int x, int y) {
            if (this.y != y) {
                return false;
            }
            
            return hasValueAt(x);
        }

        public void killParents() {
            parents = new ArrayList<Area>();
        }
    }
    
    private class Area{
        private class SegmentList extends ArrayList<Segment>{
            private static final long serialVersionUID = 1661298220478053913L;
            }
        
        SegmentList segments;

        public Area(Segment first) {
            segments = new SegmentList();
            first.addParent(this);
            this.segments.add(first);
        }
        
        public void tryToMerge(Segment segment, int delta) {
            ArrayList<Segment> segments = new ArrayList<Segment>();
            getSegmentsAtRow(segment.y - delta, segments);
            if (segments.size() == 0) {
                return;
            }
            
            for (Segment existedSegment : segments) {
                if (isIntersected(existedSegment, segment)) {
                    this.add(segment);
                    return;
                }
            }
        }
        
        public void merge(Area area, Segment segment) {
            area.segments.remove(segment);
            for(Segment segment_parent : area.segments) {
                this.add(segment_parent);
            }
        }
        
        public void add(Segment segment) {
            segment.addParent(this);
            segments.add(segment);
        }
        
        public void getSegmentsAtRow(int rowIndex, ArrayList<Segment> destination) {
            for (Segment segment : segments) {
                if (segment.y == rowIndex) {
                    destination.add(segment);
                }
            }
        }
        
        public boolean isIntersected(Segment segment1, Segment segment2) {
            if (segment1.value != segment2.value){
                return false;
            }
            int last = segment1.getLastIndex();
            for (int i = segment1.x; i < last; i++) {
                if (segment2.hasValueAt(i)) {
                    return true;
                }
            }
           
            return false;
        }
        
        @Override
        public String toString() {
            String repr = "";
            for (Segment segment : segments) {
                repr += segment + "\n";
            }
            
            return repr;
        }

        public List<Segment> getSegments() {
            return segments;
        }

        public boolean contains(int x, int y) {
            for (Segment segment : segments) {
                if (segment.contain(x, y)) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    private List<Segment> getSegmentsAtRow(CodelTableModel model
            , int index){
        List<CodelColor> row = model.getRow(index);
        int size = row.size();
        if (size == 0) {
            return null;
        }
        
        List<Segment> segments = new ArrayList<Segment>();
        CodelColor first = row.get(0);
        
        Segment segment = new Segment(0, index, first);
        
        for (int i = 1; i < size; i++) {
            
            CodelColor value = row.get(i);
            
            if (segment.isValidValue(value)) {
                segment.add(value);
            }
            else {
                segments.add(segment);
                segment = new Segment(i, index, value);
            }
        }
        
        segments.add(segment);
        
        return segments;
    }
    
    private void mergeSegmentAreas(List<Area> areas, Segment segment) {
        List<Area> parents = segment.getParents();
        Area big_parent = parents.get(0);
        
        segment.killParents();
        
        int size = parents.size();
        for (int i = 1; i < size; ++i) {
            Area soonDiedParent = parents.get(i);
            big_parent.merge(soonDiedParent, segment);
            areas.remove(soonDiedParent);
        }     
    }
    
    private List<Area> createAreas(CodelTableModel model) {
        List<Segment> segments = getSegmentsAtRow(model, 0);
        if (segments == null) {
            return null;
        }
        
        List<Area> areas = new ArrayList<Area>();
        
        for (Segment segment : segments) {
            Area area = new Area(segment);
            areas.add(area);
        }
        
        int height = model.getHeight();
        for (int y = 1; y < height; ++y) {
            segments = getSegmentsAtRow(model, y);
            //TODO //CHECK
            if (segments == null) {
                continue;
            }
            
            for (Segment segment : segments) {
                for (Area area : areas) {
                    area.tryToMerge(segment, DELTA_DOWN);
                }
                
                if (segment.hasParents() == false) {
                    Area area = new Area(segment);
                    areas.add(area);
                }
                
                if (segment.hasManyParents() == true) {
                    mergeSegmentAreas(areas, segment);
                }
                
            }
            
        }
        
        /*for (Area area : areas) {
            System.out.println(area);
            System.out.println("_________________________________");
        }*/
        return areas;
    }
   
   
    
    private CodelTableModel mModel;
    protected CodelArea mArea;
    List<Area> mAreas;
    CodelTableModelScanerIterative() {
        mArea = new CodelArea();
    }
    
    @Override
    public CodelArea getCodelArea() {
        return mArea;
    }
    
    @Override
    public void setModel(CodelTableModel _model) {
        mModel = _model;
        mAreas = createAreas(mModel);
    }
    
    public void scanForCodelNeighbors(int x, int y) {
        int width = mModel.getWidth();
        int height = mModel.getHeight();
        
        CodelColor value = mModel.getValue(x, y);

        mArea.init(x, y, value);
        mArea.setDebugRestriction(width, height);
        
        Area result = null;
        for (Area area : mAreas) {
            if(area.contains(x, y)) {
                result = area;
                break;
            }
        }
        
        if(result == null) {
           //TODO LOGGING SINGLETON
           return;
        }
        
        List<Segment> segments = result.getSegments();
       
        for (Segment segment : segments) {
            int last = segment.getLastIndex();
            int first = segment.x;
            for(int i = first; i < last; ++i) {
              //FIXME THIS IS WEIGHT
                if(x == i && y == segment.y){
                    continue;
                }
                mArea.add(i, segment.y);
            }
        }
    }
}
