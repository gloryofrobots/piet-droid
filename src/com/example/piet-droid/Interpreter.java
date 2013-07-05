package com.example.pieteditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.pieteditor.CodelTableModel.*;
import  com.example.pieteditor.Stack;


public class Interpreter {


	private enum OperationCode{
		IDLE, PUSH, POP, ADD, SUB, MUL, DIV, MOD, NOT, GREATER, POINTER,
		SWITCH, DUPLICATE, ROLL, IN_NUMBER, IN_CHAR, OUT_NUMBER, OUT_CHAR;
	}

	private Stack<Integer> mStack;

	boolean mPaused;
	CodelTableModel mModel;
    Logger mLogger;

    int countSteps;

	PietMachine mMachine;
	CodelCursor mCurrentCodel;
	CodelCursor mNextCodel;

	public Interpreter (Logger _logger, PietMachine _machine) {
		// TODO Auto-generated constructor stub
		mLogger = _logger;

        countSteps = 0;

		mCurrentCodel = new CodelCursor();
		mNextCodel = new CodelCursor();
		
		mMachine = _machine;
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
    /*
	public CodelCursor getCodelOnEdge(ArrayList<CodelCursor> _codels){
		Comparator<CodelCursor> comparator = new Comparator<CodelCursor>(){
			@Override
			public int compare(CodelCursor o1, CodelCursor o2) {
				return o1.y - o2.y;
			}
		};
		if(mDirectionPointer == DirectionPointer.RIGHT){
			if(mCodelChoser == CodelChoser.LEFT){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o1.y - o2.y;
					}
				};
			}
			else if (mCodelChoser == CodelChoser.RIGHT){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o2.y - o1.y;
					}
				};
			}
		}
		else if(mDirectionPointer == DirectionPointer.BOTTOM){
			if(mCodelChoser == CodelChoser.LEFT){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o2.x - o1.x;
					}
				};
			}
			else if (mCodelChoser == CodelChoser.RIGHT){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o1.x - o2.x;
					}
				};
			}
		}
		else if(mDirectionPointer == DirectionPointer.LEFT){
			if(mCodelChoser == CodelChoser.LEFT){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o2.y - o1.y;
					}
				};
			}
			else if (mCodelChoser == CodelChoser.RIGHT){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o1.y - o2.y;
					}
				};
			}
		}
		else if(mDirectionPointer == DirectionPointer.TOP){
			if(mCodelChoser == CodelChoser.LEFT){
				comparator = new Comparator<CodelCursor>(){
					@Override
					public int compare(CodelCursor o1, CodelCursor o2) {
						return o1.x - o2.x;
					}
				};
			}
			else if (mCodelChoser == CodelChoser.RIGHT){
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
    */
    public CodelCursor getCodelOnEdge2(ArrayList<CodelCursor> _codels){
        DirectionPointer directionPointer = mMachine.getDirectionPointer();

        int even = directionPointer.getState() % 2;

        Comparator<CodelCursor> comparator = new Comparator<CodelCursor>(){
            @Override
            public int compare(CodelCursor o1, CodelCursor o2) {
                return o1.y - o2.y;
            }
        };
        if(directionPointer.isRight()){
            if(even != 0){
                comparator = new Comparator<CodelCursor>(){
                    @Override
                    public int compare(CodelCursor o1, CodelCursor o2) {
                        return o1.y - o2.y;
                    }
                };
            }
            else{
                comparator = new Comparator<CodelCursor>(){
                    @Override
                    public int compare(CodelCursor o1, CodelCursor o2) {
                        return o2.y - o1.y;
                    }
                };
            }
        }
        else if(directionPointer.isBottom()){
            if(even != 0){
                comparator = new Comparator<CodelCursor>(){
                    @Override
                    public int compare(CodelCursor o1, CodelCursor o2) {
                        return o2.x - o1.x;
                    }
                };
            }
            else {
                comparator = new Comparator<CodelCursor>(){
                    @Override
                    public int compare(CodelCursor o1, CodelCursor o2) {
                        return o1.x - o2.x;
                    }
                };
            }
        }
        else if(directionPointer.isLeft()){
            if(even != 0){
                comparator = new Comparator<CodelCursor>(){
                    @Override
                    public int compare(CodelCursor o1, CodelCursor o2) {
                        return o2.y - o1.y;
                    }
                };
            }
            else {
                comparator = new Comparator<CodelCursor>(){
                    @Override
                    public int compare(CodelCursor o1, CodelCursor o2) {
                        return o1.y - o2.y;
                    }
                };
            }
        }
        else if(directionPointer.isTop()){
            if(even != 0){
                comparator = new Comparator<CodelCursor>(){
                    @Override
                    public int compare(CodelCursor o1, CodelCursor o2) {
                        return o1.x - o2.x;
                    }
                };
            }
            else {
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

    public CodelCursor getNextCodel(CodelCursor codel){
		CodelCursor next = new CodelCursor(codel);
        DirectionPointer directionPointer = mMachine.getDirectionPointer();

		if (directionPointer.isRight()){
			next.x += 1;
		}
		else if (directionPointer.isBottom()){
			next.y += 1;
		}
		else if (directionPointer.isLeft()){
			next.x -= 1;
		}
		else if (directionPointer.isTop()){
			next.y -= 1;
		}

		return next;
	}

	public boolean isValid(CodelCursor codel){
		if(mModel.isValid(codel) == false){
			return false;
		}
		
		CodelColor value = mModel.getValue(codel);
		if (value == CodelColor.BLACK){
			return false;
		}

		return true;
	}

	public void switchDirection(int attempt){
		if ((attempt % 2) == 0){
            CodelChoser codelChoser = mMachine.getCodelChoser();
            codelChoser.switchState();
		}
		else{
            DirectionPointer directionPointer = mMachine.getDirectionPointer();
            directionPointer.rollClockWise();
		}
	}
	
	public void executeCommand(CodelCursor _current, CodelCursor _next, Integer _input){
		CodelColor currentColor = mModel.getValue(_current);
		CodelColor nextColor = mModel.getValue(_next);

        int deltaHue = nextColor.hue - currentColor.hue;
		int deltaDark = nextColor.dark - currentColor.dark;	

        try {
            mMachine.runCommand(deltaDark, deltaHue, _input);
        }
        catch (PietMachineExecutionError exception){
            mLogger.error("BAD COMMAND EXECUTE %d %d", deltaDark, deltaHue);
        }
	}

    public void run(){
        mLogger.info("Iterpreter run");
        mPaused = false;
        countSteps = 0;
        while(mPaused == false){
            //System.out.println("count steps" + countSteps);
            if (step() == false){
                break;
            }
        }
    }

	public boolean step(){
        if(mCurrentCodel.x == 6 && mCurrentCodel.y == 7){
            int x = 1;
        }

        int attempt = 1;
		ArrayList<CodelCursor> neighbours = mModel.getNeighbors(mCurrentCodel);

        CodelCursor codelOnEdge = getCodelOnEdge2(neighbours);
		
		boolean seenWhite = false;

		CodelColor value = mModel.getValue(mCurrentCodel);

        while (attempt <= 8){
            if(attempt == 4){
                int x =1;
            }
			Integer codelData = neighbours.size();
			
			CodelCursor nextCodel = getNextCodel(codelOnEdge);
            /*printf("current %d,%d; edge %d,%d; next %d,%d;pointer %d codel %d"
                    , mCurrentCodel.x,mCurrentCodel.y,codelOnEdge.x,codelOnEdge.y, nextCodel.x, nextCodel .y,
                    mDirectionPointer.ordinal(), mCodelChoser.ordinal());    */

            if (isValid(nextCodel) == false){
				attempt += 1;
				
				switchDirection(attempt);
						
				if(value == CodelColor.WHITE){
					continue;
				}
				
				//FIX ME
				mCurrentCodel = codelOnEdge;
				neighbours = mModel.getNeighbors(mCurrentCodel);
				codelOnEdge = getCodelOnEdge2(neighbours);
				//mLogger.info("FIX ME");
			}
			else if(value == CodelColor.WHITE){
				seenWhite = true;
				attempt = 0;
				codelOnEdge = nextCodel;
			}
			else{
				if(seenWhite == false){
					executeCommand(mCurrentCodel, nextCodel, codelData);
				}
				
				mCurrentCodel = nextCodel;
				countSteps += 1;
				
				return true;
			}
		}
		
		mLogger.info("STEP FAILED");
		return false;
	}

	public static void main(String[] args){
        CodelTableModel model = FakeData.getModel();
        Logger logger = new LoggerJavaSdkStdOut();

        PietMachine machine = new PietMachine();

        Interpreter interpreter = new Interpreter(logger, machine);
        interpreter.setInput(model);

        interpreter.run();
	}
}
