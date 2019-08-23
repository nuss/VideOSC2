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
import android.widget.ImageView;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.fragments.settings.VideOSCSettingsListFragment;
import net.videosc.utilities.VideOSCDialogHelper;
import net.videosc.utilities.VideOSCUIHelpers;
import net.videosc.utilities.enums.InteractionModes;
import net.videosc.utilities.enums.RGBModes;
import net.videosc.utilities.enums.RGBToolbarStatus;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;

/**
 * Created by stefan on 14.03.17.
 */

public class ToolsMenuAdapter extends ArrayAdapter<BitmapDrawable> {
	final private static String TAG = "ToolsMenuAdapter";
	final private SparseIntArray mToolsDrawerListState = new SparseIntArray();

	public ToolsMenuAdapter(Context context, int resource, int bitmapResourceId, List<BitmapDrawable> tools) {
		super(context, resource, bitmapResourceId, tools);
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		BitmapDrawable tool = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_item, parent, false);
		}
//		final ViewGroup adapterView = parent;
		// Lookup view for data population
		final ImageView toolView = convertView.findViewById(R.id.tool);
		// Populate the data into the template view using the data object
		toolView.setImageDrawable(tool);
		toolView.setOnClickListener(new ToolViewOnClickListener(position, toolView, parent));
		// Return the completed view to render on screen
		return convertView;
	}

	public SparseIntArray getToolsDrawerListState() {
		return this.mToolsDrawerListState;
	}

	private class ToolsOnClickListener implements View.OnClickListener {
		private final VideOSCApplication app;
		private final int COLOR_MODE;
		private final ImageView toolView;
		private final WeakReference<VideOSCMainActivity> activityRef;
		private final ImageView rgbModeIndicator;

		ToolsOnClickListener(VideOSCApplication app, int COLOR_MODE, ImageView toolView, WeakReference<VideOSCMainActivity> activityRef, ImageView rgbModeIndicator) {
			this.app = app;
			this.COLOR_MODE = COLOR_MODE;
			this.toolView = toolView;
			this.activityRef = activityRef;
			this.rgbModeIndicator = rgbModeIndicator;
		}

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
					toolView.setImageDrawable(ContextCompat.getDrawable(activityRef.get(), R.drawable.rgb));
					rgbModeIndicator.setImageResource(R.drawable.rgb_indicator);
					activityRef.get().mColorModeToolsDrawer = RGBToolbarStatus.RGB;
					break;
				case R.id.mode_rgb_inv:
					app.setColorMode(RGBModes.RGB);
					if (app.getIsRGBPositive()) {
						app.setIsRGBPositive(false);
						app.setRGBHasChanged(true);
					}
					mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb_inv);
					toolView.setImageDrawable(ContextCompat.getDrawable(activityRef.get(), R.drawable.rgb_inv));
					rgbModeIndicator.setImageResource(R.drawable.rgb_inv_indicator);
					activityRef.get().mColorModeToolsDrawer = RGBToolbarStatus.RGB_INV;
					break;
				case R.id.mode_r:
					app.setColorMode(RGBModes.R);
					if (app.getIsRGBPositive()) {
						mToolsDrawerListState.put(COLOR_MODE, R.drawable.r);
						toolView.setImageDrawable(ContextCompat.getDrawable(activityRef.get(), R.drawable.r));
						activityRef.get().mColorModeToolsDrawer = RGBToolbarStatus.R;
					} else {
						mToolsDrawerListState.put(COLOR_MODE, R.drawable.r_inv);
						toolView.setImageDrawable(ContextCompat.getDrawable(activityRef.get(), R.drawable.r_inv));
						activityRef.get().mColorModeToolsDrawer = RGBToolbarStatus.R_INV;
					}
					break;
				case R.id.mode_g:
