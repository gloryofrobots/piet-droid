package com.example.piet_droid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.concurrent.Callable;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;
import ar.com.daidalos.afiledialog.FileChooserLabels;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.jpiet.Codel;

import com.example.jpiet.Piet;
import com.example.piet_droid.codel_table_view.CodelTableViewUpdater;
import com.example.piet_droid.codel_table_view.CodelTableViewUpdaterPool;
import com.example.piet_droid.fragment.DialogFragmentNewFileSettings;
import com.example.piet_droid.fragment.DialogFragmentSaveChanges;
import com.example.piet_droid.fragment.FragmentCommandLog;
import com.example.piet_droid.fragment.FragmentPaletteSimple;
import com.example.piet_droid.fragment.FragmentStateInfo;
import com.example.piet_droid.widget.AccordionTabHost;
import com.example.piet_droid.widget.ColorFieldView;
import com.example.piet_droid.widget.ColorIndicatorView;
import com.example.piet_droid.widget.ControlToolboxView;
import com.example.piet_droid.widget.TabHostBuilder;
import com.example.piet_droid.widget.HorizontalScrollViewLockable;
import com.example.piet_droid.widget.ScrollViewLockable;
import com.example.piet_droid.widget.ZoomView;

public class MainActivity extends SherlockFragmentActivity implements
        FragmentPaletteSimple.OnChooseColorListener, PietProvider {

    // //////////////////////////////////////////////////////////////////////////
    private class RunListener implements PietFileRunner.RunEventListener {
        CodelTableViewUpdater mUpdater = null;
        CodelTableViewUpdaterPool mUpdaterPool = null;

        RunListener(Resources resources) {
            mUpdaterPool = new CodelTableViewUpdaterPool(mColorField, resources);
        }

        public void setGuiTraceMode(GuiTraceMode mode) {
            mUpdater = mUpdaterPool.getUpdater(mode);
        }

        @Override
        public void onRunStart() {
            getPiet().init();
            mFragmentStateInfo.init();

            getPiet().getInOutSystem().prepare();

            mUpdater.start();
        }

        private boolean check() {
            if (getPietFile() == null || getPietFile().isValid() == false) {
                return false;
            }
            return true;
        }

        @Override
        public void onRunCancel() {
            if (!check()) {
                return;
            }

            mControlToolBoxView.setControlsToDefaultState();

            mUpdater.cancel();
            // mFragmentStateInfo.init();
        }

        @Override
        public void onRunUpdate(Codel codel) {
            mFragmentStateInfo.update();
            mPiet.getInOutSystem().flush();
            mFragmentCommandLog.update();

            mUpdater.update(codel);
        }

        @Override
        public void onRunComplete() {
            if (!check()) {
                return;
            }

            mControlToolBoxView.setControlsToDefaultState();

            mUpdater.complete();
        }

        @Override
        public void onRunError() {
            mUpdater.cancel();

            mControlToolBoxView.setControlsToDefaultState();
            showMessageFromResource(R.string.runtime_internal_error);
        }
    }

    // //////////////////////////////////////////////////////////////////////////
    public class LoadListener implements PietFileLoader.LoadListener {

        private boolean mIsTemporary;

        LoadListener(boolean isTemporary) {
            mIsTemporary = isTemporary;
        }

        @Override
        public void onComplete() {
            getPietFile().setTemporary(mIsTemporary);
            updateFileTagView();
        }

        @Override
        public void onError(int errorStringId) {
            String message = getStringFromResource(errorStringId);
            showMessage(message);
        }
    }

    // //////////////////////////////////////////////////////////////////////////
    public class SaveListener implements PietFileSaver.SaveListener {

        @Override
        public void onComplete() {
            updateFileTagView();
        }

        @Override
        public void onError(int errorStringId) {
            String message = getStringFromResource(errorStringId);
            showMessage(message);
        }
    }

    // //////////////////////////////////////////////////////////////////////////
    class InteractionListener implements ControlToolboxView.InteractionListener {
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

    private static final String SHARED_PREFERENCES_GUI_TRACE_MODE = "gui_trace_mode";
    private static final String SHARED_PREFERENCES_KEY_CELL_SIDE = "cell_side";
    private static final String SHARED_PREFERENCES_KEY_INFO_WIDGET_VISIBILITY = "info_widget_visibility";
    private static final String SHARED_PREFERENCES_KEY_LAST_FILENAME = "last_filename";
    private static final String SHARED_PREFERENCES_CELL_PADDING = "cell_padding";
    private static final String SHARED_PREFERENCES_DELAY_BEFORE_STEP = "delay_before_step";
    private static final String TEMPORARY_FILENAME = ".pietdroid_tmp.png";
    private static final String SHARED_PREFERENCES_KEY_IS_TEMPORARY = "is_temporary";
    private static final String SHARED_PREFERENCES_ZOOM_STEP = "zoom_step";
    private static final String SHARED_PREFERENCES_LOGGING_ENABLE = "logging_enable";

    private static final String PIET_FILE_TAG_UNTITLED = "untitled";

    private final String LOG_TAG = "PietDroidMainActivity";

    private static final int LOG_TAB_INDEX = 4;

    private PietFile mCurrentFile;
    private Piet mPiet;
    private ColorFieldView mColorField;
    private int mActiveColor;

    private long mSleepBetweenStep;

    private FragmentStateInfo mFragmentStateInfo;
    private ControlToolboxView mControlToolBoxView;
    private FragmentCommandLog mFragmentCommandLog;

    private int mZoomChangeValue;

    private boolean mOnScrollMode;

    private String mDataFolderName;

    private GuiTraceMode mGuiTraceMode;

    private SaveListener mSaveListener;
    private InteractionListener mInteractionListener;
    private RunListener mRunListener;

    // Toasts
    Handler mToastDelayHandler = new Handler();
    Toast mMessageToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActiveColor = 0;
        mCurrentFile = null;

        Resources resources = getResources();

        initSdCardDirectory();
        initFragments();

        initPiet(resources);
        initColorField();

        if (savedInstanceState != null) {
            initRestoredState(savedInstanceState);
        } else {
            initStartState(resources);
        }

        if (mCurrentFile == null) {
            abortApplication(getStringFromResource(R.string.abort_application_image_initialise_error));
            return;
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        initTabHost();
        initListeners(resources);
        initActionBar();
        initControlToolbox();
        initScrollLock();
        initZoomView();

        updateCurrentColorIndicator();
        updateFromPreferences();
        updateFileTagView();
    }

    private void updateCurrentColorIndicator() {
        ActionBar actionBar = getSupportActionBar();
        ColorIndicatorView indicator = (ColorIndicatorView) actionBar
                .getCustomView().findViewById(R.id.current_color_indicator);
        indicator.setColor(mActiveColor);
    }

    private void updateFileTagView() {
        PietFile pietFile = getPietFile();
        String tag = null;
        String filePath = pietFile.getPath();
        if (pietFile.isTemporary() || filePath == null) {
            tag = PIET_FILE_TAG_UNTITLED;
        } else {
            String[] parts = filePath.split(File.separator);
            // if relative path
            if (parts.length == 0) {
                tag = filePath;
            } else {
                tag = parts[parts.length - 1];
            }
        }

        ActionBar actionBar = getSupportActionBar();
        TextView textView = (TextView) actionBar.getCustomView().findViewById(
                R.id.text_view_file_tag);

        textView.setText(tag);
    }

    private void abortApplication(String format, Object... args) {
        String msg = String.format(format, args);
        Log.e(LOG_TAG, msg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.button_text_ok, null)
                .setTitle(R.string.title_error)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.button_text_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        }).show();
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

    private void initListeners(Resources resources) {
        mRunListener = new RunListener(resources);
        getRunner().addExecutionListener(mRunListener);

        mSaveListener = new SaveListener();
        mInteractionListener = new InteractionListener();
    }

    ZoomView mZoomView;

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.action_bar_custom);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        // actionBar.setDisplayShowTitleEnabled(true);
        // actionBar.setDisplayShowCustomEnabled(true);
        // FORCE SET COLOR BACKGROUND
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.action_bar_background)));

    }

    private void initControlToolbox() {
        mControlToolBoxView = (ControlToolboxView) findViewById(R.id.fragment_control_toolbox);

        mControlToolBoxView.setInteractionListener(mInteractionListener);
    }

    private void initZoomView() {
        mZoomView = (ZoomView) findViewById(R.id.zoom_view_tools);

        mZoomView.setListener(new ZoomView.ZoomListener() {
            @Override
            public void onChangeZoom(int count) {
                int side = (mZoomChangeValue * count);
                mColorField.setCellSide(side);
            }
        });
    }

    private void updateZoomView() {
        int maxCellSide = mColorField.getMaxCellSide();
        int limit = (int) Math.floor(maxCellSide / mZoomChangeValue);

        int width = mColorField.getCellWidth();

        int current = width / mZoomChangeValue;
        mZoomView.set(current, limit);
    }

    private void initScrollLock() {
        final ImageButton buttonScroll = (ImageButton) findViewById(R.id.button_scroll_toggle_tools);

        mOnScrollMode = false;
        updateScrollViews();
        buttonScroll.setSelected(!mOnScrollMode);

        buttonScroll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mOnScrollMode = mOnScrollMode == false ? true : false;
                updateScrollViews();
                buttonScroll.setSelected(!mOnScrollMode);
            }
        });
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
            initNewPietFileFromTemporary(resources);
            return;
        }

        String lastFile = preferences.getString(
                SHARED_PREFERENCES_KEY_LAST_FILENAME, null);
        // Load default empty board
        if (lastFile == null) {
            initNewEmptyPietFile(resources);
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
        Piet.setLogger(new LoggerDroid());

        final EditText inText = (EditText) findViewById(R.id.text_view_in);
        final TextView outText = (TextView) findViewById(R.id.text_view_out);

        mPiet = Piet
                .createPiet(new InOutSystemEditText(inText, outText), false);
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
                            showMessage(getStringFromResource(R.string.runtime_edit_mode_lock_warning));
                            return false;
                        }

                        return true;
                    }

                    @Override
                    public boolean isNeedToHandleMoveAsClick() {
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

    private void initNewPietFileFromTemporary(Resources resources) {
        FileInputStream input = null;
        try {
            input = openFileInput(TEMPORARY_FILENAME);
            getPietFile().getLoader().loadAsync(input, new LoadListener(true));
            // we can close file stream because it have decoded already
            input.close();

        } catch (FileNotFoundException e) {
            initNewEmptyPietFile(resources);
        } catch (IOException e) {
            initNewEmptyPietFile(resources);
        }
    }

    private void initNewPietFile(String fileName) {
        loadPietFileAsyncWithPath(fileName);
    }

    private void initNewEmptyPietFile(Resources resources) {
        int countX = resources.getInteger(R.integer.field_count_codels_x);
        int countY = resources.getInteger(R.integer.field_count_codels_y);

        initNewPietFile(countX, countY);
    }

    private void initNewPietFile(int width, int height) {
        PietFileActor actor = getActor();
        actor.resize(width, height);
        actor.invalidateView();

        getPietFile().setTemporary(true);
    }

    private void initNewPietFile(Bundle savedInstanceState) {
        PietFileActor actor = getPietFile().getActor();
        actor.restoreFromSavedState(savedInstanceState);
    }

    private void initTabHost() {
        Resources resources = getResources();
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
        if (isOnRunMode()) {
            stopRun();
        }
        // Clean up any resources including ending threads,
        // closing database connections etc.
        getPietFile().finalise();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (getPietFile().isTouched() == false) {
            finish();
            return;
        }
        if (getPietFile().isTemporary() == true) {
            MainActivity.this.saveTemporaryFile();
            MainActivity.this.finish();
            return;
        }

        showSaveChangesDialog(new DialogFragmentSaveChanges.OnAcceptListener() {
            @Override
            public void onAccept() {
                MainActivity.this.finish();
            }
        });
    }

    private void saveTemporaryFile() {
        FileOutputStream out;
        try {
            out = openFileOutput(TEMPORARY_FILENAME, MODE_PRIVATE);
            getPietFile().getSaver().save(out);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.toString());
        }
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
        updateCurrentColorIndicator();
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
        case (R.id.action_show_help):
            onActionShowHelp();
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

    private void onActionShowHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void doActionIfUserDontWantToSaveChanges(
            final Callable<Void> callable) {
        if (mCurrentFile.isTouched() == false) {
            try {
                callable.call();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
                abortApplication(getStringFromResource(R.string.abort_application_fatal_error));
            }
        } else {
            showSaveChangesDialog(new DialogFragmentSaveChanges.OnAcceptListener() {
                @Override
                public void onAccept() {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.toString());
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
                updateFileTagView();
                return true;
            }
        });

        dialog.show(getSupportFragmentManager(),
                "DialogFragmentNewFileSettings");
    }

    private void onActionSettings() {
        Intent intent = new Intent(this, Preferences.class);
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        startActivityForResult(intent, SHOW_PREFERENCES);
    }

    private void onActionSave() {
        // save temporary flag and reset it
        if (getPietFile().isTemporary()) {
            onActionSaveAs();
            return;
        }
        // R.string.runtime_bitmap_saved
        getPietFile().getSaver().saveAsync(mSaveListener);
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
                savePietFileAsyncWithPath(file.getAbsolutePath());
                source.dismiss();
            }

            public void onFileSelected(Dialog source, File folder, String name) {
                String path = folder.getAbsolutePath() + File.separator + name;
                savePietFileAsyncWithPath(path);
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
                source.dismiss();
                loadPietFileAsyncWithPath(file.getAbsolutePath());
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

        // write path to current file if it not temporary
        boolean isTemporaryFile = getPietFile().isTemporary();
        if (isTemporaryFile == false && getPietFile().hasPath() == true) {
            editor.putString(SHARED_PREFERENCES_KEY_LAST_FILENAME,
                    getPietFile().getPath());
        }

        editor.putBoolean(SHARED_PREFERENCES_KEY_IS_TEMPORARY, isTemporaryFile);

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
        // STEP DELAY
        try {
            mSleepBetweenStep = Long.valueOf(preferences.getString(
                    SHARED_PREFERENCES_DELAY_BEFORE_STEP, "0"));
        } catch (NumberFormatException e) {
            mSleepBetweenStep = 0;
            showMessageFromResource(R.string.warning_parsing_step_delay);
        }

        // CELL PADDING
        int cellPadding = 0;
        try {
            cellPadding = Integer.valueOf(preferences.getString(
                    SHARED_PREFERENCES_CELL_PADDING, "0"));
        } catch (NumberFormatException e) {
            cellPadding = 0;
            showMessageFromResource(R.string.warning_parsing_cell_padding);
        }

        getPietFile().getView().setCellPadding(cellPadding);

        // GUI TRACE MODE
        try {
            mGuiTraceMode = GuiTraceMode.valueOf(preferences.getString(
                    SHARED_PREFERENCES_GUI_TRACE_MODE, "All"));
        } catch (IllegalArgumentException e) {
            mGuiTraceMode = GuiTraceMode.All;
        }

        mRunListener.setGuiTraceMode(mGuiTraceMode);

        // LOGGING
        boolean logging_enable = preferences.getBoolean(
                SHARED_PREFERENCES_LOGGING_ENABLE, true);
        updateLogging(logging_enable);

        // CELL SIDE
        int cellSide = preferences.getInt(SHARED_PREFERENCES_KEY_CELL_SIDE,
                mColorField.getMinCellSide());
        getPietFile().getView().setCellSide(cellSide);

        // INFO WIDGET
        boolean infoWidgetVisibility = preferences.getBoolean(
                SHARED_PREFERENCES_KEY_INFO_WIDGET_VISIBILITY, true);
        updateInfoWidgetVisibility(infoWidgetVisibility);

        // ZOOM STEP. IT MUST LOADS AFTER LOADING CELL SIDE. IT DEPENDS ON IT
        try {
            mZoomChangeValue = Integer.parseInt(preferences.getString(
                    SHARED_PREFERENCES_ZOOM_STEP, "2"));
        } catch (NumberFormatException e) {
            mZoomChangeValue = 2;
            showMessageFromResource(R.string.warning_parsing_zoom_step);
        }

        updateZoomView();
    }

    private void updateLogging(boolean logging_enable) {
        final AccordionTabHost tabs = (AccordionTabHost) findViewById(android.R.id.tabhost);
        int resultTab = tabs.getCurrentTab();
        if (resultTab == LOG_TAB_INDEX) {
            resultTab = 0;
        }

        int visibility;

        if (logging_enable == true) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }

        tabs.getTabWidget().getChildTabViewAt(LOG_TAB_INDEX)
                .setVisibility(visibility);
        mFragmentCommandLog.enableLogging(logging_enable);
        tabs.setCurrentTabForce(resultTab);
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

    private void showMessage(String msg) {
        if (mMessageToast != null) {
            return;
        }

        mMessageToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mMessageToast.show();

        mToastDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMessageToast = null;
            }
        }, 2000);

    }

    private void showMessageFromResource(int id) {
        String msg = getStringFromResource(id);
        showMessage(msg);
    }

    private void loadPietFileAsyncWithPath(String path) {
        getPietFile().getLoader().loadAsync(path, new LoadListener(false));
    }

    private void savePietFileAsyncWithPath(String path) {
        getPietFile().getSaver().saveAsync(path, mSaveListener);
    }
}
