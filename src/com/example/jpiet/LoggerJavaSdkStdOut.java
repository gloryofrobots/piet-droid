package com.example.jpiet;

public class LoggerJavaSdkStdOut implements Logger {
	@Override
	public void error(String _msg, Object... args) {
		// TODO Auto-generated method stub
		System.out.printf(_msg + "\n", args);
	}

	@Override
	public void info(String _msg, Object... args) {
		// TODO Auto-generated method stub
		//System.out.printf(_msg + "\n", args);
	}

	@Override
	public void warning(String _msg, Object... args) {
		// TODO Auto-generated method stub
		System.out.printf(_msg + "\n", args);
	}
}
