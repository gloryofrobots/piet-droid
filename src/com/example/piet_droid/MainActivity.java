package com.example.piet_droid;

import java.util.List;
import java.util.Locale;

import android.os.Bundle;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;


import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jpiet.Codel;
import com.example.jpiet.CodelChoser;
import com.example.jpiet.Command;
import com.example.jpiet.CommandRunListener;
import com.example.jpiet.DirectionPointer;

import com.example.jpiet.Piet;
import com.example.jpiet.PietMachineStack;
import com.example.piet_droid.AsyncTaskLoadBitmap.Pixel;

public class MainActivity extends FragmentActivity implements
        ControlToolBoxFragment.InteractionListener, PaletteFragmentSimple.OnChooseColorListener
        , AsyncTaskRunPiet.ExecutionProcessListener, AsyncTaskLoadBitmap.LoadProcessListener{

    Piet mPiet;
    InOutSystemEditText mInOutSystem;
    ColorFieldView mColorField;
    private int mActiveColor;
    private FilledCircleDrawable mDebugDrawable;
    TextView mInfoText;
    long mSleepBetweenStep;
    
    AsyncTaskRunPiet mCurrentRunTask;
    
    ControlToolBoxFragment mToolBoxFragment;
    CommandHelperFragment mCommandHelperFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        mSleepBetweenStep = 2L;
        
        mDebugDrawable = new FilledCircleDrawable();
        int drawableColor = resources.getColor(R.color.debug_cell_highlight);
        mDebugDrawable.setColor(drawableColor);
        
        mActiveColor = 0;
        
        initPiet(resources);
        mInfoText = (TextView) findViewById(R.id.edit_text_info);
        initColorField();
        
        mActiveColor = resources.getColor(R.color.default_draw_color);
                
        mToolBoxFragment = (ControlToolBoxFragment) getSupportFragmentManager()
                .findFragmentById(R.id.control_toolbox_fragment);
        
        
        mCommandHelperFragment = (CommandHelperFragment) getSupportFragmentManager()
                .findFragmentById(R.id.command_helper_fragment);
        
        final Button buttonLoad = (Button) findViewById(R.id.button_load);
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                load();
            }
        });
        
        PaletteFragmentSimple palette =  (PaletteFragmentSimple) getSupportFragmentManager()
                .findFragmentById(R.id.palette_fragment);
        
        palette.setPiet(mPiet);
        palette.chooseColor(mActiveColor);
        
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
        int stepCounter = mPiet.getStepNumber();
        DirectionPointer directionPointer = mPiet.getDirectionPointer();

        CodelChoser codelChoser = mPiet.getCodelChoser();
        Codel currentCodel = mPiet.getCurrentCodel();

        String info = String.format(Locale.US,
                "step : %d cur codel : (%d,%d) \n DP : %s, CC : %s", stepCounter,
                currentCodel.x, currentCodel.y,
                directionPointer.toString(), codelChoser.toString());
        
        mColorField.setCellDrawable(currentCodel.x, currentCodel.y, mDebugDrawable);
        mInfoText.setText(info);
        mInOutSystem.flush();
    }
    
    private void initPiet(Resources resources) {

        final EditText inText = (EditText) findViewById(R.id.edit_text_in);
        final TextView outText = (TextView) findViewById(R.id.edit_text_out);

        mInOutSystem = new InOutSystemEditText(inText, outText);
        LoggerDroid logger = new LoggerDroid();

        mPiet = new Piet(logger, mInOutSystem);

        int countX = resources.getInteger(R.integer.field_count_codels_x);
        int countY = resources.getInteger(R.integer.field_count_codels_y);

        mPiet.createModel(countX, countY);
        
        mPiet.setCommandRunListener(new CommandRunListener() {
            @Override
            public void onRunCommand(Command command, PietMachineStack stack) {
                Log.i("RUN COMMAND",
                        String.format("%s : %s", command.toString(),
                                stack.toString()));
            }
        });
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
        mCommandHelperFragment.setColor(color, mPiet);
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
        mInfoText.setText("");
        mInOutSystem.prepare();
        mPiet.init();
    }

    @Override
    public void onRunCancel() {
        // TODO Auto-generated method stub
        mColorField.clearDrawables();
        
        mInfoText.setText("");
        mInOutSystem.prepare();
        
        mCurrentRunTask = null;
    }
    
    //codelsToUpdate must be synchronized
    @Override
    public void onRunUpdate(List<Codel> codelsToUpdate) {
        updateViewAfterStep();
        
        synchronized (codelsToUpdate) {
            for (Codel codel : codelsToUpdate) {
                mColorField.setCellDrawable(codel.x, codel.y, mDebugDrawable);
            }
        }
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

    @Override
    public void onLoadBitmapUpdate(List<Pixel> pixels) {
        
        synchronized (pixels) {
            for(Pixel pixel : pixels){
                setCell(pixel.x, pixel.y, pixel.color);
            }
        }
    }

    @Override
    public void onLoadBitmapComplete() {
        mColorField.setVisibility(View.VISIBLE);
        mColorField.invalidate();
    }
}
