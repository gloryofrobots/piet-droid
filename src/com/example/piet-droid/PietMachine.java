package com.example.pieteditor;

import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

public class PietMachine {
	Stack<Integer> mStack;
	Integer mValue;

    DirectionPointer mDirectionPointer;
    CodelChoser mCodelChoser;

	abstract class Command{
		public boolean exec(Stack<Integer> _stack, Integer _input
                , DirectionPointer _directionPointer, CodelChoser _codelChoser) throws PietMachineExecutionError {
			try{
				this.onExec(_stack, _input, _directionPointer,_codelChoser);
                //System.out.println(_stack);
				return true;
            }
			catch(NullPointerException e){
                System.out.println(e.toString());
				return false;
			}
		}
		
		abstract void onExec(Stack<Integer> _stack, Integer _input,
                             DirectionPointer _directionPointer, CodelChoser _codelChoser) throws PietMachineExecutionError;
	}

	Command[][] mCommands;
	private final int DARK_COUNT = 6;
	private final int HUE_COUNT = 3;
	public PietMachine() {
		mStack = new Stack<Integer>();
		mValue = 0;
		mCommands = new Command[DARK_COUNT][HUE_COUNT];

        mDirectionPointer = new DirectionPointer();
        mCodelChoser = new CodelChoser();

		setupCommands();
	}

    public final DirectionPointer getDirectionPointer(){
        return mDirectionPointer;
    }

    public final CodelChoser getCodelChoser(){
        return mCodelChoser;
    }

