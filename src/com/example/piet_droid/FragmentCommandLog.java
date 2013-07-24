package com.example.piet_droid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.jpiet.Command;
import com.example.jpiet.CommandRunListener;
import com.example.jpiet.Piet;
import com.example.jpiet.PietMachineStack;

import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentCommandLog extends SherlockFragment {
    List<String> mQueue;
    TextView mLogText;

    public FragmentCommandLog() {
        mQueue = Collections.synchronizedList(new ArrayList<String>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_command_log,
                container, false);

        mLogText = (TextView) view.findViewById(R.id.text_view_log);
        mLogText.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        Activity activity = getActivity();
        super.onActivityCreated(savedInstanceState);
        
        try {
            PietProvider provider = (PietProvider) activity;
            Piet piet = provider.getPiet();
            piet.addCommandRunListener(new CommandRunListener() {
                @Override
                public void onRunCommand(Command command, PietMachineStack stack) {
                    String text = String.format("%s : %s \n",
                            command.toString(), stack.toString());
                    mQueue.add(text);
                }
            });
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PietProvider");
        }
    }

    public void update() {       
       String temp = "";
       synchronized (mQueue) {
           for (String record : mQueue) {
               temp = record;
           }
            
            mLogText.setText(temp);
        }
        mQueue.clear();
    }
}
