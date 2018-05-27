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

package net.videosc2.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.adapters.ToolsMenuAdapter;
import net.videosc2.db.SettingsContract;
import net.videosc2.db.SettingsDBHelper;
import net.videosc2.fragments.VideOSCBaseFragment;
import net.videosc2.fragments.VideOSCCameraFragment;
import net.videosc2.fragments.VideOSCMultiSliderFragment;
import net.videosc2.fragments.VideOSCSettingsFragment;
import net.videosc2.utilities.VideOSCDialogHelper;
import net.videosc2.utilities.VideOSCUIHelpers;
import net.videosc2.utilities.enums.GestureModes;
import net.videosc2.utilities.enums.InteractionModes;
import net.videosc2.utilities.enums.RGBModes;
import net.videosc2.utilities.enums.RGBToolbarStatus;
import net.videosc2.views.SliderBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stefan Nussbaumer on 2017-03-15.
 */
public class VideOSCMainActivity extends AppCompatActivity
		implements VideOSCBaseFragment.OnFragmentInteractionListener {

	static final String TAG = "VideOSCMainActivity";

	private View mCamView;
	private Point mDimensions;
	private DrawerLayout mToolsDrawerLayout;

	// is flashlight on?
	public boolean isTorchOn = false;
	// don't create more than one color mode panel
	private boolean isColorModePanelOpen = false;
	// is the frame rate / calculation period panel currently open?
	private boolean isFPSCalcPanelOpen = false;
	// is the multisliderview currently visible?
	private boolean isMultiSliderVisible = false;

	private Fragment mCameraPreview;
	private Fragment mMultiSliderView;
	// ID of currently opened camera
	public static int backsideCameraId;
	public static int frontsideCameraId;
	public static int currentCameraID;

	private View mIndicatorPanel;

	// the global application, used to exchange various temporary data
	private VideOSCApplication mApp;

	// the current color mode
	// RGB or RGB inverted?
	// set to true when isRGBPositive changes
	private boolean rgbHasChanged = false;
	// the current interaction mode
	public Enum mInteractionMode = InteractionModes.BASIC;
	// the current gesture mode
	public Enum gestureMode = GestureModes.SWAP;

	// ListView for the tools drawer
	private List<BitmapDrawable> mToolsList = new ArrayList<>();
	private ListView mToolsDrawerList;
	private HashMap<Integer, Integer> mToolsDrawerListState = new HashMap<>();
	// toolbar status
	public Enum mColorModeToolsDrawer = RGBToolbarStatus.RGB;

	// pop-out menu for setting color mode
	private ViewGroup mModePanel;
	// panel for displaying frame rate, calculation period
	private ViewGroup mFrameRateCalculationPanel;
	// the settings list
	private ViewGroup settingsList;
	// multislider view
//	private ViewGroup mMultiSliderView;

	// drawer menu
	private int START_STOP, TORCH, COLOR_MODE, INTERACTION, SELECT_CAM, INFO, SETTINGS, QUIT;

	// settings, retrieved from sqlite db
	public SettingsDBHelper mDbHelper;

	Intent starterIntent;
	private static final int CODE_WRITE_SETTINGS_PERMISSION = 111;
	private static final int PERMISSION_REQUEST_CAMERA = 1;

	private View mDecorView;
	private boolean hasTorch;

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
		backsideCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		if (VideOSCUIHelpers.hasFrontsideCamera()) {
			frontsideCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		}
		currentCameraID = backsideCameraId;

		final float scale = getResources().getDisplayMetrics().density;
		mApp.setScreenDensity(scale);

		// keep db access open through the app's lifetime
		mDbHelper = new SettingsDBHelper(this);
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		final String[] settingsFields = new String[]{
				SettingsContract.AddressSettingsEntry.IP_ADDRESS,
				SettingsContract.AddressSettingsEntry.PORT
		};

		Cursor cursor = db.query(
				SettingsContract.AddressSettingsEntry.TABLE_NAME,
				settingsFields,
				null,
				null,
				null,
				null,
				null
		);

		// for now we only have one address stored in the addresses table
		// protocol will be UDP
		if (cursor.moveToFirst()) {
			mApp.mOscHelper.setBroadcastAddr(
					cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.IP_ADDRESS)),
					cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.PORT))
			);
		}

		cursor.close();

		final LayoutInflater inflater = getLayoutInflater();

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		View mainLayout = inflater.inflate(R.layout.activity_main, null);
		setContentView(mainLayout);

		mCamView = mainLayout.findViewById(R.id.camera_preview);
		mApp.setCamView((FrameLayout) mCamView);

		// maybe needed on devices other than Google Nexus?
		// startActivity(new Intent(this, RefreshScreen.class));

		final FragmentManager fragmentManager = getFragmentManager();

		if (savedInstanceState != null) return;

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			mCameraPreview = new VideOSCCameraFragment();
			fragmentManager.beginTransaction()
					.replace(R.id.camera_preview, mCameraPreview, "CamPreview")
					.commit();
			buildUI();
		} else {
			requestCameraPermission();
		}
	}

	private void buildUI() {
		final LayoutInflater inflater = getLayoutInflater();
		final FragmentManager fragmentManager = getFragmentManager();
		final Activity activity = this;

		// does the device have an inbuilt flashlight? frontside camera? flashlight but no frontside camera
		// frontside camer but no flashlight?...
		int drawerIconsIds = hasTorch ?
				VideOSCUIHelpers.hasFrontsideCamera() ? R.array.drawer_icons : R.array.drawer_icons_no_frontside_cam :
				VideOSCUIHelpers.hasFrontsideCamera() ? R.array.drawer_icons_no_torch : R.array.drawer_icons_no_torch_no_frontside_cam;

		TypedArray tools = getResources().obtainTypedArray(drawerIconsIds);
//		Log.d(TAG, "tools: " + tools.getClass());
		mToolsDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mToolsDrawerLayout.setScrimColor(Color.TRANSPARENT);

		mToolsDrawerList = (ListView) findViewById(R.id.drawer);

		for (int i = 0; i < tools.length(); i++) {
			mToolsList.add((BitmapDrawable) tools.getDrawable(i));
		}

		mToolsDrawerList.setAdapter(new ToolsMenuAdapter(this, R.layout.drawer_item, R.id.tool, mToolsList));
		tools.recycle();

		mModePanel = (ViewGroup) inflater.inflate(R.layout.color_mode_panel, (FrameLayout) mCamView, false);
		mApp.setColorModePanel(mModePanel);
//		mMultiSliderView = (ViewGroup) inflater.inflate(R.layout.multislider_view, (FrameLayout) mCamView, false);
		mFrameRateCalculationPanel = (ViewGroup) inflater.inflate(R.layout.framerate_calculation_indicator, (FrameLayout) mCamView, false);

		// get keys for toolsDrawer
		HashMap<String, Integer> toolsDrawerKeys = toolsDrawerKeys();
		START_STOP = toolsDrawerKeys.get("startStop");
		if (toolsDrawerKeys.containsKey("torch"))
			TORCH = toolsDrawerKeys.get("torch");
		COLOR_MODE = toolsDrawerKeys.get("modeSelect");
		INTERACTION = toolsDrawerKeys.get("mInteractionMode");
		if (toolsDrawerKeys.containsKey("camSelect"))
			SELECT_CAM = toolsDrawerKeys.get("camSelect");
		INFO = toolsDrawerKeys.get("info");
		SETTINGS = toolsDrawerKeys.get("prefs");
		QUIT = toolsDrawerKeys.get("quit");

		mToolsDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
				BitmapDrawable img;
				final ImageView oscIndicatorView = (ImageView) findViewById(R.id.indicator_osc);
				final ImageView rgbModeIndicator = (ImageView) findViewById(R.id.indicator_color);
				final ImageView interactionModeIndicator = (ImageView) findViewById(R.id.indicator_interaction);
				final ImageView cameraIndicator = (ImageView) findViewById(R.id.indicator_camera);
				final ImageView torchIndicatorView = (ImageView) findViewById(R.id.torch_status_indicator);
				final ImageView imgView = (ImageView) view.findViewById(R.id.tool);
				Context context = getApplicationContext();
				// cameraView provides all instance methods of the camera preview
				// no reflections needed
				VideOSCCameraFragment cameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
				Camera camera = cameraView.mCamera;
				Camera.Parameters cameraParameters;

				if (i == START_STOP) {
					if (isColorModePanelOpen) {
						isColorModePanelOpen = VideOSCUIHelpers.removeView(mModePanel, (FrameLayout) mCamView);
						mApp.setIsColorModePanelOpen(isColorModePanelOpen);
					}
					if (!mApp.getPlay()) {
						mApp.setPlay(true);
						mToolsDrawerListState.put(START_STOP, R.drawable.stop);
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.stop);
						oscIndicatorView.setImageResource(R.drawable.osc_playing);
					} else {
						mApp.setPlay(false);
						mToolsDrawerListState.put(START_STOP, R.drawable.start);
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.start);
						oscIndicatorView.setImageResource(R.drawable.osc_paused);
					}
					imgView.setImageDrawable(img);
				} else if (i == TORCH) {
					if (isColorModePanelOpen) {
						isColorModePanelOpen = VideOSCUIHelpers.removeView(mModePanel, (FrameLayout) mCamView);
						mApp.setIsColorModePanelOpen(isColorModePanelOpen);
					}
					cameraParameters = camera.getParameters();
					if (currentCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
						String flashMode = cameraParameters.getFlashMode();
						isTorchOn = !isTorchOn;
						if (!flashMode.equals("torch")) {
							cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
							mToolsDrawerListState.put(TORCH, R.drawable.light_on);
							img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.light_on);
							torchIndicatorView.setImageResource(R.drawable.light_on_indicator);
						} else {
							cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							mToolsDrawerListState.put(TORCH, R.drawable.light);
							img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.light);
							torchIndicatorView.setImageResource(R.drawable.light_off_indicator);
						}
						camera.setParameters(cameraParameters);
						imgView.setImageDrawable(img);
					}

				} else if (i == COLOR_MODE) {
					if (!isColorModePanelOpen) {
						int y = (int) view.getY();

						VideOSCUIHelpers.setTransitionAnimation(mModePanel);

						isColorModePanelOpen = VideOSCUIHelpers.addView(mModePanel, (FrameLayout) mCamView);
						mApp.setIsColorModePanelOpen(isColorModePanelOpen);

						if (rgbHasChanged) {
							ImageView red = (ImageView) findViewById(R.id.mode_r);
							ImageView green = (ImageView) findViewById(R.id.mode_g);
							ImageView blue = (ImageView) findViewById(R.id.mode_b);
							int redRes = mApp.getIsRGBPositive() ? R.drawable.r : R.drawable.r_inv;
							int greenRes = mApp.getIsRGBPositive() ? R.drawable.g : R.drawable.g_inv;
							int blueRes = mApp.getIsRGBPositive() ? R.drawable.b : R.drawable.b_inv;
							red.setImageResource(redRes);
							green.setImageResource(greenRes);
							blue.setImageResource(blueRes);
						}

						final View modePanelInner = mModePanel.findViewById(R.id.color_mode_panel);

						VideOSCUIHelpers.setMargins(modePanelInner, 0, y, 60, 0);

						for (int k = 0; k < ((ViewGroup) modePanelInner).getChildCount(); k++) {
							final Context iContext = context;

							View button = ((ViewGroup) modePanelInner).getChildAt(k);
							button.setFocusableInTouchMode(true);
							button.setOnTouchListener(new View.OnTouchListener() {
								@Override
								public boolean onTouch(View view, MotionEvent motionEvent) {
									view.performClick();
									if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
										switch (view.getId()) {
											case R.id.mode_rgb:
												mApp.setColorMode(RGBModes.RGB);
												if (!mApp.getIsRGBPositive()) {
													mApp.setIsRGBPositive(true);
													rgbHasChanged = true;
												}
												mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb);
												imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.rgb));
												rgbModeIndicator.setImageResource(R.drawable.rgb_indicator);
												mColorModeToolsDrawer = RGBToolbarStatus.RGB;
												break;
											case R.id.mode_rgb_inv:
												mApp.setColorMode(RGBModes.RGB);
												if (mApp.getIsRGBPositive()) {
													mApp.setIsRGBPositive(false);
													rgbHasChanged = true;
												}
												mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb_inv);
												imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.rgb_inv));
												rgbModeIndicator.setImageResource(R.drawable.rgb_inv_indicator);
												mColorModeToolsDrawer = RGBToolbarStatus.RGB_INV;
												break;
											case R.id.mode_r:
												mApp.setColorMode(RGBModes.R);
												if (mApp.getIsRGBPositive()) {
													mToolsDrawerListState.put(COLOR_MODE, R.drawable.r);
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.r));
													mColorModeToolsDrawer = RGBToolbarStatus.R;
												} else {
													mToolsDrawerListState.put(COLOR_MODE, R.drawable.r_inv);
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.r_inv));
													mColorModeToolsDrawer = RGBToolbarStatus.R_INV;
												}
												break;
											case R.id.mode_g:
