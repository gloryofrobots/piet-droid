package com.example.piet_droid;

import java.util.HashMap;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.jpiet.Piet;
import com.example.jpiet.PietMachine;

import android.app.Activity;
import android.content.res.Resources;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentPaletteSimple extends SherlockFragment {
    public class TextDrawable extends Drawable {

        private final String mText;
        private final Paint mPaint;
        private final Rect mTextBounds;
        public TextDrawable(String text) {

            mText = text;
            mTextBounds = new Rect();
            mPaint = new Paint();
            mPaint.getTextBounds(text, 0, mText.length(), mTextBounds);
            //FIXME
            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(10f);
            mPaint.setAntiAlias(true);
            mPaint.setFakeBoldText(true);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextAlign(Paint.Align.LEFT);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            
            int length = mText.length();
            float[] widths = new float[length];
            
            mPaint.getTextWidths(mText, widths);
            int totalW = 0;
            for(float w : widths){
                totalW += w;
            }

            int textSize = (int) mPaint.getTextSize();

            int px = bounds.left + ((bounds.width() - totalW) / 2);
            int py = bounds.top + ((bounds.height() - textSize) / 2) + mTextBounds.height();

            canvas.drawText(mText, px, py, mPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mPaint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    public class CurrentColorHighlightDrawable extends Drawable {
        private final Paint paint;

        public CurrentColorHighlightDrawable() {
            this.paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(4);
            paint.setStyle(Style.STROKE);
        }

        @Override
        public void draw(Canvas canvas) {
            
            Rect bounds = getBounds();
            int strokeWidth = (int) paint.getStrokeWidth() / 2;

            canvas.drawRect(bounds.left + strokeWidth,
                    bounds.top + strokeWidth,
                    bounds.right - strokeWidth,
                    bounds.bottom - strokeWidth
                    , paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            paint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    ColorFieldView mPalette;
    Piet mPiet;

    GradientDrawable mDrawable;
    CurrentColorHighlightDrawable mHighlightDrawable;
    
    private int mActiveColor;
    private final String SAVE_KEY_ACTIVE_COLOR = "PietPaletteActiveColor"; 
    
    private HashMap<String, TextDrawable> mTagsAliasLink;

    public interface OnChooseColorListener {
        public void onChooseColor(int color);
    }

    public FragmentPaletteSimple() {
    }

    private OnChooseColorListener mOnChooseColorListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_palette_simple, container,
                false);

        Resources resources = getResources();
        
        if(savedInstanceState == null) {
            mActiveColor = resources.getColor(R.color.default_draw_color);
        }
        else {
            mActiveColor = savedInstanceState.getInt(SAVE_KEY_ACTIVE_COLOR);
        }
        
        
        mHighlightDrawable = new CurrentColorHighlightDrawable();
        
        mDrawable = new GradientDrawable();
        
        //View currentColor = view.findViewById(R.id.current_color_view);
        //currentColor.setBackgroundDrawable(mDrawable);

        // TODO CHECK CACHED INSTANCE(SEE DEVELOPER TUTORIAL ON ANDROID DOCS) OR
        // IT IN ACTIVITY PERHAPS

        mPalette = (ColorFieldView) view.findViewById(R.id.colorPalette);

        mPalette.setOnCellClickListener(new ColorFieldView.CellClickListener() {
            @Override
            public void onCellClick(int x, int y) {
                int color = mPalette.getCellColor(x, y);
                mPalette.clearDrawables();
                mPalette.setCellDrawable(x, y, mHighlightDrawable);
                chooseColor(color);
            }

            @Override
            public boolean isProcessClickWanted() {
                return true;
            }
        });
        
        //TODO black white cell highlight
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

        String[] command_tags = resources.getStringArray(R.array.command_tags);
        String[] command_alias = resources
                .getStringArray(R.array.command_alias);

        if (command_tags.length != command_alias.length) {
            throw new IllegalArgumentException(
                    "tags and titles for commans has different length");
        }

        mTagsAliasLink = new HashMap<String, FragmentPaletteSimple.TextDrawable>();
        int size = command_tags.length;
        for (int i = 0; i < size; i++) {
            String alias = command_alias[i];
            String tag = command_tags[i];
            TextDrawable drawable = new TextDrawable(alias);

            mTagsAliasLink.put(tag, drawable);
        }
        
        return view;
    }
    
    @Override
    public void onStart(){
        super.onStart();
        mPalette.setDrawableForColor(mActiveColor, mHighlightDrawable);
        chooseColor(mActiveColor);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate and
        // onRestoreInstanceState if the process is
        // killed and restarted by the run time.
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(SAVE_KEY_ACTIVE_COLOR, mActiveColor);
    }
    
    
    public void chooseColor(int color) {
        //GradientDrawable gd = (GradientDrawable) mDrawable.mutate();
        //gd.setColor(color);
        //gd.invalidateSelf();
        mActiveColor = color;
        mOnChooseColorListener.onChooseColor(color);

        if (color == Color.WHITE || color == Color.BLACK) {
            return;
        }

        mPiet.calculateCommandOpportunity(color,
                new PietMachine.CommandOpportunityVisitor() {
                    @Override
                    public void acceptCommandOpportunity(String tag, int color) {
                        TextDrawable drawable = mTagsAliasLink.get(tag);

                        if (drawable == null) {
                            return;
                        }

                        mPalette.setDrawableForColor(color, drawable);
                    }
                });
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Activity activity = getActivity();
        try {
            mOnChooseColorListener = (OnChooseColorListener) activity;
            PietProvider provider = (PietProvider) activity;
            mPiet = provider.getPiet();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChooseColorListener and PietProvider");
        }
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