//												Log.d(TAG, "green");
					app.setColorMode(RGBModes.G);
					if (app.getIsRGBPositive()) {
						mToolsDrawerListState.put(COLOR_MODE, R.drawable.g);
						toolView.setImageDrawable(ContextCompat.getDrawable(activityRef.get(), R.drawable.g));
						activityRef.get().mColorModeToolsDrawer = RGBToolbarStatus.G;
					} else {
						mToolsDrawerListState.put(COLOR_MODE, R.drawable.g_inv);
						toolView.setImageDrawable(ContextCompat.getDrawable(activityRef.get(), R.drawable.g_inv));
						activityRef.get().mColorModeToolsDrawer = RGBToolbarStatus.G_INV;
					}
					break;
				case R.id.mode_b:
//												Log.d(TAG, "blue");
					app.setColorMode(RGBModes.B);
					if (app.getIsRGBPositive()) {
						mToolsDrawerListState.put(COLOR_MODE, R.drawable.b);
						toolView.setImageDrawable(ContextCompat.getDrawable(activityRef.get(), R.drawable.b));
						activityRef.get().mColorModeToolsDrawer = RGBToolbarStatus.B;
					} else {
						mToolsDrawerListState.put(COLOR_MODE, R.drawable.b_inv);
						toolView.setImageDrawable(ContextCompat.getDrawable(activityRef.get(), R.drawable.b_inv));
						activityRef.get().mColorModeToolsDrawer = RGBToolbarStatus.B_INV;
					}
					break;
				default:
					app.setColorMode(RGBModes.RGB);
					mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb);
					toolView.setImageDrawable(ContextCompat.getDrawable(activityRef.get(), R.drawable.rgb));
					activityRef.get().mColorModeToolsDrawer = RGBToolbarStatus.RGB;
			}
