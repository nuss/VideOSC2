/*
 * Copyright (c) 2014 Rex St. John on behalf of AirPair.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYEND HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.videosc.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.adapters.ToolsMenuAdapter;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.fragments.VideOSCSelectSnapshotFragment;
import net.videosc.utilities.VideOSCDBHelpers;
import net.videosc.utilities.VideOSCDialogHelper;
import net.videosc.utilities.VideOSCOscHandler;
import net.videosc.utilities.VideOSCUIHelpers;
import net.videosc.utilities.enums.GestureModes;
import net.videosc.utilities.enums.InteractionModes;
import net.videosc.utilities.enums.PixelEditModes;
import net.videosc.utilities.enums.RGBModes;
import net.videosc.utilities.enums.RGBToolbarStatus;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Stefan Nussbaumer on 2017-03-15.
 */
public class VideOSCMainActivity extends FragmentActivity
        implements VideOSCBaseFragment.OnFragmentInteractionListener, View.OnTouchListener {

    static final String TAG = "VideOSCMainActivity";

    private androidx.fragment.app.FragmentManager mFragmentManager;

    public View mCamView;
    public DrawerLayout mToolsDrawerLayout;

    private VideOSCCameraFragment mCameraPreview;
    public androidx.fragment.app.Fragment mMultiSliderView;
    // ID of currently opened camera
    public static int backsideCameraId;
    public static int frontsideCameraId;

    // the global application, used to exchange various temporary data
    private VideOSCApplication mApp;

    // the current gesture mode
    public Enum<GestureModes> mGestureMode = GestureModes.SWAP;

    // ListView for the tools drawer
    private final List<BitmapDrawable> mToolsList = new ArrayList<>();
    private ListView mToolsDrawerList;
    //	public HashMap<Integer, Integer> mToolsDrawerListState = new HashMap<>();
    // toolbar status
    public Enum<RGBToolbarStatus> mColorModeToolsDrawer = RGBToolbarStatus.RGB;

    // pop-out menu for setting color mode
    public ViewGroup mModePanel;
    // panel for displaying frame rate, calculation period
    public ViewGroup mFrameRateCalculationPanel;

    // settings, retrieved from sqlite db
//    public SettingsDBHelper mDbHelper;
    VideOSCDBHelpers mDbHelper;

    Intent starterIntent;
    private static final int CODE_WRITE_SETTINGS_PERMISSION = 111;
    private static final int PERMISSION_REQUEST_CAMERA = 1;

    private View mDecorView;
    public ViewGroup mPixelEditor;
    private int mOldX;
    private int mOldY;
    private float mEditorBoxAlpha;
    public ViewGroup mBasicToolbar;

    public SQLiteDatabase mDb;
    private VideOSCOscHandler mOscHelper;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // immersive fullscreen
        mDecorView = getWindow().getDecorView();

        VideOSCUIHelpers.resetSystemUIState(mDecorView);

        starterIntent = getIntent();
//		requestSettingsPermission();

        mApp = (VideOSCApplication) getApplicationContext();
        mOscHelper = mApp.getOscHelper();

        backsideCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        if (VideOSCUIHelpers.hasFrontsideCamera()) {
            frontsideCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        mApp.setCurrentCameraId(backsideCameraId);

        final float scale = getResources().getDisplayMetrics().density;
        mApp.setScreenDensity(scale);

        // keep db access open through the app's lifetime
        mDbHelper = new VideOSCDBHelpers(this);
        mDb = mDbHelper.getDatabase();

        final int udpPort = mDbHelper.getUdpReceivePort();
        final int tcpPort = mDbHelper.getTcpReceivePort();
        mOscHelper.createListeners(udpPort, tcpPort);

        mDbHelper.getBroadcastClients();

        final SparseArray<String> mappings = mDbHelper.getMappings();
        mApp.setCommandMappings(mappings);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View mainLayout = View.inflate(this, R.layout.activity_main, null);
        setContentView(mainLayout);

        mCamView = mainLayout.findViewById(R.id.camera_preview);

        // check if device is tablet and set app member variable accordingly
        mApp.setIsTablet(getResources().getBoolean(R.bool.isTablet));

        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState != null) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            checkCamera();
            mCameraPreview = new VideOSCCameraFragment(this);
            mFragmentManager.beginTransaction()
                    .replace(R.id.camera_preview, mCameraPreview, "CamPreview")
                    .commit();
            buildUI();
        } else {
            requestCameraPermission();
        }
    }

    private void buildUI() {
        final LayoutInflater inflater = getLayoutInflater();

        // does the device have an inbuilt flashlight? frontside camera? flashlight but no frontside camera
        // frontside camer but no flashlight?...
        int drawerIconsIds = mApp.getHasTorch() ?
                VideOSCUIHelpers.hasFrontsideCamera() ? R.array.drawer_icons : R.array.drawer_icons_no_frontside_cam :
                VideOSCUIHelpers.hasFrontsideCamera() ? R.array.drawer_icons_no_torch : R.array.drawer_icons_no_torch_no_frontside_cam;

        TypedArray tools = getResources().obtainTypedArray(drawerIconsIds);
        mToolsDrawerLayout = findViewById(R.id.drawer_layout);
        mToolsDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mToolsDrawerList = findViewById(R.id.drawer);

        for (int i = 0; i < tools.length(); i++) {
            mToolsList.add((BitmapDrawable) tools.getDrawable(i));
        }

        mToolsDrawerList.setAdapter(new ToolsMenuAdapter(this, R.layout.drawer_item, R.id.tool, mToolsList));
        tools.recycle();

        mModePanel = (ViewGroup) inflater.inflate(R.layout.color_mode_panel, (FrameLayout) mCamView, false);
        mFrameRateCalculationPanel = (ViewGroup) inflater.inflate(R.layout.framerate_calculation_indicator, (FrameLayout) mCamView, false);
        mPixelEditor = (ViewGroup) inflater.inflate(R.layout.pixel_editor_toolbox, (FrameLayout) mCamView, false);
        mPixelEditor.requestDisallowInterceptTouchEvent(true);
        mPixelEditor.setOnTouchListener(this);

        mBasicToolbar = (ViewGroup) inflater.inflate(R.layout.basic_tools_bar, (FrameLayout) mCamView, false);
        VideOSCUIHelpers.addView(mBasicToolbar, (FrameLayout) mCamView);
        long numSnapshots = DatabaseUtils.queryNumEntries(mDb, SettingsContract.PixelSnapshotEntries.TABLE_NAME);
        if (numSnapshots > 0) {
            TextView numSnapshotsIndicator = mBasicToolbar.findViewById(R.id.num_snapshots);
            numSnapshotsIndicator.setActivated(true);
            numSnapshotsIndicator.setText(String.valueOf(numSnapshots));
            numSnapshotsIndicator.setTextColor(0xffffffff);
        }

        mBasicToolbar.requestDisallowInterceptTouchEvent(true);
        mBasicToolbar.setOnTouchListener(this);

        final ImageButton editPixels = mPixelEditor.findViewById(R.id.edit_pixels);
        final ImageButton applyPixelSelection = mPixelEditor.findViewById(R.id.apply_pixel_selection);
        editPixels.setActivated(true);
        applyPixelSelection.setActivated(true);
        applyPixelSelection.setEnabled(true);
        mApp.setPixelEditMode(PixelEditModes.EDIT_PIXELS);
        final ImageButton quickEditPixels = mPixelEditor.findViewById(R.id.quick_edit_pixels);
        final ImageButton deleteEditsInPixels = mPixelEditor.findViewById(R.id.delete_edits);

        final ImageButton oscFeedbackButton = mBasicToolbar.findViewById(R.id.osc_feedback_button);
        final ImageButton loadSnapshotsButton = mBasicToolbar.findViewById(R.id.saved_snapshots_button);
        final ImageButton saveSnapshotButton = mBasicToolbar.findViewById(R.id.save_snapshot);

        quickEditPixels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(true);
                editPixels.setActivated(false);
                deleteEditsInPixels.setActivated(false);
                applyPixelSelection.setActivated(false);
                applyPixelSelection.setEnabled(false);
                mApp.setPixelEditMode(PixelEditModes.QUICK_EDIT_PIXELS);
            }
        });

        editPixels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(true);
                quickEditPixels.setActivated(false);
                deleteEditsInPixels.setActivated(false);
                applyPixelSelection.setActivated(true);
                applyPixelSelection.setEnabled(true);
                mApp.setPixelEditMode(PixelEditModes.EDIT_PIXELS);
            }
        });

        deleteEditsInPixels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(true);
                quickEditPixels.setActivated(false);
                editPixels.setActivated(false);
                applyPixelSelection.setActivated(false);
                applyPixelSelection.setEnabled(false);
                final VideOSCCameraFragment cameraFragment = (VideOSCCameraFragment) mFragmentManager.findFragmentByTag("CamPreview");
                assert cameraFragment != null;
                cameraFragment.getSelectedPixels().clear();
                cameraFragment.getPixelNumbers().clear();
                mApp.setPixelEditMode(PixelEditModes.DELETE_EDITS);
                Log.d(TAG, "delete edits");
            }
        });

        loadSnapshotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MatrixCursor extras = mDbHelper.getSnapshotsMatrixCursor();
                final Cursor cursor = mDbHelper.getSnapshotsCursor();
                final Cursor[] cursors = {cursor, extras};
                final MergeCursor mergedCursor = new MergeCursor(cursors);
                VideOSCSelectSnapshotFragment snapshotSelect = new VideOSCSelectSnapshotFragment(VideOSCMainActivity.this);
                snapshotSelect.setDatabase(mDb);
                snapshotSelect.setCursors(mergedCursor, cursor, extras);
                if (!snapshotSelect.isVisible()) {
                    mFragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                            .add(R.id.camera_preview, snapshotSelect, "snapshot select")
                            .commit();
                }
            }
        });

        saveSnapshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(VideOSCMainActivity.this);
                final ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.snapshot_dialogs, (FrameLayout) mCamView, false);

                // FIXME: Alert Dialogs should have a white backround like other dialogs
				/*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
						new ContextThemeWrapper(
								VideOSCMainActivity.this, R.style.AlertDialogCustom
						)
				);*/
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(VideOSCMainActivity.this);
                dialogBuilder.setView(dialogView);
                final EditText nameInput = dialogView.findViewById(R.id.save_snapshot_name);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date now = new Date();
                String nowString = df.format(now);
                nameInput.setText(nowString);

                dialogBuilder
                        .setCancelable(true)
                        .setPositiveButton(R.string.save_snapshot,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final ContentValues values = new ContentValues();
                                        final VideOSCCameraFragment cameraFragment = (VideOSCCameraFragment) mFragmentManager.findFragmentByTag("CamPreview");
                                        String valuesString;
                                        values.put(SettingsContract.PixelSnapshotEntries.SNAPSHOT_NAME, nameInput.getText().toString());
                                        // red values
                                        assert cameraFragment != null;
                                        valuesString = convertPixelValuesToString(cameraFragment.getRedValues());
                                        values.put(SettingsContract.PixelSnapshotEntries.SNAPSHOT_RED_VALUES, valuesString);
                                        // red mix values
                                        valuesString = convertPixelValuesToString(cameraFragment.getRedMixValues());
                                        values.put(SettingsContract.PixelSnapshotEntries.SNAPSHOT_RED_MIX_VALUES, valuesString);
                                        // green values
                                        valuesString = convertPixelValuesToString(cameraFragment.getGreenValues());
                                        values.put(SettingsContract.PixelSnapshotEntries.SNAPSHOT_GREEN_VALUES, valuesString);
                                        // green mix values
                                        valuesString = convertPixelValuesToString(cameraFragment.getGreenMixValues());
                                        values.put(SettingsContract.PixelSnapshotEntries.SNAPSHOT_GREEN_MIX_VALUES, valuesString);
                                        // blue values
                                        valuesString = convertPixelValuesToString(cameraFragment.getBlueValues());
                                        values.put(SettingsContract.PixelSnapshotEntries.SNAPSHOT_BLUE_VALUES, valuesString);
                                        // blue mix values
                                        valuesString = convertPixelValuesToString(cameraFragment.getBlueMixValues());
                                        values.put(SettingsContract.PixelSnapshotEntries.SNAPSHOT_BLUE_MIX_VALUES, valuesString);
                                        Point resolution = mApp.getResolution();
                                        values.put(SettingsContract.PixelSnapshotEntries.SNAPSHOT_SIZE, resolution.x * resolution.y);
                                        long result = mDb.insert(
                                                SettingsContract.PixelSnapshotEntries.TABLE_NAME,
                                                null,
                                                values
                                        );
                                        if (result > 0) {
                                            long numSnapshots = DatabaseUtils.queryNumEntries(mDb, SettingsContract.PixelSnapshotEntries.TABLE_NAME);
                                            if (numSnapshots > 0) {
                                                TextView numSnapshotsIndicator = mBasicToolbar.findViewById(R.id.num_snapshots);
                                                numSnapshotsIndicator.setActivated(true);
                                                numSnapshotsIndicator.setText(String.valueOf(numSnapshots));
                                                numSnapshotsIndicator.setTextColor(0xffffffff);
                                            }
                                        }
                                        ((FrameLayout) mCamView).removeView(dialogView);
                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((FrameLayout) mCamView).removeView(dialogView);
                                    }
                                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });

