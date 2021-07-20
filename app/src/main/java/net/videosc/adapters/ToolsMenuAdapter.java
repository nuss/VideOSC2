package net.videosc.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.fragments.settings.VideOSCSettingsListFragment;
import net.videosc.utilities.VideOSCDBHelpers;
import net.videosc.utilities.VideOSCDialogHelper;
import net.videosc.utilities.VideOSCUIHelpers;
import net.videosc.utilities.enums.InteractionModes;
import net.videosc.utilities.enums.RGBModes;
import net.videosc.utilities.enums.RGBToolbarStatus;

import java.util.HashMap;
import java.util.List;

/**
 * Created by stefan on 14.03.17.
 */

public class ToolsMenuAdapter extends ArrayAdapter<BitmapDrawable> {
    final private static String TAG = "ToolsMenuAdapter";
    final private SparseIntArray mToolsDrawerListState = new SparseIntArray();
    private final Context mContext;

    public ToolsMenuAdapter(Context context, int resource, int bitmapResourceId, List<BitmapDrawable> tools) {
        super(context, resource, bitmapResourceId, tools);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        BitmapDrawable tool = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_item, parent, false);
        }
        // Lookup view for data population
        final ImageView toolView = convertView.findViewById(R.id.tool);
        // Populate the data into the template view using the data object
        toolView.setImageDrawable(tool);
        toolView.setOnClickListener(new ToolViewOnClickListener(mContext, position, toolView, parent));
        // Return the completed view to render on screen
        return convertView;
    }

    public SparseIntArray getToolsDrawerListState() {
        return this.mToolsDrawerListState;
    }

    private class ToolsOnClickListener implements View.OnClickListener {
        private final VideOSCApplication mApp;
        private final int COLOR_MODE;
        private final ImageView mToolView;
        //		private final WeakReference<VideOSCMainActivity> activityRef;
        private final ImageView mRGBModeIndicator;
        private final VideOSCMainActivity mActivity;

        ToolsOnClickListener(VideOSCApplication app, int COLOR_MODE, ImageView toolView, Context context, ImageView rgbModeIndicator) {
            this.mApp = app;
            this.COLOR_MODE = COLOR_MODE;
            this.mToolView = toolView;
            this.mActivity = (VideOSCMainActivity) context;
            this.mRGBModeIndicator = rgbModeIndicator;
        }

        @Override
        public void onClick(View v) {
            final int id = v.getId();
            if (id == R.id.mode_rgb) {
                mApp.setColorMode(RGBModes.RGB);
                if (!mApp.getIsRGBPositive()) {
                    mApp.setIsRGBPositive(true);
                    mApp.setRGBHasChanged(true);
                }
                mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb);
                mToolView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.rgb));
                mRGBModeIndicator.setImageResource(R.drawable.rgb_indicator);
                mActivity.mColorModeToolsDrawer = RGBToolbarStatus.RGB;
            } else if (id == R.id.mode_rgb_inv) {
                mApp.setColorMode(RGBModes.RGB);
                if (mApp.getIsRGBPositive()) {
                    mApp.setIsRGBPositive(false);
                    mApp.setRGBHasChanged(true);
                }
                mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb_inv);
                mToolView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.rgb_inv));
                mRGBModeIndicator.setImageResource(R.drawable.rgb_inv_indicator);
                mActivity.mColorModeToolsDrawer = RGBToolbarStatus.RGB_INV;
            } else if (id == R.id.mode_r) {
                mApp.setColorMode(RGBModes.R);
                if (mApp.getIsRGBPositive()) {
                    mToolsDrawerListState.put(COLOR_MODE, R.drawable.r);
                    mToolView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.r));
                    mActivity.mColorModeToolsDrawer = RGBToolbarStatus.R;
                } else {
                    mToolsDrawerListState.put(COLOR_MODE, R.drawable.r_inv);
                    mToolView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.r_inv));
                    mActivity.mColorModeToolsDrawer = RGBToolbarStatus.R_INV;
                }
            } else if (id == R.id.mode_g) {
                mApp.setColorMode(RGBModes.G);
                if (mApp.getIsRGBPositive()) {
                    mToolsDrawerListState.put(COLOR_MODE, R.drawable.g);
                    mToolView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.g));
                    mActivity.mColorModeToolsDrawer = RGBToolbarStatus.G;
                } else {
                    mToolsDrawerListState.put(COLOR_MODE, R.drawable.g_inv);
                    mToolView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.g_inv));
                    mActivity.mColorModeToolsDrawer = RGBToolbarStatus.G_INV;
                }
            } else if (id == R.id.mode_b) {
                mApp.setColorMode(RGBModes.B);
                if (mApp.getIsRGBPositive()) {
                    mToolsDrawerListState.put(COLOR_MODE, R.drawable.b);
                    mToolView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.b));
                    mActivity.mColorModeToolsDrawer = RGBToolbarStatus.B;
                } else {
                    mToolsDrawerListState.put(COLOR_MODE, R.drawable.b_inv);
                    mToolView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.b_inv));
                    mActivity.mColorModeToolsDrawer = RGBToolbarStatus.B_INV;
                }
            } else {
                mApp.setColorMode(RGBModes.RGB);
                mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb);
                mToolView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.rgb));
                mActivity.mColorModeToolsDrawer = RGBToolbarStatus.RGB;
            }
