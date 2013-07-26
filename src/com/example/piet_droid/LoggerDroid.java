package com.example.piet_droid;

import android.util.Log;

import com.example.jpiet.Logger;

public class LoggerDroid extends Logger {
	private final String LABEL = "PIET_DROID_LOGGER";

    @Override
    public void _onError(String msg) {
        // TODO Auto-generated method stub
        Log.e(LABEL, msg);
    }


    @Override
    public void _onInfo(String msg) {
        // TODO Auto-generated method stub
        Log.i(LABEL, msg);
    }

    @Override
    public void _onWarning(String msg) {
        // TODO Auto-generated method stub
        Log.w(LABEL, msg);
    }

}
