/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.pieteditor;

abstract class Command {
    /*
     * Single instruction in stack machine runtime execution
     */
    String mRepr;
    Command() {
        mRepr = new String();
    }

    /**
     * Set`s string representation of command.
     * Each derived class must do that for logging cappabilities
     * @param commandName
     * @param format
     * @param args
     */
    public void setRepresentation(String commandName, String format, Object... args) {
        mRepr = commandName + " " + String.format(format, args);
    }

    @Override
    public String toString(){
        return mRepr;
    }
    /**
     * Execute command with input data
     * @param _stack
     * @param _input
     * @param _directionPointer
     * @param _codelChoser
     * @throws PietMachineExecutionError
     */
    public void exec(PietMachineStack _stack, Integer _input, DirectionPointer _directionPointer,
            CodelChoser _codelChoser, InOutSystem _inOutSystem)
            throws PietMachineExecutionError {
        try {
            this.onExec(_stack, _input, _directionPointer, _codelChoser, _inOutSystem);
            //System.out.println(_stack);
        } catch (NullPointerException e) {
            //FIXME
            String str = "Stack Manipulation Error " + e.toString() 
                    + " message : " + e.getMessage();
            throw new PietMachineExecutionError(str);
        }
    }

    public abstract void onExec(PietMachineStack _stack, Integer _input,
            DirectionPointer _directionPointer, CodelChoser _codelChoser, InOutSystem _inOutSystem) throws PietMachineExecutionError;
}
