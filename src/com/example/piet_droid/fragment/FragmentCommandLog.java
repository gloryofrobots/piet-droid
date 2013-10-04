package com.example.piet_droid.fragment;

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
import com.example.piet_droid.PietProvider;
import com.example.piet_droid.R;
import com.example.piet_droid.R.id;
import com.example.piet_droid.R.layout;


import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentCommandLog extends SherlockFragment {
    List<String> mQueue;
    ArrayList<String> mItems;
    ArrayAdapter<String> mAdapter;
    ListView mListView;
    private final String SAVE_KEY_ITEMS = "SAVE_KEY_ITEMS";
    
    public FragmentCommandLog() {
        mQueue = Collections.synchronizedList(new LinkedList<String>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_command_log,
                container, false);
        
        if(savedInstanceState != null) {
            mItems = savedInstanceState.getStringArrayList(SAVE_KEY_ITEMS);
        }
        else {
            mItems = new ArrayList<String>();
        }
        
        mListView = (ListView) view.findViewById(R.id.list_view_log);
        
        registerForContextMenu(mListView);
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList(SAVE_KEY_ITEMS, mItems);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_log, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_log:
                clear();
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Activity activity = getActivity();
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
       synchronized (mQueue) {
           for (String record : mQueue) {
               mItems.add(record);
           }
       }
       
       mAdapter.notifyDataSetChanged();
       mQueue.clear();
    }

    public void clear() {
        mItems.clear();
        mAdapter.notifyDataSetChanged();
        mQueue.clear();
    }
}
