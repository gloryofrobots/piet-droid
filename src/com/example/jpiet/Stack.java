package com.example.jpiet;

public class Stack<T> {
	
	private class Link {
		public T item;
		public Link link;
		
 		public Link(T _item, Link _link){
			item = _item;
			link = _link;
		}

        public String toString()
        {
            String str = "";
            if(item != null){
                str = item.toString();
            }

            if(link != null){
                return str += "," + link.toString();
            }

            return  str;
        }
	}

    private int mSize;
	private Link mHead;
	
	public Stack() {
		clear();
	}
	
	public T pop() {
		if (mHead.link == null){
			throw new NullPointerException();
		}
		
		Link previous = mHead.link;
		T removed = mHead.item;
		
		mHead = previous;
        --mSize;
		return removed;
	}
	
	public void push (T _item){
        ++mSize;
		mHead = new Link(_item, mHead);
	}
	
	public void clear() {
	    mHead = new Link(null, null);
        mSize = 0;
	}
	
    public int size(){
        return mSize;
    }

    public String toString(){
        String str = "Stack:[";
        if (mHead!= null){
            str += mHead.toString();
        }

        str += "]";

        return str;
    }
}
