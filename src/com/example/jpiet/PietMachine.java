package com.example.jpiet;

import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

public class PietMachine {
    PietMachineStack mStack;
    Integer mValue;
    DirectionPointer mDirectionPointer;
    CodelChoser mCodelChoser;
    InOutSystem mInOutSystem;
    ArrayList<CommandRunListener> mCommandRunListeners;
    Command[][] mCommands;
    
    Command mLastCommand;
    
    private final int DARK_COUNT = 6;
    private final int HUE_COUNT = 3;

    public PietMachine(InOutSystem _inOutSystem) {
        mInOutSystem = _inOutSystem;

        mStack = new PietMachineStack();
        mValue = 0;

        mCommands = new Command[DARK_COUNT][HUE_COUNT];
        setupCommands();

        mCommandRunListeners = new ArrayList<CommandRunListener>();

        mDirectionPointer = new DirectionPointer();
        mCodelChoser = new CodelChoser();
    }

    public final void addCommandRunListener(CommandRunListener listener) {
        mCommandRunListeners.add(listener);
    }

    public final DirectionPointer getDirectionPointer() {
        return mDirectionPointer;
    }

    public final CodelChoser getCodelChoser() {
        return mCodelChoser;
    }

    public void setCommand(int dark, int hue, Command command)
            throws ArrayIndexOutOfBoundsException {
        mCommands[dark][hue] = command;
    }

