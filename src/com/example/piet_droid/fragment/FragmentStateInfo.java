package com.example.piet_droid.fragment;

import java.util.Locale;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.jpiet.Codel;
import com.example.jpiet.CodelChoser;
import com.example.jpiet.Command;
import com.example.jpiet.CommandRunListener;
import com.example.jpiet.PietMachineStack;

import com.example.jpiet.DirectionPointer;
import com.example.jpiet.Piet;
import com.example.piet_droid.PietProvider;
import com.example.piet_droid.R;
import com.example.piet_droid.R.id;
import com.example.piet_droid.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentStateInfo extends SherlockFragment {
    
    private final String SAVE_KEY_TEXT_INFO = "SAVE_KEY_TEXT_INFO";
    private TextView mInfoText;
    Piet mPiet;
    private String mLastCommandState;
    
    public FragmentStateInfo() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_state_info_,
                container, false);

        mInfoText = (TextView) view.findViewById(R.id.text_view_info);
        if(savedInstanceState != null ){
            mInfoText.setText(savedInstanceState.getString(SAVE_KEY_TEXT_INFO));
        }
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(SAVE_KEY_TEXT_INFO, mInfoText.getText().toString());
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        
        try {
            PietProvider provider = (PietProvider) activity;
            mPiet = provider.getPiet();
            mPiet.addCommandRunListener(new CommandRunListener() {
                @Override
                public void onRunCommand(Command command, PietMachineStack stack) {
                    mLastCommandState = String.format("%s : %s \n",
                            command.toString(), stack.toString());
                }
            });
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PietProvider");
        }
    }

    public void update() {
        int stepCounter = mPiet.getStepNumber();
        DirectionPointer directionPointer = mPiet.getDirectionPointer();

        CodelChoser codelChoser = mPiet.getCodelChoser();
        Codel currentCodel = mPiet.getCurrentCodel();

        String info = String.format(Locale.US,
                "step : %d cur codel : (%d,%d) \n DP : %s, CC : %s \n %s",
                stepCounter, currentCodel.x, currentCodel.y,
                directionPointer.toString(), codelChoser.toString(), mLastCommandState);

        mInfoText.setText(info);
    }

    public void init() {
        mInfoText.setText("");
    }

}