//		saveSnapshotButton.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View view) {
//				view.setOnTouchListener(VideOSCMainActivity.this);
//				return true;
//			}
//		});

        oscFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mApp.setOSCFeedbackActivated(!mApp.getOSCFeedbackActivated());
                view.setActivated(mApp.getOSCFeedbackActivated());
                if (mApp.getOSCFeedbackActivated()) {
                    mOscHelper.addOscUdpEventListener();
                    mOscHelper.addOscTcpEventListener();
                } else {
                    mOscHelper.removeOscUdpEventListener();
                    mOscHelper.removeOscTcpEventListener();
                }
            }
        });

        mToolsDrawerLayout.openDrawer(GravityCompat.END);

        final Point dimensions = getAbsoluteScreenSize();
        mApp.setDimensions(dimensions);

        ImageButton menuButton = findViewById(R.id.show_menu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mToolsDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    mToolsDrawerLayout.openDrawer(GravityCompat.END);
                }
                closeColorModePanel();
            }
        });

        int indicatorXMLiD = mApp.getHasTorch() ? R.layout.indicator_panel : R.layout.indicator_panel_no_torch;
        inflater.inflate(indicatorXMLiD, (FrameLayout) mCamView, true);
    }

    public void closeColorModePanel() {
        if (mApp.getIsColorModePanelOpen())
            mApp.setIsColorModePanelOpen(VideOSCUIHelpers.removeView(mModePanel, (FrameLayout) mCamView));
    }

    private String convertPixelValuesToString(@NonNull ArrayList<Double> values) {
        String result = "";
        int size = values.size();
        for (int i = 0; i < size; i++) {
            Double value = values.get(i);
            if (value != null)
                result = result.concat(String.valueOf(value));
            if ((i + 1) < size)
                result = result.concat(",");
        }

        return result;
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     * can be used by any moveable element if the activity is present:
     * element.setOnTouchListener(activity)
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        final int x = (int) event.getRawX();
        final int y = (int) event.getRawY();
        int deltaX, deltaY;
        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        final int drawerWidth = (int) (50 * mApp.getScreenDensity());
        final int maxTopMargin = mApp.getDimensions().y - v.getHeight();
        final int maxRightMargin = mApp.getDimensions().x - v.getWidth();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mEditorBoxAlpha = v.getAlpha();
                v.setAlpha(0.3f);
                v.bringToFront();
                mOldX = x;
                mOldY = y;
                break;
            case MotionEvent.ACTION_UP:
                v.setAlpha(mEditorBoxAlpha);
                break;
            case MotionEvent.ACTION_MOVE:
                deltaX = x - mOldX;
                deltaY = y - mOldY;
                lp.topMargin = lp.topMargin >= 0 ? lp.topMargin + deltaY : 0;
                if (lp.topMargin >= maxTopMargin) lp.topMargin = maxTopMargin;
                lp.rightMargin = lp.rightMargin >= drawerWidth ?
                        lp.rightMargin - deltaX : drawerWidth;
                if (lp.rightMargin >= maxRightMargin) lp.rightMargin = maxRightMargin;
                v.setLayoutParams(lp);
                mOldX = x;
                mOldY = y;
                break;
        }
        return true;
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }

    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        VideOSCUIHelpers.resetSystemUIState(mDecorView);
    }

    @Override
    public void onBackPressed() {
        Fragment
                settingsContainerFragment, networkSettingsDialog, resolutionSettingsDialog,
                commandMappingsDialog, sensorSettingsDialog, debugSettingsDialog, about;
        final boolean isTablet = mApp.getIsTablet();
        final int settingsContainerID = mApp.getSettingsContainerID();
        final int networkSettingsID = mApp.getNetworkSettingsID();
        final int resolutionSettingsID = mApp.getResolutionSettingsID();
        final int commandMappingsID = mApp.getCommandMappingsID();
        final int sensorSettingsID = mApp.getSensorSettingsID();
        final int debugSettingsID = mApp.getDebugSettingsID();
        final int aboutID = mApp.getAboutSettingsID();
        if (settingsContainerID > 0) {
            settingsContainerFragment = mFragmentManager.findFragmentById(settingsContainerID);
            assert settingsContainerFragment != null;
            final View settingsView = settingsContainerFragment.getView();
            assert settingsView != null;
            final View settingsList = settingsView.findViewById(R.id.settings_list);
            final View settingsContainer = settingsView.findViewById(R.id.settings_container);
            final FragmentTransaction ft = mFragmentManager.beginTransaction();
            if (isTablet) {
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .remove(settingsContainerFragment)
                        .commit();
                mApp.setSettingsContainerID(-1);
                VideOSCUIHelpers.resetSystemUIState(mDecorView);
            } else {
                if (networkSettingsID < 0 &&
                        resolutionSettingsID < 0 &&
                        commandMappingsID < 0 &&
                        sensorSettingsID < 0 &&
                        debugSettingsID < 0 &&
                        aboutID < 0) {
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                            .remove(settingsContainerFragment)
                            .commit();
                    mApp.setSettingsContainerID(-1);
                    VideOSCUIHelpers.resetSystemUIState(mDecorView);
                    // hack - otherwise mToolsDrawerLayout.isDrawerOpen(GravityCompat.END) returns true
                    // though the drawer appears to be closed (???)
                    mToolsDrawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    if (networkSettingsID > 0) {
                        networkSettingsDialog = mFragmentManager.findFragmentById(networkSettingsID);
                        assert networkSettingsDialog != null;
                        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                .remove(networkSettingsDialog)
                                .commit();
                        mApp.setNetworkSettingsID(-1);
                    }
                    if (resolutionSettingsID > 0) {
                        resolutionSettingsDialog = mFragmentManager.findFragmentById(resolutionSettingsID);
                        assert resolutionSettingsDialog != null;
                        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                .remove(resolutionSettingsDialog)
                                .commit();
                        mApp.setResolutionSettingsID(-1);
                    }
                    if (commandMappingsID > 0) {
                        commandMappingsDialog = mFragmentManager.findFragmentById(commandMappingsID);
                        assert commandMappingsDialog != null;
                        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                .remove(commandMappingsDialog)
                                .commit();
                        mApp.setCommandMappingsID(-1);
                    }
                    if (sensorSettingsID > 0) {
                        sensorSettingsDialog = mFragmentManager.findFragmentById(sensorSettingsID);
                        assert sensorSettingsDialog != null;
                        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                .remove(sensorSettingsDialog)
                                .commit();
                        mApp.setSensorSettingsID(-1);
                    }
                    if (debugSettingsID > 0) {
                        debugSettingsDialog = mFragmentManager.findFragmentById(debugSettingsID);
                        assert debugSettingsDialog != null;
                        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                .remove(debugSettingsDialog)
                                .commit();
                        mApp.setDebugSettingsID(-1);
                    }
                    if (aboutID > 0) {
                        about = mFragmentManager.findFragmentById(aboutID);
                        assert about != null;
                        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                .remove(about)
                                .commit();
                        mApp.setAboutSettingsID(-1);
                    }
                    assert settingsList != null;
                    settingsList.setVisibility(View.VISIBLE);
                    settingsContainer.setBackgroundResource(0);
                }
            }
        } else {
            VideOSCDialogHelper.showQuitDialog(this);
        }
    }

    public void setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mCamView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            mCamView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    public void showBackButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    public HashMap<String, Integer> toolsDrawerKeys() {
        HashMap<String, Integer> toolsDrawerKeys = new HashMap<>();
        int index = 0;

        toolsDrawerKeys.put("startStop", index);
        if (mApp.getHasTorch())
            toolsDrawerKeys.put("torch", ++index);
        toolsDrawerKeys.put("modeSelect", ++index);
        toolsDrawerKeys.put("mInteractionMode", ++index);
        if (VideOSCUIHelpers.hasFrontsideCamera())
            toolsDrawerKeys.put("camSelect", ++index);
        toolsDrawerKeys.put("info", ++index);
        toolsDrawerKeys.put("prefs", ++index);
        toolsDrawerKeys.put("quit", ++index);

        return toolsDrawerKeys;
    }

    @Override
    public void onDestroy() {
        // stop sending OSC (probably not necessary)
        mApp.setCameraOSCisPlaying(false);
        // reset inverted colors
        mApp.setIsRGBPositive(true);
        // reset debug settings
        mApp.setPixelImageHidden(false);
        VideOSCApplication.setDebugPixelOsc(false);
        mApp.setHasExposureSettingBeenCancelled(false);
        mApp.setExposureIsFixed(false);
        mApp.setInteractionMode(InteractionModes.BASIC);
        mApp.setColorMode(RGBModes.RGB);
        mApp.setIsColorModePanelOpen(false);
        mApp.setIsIndicatorPanelOpen(false);
        mApp.setIsFPSCalcPanelOpen(false);
        mApp.setIsMultiSliderActive(false);
        mApp.setCurrentCameraId(VideOSCMainActivity.backsideCameraId);
        // close db
        mOscHelper.removeOscTcpEventListener();
        mOscHelper.removeOscUdpEventListener();
        try {
            mOscHelper.close();
            Log.d(TAG, "osc helper closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDbHelper.close();
//        mDbHelper.close();
        super.onDestroy();
    }

    public Enum<RGBToolbarStatus> getColorModeToolsDrawer() {
        return this.mColorModeToolsDrawer;
    }

    public VideOSCDBHelpers getDbHelper() {
        return this.mDbHelper;
    }

    public SQLiteDatabase getDatabase() {
        return this.mDb;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mToolsDrawerList != null) {
            ToolsMenuAdapter adapter = (ToolsMenuAdapter) mToolsDrawerList.getAdapter();
            SparseIntArray toolsDrawerListState = adapter.getToolsDrawerListState();
            // update tools drawer if some item's state has changed
            for (int i = 0; i < toolsDrawerListState.size(); i++) {
                int tool = toolsDrawerListState.get(i);
                if (tool != 0) {
                    mToolsList.set(i, (BitmapDrawable) ContextCompat.getDrawable(this, tool));
                }
            }
            mToolsDrawerList.setAdapter(new ToolsMenuAdapter(this, R.layout.drawer_item, R.id.tool, mToolsList));
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Snackbar.make(mCamView, R.string.camera_permission_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.grant, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(VideOSCMainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            }).show();
        } else {
            Snackbar.make(mCamView,
                    R.string.camera_permission_not_available,
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(
                        mCamView,
                        R.string.camera_permissions_granted,
                        Snackbar.LENGTH_SHORT
                ).show();

                // make sure permissions are granted before we touch the camera
                checkCamera();

                // the camera fragment overlays all other screen elements
                // hence, we get gui elements to front in surfaceCreated() within CameraPreview (VideOSCCameraFragment)
                mCameraPreview = new VideOSCCameraFragment(this);
                mFragmentManager.beginTransaction()
                        .replace(R.id.camera_preview, mCameraPreview, "CamPreview")
                        .commit();
                buildUI();
            } else {
                Snackbar.make(
                        mCamView,
                        R.string.camera_permissions_denied,
                        Snackbar.LENGTH_SHORT
                ).show();
            }
        }
    }

    // check if device has inbuilt torch
    // if camera failes to open show warning
    private void checkCamera() {
        Camera camera = null;

        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (camera != null) {
            mApp.setHasTorch(VideOSCUIHelpers.hasTorch(camera));
            camera.release();
        } else {
            VideOSCDialogHelper.showDialog(
                    this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    getString(R.string.msg_on_camera_open_fail),
                    getString(R.string.OK),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    }, null, null
            );
        }
    }

    private Point getAbsoluteScreenSize() {
        Point dimensions = new Point();

        final Display display = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            display.getRealSize(dimensions);
        else display.getSize(dimensions);

        return dimensions;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && requestCode == CODE_WRITE_SETTINGS_PERMISSION
                && Settings.System.canWrite(this)) {
            finish();
            startActivity(starterIntent);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
//		Log.d(TAG, "onFragmentInteraction, uri: " + uri);
    }

    @Override
    public void onFragmentInteraction(String id) {
//		Log.d(TAG, "onFragmentInteraction, id: " + id);
    }

    @Override
    public void onFragmentInteraction(int actionId) {
//		Log.d(TAG, "onFragmentInteraction, actionId: " + actionId);
    }

	/*@Override
	public void onCompleteCameraFragment() {
		Log.d(TAG, "onCompleteCameraFragment");
		final FragmentManager fragmentManager = getFragmentManager();
		final VideOSCCameraFragment cameraFragment = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
		Log.d(TAG, "camera fragment: " + cameraFragment.mPreview);
	}*/

}
