package com.example.piet_droid;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class PaletteFragment extends Fragment {
    
    public interface  OnChooseColorListener{
        public void onChooseColor(int color);
    }
    
    public PaletteFragment() {
    }
    
    private OnChooseColorListener mOnChooseColorListener;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.control_panel_toolbox, container,
                false);
        
        Resources resources = getResources();
        //TODO CHECK CACHED INSTANCE(SEE DEVELOPER TUTORIAL ON ANDROID DOCS)
        
        final ColorFieldView palette = (ColorFieldView) view.findViewById(R.id.colorPalette);

        TypedArray colors = resources.obtainTypedArray(R.array.colors);
        int size = colors.length();
        int y = 0;
        int x = 0;
        for (int i = 0; i < size; i++) {
            int color = colors.getColor(i, 0);
            palette.setCellColor(x, y, color);
            x++;

            if (((i + 1) % 10) == 0) {
                y++;
                x = 0;
            }

        }

        colors.recycle();

        palette.setOnCellClickListener(new ColorFieldView.CellClickListener() {
            @Override
            public void onCellClick(int x, int y) {
                // Log.e("TEST", String.format("%d-%d", x, y));
                int color = palette.getCellColor(x, y);
                
                mOnChooseColorListener.onChooseColor(color);
            }
        });
        
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnChooseColorListener = (OnChooseColorListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChooseColorListener");
        }
    }
}
