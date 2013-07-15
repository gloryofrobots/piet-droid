package com.example.piet_droid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InOutBuffersFragment extends Fragment {

    public InOutBuffersFragment() {
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.command_helper_fragment, container,
                false);
        
      
        return view;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       
    }
}

   

