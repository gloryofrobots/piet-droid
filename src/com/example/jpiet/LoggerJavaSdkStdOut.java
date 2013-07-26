package com.example.jpiet;

public class LoggerJavaSdkStdOut extends Logger {

    @Override
    public void _onError(String msg) {
        System.out.println(msg);
        
    }

    @Override
    public void _onInfo(String msg) {
        System.out.println(msg);
    }

    @Override
    public void _onWarning(String msg) {
        System.out.println(msg);
    }
	
}