//									v.clearFocus();
            mApp.setIsColorModePanelOpen(VideOSCUIHelpers.removeView(
                    mActivity.mModePanel, (FrameLayout) mActivity.mCamView
            ));
        }
    }

    private class ToolViewOnClickListener implements View.OnClickListener {
        private final int mPosition;
        private final ImageView mToolView;
        private final ViewGroup mAdapterView;
        private final VideOSCMainActivity mActivity;
        private final VideOSCDBHelpers mDBHelper;

        ToolViewOnClickListener(Context context, int position, ImageView toolView, ViewGroup adapterView) {
            this.mActivity = (VideOSCMainActivity) context;
            this.mPosition = position;
            this.mToolView = toolView;
            this.mAdapterView = adapterView;
            this.mDBHelper = new VideOSCDBHelpers(mActivity);
        }

        @Override
        public void onClick(View view) {
            final VideOSCApplication app = (VideOSCApplication) mActivity.getApplicationContext();
            final FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            final ViewGroup indicators = mActivity.mCamView.findViewById(R.id.indicator_panel);
            final ImageView oscIndicator = indicators.findViewById(R.id.indicator_osc);
            final ImageView rgbModeIndicator = indicators.findViewById(R.id.indicator_color);
            final ImageView interactionModeIndicator = indicators.findViewById(R.id.indicator_interaction);
            final ImageView cameraIndicator = indicators.findViewById(R.id.indicator_camera);
            final ImageView torchIndicator = indicators.findViewById(R.id.torch_status_indicator);
            final ViewGroup basicToolsPalette = mActivity.mCamView.findViewById(R.id.basic_tools_bar);
            final ImageButton feedbackButton = basicToolsPalette.findViewById(R.id.osc_feedback_button);
            // cameraFragment provides all instance methods of the camera preview
            // no reflections needed
            final VideOSCCameraFragment cameraFragment = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
            HashMap<String, Integer> toolsDrawerKeys = mActivity.toolsDrawerKeys();
//            final Integer START_STOP = toolsDrawerKeys.get("startStop");
            final Integer TORCH = toolsDrawerKeys.containsKey("torch") ? toolsDrawerKeys.get("torch") : null;
            final Integer COLOR_MODE = toolsDrawerKeys.get("modeSelect");
            final Integer INTERACTION = toolsDrawerKeys.get("mInteractionMode");
            final Integer SELECT_CAM = toolsDrawerKeys.containsKey("camSelect") ? toolsDrawerKeys.get("camSelect") : null;
            final Integer INFO = toolsDrawerKeys.get("info");
            final Integer SETTINGS = toolsDrawerKeys.get("prefs");
            final Integer QUIT = toolsDrawerKeys.get("quit");
            BitmapDrawable img;
            assert cameraFragment != null;
            Camera camera = cameraFragment.mCamera;
            Camera.Parameters cameraParameters = camera.getParameters();

/*
            if (START_STOP != null && mPosition == START_STOP) {
                mActivity.closeColorModePanel();
                if (mDBHelper.countAddresses() > 0) {
                    if (!app.getCameraOSCisPlaying()) {
                        app.setCameraOSCisPlaying(true);
                        mToolsDrawerListState.put(START_STOP, R.drawable.stop);
                        img = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.stop);
                        oscIndicator.setImageResource(R.drawable.osc_playing);
                    } else {
                        app.setCameraOSCisPlaying(false);
                        VideOSCOscHandler oscHandler = app.getOscHelper();
                        app.setOSCFeedbackActivated(false);
                        feedbackButton.setActivated(false);
                        oscHandler.removeOscUdpEventListener();
                        oscHandler.removeOscTcpEventListener();
                        mToolsDrawerListState.put(START_STOP, R.drawable.start);
                        img = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.start);
                        oscIndicator.setImageResource(R.drawable.osc_paused);
                    }
                    mToolView.setImageDrawable(img);
                } else {
                    VideOSCDialogHelper.showWarning(
                            mActivity,
                            android.R.style.Theme_Holo_Light_Dialog,
                            app.getString(R.string.define_client_address),
                            app.getString(R.string.OK)
                    );
                }
            } else
*/
            if (TORCH != null && mPosition == TORCH) {
                mActivity.closeColorModePanel();
                if (app.getCurrentCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    String flashMode = cameraParameters.getFlashMode();
                    app.setIsTorchOn(!app.getIsTorchOn());
                    if (!flashMode.equals("torch")) {
                        cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mToolsDrawerListState.put(TORCH, R.drawable.light);
                        img = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.light);
                        torchIndicator.setImageResource(R.drawable.light_on_indicator);
                    } else {
                        cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mToolsDrawerListState.put(TORCH, R.drawable.light_on);
                        img = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.light_on);
                        torchIndicator.setImageResource(R.drawable.light_off_indicator);
                    }
                    camera.setParameters(cameraParameters);
                    mToolView.setImageDrawable(img);
                }
            } else if (COLOR_MODE != null && mPosition == COLOR_MODE) {
                if (!app.getIsColorModePanelOpen()) {
                    int y = (int) view.getY();

                    VideOSCUIHelpers.setTransitionAnimation(mActivity.mModePanel);
                    app.setIsColorModePanelOpen(VideOSCUIHelpers.addView(mActivity.mModePanel, (FrameLayout) mActivity.mCamView));

                    if (app.getRGBHasChanged()) {
                        ImageView red = mActivity.findViewById(R.id.mode_r);
                        ImageView green = mActivity.findViewById(R.id.mode_g);
                        ImageView blue = mActivity.findViewById(R.id.mode_b);
                        int redRes = app.getIsRGBPositive() ? R.drawable.r : R.drawable.r_inv;
                        int greenRes = app.getIsRGBPositive() ? R.drawable.g : R.drawable.g_inv;
                        int blueRes = app.getIsRGBPositive() ? R.drawable.b : R.drawable.b_inv;
                        red.setImageResource(redRes);
                        green.setImageResource(greenRes);
                        blue.setImageResource(blueRes);
                    }

                    VideOSCUIHelpers.setMargins(mActivity.mModePanel, 0, y, 60, 0);

                    for (int k = 0; k < mActivity.mModePanel.getChildCount(); k++) {
                        View button = mActivity.mModePanel.getChildAt(k);
                        button.setOnClickListener(new ToolsOnClickListener(app, COLOR_MODE, mToolView, mActivity, rgbModeIndicator));
                    }
                }
            } else if (INTERACTION != null && mPosition == INTERACTION) {
                mActivity.closeColorModePanel();
                if (app.getInteractionMode().equals(InteractionModes.BASIC)) {
                    app.setInteractionMode(InteractionModes.SINGLE_PIXEL);
                    VideOSCUIHelpers.addView(mActivity.mPixelEditor, (FrameLayout) mActivity.mCamView);
                    mToolsDrawerListState.put(INTERACTION, R.drawable.interactionplus);
                    img = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.interaction);
                    interactionModeIndicator.setImageResource(R.drawable.interaction_plus_indicator);
                } else if (app.getInteractionMode().equals(InteractionModes.SINGLE_PIXEL)) {
                    app.setInteractionMode(InteractionModes.BASIC);
                    cameraFragment.getSelectedPixels().clear();
                    mToolsDrawerListState.put(INTERACTION, R.drawable.interaction);
                    VideOSCUIHelpers.removeView(mActivity.mPixelEditor, (FrameLayout) mActivity.mCamView);
                    img = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.interactionplus);
                    interactionModeIndicator.setImageResource(R.drawable.interaction_none_indicator);
                    if (mActivity.mMultiSliderView != null)
                        fragmentManager.beginTransaction().remove(mActivity.mMultiSliderView).commit();
                } else {
                    mToolsDrawerListState.put(INTERACTION, R.drawable.interaction);
                    img = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.interaction);
                    if (mActivity.mMultiSliderView != null)
                        fragmentManager.beginTransaction().remove(mActivity.mMultiSliderView).commit();
                }
                mToolView.setImageDrawable(img);
            } else if (SELECT_CAM != null && mPosition == SELECT_CAM) {
                mActivity.closeColorModePanel();
                BitmapDrawable noTorch = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.no_torch);
                if (app.getCurrentCameraId() == VideOSCMainActivity.backsideCameraId) {
//						Log.d(TAG, "current: backside camera");
                    app.setCurrentCameraId(VideOSCMainActivity.frontsideCameraId);
                    app.setIsTorchOn(false);
                    mToolsDrawerListState.put(SELECT_CAM, R.drawable.back_camera);
                    if (TORCH != null)
                        mToolsDrawerListState.put(TORCH, R.drawable.no_torch);
                    img = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.back_camera);
                    if (app.getHasTorch()/* && cameraParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)*/) {
                        cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        torchIndicator.setImageResource(R.drawable.light_off_indicator);
                        if (TORCH != null) {
                            ImageView torchSwitch = (ImageView) mAdapterView.getChildAt(TORCH);
                            torchSwitch.setImageDrawable(noTorch);
                        }
                    }
                    cameraIndicator.setImageResource(R.drawable.indicator_camera_front);
                } else {
                    app.setCurrentCameraId(VideOSCMainActivity.backsideCameraId);
                    mToolsDrawerListState.put(SELECT_CAM, R.drawable.front_camera);
                    if (TORCH != null)
                        mToolsDrawerListState.put(TORCH, R.drawable.light_on);
                    img = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.front_camera);
                    cameraIndicator.setImageResource(R.drawable.indicator_camera_back);
                    if (app.getHasTorch() && TORCH != null) {
                        ImageView torchSwitch = (ImageView) mAdapterView.getChildAt(TORCH);
                        BitmapDrawable torchOn = (BitmapDrawable) ContextCompat.getDrawable(mActivity, R.drawable.light_on);
                        torchSwitch.setImageDrawable(torchOn);
                    }
                }
                mToolView.setImageDrawable(img);
                // invoke setting of new camera
                // camera ID should already have been set in currentCameraID
                cameraFragment.safeCameraOpenInView(mActivity.mCamView);
            } else if (INFO != null && mPosition == INFO) {
                mActivity.closeColorModePanel();
                if (app.getIsFPSCalcPanelOpen())
                    app.setIsFPSCalcPanelOpen(VideOSCUIHelpers.removeView(
                            mActivity.mFrameRateCalculationPanel,
                            (FrameLayout) mActivity.mCamView
                    ));
                else {
                    VideOSCUIHelpers.setTransitionAnimation(mActivity.mFrameRateCalculationPanel);
                    app.setIsFPSCalcPanelOpen(VideOSCUIHelpers.addView(
                            mActivity.mFrameRateCalculationPanel,
                            (FrameLayout) mActivity.mCamView
                    ));
                }
            } else if (SETTINGS != null && mPosition == SETTINGS) {
                mActivity.closeColorModePanel();
                mActivity.showBackButton();
                VideOSCSettingsListFragment settings = new VideOSCSettingsListFragment(mActivity);
                if (fragmentManager.findFragmentByTag("settings selection") == null
                        && fragmentManager.findFragmentByTag("snapshot selection") == null)
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                            .add(R.id.camera_preview, settings, "settings selection")
                            .commit();
            } else if (QUIT != null && mPosition == QUIT) {
                VideOSCDialogHelper.showQuitDialog(mActivity);
            }
            mActivity.mToolsDrawerLayout.closeDrawer(GravityCompat.END);
            // reset menu item background immediatly
            view.setBackgroundColor(0x00000000);
        }
    }
}
