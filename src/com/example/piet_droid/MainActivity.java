package com.example.piet_droid;

import java.io.File;
import java.lang.reflect.Method;

import java.util.List;
import java.util.concurrent.Callable;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import android.util.AttributeSet;
import android.util.Log;

import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.jpiet.Codel;
import com.example.jpiet.CodelColor;
import com.example.jpiet.CodelTableModel;
import com.example.jpiet.CodelTableModelSerializedData;
import com.example.jpiet.Logger;

import com.example.jpiet.InOutSystem;

import com.example.jpiet.Piet;
import com.example.piet_droid.fragment.DialogFragmentNewFileSettings;
import com.example.piet_droid.fragment.DialogFragmentSaveChanges;
import com.example.piet_droid.fragment.FragmentCommandLog;
import com.example.piet_droid.fragment.FragmentPaletteSimple;
import com.example.piet_droid.fragment.FragmentStateInfo;
import com.example.piet_droid.fragment.PietPreferenceFragment;
import com.example.piet_droid.widget.ColorFieldView;
import com.example.piet_droid.widget.ControlToolboxView;
import com.example.piet_droid.widget.DrawableFilledCircle;
import com.example.piet_droid.widget.HelperTabHost;
import com.example.piet_droid.widget.HorizontalScrollViewLockable;
import com.example.piet_droid.widget.ScrollViewLockable;

