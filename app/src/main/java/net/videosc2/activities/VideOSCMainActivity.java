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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
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
import android.widget.ScrollView;

import net.videosc2.R;
import net.videosc2.adapters.ToolsMenuAdapter;
import net.videosc2.fragments.VideOSCBaseFragment;
import net.videosc2.fragments.VideOSCCamera2Fragment;
import net.videosc2.fragments.VideOSCCameraFragment;
import net.videosc2.fragments.VideOSCSettingsFragment;
import net.videosc2.utilities.VideOSCDialogHelper;
import net.videosc2.utilities.VideOSCUIHelpers;
import net.videosc2.utilities.enums.GestureModes;
import net.videosc2.utilities.enums.InteractionModes;
import net.videosc2.utilities.enums.RGBModes;
import net.videosc2.utilities.enums.RGBToolbarStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ketai.net.KetaiNet;

/**
 * Created by Stefan Nussbaumer on 2017-03-15.
 */
public class VideOSCMainActivity extends AppCompatActivity
		implements VideOSCBaseFragment.OnFragmentInteractionListener {

	static final String TAG = "VideOSCMainActivity";

	private View mCamView;
	public static Point dimensions;
	private DrawerLayout mToolsDrawerLayout;

	// is device currently sending OSC?
	public boolean isPlaying = false;
	// is flashlight on?
	public boolean isTorchOn = false;
	// don't create more than one color mode panel
	private boolean isColorModePanelOpen = false;
	// is the frame rate / calculation period panel currently open?
	private boolean isFPSCalcPanelOpen = false;

	public Fragment mCameraPreview;
	Camera mCamera;
	// ID of currently opened camera
	public static int backsideCameraId;
	public static int frontsideCameraId;
	public static int currentCameraID;

	private View mIndicatorPanel;

	// the current color mode
	public Enum colorChannel = RGBModes.ALL;
	// RGB or RGB inverted?
	public boolean isRGBPositive = true;
	// set to true when isRGBPositive changes
	private boolean rgbHasChanged = false;
	// the current interaction mode
	public Enum interactionMode = InteractionModes.BASIC;
	// the current gesture mode
	public Enum gestureMode = GestureModes.SWAP;

	// toolbar status
	public Enum colorModeToolsDrawer = RGBToolbarStatus.RGB;

	// settings
//	public static boolean isSettingsFirstLevel = false;
//	public static boolean isSettingsSecondLevel = false;
	// levels within settings dialog
	// 0: no dialog, normal mode
	// 1: first level - selections 'network settings', 'resolution settings', 'sensor settings', 'about'
	// 2: editor setting details
	private int settingsLevel = 0;

	// pop-out menu for setting color mode
	private ViewGroup modePanel;
	// panel for displaying frame rate, calculation period
	private ViewGroup frameRateCalculationPanel;
	// the settings list
	private ViewGroup settingsList;

	// drawer menu
	private int START_STOP, TORCH, COLOR_MODE, INTERACTION, SELECT_CAM, INFO, SETTINGS, QUIT;

	// reflection method from VideOSCCameraFragment
	private Method safeCameraOpenInView;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate");

		// FIXME: preliminary
		final boolean hasTorch;
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
		hasTorch = VideOSCUIHelpers.hasTorch();
		backsideCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		if (VideOSCUIHelpers.hasFrontsideCamera()) {
			frontsideCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		}
		currentCameraID = backsideCameraId;
//		else
//			hasTorch = false;

		final LayoutInflater inflater = getLayoutInflater();
		final Activity activity = this;

		Log.d(TAG, "backside camera: " + Camera.CameraInfo.CAMERA_FACING_BACK);
		Log.d(TAG, "frontside camera: " + Camera.CameraInfo.CAMERA_FACING_FRONT);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		final FragmentManager fragmentManager = getFragmentManager();
		if (findViewById(R.id.camera_preview) != null) {
			mCamView = findViewById(R.id.camera_preview);

			if (savedInstanceState != null) return;
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			mCameraPreview = new VideOSCCameraFragment();
//			else
//				mCameraPreview = new VideOSCCamera2Fragment();

			fragmentManager.beginTransaction()
					.replace(R.id.camera_preview, mCameraPreview, "CamPreview")
					.commit();
		}

		// get safeCameraOpenInView(View view) from VideOSCCameraFragment for switching cameras
		try {
			Class[] lArg = new Class[1];
			lArg[0] = View.class;
			safeCameraOpenInView = mCameraPreview.getClass().getMethod("safeCameraOpenInView", lArg);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}


		int indicatorXMLiD = hasTorch ? R.layout.indicator_panel : R.layout.indicator_panel_no_torch;
		mIndicatorPanel = inflater.inflate(indicatorXMLiD, (FrameLayout) mCamView, true);

		// does the device have an inbuilt flash light?
		int drawerIconsIds = hasTorch ? R.array.drawer_icons : R.array.drawer_icons_no_torch;

		TypedArray tools = getResources().obtainTypedArray(drawerIconsIds);
//		Log.d(TAG, "tools: " + tools.getClass());
		mToolsDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mToolsDrawerLayout.setScrimColor(Color.TRANSPARENT);

		final ListView toolsDrawerList = (ListView) findViewById(R.id.drawer);

		List<BitmapDrawable> toolsList = new ArrayList<>();
		for (int i = 0; i < tools.length(); i++) {
			toolsList.add((BitmapDrawable) tools.getDrawable(i));
		}

		toolsDrawerList.setAdapter(new ToolsMenuAdapter(this, R.layout.drawer_item, R.id.tool, toolsList));
		tools.recycle();

		modePanel = (ViewGroup) inflater.inflate(R.layout.color_mode_panel, (FrameLayout) mCamView, false);
		frameRateCalculationPanel = (ViewGroup) inflater.inflate(R.layout.framerate_calculation_indicator, (FrameLayout) mCamView, false);

		// get keys for toolsDrawer
		HashMap<String, Integer> toolsDrawerKeys = toolsDrawerKeys();
		START_STOP = toolsDrawerKeys.get("startStop");
		if (toolsDrawerKeys.containsKey("torch"))
			TORCH = toolsDrawerKeys.get("torch");
		COLOR_MODE = toolsDrawerKeys.get("modeSelect");
		INTERACTION = toolsDrawerKeys.get("interactionMode");
		if (toolsDrawerKeys.containsKey("camSelect"))
			SELECT_CAM = toolsDrawerKeys.get("camSelect");
		INFO = toolsDrawerKeys.get("info");
		SETTINGS = toolsDrawerKeys.get("prefs");
		QUIT = toolsDrawerKeys.get("quit");

//		toolsDrawerList.getChildAt()
//		toolsDrawerKeys.get(0)

		toolsDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
				BitmapDrawable img;
				final ImageView oscIndicatorView = (ImageView) findViewById(R.id.indicator_osc);
				final ImageView torchIndicatorView = (ImageView) findViewById(R.id.torch_status_indicator);
				final ImageView imgView = (ImageView) view.findViewById(R.id.tool);
				Context context = getApplicationContext();


				// we can not use 'mCameraPreview' to retrieve the 'mCamera' object
				// FIXME: just for now deactivate for LOLLIPOP and up
//				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				VideOSCCameraFragment camPreview = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
				mCamera = camPreview.mCamera;
				Camera.Parameters cParameters = mCamera.getParameters();
//				}

				if (i == START_STOP) {
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) mCamView);;
					isPlaying = !isPlaying;
					if (isPlaying) {
						// TODO: stop sending OSC
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.stop);
						oscIndicatorView.setImageResource(R.drawable.osc_playing);
					} else {
						// TODO: start sending OSC
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.start);
						oscIndicatorView.setImageResource(R.drawable.osc_paused);
					}
					imgView.setImageDrawable(img);
				} else if (i == TORCH) {
					Log.d(TAG, "camera: " + mCamera);
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) mCamView);;
					if (mCamera != null && currentCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
						String flashMode = cParameters.getFlashMode();
						Log.d(TAG, "flash mode: " + flashMode);
						isTorchOn = !isTorchOn;
						if (!flashMode.equals("torch")) {
							cParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
							img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.light_on);
							torchIndicatorView.setImageResource(R.drawable.light_on_indicator);
						} else {
							cParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.light);
							torchIndicatorView.setImageResource(R.drawable.light_off_indicator);
						}
						mCamera.setParameters(cParameters);
						imgView.setImageDrawable(img);
					}
				} else if (i == COLOR_MODE) {
					if (!isColorModePanelOpen) {
						int y = (int) view.getY();

						VideOSCUIHelpers.setTransitionAnimation(modePanel);

						isColorModePanelOpen = VideOSCUIHelpers.addView(modePanel, (FrameLayout) mCamView);

						if (rgbHasChanged) {
							ImageView red = (ImageView) findViewById(R.id.mode_r);
							ImageView green = (ImageView) findViewById(R.id.mode_g);
							ImageView blue = (ImageView) findViewById(R.id.mode_b);
							int redRes = isRGBPositive ? R.drawable.r : R.drawable.r_inv;
							int greenRes = isRGBPositive ? R.drawable.g : R.drawable.g_inv;
							int blueRes = isRGBPositive ? R.drawable.b : R.drawable.b_inv;
							red.setImageResource(redRes);
							green.setImageResource(greenRes);
							blue.setImageResource(blueRes);
						}

						final View modePanelInner = modePanel.findViewById(R.id.color_mode_panel);

						VideOSCUIHelpers.setMargins(modePanelInner, 0, y, 60, 0);

						for (int k = 0; k < ((ViewGroup) modePanelInner).getChildCount(); k++) {
							final Context iContext = context;

							View button = ((ViewGroup) modePanelInner).getChildAt(k);
							button.setFocusableInTouchMode(true);
							button.setOnTouchListener(new View.OnTouchListener() {
								@Override
								public boolean onTouch(View view, MotionEvent motionEvent) {
									if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
										switch (view.getId()) {
											case R.id.mode_rgb:
												if (!isRGBPositive) {
													isRGBPositive = true;
													rgbHasChanged = true;
												}
												imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.rgb));
												oscIndicatorView.setImageResource(R.drawable.rgb_indicator);
												colorModeToolsDrawer = RGBToolbarStatus.RGB;
												break;
											case R.id.mode_rgb_inv:
												if (isRGBPositive) {
													isRGBPositive = false;
													rgbHasChanged = true;
												}
												imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.rgb_inv));
												oscIndicatorView.setImageResource(R.drawable.rgb_inv_indicator);
												colorModeToolsDrawer = RGBToolbarStatus.RGB_INV;
												break;
											case R.id.mode_r:
												if (isRGBPositive) {
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.r));
													colorModeToolsDrawer = RGBToolbarStatus.R;
												} else {
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.r_inv));
													colorModeToolsDrawer = RGBToolbarStatus.R_INV;
												}
												break;
											case R.id.mode_g:
												Log.d(TAG, "green");
												if (isRGBPositive) {
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.g));
													colorModeToolsDrawer = RGBToolbarStatus.G;
												} else {
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.g_inv));
													colorModeToolsDrawer = RGBToolbarStatus.G_INV;
												}
												break;
											case R.id.mode_b:
												Log.d(TAG, "blue");
												if (isRGBPositive) {
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.b));
													colorModeToolsDrawer = RGBToolbarStatus.B;
												} else {
													imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.b_inv));
													colorModeToolsDrawer = RGBToolbarStatus.B_INV;
												}
												break;
											default:
												imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.rgb));
												colorModeToolsDrawer = RGBToolbarStatus.RGB;
										}
										view.clearFocus();
										VideOSCUIHelpers.removeView(modePanel, (FrameLayout) mCamView);
										isColorModePanelOpen = false;
									}
									return false;
								}
							});
						}
					}
				} else if (i == INTERACTION) {
					Log.d(TAG, "set interaction mode");
					if (isColorModePanelOpen)
						isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) mCamView);
					if (interactionMode.equals(InteractionModes.BASIC)) {
						interactionMode = InteractionModes.SINGLE_PIXEL;
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.interactionplus);
						oscIndicatorView.setImageResource(R.drawable.interaction_plus_indicator);
					} else if (interactionMode.equals(InteractionModes.SINGLE_PIXEL)) {
						interactionMode = InteractionModes.BASIC;
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.interaction);
						oscIndicatorView.setImageResource(R.drawable.interaction_none_indicator);
					} else {
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.interaction);
					}
					imgView.setImageDrawable(img);
				} else if (i == SELECT_CAM) {
					Log.d(TAG, "switch camera");
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) mCamView);

					// FIXME
					if (currentCameraID == backsideCameraId) {
						currentCameraID = frontsideCameraId;
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.front_camera);
						if (hasTorch && cParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
							cParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							BitmapDrawable torchImg = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.light);
							torchIndicatorView.setImageResource(R.drawable.light_off_indicator);
							ImageView torchSwitch = (ImageView) adapterView.getChildAt(TORCH);
							torchSwitch.setImageDrawable(torchImg);
						}
					} else {
						currentCameraID = backsideCameraId;
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.back_camera);
					}
					imgView.setImageDrawable(img);
					// invoke setting of new camera
					// camera ID should already have been set in currentCameraID
					try {
						safeCameraOpenInView.invoke(mCameraPreview, mCamView);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				} else if (i == INFO) {
					Log.d(TAG, "framerate, calculation period info");
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) mCamView);
					if (isFPSCalcPanelOpen) {
						VideOSCUIHelpers.removeView(frameRateCalculationPanel, (FrameLayout) mCamView);
						isFPSCalcPanelOpen = false;
					} else {
						VideOSCUIHelpers.setTransitionAnimation(frameRateCalculationPanel);
						VideOSCUIHelpers.addView(frameRateCalculationPanel, (FrameLayout) mCamView);
						isFPSCalcPanelOpen = true;
					}
				} else if (i == SETTINGS) {
					Log.d(TAG, "settings");
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) mCamView);
					setSettingsLevel(1);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						mCamView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
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
		if (getSettingsLevel() < 1)
			mCamView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		mToolsDrawerLayout.openDrawer(Gravity.END);

		DisplayMetrics dm = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(dm);
		// width/height of the screen
		dimensions = new Point(dm.widthPixels, dm.heightPixels);
	}

