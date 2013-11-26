package com.example.jpiet;

public class Interpreter {
    public interface ExecutionStepListener {
        public void beforeStep(Codel currentCodel,
                DirectionPointer directionPointer, CodelChoser codelChoser);
    }

    int mStepNumber;

    CodelTableModel mModel;
    CodelTableModelScanner mModelScanner;

    PietMachine mMachine;
    boolean mIsDebug;
    Codel mCurrentCodel;
    Codel mNextCodel;
    Codel mEdgeCodel;
    private int mLimit = -1;

    ExecutionStepListener mExecutionStepListener;

    public Interpreter(PietMachine _machine, CodelTableModelScanner scaner, boolean isDebug) {
        mMachine = _machine;
        mStepNumber = 0;
        mIsDebug = isDebug;
        mCurrentCodel = new Codel();
        mNextCodel = new Codel();
        mEdgeCodel = new Codel();
        
        mModelScanner = scaner;
    }

    public void setExecutionStepListener(
            ExecutionStepListener executionStepListener) {
        mExecutionStepListener = executionStepListener;
    }

    public void setInput(CodelTableModel _model) {
        if( _model.equals(mModel)) {
            return;
        }
        
        mModel = _model;
        mModelScanner.setModel(_model);
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
        return mCurrentCodel;
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

    private void executeCommand(Codel _current, Codel _next, Integer _input) {
        CodelColor currentColor = mModel.getValue(_current);
        CodelColor nextColor = mModel.getValue(_next);
        try {
            mMachine.runCommand(currentColor, nextColor, _input);
        } catch (PietMachineExecutionError exception) {
            Piet.logger()
                    .error("Step %d %s ERROR : %s",
                            mStepNumber, mMachine.getLastCommand().getTag(),
                            exception.getMessage());
        }
    }

    private void findCodelOnEdge(CodelArea area) {
        DirectionPointer directionPointer = mMachine.getDirectionPointer();
        CodelChoser codelChoser = mMachine.getCodelChoser();

        boolean isLeft = codelChoser.isLeft();

        if (directionPointer.isRight()) {
            if (isLeft) {
                mEdgeCodel.set(area.maxXMinY());
            } else {
                mEdgeCodel.set(area.maxXMaxY());
            }
        } else if (directionPointer.isBottom()) {
            if (isLeft) {
                mEdgeCodel.set(area.maxYMaxX());
            } else {
                mEdgeCodel.set(area.maxYMinX());
            }

        } else if (directionPointer.isLeft()) {
            if (isLeft) {
                mEdgeCodel.set(area.minXMaxY());
            } else {
                mEdgeCodel.set(area.minXMinY());
            }
        } else if (directionPointer.isTop()) {
            if (isLeft) {
                mEdgeCodel.set(area.minYMinX());
            } else {
                mEdgeCodel.set(area.minYMaxX());
            }
        }
    }

    private CodelArea findCodelArea(Codel codel) {
        mModelScanner.scanForCodelNeighbors(codel.x, codel.y);
        CodelArea area = mModelScanner.getCodelArea();
        debugTrace("STEP:%d %s\n", mStepNumber, area);
        return area;
    }
    
    public void debugTrace(String format, Object ... args) {
       if(mIsDebug == false) {
           return;
       }
        Piet.logger().info(format, args);
    }
    
    public void init() {
        mStepNumber = 0;
        mCurrentCodel.set(0, 0);
        mMachine.init();
    }

    public void run() {
        Piet.logger().info("Iterpreter run");

        mStepNumber = 0;
        while (true) {
            if (step() == false) {
                break;
            }
            if (mLimit != -1 && mStepNumber >= mLimit) {
                break;
            }
        }
    }

    public boolean step() {
        CodelColor currentColor = mModel.getValue(mCurrentCodel);
        boolean whiteCrossed = (currentColor == CodelColor.WHITE);

        if (currentColor == CodelColor.BLACK) {
            Piet.logger()
                    .error("We are on black hole :)");
            return false;
        }

        int weight = 0;
        CodelArea area;
        // ??
        mEdgeCodel.set(mCurrentCodel);
        CodelChoser codelChoser = mMachine.getCodelChoser();
        DirectionPointer directionPointer = mMachine.getDirectionPointer();

        for (int attempt = 0; attempt < 8; ++attempt) {
            
            if (currentColor == CodelColor.WHITE) {
                if (attempt == 0) {
                    debugTrace("special case: we at a white codel");
                }
                weight = 1;
            } else {
                area = findCodelArea(mCurrentCodel);
                findCodelOnEdge(area);
                weight = area.size;
            }

            findNextCodel(mEdgeCodel);
            
            CodelColor nextColor = null;
            
            if (isValid(mNextCodel)) {
                nextColor = mModel.getValue(mNextCodel);
            }
            
            if (nextColor == CodelColor.WHITE) {
                while (nextColor == CodelColor.WHITE) {
                    findNextCodel(mNextCodel);
                    if (mModel.isValid(mNextCodel) == false) {
                        break;
                    }
                    nextColor = mModel.getValue(mNextCodel);
                }

                if (isValid(mNextCodel)) {
                    whiteCrossed = true;
                } else {
                    whiteCrossed = true;
                    nextColor = CodelColor.WHITE;
                    findPreviousCodel(mNextCodel);
                }
            }
            if (isValid(mNextCodel) == false) {
                if (currentColor == CodelColor.WHITE) {
                    codelChoser.switchState();
                    directionPointer.rollClockWise();
                    debugTrace("we in white codel turn dp and cc both");
                } else {
                    if ((attempt % 2) == 0) {
                        codelChoser.switchState();
                    } else {
                        directionPointer.rollClockWise();
                    }
                }
            } else {
                    debugTrace("step %d current %d,%d", mStepNumber,
                        mCurrentCodel.x, mCurrentCodel.y);
                    /*
                     * System.out.printf(
                     * "step %d current %d,%d; edge %d,%d; next %d,%d;pointer %s codel %s\n"
                     * ,mStepNumber ,
                     * mCurrentCodel.x,mCurrentCodel.y,mEdgeCodel.
                     * x,mEdgeCodel.y, mNextCodel.x, mNextCodel .y,
                     * directionPointer.toString(), codelChoser.toString());
                     */
                mStepNumber++;
                if (!whiteCrossed) {
                    executeCommand(mCurrentCodel, mNextCodel, weight);
                } else {
                    // PolicyStorage.getInstance().getLogger().info(" no command is executed - anything is fine");
                }

                mCurrentCodel.set(mNextCodel);
                return true;
            }
        }
        return false;
    }

    public void setLimit(int limit) {
        mLimit = limit;
    }
}
