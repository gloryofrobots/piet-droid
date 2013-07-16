package com.example.piet_droid;

import java.util.List;

import android.os.Bundle;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;


import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.example.jpiet.Codel;

import com.example.jpiet.InOutSystem;

import com.example.jpiet.Piet;
import com.example.piet_droid.AsyncTaskLoadBitmap.Pixel;

public class MainActivity extends FragmentActivity implements
        FragmentControlToolBox.InteractionListener, FragmentPaletteSimple.OnChooseColorListener
        , AsyncTaskRunPiet.ExecutionProcessListener, AsyncTaskLoadBitmap.LoadProcessListener
        , PietProvider{

    Piet mPiet;
    
    ColorFieldView mColorField;
    private int mActiveColor;
    private DrawableFilledCircle mDebugDrawable;
    
    long mSleepBetweenStep;
    
    AsyncTaskRunPiet mCurrentRunTask;
    
    FragmentInOutBuffers mFragmentInOutBuffers;
    FragmentControlToolBox mToolBoxFragment;
    FragmentCommandHelper mCommandHelperFragment;
    FragmentStateInfo mFragmentStateInfo;
    FragmentCommandLog mFragmentCommandLog;
    FragmentPaletteSimple mFragmentPaletteSimple;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        mSleepBetweenStep = 2L;
        
        mDebugDrawable = new DrawableFilledCircle();
        int drawableColor = resources.getColor(R.color.debug_cell_highlight);
        mDebugDrawable.setColor(drawableColor);
        
        mActiveColor = 0;
        
        mActiveColor = resources.getColor(R.color.default_draw_color);
                
        mToolBoxFragment = (FragmentControlToolBox) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_control_toolbox);
        
        
        mCommandHelperFragment = (FragmentCommandHelper) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_command_helper);
        
        
        mFragmentStateInfo = (FragmentStateInfo) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_state_info);
        
        mFragmentCommandLog = (FragmentCommandLog) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_command_log);
        
        mFragmentInOutBuffers = (FragmentInOutBuffers) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_in_out_buffers);
        
        
        initPiet(resources);
        
        initColorField();
        
        final Button buttonLoad = (Button) findViewById(R.id.button_load);
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                load();
            }
        });
        
        mFragmentPaletteSimple =  (FragmentPaletteSimple) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_palette_simple);
        
        mFragmentPaletteSimple.setPiet(mPiet);
        mFragmentPaletteSimple.chooseColor(mActiveColor);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mCommandHelperFragment.invalidate();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setCell(int x, int y, int color) {
        mColorField.setCellColor(x, y, color);
        mPiet.setColor(x, y, color);
    }
     
    private void updateViewAfterStep(){
        Codel currentCodel = mPiet.getCurrentCodel();
        //mColorField.setCellDrawable(currentCodel.x, currentCodel.y, mDebugDrawable);
        mFragmentStateInfo.update();
        mFragmentInOutBuffers.update();
        mFragmentCommandLog.update();
    }
    
    private void initPiet(Resources resources) {
        InOutSystem inOutSystem = mFragmentInOutBuffers.getInOutSystem();
        LoggerDroid logger = new LoggerDroid();

        mPiet = new Piet(logger, inOutSystem);

        int countX = resources.getInteger(R.integer.field_count_codels_x);
        int countY = resources.getInteger(R.integer.field_count_codels_y);

        mPiet.createModel(countX, countY);
    }

    private void initColorField() {
        mColorField = (ColorFieldView) findViewById(R.id.codelField);

        mColorField
                .setOnCellClickListener(new ColorFieldView.CellClickListener() {
                    @Override
                    public void onCellClick(int x, int y) {
                        setCell(x, y, mActiveColor);
                        mColorField.setCellToRedraw(x, y);
                    }
                });
    }

    @Override
    public void onChooseColor(int color) {
        mActiveColor = color;
        mCommandHelperFragment.setColor(color);
    }
    
    @Override
    public void onInteractionRun() {
        if(mCurrentRunTask != null) {
            if (mCurrentRunTask.isOnPause()) {
                mCurrentRunTask.resume();
                return;
            }
                onRunCancel();
           
        }
        
        
        mCurrentRunTask = new AsyncTaskRunPiet(this, mSleepBetweenStep);
        mCurrentRunTask.execute(mPiet);
    }

    @Override
    public void onInteractionStep() {
        mPiet.step();
        updateViewAfterStep();
    }

    @Override
    public void onInteractionStop() {
        if( mCurrentRunTask == null ){
            return;
        }
        mCurrentRunTask.pause();
    }
    
    @Override
    public void onInteractionReset() {
        if( mCurrentRunTask == null ){
            return;
        }
        mCurrentRunTask.cancel(true);
    }
    
    @Override
    public void onRunStart() {
        mFragmentStateInfo.init();
        mColorField.clearDrawables();
        mFragmentInOutBuffers.prepare();
        mPiet.init();
    }

    @Override
    public void onRunCancel() {
        mColorField.clearDrawables();
        mFragmentStateInfo.init();
        mFragmentInOutBuffers.prepare();
        mCurrentRunTask = null;
    }
    
    //codelsToUpdate must be synchronized
    @Override
    public void onRunUpdate(List<Codel> codelsToUpdate) {
        updateViewAfterStep();
        /*
        synchronized (codelsToUpdate) {
            for (Codel codel : codelsToUpdate) {
                mColorField.setCellDrawable(codel.x, codel.y, mDebugDrawable);
            }
        }*/
    }

    @Override
    public void onRunComplete() {
        mToolBoxFragment.setControlsToDefaultState();
        mCurrentRunTask = null;
    }
    
    public void load() {
        //TODO FADE OUT FADE IN
        Bitmap bitmap = BitmapFactory.decodeFile("/data/helloWorld_small.png");
        
        // TODO CODEL SIZE HERE
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        mPiet.createModel(width, height);
        mColorField.setVisibility(View.INVISIBLE);
        mColorField.resize(width, height);
        
        AsyncTaskLoadBitmap loadTask = new AsyncTaskLoadBitmap(this, this);
        loadTask.execute(bitmap);
    }
    
    @Override
    public void onLoadBitmapCancel() {
        // TODO Auto-generated method stub
        
    }

   /* @Override
    public void onLoadBitmapUpdate(List<Pixel> pixels) {
        
        synchronized (pixels) {
            for(Pixel pixel : pixels){
                setCell(pixel.x, pixel.y, pixel.color);
            }
        }
    }*/
    
    public void onLoadBitmapPixel(int x, int y, int color){
        setCell(x, y, color);
    }
    
    @Override
    public void onLoadBitmapComplete() {
        mColorField.setVisibility(View.VISIBLE);
        mColorField.invalidate();
    }

    @Override
    public Piet getPiet() {
        return mPiet;
    }
}
