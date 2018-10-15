package net.videosc2.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.fragments.VideOSCCameraFragment;
import net.videosc2.fragments.VideOSCSettingsFragment;
import net.videosc2.utilities.VideOSCDialogHelper;
import net.videosc2.utilities.VideOSCUIHelpers;
import net.videosc2.utilities.enums.InteractionModes;
import net.videosc2.utilities.enums.RGBModes;
import net.videosc2.utilities.enums.RGBToolbarStatus;

import java.util.HashMap;
import java.util.List;

/**
 * Created by stefan on 14.03.17.
 */

public class ToolsMenuAdapter extends ArrayAdapter<BitmapDrawable> {
	final private static String TAG = "ToolsMenuAdapter";
	final private HashMap<Integer, Integer> mToolsDrawerListState = new HashMap<>();

	public ToolsMenuAdapter(Context context, int resource, int bitmapResourceId, List<BitmapDrawable> tools) {
		super(context, resource, bitmapResourceId, tools);
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		final VideOSCMainActivity activity = (VideOSCMainActivity) getContext();
		final VideOSCApplication app = (VideOSCApplication) activity.getApplication();
		final android.app.FragmentManager fragmentManager = activity.getFragmentManager();
		final ViewGroup indicators = activity.mCamView.findViewById(R.id.indicator_panel);
		final ImageView oscIndicator = indicators.findViewById(R.id.indicator_osc);
		final ImageView rgbModeIndicator = indicators.findViewById(R.id.indicator_color);
		final ImageView interactionModeIndicator = indicators.findViewById(R.id.indicator_interaction);
		final ImageView cameraIndicator = indicators.findViewById(R.id.indicator_camera);
		final ImageView torchIndicator = indicators.findViewById(R.id.torch_status_indicator);
		// cameraFragment provides all instance methods of the camera preview
		// no reflections needed
		final VideOSCCameraFragment cameraFragment = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
//		final Camera camera = cameraFragment.mCamera;

		HashMap<String, Integer> toolsDrawerKeys = activity.toolsDrawerKeys();
		final int START_STOP = toolsDrawerKeys.get("startStop");
		final Integer TORCH = toolsDrawerKeys.containsKey("torch") ? toolsDrawerKeys.get("torch") : null;
		final int COLOR_MODE = toolsDrawerKeys.get("modeSelect");
		final int INTERACTION = toolsDrawerKeys.get("mInteractionMode");
		final Integer SELECT_CAM = toolsDrawerKeys.containsKey("camSelect") ? toolsDrawerKeys.get("camSelect") : null;
		final int INFO = toolsDrawerKeys.get("info");
		final int SETTINGS = toolsDrawerKeys.get("prefs");
		final int QUIT = toolsDrawerKeys.get("quit");

		BitmapDrawable tool;

		tool = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_item, parent, false);
		}
		final View adapterView = parent;
		// Lookup view for data population
		final ImageView toolView = convertView.findViewById(R.id.tool);
		// Populate the data into the template view using the data object
		toolView.setImageDrawable(tool);
		toolView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				BitmapDrawable img;
				Camera camera = cameraFragment.mCamera;
				Camera.Parameters cameraParameters = camera.getParameters();

				if (position == START_STOP) {
					activity.closeColorModePanel();
					if (!app.getCameraOSCisPlaying()) {
						app.setCameraOSCisPlaying(true);
						mToolsDrawerListState.put(START_STOP, R.drawable.stop);
						img = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.stop);
						oscIndicator.setImageResource(R.drawable.osc_playing);
					} else {
						app.setCameraOSCisPlaying(false);
						mToolsDrawerListState.put(START_STOP, R.drawable.start);
						img = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.start);
						oscIndicator.setImageResource(R.drawable.osc_paused);
					}
					toolView.setImageDrawable(img);
				} else if (TORCH != null && position == TORCH) {
					activity.closeColorModePanel();
					if (app.getCurrentCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
						String flashMode = cameraParameters.getFlashMode();
						app.setIsTorchOn(!app.getIsTorchOn());
						if (!flashMode.equals("torch")) {
							cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
							mToolsDrawerListState.put(TORCH, R.drawable.light);
							img = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.light);
							torchIndicator.setImageResource(R.drawable.light_on_indicator);
						} else {
							cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							mToolsDrawerListState.put(TORCH, R.drawable.light_on);
							img = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.light_on);
							torchIndicator.setImageResource(R.drawable.light_off_indicator);
						}
						camera.setParameters(cameraParameters);
						toolView.setImageDrawable(img);
					}
				} else if (position == COLOR_MODE) {
					if (!app.getIsColorModePanelOpen()) {
						int y = (int) view.getY();

						VideOSCUIHelpers.setTransitionAnimation(activity.mModePanel);
						app.setIsColorModePanelOpen(VideOSCUIHelpers.addView(activity.mModePanel, (FrameLayout) activity.mCamView));

						if (app.getRGBHasChanged()) {
							ImageView red = activity.findViewById(R.id.mode_r);
							ImageView green = activity.findViewById(R.id.mode_g);
							ImageView blue = activity.findViewById(R.id.mode_b);
							int redRes = app.getIsRGBPositive() ? R.drawable.r : R.drawable.r_inv;
							int greenRes = app.getIsRGBPositive() ? R.drawable.g : R.drawable.g_inv;
							int blueRes = app.getIsRGBPositive() ? R.drawable.b : R.drawable.b_inv;
							red.setImageResource(redRes);
							green.setImageResource(greenRes);
							blue.setImageResource(blueRes);
						}

						VideOSCUIHelpers.setMargins(activity.mModePanel, 0, y, 60, 0);

						for (int k = 0; k < activity.mModePanel.getChildCount(); k++) {
							View button = activity.mModePanel.getChildAt(k);
							button.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									switch (v.getId()) {
										case R.id.mode_rgb:
											app.setColorMode(RGBModes.RGB);
											if (!app.getIsRGBPositive()) {
												app.setIsRGBPositive(true);
												app.setRGBHasChanged(true);
											}
											mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb);
											toolView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.rgb));
											rgbModeIndicator.setImageResource(R.drawable.rgb_indicator);
											activity.mColorModeToolsDrawer = RGBToolbarStatus.RGB;
											break;
										case R.id.mode_rgb_inv:
											app.setColorMode(RGBModes.RGB);
											if (app.getIsRGBPositive()) {
												app.setIsRGBPositive(false);
												app.setRGBHasChanged(true);
											}
											mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb_inv);
											toolView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.rgb_inv));
											rgbModeIndicator.setImageResource(R.drawable.rgb_inv_indicator);
											activity.mColorModeToolsDrawer = RGBToolbarStatus.RGB_INV;
											break;
										case R.id.mode_r:
											app.setColorMode(RGBModes.R);
											if (app.getIsRGBPositive()) {
												mToolsDrawerListState.put(COLOR_MODE, R.drawable.r);
												toolView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.r));
												activity.mColorModeToolsDrawer = RGBToolbarStatus.R;
											} else {
												mToolsDrawerListState.put(COLOR_MODE, R.drawable.r_inv);
												toolView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.r_inv));
												activity.mColorModeToolsDrawer = RGBToolbarStatus.R_INV;
											}
											break;
										case R.id.mode_g:
