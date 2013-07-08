package com.example.pieteditor;

import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

public class PietMachine {

    public interface CommandRunListener {
        public void onRunCommand(final Command command, final PietMachineStack stack);
    }

    PietMachineStack mStack;
    Integer mValue;
    DirectionPointer mDirectionPointer;
    CodelChoser mCodelChoser;
    InOutSystem mInOutSystem;
    CommandRunListener mCommandRunListener;
    Command[][] mCommands;

    private final int DARK_COUNT = 6;
    private final int HUE_COUNT = 3;

    public PietMachine(InOutSystem _inOutSystem) {
        mInOutSystem = _inOutSystem;

        mStack = new PietMachineStack();
        mValue = 0;

        mCommands = new Command[DARK_COUNT][HUE_COUNT];

        mCommandRunListener = null;

        mDirectionPointer = new DirectionPointer();
        mCodelChoser = new CodelChoser();

        setupCommands();
    }

    public final void setCommandRunListener(CommandRunListener listener) {
        mCommandRunListener = listener;
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
        //idle
        mCommands[0][0] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
            }
        };
        //push
        mCommands[0][1] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                _stack.push(_input);
                setRepresentation("PUSH", "%d", _input);
            }
        };
        //pop
        mCommands[0][2] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer removed = _stack.pop();
                setRepresentation("POP", "%d", removed);
            }
        };
        //add
        mCommands[1][0] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(top + next);

                setRepresentation("ADD", "%d %d", top, next);
            }
        };
        //sub
        mCommands[1][1] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(next - top);

                setRepresentation("SUB", "%d %d", next, top);
            }
        };
        //mul
        mCommands[1][2] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(next * top);

                setRepresentation("MUL", "%d %d", next, top);
            }
        };
        //DIV
        mCommands[2][0] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(next / top);

                setRepresentation("DIV", "%d %d", next, top);
            }
        };

        //MOD
        mCommands[2][1] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                _stack.push(next % top);

                setRepresentation("MOD", "%d %d", next, top);
            }
        };
        //NOT
        mCommands[2][2] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer result = 0;

                if (top == 0) {
                    result = 1;
                }

                _stack.push(result);

                setRepresentation("NOT", "%d", result);
            }
        };
        //GREATER
        mCommands[3][0] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                Integer next = _stack.pop();
                Integer result = 0;

                if (next > top) {
                    result = 1;
                }

                _stack.push(result);

                setRepresentation("GREATER", "%d %d", next, top);
            }
        };
        //POINTER
        mCommands[3][1] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();

                _direction.roll(top);

                setRepresentation("POINTER", "%d", top);
            }
        };
        //SWITCH
        mCommands[3][2] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();

                _codelChoser.roll(top);

                setRepresentation("SWITCH", "%d", top);
            }
        };
        //DUPLICATE
        mCommands[4][0] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();

                _stack.push(top);
                _stack.push(top);

                setRepresentation("DUPLICATE", "%d", top);
            }
        };
        //ROLL
        mCommands[4][1] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer num = _stack.pop();
                Integer depth = _stack.pop();
                num %= depth;

                if (depth <= 0 || num == 0) {
                    return;
                }

                int firstPosition = depth < 0 ? Math.abs(depth) : _stack.size() - depth;

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

                setRepresentation("ROLL", "%d %d", num, depth);
            }
        };
        //IN_NUMBER
        mCommands[4][2] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem)
                    throws PietMachineExecutionError {
                try {
                    Integer value = _inOutSystem.read();
                    _stack.push(value);
                    setRepresentation("IN_NUMBER", "%d", value);
                } catch (IOException exception) {
                    String msg = exception.getMessage();
                    throw new PietMachineExecutionError(msg);
                }
            }
        };
        //IN_CHAR
        mCommands[5][0] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem)
                    throws PietMachineExecutionError {
                try {
                    Integer value = _inOutSystem.read();
                    _stack.push(value);
                    setRepresentation("IN_CHAR", "%d", value);
                } catch (IOException exception) {
                    String msg = exception.getMessage();
                    throw new PietMachineExecutionError(msg);
                }
            }
        };
        //OUT_NUMBER
        mCommands[5][1] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();
                _inOutSystem.write(top);

                setRepresentation("OUT_NUMBER", "%d", top);
            }
        };
        //OUT_CHAR
        mCommands[5][2] = new Command() {

            public void onExec(PietMachineStack _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser, InOutSystem _inOutSystem) {
                Integer top = _stack.pop();

                char[] unicode = Character.toChars(top);
                String repr = new String(unicode);
                _inOutSystem.write(repr);

                setRepresentation("OUT_CHAR", "%s", repr);
            }
        };
    }

    public void runCommand(int _dark, int _hue, Integer _input) throws PietMachineExecutionError {
        int y = _hue;
        int x = _dark;
        if (_hue < 0) {
            y = DARK_COUNT - (-1 * _hue);
        }
        if (_dark < 0) {
            x = HUE_COUNT - (-1 * _dark);
        }

        //String format = String.format("%d,%d (%d)", x,y,_input);
        //System.out.println(format);
        Command command = mCommands[y][x];
        command.exec(mStack, _input, mDirectionPointer, mCodelChoser, mInOutSystem);

        onRunCommand(command);
    }

    private void onRunCommand(Command command) {
        if (mCommandRunListener == null) {
            return;
        }

        mCommandRunListener.onRunCommand(command, mStack);
    }
}
