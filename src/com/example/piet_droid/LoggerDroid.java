package com.example.piet_droid;

import android.util.Log;

import com.example.jpiet.Logger;

public class LoggerDroid implements Logger {
	private final String LABEL = "PIET_DROID_LOGGER";
	
	public LoggerDroid() {
	}

	public String prepare(String _msg, Object... args) {
		String str = String.format(_msg, args);
		return str;
	}
	
	@Override
	public void error(String _msg, Object... args) {
		String data = prepare(_msg, args);
		Log.e(LABEL, data);
	}

	@Override
	public void info(String msg, Object... args) {
		String data = prepare(msg, args);
		Log.i(LABEL, data);
	}

	@Override
	public void warning(String msg, Object... args) {
		String data = prepare(msg, args);
		Log.w(LABEL, data);
	}

}