//												Log.d(TAG, "green");
											app.setColorMode(RGBModes.G);
											if (app.getIsRGBPositive()) {
												mToolsDrawerListState.put(COLOR_MODE, R.drawable.g);
												toolView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.g));
												activity.mColorModeToolsDrawer = RGBToolbarStatus.G;
											} else {
												mToolsDrawerListState.put(COLOR_MODE, R.drawable.g_inv);
												toolView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.g_inv));
												activity.mColorModeToolsDrawer = RGBToolbarStatus.G_INV;
											}
											break;
										case R.id.mode_b:
//												Log.d(TAG, "blue");
											app.setColorMode(RGBModes.B);
											if (app.getIsRGBPositive()) {
												mToolsDrawerListState.put(COLOR_MODE, R.drawable.b);
												toolView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.b));
												activity.mColorModeToolsDrawer = RGBToolbarStatus.B;
											} else {
												mToolsDrawerListState.put(COLOR_MODE, R.drawable.b_inv);
												toolView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.b_inv));
												activity.mColorModeToolsDrawer = RGBToolbarStatus.B_INV;
											}
											break;
										default:
											app.setColorMode(RGBModes.RGB);
											mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb);
											toolView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.rgb));
											activity.mColorModeToolsDrawer = RGBToolbarStatus.RGB;
									}
