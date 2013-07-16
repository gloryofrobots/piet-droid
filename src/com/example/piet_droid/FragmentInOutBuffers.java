package com.example.piet_droid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class FragmentInOutBuffers extends Fragment {
    
    InOutSystemEditText mInOutSystem;
    
    public FragmentInOutBuffers() {
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_in_out_buffers, container,
                false);
        
        final EditText inText = (EditText) view.findViewById(R.id.text_view_in);
        final TextView outText = (TextView) view.findViewById(R.id.text_view_out);

        mInOutSystem = new InOutSystemEditText(inText, outText);
        
        return view;
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

   

