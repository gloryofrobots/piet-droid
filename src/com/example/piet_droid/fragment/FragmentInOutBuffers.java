package com.example.piet_droid.fragment;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.piet_droid.InOutSystemEditText;
import com.example.piet_droid.R;
import com.example.piet_droid.R.id;
import com.example.piet_droid.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class FragmentInOutBuffers extends SherlockFragment {
    
    InOutSystemEditText mInOutSystem;
    private final String SAVE_KEY_IN_BUFFER_TEXT = "SAVE_KEY_IN_BUFFER_TEXT";
    private final String SAVE_KEY_OUT_BUFFER_TEXT = "SAVE_KEY_OUT_BUFFER_TEXT";
    
    public FragmentInOutBuffers() {
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_in_out_buffers, container,
                false);
        
        final EditText inText = (EditText) view.findViewById(R.id.text_view_in);
        final TextView outText = (TextView) view.findViewById(R.id.text_view_out);
        
        if(savedInstanceState != null) {
            final String input = savedInstanceState.getString(SAVE_KEY_IN_BUFFER_TEXT);
            inText.setText(input);
            final String output = savedInstanceState.getString(SAVE_KEY_OUT_BUFFER_TEXT);
            outText.setText(output);
        }
        
        mInOutSystem = new InOutSystemEditText(inText, outText);
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(SAVE_KEY_IN_BUFFER_TEXT, mInOutSystem.getInputString());
        savedInstanceState.putString(SAVE_KEY_OUT_BUFFER_TEXT, mInOutSystem.getOutputString());
    }
    
    public com.example.jpiet.InOutSystem getInOutSystem() {
        return mInOutSystem;
    }
    
    public void appendLine(String text) {
        mInOutSystem.write(text + "\n");
        update();
    }
    
    public void prepare() {
        mInOutSystem.prepare();
    }
    
    public void update() {
        mInOutSystem.flush();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       
    }
}

   

