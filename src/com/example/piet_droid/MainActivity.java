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

public class MainActivity extends SherlockFragmentActivity implements
        FragmentControlToolBox.InteractionListener,
        FragmentPaletteSimple.OnChooseColorListener,
        AsyncTaskRunPiet.ExecutionProcessListener,
        AsyncTaskLoadBitmap.LoadProcessListener, PietProvider {

    private static final int REQUEST_SAVE = 0;

    private static final int REQUEST_OPEN = 1;

    private static final int SHOW_PREFERENCES = 1;

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
        /*
         * mFragmentPaletteSimple = (FragmentPaletteSimple)
         * getSupportFragmentManager()
         * .findFragmentById(R.id.fragment_palette_simple);
         */
    }

    @Override
    public void onStart() {
        super.onStart();
        // mCommandHelperFragment.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
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
        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.loadFolder(Environment.getExternalStorageDirectory().getPath());
        dialog.setFilter(".*jpg|.*jpeg|.*png|.*gif|.*JPG|.*JPEG|.*PNG|.*GIF|");

        dialog.setCanCreateFiles(true);
        dialog.setFolderMode(false);
        dialog.setShowConfirmation(true, false);

        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                source.hide();
                MainActivity.this.saveImageFile(file.getAbsolutePath());
            }

            public void onFileSelected(Dialog source, File folder, String name) {
                source.hide();
                String path = folder.getAbsolutePath() + "/" + name;
                MainActivity.this.saveImageFile(path);
            }
        });

        dialog.show();
    }

    private void onActionLoad() {
        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.loadFolder(Environment.getExternalStorageDirectory().getPath());
        dialog.setFilter(".*jpg|.*jpeg|.*png|.*gif|.*JPG|.*JPEG|.*PNG|.*GIF|");
        dialog.setCanCreateFiles(false);
        dialog.setFolderMode(false);

        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                source.hide();
                MainActivity.this.loadImageFile(file.getAbsolutePath());
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
        clearCells();
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
    
    private void clearColorFieldDrawables() {
        mColorField.clearDrawables();
    }

    private void clearCells() {
        mColorField.clearAll();
        mPiet.clear();
    }

    private void setCell(int x, int y, int color) {
        mColorField.setCellColor(x, y, color);
        mPiet.setColor(x, y, color);
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
                        setCell(x, y, mActiveColor);
                        // FIXME
                        MainActivity.this.mColorField.setCellToRedraw(x, y);
                    }

                    @Override
                    public boolean isProcessClickWanted() {
                        return MainActivity.this.isOnRunMode() == false;
                    }
                });
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

            return;
        }

        mCurrentRunTask = new AsyncTaskRunPiet(this, mSleepBetweenStep);
        mCurrentRunTask.execute(mPiet);
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
    }

    @Override
    public void onInteractionPause() {

        if (mCurrentRunTask == null) {
            return;
        }

        mCurrentRunTask.setWait();
    }

    @Override
    public void onInteractionStop() {
        if (mCurrentRunTask == null) {
            return;
        }

        mCurrentRunTask.cancel(true);
    }

    @Override
    public void onRunStart() {
        mFragmentStateInfo.init();
        clearColorFieldDrawables();
        mFragmentInOutBuffers.prepare();
    }

    @Override
    public void onRunCancel() {
        mPiet.init();
        clearColorFieldDrawables();
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

    private void showInToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public void saveImageFile(String path) {
        AsyncTaskWriteBitmap saveTask = new AsyncTaskWriteBitmap(mPiet,
                new AsyncTaskWriteBitmap.SaveProcessListener() {

                    @Override
                    public void onSaveBitmapError() {
                        // TODO Auto-generated method stub
                        showInToast("Error occurred during saving bitmap");
                    }

                    @Override
                    public void onSaveBitmapComplete() {
                        showInToast("Bitmap saved");
                    }

                    @Override
                    public void onSaveBitmapCancel() {
                    }
                }, this);

        saveTask.execute(path);
    }

    public void loadImageFile(String path) {
        // TODO FADE OUT FADE IN
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        // "/data/helloWorld_small.png"
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

    public void onLoadBitmapPixel(int x, int y, int color) {
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
