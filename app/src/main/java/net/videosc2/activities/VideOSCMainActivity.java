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
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.videosc2.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import net.videosc2.R;
import net.videosc2.fragments.SettingsFragment;
import net.videosc2.fragments.VideOSCBaseFragment;
import net.videosc2.fragments.VideOSCDrawerFragment;
import net.videosc2.fragments.VideOSCNavigationDrawerFragment;
import net.videosc2.fragments.VideOSCCameraFragment;
import net.videosc2.processing.TestApplet;


/**
 * Created by Rex St. John (on behalf of AirPair.com) on 3/4/14.
 */
public class VideOSCMainActivity extends VideOSCCameraActivity
		implements /*NavigationDrawerFragment.NavigationDrawerCallbacks, */VideOSCBaseFragment.OnFragmentInteractionListener {

	static final String TAG = "VideOSCMainActivity";

	View camView;
	public static Point dimensions;
	private DrawerLayout mDrawer;
	private Toolbar toolbar;
	private NavigationView nvDrawer;

	/**
	 * Actions
	 */
//    public static final int SELECT_PHOTO_ACTION = 0;

	/**
	 * Fragment Identifiers
	 */
    public static final int SET_START_STOP_FRAGMENT = 0;
    public static final int SET_FLASHLIGHT_FRAGMENT = 1;
    public static final int SET_RGB_MODE_FRAGMENT = 2;
    public static final int INFO_FRAGMENT = 3;
    public static final int SETTINGS_FRAGMENT = 4;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
//    private VideOSCNavigationDrawerFragment mNavigationDrawerFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//			    WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		FragmentManager fragmentManager = getFragmentManager();
		if (findViewById(R.id.camera_preview) != null) {
			camView = findViewById(R.id.camera_preview);

			if (savedInstanceState != null) return;
			Fragment cameraPreview = new VideOSCCameraFragment();
			fragmentManager.beginTransaction()
					.replace(R.id.camera_preview, cameraPreview)
					.commit();
		}

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		dimensions = new Point(dm.widthPixels, dm.heightPixels);

		nvDrawer = (NavigationView) findViewById(R.id.nvView);
		Log.d(TAG, "nvDrawer: " + nvDrawer);
		setupDrawerContent(nvDrawer);


/*
        mNavigationDrawerFragment = (VideOSCNavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
*/
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawer.openDrawer(Gravity.RIGHT);
	}

	private void setupDrawerContent(NavigationView navigationView) {
		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				selectDrawerItem(item);
				return true;
			}
		});
	}

	public void selectDrawerItem(MenuItem menuItem) {
		// Create a new fragment and specify the fragment to show based on nav item clicked
		Fragment fragment = null;
//		Class fragmentClass;

		switch(menuItem.getItemId()) {
			case R.id.play:
//				fragmentClass = FirstFragment.class;
				Log.d(TAG, "clicked 'play'");
				break;
			case R.id.flashlight:
//				fragmentClass = SecondFragment.class;
				Log.d(TAG, "clicked flashlight");
				break;
			case R.id.rgb:
//				fragmentClass = ThirdFragment.class;
				Log.d(TAG, "clicked RGB mode selector");
				break;
			case R.id.interaction:
				Log.d(TAG, "clicked interaction selector");
				break;
			case R.id.info:
//				fragmentClass = ThirdFragment.class;
				Log.d(TAG, "clicked info");
				break;
			case R.id.settings:
//				fragmentClass = SettingsFragment.class;
				Log.d(TAG, "clicked settings");
				break;
			default:
//				fragmentClass = FirstFragment.class;
				Log.d(TAG, "clicked 'play'");
		}

		if (menuItem.getItemId() == R.id.settings) {
			try {
				fragment = SettingsFragment.newInstance();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.camera_preview, fragment).commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Highlight the selected item has been done by NavigationView
		menuItem.setChecked(true);

		// Set action bar title
		setTitle(menuItem.getTitle());

		// Close the navigation drawer
		mDrawer.closeDrawers();

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			camView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			camView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN);
		} else {
			camView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				mDrawer.openDrawer(GravityCompat.END);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}


//    @Override
//    public void onNavigationDrawerItemSelected(int position) {

//        // update the main content by replacing fragments
//        FragmentManager fragmentManager = getFragmentManager();
//        VideOSCBaseFragment targetFragment = null;
//
//        // Populate the fragment
//        switch (position) {
//            case SIMPLE_CAMERA_INTENT_FRAGMENT: {
//                targetFragment = SimpleCameraIntentFragment.newInstance(position + 1);
//                break;
//            }
//            case SIMPLE_PHOTO_GALLERY_FRAGMENT: {
//                targetFragment = SimplePhotoGalleryListFragment.newInstance(position + 1);
//                break;
//            }
//            case SIMPLE_PHOTO_PICKER_FRAGMENT: {
//                targetFragment = SimpleAndroidImagePickerFragment.newInstance(position + 1);
//                break;
//            }
//            case NATIVE_CAMERA_FRAGMENT: {
//                targetFragment = NativeCameraFragment.newInstance(position + 1);
//                break;
//            }
//            case HORIZONTAL_GALLERY_FRAGMENT:{
//                targetFragment = HorizontalPhotoGalleryFragment.newInstance(position + 1);
//                break;
//            }
//            default:
//                break;
//        }
//
//        // Select the fragment.
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, targetFragment)
//                .commit();
//    }

/*
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
        }
    }
*/

/*
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
*/

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
*/

/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
*/

	/**
	 * Handle Incoming messages from contained fragments.
	 */

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
