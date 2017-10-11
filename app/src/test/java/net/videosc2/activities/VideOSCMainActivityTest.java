package net.videosc2.activities;


import android.support.v4.widget.DrawerLayout;

import net.videosc2.BuildConfig;
import net.videosc2.R;
import net.videosc2.utilities.enums.RGBToolbarStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by stefan on 23.06.17, package net.videosc2, project VideOSC22.
 */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class VideOSCMainActivityTest {

	private static final String TAG = "VideOSCMainActivityTest";
	private VideOSCMainActivity activity;

	@Before
	public void setUp() throws Exception {
		activity = Robolectric.buildActivity(VideOSCMainActivity.class)
				.create()
				.resume()
				.get();
	}

	@Test
	public void testOnCreate() throws Exception {
		assertEquals(VideOSCMainActivity.currentCameraID, VideOSCMainActivity.frontsideCameraId);
//		assertNotNull(activity.findViewById(R.id.camera_preview));
		assertEquals(activity.mColorModeToolsDrawer, RGBToolbarStatus.RGB);
	}

	@Test
	public void testOnContentChanged() throws Exception {
		// nothing yet
	}

	@Test
	public void testOnPostCreate() throws Exception {
		DrawerLayout toolsDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
		assertNotNull("tools drawer is expected to be not null", toolsDrawerLayout);
	}

	@Test
	public void testOnWindowFocusChanged() throws Exception {
//		View view = activity.findViewById(R.id.camera_preview);
//		Log.d(TAG, "view.getSystemUiVisibility: " + view.getSystemUiVisibility());
	}

	@Test
	public void testOnBackPressed() throws Exception {

	}

	@Test
	public void testGetColorModeToolsDrawer() throws Exception {
		assertEquals(activity.getColorModeToolsDrawer(), RGBToolbarStatus.RGB);
	}

	@Test
	public void testOnPause() throws Exception {

	}

	@Test
	public void testOnResume() throws Exception {

	}

	@Test
	public void testOnConfigurationChanged() throws Exception {

	}

	@Test
	public void testOnFragmentInteraction() throws Exception {

	}

	@Test
	public void testOnFragmentInteraction1() throws Exception {

	}

	@Test
	public void testOnFragmentInteraction2() throws Exception {

	}

	public class CustomRobolectricTestRunner extends RobolectricTestRunner {
		public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
			super(testClass);
//			String buildVariant = BuildConfig.BUILD_TYPE + (BuildConfig.FLAVOR.isEmpty()? "" : "/" + BuildConfig.FLAVOR);
//			System.setProperty("android.package", BuildConfig.APPLICATION_ID);
//			System.setProperty("android.manifest", "build/intermediates/manifests/full/" + buildVariant + "/AndroidManifest.xml");
//			System.setProperty("android.resources", "build/intermediates/res/" + buildVariant);
//			System.setProperty("android.assets", "build/intermediates/assets/" + buildVariant);
		}
	}
}
