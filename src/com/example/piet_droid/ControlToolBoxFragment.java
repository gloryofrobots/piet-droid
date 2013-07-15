package com.example.piet_droid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ControlToolBoxFragment extends Fragment {
    Button mButtonRun ;
    Button mButtonStep;
    Button mButtonPause ;
    Button mButtonStop;
    
    public ControlToolBoxFragment(){
    }
    
    public interface InteractionListener {
        public void onInteractionRun();

        public void onInteractionStep();

        public void onInteractionStop();
        
        public void onInteractionReset();
        // /
    }

    private InteractionListener mInteractionListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.control_panel_toolbox, container,
                false);

        mButtonRun = (Button) view.findViewById(R.id.button_run);
        mButtonStep = (Button) view.findViewById(R.id.button_step);
        mButtonPause = (Button) view.findViewById(R.id.button_pause);
        mButtonStop = (Button) view.findViewById(R.id.button_stop);
        
        setControlsToDefaultState();
        
        mButtonRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonPause.setEnabled(true);
                
                mButtonStop.setEnabled(false);
                mButtonStep.setEnabled(false);
                mButtonRun.setEnabled(false);
                
                mInteractionListener.onInteractionRun();
            }
        });

        mButtonStep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mInteractionListener.onInteractionStep();
            }
        });
        
        mButtonPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonPause.setEnabled(false);
                mButtonStop.setEnabled(true);
                mButtonStep.setEnabled(true);
                mButtonRun.setEnabled(true);
                
                mInteractionListener.onInteractionStop();
                
            }
        });
        
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setControlsToDefaultState();
                mInteractionListener.onInteractionReset();
                
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mInteractionListener = (InteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement InteractionListener");
        }
    }
    
    public void setControlsToDefaultState() {
        mButtonRun.setEnabled(true);
        mButtonStep.setEnabled(true);
        mButtonPause.setEnabled(false);
        mButtonStop.setEnabled(false);
    }
    
}
