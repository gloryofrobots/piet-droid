package com.example.piet_droid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.actionbarsherlock.app.SherlockFragment;

public class FragmentEditBoard extends SherlockFragment {

    public FragmentEditBoard() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_board, container,
                false);
        return view;

    }
}