//												Log.d(TAG, "green");
												mApp.setColorMode(RGBModes.G);
												if (mApp.getIsRGBPositive()) {
													mToolsDrawerListState.put(COLOR_MODE, R.drawable.g);
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.g));
													mColorModeToolsDrawer = RGBToolbarStatus.G;
												} else {
													mToolsDrawerListState.put(COLOR_MODE, R.drawable.g_inv);
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.g_inv));
													mColorModeToolsDrawer = RGBToolbarStatus.G_INV;
												}
												break;
											case R.id.mode_b:
//												Log.d(TAG, "blue");
												mApp.setColorMode(RGBModes.B);
												if (mApp.getIsRGBPositive()) {
													mToolsDrawerListState.put(COLOR_MODE, R.drawable.b);
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.b));
													mColorModeToolsDrawer = RGBToolbarStatus.B;
												} else {
													mToolsDrawerListState.put(COLOR_MODE, R.drawable.b_inv);
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.b_inv));
													mColorModeToolsDrawer = RGBToolbarStatus.B_INV;
												}
												break;
											default:
												mApp.setColorMode(RGBModes.RGB);
												mToolsDrawerListState.put(COLOR_MODE, R.drawable.rgb);
												imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.rgb));
												mColorModeToolsDrawer = RGBToolbarStatus.RGB;
										}
										view.clearFocus();
										VideOSCUIHelpers.removeView(mModePanel, (FrameLayout) mCamView);
										isColorModePanelOpen = false;
										mApp.setIsColorModePanelOpen(isColorModePanelOpen);
									}
									return false;
								}
							});
						}
					}
				} else if (i == INTERACTION) {
					if (isColorModePanelOpen) {
						isColorModePanelOpen = VideOSCUIHelpers.removeView(mModePanel, (FrameLayout) mCamView);
						mApp.setIsColorModePanelOpen(isColorModePanelOpen);
					}
					if (mInteractionMode.equals(InteractionModes.BASIC)) {
						mInteractionMode = InteractionModes.SINGLE_PIXEL;
						mToolsDrawerListState.put(INTERACTION, R.drawable.interactionplus);
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.interactionplus);
						interactionModeIndicator.setImageResource(R.drawable.interaction_plus_indicator);
						mMultiSliderView = new VideOSCMultiSliderFragment();
						fragmentManager.beginTransaction()
								.add(R.id.camera_preview, mMultiSliderView, "MultiSliderView")
								.commit();
						VideOSCMultiSliderFragment multiSliderView = (VideOSCMultiSliderFragment) fragmentManager.findFragmentByTag("MultiSliderView");
						Bundle msArgsBundle = new Bundle();
						ArrayList<Integer> testList = new ArrayList<>(Arrays.asList(2, 4, 5, 7, 9, 12, 14, 15, 17, 19, 21, 22, 23, 30, 34, 37, 34));
						msArgsBundle.putIntegerArrayList("nums", testList);
						mMultiSliderView.setArguments(msArgsBundle);

						/* VideOSCUIHelpers.setTransitionAnimation(mMultiSliderView);
						isMultiSliderVisible = VideOSCUIHelpers.addView(mMultiSliderView, (FrameLayout) mCamView);
						SliderBar slider = new SliderBar(activity,20, 20, 200, mDimensions.y - 20, 20, "13");
						mMultiSliderView.addView(slider); */
					} else if (mInteractionMode.equals(InteractionModes.SINGLE_PIXEL)) {
						mInteractionMode = InteractionModes.BASIC;
						mToolsDrawerListState.put(INTERACTION, R.drawable.interaction);
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.interaction);
						interactionModeIndicator.setImageResource(R.drawable.interaction_none_indicator);
						if (mMultiSliderView != null)
							fragmentManager.beginTransaction().remove(mMultiSliderView).commit();
