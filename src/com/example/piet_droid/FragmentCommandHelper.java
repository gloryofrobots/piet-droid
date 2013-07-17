package com.example.piet_droid;

import com.example.jpiet.Piet;
import com.example.jpiet.PietMachine;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentCommandHelper extends Fragment {
    public interface OnChooseCommandListener {
        public void onChooseCommandColor(int color);
    }

    private OnChooseCommandListener mChooseCommandListener;

    private LinearLayout mContainer;
    private Piet mPiet;
    private int mColor;
    
    public FragmentCommandHelper() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_command_helper,
                container, false);

        mContainer = (LinearLayout) view
                .findViewById(R.id.command_helper_container);

        Resources resources = getResources();

        String[] command_tags = resources.getStringArray(R.array.command_tags);
        String[] command_titles = resources
                .getStringArray(R.array.command_titles);

        if (command_tags.length != command_titles.length) {
            throw new IllegalArgumentException(
                    "tags and titles for commans has different length");
        }
        Activity activity = getActivity();

        int size = command_tags.length;
        for (int i = 0; i < size; i++) {
            TextView text = new TextView(activity);
            //FIXME
            text.setText(command_titles[i]);
            text.setTag(command_tags[i]);
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            text.setPadding(2, 2, 2, 2);
            text.setTextSize(12.0f);
            mContainer.addView(text);
        }

        return view;
    }

    public void setColor(int color) {
        if(mColor == color) {
            return;
        }
        
        mColor = color;
        
        if(mPiet == null) {
            return;
        }
        
        invalidate();
    }
    
    public void invalidate() {
        if (mColor == Color.WHITE || mColor == Color.BLACK) {
            return;
        }

        mPiet.calculateCommandOpportunity(mColor,
                new PietMachine.CommandOpportunityVisitor() {

                    @Override
                    public void acceptCommandOpportunity(String tag, int color) {
                        TextView view = (TextView) mContainer
                                .findViewWithTag(tag);
                        if (view == null) {
                            return;
                        }

                        view.setTextColor(color);
                    }
                });
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Activity activity = getActivity();
        try {
            PietProvider provider = (PietProvider) activity;
            mPiet = provider.getPiet();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PietProvider");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