//TODO CONTEXT MENU
public class MainActivity extends SherlockFragmentActivity implements
        FragmentPaletteSimple.OnChooseColorListener, PietProvider {

    // //////////////////////////////////////////////////////////////////////////
    private class RunListener implements PietFileRunner.RunEventListener {

        private Codel mPreviousCodel;
        private DrawableFilledCircle mCurrentCellDrawable;
        private DrawableFilledCircle mPreviousCellDrawable;

        public void initDebugDrawables(Resources resources) {
            mCurrentCellDrawable = new DrawableFilledCircle();
            int currentDrawableColor = resources
                    .getColor(R.color.debug_cell_highlight);
            mCurrentCellDrawable.setColor(currentDrawableColor);

            mPreviousCellDrawable = new DrawableFilledCircle();
            int prevDrawableColor = resources
                    .getColor(R.color.debug_previous_cell_highlight);

            mPreviousCellDrawable.setColor(prevDrawableColor);
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
            PietFileActor actor = getCurrentPietFile().getActor();
            actor.clearViewDrawables();
            mFragmentStateInfo.init();
        }

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
            PietFileActor actor = getCurrentPietFile().getActor();
            actor.setCellDrawable(mPreviousCodel.x, mPreviousCodel.y,
                    mPreviousCellDrawable);
            mControlToolBoxView.setControlsToDefaultState();
        }

    }

    // //////////////////////////////////////////////////////////////////////////
    private ControlToolboxView.InteractionListener mInteractionListener = new ControlToolboxView.InteractionListener() {

        @Override
        public void onInteractionRun() {
            getCurrentPietFile().getRunner().run(mSleepBetweenStep);
            // hideFileMenu();
        }

        @Override
        public void onInteractionStep() {
            getCurrentPietFile().getRunner().step(mSleepBetweenStep);

            // hideFileMenu();
        }

        @Override
        public void onInteractionPause() {
            if (isOnRunMode() == false) {
                return;
            }

            // /showFileMenu();
            stopRun();
        }

        @Override
        public void onInteractionStop() {
            if (isOnRunMode() == false) {
                return;
            }

            // showFileMenu();
            stopRun();
        }
    };

    // ////////////////////////////////////////////////////////////////////////

    private static final int REQUEST_SAVE = 0;

    private static final int REQUEST_OPEN = 1;

    private static final int SHOW_PREFERENCES = 1;

    PietFile mCurrentFile;

    Piet mPiet;

    ColorFieldView mColorField;
    private int mActiveColor;

    RunListener mRunListener;
    long mSleepBetweenStep;

    FragmentStateInfo mFragmentStateInfo;
    ControlToolboxView mControlToolBoxView;
    FragmentCommandLog mFragmentCommandLog;

    private int mZoomChangeValue = 10;
    
    boolean mOnScrollMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSleepBetweenStep = 1L;
        mActiveColor = 0;

        Resources resources = getResources();

        initRunListener(resources);
        initFragments();

        initPiet(resources);
        mCurrentFile = null;
        initColorField();

        if (savedInstanceState != null) {
            initRestoredState(savedInstanceState);
        } else {
            initDefaultState(resources);
        }

        initHelpersTabHost();
        initActionBarAndScrollLock();
        updateFromPreferences();
        getCurrentPietFile().getActor().lockOrientation();
    }

    private void initRunListener(Resources resources) {
        mRunListener = new RunListener();
        mRunListener.initDebugDrawables(resources);
    }

    private void initActionBarAndScrollLock() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.action_bar_custom);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                );
        mControlToolBoxView = (ControlToolboxView) actionBar.getCustomView()
                .findViewById(R.id.fragment_control_toolbox);

        mControlToolBoxView.setInteractionListener(mInteractionListener);

        final ImageButton buttonScroll = (ImageButton) actionBar
                .getCustomView().findViewById(R.id.button_scroll_toggle);
        
        final ImageButton buttonZoomInrease = (ImageButton) actionBar
                .getCustomView().findViewById(R.id.button_zoom_increase);
        
        final ImageButton buttonZoomDecrease = (ImageButton) actionBar
                .getCustomView().findViewById(R.id.button_zoom_decrease);

        initScrollLock(buttonScroll);
        initZoomButtons(buttonZoomInrease, buttonZoomDecrease);
    }
    
    private void initZoomButtons(final ImageButton buttonZoomIncrease, final ImageButton buttonZoomDecrease) {
        
        buttonZoomIncrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int width = mColorField.getCellWidth();
                int newSide = width + mZoomChangeValue;
                
                if(newSide > ColorFieldView.MIN_CELL_SIDE) {
                    buttonZoomDecrease.setEnabled(true);
                }
                
                if(newSide > ColorFieldView.MAX_CELL_SIDE) {
                    buttonZoomIncrease.setEnabled(false);
                    return;
                }
                
                mColorField.setCellSide(newSide);
            }
        });
        
        buttonZoomDecrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int width = mColorField.getCellWidth();
                int newSide = width - mZoomChangeValue;
                
                if(newSide < ColorFieldView.MAX_CELL_SIDE) {
                    buttonZoomIncrease.setEnabled(true);
                }
                
                if(newSide < ColorFieldView.MIN_CELL_SIDE) {
                    buttonZoomDecrease.setEnabled(false);
                    return;
                }
                
                mColorField.setCellSide(newSide);
            }
        });
        
        int width = mColorField.getCellWidth();
        if((width - mZoomChangeValue) <= ColorFieldView.MIN_CELL_SIDE) {
            buttonZoomDecrease.setEnabled(false);
        } else if((width + mZoomChangeValue) >= ColorFieldView.MAX_CELL_SIDE) {
            buttonZoomIncrease.setEnabled(false);
        }
    }

    private void initScrollLock(final ImageButton buttonScroll) {
        // Only for avoid code duplication
        // set to true because we swap it in manual click call
        mOnScrollMode = true;

        buttonScroll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mOnScrollMode = mOnScrollMode == false ? true : false;
                updateScrollViews();
                buttonScroll.setSelected(!mOnScrollMode);
                // buttonScroll.setEnabled(!mOnScrollMode);
            }
        });
        
        // manual click call to disable swap scroll
        buttonScroll.performClick();
    }

    private void updateScrollViews() {
        final ScrollViewLockable scrollVertical = (ScrollViewLockable) findViewById(R.id.scrollview_codelField_vertical);
        final HorizontalScrollViewLockable scrollHorizontal = (HorizontalScrollViewLockable) findViewById(R.id.scrollview_codelField_horizontal);
        scrollVertical.setScrollingEnabled(mOnScrollMode);
        scrollHorizontal.setScrollingEnabled(mOnScrollMode);
    }

    private void initFragments() {
        mFragmentStateInfo = (FragmentStateInfo) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_state_info);

        mFragmentCommandLog = (FragmentCommandLog) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_command_log);
    }

    private void initDefaultState(Resources resources) {
        int countX = resources.getInteger(R.integer.field_count_codels_x);
        int countY = resources.getInteger(R.integer.field_count_codels_y);

        initNewPietFile(countX, countY);
    }

    private void initRestoredState(Bundle savedInstanceState) {
        initNewPietFile(savedInstanceState);
    }

    private void initPiet(Resources resources) {
        final EditText inText = (EditText) findViewById(R.id.text_view_in);
        final TextView outText = (TextView) findViewById(R.id.text_view_out);

        InOutSystem inOutSystem = new InOutSystemEditText(inText, outText);

        LoggerDroid logger = new LoggerDroid();

        mPiet = new Piet(logger, inOutSystem);
    }
    
    public boolean isOnScrollMode() {
        return mOnScrollMode;
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
                        if (isOnScrollMode()) {
                            return false;
                        }
                        
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

        mCurrentFile = new PietFile(mColorField, mPiet, this);

        PietFileActor actor = mCurrentFile.getActor();
        actor.resize(countX, countY);
        actor.invalidateView();

        mCurrentFile.getRunner().addExecutionListener(mRunListener);
    }

    private void initNewPietFile(Bundle savedInstanceState) {
        if (mCurrentFile != null) {
            mCurrentFile.finalise();
        }
        mCurrentFile = new PietFile(mColorField, mPiet, this);

        PietFileActor actor = mCurrentFile.getActor();
        actor.restoreFromSavedState(savedInstanceState);
        mCurrentFile.getRunner().addExecutionListener(mRunListener);
    }

    private void initHelpersTabHost() {

        Resources resources = getResources();
        // final View tabsInclude = findViewById(R.id.tablayout);
        final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
       
        HelperTabHost
                .create(tabs)
                .setActiveTabColor(
                        resources.getColor(R.color.info_widget_tabhost_active_tab_color))
                .setPassiveTabColor(
                        resources.getColor(R.color.info_widget_tabhost_passive_tab_color))
                .setTabHeight(
                        resources
                                .getDimensionPixelSize(R.dimen.info_widget_tabhost_tab_height))
                .setTextColor(
                        resources.getColor(R.color.info_widget_tabhost_tab_text_color))
                .setTextSize(
                        resources.getDimensionPixelSize(R.dimen.info_widget_tab_text_size))
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
    }

    // Called to save UI state changes at the
    // end of the active lifecycle.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate and
        // onRestoreInstanceState if the process is
        // killed and restarted by the run time.

        super.onSaveInstanceState(savedInstanceState);
        if (hasPietFile() == false) {
            return;
        }

        getCurrentPietFile().getActor().saveInstanceState(savedInstanceState);
    }

    // Called at the end of the active lifetime.
    @Override
    public void onPause() {
        if (isOnRunMode()) {
            stopRun();
        }

        // Suspend UI updates, threads, or CPU intensive processes
        // that don’t need to be updated when the Activity isn’t
        // the active foreground Activity.
        super.onPause();
    }

    // Called at the end of the visible lifetime.
    @Override
    public void onStop() {
        if (isOnRunMode()) {
            stopRun();
        }
        
        saveToSharedPreferences();
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
        
        updateOptionsMenu();
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
        case (R.id.action_hide_tabhost):
            onActionHideTabHost(item.isChecked());
            item.setChecked(!item.isChecked());
            return true;

        case (R.id.action_load):
            doActionIfUserDontWantToSaveChanges(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    onActionLoad();
                    return null;
                }

            });

            return true;

        case (R.id.action_clear):
            doActionIfUserDontWantToSaveChanges(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    onActionClear();
                    return null;
                }

            });
            return true;

        case (R.id.action_new):
            doActionIfUserDontWantToSaveChanges(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    onActionNew();
                    return null;
                }

            });
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

    public void doActionIfUserDontWantToSaveChanges(
            final Callable<Void> callable) {
        if (mCurrentFile.isTouched() == false) {
            try {
                callable.call();
            } catch (Exception e) {
                Log.e("", e.toString());
            }
        } else {
            DialogFragmentSaveChanges dialog = new DialogFragmentSaveChanges();

            dialog.setOnAcceptListener(new DialogFragmentSaveChanges.OnAcceptListener() {
                @Override
                public void onAccept() {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        Log.e("", e.toString());
                    }

                }
            });

            dialog.show(getSupportFragmentManager(),
                    "DialogFragmentSaveChanges");
        }
    }

    //toggle info widget visibility 
    private void onActionHideTabHost(boolean checked) {
        updateInfoWidgetVisibility(!checked);
    }
    
    private void updateInfoWidgetVisibility(boolean isVisible) {
        final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
        if (isVisible) {
            tabs.setVisibility(View.VISIBLE);
        } else {
            tabs.setVisibility(View.GONE);
        }
    }
    
    private void onActionNew() {
        DialogFragmentNewFileSettings dialog = new DialogFragmentNewFileSettings();
        if (hasPietFile() == true) {
            int width = getCurrentPietFile().getWidth();
            int height = getCurrentPietFile().getHeight();
            dialog.setBitmapDimensions(width, height);
        }

        dialog.setListener(new DialogFragmentNewFileSettings.Listener() {

            @Override
            public void onCancelNewFileSettings() {
            }

            @Override
            public boolean onAcceptNewFileSettings(int width, int height) {
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
        Intent intent = new Intent( this, Preferences.class );
        //intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, PietPreferenceFragment.class.getName() );
        intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
        startActivityForResult(intent, SHOW_PREFERENCES);
    }

    private void onActionSave() {
        if (getCurrentPietFile().hasPath() == false) {
            onActionSaveAs();
            return;
        }
        // LOCK
        getCurrentPietFile().getActor().saveAsync();
    }

    private void onActionSaveAs() {
        // LOCK
        if (isOnRunMode()) {
            mInteractionListener.onInteractionStop();
        }

        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.loadFolder(Environment.getExternalStorageDirectory().getPath());
        dialog.setFilter(".*jpg|.*jpeg|.*png|.*gif|.*JPG|.*JPEG|.*PNG|.*GIF|");

        dialog.setCanCreateFiles(true);
        dialog.setFolderMode(false);
        dialog.setShowConfirmation(true, false);

        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                getCurrentPietFile().getActor().saveAsync(
                        file.getAbsolutePath());
                source.dismiss();
            }

            public void onFileSelected(Dialog source, File folder, String name) {
                String path = folder.getAbsolutePath() + "/" + name;
                getCurrentPietFile().getActor().saveAsync(path);
                source.dismiss();
            }
        });

        dialog.show();
    }

    private void onActionLoad() {
        // LOCK
        if (isOnRunMode()) {
            mInteractionListener.onInteractionStop();
        }

        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.loadFolder(Environment.getExternalStorageDirectory().getPath());
        dialog.setFilter(".*jpg|.*jpeg|.*png|.*gif|.*JPG|.*JPEG|.*PNG|.*GIF|");
        dialog.setCanCreateFiles(false);
        dialog.setFolderMode(false);

        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                // TODO CHECK ERROR!!!!!
                source.dismiss();
                getCurrentPietFile().getActor().load(file.getAbsolutePath());
            }

            public void onFileSelected(Dialog source, File folder, String name) {
            }
        });

        dialog.show();
    }

    public void onActionClear() {
        if (isOnRunMode()) {
            mInteractionListener.onInteractionStop();
        }

        mControlToolBoxView.setControlsToDefaultState();
        getCurrentPietFile().getActor().clear();
        if(getCurrentPietFile().hasPath() == false) {
            getCurrentPietFile().untouch();
        }
    }

    public synchronized void onActivityResult(final int requestCode,
            int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != SHOW_PREFERENCES) {
            return;
        }

        updateFromPreferences();
    }
    
    private void saveToSharedPreferences() {
        Context context = getApplicationContext();
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        
        int width = mColorField.getCellWidth();
        editor.putInt("cell_side", width);
        
        final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
        boolean visibility = tabs.getVisibility() == View.VISIBLE;
        editor.putBoolean("info_widget_visibility", visibility);
        
        editor.commit();
    }
    private void updateOptionsMenu() {
        MenuItem infoWidgetVisibilityItem = mMenu.findItem(R.id.action_hide_tabhost);
        final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
        boolean infoWidgetVisibility = tabs.getVisibility() == View.VISIBLE;
        infoWidgetVisibilityItem.setChecked(infoWidgetVisibility);
    }
    private void updateFromPreferences() {
        
        Context context = getApplicationContext();
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        mSleepBetweenStep = Long.valueOf(preferences.getString(
                "delay_before_step", "0"));

        int cellPadding = Integer.valueOf(preferences.getString("cell_padding",
                "0"));

        getCurrentPietFile().getView().setCellPadding(cellPadding);

        int cellSide = preferences.getInt("cell_side", ColorFieldView.MIN_CELL_SIDE);
        getCurrentPietFile().getView().setCellSide(cellSide);
        
       
        boolean infoWidgetVisibility = preferences.getBoolean("info_widget_visibility", true);
        updateInfoWidgetVisibility(infoWidgetVisibility);
        
        
        if (isOnRunMode() == true) {
            getCurrentPietFile().getRunner().setStepDelay(mSleepBetweenStep);
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
        return mCurrentFile.getRunner().isOnRunMode();
    }

    @Override
    public void onChooseColor(int color) {
        mActiveColor = color;
    }

    public void stopRun() {
        getCurrentPietFile().getRunner().stop();
    }

    @Override
    public Piet getPiet() {
        return mPiet;
    }
}