//						isMultiSliderVisible = VideOSCUIHelpers.removeView(mMultiSliderView, (FrameLayout) mCamView);
					} else {
						mToolsDrawerListState.put(INTERACTION, R.drawable.interaction);
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.interaction);
						if (mMultiSliderView != null)
							fragmentManager.beginTransaction().remove(mMultiSliderView).commit();
//						isMultiSliderVisible = VideOSCUIHelpers.removeView(mMultiSliderView, (FrameLayout) mCamView);
					}
					imgView.setImageDrawable(img);
				} else if (i == SELECT_CAM) {
					if (isColorModePanelOpen) {
						isColorModePanelOpen = VideOSCUIHelpers.removeView(mModePanel, (FrameLayout) mCamView);
						mApp.setIsColorModePanelOpen(isColorModePanelOpen);
					}
					cameraParameters = camera.getParameters();
					if (currentCameraID == backsideCameraId) {
						currentCameraID = frontsideCameraId;
						mToolsDrawerListState.put(SELECT_CAM, R.drawable.front_camera);
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.front_camera);
						if (hasTorch && cameraParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
							cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							BitmapDrawable torchImg = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.light);
							torchIndicatorView.setImageResource(R.drawable.light_off_indicator);
							ImageView torchSwitch = (ImageView) adapterView.getChildAt(TORCH);
							torchSwitch.setImageDrawable(torchImg);
						}
						cameraIndicator.setImageResource(R.drawable.indicator_camera_front);
					} else {
						currentCameraID = backsideCameraId;
						mToolsDrawerListState.put(SELECT_CAM, R.drawable.back_camera);
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.back_camera);
						cameraIndicator.setImageResource(R.drawable.indicator_camera_back);
					}
					imgView.setImageDrawable(img);
					// invoke setting of new camera
					// camera ID should already have been set in currentCameraID
					cameraView.safeCameraOpenInView(mCamView);
				} else if (i == INFO) {
					if (isColorModePanelOpen) {
						isColorModePanelOpen = VideOSCUIHelpers.removeView(mModePanel, (FrameLayout) mCamView);
						mApp.setIsColorModePanelOpen(isColorModePanelOpen);
					}
					if (isFPSCalcPanelOpen) {
						VideOSCUIHelpers.removeView(mFrameRateCalculationPanel, (FrameLayout) mCamView);
						isFPSCalcPanelOpen = false;
					} else {
						VideOSCUIHelpers.setTransitionAnimation(mFrameRateCalculationPanel);
						VideOSCUIHelpers.addView(mFrameRateCalculationPanel, (FrameLayout) mCamView);
						isFPSCalcPanelOpen = true;
					}
				} else if (i == SETTINGS) {
//					Log.d(TAG, "settings");
					if (isColorModePanelOpen) {
						isColorModePanelOpen = VideOSCUIHelpers.removeView(mModePanel, (FrameLayout) mCamView);
						mApp.setIsColorModePanelOpen(isColorModePanelOpen);
					}
					mApp.setSettingsLevel(1);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
					}
					VideOSCSettingsFragment settings = new VideOSCSettingsFragment();
					fragmentManager.beginTransaction().add(R.id.camera_preview, settings, "settings selection").commit();
				} else if (i == QUIT) {
					VideOSCDialogHelper.showQuitDialog(activity);
				}
				mToolsDrawerLayout.closeDrawer(Gravity.END);
				// reset menu item background immediatly
				view.setBackgroundColor(0x00000000);
			}
		});
		if (mApp.getSettingsLevel() < 1)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				Log.d(TAG, "KitKat or higher");
				mCamView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			} else
				mCamView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		mToolsDrawerLayout.openDrawer(Gravity.END);

		mDimensions = getAbsoluteScreenSize();
		mApp.setDimensions(mDimensions);

		ImageButton menuButton = (ImageButton) findViewById(R.id.show_menu);
		menuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mToolsDrawerLayout.isDrawerOpen(Gravity.END))
					mToolsDrawerLayout.openDrawer(Gravity.END);
				if (isColorModePanelOpen) {
					isColorModePanelOpen = VideOSCUIHelpers.removeView(mModePanel, (FrameLayout) mCamView);
					mApp.setIsColorModePanelOpen(isColorModePanelOpen);
				}
			}
		});

		int indicatorXMLiD = hasTorch ? R.layout.indicator_panel : R.layout.indicator_panel_no_torch;
		mIndicatorPanel = inflater.inflate(indicatorXMLiD, (FrameLayout) mCamView, true);
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
		View bg = findViewById(R.id.settings_background);
		short settingsLevel = mApp.getSettingsLevel();
		View networkSettingsDialog = findViewById(R.id.network_settings);
		View resolutionSettingsDialog = findViewById(R.id.resolution_settings);
		View sensorSettingsDialog = findViewById(R.id.sensor_settings);
		View debugSettingsDialog = findViewById(R.id.debug_settings);
		View about = findViewById(R.id.about);

		switch (settingsLevel) {
			case 1:
				FragmentManager manager = getFragmentManager();
				Fragment fragment = manager.findFragmentByTag("settings selection");
				VideOSCUIHelpers.removeView(findViewById(R.id.settings_selection), (FrameLayout) mCamView);
				VideOSCUIHelpers.removeView(bg, (FrameLayout) mCamView);
				VideOSCUIHelpers.resetSystemUIState(mDecorView);
				if (fragment != null)
					manager.beginTransaction().remove(fragment).commit();
				mToolsDrawerLayout.closeDrawer(Gravity.END);
				mApp.setSettingsLevel(0);
				break;
			case 2:
				findViewById(R.id.settings_selection_list).setVisibility(View.VISIBLE);
				if (networkSettingsDialog != null)
					VideOSCUIHelpers.removeView(networkSettingsDialog, (ViewGroup) bg);
				if (resolutionSettingsDialog != null)
					VideOSCUIHelpers.removeView(resolutionSettingsDialog, (ViewGroup) bg);
				if (sensorSettingsDialog != null)
					VideOSCUIHelpers.removeView(sensorSettingsDialog, (ViewGroup) bg);
				if (debugSettingsDialog != null)
					VideOSCUIHelpers.removeView(debugSettingsDialog, (ViewGroup) bg);
				if (about != null)
					VideOSCUIHelpers.removeView(about, (ViewGroup) bg);
				bg.setVisibility(View.VISIBLE);
				mApp.setSettingsLevel(1);
				break;
			case 3:
				View exposureSetters = findViewById(R.id.fix_exposure_button_layout);
				Switch exposureSwitch = (Switch) findViewById(R.id.fix_exposure_checkbox);
				// temporarily disable checked-change listener
				mApp.setBackPressed(true);
				exposureSwitch.setChecked(mApp.getExposureIsFixed());
				mApp.setBackPressed(false);
				if (exposureSetters != null)
					VideOSCUIHelpers.removeView(exposureSetters, (FrameLayout) mCamView);
				bg.setVisibility(View.VISIBLE);
				resolutionSettingsDialog.setVisibility(View.VISIBLE);
				mApp.setExposureIsFixed(false);
				mApp.setSettingsLevel(2);
				break;
			default:
				VideOSCDialogHelper.showQuitDialog(this);
		}
	}

	private HashMap<String, Integer> toolsDrawerKeys() {
		HashMap<String, Integer> toolsDrawerKeys = new HashMap<>();
		int index = 0;

		toolsDrawerKeys.put("startStop", index);
		if (hasTorch)
			toolsDrawerKeys.put("torch", ++index);
		toolsDrawerKeys.put("modeSelect", ++index);
		toolsDrawerKeys.put("mInteractionMode", ++index);
		Log.d(TAG, "has frontside camera: " + VideOSCUIHelpers.hasFrontsideCamera());
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
		mApp.setPlay(false);
		// reset inverted colors
		mApp.setIsRGBPositive(true);
		// reset debug settings
		mApp.setPixelImageHidden(false);
		VideOSCApplication.setDebugPixelOsc(false);
		mApp.setHasExposureSettingBeenCancelled(false);
		mApp.setExposureIsFixed(false);
		// close db
		mDbHelper.close();
		super.onDestroy();
	}

	public Enum getColorModeToolsDrawer() {
		return this.mColorModeToolsDrawer;
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "main activity on pause");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume called: " + mToolsDrawerList);
		// update tools drawer if some item's state has changed
		if (mToolsDrawerList != null) {
			for (Integer key : mToolsDrawerListState.keySet()) {
				mToolsList.set(key, (BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(), mToolsDrawerListState.get(key)));
			}
			mToolsDrawerList.setAdapter(new ToolsMenuAdapter(this, R.layout.drawer_item, R.id.tool, mToolsList));
		}
	}

	/* private void requestSettingsPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (!Settings.System.canWrite(this)) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
				intent.setData(Uri.parse("package:" + getPackageName()));
				startActivityForResult(intent, VideOSCMainActivity.CODE_WRITE_SETTINGS_PERMISSION);
			}
		}
	} */

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
		Camera camera = null;

		if(requestCode == PERMISSION_REQUEST_CAMERA) {
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.d(TAG, "onRequestPermissionsResult()");
				Snackbar.make(
						mCamView,
						R.string.camera_permissions_granted,
						Snackbar.LENGTH_SHORT
				).show();
				// make sure permissions are granted before we touch the camera

				try {
					camera = Camera.open();
				} catch(Exception e) {
					e.printStackTrace();
				}

				if (camera != null) {
					hasTorch = VideOSCUIHelpers.hasTorch(camera);
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

				// the camera fragment overlays all other screen elements
				// hence, we get gui elements to front in surfaceCreated() within CameraPreview (VideOSCCameraFragment)
				mCameraPreview = new VideOSCCameraFragment();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction()
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
			Log.d(TAG, "CODE_WRITE_SETTINGS_PERMISSION success");
			finish();
			startActivity(starterIntent);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}

	@Override
	public void onFragmentInteraction(String id) {

	}

	@Override
	public void onFragmentInteraction(int actionId) {

	}
}