	private void setupCommands(){
		//idle
		mCommands[0][0] = new Command(){
			void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				
			}
		};
		//push
		mCommands[0][1] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				_stack.push(_input);
			}
		};
		//pop
		mCommands[0][2] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				_stack.pop();
			}
		};
		//add
		mCommands[1][0] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				Integer top = _stack.pop();
				Integer next = _stack.pop();
				_stack.push(top + next);
			}
		};
		//sub
		mCommands[1][1] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				Integer top = _stack.pop();
				Integer next = _stack.pop();
				_stack.push(next - top);
			}
		};
		//mul
		mCommands[1][2] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				Integer top = _stack.pop();
				Integer next = _stack.pop();
				_stack.push(next * top);
			}
		};
		//DIV
		mCommands[2][0] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				Integer top = _stack.pop();
				Integer next = _stack.pop();
				_stack.push(next / top);
			}
		};
	
		//MOD
		mCommands[2][1] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				Integer top = _stack.pop();
				Integer next = _stack.pop();
				_stack.push(next % top);
			}
		};
		//NOT
		mCommands[2][2] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				Integer top = _stack.pop();
				Integer result = 0;
				
				if(top == 0){
					result = 1;
				}
				
				_stack.push(result);
			}
		};
		//GREATER
		mCommands[3][0] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				Integer top = _stack.pop();
				Integer next = _stack.pop();
				if (next > top){
					_stack.push(1);
				}
				else{
					_stack.push(0);
				}
			}
		};
		//POINTER
		mCommands[3][1] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
                Integer top = _stack.pop();

                _direction.roll(top);
			}
		};
		//SWITCH
		mCommands[3][2] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
                Integer top = _stack.pop();

                _codelChoser.roll(top);
			}
		};
		//DUPLICATE
		mCommands[4][0] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
                Integer top = _stack.pop();

                _stack.push(top);
                _stack.push(top);
			}
		};
		//ROLL
		mCommands[4][1] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
                Integer num = _stack.pop();
                Integer depth = _stack.pop();
                num %= depth;

                if (depth <= 0 || num == 0){
                    return;
                }

                int firstPosition = depth < 0 ? Math.abs(depth) : _stack.size() - depth;

                int numWeight = (num < 0) ? 1 : 0;
                int x = (-1 * Math.abs(num)) + (depth * numWeight);
                int secondPosition = x > 0 ? x : _stack.size() + x;

                int aboveRange = _stack.size() - secondPosition;
                ArrayList<Integer> aboveElements = new ArrayList<Integer>();
                for(int i = 0; i < aboveRange; ++i){
                    Integer value = _stack.pop();
                    aboveElements.add(value);
                }
                Collections.reverse(aboveElements);

                int belowRange = secondPosition - firstPosition;
                ArrayList<Integer> belowElements = new ArrayList<Integer>();
                for(int i = 0; i < belowRange; ++i){
                    Integer value = _stack.pop();
                    belowElements.add(value);
                }
                Collections.reverse(belowElements);

                for (Integer value : aboveElements){
                    _stack.push(value);
                }

                for (Integer value : belowElements){
                    _stack.push(value);
                }
			}
		};
		//IN_NUMBER
		mCommands[4][2] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser)
                    throws PietMachineExecutionError{
                try{
                    Integer value = System.in.read();
                    _stack.push(value);
                }
                catch (IOException exception){
                    String msg = exception.getMessage();
                    throw new PietMachineExecutionError(msg);
                }
			}
		};
		//IN_CHAR
		mCommands[5][0] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser)
                    throws PietMachineExecutionError{
                try{
                    Integer value = System.in.read();
                    _stack.push(value);
                }
                catch (IOException exception){
                    String msg = exception.getMessage();
                    throw new PietMachineExecutionError(msg);
                }
			}
		};
		//OUT_NUMBER
		mCommands[5][1] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				Integer top = _stack.pop();
				System.out.print(top);
			}
		};
		//OUT_CHAR
		mCommands[5][2] = new Command(){
            void onExec(Stack<Integer> _stack, Integer _input, DirectionPointer _direction, CodelChoser _codelChoser){
				Integer top = _stack.pop();
				
				char [] unicode = Character.toChars(top);
				System.out.print(unicode);
			}
		};
	}

	public void runCommand(int _dark, int _hue, Integer _input) throws PietMachineExecutionError {
        int y = _hue;
        int x = _dark;
        if(_hue < 0){
            y = DARK_COUNT - (-1 * _hue);
        }
        if(_dark < 0){
            x = HUE_COUNT - (-1 * _dark);
        }

        //String format = String.format("%d,%d (%d)", x,y,_input);
        //System.out.println(format);
		mCommands[y][x].exec(mStack, _input, mDirectionPointer, mCodelChoser);
	}
	
	/*
	    def NOT(self):
	        if len(self.stack) < 1:
	            return
	        top = self.stack.pop()
	        self.stack.append(int(not top))

	    def GTR(self):
	        if len(self.stack) < 2:
	            return
	        top = self.stack.pop()
	        next = self.stack.pop()
	        self.stack.append(int(next > top))

	    def PNTR(self):
	        if len(self.stack) < 1:
	            return
	        top = self.stack.pop()
	        self.dp = (self.dp + top) % 4

	    def SWCH(self):
	        if len(self.stack) < 1:
	            return
	        top = self.stack.pop()
	        self.cc *= (-1) ** (top % 2)

	    def DUP(self):
	        if len(self.stack) < 1:
	            return
	        self.stack.append(self.stack[-1])

	    def ROLL(self):
	        if len(self.stack) < 2:
	            return
	        num = self.stack.pop()
	        depth = self.stack.pop()
	        num %= depth
	        if depth <= 0 or num == 0:
	            return
	        x = -abs(num) + depth * (num < 0)
	        self.stack[-depth:] = self.stack[x:] + self.stack[-depth:x]

	    def N_IN(self):

	        n = int(raw_input("Enter an integer: "))
	        self.stack.append(n)

	    def C_IN(self):
	        c = ord(raw_input("Enter a character: "))
	        self.stack.append(c)

	    def NOUT(self):
	        if len(self.stack) < 1:
	            return
	        top = self.stack.pop()
	        sys.stdout.write(str(top))

	    def COUT(self):
	        if len(self.stack) < 1:
	            return
	        top = self.stack.pop()
	        sys.stdout.write(chr(top))*/
}