//									v.clearFocus();
									app.setIsColorModePanelOpen(VideOSCUIHelpers.removeView(activity.mModePanel, (FrameLayout) activity.mCamView));
								}
							});
						}
					}
				} else if (position == INTERACTION) {
					activity.closeColorModePanel();
					if (app.getInteractionMode().equals(InteractionModes.BASIC)) {
						app.setInteractionMode(InteractionModes.SINGLE_PIXEL);
						VideOSCUIHelpers.addView(activity.mPixelEditor, (FrameLayout) activity.mCamView);
						mToolsDrawerListState.put(INTERACTION, R.drawable.interactionplus);
						img = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.interactionplus);
						interactionModeIndicator.setImageResource(R.drawable.interaction_plus_indicator);
					} else if (app.getInteractionMode().equals(InteractionModes.SINGLE_PIXEL)) {
						app.setInteractionMode(InteractionModes.BASIC);
						cameraFragment.getSelectedPixels().clear();
						mToolsDrawerListState.put(INTERACTION, R.drawable.interaction);
						VideOSCUIHelpers.removeView(activity.mPixelEditor, (FrameLayout) activity.mCamView);
						img = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.interaction);
						interactionModeIndicator.setImageResource(R.drawable.interaction_none_indicator);
						if (activity.mMultiSliderView != null)
							fragmentManager.beginTransaction().remove(activity.mMultiSliderView).commit();
//						isMultiSliderVisible = VideOSCUIHelpers.removeView(mMultiSliderView, (FrameLayout) mCamView);
					} else {
						mToolsDrawerListState.put(INTERACTION, R.drawable.interaction);
						img = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.interaction);
						if (activity.mMultiSliderView != null)
							fragmentManager.beginTransaction().remove(activity.mMultiSliderView).commit();
					}
					toolView.setImageDrawable(img);
				} else if (SELECT_CAM != null && position == SELECT_CAM) {
					activity.closeColorModePanel();
					BitmapDrawable noTorch = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.no_torch);
					Log.d(TAG, "current camera id: " + app.getCurrentCameraId() + ", backside camera: " + VideOSCMainActivity.backsideCameraId + ", frontside camera: " + VideOSCMainActivity.frontsideCameraId);
					if (app.getCurrentCameraId() == VideOSCMainActivity.backsideCameraId) {
						app.setCurrentCameraId(VideOSCMainActivity.frontsideCameraId);
						app.setIsTorchOn(false);
						mToolsDrawerListState.put(SELECT_CAM, R.drawable.front_camera);
						img = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.front_camera);
						if (app.getHasTorch()/* && cameraParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)*/) {
							cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							torchIndicator.setImageResource(R.drawable.light_off_indicator);
							if (TORCH != null) {
								ImageView torchSwitch = (ImageView) ((ViewGroup) adapterView).getChildAt(TORCH);
								torchSwitch.setImageDrawable(noTorch);
							}
						}
						cameraIndicator.setImageResource(R.drawable.indicator_camera_front);
					} else {
						app.setCurrentCameraId(VideOSCMainActivity.backsideCameraId);
						mToolsDrawerListState.put(SELECT_CAM, R.drawable.back_camera);
						img = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.back_camera);
						cameraIndicator.setImageResource(R.drawable.indicator_camera_back);
						if (app.getHasTorch() && TORCH != null) {
							ImageView torchSwitch = (ImageView) ((ViewGroup) adapterView).getChildAt(TORCH);
							BitmapDrawable torchOn = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.light_on);
							torchSwitch.setImageDrawable(torchOn);
						}
					}
					toolView.setImageDrawable(img);
					// invoke setting of new camera
					// camera ID should already have been set in currentCameraID
					cameraFragment.safeCameraOpenInView(activity.mCamView);
				} else if (position == INFO) {
					activity.closeColorModePanel();
					if (app.getIsFPSCalcPanelOpen())
						app.setIsFPSCalcPanelOpen(VideOSCUIHelpers.removeView(activity.mFrameRateCalculationPanel, (FrameLayout) activity.mCamView));
					else {
						VideOSCUIHelpers.setTransitionAnimation(activity.mFrameRateCalculationPanel);
						app.setIsFPSCalcPanelOpen(VideOSCUIHelpers.addView(activity.mFrameRateCalculationPanel, (FrameLayout) activity.mCamView));
					}
				} else if (position == SETTINGS) {
					activity.closeColorModePanel();
					app.setSettingsLevel(1);
					activity.showBackButton();
					VideOSCSettingsFragment settings = new VideOSCSettingsFragment();
					if (fragmentManager.findFragmentByTag("settings selection") == null
							&& fragmentManager.findFragmentByTag("snapshot selection") == null)
						fragmentManager.beginTransaction().add(R.id.camera_preview, settings, "settings selection").commit();
				} else if (position == QUIT) {
					VideOSCDialogHelper.showQuitDialog(activity);
				}
				activity.mToolsDrawerLayout.closeDrawer(Gravity.END);
				// reset menu item background immediatly
				view.setBackgroundColor(0x00000000);
				Log.d(TAG, "position: " + position + ", view: " + view);
			}
		});
		// Return the completed view to render on screen
		return convertView;
	}

	public HashMap<Integer, Integer> getToolsDrawerListState() {
		return this.mToolsDrawerListState;
	}
}
