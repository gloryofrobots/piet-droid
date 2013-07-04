package com.example.pieteditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.pieteditor.CodelTableModel.*;



public class Interpreter {

	class CodelPoint{
		public int x;
		public int y;
		CodelPoint(int _x, int _y){
			x = _x;
			y = _y;
		}
	}

	private enum DirectionPointer{
		/*
		 * Determines with Codel Choser scanning direction
		 * */
		RIGHT,BOTTOM,TOP,LEFT;

	}

	private enum CodelChoser{
		/*
		 * Determines with Direction Pointer scanning direction
		 * */
		LEFT,RIGHT;
	}


	private enum OperationCode{
		IDLE, PUSH, POP, ADD, SUB, MUL, DIV, MOD, NOT, GREATER, POINTER,
		SWITCH, DUPLICATE, ROLL, IN_NUMBER, IN_CHAR, OUT_NUMBER, OUT_CHAR;
	}

	private enum ExecuteFlowState{
		PAUSE,CLEAR,RUN;
	}

	private Stack<Integer> mStack;

	CodelChoser mCodelChoser;
	DirectionPointer mDirectionPointer;
	boolean mPaused;
	CodelTableModel mModel;
	OperationCode mOperationCode;
	Logger mLogger;

	int countSteps;
	
	CodelCursor mCurrentCodel;
	CodelCursor mNextCodel;

	public Interpreter ( Logger _logger ) {
		// TODO Auto-generated constructor stub
		mCodelChoser = CodelChoser.LEFT;
		mDirectionPointer = DirectionPointer.RIGHT;
		mOperationCode = OperationCode.IDLE;

		mLogger = _logger;
		countSteps = 0;
		mCurrentCodel = new CodelCursor(0,0);
		mNextCodel = new CodelCursor(-1,-1);
	}
	
	public void setInput(CodelTableModel _model){
		mModel = _model;
	}

	public boolean getPaused(){
		return mPaused;
	}

	public void pause(){
		mPaused = true;
	}
	/*  вправо 	влево 	самый верхний
				вправо 	самый нижний
		вниз 	влево 	самый правый
				вправо 	самый левый
		влево 	влево 	самый нижний
				вправо 	самый верхний
		вверх 	влево 	самый левый
				вправо 	самый правый*/

	/**
	 * Collections.sort(list, new Comparator<String>() {
	    public int compare(String a, String b) {
	        return Integer.signum(fixString(a) - fixString(b));
	    }
	    private int fixString(String in) {
	        return Integer.parseInt(in.substring(0, in.indexOf('_')));
	    }
	});
	 */

	public CodelCursor getCodelOnEdge(ArrayList<CodelCursor> _codels){
		Comparator<CodelCursor> comparator = new Comparator<CodelCursor>(){
			@Override
			public int compare(CodelCursor o1, CodelCursor o2) {
				return o1.y - o2.y;
			}
		};;
		if( mDirectionPointer == DirectionPointer.RIGHT ){
			if( mCodelChoser == CodelChoser.LEFT ){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o1.y - o2.y;
					}
				};
			}
			else if ( mCodelChoser == CodelChoser.RIGHT ){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o2.y - o1.y;
					}
				};
			}
		}
		else if( mDirectionPointer == DirectionPointer.BOTTOM ){
			if( mCodelChoser == CodelChoser.LEFT ){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o2.x - o1.x;
					}
				};
			}
			else if ( mCodelChoser == CodelChoser.RIGHT ){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o1.x - o2.x;
					}
				};
			}
		}
		else if( mDirectionPointer == DirectionPointer.LEFT ){
			if( mCodelChoser == CodelChoser.LEFT ){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o2.y - o1.y;
					}
				};
			}
			else if ( mCodelChoser == CodelChoser.RIGHT ){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o1.y - o2.y;
					}
				};
			}
		}
		else if( mDirectionPointer == DirectionPointer.TOP ){
			if( mCodelChoser == CodelChoser.LEFT ){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o1.x - o2.x;
					}
				};
			}
			else if ( mCodelChoser == CodelChoser.RIGHT ){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o2.x - o1.x;
					}
				};
			}
		}

		Collections.sort(_codels, comparator);

		CodelCursor chosen = _codels.get(0);
		return chosen;
	}

	public void run(){
		mLogger.info("Iterpreter run");
		mPaused = false;
		countSteps = 0;
		while( mPaused == false ){
			if ( step() == false ){
				break;
			}
		}
	}

	public CodelCursor getNextCodel( CodelCursor codel){
		CodelCursor next = new CodelCursor(codel); 
		if ( mDirectionPointer == DirectionPointer.RIGHT ){
			next.x += 1;
		}
		else if ( mDirectionPointer == DirectionPointer.BOTTOM ){
			next.y += 1;
		}
		else if ( mDirectionPointer == DirectionPointer.LEFT ){
			next.x -= 1;
		}
		else if ( mDirectionPointer == DirectionPointer.TOP ){
			next.y -= 1;
		}

		return next;
	}

	public boolean isValid( CodelCursor codel ){
		if( mModel.isValid(codel) == false ){
			return false;
		}
		
		CodelColor value = mModel.getValue(codel);
		if ( value == CodelColor.BLACK ){
			return false;
		}

		return true;
	}

	public void switchDirection( int attempt ){
		if ( (attempt % 2) == 0 ){
			if( mCodelChoser == CodelChoser.LEFT ){
				mCodelChoser = CodelChoser.RIGHT;
			}
			else{
				mCodelChoser = CodelChoser.LEFT;
			}
		}
		else{
			if ( mDirectionPointer == DirectionPointer.RIGHT ){
				mDirectionPointer = DirectionPointer.BOTTOM;
			}
			else if ( mDirectionPointer == DirectionPointer.BOTTOM ){
				mDirectionPointer = DirectionPointer.LEFT;
			}	
			else if ( mDirectionPointer == DirectionPointer.LEFT ){
				mDirectionPointer = DirectionPointer.TOP;
			}
			else if ( mDirectionPointer == DirectionPointer.TOP ){
				mDirectionPointer = DirectionPointer.RIGHT;
			}
		}
	}
	
	public boolean executeCommand(){
//		dH = hex2tuple[self.matrix[ny][nx]]['hue'] - \
//                hex2tuple[self.matrix[self.y][self.x]]['hue']
//           dD = hex2tuple[self.matrix[ny][nx]]['dark'] - \
//                hex2tuple[self.matrix[self.y][self.x]]['dark']
		
	}
	
	public boolean step(){
		int attempt = 1;
		ArrayList<CodelCursor> neighbours = mModel.getNeighbors(mCurrentCodel);
		CodelCursor codelOnEdge = getCodelOnEdge(neighbours);

		boolean seenWhite = false;

		mLogger.info("Start");
		CodelColor value = mModel.getValue(mCurrentCodel);
		
		while (attempt <= 8){
			CodelCursor nextCodel = getNextCodel(codelOnEdge);
			if (isValid(nextCodel) == false){
				attempt += 1;
				
				switchDirection(attempt);
						
				if( value == CodelColor.WHITE ){
					continue;
				}
				
				mLogger.info("FIX ME");
			}
			else if( value == CodelColor.WHITE ){
				seenWhite = true;
				attempt = 0;
				codelOnEdge = nextCodel;
			}
			else{
				if( seenWhite == false ){
					executeCommand();
				}
				
				mCurrentCodel = nextCodel;
				countSteps += 1;
				
				return true;
			}
		}
		
		mLogger.info("STEP FAILED");
		return false;
	}
}
