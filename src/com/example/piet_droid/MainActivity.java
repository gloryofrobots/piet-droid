package com.example.piet_droid;

import java.io.File;

import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.jpiet.Codel;

import com.example.jpiet.InOutSystem;

import com.example.jpiet.Piet;

//TODO CONTEXT MENU
public class MainActivity extends SherlockFragmentActivity implements
        FragmentControlToolBox.InteractionListener,
        FragmentPaletteSimple.OnChooseColorListener,
        AsyncTaskRunPiet.ExecutionProcessListener,
        PietProvider {

    private static final int REQUEST_SAVE = 0;

    private static final int REQUEST_OPEN = 1;

    private static final int SHOW_PREFERENCES = 1;
    
    PietFile mCurrentFile;
    
    Piet mPiet;

    ColorFieldView mColorField;
    private int mActiveColor;
    private DrawableFilledCircle mDebugDrawable;

    long mSleepBetweenStep;

    AsyncTaskRunPiet mCurrentRunTask;

    FragmentInOutBuffers mFragmentInOutBuffers;
    FragmentControlToolBox mFragmentControlToolBox;
    // FragmentCommandHelper mCommandHelperFragment;
    FragmentStateInfo mFragmentStateInfo;

    // FragmentCommandLog mFragmentCommandLog;
    // FragmentPaletteSimple mFragmentPaletteSimple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        mSleepBetweenStep = 1L;

        mDebugDrawable = new DrawableFilledCircle();
        int drawableColor = resources.getColor(R.color.debug_cell_highlight);
        mDebugDrawable.setColor(drawableColor);

        mActiveColor = 0;

        mFragmentControlToolBox = (FragmentControlToolBox) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_control_toolbox);

        /*
         * mCommandHelperFragment = (FragmentCommandHelper)
         * getSupportFragmentManager()
         * .findFragmentById(R.id.fragment_command_helper);
         */

        mFragmentStateInfo = (FragmentStateInfo) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_state_info);
        /*
         * mFragmentCommandLog = (FragmentCommandLog)
         * getSupportFragmentManager()
         * .findFragmentById(R.id.fragment_command_log);
         */
        mFragmentInOutBuffers = (FragmentInOutBuffers) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_in_out_buffers);

        initPiet(resources);

        initColorField();
        
        mCurrentFile = null;
        initNewPietFile();
        
        /*
         * mFragmentPaletteSimple = (FragmentPaletteSimple)
         * getSupportFragmentManager()
         * .findFragmentById(R.id.fragment_palette_simple);
         */
    }
    
    private void initNewPietFile() {
        if(mCurrentFile != null) {
            mCurrentFile.finalise();
        }
        
        PietFileActor actor = new PietFileActor(mColorField, mPiet, this);
        mCurrentFile = new PietFile(actor);
    }

    @Override
    public void onStart() {
        super.onStart();
        // mCommandHelperFragment.invalidate();
    }
    
    private Menu mMenu = null;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        return true;
    }
    
    public void hideFileMenu() {
        MenuItem fileMenu = mMenu.findItem(R.id.action_file);
        fileMenu.setVisible(false);
    }
    
    public void showFileMenu() {
        MenuItem fileMenu = mMenu.findItem(R.id.action_file);
        fileMenu.setVisible(true);
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
        case (R.id.action_load):
            onActionLoad();
            return true;
        case (R.id.action_clear):
            onActionClear();
            return true;
        case (R.id.action_new):
            return true;
        case (R.id.action_quit):
            return true;
        case (R.id.action_save): {
            onActionSave();
            return true;
        }
        case (R.id.action_save_as): {
            onActionSaveAs();
            return true;
        }
        case (R.id.action_settings):
            onActionSettings();
            return true;

        default:
            return false;
        }
    }

    public void onActionSettings() {
        startActivityForResult(new Intent(this, Preferences.class),
                SHOW_PREFERENCES);
    }

    
    public void onActionSave() {
        //if current file source not null save it
    }
    
    public void onActionSaveAs() {
        if(isOnRunMode()) {
            onRunCancel();
        }
        
        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.loadFolder(Environment.getExternalStorageDirectory().getPath());
        dialog.setFilter(".*jpg|.*jpeg|.*png|.*gif|.*JPG|.*JPEG|.*PNG|.*GIF|");

        dialog.setCanCreateFiles(true);
        dialog.setFolderMode(false);
        dialog.setShowConfirmation(true, false);

        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                source.hide();
                getCurrentPietFile().getActor().save(file.getAbsolutePath());
            }

            public void onFileSelected(Dialog source, File folder, String name) {
                source.hide();
                String path = folder.getAbsolutePath() + "/" + name;
                getCurrentPietFile().getActor().save(path);
            }
        });

        dialog.show();
    }

    private void onActionLoad() {
        if(isOnRunMode()) {
            onRunCancel();
        }
        
        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.loadFolder(Environment.getExternalStorageDirectory().getPath());
        dialog.setFilter(".*jpg|.*jpeg|.*png|.*gif|.*JPG|.*JPEG|.*PNG|.*GIF|");
        dialog.setCanCreateFiles(false);
        dialog.setFolderMode(false);

        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                //TODO CHECK ERROR!!!!!
                source.hide();
                getCurrentPietFile().getActor().load(file.getAbsolutePath());
            }

            public void onFileSelected(Dialog source, File folder, String name) {

            }
        });

        dialog.show();
    }

    public void onActionClear() {
        if (isOnRunMode()) {
            onInteractionStop();
        }

        mFragmentControlToolBox.setControlsToDefaultState();
        getCurrentPietFile().getActor().clear();
    }

    public synchronized void onActivityResult(final int requestCode,
            int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_PREFERENCES) {
                updateFromPreferences();
        }
    }
    //TODO CHECK REAL APPLICATION FOR PREFS EXAMPLE!
    private void updateFromPreferences() {
        Context context = getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mSleepBetweenStep = Long.valueOf(preferences.getString("delay_before_step", "0"));
        if(isOnRunMode()) {
            mCurrentRunTask.setStepDelay(mSleepBetweenStep);
        }
    }
    
    private void updateViewAfterStep() {
        Codel currentCodel = mPiet.getCurrentCodel();
        mColorField.setCellDrawable(currentCodel.x, currentCodel.y,
                mDebugDrawable);
        mFragmentStateInfo.update();
        mFragmentInOutBuffers.update();
        // mFragmentCommandLog.update();
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
                        PietFileActor actor = MainActivity.this.getCurrentPietFile().getActor();
                        actor.setCell(x, y, MainActivity.this.getActiveColor());
                        actor.redrawCell(x, y);
                    }

                    @Override
                    public boolean isProcessClickWanted() {
                        return MainActivity.this.isOnRunMode() == false;
                    }
                });
    }
    
    protected int getActiveColor() {
        return mActiveColor;
    }

    public PietFile getCurrentPietFile() {
        return mCurrentFile;
    }
    
    public boolean isOnRunMode() {
        return mCurrentRunTask != null;
    }

    @Override
    public void onChooseColor(int color) {
        mActiveColor = color;
        // mCommandHelperFragment.setColor(color);
    }

    @Override
    public void onInteractionRun() {
        if (mCurrentRunTask != null) {
            if (mCurrentRunTask.isWaiting()) {
                mCurrentRunTask.allowRun();
            }
        }
        else {
            mCurrentRunTask = new AsyncTaskRunPiet(this, mSleepBetweenStep);
            mCurrentRunTask.execute(mPiet);
        }
        
        //hideFileMenu();
    }

    @Override
    public void onInteractionStep() {
        if (mCurrentRunTask != null) {
            mCurrentRunTask.allowOneStepOnly();
        } else {
            mCurrentRunTask = new AsyncTaskRunPiet(this, mSleepBetweenStep);
            mCurrentRunTask.allowOneStepOnly();
            mCurrentRunTask.execute(mPiet);
        }
        
        //hideFileMenu();
    }

    @Override
    public void onInteractionPause() {
        if (mCurrentRunTask == null) {
            return;
        }
        
        showFileMenu();
        mCurrentRunTask.setWait();
    }

    @Override
    public void onInteractionStop() {
        if (mCurrentRunTask == null) {
            return;
        }
        
        showFileMenu();
        mCurrentRunTask.cancel(true);
    }

    @Override
    public void onRunStart() {
        mFragmentStateInfo.init();
        PietFileActor actor =getCurrentPietFile().getActor();
        actor.clearViewDrawables();
        actor.setCellDrawable(0, 0, mDebugDrawable);
        mFragmentInOutBuffers.prepare();
    }

    @Override
    public void onRunCancel() {
        mPiet.init();
        getCurrentPietFile().getActor().clearViewDrawables();
        mFragmentStateInfo.init();
        mFragmentInOutBuffers.prepare();
        mCurrentRunTask = null;
    }

    @Override
    public void onRunUpdate(Codel codel) {
        if (mCurrentRunTask == null || mCurrentRunTask.isCancelled()) {
            return;
        }

        updateViewAfterStep();
        mColorField.setCellDrawable(codel.x, codel.y, mDebugDrawable);
    }

    @Override
    public void onRunComplete() {
        mFragmentControlToolBox.setControlsToDefaultState();
        mCurrentRunTask = null;
    }
   
    @Override
    public Piet getPiet() {
        return mPiet;
    }
}
