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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

import ketai.net.KetaiNet;

/**
 * Created by Stefan Nussbaumer on 2017-03-15.
 */
public class VideOSCMainActivity extends AppCompatActivity
		implements VideOSCBaseFragment.OnFragmentInteractionListener {

	static final String TAG = "VideOSCMainActivity";

	private View camView;
	public static Point dimensions;
	private DrawerLayout toolsDrawerLayout;

	// is device currently sending OSC?
	public boolean isPlaying = false;
	// is flashlight on?
	public boolean isTorchOn = false;
	// don't create more than one color mode panel
	private boolean isColorModePanelOpen = false;
	// is the frame rate / calculation period panel currently open?
	private boolean isFPSCalcPanelOpen = false;

	public Fragment cameraPreview;
	Camera camera;

	private View indicatorPanel;

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
	public static boolean isSettingsFirstLevel = false;
	public static boolean isSettingsSecondLevel = false;

	// pop-out menu for setting color mode
	private ViewGroup modePanel;
	// panel for displaying frame rate, calculation period
	private ViewGroup frameRateCalculationPanel;
	// the settings list
	private ViewGroup settingsList;

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
//		else
//			hasTorch = false;

		final LayoutInflater inflater = getLayoutInflater();
		final Activity activity = this;

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		final FragmentManager fragmentManager = getFragmentManager();
		if (findViewById(R.id.camera_preview) != null) {
			camView = findViewById(R.id.camera_preview);

			if (savedInstanceState != null) return;
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
				cameraPreview = new VideOSCCameraFragment();
//			else
//				cameraPreview = new VideOSCCamera2Fragment();

			fragmentManager.beginTransaction()
					.replace(R.id.camera_preview, cameraPreview, "CamPreview")
					.commit();
		}

		int indicatorXMLiD = hasTorch ? R.layout.indicator_panel : R.layout.indicator_panel_no_torch;
		indicatorPanel = inflater.inflate(indicatorXMLiD, (FrameLayout) camView, true);

		// does the device have an inbuilt flash light?
		int drawerIconsIds = hasTorch ? R.array.drawer_icons : R.array.drawer_icons_no_torch;

		TypedArray tools = getResources().obtainTypedArray(drawerIconsIds);
//		Log.d(TAG, "tools: " + tools.getClass());
		toolsDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		toolsDrawerLayout.setScrimColor(Color.TRANSPARENT);

		final ListView toolsDrawerList = (ListView) findViewById(R.id.drawer);

		List<BitmapDrawable> toolsList = new ArrayList<>();
		for (int i = 0; i < tools.length(); i++) {
			toolsList.add((BitmapDrawable) tools.getDrawable(i));
		}

		toolsDrawerList.setAdapter(new ToolsMenuAdapter(this, R.layout.drawer_item, R.id.tool, toolsList));
		tools.recycle();

		modePanel = (ViewGroup) inflater.inflate(R.layout.color_mode_panel, (FrameLayout) camView, false);
		frameRateCalculationPanel = (ViewGroup) inflater.inflate(R.layout.framerate_calculation_indicator, (FrameLayout) camView, false);

		toolsDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
				BitmapDrawable img;
				final ImageView indicatorView;
				final ImageView imgView = (ImageView) view.findViewById(R.id.tool);
				Context context = getApplicationContext();
				// we can not use 'cameraPreview' to retrieve the 'mCamera' object
				// FIXME: just for now deactivate for LOLLIPOP and up
//				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					VideOSCCameraFragment camPreview = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
					camera = camPreview.mCamera;
//				}

				if (i == 0) {
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) camView);;
					indicatorView = (ImageView) findViewById(R.id.indicator_osc);
					isPlaying = !isPlaying;
					if (isPlaying) {
						// TODO: stop sending OSC
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.stop);
						indicatorView.setImageResource(R.drawable.osc_playing);
					} else {
						// TODO: start sending OSC
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.start);
						indicatorView.setImageResource(R.drawable.osc_paused);
					}
					imgView.setImageDrawable(img);
				} else if (i == 1 && hasTorch) {
					Log.d(TAG, "camera: " + camera);
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) camView);;
					if (camera != null) {
						Camera.Parameters cParameters = camera.getParameters();
						String flashMode = cParameters.getFlashMode();
						Log.d(TAG, "flash mode: " + flashMode);
						indicatorView = (ImageView) findViewById(R.id.torch_status_indicator);
						isTorchOn = !isTorchOn;
						if (!flashMode.equals("torch")) {
							cParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
							img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.light_on);
							indicatorView.setImageResource(R.drawable.light_on_indicator);
						} else {
							cParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.light);
							indicatorView.setImageResource(R.drawable.light_off_indicator);
						}
						camera.setParameters(cParameters);
						imgView.setImageDrawable(img);
					}
				} else if ((i == 2 && hasTorch) || i == 1) {
					if (!isColorModePanelOpen) {
						int y = (int) view.getY();

						VideOSCUIHelpers.setTransitionAnimation(modePanel);

						isColorModePanelOpen = VideOSCUIHelpers.addView(modePanel, (FrameLayout) camView);

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

						indicatorView = (ImageView) findViewById(R.id.indicator_color);
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
												indicatorView.setImageResource(R.drawable.rgb_indicator);
												colorModeToolsDrawer = RGBToolbarStatus.RGB;
												break;
											case R.id.mode_rgb_inv:
												if (isRGBPositive) {
													isRGBPositive = false;
													rgbHasChanged = true;
												}
												imgView.setImageDrawable(ContextCompat.getDrawable(iContext, R.drawable.rgb_inv));
												indicatorView.setImageResource(R.drawable.rgb_inv_indicator);
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
										VideOSCUIHelpers.removeView(modePanel, (FrameLayout) camView);
										isColorModePanelOpen = false;
									}
									return false;
								}
							});
						}
					}
				} else if ((i == 3 && hasTorch) || i == 2) {
					Log.d(TAG, "set interaction mode");
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) camView);;
					indicatorView = (ImageView) findViewById(R.id.indicator_interaction);
					if (interactionMode.equals(InteractionModes.BASIC)) {
						interactionMode = InteractionModes.SINGLE_PIXEL;
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.interactionplus);
						indicatorView.setImageResource(R.drawable.interaction_plus_indicator);
					} else if (interactionMode.equals(InteractionModes.SINGLE_PIXEL)) {
						interactionMode = InteractionModes.BASIC;
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.interaction);
						indicatorView.setImageResource(R.drawable.interaction_none_indicator);
					} else {
						img = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.interaction);
					}
					imgView.setImageDrawable(img);
				} else if ((i == 4 && hasTorch) || i == 3) {
					Log.d(TAG, "framerate, calculation period info");
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) camView);
					if (isFPSCalcPanelOpen) {
						VideOSCUIHelpers.removeView(frameRateCalculationPanel, (FrameLayout) camView);
						isFPSCalcPanelOpen = false;
					} else {
						VideOSCUIHelpers.setTransitionAnimation(frameRateCalculationPanel);
						VideOSCUIHelpers.addView(frameRateCalculationPanel, (FrameLayout) camView);
						isFPSCalcPanelOpen = true;
					}
				} else if ((i == 5 && hasTorch) || i == 4) {
					Log.d(TAG, "settings");
					if (isColorModePanelOpen) isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) camView);
					isSettingsFirstLevel = true;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						camView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
					}
					VideOSCSettingsFragment settings = new VideOSCSettingsFragment();
					fragmentManager.beginTransaction().add(R.id.camera_preview, settings, "settings selection").commit();
				} else if ((i == 6) && hasTorch || i == 5) {
					VideOSCDialogHelper.showQuitDialog(activity);
				}
				toolsDrawerLayout.closeDrawer(Gravity.END);
				// reset menu item background immediatly
				view.setBackgroundColor(0x00000000);
			}
		});
		if (isSettingsFirstLevel || isSettingsSecondLevel)
			camView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		toolsDrawerLayout.openDrawer(Gravity.END);

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
				if (!toolsDrawerLayout.isDrawerOpen(Gravity.END))
					toolsDrawerLayout.openDrawer(Gravity.END);
				if (isColorModePanelOpen)
					isColorModePanelOpen = VideOSCUIHelpers.removeView(modePanel, (FrameLayout) camView);
			}
		});

		View indicatorPanelInner = indicatorPanel.findViewById(R.id.indicator_panel);
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
		VideOSCUIHelpers.resetSystemUIState(camView);
	}

	@Override
	public void onBackPressed() {
		View bg = findViewById(R.id.settings_background);
		if (!isSettingsFirstLevel && !isSettingsSecondLevel)
			VideOSCDialogHelper.showQuitDialog(this);
		else if (isSettingsFirstLevel) {
			VideOSCUIHelpers.removeView(findViewById(R.id.settings_selection), (FrameLayout) camView);
			VideOSCUIHelpers.removeView(bg, (FrameLayout) camView);
			VideOSCUIHelpers.resetSystemUIState(camView);
			toolsDrawerLayout.closeDrawer(Gravity.END);
			isSettingsFirstLevel = false;
		} else {
			findViewById(R.id.settings_selection_list).setVisibility(View.VISIBLE);
			VideOSCUIHelpers.removeView(findViewById(R.id.network_settings), (ViewGroup) bg);
			VideOSCUIHelpers.removeView(findViewById(R.id.resolution_settings), (ViewGroup) bg);
			VideOSCUIHelpers.removeView(findViewById(R.id.sensor_settings), (ViewGroup) bg);
			VideOSCUIHelpers.removeView(findViewById(R.id.about), (ViewGroup) bg);
			isSettingsSecondLevel = false;
			isSettingsFirstLevel = true;
		}
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

/*
	@Override
	public void onPause() {
		super.onPause();
		// TODO
	}

	@Override
	public void onResume() {
		super.onResume();
		View focused = getCurrentFocus();
		if (focused != null) {
			focused.clearFocus();
		}
		Log.d(TAG, "focused: " + focused);
	}
*/

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
