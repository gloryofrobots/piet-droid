package com.example.piet_droid;

import android.graphics.Paint;

public interface PaletteProvider {

	public class UnknownColorIdException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3660887208939019845L;
	}

	public Paint getPaint(int id) throws UnknownColorIdException;
}