    private void setupCommands() {
        // idle
        mCommands[0][0] = new Command("IDLE") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
            }
        };
        // push
        mCommands[0][1] = new Command("PUSH") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                _stack.push(_input);
                setRepresentation("%d", _input);
            }
        };
        // pop
        mCommands[0][2] = new Command("POP") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer removed = _stack.pop();
                setRepresentation("%d", removed);
            }
        };
        // add
        mCommands[1][0] = new Command("ADD") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(top + next);

                setRepresentation("%d %d", top, next);
            }
        };
        // sub
        mCommands[1][1] = new Command("SUB") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(next - top);

                setRepresentation("%d %d", next, top);
            }
        };
        // mul
        mCommands[1][2] = new Command("MUL") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(next * top);

                setRepresentation("%d %d", next, top);
            }
        };
        // DIV
        mCommands[2][0] = new Command("DIV") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(next / top);

                setRepresentation("%d %d", next, top);
            }
        };

        // MOD
        mCommands[2][1] = new Command("MOD") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(next % top);

                setRepresentation("%d %d", next, top);
            }
        };
        // NOT
        mCommands[2][2] = new Command("NOT") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer result = 0;

                if (top == 0) {
                    result = 1;
                }

                _stack.push(result);

                setRepresentation("%d", result);
            }
        };
        // GREATER
        mCommands[3][0] = new Command("GREATER") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                Integer result = 0;

                if (next > top) {
                    result = 1;
                }

                _stack.push(result);

                setRepresentation("%d %d", next, top);
            }
        };
        // POINTER
        mCommands[3][1] = new Command("POINTER") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();

                _direction.roll(top);

                setRepresentation("%d", top);
            }
        };
        // SWITCH
        mCommands[3][2] = new Command("SWITCH") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();

                _codelChoser.roll(top);

                setRepresentation("%d", top);
            }
        };
        // DUPLICATE
        mCommands[4][0] = new Command("DUPLICATE") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();

                _stack.push(top);
                _stack.push(top);

                setRepresentation("%d", top);
            }
        };
        // ROLL
        mCommands[4][1] = new Command("ROLL") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer num = _stack.pop();
                Integer depth = _stack.pop();
                num %= depth;

                if (depth <= 0 || num == 0) {
                    return;
                }

                int firstPosition = depth < 0 ? Math.abs(depth) : _stack.size()
                        - depth;

                int numWeight = (num < 0) ? 1 : 0;
                int x = (-1 * Math.abs(num)) + (depth * numWeight);
                int secondPosition = x > 0 ? x : _stack.size() + x;

                int aboveRange = _stack.size() - secondPosition;
                ArrayList<Integer> aboveElements = new ArrayList<Integer>();
                for (int i = 0; i < aboveRange; ++i) {
                    Integer value = _stack.pop();
                    aboveElements.add(value);
                }
                Collections.reverse(aboveElements);

                int belowRange = secondPosition - firstPosition;
                ArrayList<Integer> belowElements = new ArrayList<Integer>();
                for (int i = 0; i < belowRange; ++i) {
                    Integer value = _stack.pop();
                    belowElements.add(value);
                }
                Collections.reverse(belowElements);

                for (Integer value : aboveElements) {
                    _stack.push(value);
                }

                for (Integer value : belowElements) {
                    _stack.push(value);
                }

                setRepresentation("%d %d", num, depth);
            }
        };
        // IN_NUMBER
        mCommands[4][2] = new Command("IN_NUMBER") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) throws PietMachineExecutionError {
                try {
                    Integer value = _inOutSystem.read();
                    _stack.push(value);
                    setRepresentation("%d", value);
                } catch (IOException exception) {
                    String msg = exception.getMessage();
                    throw new PietMachineExecutionError(msg);
                }
            }
        };
        // IN_CHAR
        mCommands[5][0] = new Command("IN_CHAR") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) throws PietMachineExecutionError {
                try {
                    Integer value = _inOutSystem.read();
                    _stack.push(value);
                    setRepresentation("%d", value);
                } catch (IOException exception) {
                    String msg = exception.getMessage();
                    throw new PietMachineExecutionError(msg);
                }
            }
        };
        // OUT_NUMBER
        mCommands[5][1] = new Command("OUT_NUMBER") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                _inOutSystem.write(top);

                setRepresentation("%d", top);
            }
        };
        // OUT_CHAR
        mCommands[5][2] = new Command("OUT_CHAR") {

            public void onExec(PietMachineStack _stack, Integer _input,
                    DirectionPointer _direction, CodelChoser _codelChoser,
                    InOutSystem _inOutSystem) {
                Integer top = _stack.pop();

                char[] unicode = Character.toChars(top);
                String repr = new String(unicode);
                _inOutSystem.write(repr);

                setRepresentation("%s", repr);
            }
        };
    }

    public void init() {
        mStack.clear();
    }

    public Command getCommand(CodelColor currentColor, CodelColor nextColor) {
        int deltaHue = nextColor.hue - currentColor.hue;
        int deltaDark = nextColor.dark - currentColor.dark;
        int y = deltaHue;
        int x = deltaDark;
        
        if(Math.abs(x) > HUE_COUNT) {
            return null;
        }
        
        if(Math.abs(y) > DARK_COUNT) {
            return null;
        }
        
        if (deltaHue < 0) {
            y = DARK_COUNT - (-1 * deltaHue);
        }

        if (deltaDark < 0) {
            x = HUE_COUNT - (-1 * deltaDark);
        }

        Command command = mCommands[y][x];
        
        
        //System.out.printf("\nx,y -> %d,%d ", x,y);
        
        return command;
    }

    public void runCommand(CodelColor currentColor, CodelColor nextColor,
            Integer _input) throws PietMachineExecutionError {

        
        mLastCommand = getCommand(currentColor, nextColor);
        mLastCommand.exec(mStack, _input, mDirectionPointer, mCodelChoser,
                mInOutSystem);
        //System.out.printf("%s -->%d \n", command,_input);
        onRunCommand(mLastCommand);
    }

    private void onRunCommand(Command command) {
        for(CommandRunListener listener: mCommandRunListeners) {
            listener.onRunCommand(command, mStack);
        }
    }
    
    public class CommandInfo {
        public String tag;
        /**
         * color which may start command in current state.
         */
        public int expectedColor;
    }
    
    public void getCommandTags(ArrayList<String> _tags) {
        for (int y = 0; y < DARK_COUNT; y++ ) {
            for ( int x = 0; x < HUE_COUNT; x++ ) {
                Command command = mCommands[y][x];
                _tags.add(command.getTag());
            }
        }
    }
    
    public interface CommandOpportunityVisitor{
        public void acceptCommandOpportunity(String tag, int color);
    }
    
    public void calculateCommandOpportunity(int colorRGB, CommandOpportunityVisitor visitor) {
        CodelColor currentColor = CodelColor.findColor(colorRGB);
        
        for(CodelColor color : CodelColor.values()) {
            /*if( color.equals(currentColor)){
                continue;
            }*/
            
            Command command = getCommand(currentColor, color);
            if (command == null) {
                continue;
            }
            
            int argb = color.getARGB();
            visitor.acceptCommandOpportunity(command.getTag(), argb);
        }
    }

    public Command getLastCommand() {
        // TODO Auto-generated method stub
        return mLastCommand;
    }
}
