package com.example.pieteditor;

public class Stack<T> {
	
	private class Link {
		public T item;
		public Link link;
		
 		public Link( T _item, Link _link ){
			item = _item;
			link = _link;
		}
	}
	
	private Link mHead;
	
	public Stack() {
		mHead = new Link(null, null);
	}
	
	public T pop() {
		if (mHead.link == null){
			throw new NullPointerException();
		}
		
		Link previous = mHead.link;
		T removed = mHead.item;
		
		mHead = previous;
		return removed;
	}
	
	public void push (T _item){
		mHead = new Link(_item, mHead);
	}
}
