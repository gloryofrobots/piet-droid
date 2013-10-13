package com.example.piet_droid.fragment;

import java.util.HashMap;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.jpiet.Piet;
import com.example.jpiet.PietMachine;
import com.example.piet_droid.PietProvider;
import com.example.piet_droid.R;
import com.example.piet_droid.R.array;
import com.example.piet_droid.R.color;
import com.example.piet_droid.R.id;
import com.example.piet_droid.R.layout;
import com.example.piet_droid.widget.ColorFieldView;
import com.example.piet_droid.widget.ColorFieldView.CellClickListener;

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

        public TextDrawable(String text, int textColor, int textSize) {

            mText = text;
            mTextBounds = new Rect();
            mPaint = new Paint();
            mPaint.getTextBounds(text, 0, mText.length(), mTextBounds);
            mPaint.setColor(textColor);
            mPaint.setTextSize(textSize);
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
            for (float w : widths) {
                totalW += w;
            }

            int textSize = (int) mPaint.getTextSize();

            int px = bounds.left + ((bounds.width() - totalW) / 2);
            int py = bounds.top + ((bounds.height() - textSize) / 2)
                    + mTextBounds.height();

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

        public CurrentColorHighlightDrawable(int color) {
            this.paint = new Paint();
            paint.setColor(color);
            paint.setStrokeWidth(4);
            paint.setStyle(Style.STROKE);
        }

        @Override
        public void draw(Canvas canvas) {

            /*Rect bounds = getBounds();
            int strokeWidth = (int) paint.getStrokeWidth() / 2;

            canvas.drawRect(bounds.left + strokeWidth,
                    bounds.top + strokeWidth, bounds.right - strokeWidth,
                    bounds.bottom - strokeWidth, paint);*/
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
        View view = inflater.inflate(R.layout.fragment_palette_simple,
                container, false);

        Resources resources = getResources();

        if (savedInstanceState == null) {
            mActiveColor = resources.getColor(R.color.default_draw_color);
        } else {
            mActiveColor = savedInstanceState.getInt(SAVE_KEY_ACTIVE_COLOR);
        }

        int highlightColor = resources
                .getColor(R.color.palette_highlight_color);
        mHighlightDrawable = new CurrentColorHighlightDrawable(highlightColor);

        mDrawable = new GradientDrawable();

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

        String[] command_tags = resources.getStringArray(R.array.command_tags);
        String[] command_alias = resources
                .getStringArray(R.array.command_alias);

        if (command_tags.length != command_alias.length) {
            throw new IllegalArgumentException(
                    "tags and titles for commans has different length");
        }

        int textSize = resources
                .getDimensionPixelSize(R.dimen.palette_text_size);
        int textColor = resources.getColor(R.color.palette_text_color);

        mTagsAliasLink = new HashMap<String, FragmentPaletteSimple.TextDrawable>();
        int size = command_tags.length;
        for (int i = 0; i < size; i++) {
            String alias = command_alias[i];
            String tag = command_tags[i];
            TextDrawable drawable = new TextDrawable(alias, textColor, textSize);

            mTagsAliasLink.put(tag, drawable);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPalette.setDrawableForColor(mActiveColor, mHighlightDrawable);
        chooseColor(mActiveColor);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(SAVE_KEY_ACTIVE_COLOR, mActiveColor);
    }

    public void chooseColor(int color) {
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
}
