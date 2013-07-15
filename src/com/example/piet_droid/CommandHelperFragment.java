package com.example.piet_droid;

import com.example.jpiet.Piet;
import com.example.jpiet.PietMachine;
import com.example.piet_droid.PaletteFragment.OnChooseColorListener;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommandHelperFragment extends Fragment {
    public interface  OnChooseCommandListener{
        public void onChooseCommandColor(int color);
    }
    
    private OnChooseCommandListener mChooseCommandListener;
    
    LinearLayout mContainer;
    
    public CommandHelperFragment() {
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.command_helper_fragment, container,
                false);
        
        View.OnClickListener listener = new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                
            }
        };
        
        mContainer = (LinearLayout) view.findViewById(R.id.command_helper_container);
        
        Resources resources = getResources();
        
        String[] command_tags = resources.getStringArray(R.array.command_tags);
        String[] command_titles = resources.getStringArray(R.array.command_titles);
        
        if( command_tags.length != command_titles.length){
            throw new IllegalArgumentException("tags and titles for commans has different length");
        }
        Activity activity = getActivity();
        
        int size = command_tags.length;
        for(int i = 0; i < size; i++){
            TextView text = new TextView(activity);
            text.setText(command_titles[i]);
            text.setTag(command_tags[i]);
            mContainer.addView(text);
        }
        
        return view;
    }
    
    public void setColor(int color, Piet piet){
        if (color == Color.WHITE || color == Color.BLACK){
            return;
        }
        
        piet.calculateCommandOpportunity(color, new PietMachine.CommandOpportunityVisitor() {
            
            @Override
            public void acceptCommandOpportunity(String tag, int color) {
                TextView view = (TextView) mContainer.findViewWithTag(tag);
                if (view == null){
                    return;
                }
                
                view.setTextColor(color);
            }
        });
    }
    
    /*public void chooseColor(int color) {
        GradientDrawable gd = (GradientDrawable) mDrawable.mutate();
        gd.setColor(color);
        gd.invalidateSelf();
        mOnChooseColorListener.onChooseColor(color);
    }*/
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            OnChooseCommandListener = (OnChooseCommandListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChooseColorListener");
        }*/
    }
}
