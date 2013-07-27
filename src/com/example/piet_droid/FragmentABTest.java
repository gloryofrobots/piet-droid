package com.example.piet_droid;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class FragmentABTest extends SherlockFragment {
    ImageButton mButtonRun ;

    
    public FragmentABTest(){
    }
  


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragmet_ab_test, container,
                false);

        mButtonRun = (ImageButton) view.findViewById(R.id.button_run2);
      

        
        mButtonRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast toast = Toast.makeText(getActivity(), "HELLO!!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    
        return view;
    }

}
