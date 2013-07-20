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
        
        try {
            mMachine.runCommand(currentColor, nextColor, _input);
            return true;
        }
        catch (PietMachineExecutionError exception) {
            //TODO ERROR REPORTING
            mLogger.error("BAD COMMAND EXECUTE ON STEP %d command %s error: %s"
                    ,mStepNumber, mMachine.getLastCommand().getTag(), exception.toString());
            return false;
        }
    }

    private void findCodelOnEdge(CodelArea area) {
        DirectionPointer directionPointer = mMachine.getDirectionPointer();
        CodelChoser codelChoser = mMachine.getCodelChoser();
        int even = 1;
        /*if (directionPointer.getState() < 2){
           even = 1;
        }*/
        //FIXME FUCK!
        
        if (codelChoser.isRight()) {
            even = 0;
        }

        //int even = directionPointer.getState() % 2;
       
        if (directionPointer.isRight()) {
            if (even != 0) {
                mEdgeCodel.set(area.maxXMinY());
            } else {
                mEdgeCodel.set(area.maxXMaxY());
            }
        } else if (directionPointer.isBottom()) {
            if (even != 0) {
                mEdgeCodel.set(area.maxYMaxX());
            } else {
                mEdgeCodel.set(area.maxYMinX());
            }

        } else if (directionPointer.isLeft()) {
            if (even != 0) {
                mEdgeCodel.set(area.minXMaxY());
            } else {
                mEdgeCodel.set(area.minXMinY());
            }
        } else if (directionPointer.isTop()) {
            if (even != 0) {
                mEdgeCodel.set(area.minYMinX());
            } else {
                mEdgeCodel.set(area.minYMaxX());
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
         mMachine.init();
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
        CodelColor currentColor = mModel.getValue(mCurrentCodel);
        int toggle = 0;
        boolean whiteCrossed = (currentColor == CodelColor.WHITE);
        boolean inWhite = false;
        
        if(currentColor == CodelColor.BLACK) {
            mLogger.error("We are on black hole :)");
            return false;
        }
        
        int weight = 0;
        CodelArea area;
        //??
        mEdgeCodel.set(mCurrentCodel);
        CodelChoser codelChoser = mMachine.getCodelChoser();
        DirectionPointer directionPointer = mMachine.getDirectionPointer();
        //FIXME
        if(mStepNumber >= 5){
            int x = 2;
        }
        for(int attempt = 0; attempt < 8; ++attempt) {
            
            CodelColor nextColor;
            
            if(currentColor == CodelColor.WHITE) {
                if(attempt == 0){
                    mLogger.info(" special case: we at a white codel");
                }
                weight = 1;
            }
            else {
                area = findCodelArea(mCurrentCodel);
                findCodelOnEdge(area);
                weight = area.size;
            }
            
            findNextCodel(mEdgeCodel);
            if (isValid(mNextCodel) == false) {
                nextColor = null;
            }
            else {
                nextColor = mModel.getValue(mNextCodel);
            }
            
            if(nextColor == CodelColor.WHITE) {
                while(nextColor == CodelColor.WHITE) {
                    findNextCodel(mNextCodel);
                    if(mModel.isValid(mNextCodel) == false) {
                        break;
                    }
                    nextColor = mModel.getValue(mNextCodel);
                }
                
                if (isValid(mNextCodel)) {
                    whiteCrossed = true;
                }
                else {
                    whiteCrossed = true;
                    nextColor = CodelColor.WHITE;
                    findPreviousCodel(mNextCodel);
                }
            }
            if(isValid(mNextCodel) == false) {
                if(currentColor == CodelColor.WHITE || inWhite) {
                    codelChoser.switchState();
                    directionPointer.rollClockWise();
                    mLogger.info("we in white codel turn dp and cc both");
                }
                else {
                    if ((toggle % 2) == 0) {
                        codelChoser.switchState();
                    }
                    else {
                        directionPointer.rollClockWise();
                    }
                    
                }
                
                toggle++;
            }
            else {
                System.out.printf("step %d current %d,%d\n",mStepNumber, mCurrentCodel.x,mCurrentCodel.y);
                /*System.out.printf("step %d current %d,%d; edge %d,%d; next %d,%d;pointer %s codel %s\n"
                ,mStepNumber , mCurrentCodel.x,mCurrentCodel.y,mEdgeCodel.x,mEdgeCodel.y, mNextCodel.x, mNextCodel .y,
                directionPointer.toString(), codelChoser.toString());*/
                mStepNumber++;
                if(whiteCrossed) {
                    //mLogger.info(" no command is executed - anything is fine");
                }
                else {
                    boolean result = executeCommand(mCurrentCodel, mNextCodel, weight);
                    if (result == false) {
                        mLogger.info("COMMAND ERROR");
                        //ERROR HANDLING HERE
                        return false;
                    }
                }
                
                mCurrentCodel.set(mNextCodel);
                return true;
            }
        }
        
        mLogger.info("STEP FAILED");
        return false;
    }
    
    
    private void findPreviousCodel(Codel codel) {
        int x = codel.x;
        int y = codel.y;

        DirectionPointer directionPointer = mMachine.getDirectionPointer();

        if (directionPointer.isRight()) {
            x -= 1;
        } else if (directionPointer.isBottom()) {
            y -= 1;
        } else if (directionPointer.isLeft()) {
            x += 1;
        } else if (directionPointer.isTop()) {
            y += 1;
        }

        mNextCodel.set(x, y);
    }

    
	public Codel getCurrentCodel() {
		// TODO Auto-generated method stub
		return mCurrentCodel;
	}
}
