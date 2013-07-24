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

public class FragmentControlToolBox extends SherlockFragment {
    ImageButton mButtonRun ;
    ImageButton mButtonStep;
    ImageButton mButtonPause ;
    ImageButton mButtonStop;
    
    public FragmentControlToolBox(){
    }
    
    public interface InteractionListener {
        public void onInteractionRun();

        public void onInteractionStep();

        public void onInteractionPause();
        
        public void onInteractionStop();
        // /
    }

    private InteractionListener mInteractionListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control_toolbox, container,
                false);

        mButtonRun = (ImageButton) view.findViewById(R.id.button_run);
        mButtonStep = (ImageButton) view.findViewById(R.id.button_step);
        mButtonPause = (ImageButton) view.findViewById(R.id.button_pause);
        mButtonStop = (ImageButton) view.findViewById(R.id.button_stop);
        
        setControlsToDefaultState();
        
        mButtonRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonPause.setEnabled(true);
                mButtonStop.setEnabled(true);
                mButtonStep.setEnabled(false);
                mButtonRun.setEnabled(false);
                
                mInteractionListener.onInteractionRun();
            }
        });

        mButtonStep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonStop.setEnabled(true);
                mInteractionListener.onInteractionStep();
            }
        });
        
        mButtonPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonPause.setEnabled(false);
                mButtonStop.setEnabled(true);
                mButtonStep.setEnabled(true);
                mButtonRun.setEnabled(true);
                
                mInteractionListener.onInteractionPause();
                
            }
        });
        
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setControlsToDefaultState();
                mInteractionListener.onInteractionStop();
                
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
