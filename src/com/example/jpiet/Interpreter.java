package com.example.jpiet;

public class Interpreter {
    
	public interface ExecutionStepListener{
		public void beforeStep(Codel currentCodel, DirectionPointer directionPointer, CodelChoser codelChoser);
	}

    int mStepNumber;

    CodelTableModel mModel;
    CodelTableModelScaner mModelScaner;

    Logger mLogger;
    PietMachine mMachine;

    Codel mCurrentCodel;
    Codel mNextCodel;
    Codel mEdgeCodel;

    InOutSystem mInOutSystem;
    ExecutionStepListener mExecutionStepListener;
    
	public Interpreter(Logger _logger, PietMachine _machine, InOutSystem inOutSystem) {
        // TODO Auto-generated constructor stub
        mLogger = _logger;
        mMachine = _machine;
        mInOutSystem = inOutSystem;
        
        mStepNumber = 0;

        mCurrentCodel = new Codel();
        mNextCodel = new Codel();
        mEdgeCodel = new Codel();

        mModelScaner = new CodelTableModelScaner();
    }
	
    /**
	 * @param mExecutionStepListener the mExecutionStepListener to set
	 */
	public void setExecutionStepListener( ExecutionStepListener executionStepListener) {
		mExecutionStepListener = executionStepListener;
	}

	
    public void setInput(CodelTableModel _model) {
        mModel = _model;
        mModelScaner.setModel(_model);
    }

    public int getStepNumber() {
        return mStepNumber;
    }
    
    private void findNextCodel(Codel codel) {
        int x = codel.x;
        int y = codel.y;

        DirectionPointer directionPointer = mMachine.getDirectionPointer();

        if (directionPointer.isRight()) {
            x += 1;
        } else if (directionPointer.isBottom()) {
            y += 1;
        } else if (directionPointer.isLeft()) {
            x -= 1;
        } else if (directionPointer.isTop()) {
            y -= 1;
        }

        mNextCodel.set(x, y);
    }

    private boolean isValid(Codel codel) {
        if (mModel.isValid(codel) == false) {
            return false;
        }

        CodelColor value = mModel.getValue(codel);
        if (value == CodelColor.BLACK) {
            return false;
        }

        return true;
    }

    private void switchDirection(int attempt) {
        if ((attempt % 2) == 0) {
            CodelChoser codelChoser = mMachine.getCodelChoser();
            codelChoser.switchState();
        } else {
            DirectionPointer directionPointer = mMachine.getDirectionPointer();
            directionPointer.rollClockWise();
        }
    }

    private boolean executeCommand(Codel _current, Codel _next, Integer _input) {
        CodelColor currentColor = mModel.getValue(_current);
        CodelColor nextColor = mModel.getValue(_next);

        int deltaHue = nextColor.hue - currentColor.hue;
        int deltaDark = nextColor.dark - currentColor.dark;

        try {
            mMachine.runCommand(deltaDark, deltaHue, _input);
            return true;
        }
        catch (PietMachineExecutionError exception) {
            mLogger.error("BAD COMMAND EXECUTE delta dark :%d "
                    + " delta hue %d error: %s", deltaDark, deltaHue, exception.toString());
            return false;
        }
    }

    /**In Piet Language specifiation chosing between two variants of moving
     * made by CodelChoser,
     * but this caught error on common used HelloWorld example.
     * I don`t know why,
     * but in iterpreters often used DirectionalPointer < 2 for switching between two variants of moving
     * So i decide to use the same method.
     */
    private void findCodelOnEdge(CodelArea area) {
        DirectionPointer directionPointer = mMachine.getDirectionPointer();
        CodelChoser codelChoser = mMachine.getCodelChoser();
        int even = 0;
        if (directionPointer.getState() < 2){
           even = 1;
        }
        //FIX ME FUCK!
        /*
        if (codelChoser.isRight()) {
            even = 1;
        }*/

        //int even = directionPointer.getState() % 2;
       
        if (directionPointer.isRight()) {
            if (even != 0) {
                mEdgeCodel.set(area.minYCodel);
            } else {
                mEdgeCodel.set(area.maxYCodel);
            }
        } else if (directionPointer.isBottom()) {
            if (even != 0) {
                mEdgeCodel.set(area.maxXCodel);
            } else {
                mEdgeCodel.set(area.minXCodel);
            }

        } else if (directionPointer.isLeft()) {
            if (even != 0) {
                mEdgeCodel.set(area.maxYCodel);
            } else {
                mEdgeCodel.set(area.minYCodel);
            }
        } else if (directionPointer.isTop()) {
            if (even != 0) {
                mEdgeCodel.set(area.minXCodel);
            } else {
                mEdgeCodel.set(area.maxXCodel);
            }
        }
    }

    private CodelArea findCodelArea(Codel codel) {
        mModelScaner.scanForCodelNeighbors(codel.x, codel.y);
        CodelArea area = mModelScaner.getCodelArea();
        return area;
    }
    
    public void init(){
    	 mStepNumber = 0;
         mCurrentCodel.set(0, 0);
    }
    
    public void run() {
        mLogger.info("Iterpreter run");

        mStepNumber = 0;
        while (true) {
            if (step() == false) {
                break;
            }
        }
    }

    public void notifyBeforeStep() {
    	if (mExecutionStepListener == null) {
    		return;
    	}
    	
    	//TODO May be use public constants
    	DirectionPointer directionPointer = mMachine.getDirectionPointer();
        CodelChoser codelChoser = mMachine.getCodelChoser();
    	
    	mExecutionStepListener.beforeStep(mCurrentCodel, directionPointer, codelChoser);
    }
    
    public boolean step() {
    	notifyBeforeStep();
    	
        CodelArea area = findCodelArea(mCurrentCodel);
        findCodelOnEdge(area);

        boolean seenWhite = false;

        CodelColor value = mModel.getValue(mCurrentCodel);

        int attempt = 1;
        while (attempt <= 8) {         
            Integer codelData = area.size;

            findNextCodel(mEdgeCodel);
            /*printf("current %d,%d; edge %d,%d; next %d,%d;pointer %d codel %d"
            , mCurrentCodel.x,mCurrentCodel.y,codelOnEdge.x,codelOnEdge.y, nextCodel.x, nextCodel .y,
            mDirectionPointer.ordinal(), mCodelChoser.ordinal());    */

            if (isValid(mNextCodel) == false) {
                attempt += 1;

                switchDirection(attempt);

                if (value == CodelColor.WHITE) {
                    continue;
                }

                //FIX ME
                mCurrentCodel.set(mEdgeCodel);
                
                area = findCodelArea(mCurrentCodel);
                findCodelOnEdge(area);
            }
            else if (value == CodelColor.WHITE) {
                seenWhite = true;
                attempt = 0;
                mEdgeCodel.set(mNextCodel);
            }
            else {
                if (seenWhite == false) {
                    boolean result = executeCommand(mCurrentCodel, mNextCodel, codelData);
                    if (result == false) {
                        //ERROR HANDLING HERE
                        return false;
                    }
                }

                mCurrentCodel.set(mNextCodel);
                mStepNumber += 1;
                return true;
            }
        }

        mLogger.info("STEP FAILED");
        return false;
    }

	public Codel getCurrentCodel() {
		// TODO Auto-generated method stub
		return mCurrentCodel;
	}
}
