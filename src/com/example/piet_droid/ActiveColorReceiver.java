package com.example.piet_droid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class ActiveColorReceiver extends BroadcastReceiver {
	ChangeActiveColorListener mListener;
	
	public static String ACTIVE_COLOR_ID = "ACTIVE_COLOR_ID";
	
	public ActiveColorReceiver( ChangeActiveColorListener listener) {
		mListener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		int color = intent.getIntExtra(ACTIVE_COLOR_ID, 0);
		mListener.onChangeActiveColor(color);
	}
}
