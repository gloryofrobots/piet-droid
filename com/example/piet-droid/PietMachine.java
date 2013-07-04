package com.example.pieteditor;

public class PietMachine {
	Stack<Integer> mStack;
	Integer mValue;
	
	public PietMachine() {
		mStack = new Stack<Integer>();
		mValue = 0;
	}
	
	public boolean idle(){
		return true;
	}
	
	public boolean push(){
		mStack.push( mValue );
		return true;
	}
	
	public boolean pop(){
		try{
			mStack.pop();
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	
	public boolean add(){
		try{
			Integer top = mStack.pop();
			Integer next = mStack.pop();
			mStack.push( top + next );
			
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	
	public boolean sub(){
		try{
			Integer top = mStack.pop();
			Integer next = mStack.pop();
			mStack.push( next - top );
			
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	
	public boolean mul(){
		try{
			Integer top = mStack.pop();
			Integer next = mStack.pop();
			mStack.push( next * top );
			
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	
	public boolean div(){
		try{
			Integer top = mStack.pop();
			Integer next = mStack.pop();
			mStack.push( next / top );
			
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	
	public boolean mod(){
		try{
			Integer top = mStack.pop();
			Integer next = mStack.pop();
			mStack.push( next % top );
			
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	
	public boolean not(){
		try{
			Integer top = mStack.pop();
			Integer result = 0;
			
			if( top == 0 ){
				result = 1;
			}
			
			mStack.push( result );
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
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
