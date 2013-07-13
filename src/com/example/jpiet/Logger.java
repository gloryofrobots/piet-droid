package com.example.jpiet;

public interface Logger {
	public void error(String _msg, Object... args);
	public void info(String msg, Object... args);
	public void warning(String msg, Object... args);
}
