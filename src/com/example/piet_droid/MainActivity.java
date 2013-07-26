package com.example.piet_droid;

import java.io.File;

import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.jpiet.Codel;
import com.example.jpiet.CodelColor;
import com.example.jpiet.CodelTableModel;
import com.example.jpiet.Logger;

import com.example.jpiet.InOutSystem;

import com.example.jpiet.Piet;

//TODO CONTEXT MENU
public class MainActivity extends SherlockFragmentActivity implements
        FragmentControlToolBox.InteractionListener,
        FragmentPaletteSimple.OnChooseColorListener,
        AsyncTaskRunPiet.ExecutionProcessListener, PietProvider {

    private static final int REQUEST_SAVE = 0;

    private static final int REQUEST_OPEN = 1;

    private static final int SHOW_PREFERENCES = 1;

    PietFile mCurrentFile;

    Piet mPiet;

    ColorFieldView mColorField;
    private int mActiveColor;
    private DrawableFilledCircle mCurrentCellDrawable;
    private DrawableFilledCircle mPreviousCellDrawable;

    long mSleepBetweenStep;

    

    FragmentControlToolBox mFragmentControlToolBox;
    // FragmentCommandHelper mCommandHelperFragment;
    FragmentStateInfo mFragmentStateInfo;

    FragmentCommandLog mFragmentCommandLog;

    // FragmentPaletteSimple mFragmentPaletteSimple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        mSleepBetweenStep = 1L;

        mCurrentCellDrawable = new DrawableFilledCircle();
        int currentDrawableColor = resources
                .getColor(R.color.debug_cell_highlight);
        mCurrentCellDrawable.setColor(currentDrawableColor);

        mPreviousCellDrawable = new DrawableFilledCircle();
        int prevDrawableColor = resources
                .getColor(R.color.debug_previous_cell_highlight);
        mPreviousCellDrawable.setColor(prevDrawableColor);

        mActiveColor = 0;

        mFragmentControlToolBox = (FragmentControlToolBox) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_control_toolbox);

        /*
         * mCommandHelperFragment = (FragmentCommandHelper)
         * getSupportFragmentManager()
         * .findFragmentById(R.id.fragment_command_helper);
         */

        /*
         * mFragmentPaletteSimple = (FragmentPaletteSimple)
         * getSupportFragmentManager()
         * .findFragmentById(R.id.fragment_palette_simple);
         */

        mFragmentStateInfo = (FragmentStateInfo) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_state_info);

        mFragmentCommandLog = (FragmentCommandLog) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_command_log);

        initPiet(resources);
        mCurrentFile = null;
        initColorField();

        if (savedInstanceState != null) {
            initRestoredState(savedInstanceState);
        } else {
            initDefaultState();
        }

        initHelpersTabHost();
    }

    private void initDefaultState() {
        // TODO Auto-generated method stub

        Resources resources = getResources();
        int countX = resources.getInteger(R.integer.field_count_codels_x);
        int countY = resources.getInteger(R.integer.field_count_codels_y);

        initNewPietFile(countX, countY);
    }

    private void initRestoredState(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        CodelTableModel.SerializedData data = (CodelTableModel.SerializedData) savedInstanceState
                .getSerializable(SAVE_KEY_MODEL);
        
        initNewPietFile(data);

        String fileName = savedInstanceState
                .getString(SAVE_KEY_CURRENT_FILENAME);
        if (fileName == null) {
            return;
        }

        getCurrentPietFile().setPath(fileName);
    }

    private void initPiet(Resources resources) {
        final EditText inText = (EditText) findViewById(R.id.text_view_in);
        final TextView outText = (TextView) findViewById(R.id.text_view_out);

        InOutSystem inOutSystem = new InOutSystemEditText(inText, outText);

        LoggerDroid logger = new LoggerDroid();

        mPiet = new Piet(logger, inOutSystem);
    }

    private void initColorField() {
        mColorField = (ColorFieldView) findViewById(R.id.codelField);

        mColorField
                .setOnCellClickListener(new ColorFieldView.CellClickListener() {
                    @Override
                    public void onCellClick(int x, int y) {
                        PietFileActor actor = MainActivity.this
                                .getCurrentPietFile().getActor();
                        actor.setCell(x, y, MainActivity.this.getActiveColor());
                        actor.redrawCell(x, y);
                    }

                    @Override
                    public boolean isProcessClickWanted() {
                        if (MainActivity.this.isOnRunMode() == true) {
                            getCurrentPietFile()
                                    .getActor()
                                    .showMessage(
                                            "Edit mode disabled until program executed.");
                            return false;
                        }

                        return true;
                    }
                });
    }

    private void initNewPietFile(int countX, int countY) {
        if (mCurrentFile != null) {
            mCurrentFile.finalise();
        }

        PietFileActor actor = new PietFileActor(mColorField, mPiet, this);
        actor.resize(countX, countY);
        actor.invalidateView();
        mCurrentFile = new PietFile(actor);
    }

    private void initNewPietFile(CodelTableModel.SerializedData data) {
        if (mCurrentFile != null) {
            mCurrentFile.finalise();
        }
        
        CodelTableModel model = CodelTableModel.createCodelTableModelFromSerializedData(data);
        PietFileActor actor = new PietFileActor(mColorField, mPiet, this);
        actor.attachModel(model);
        actor.invalidateView();
        mCurrentFile = new PietFile(actor);
    }

    private void initHelpersTabHost() {
        Resources resources = getResources();

        final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);

        HelperTabHost
                .create(tabs)
                .setActiveTabColor(Color.parseColor("#9a0000"))
                .setPassiveTabColor(Color.parseColor("#555555"))
                .setTabHeight(50)
                .setTextColor(Color.parseColor("#ffffff"))
                .addTab(R.id.tabInput, "input",
                        resources.getString(R.string.tab_in))
                .addTab(R.id.tabOutput, "output",
                        resources.getString(R.string.tab_out))
                .addTab(R.id.tabState, "state",
                        resources.getString(R.string.tab_state))
                .addTab(R.id.tabLog, "log",
                        resources.getString(R.string.tab_log)).build(0);
    }

    // Called after onCreate has finished, use to restore UI state
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        // Will only be called if the Activity has been
        // killed by the system since it was last visible.
    }

    // Called before subsequent visible lifetimes
    // for an Activity process.
    @Override
    public void onRestart() {
        super.onRestart();
        // Load changes knowing that the Activity has already
        // been visible within this process.
    }

    // Called at the start of the active lifetime.
    @Override
    public void onResume() {
        super.onResume();
        // Resume any paused UI updates, threads, or processes required
        // by the Activity but suspended when it was inactive.
    }

    @Override
    public void onStart() {
        super.onStart();
        // mCommandHelperFragment.invalidate();
    }

    private final String SAVE_KEY_MODEL = "PietCodelTableModel";
    private final String SAVE_KEY_CURRENT_FILENAME = "PietCurrentFileName";

    // Called to save UI state changes at the
    // end of the active lifecycle.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate and
        // onRestoreInstanceState if the process is
        // killed and restarted by the run time.

        super.onSaveInstanceState(savedInstanceState);

        CodelTableModel model = getPiet().getModel();
        CodelTableModel.SerializedData data = model.getSerializeData();
        savedInstanceState.putSerializable(SAVE_KEY_MODEL, data);

        if (hasPietFile() == false) {
            return;
        }

        if (getCurrentPietFile().hasPath() == false) {
            return;
        }

        savedInstanceState.putString(SAVE_KEY_CURRENT_FILENAME,
                getCurrentPietFile().getPath());
    }

    // Called at the end of the active lifetime.
    @Override
    public void onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don’t need to be updated when the Activity isn’t
        // the active foreground Activity.
        super.onPause();
    }

    // Called at the end of the visible lifetime.
    @Override
    public void onStop() {
        // Suspend remaining UI updates, threads, or processing
        // that aren’t required when the Activity isn’t visible.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        super.onStop();
    }

    // Sometimes called at the end of the full lifetime.
    @Override
    public void onDestroy() {
        // Clean up any resources including ending threads,
        // closing database connections etc.
        super.onDestroy();
    }

    private Menu mMenu = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        return true;
    }

    public void hideFileMenu() {
        MenuItem fileMenu = mMenu.findItem(R.id.action_bitmap);
        fileMenu.setVisible(false);
    }

    public void showFileMenu() {
        MenuItem fileMenu = mMenu.findItem(R.id.action_bitmap);
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
            onActionNew();
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

    private void onActionNew() {
        // TODO DIALOG WITH width height!!
        DialogFragmentNewFileSettings dialog = new DialogFragmentNewFileSettings();
        dialog.setListener(new DialogFragmentNewFileSettings.Listener() {

            @Override
            public void onCancelNewFileSettings() {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onAcceptNewFileSettings(int width, int height) {
                // TODO Auto-generated method stub
                if (width == 0 || height == 0) {
                    return false;
                }

                initNewPietFile(width, height);
                return true;
            }
        });

        dialog.show(getSupportFragmentManager(),
                "DialogFragmentNewFileSettings");
    }

    private void onActionSettings() {
        startActivityForResult(new Intent(this, Preferences.class),
                SHOW_PREFERENCES);
    }

    private void onActionSave() {
        if (getCurrentPietFile().hasPath() == false) {
            onActionSaveAs();
            return;
        }

        getCurrentPietFile().getActor().save();
    }

    private void onActionSaveAs() {
        if (isOnRunMode()) {
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
        if (isOnRunMode()) {
            onRunCancel();
        }

        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.loadFolder(Environment.getExternalStorageDirectory().getPath());
        dialog.setFilter(".*jpg|.*jpeg|.*png|.*gif|.*JPG|.*JPEG|.*PNG|.*GIF|");
        dialog.setCanCreateFiles(false);
        dialog.setFolderMode(false);

        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                // TODO CHECK ERROR!!!!!
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

    // TODO CHECK REAL APPLICATION FOR PREFS EXAMPLE!
    private void updateFromPreferences() {
        Context context = getApplicationContext();
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        mSleepBetweenStep = Long.valueOf(preferences.getString(
                "delay_before_step", "0"));
        if (isOnRunMode()) {
            getCurrentPietFile().getActor().setStepDelay(mSleepBetweenStep);
        }
    }

    protected int getActiveColor() {
        return mActiveColor;
    }

    public PietFile getCurrentPietFile() {
        return mCurrentFile;
    }

    protected boolean hasPietFile() {
        return mCurrentFile != null;
    }

    public boolean isOnRunMode() {
        return mCurrentRunTask != null;
    }

    @Override
    public void onChooseColor(int color) {
        mActiveColor = color;
        // mCommandHelperFragment.setColor(color);
    }
    
    public PietFileActor getCurrentActor() {
        return getCurrentPietFile().getActor();
    }
    
    @Override
    public void onInteractionRun() {
        getCurrentActor().run(mSleepBetweenStep);
        // hideFileMenu();
    }

    @Override
    public void onInteractionStep() {
        getCurrentActor().step(mSleepBetweenStep);

        // hideFileMenu();
    }

    @Override
    public void onInteractionPause() {
        if (isOnRunMode() == false) {
            return;
        }

        showFileMenu();
        getCurrentActor().pause();
    }

    @Override
    public void onInteractionStop() {
        if (isOnRunMode() == false) {
            return;
        }

        showFileMenu();
        getCurrentActor().stop();
    }

    @Override
    public void onRunStart() {
        mPreviousCodel = new Codel(0, 0);
        getPiet().init();
        mFragmentStateInfo.init();
        PietFileActor actor = getCurrentPietFile().getActor();
        actor.clearViewDrawables();
        actor.setCellDrawable(0, 0, mCurrentCellDrawable);
        getPiet().getInOutSystem().prepare();
    }

    @Override
    public void onRunCancel() {
        getCurrentPietFile().getActor().clearViewDrawables();
        mFragmentStateInfo.init();
    }

    private Codel mPreviousCodel;

    @Override
    public void onRunUpdate(Codel codel) {
        mFragmentStateInfo.update();
        mPiet.getInOutSystem().flush();
        mFragmentCommandLog.update();

        PietFileActor actor = getCurrentPietFile().getActor();
        actor.setCellDrawable(mPreviousCodel.x, mPreviousCodel.y,
                mPreviousCellDrawable);

        actor.setCellDrawable(codel.x, codel.y, mCurrentCellDrawable);
        mPreviousCodel.set(codel);
    }

    @Override
    public void onRunComplete() {
        getCurrentPietFile().getActor().setCellDrawable(mPreviousCodel.x,
                mPreviousCodel.y, mPreviousCellDrawable);
        mFragmentControlToolBox.setControlsToDefaultState();
        mCurrentRunTask = null;
    }

    @Override
    public Piet getPiet() {
        return mPiet;
    }
}
