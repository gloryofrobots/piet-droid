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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import ar.com.daidalos.afiledialog.FileChooserLabels;

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
import com.example.piet_droid.widget.TabHostBuilder;
import com.example.piet_droid.widget.HorizontalScrollViewLockable;
import com.example.piet_droid.widget.ScrollViewLockable;

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
            PietFileActor actor = getActor();
            actor.clearViewDrawables();
            actor.setCellDrawable(0, 0, mCurrentCellDrawable);
            getPiet().getInOutSystem().prepare();
        }

        @Override
        public void onRunCancel() {
            PietFileActor actor = getActor();
            actor.clearViewDrawables();
            mControlToolBoxView.setControlsToDefaultState();
            // mFragmentStateInfo.init();
        }

        @Override
        public void onRunUpdate(Codel codel) {
            mFragmentStateInfo.update();
            mPiet.getInOutSystem().flush();
            mFragmentCommandLog.update();

            PietFileActor actor = getActor();
            actor.setCellDrawable(mPreviousCodel.x, mPreviousCodel.y,
                    mPreviousCellDrawable);

            actor.setCellDrawable(codel.x, codel.y, mCurrentCellDrawable);
            mPreviousCodel.set(codel);
        }

        @Override
        public void onRunComplete() {
            PietFileActor actor = getActor();
            actor.setCellDrawable(mPreviousCodel.x, mPreviousCodel.y,
                    mPreviousCellDrawable);
            mControlToolBoxView.setControlsToDefaultState();
        }

        @Override
        public void onRunError() {
            PietFileActor actor = getActor();
            actor.clearViewDrawables();
            mControlToolBoxView.setControlsToDefaultState();
            String message = getResources().getString(
                    R.string.runtime_internal_error);
            getActor().showMessage(message);
        }
    }

    // //////////////////////////////////////////////////////////////////////////
    private ControlToolboxView.InteractionListener mInteractionListener = new ControlToolboxView.InteractionListener() {

        @Override
        public void onInteractionRun() {
            getRunner().run(mSleepBetweenStep);
        }

        @Override
        public void onInteractionStep() {
            getRunner().step(mSleepBetweenStep);
        }

        @Override
        public void onInteractionPause() {
            if (isOnRunMode() == false) {
                return;
            }
            getRunner().pause();
        }

        @Override
        public void onInteractionStop() {
            if (isOnRunMode() == false) {
                return;
            }
            stopRun();
        }
    };

    // ////////////////////////////////////////////////////////////////////////

    private static final int SHOW_PREFERENCES = 1;

    private final String SHARED_PREFERENCES_KEY_CELL_SIDE = "cell_side";
    private final String SHARED_PREFERENCES_KEY_INFO_WIDGET_VISIBILITY = "info_widget_visibility";
    private final String SHARED_PREFERENCES_KEY_LAST_FILENAME = "last_filename";
    private final String SHARED_PREFERENCES_CELL_PADDING = "cell_padding";
    private final String SHARED_PREFERENCES_DELAY_BEFORE_STEP = "delay_before_step";
    private final String TEMPORARY_FILENAME = ".pietdroid_tmp.png";
    private final String SHARED_PREFERENCES_KEY_IS_TEMPORARY = "is_temporary";

    private PietFile mCurrentFile;
    private Piet mPiet;
    private ColorFieldView mColorField;
    private int mActiveColor;
    private RunListener mRunListener;
    private long mSleepBetweenStep;

    private FragmentStateInfo mFragmentStateInfo;
    private ControlToolboxView mControlToolBoxView;
    private FragmentCommandLog mFragmentCommandLog;

    // TODO Move to Settings
    private int mZoomChangeValue = 2;

    private boolean mOnScrollMode;

    private String mDataFolderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActiveColor = 0;

        Resources resources = getResources();

        initSdCardDirectory();

        initRunListener(resources);
        initFragments();

        initPiet(resources);
        mCurrentFile = null;
        initColorField();

        if (savedInstanceState != null) {
            initRestoredState(savedInstanceState);
        } else {
            initStartState(resources);
        }

        initTabHost();
        initActionBarAndScrollLock();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        updateFromPreferences();
        // getActor().lockOrientation();
    }

    private void abortApplication(String format, Object... args) {
        String msg = String.format(format, args);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", null).setTitle("Error").setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                }).show();
        return;

    }

    private void initSdCardDirectory() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED) == false) {

            abortApplication(
                    getStringFromResource(R.string.abort_application_not_mounted),
                    mDataFolderName);
        }

        mDataFolderName = Environment.getExternalStorageDirectory()
                + File.separator + "." + getString(R.string.app_name);

        File folder = new File(mDataFolderName);

        if (folder.exists() == false) {
            if (folder.mkdir() == false) {
                abortApplication(
                        getStringFromResource(R.string.abort_application_cant_create_dir),
                        mDataFolderName);
            }
        }

        if (folder.isDirectory() == false) {
            abortApplication(
                    getStringFromResource(R.string.abort_application_is_not_dir),
                    mDataFolderName);
        }

        if (folder.canWrite() == false || folder.canRead() == false) {
            abortApplication(
                    getStringFromResource(R.string.abort_application_is_not_writable),
                    mDataFolderName);
        }
    }

    private void initRunListener(Resources resources) {
        mRunListener = new RunListener();
        mRunListener.initDebugDrawables(resources);
    }

    private void initActionBarAndScrollLock() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.action_bar_custom);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // FORCE SET COLOR BACKGROUND
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.action_bar_background)));

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

    private void initZoomButtons(final ImageButton buttonZoomIncrease,
            final ImageButton buttonZoomDecrease) {
        final int minCellSide = mColorField.getMinCellSide();
        final int maxCellSide = mColorField.getMaxCellSide();

        buttonZoomIncrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int width = mColorField.getCellWidth();
                int newSide = width + mZoomChangeValue;

                if (newSide > minCellSide) {
                    buttonZoomDecrease.setEnabled(true);
                }

                if (newSide > maxCellSide) {
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

                if (newSide < maxCellSide) {
                    buttonZoomIncrease.setEnabled(true);
                }

                if (newSide < minCellSide) {
                    buttonZoomDecrease.setEnabled(false);
                    return;
                }

                mColorField.setCellSide(newSide);
            }
        });

        int width = mColorField.getCellWidth();
        if ((width - mZoomChangeValue) <= minCellSide) {
            buttonZoomDecrease.setEnabled(false);
        } else if ((width + mZoomChangeValue) >= maxCellSide) {
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

    private void initStartState(Resources resources) {
        createNewPietFile();

        Context context = getApplicationContext();
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        // Load temporary file
        boolean isTemporary = preferences.getBoolean(
                SHARED_PREFERENCES_KEY_IS_TEMPORARY, false);
        if (isTemporary == true) {
            initNewPietFileFromTemporary();
            return;
        }

        String lastFile = preferences.getString(
                SHARED_PREFERENCES_KEY_LAST_FILENAME, null);
        // Load default empty board
        if (lastFile == null) {
            int countX = resources.getInteger(R.integer.field_count_codels_x);
            int countY = resources.getInteger(R.integer.field_count_codels_y);
            initNewPietFile(countX, countY);
        } else {
            // Load last edited file
            initNewPietFile(lastFile);
        }
    }


    private void initRestoredState(Bundle savedInstanceState) {
        createNewPietFile();
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
                        PietFileActor actor = MainActivity.this.getActor();
                        actor.setCell(x, y, MainActivity.this.getActiveColor());
                        actor.redrawCell(x, y);
                    }

                    @Override
                    public boolean isProcessClickWanted() {
                        if (isOnScrollMode()) {
                            return false;
                        }

                        if (MainActivity.this.isOnRunMode() == true) {
                            getPietFile()
                                    .getActor()
                                    .showMessage(
                                            getStringFromResource(R.string.runtime_edit_mode_lock_warning));
                            return false;
                        }

                        return true;
                    }
                });

    }

    private void createNewPietFile() {
        if (mCurrentFile != null) {
            mCurrentFile.finalise();
        }

        mCurrentFile = new PietFile(mColorField, mPiet, this);
    }
    
  private void initNewPietFileFromTemporary() {
        
      getPietFile().setTemporary(true);
    }
  
    private void initNewPietFile(String fileName) {
        getActor().loadAsync(fileName);
        getRunner().addExecutionListener(mRunListener);
    }

    private void initNewPietFile(int countX, int countY) {
        PietFileActor actor = getActor();
        actor.resize(countX, countY);
        actor.invalidateView();
        getPietFile().setTemporary(true);
        getRunner().addExecutionListener(mRunListener);
    }

    private void initNewPietFile(Bundle savedInstanceState) {
        PietFileActor actor = mCurrentFile.getActor();
        actor.restoreFromSavedState(savedInstanceState);
        getRunner().addExecutionListener(mRunListener);
    }

    private void initTabHost() {
        Resources resources = getResources();
        // final View tabsInclude = findViewById(R.id.tablayout);
        final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);

        TabHostBuilder
                .create(tabs)
                .setActiveTabColor(
                        resources
                                .getColor(R.color.info_widget_tabhost_active_tab_color))
                .setPassiveTabColor(
                        resources
                                .getColor(R.color.info_widget_tabhost_passive_tab_color))
                .setTabHeight(
                        resources
                                .getDimensionPixelSize(R.dimen.info_widget_tabhost_tab_height))
                .setTabWidth(
                        resources
                                .getDimensionPixelSize(R.dimen.info_widget_tabhost_tab_width))
                .setTextColor(
                        resources
                                .getColor(R.color.info_widget_tabhost_tab_text_color))
                .setTextSize(
                        resources
                                .getDimensionPixelSize(R.dimen.info_widget_tab_text_size))
                .addTab(R.id.tabInput, "input",
                        resources.getString(R.string.tab_in))
                .addTab(R.id.tabOutput, "output",
                        resources.getString(R.string.tab_out))
                .addTab(R.id.tabState, "state",
                        resources.getString(R.string.tab_state))
                .addTab(R.id.tabLog, "log",
                        resources.getString(R.string.tab_log)).build(2);
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

        getActor().saveInstanceState(savedInstanceState);
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

    @Override
    public void onBackPressed() {
        if (getPietFile().isTouched() == false) {
            finish();
            return;
        }

        showSaveChangesDialog(new DialogFragmentSaveChanges.OnAcceptListener() {
            @Override
            public void onAccept() {
                if (getPietFile().isTemporary()) {
                    MainActivity.this.saveTemporaryFile();
                }

                MainActivity.this.finish();
            }
        });
    }

    private void saveTemporaryFile() {
        getActor().save(mTemporaryFileName);
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

    @Override
    public void onChooseColor(int color) {
        mActiveColor = color;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
        case (R.id.action_hide_tabhost):
            onActionHideTabHost(item.isChecked());
            if (item.isChecked()) {
                item.setTitle(R.string.action_show_tabhost);
            } else {
                item.setTitle(R.string.action_hide_tabhost);
            }
            item.setChecked(!item.isChecked());
            return true;

        case (R.id.action_load):
            doActionIfUserDontWantToSaveChanges(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    emulateInteractionStopIfOnRun();
                    onActionLoad();
                    return null;
                }

            });

            return true;

        case (R.id.action_clear):
            doActionIfUserDontWantToSaveChanges(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    emulateInteractionStopIfOnRun();
                    onActionClear();
                    return null;
                }

            });
            return true;

        case (R.id.action_new):
            doActionIfUserDontWantToSaveChanges(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    emulateInteractionStopIfOnRun();
                    onActionNew();
                    return null;
                }

            });
            return true;
        case (R.id.action_save): {
            emulateInteractionStopIfOnRun();
            onActionSave();
            return true;
        }
        case (R.id.action_save_as): {
            emulateInteractionStopIfOnRun();
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

    final private String LOG_LABEL_ACTION = "ACTION";

    public void doActionIfUserDontWantToSaveChanges(
            final Callable<Void> callable) {
        if (mCurrentFile.isTouched() == false) {
            try {
                callable.call();
            } catch (Exception e) {
                Log.e(LOG_LABEL_ACTION, e.toString());
                abortApplication(getStringFromResource(R.string.abort_application_fatal_error));
            }
        } else {
            showSaveChangesDialog(new DialogFragmentSaveChanges.OnAcceptListener() {
                @Override
                public void onAccept() {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        Log.e(LOG_LABEL_ACTION, e.toString());
                        abortApplication(getStringFromResource(R.string.abort_application_fatal_error));
                    }

                }
            });
        }
    }

    private void showSaveChangesDialog(
            DialogFragmentSaveChanges.OnAcceptListener listener) {
        DialogFragmentSaveChanges dialog = new DialogFragmentSaveChanges();

        dialog.setOnAcceptListener(listener);

        dialog.show(getSupportFragmentManager(), "DialogFragmentSaveChanges");
    }

    // toggle info widget visibility
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
            int width = getPietFile().getWidth();
            int height = getPietFile().getHeight();
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
        Intent intent = new Intent(this, Preferences.class);
        // intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT,
        // PietPreferenceFragment.class.getName() );
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        startActivityForResult(intent, SHOW_PREFERENCES);
    }

    private void onActionSave() {
        //save temporary flag and reset it
        if (getPietFile().isTemporary()) {
            onActionSaveAs();
            getPietFile().setTemporary(false);
            return;
        }

        getActor().saveAsync();
    }

    private void emulateInteractionStopIfOnRun() {
        if (isOnRunMode() == false) {
            return;
        }

        mInteractionListener.onInteractionStop();
    }

    private void onActionSaveAs() {
        FileChooserDialog dialog = new FileChooserDialog(this);

        FileChooserLabels labels = new FileChooserLabels();

        labels.createFileDialogMessage = getResources().getString(
                R.string.dialog_save_file_label_new_file);

        dialog.setLabels(labels);

        dialog.loadFolder(mDataFolderName);
        // dialog.setFilter(".*jpg|.*jpeg|.*png|.*gif|.*JPG|.*JPEG|.*PNG|.*GIF|");
        dialog.setCanCreateFiles(true);
        dialog.setFolderMode(false);
        dialog.setShowConfirmation(true, false);
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                getActor().saveAsync(file.getAbsolutePath());
                source.dismiss();
            }

            public void onFileSelected(Dialog source, File folder, String name) {
                String path = folder.getAbsolutePath() + "/" + name;
                getActor().saveAsync(path);
                source.dismiss();
            }
        });

        dialog.show();
    }

    private void onActionLoad() {
        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.loadFolder(mDataFolderName);
        dialog.setCanCreateFiles(false);
        dialog.setFolderMode(false);
        // dialog.setFilter(".*jpg|.*jpeg|.*png|.*gif|.*JPG|.*JPEG|.*PNG|.*GIF|");

        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                // TODO CHECK ERROR!!!!!
                source.dismiss();
                getActor().loadAsync(file.getAbsolutePath());
            }

            public void onFileSelected(Dialog source, File folder, String name) {
            }
        });

        dialog.show();
    }

    private void onActionClear() {
        mControlToolBoxView.setControlsToDefaultState();
        getActor().clear();
        if (getPietFile().hasPath() == false) {
            getPietFile().untouch();
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
        editor.putInt(SHARED_PREFERENCES_KEY_CELL_SIDE, width);

        final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
        boolean visibility = tabs.getVisibility() == View.VISIBLE;
        editor.putBoolean(SHARED_PREFERENCES_KEY_INFO_WIDGET_VISIBILITY,
                visibility);
        
        //write path to current file if it not temporary
        boolean isTemporaryFile = getPietFile().isTemporary();
        if (isTemporaryFile == false && getPietFile().hasPath() == true) {
            editor.putString(SHARED_PREFERENCES_KEY_LAST_FILENAME,
                    getPietFile().getPath());
        } 
        
        editor.putBoolean(SHARED_PREFERENCES_KEY_IS_TEMPORARY,
                isTemporaryFile);
        
        editor.commit();
    }

    private void updateOptionsMenu() {
        MenuItem infoWidgetVisibilityItem = mMenu
                .findItem(R.id.action_hide_tabhost);
        final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
        boolean infoWidgetVisibility = tabs.getVisibility() == View.VISIBLE;
        infoWidgetVisibilityItem.setChecked(infoWidgetVisibility);
    }

    private void updateFromPreferences() {

        Context context = getApplicationContext();
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        try {
            mSleepBetweenStep = Long.valueOf(preferences.getString(
                    SHARED_PREFERENCES_DELAY_BEFORE_STEP, "0"));
        } catch (NumberFormatException e) {
            mSleepBetweenStep = 0;
            showMessage("Error parsing delay before step");
        }

        int cellPadding = 0;
        try {
            cellPadding = Integer.valueOf(preferences.getString(
                    SHARED_PREFERENCES_CELL_PADDING, "0"));
        } catch (NumberFormatException e) {
            cellPadding = 0;
            showMessage("Error parsing cell padding");
        }

        getPietFile().getView().setCellPadding(cellPadding);

        int cellSide = preferences.getInt(SHARED_PREFERENCES_KEY_CELL_SIDE,
                mColorField.getMinCellSide());
        getPietFile().getView().setCellSide(cellSide);

        boolean infoWidgetVisibility = preferences.getBoolean(
                SHARED_PREFERENCES_KEY_INFO_WIDGET_VISIBILITY, true);
        updateInfoWidgetVisibility(infoWidgetVisibility);

        if (isOnRunMode() == true) {
            getRunner().setStepDelay(mSleepBetweenStep);
        }
    }

    private void showMessage(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    private int getActiveColor() {
        return mActiveColor;
    }

    private PietFile getPietFile() {
        return mCurrentFile;
    }

    private PietFileActor getActor() {
        return getPietFile().getActor();
    }

    private PietFileRunner getRunner() {
        return getPietFile().getRunner();
    }

    private boolean hasPietFile() {
        return mCurrentFile != null;
    }

    private boolean isOnRunMode() {
        return getRunner().isOnRunMode();
    }

    public void stopRun() {
        getRunner().stop();
    }

    @Override
    public Piet getPiet() {
        return mPiet;
    }

    public String getStringFromResource(int id) {
        return getResources().getString(id);
    }
}