/*
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return !(isColorModePanelOpen && event.getAction() == MotionEvent.ACTION_UP) && super.dispatchTouchEvent(event);
	}
*/

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		Log.d(TAG, "onContentChanged");
	}

	// There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		ImageButton menuButton = (ImageButton) findViewById(R.id.show_menu);
		menuButton.bringToFront();
		menuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mToolsDrawerLayout.isDrawerOpen(Gravity.END))
					mToolsDrawerLayout.openDrawer(Gravity.END);
				if (isColorModePanelOpen)
					isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) mCamView);
			}
		});

		View indicatorPanelInner = mIndicatorPanel.findViewById(R.id.indicator_panel);
		indicatorPanelInner.bringToFront();

		Settings.System.putInt(this.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, 20);

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = 1;// 100 / 100.0f;
		getWindow().setAttributes(lp);

		// maybe needed on devices other than Google Nexus?
		// startActivity(new Intent(this, RefreshScreen.class));
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		VideOSCUIHelpers.resetSystemUIState(mCamView);
	}

	@Override
	public void onBackPressed() {
		View bg = findViewById(R.id.settings_background);
		switch (settingsLevel) {
			case 1:
				VideOSCUIHelpers.removeView(findViewById(R.id.settings_selection), (FrameLayout) mCamView);
				VideOSCUIHelpers.removeView(bg, (FrameLayout) mCamView);
				VideOSCUIHelpers.resetSystemUIState(mCamView);
				mToolsDrawerLayout.closeDrawer(Gravity.END);
				setSettingsLevel(0);
				break;
			case 2:
				findViewById(R.id.settings_selection_list).setVisibility(View.VISIBLE);
				VideOSCUIHelpers.removeView(findViewById(R.id.network_settings), (ViewGroup) bg);
				VideOSCUIHelpers.removeView(findViewById(R.id.resolution_settings), (ViewGroup) bg);
				VideOSCUIHelpers.removeView(findViewById(R.id.sensor_settings), (ViewGroup) bg);
				VideOSCUIHelpers.removeView(findViewById(R.id.about), (ViewGroup) bg);
				setSettingsLevel(1);
				break;
			default:
				VideOSCDialogHelper.showQuitDialog(this);
		}
	}

	public int getSettingsLevel() {
		return settingsLevel;
	}

	public void setSettingsLevel(Integer level) {
		this.settingsLevel = level;
	}

	private HashMap<String, Integer> toolsDrawerKeys() {
		HashMap<String, Integer> toolsDrawerKeys = new HashMap<>();
		int index = 0;

		toolsDrawerKeys.put("startStop", index);
		if (VideOSCUIHelpers.hasTorch())
			toolsDrawerKeys.put("torch", ++index);
		toolsDrawerKeys.put("modeSelect", ++index);
		toolsDrawerKeys.put("interactionMode", ++index);
		if (VideOSCUIHelpers.hasFrontsideCamera())
			toolsDrawerKeys.put("camSelect", ++index);
		toolsDrawerKeys.put("info", ++index);
		toolsDrawerKeys.put("prefs", ++index);
		toolsDrawerKeys.put("quit", ++index);

		return toolsDrawerKeys;
	}

/*
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "activity on destroy");
		// reset static colorModeToolsDrawer enum,
		// otherwise app will restart with status as set when app was quit
		colorModeToolsDrawer = RGBToolbarStatus.RGB;
	}
*/

	public Enum getColorModeToolsDrawer() {
		return this.colorModeToolsDrawer;
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "main activity on pause");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "main activity on resume!");
	}

/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "menu item: " + item.getItemId());
		switch(item.getItemId()) {
			case android.R.id.home:
				mDrawer.openDrawer(GravityCompat.END);
				return true;
		}

		return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}
*/

/*
	private ActionBarDrawerToggle setupDrawerToggle() {
		// NOTE: Make sure you pass in a valid toolbar reference.
		// ActionBarDrawToggle() does not require it
		// and will not render the hamburger icon without it.
		return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
	}
*/

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
//		drawerToggle.onConfigurationChanged(newConfig);
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
