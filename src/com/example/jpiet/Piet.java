package com.example.jpiet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/*
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;*/


public class Piet {
	CodelTableModel mModel;
	Interpreter mInterpreter;
	PietMachine mMachine;
	Command mLastCommand;
	
	public Piet(Logger logger, InOutSystem inOutSystem){
        mMachine = new PietMachine(inOutSystem);
        
        
        mInterpreter = new Interpreter(logger, mMachine, inOutSystem);
        
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
	
	public int getStepNumber(){
	    return mInterpreter.getStepNumber();
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
		mModel = new CodelTableModel(width, height, CodelColor.WHITE);
		/*if (mInterpreter.isRun()) {
			mInterpreter.stop();
		}*/
		
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
	
    public static CodelTableModel createModel(String _filename){
        try {
            BufferedImage img = ImageIO.read(new File(_filename));
            CodelTableModel model = new CodelTableModel(img.getWidth(), img.getHeight(), CodelColor.WHITE);

            for(int y = 0; y < img.getHeight(); y++){
                for(int x = 0; x < img.getWidth(); x++){
                    int pixel = img.getRGB(x, y);
                    int R = (pixel & 0xFF0000) >> 16;
                    int G = (pixel & 0xFF00) >> 8;
                    int B = (pixel & 0xFF);

                    CodelColor color = CodelColor.findColor(R, G, B);
                    model.set(x, y, color);
                }
            }
            return model;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
      return  null;
    }
    //CODEL SIZE !!!!!!!!
    public static void main(String[] args) {
        //CodelTableModel model = FakeData.getPietModel();
        
        String filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/helloworld-mondrian.png";
        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/japh.png";
        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/99bottles.png";
        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/cowsay.png";
        
        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/fib.gif"; // ERROR
        
        
        //filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/hanoi.gif"; //ERROR
        
        //filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/hw1-1.gif"; //ERROR
        filename = "/home/gloryofrobots/develop/piet/hipi/programs/in question/hw2-1.gif";
        
        CodelTableModel model = Piet.createModel(filename);
        //CodelTableModel model = Piet.createModel("/home/gloryofrobots/bin/hipi/piet.png");
        //CodelTableModel model = Piet.createModel("/home/gloryofrobots/bin/hipi/addition.png");
        
        InOutSystemTest inOutTest = new InOutSystemTest("3\n");
        
        Logger logger = new LoggerJavaSdkStdOut();

        InOutSystem inOutSystem = new InOutSystemJDK();
        PietMachine machine = new PietMachine(inOutSystem);

        Interpreter interpreter = new Interpreter(logger, machine, inOutSystem);
        interpreter.setInput(model);

        interpreter.run();
        
        System.out.println(inOutTest.output);
    }

    public CodelTableModel getModel() {
        return mModel;
    }
}