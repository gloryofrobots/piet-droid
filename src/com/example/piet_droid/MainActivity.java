package com.example.piet_droid;

import java.util.ArrayList;

import android.os.AsyncTask;
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

public class MainActivity extends FragmentActivity implements
        ControlToolBoxFragment.InteractionListener, PaletteFragment.OnChooseColorListener {

    Piet mPiet;
    InOutSystemEditText mInOutSystem;
    ColorFieldView mColorField;
    private int mActiveColor;
    private FilledCircleDrawable mDebugDrawable;
    TextView mInfoText;
    long mSleepBetweenStep;
    
    RunPietTask mCurrentRunTask;
    
    ControlToolBoxFragment mToolBoxFragment;
    
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
        
        mToolBoxFragment = (ControlToolBoxFragment) getSupportFragmentManager()
                .findFragmentById(R.id.control_toolbox_fragment);
        
        
        final Button buttonLoad = (Button) findViewById(R.id.button_load);
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                load();
            }
        });
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

    public void load() {
        Bitmap bitmap = BitmapFactory.decodeFile("/data/helloWorld.png");
        // TODO CODEL SIZE HERE
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        mPiet.createModel(width, height);
        mColorField.resize(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                setCell(x, y, pixel);
            }
        }

        bitmap.recycle();
        mColorField.invalidate();
    }

    @Override
    public void onInteractionRun() {
        if(mCurrentRunTask != null) {
            mCurrentRunTask.cancel(true);
        }
        
        mInfoText.setText("");
        mInOutSystem.prepare();
        mPiet.init();
        
        mCurrentRunTask = new RunPietTask();
        mCurrentRunTask.execute();
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
        mCurrentRunTask.cancel(true);
    }
    
    private void updateViewAfterStep(){
        int stepCounter = mPiet.getStepNumber();
        DirectionPointer directionPointer = mPiet.getDirectionPointer();

        CodelChoser codelChoser = mPiet.getCodelChoser();
        Codel currentCodel = mPiet.getCurrentCodel();

        String info = String.format(
                "step : %d cur codel : (%d,%d) \n DP : %s, CC : %s", stepCounter,
                currentCodel.x, currentCodel.y,
                directionPointer.toString(), codelChoser.toString());

        mInfoText.setText(info);
        mInOutSystem.flush();
    }
    
    class RunPietTask extends AsyncTask<Void, Integer, Integer> {
        ArrayList<Codel> mQueue;
        boolean mLock;

        @Override
        protected void onPreExecute() {
            mQueue = new ArrayList<Codel>();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int count = 0;
            while (mPiet.step() == true) {
                Codel currentCodel = mPiet.getCurrentCodel();

                while (mLock == true) {
                }

                mQueue.add(new Codel(currentCodel));

                count++;
                publishProgress(count);
                try {
                    Thread.sleep(mSleepBetweenStep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return count;
        }

        protected void onProgressUpdate(Integer... progress) {
            
            updateViewAfterStep();
            
            for (Codel codel : mQueue) {
                mColorField.setCellDrawable(codel.x, codel.y, mDebugDrawable);
            }

            mLock = true;
            mQueue.clear();
            mLock = false;
        }
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
                        // Log.e("TEST", String.format("%d-%d", x, y));
                        setCell(x, y, mActiveColor);
                        mColorField.setCellToRedraw(x, y);
                    }
                });
    }

    @Override
    public void onChooseColor(int color) {
        mActiveColor = color;
    }
}
