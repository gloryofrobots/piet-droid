package com.example.jpiet;

import java.util.LinkedList;
import java.util.List;

//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//
//import javax.imageio.ImageIO;




public class Piet {
    
	CodelTableModel mModel;
	Interpreter mInterpreter;
	PietMachine mMachine;
	Command mLastCommand;
	
	
	public Piet(Logger logger, InOutSystem inOutSystem){
        mMachine = new PietMachine(inOutSystem);
        PolicyStorage policy = PolicyStorage.getInstance();
        policy.setLogger(logger);
        policy.setDebugMode(false);
        policy.setModelScaner(CodelTableModelScannerRecursiveFold.class);
        
        mInterpreter = new Interpreter(mMachine);
	}
	
	public void addCommandRunListener(CommandRunListener listener){
		mMachine.addCommandRunListener(listener);
		
		/*machine.setCommandRunListener( new PietMachine.CommandRunListener() {
             public void onRunCommand(final Command command,
                     final PietMachineStack stack) {
                 System.out.print(command);
                 System.out.println(" Stack : " + stack.toString());
             }
         });*/
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
	
	public void createModel(int width, int height){
		mModel = CodelTableModel.createEmptyCodelTableModel(width, height, CodelColor.WHITE);
		mInterpreter.setInput(mModel);
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
//	
//    public static CodelTableModel createModel(String _filename){
//        try {
//            BufferedImage img = ImageIO.read(new File(_filename));
//            CodelTableModel model = new CodelTableModel(img.getWidth(), img.getHeight(), CodelColor.WHITE);
//
//            for(int y = 0; y < img.getHeight(); y++){
//                for(int x = 0; x < img.getWidth(); x++){
//                    int pixel = img.getRGB(x, y);
//                    int R = (pixel & 0xFF0000) >> 16;
//                    int G = (pixel & 0xFF00) >> 8;
//                    int B = (pixel & 0xFF);
//
//                    CodelColor color = CodelColor.findColor(R, G, B);
//                    model.set(x, y, color);
//                }
//            }
//            return model;
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//      return  null;
//    }
//    //CODEL SIZE !!!!!!!!
//    public static void main(String[] args) {
//        //CodelTableModel model = FakeData.getPietModel();
//        String filename ;
//        
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/fib.gif"; // ERROR
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/hanoi.gif"; //ERROR
//        
//        
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/helloworld-mondrian.png";
//        
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/99bottles.png";
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/cowsay.png";
//        
//        
//        
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/hw1-1.gif"; //ERROR
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/hw2-1.gif";
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/piet_pi.png"; //ERROR
//        
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/adder.png";
//        
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/alpha_filled.png";
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/euclid_clint.png";
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/fizzbuzz.png";
//        
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/helloworld-piet.gif";
//        
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/hw3-1.gif";
//        
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/hw6.png";
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/piet_factorial.png";
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/Piet_hello.png";
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/power2.png";
//        
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/sortgnu.png"; //ERROR ?
//        
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/Piet-1.gif"; // ERROR
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/tetris.png";
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/hw5.png";
//        
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/piet_bfi.gif";
//        //filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/dayofweek.png";
//        
//        //filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/pietquest.png";
//        
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/helloWorld_small.png";
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/piet.png";
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/addition.png"; //ERROR?
//        filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/piet_bfi.gif";
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/japh.png";
//        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/piet_pi.png";
//        
//        
//        //filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/hw5.png";
//        //filename = "/home/gloryofrobots/develop/eclipse_workspace/JPiet/tests/images/erat2.png"; //ERROR
//        CodelTableModel model = Piet.createModel(filename);
//        //CodelTableModel model = Piet.createModel("/home/gloryofrobots/bin/hipi/piet.png");
//        //CodelTableModel model = Piet.createModel("/home/gloryofrobots/bin/hipi/addition.png");
//        
//        
//        Logger logger = new LoggerJavaSdkStdOut();
//        
//        PolicyStorage policy = PolicyStorage.getInstance();
//        policy.setLogger(logger);
//        policy.setDebugMode(false);
//        policy.setModelScaner(CodelTableModelScanerIterative.class);
//        //CodelTableModelScanerRecursive
//        
//        
//        InOutSystem inOutSystem = new InOutSystemJDK();
//        PietMachine machine = new PietMachine(inOutSystem);
//        
//        Interpreter interpreter = new Interpreter(machine);
//        interpreter.setInput(model);
//
//        interpreter.run();
//
//    }
}