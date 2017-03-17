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
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import net.videosc2.R;
import net.videosc2.adapters.ToolsMenuAdapter;
import net.videosc2.fragments.VideOSCBaseFragment;
import net.videosc2.fragments.VideOSCCameraFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Rex St. John (on behalf of AirPair.com) on 3/4/14.
 */
public class VideOSCMainActivity extends AppCompatActivity
		implements /*NavigationDrawerFragment.NavigationDrawerCallbacks, */VideOSCBaseFragment.OnFragmentInteractionListener {

	static final String TAG = "VideOSCMainActivity";

	View camView;
	public static Point dimensions;
	private DrawerLayout toolsDrawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	protected ArrayList<View> uiElements = new ArrayList<>();
	public boolean isPlaying = false;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			    WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

		TypedArray tools = getResources().obtainTypedArray(R.array.drawer_icons);
//		Log.d(TAG, "tools: " + tools.toString());
		toolsDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		ListView toolsDrawerList = (ListView) findViewById(R.id.drawer);

		List<BitmapDrawable> toolsList = new ArrayList<>();
		for (int i = 0; i < tools.length(); i++) {
			toolsList.add((BitmapDrawable) tools.getDrawable(i));
		}

		toolsDrawerList.setAdapter(new ToolsMenuAdapter(this, R.layout.drawer_item, R.id.tool, toolsList));
		tools.recycle();

		toolsDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				BitmapDrawable img;
				ImageView imgView;
				switch (i) {
					case 0:
						isPlaying = !isPlaying;
						imgView = (ImageView) view.findViewById(R.id.tool);
						Log.d(TAG, "start/stop sending OSC: " + imgView.getDrawable().getLevel() + "\nisPlaying: " + isPlaying);
						if (isPlaying) {
							img = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
									(BitmapDrawable) getResources().getDrawable(R.drawable.stop, getApplicationContext().getTheme()) :
									(BitmapDrawable) getResources().getDrawable(R.drawable.stop);
						} else {
							img = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
									(BitmapDrawable) getResources().getDrawable(R.drawable.start, getApplicationContext().getTheme()) :
									(BitmapDrawable) getResources().getDrawable(R.drawable.start);
						}
						imgView.setImageDrawable(img);
						toolsDrawerLayout.closeDrawer(Gravity.RIGHT);
						break;
					case 1:
						Log.d(TAG, "switch on/off torch: " +  + view.getResources().getIdentifier("id", null, null));
						imgView = (ImageView) view.findViewById(R.id.tool);
						Log.d(TAG, "switch on/off torch: " + imgView.getDrawable().getLevel());
						toolsDrawerLayout.closeDrawer(Gravity.RIGHT);
						break;
					case 2:
						Log.d(TAG, "select color mode: " + view.getY());
						float y = view.getY();

						break;
					case 3:
						Log.d(TAG, "set interaction mode");
						toolsDrawerLayout.closeDrawer(Gravity.RIGHT);
						break;
					case 4:
						Log.d(TAG, "framerate, calculation period info");
						toolsDrawerLayout.closeDrawer(Gravity.RIGHT);
						break;
					case 5:
						Log.d(TAG, "settings");
						toolsDrawerLayout.closeDrawer(Gravity.RIGHT);
						break;
					default:
						Log.d(TAG, "what u wanna?");
						toolsDrawerLayout.closeDrawer(Gravity.RIGHT);
				}
			}
		});
		toolsDrawerLayout.openDrawer(Gravity.RIGHT);

//		drawerToggle = setupDrawerToggle();
//		mDrawer.addDrawerListener(drawerToggle);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		// width/height of the screen
		dimensions = new Point(dm.widthPixels, dm.heightPixels);
	}

	@Override
	public void  onContentChanged() {
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
				if (toolsDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
					toolsDrawerLayout.closeDrawer(Gravity.RIGHT);
				} else if (!toolsDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
					toolsDrawerLayout.openDrawer(Gravity.RIGHT);
				}
			}
		});
//		mDrawer.openDrawer(Gravity.RIGHT);
		// Sync the toggle state after onRestoreInstanceState has occurred.
//		drawerToggle.syncState();
	}

/*
	private void setupDrawerContent(NavigationView navigationView) {
		navigationView.setItemIconTintList(null);
		navigationView.getBackground().setAlpha(127);
		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				selectDrawerItem(item);
				return true;
			}
		});
	}
*/

/*
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
*/

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			Log.d(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT");
			camView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		} else {
//			Log.d(TAG, "else branch");
			camView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
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