//									v.clearFocus();
			app.setIsColorModePanelOpen(VideOSCUIHelpers.removeView(
					activityRef.get().mModePanel, (FrameLayout) activityRef.get().mCamView
			));
		}
	}

	private class ToolViewOnClickListener implements View.OnClickListener {
		private final int position;
		private final ImageView toolView;
		private final ViewGroup adapterView;

		ToolViewOnClickListener(int position, ImageView toolView, ViewGroup adapterView) {
			this.position = position;
			this.toolView = toolView;
			this.adapterView = adapterView;
		}

		@Override
		public void onClick(View view) {
			final WeakReference<VideOSCMainActivity> activityRef = new WeakReference<>((VideOSCMainActivity) getContext());
			final VideOSCApplication app = (VideOSCApplication) activityRef.get().getApplication();
			final FragmentManager fragmentManager = activityRef.get().getSupportFragmentManager();
			final ViewGroup indicators = activityRef.get().mCamView.findViewById(R.id.indicator_panel);
			final ImageView oscIndicator = indicators.findViewById(R.id.indicator_osc);
			final ImageView rgbModeIndicator = indicators.findViewById(R.id.indicator_color);
			final ImageView interactionModeIndicator = indicators.findViewById(R.id.indicator_interaction);
			final ImageView cameraIndicator = indicators.findViewById(R.id.indicator_camera);
			final ImageView torchIndicator = indicators.findViewById(R.id.torch_status_indicator);
			// cameraFragment provides all instance methods of the camera preview
			// no reflections needed
			final VideOSCCameraFragment cameraFragment = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
			HashMap<String, Integer> toolsDrawerKeys = activityRef.get().toolsDrawerKeys();
			//noinspection ConstantConditions
			final int START_STOP = toolsDrawerKeys.get("startStop");
			final Integer TORCH = toolsDrawerKeys.containsKey("torch") ? toolsDrawerKeys.get("torch") : null;
			//noinspection ConstantConditions
			final int COLOR_MODE = toolsDrawerKeys.get("modeSelect");
			//noinspection ConstantConditions
			final int INTERACTION = toolsDrawerKeys.get("mInteractionMode");
			final Integer SELECT_CAM = toolsDrawerKeys.containsKey("camSelect") ? toolsDrawerKeys.get("camSelect") : null;
			//noinspection ConstantConditions
			final int INFO = toolsDrawerKeys.get("info");
			//noinspection ConstantConditions
			final int SETTINGS = toolsDrawerKeys.get("prefs");
			//noinspection ConstantConditions
			final int QUIT = toolsDrawerKeys.get("quit");
			BitmapDrawable img;
			assert cameraFragment != null;
			Camera camera = cameraFragment.mCamera;
			Camera.Parameters cameraParameters = camera.getParameters();

			if (position == START_STOP) {
				activityRef.get().closeColorModePanel();
				if (!app.getCameraOSCisPlaying()) {
					app.setCameraOSCisPlaying(true);
					mToolsDrawerListState.put(START_STOP, R.drawable.stop);
					img = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.stop);
					oscIndicator.setImageResource(R.drawable.osc_playing);
				} else {
					app.setCameraOSCisPlaying(false);
					mToolsDrawerListState.put(START_STOP, R.drawable.start);
					img = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.start);
					oscIndicator.setImageResource(R.drawable.osc_paused);
				}
				toolView.setImageDrawable(img);
			} else if (TORCH != null && position == TORCH) {
				activityRef.get().closeColorModePanel();
				if (app.getCurrentCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
					String flashMode = cameraParameters.getFlashMode();
					app.setIsTorchOn(!app.getIsTorchOn());
					if (!flashMode.equals("torch")) {
						cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
						mToolsDrawerListState.put(TORCH, R.drawable.light);
						img = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.light);
						torchIndicator.setImageResource(R.drawable.light_on_indicator);
					} else {
						cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
						mToolsDrawerListState.put(TORCH, R.drawable.light_on);
						img = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.light_on);
						torchIndicator.setImageResource(R.drawable.light_off_indicator);
					}
					camera.setParameters(cameraParameters);
					toolView.setImageDrawable(img);
				}
			} else if (position == COLOR_MODE) {
				if (!app.getIsColorModePanelOpen()) {
					int y = (int) view.getY();

					VideOSCUIHelpers.setTransitionAnimation(activityRef.get().mModePanel);
					app.setIsColorModePanelOpen(VideOSCUIHelpers.addView(activityRef.get().mModePanel, (FrameLayout) activityRef.get().mCamView));

					if (app.getRGBHasChanged()) {
						ImageView red = activityRef.get().findViewById(R.id.mode_r);
						ImageView green = activityRef.get().findViewById(R.id.mode_g);
						ImageView blue = activityRef.get().findViewById(R.id.mode_b);
						int redRes = app.getIsRGBPositive() ? R.drawable.r : R.drawable.r_inv;
						int greenRes = app.getIsRGBPositive() ? R.drawable.g : R.drawable.g_inv;
						int blueRes = app.getIsRGBPositive() ? R.drawable.b : R.drawable.b_inv;
						red.setImageResource(redRes);
						green.setImageResource(greenRes);
						blue.setImageResource(blueRes);
					}

					VideOSCUIHelpers.setMargins(activityRef.get().mModePanel, 0, y, 60, 0);

					for (int k = 0; k < activityRef.get().mModePanel.getChildCount(); k++) {
						View button = activityRef.get().mModePanel.getChildAt(k);
						button.setOnClickListener(new ToolsOnClickListener(app, COLOR_MODE, toolView, activityRef, rgbModeIndicator));
					}
				}
			} else if (position == INTERACTION) {
				activityRef.get().closeColorModePanel();
				if (app.getInteractionMode().equals(InteractionModes.BASIC)) {
					app.setInteractionMode(InteractionModes.SINGLE_PIXEL);
					VideOSCUIHelpers.addView(activityRef.get().mPixelEditor, (FrameLayout) activityRef.get().mCamView);
					mToolsDrawerListState.put(INTERACTION, R.drawable.interactionplus);
					img = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.interaction);
					interactionModeIndicator.setImageResource(R.drawable.interaction_plus_indicator);
				} else if (app.getInteractionMode().equals(InteractionModes.SINGLE_PIXEL)) {
					app.setInteractionMode(InteractionModes.BASIC);
					cameraFragment.getSelectedPixels().clear();
					mToolsDrawerListState.put(INTERACTION, R.drawable.interaction);
					VideOSCUIHelpers.removeView(activityRef.get().mPixelEditor, (FrameLayout) activityRef.get().mCamView);
					img = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.interactionplus);
					interactionModeIndicator.setImageResource(R.drawable.interaction_none_indicator);
					if (activityRef.get().mMultiSliderView != null)
						fragmentManager.beginTransaction().remove(activityRef.get().mMultiSliderView).commit();
				} else {
					mToolsDrawerListState.put(INTERACTION, R.drawable.interaction);
					img = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.interaction);
					if (activityRef.get().mMultiSliderView != null)
						fragmentManager.beginTransaction().remove(activityRef.get().mMultiSliderView).commit();
				}
				toolView.setImageDrawable(img);
			} else if (SELECT_CAM != null && position == SELECT_CAM) {
				activityRef.get().closeColorModePanel();
				BitmapDrawable noTorch = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.no_torch);
				if (app.getCurrentCameraId() == VideOSCMainActivity.backsideCameraId) {
//						Log.d(TAG, "current: backside camera");
					app.setCurrentCameraId(VideOSCMainActivity.frontsideCameraId);
					app.setIsTorchOn(false);
					mToolsDrawerListState.put(SELECT_CAM, R.drawable.back_camera);
					if (TORCH != null)
						mToolsDrawerListState.put(TORCH, R.drawable.no_torch);
					img = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.back_camera);
					if (app.getHasTorch()/* && cameraParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)*/) {
						cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
						torchIndicator.setImageResource(R.drawable.light_off_indicator);
						if (TORCH != null) {
							ImageView torchSwitch = (ImageView) adapterView.getChildAt(TORCH);
							torchSwitch.setImageDrawable(noTorch);
						}
					}
					cameraIndicator.setImageResource(R.drawable.indicator_camera_front);
				} else {
					app.setCurrentCameraId(VideOSCMainActivity.backsideCameraId);
					mToolsDrawerListState.put(SELECT_CAM, R.drawable.front_camera);
					if (TORCH != null)
						mToolsDrawerListState.put(TORCH, R.drawable.light_on);
//						Log.d(TAG, "new CameraId is back? " + (app.getCurrentCameraId() == VideOSCMainActivity.backsideCameraId) + ", R.drawable.back_camera: " + R.drawable.back_camera);
					img = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.front_camera);
					cameraIndicator.setImageResource(R.drawable.indicator_camera_back);
					if (app.getHasTorch() && TORCH != null) {
						ImageView torchSwitch = (ImageView) adapterView.getChildAt(TORCH);
						BitmapDrawable torchOn = (BitmapDrawable) ContextCompat.getDrawable(activityRef.get(), R.drawable.light_on);
						torchSwitch.setImageDrawable(torchOn);
					}
				}
				toolView.setImageDrawable(img);
				// invoke setting of new camera
				// camera ID should already have been set in currentCameraID
				cameraFragment.safeCameraOpenInView(activityRef.get().mCamView);
			} else if (position == INFO) {
				activityRef.get().closeColorModePanel();
				if (app.getIsFPSCalcPanelOpen())
					app.setIsFPSCalcPanelOpen(VideOSCUIHelpers.removeView(
							activityRef.get().mFrameRateCalculationPanel,
							(FrameLayout) activityRef.get().mCamView
					));
				else {
					VideOSCUIHelpers.setTransitionAnimation(activityRef.get().mFrameRateCalculationPanel);
					app.setIsFPSCalcPanelOpen(VideOSCUIHelpers.addView(
							activityRef.get().mFrameRateCalculationPanel,
							(FrameLayout) activityRef.get().mCamView
					));
				}
			} else if (position == SETTINGS) {
				activityRef.get().closeColorModePanel();
//				app.setSettingsLevel(1);
				activityRef.get().showBackButton();
				VideOSCSettingsListFragment settings = new VideOSCSettingsListFragment();
				if (fragmentManager.findFragmentByTag("settings selection") == null
						&& fragmentManager.findFragmentByTag("snapshot selection") == null)
					fragmentManager.beginTransaction().add(R.id.camera_preview, settings, "settings selection").commit();
			} else if (position == QUIT) {
				VideOSCDialogHelper.showQuitDialog(activityRef.get());
			}
			activityRef.get().mToolsDrawerLayout.closeDrawer(GravityCompat.END);
			// reset menu item background immediatly
			view.setBackgroundColor(0x00000000);
		}
	}
}
