package com.example.piet_droid;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class PaletteFragment extends Fragment {
    
    GradientDrawable mDrawable;
    
    public interface  OnChooseColorListener{
        public void onChooseColor(int color);
    }
    
    public PaletteFragment() {
    }
    
    private OnChooseColorListener mOnChooseColorListener;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.palette_fragment, container,
                false);
        
        Resources resources = getResources();
        
        mDrawable = new GradientDrawable();
        int defaultColor = resources.getColor(R.color.default_draw_color);
        mDrawable.setColor(defaultColor);
        
        View currentColor = view.findViewById(R.id.current_color_view);
        currentColor.setBackgroundDrawable(mDrawable);
        
        
        //TODO CHECK CACHED INSTANCE(SEE DEVELOPER TUTORIAL ON ANDROID DOCS) OR IT IN ACTIVITY PERHAPS
        
        final ColorFieldView palette = (ColorFieldView) view.findViewById(R.id.colorPalette);

        palette.setOnCellClickListener(new ColorFieldView.CellClickListener() {
            @Override
            public void onCellClick(int x, int y) {
                int color = palette.getCellColor(x, y);
                chooseColor(color);
            }
        });
        
        
        View blackCell = view.findViewById(R.id.black_palette);
        blackCell.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                chooseColor(Color.BLACK);
                
            }
        });
        
        View whiteCell = view.findViewById(R.id.white_palette);
        whiteCell.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                chooseColor(Color.WHITE);
            }
        });
        
        return view;
    }
    
    public void chooseColor(int color) {
        GradientDrawable gd = (GradientDrawable) mDrawable.mutate();
        gd.setColor(color);
        gd.invalidateSelf();
        mOnChooseColorListener.onChooseColor(color);
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
