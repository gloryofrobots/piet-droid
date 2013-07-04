package com.example.pieteditor;

import java.util.ArrayList;

import com.example.pieteditor.CodelTableModel.CodelCursor;

public class CodelTableModel {
	
	static public class CodelCursor{
		public int x;
		public int y;
		CodelColor color;
		
		public CodelCursor(int _x, int _y, CodelColor _color){
			x = _x;
			y = _y;
			color = _color;
		}
		
		public CodelCursor( CodelCursor _codel ){
			this(_codel.x, _codel.y, _codel.color);
		}
		
		public CodelCursor() {
			this(0, 0, CodelColor.BLACK);
		}
	}
	
	protected int mSize;
	protected CodelCursor mData[];
	protected int mWidth;
	protected int mHeight;
	
	public CodelTableModel(int _width, int _height) {
		// TODO Auto-generated constructor stub
		mSize = _width * _height;
		mData = new CodelCursor[mSize];
	}
	
	public void set(int _index, CodelColor _item) {
		if ( _index >= mSize ){
			throw new IndexOutOfBoundsException();
		}
		
		mData[_index] = _item;
	}
	
//	public ArrayList<CodelCursor> getRow( int _y ){
//		ArrayList<CodelCursor> result = new ArrayList<CodelCursor>();
//		
//	}
	
	private void addNeighbour( int x, int y, CodelColor value, ArrayList<CodelCursor> result,
			 boolean[] _checked){
		CodelCursor codel = getCodelCursor(x, y);
		
		if (codel == null){
			return;
		}
		
		int index = getIndex(codel);
		
		if( _checked[index] == true ){
			return;
		}
			
		if ( getValue(codel) != value ){
			return;
		}
		
		_checked[index] = true;
		result.add(codel);
		addNeighbours(codel, value, result, _checked);
	}
	
	private void addNeighbours(CodelCursor _cursor, CodelColor value,
			ArrayList<CodelCursor> result, boolean[] _checked){
		int left = _cursor.x - 1;
		int top = _cursor.y - 1;
		int bottom = _cursor.y + 1;
		int right = _cursor.x + 1;
		addNeighbour(left, _cursor.y, value, result, _checked);
		addNeighbour(right, _cursor.y, value, result, _checked);
		addNeighbour(_cursor.x, bottom, value, result, _checked);
		addNeighbour(_cursor.x, top, value, result, _checked);
	}
	
	public ArrayList<CodelCursor> getNeighbors( CodelCursor _cursor ){
		ArrayList<CodelCursor> result = new ArrayList<CodelCursor>();
		boolean checked[] = new boolean[mSize];
		
		CodelColor value = getValue(_cursor);
		result.add(_cursor);
		addNeighbours(_cursor, value, result, checked);
		return result;
	}
	
	public CodelCursor getNeighbor( CodelCursor _cursor, int _deltaX, int _deltaY ){
		int nextX =  _cursor.x + _deltaX;
		int nextY = _cursor.y + _deltaY;
		
		return getCodelCursor( nextX, nextY );
	}
	
	public int getIndex(CodelCursor _cursor){
		return getIndex(_cursor.x, _cursor.y);
	}
	
	public int getIndex(int x , int y){
		return x + y * mWidth;
	}
	
	public CodelColor getValue( CodelCursor _cursor ){
		int index = getIndex(_cursor);
		return mData[index];
	}
	
	public CodelCursor getCodelCursor( int x, int y ){
		 
		if ( x < 0 || x >= mWidth || y < 0 || y >= mHeight ){
			return null;
		}
		
		return new CodelCursor(x, y);
	}

	public boolean isValid( CodelCursor codel ) {
		if ( codel.x < 0 || codel.x >= mWidth || codel.y < 0 || codel.y >= mHeight ){
			return false;
		}
		
		return true;
	}
}
