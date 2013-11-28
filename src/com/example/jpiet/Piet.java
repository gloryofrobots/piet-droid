package com.example.jpiet;

public class Piet {
	CodelTableModel mModel;
	Interpreter mInterpreter;
	PietMachine mMachine;
	Command mLastCommand;
	static private Logger sLogger;
    
	public enum ScanerType{
	    RecursiveFold, Recursive,Iterative;
	}
	
	public static Piet createPiet(InOutSystem inOutSystem, boolean debugMode) {
	    return createPiet(inOutSystem, debugMode, ScanerType.RecursiveFold);
	}
	
	public static Piet createPiet(InOutSystem inOutSystem, boolean debugMode, ScanerType scanerType) {
        return new Piet(inOutSystem, debugMode, scanerType);
    }
	
	private Piet(InOutSystem inOutSystem, boolean debugMode, ScanerType scanerType){
        mMachine = new PietMachine(inOutSystem);
        CodelTableModelScanner scaner = createModelScaner(scanerType);
        mInterpreter = new Interpreter(mMachine, scaner, debugMode);
	}
	
	public CodelTableModelScanner createModelScaner(ScanerType scanerType) {
	    Class<? extends CodelTableModelScanner> scanerClass = null;
	    switch(scanerType) {
        case RecursiveFold:
            scanerClass = CodelTableModelScannerRecursiveFold.class;
            break;
        case Recursive:
            scanerClass = CodelTableModelScannerRecursive.class;
            break;
        case Iterative:
            scanerClass = CodelTableModelScannerIterative.class;
            break;
        }
	    try {
            return (CodelTableModelScanner) scanerClass.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
	
	public void addCommandRunListener(CommandRunListener listener){
		mMachine.addCommandRunListener(listener);
	}
	
	public CodelTableModel getModel() {
	        return mModel;
	    }
	    
	public int getStepNumber(){
	    return mInterpreter.getStepNumber();
	}
	
	public InOutSystem getInOutSystem() {
	    return mMachine.getInOutSystem();
	}
	
	public DirectionPointer getDirectionPointer() {
		return mMachine.getDirectionPointer();
	}
	
	public CodelChoser getCodelChoser() {
		return mMachine.getCodelChoser();
	}
	
	public Codel getCurrentCodel() {
		return mInterpreter.getCurrentCodel();
	}
	
	public void setExecutionStepListener(Interpreter.ExecutionStepListener listener){
		mInterpreter.setExecutionStepListener(listener);
	}
	
	public void calculateCommandOpportunity(int color, PietMachine.CommandOpportunityVisitor visitor) {
	    mMachine.calculateCommandOpportunity(color, visitor);
	}
	
	public CodelTableModel createModel(int width, int height){
		return CodelTableModel.createEmptyCodelTableModel(width, height, CodelColor.WHITE);
	}
	
	public void setNewModel(int width, int height) {
	    setModel(createModel(width, height));
	}
	
	public void setModel(CodelTableModel model){
        mModel = model;
        mInterpreter.setInput(mModel);
    }
    
	public void clear(){
	    mModel.fillWithColor(CodelColor.WHITE);
	}
	
	public void setColor(int x, int y, int color) {
		CodelColor codel = CodelColor.findColor(color);
		setCodel(x, y, codel);
	}
	
	public void setCodel(int x, int y, CodelColor codel) {
		 mModel.set(x, y, codel);
	}
	
	public void init() {
		mInterpreter.init();
	}
	
	public void run(){
		init();
		mInterpreter.run();
	}
	
	public boolean step(){
		return mInterpreter.step();
	}
	
	public static void setLogger(Logger logger) {
        sLogger = logger;
    }
	
	public static synchronized Logger logger() {
        return sLogger;
    }
}