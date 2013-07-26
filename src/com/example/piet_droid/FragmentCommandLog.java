package com.example.piet_droid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


import com.actionbarsherlock.app.SherlockFragment;
import com.example.jpiet.Command;
import com.example.jpiet.CommandRunListener;
import com.example.jpiet.Piet;
import com.example.jpiet.PietMachineStack;
import com.example.jpiet.PolicyStorage;
import com.example.jpiet.Logger;


import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentCommandLog extends SherlockFragment {
    List<String> mQueue;
    //TextView mLogText;
    ArrayList<String> mItems;
    ArrayAdapter<String> mAdapter;
    ListView mListView;
    //List<String> mErrors;
    public FragmentCommandLog() {
        mQueue = Collections.synchronizedList(new LinkedList<String>());
        mItems = new ArrayList<String>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_command_log,
                container, false);

        //mLogText = (TextView) view.findViewById(R.id.text_view_log);
        mListView = (ListView) view.findViewById(R.id.list_view_log);
        //mLogText.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        Activity activity = getActivity();
        super.onActivityCreated(savedInstanceState);
        
        mAdapter = new  ArrayAdapter<String>(activity, R.layout.log_list_item, mItems);
   
        mListView.setAdapter(mAdapter);
        try {
            PietProvider provider = (PietProvider) activity;
            final Piet piet = provider.getPiet();
            piet.addCommandRunListener(new CommandRunListener() {
                @Override
                public void onRunCommand(Command command, PietMachineStack stack) {
                    int number = piet.getStepNumber();
                    String text = String.format(Locale.ENGLISH,"Step %d %s : %s \n",
                            number, command.toString(), stack.toString());
                    mQueue.add(text);
                }
            });
            
            Logger logger = PolicyStorage.getInstance().getLogger();
            
            logger.addListener(new Logger.EventListener() {
                
                @Override
                public void onWarning(String error) {
                }
                
                @Override
                public void onInfo(String error) {
                }
                
                @Override
                public void onError(String error) {
                    mQueue.add(error);
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
               mItems.add(record);
               //temp += record;
           }
            
            //mLogText.setText(mLogText.getText() + temp);
        }
       
       mAdapter.notifyDataSetChanged();
       mQueue.clear();
    }
}
