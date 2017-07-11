package net.videosc2.activities;

import android.content.Context;
import android.test.mock.MockContext;
import android.util.Log;
import android.view.View;

import junit.framework.TestCase;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.VideOSCApplication;
import net.videosc2.fragments.VideOSCCameraFragment;
import net.videosc2.utilities.enums.RGBModes;
import net.videosc2.utilities.enums.RGBToolbarStatus;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by stefan on 23.06.17, package net.videosc2, project VideOSC22.
 */
public class VideOSCMainActivityTest extends TestCase {

	private static final String TAG = "VideOSCMainActivityTest";
	private VideOSCMainActivity activity = new VideOSCMainActivity();

	public void testOnCreate() throws Exception {
		assertEquals(VideOSCMainActivity.currentCameraID, VideOSCMainActivity.frontsideCameraId);
//		assertNotNull(activity.findViewById(R.id.camera_preview));
		assertEquals(activity.mColorModeToolsDrawer, RGBToolbarStatus.RGB);
	}

	public void testOnContentChanged() throws Exception {

	}

	public void testOnPostCreate() throws Exception {

	}

	public void testOnWindowFocusChanged() throws Exception {
//		View view = activity.findViewById(R.id.camera_preview);
//		Log.d(TAG, "view.getSystemUiVisibility: " + view.getSystemUiVisibility());
	}

	public void testOnBackPressed() throws Exception {

	}

	public void testGetColorModeToolsDrawer() throws Exception {
		assertEquals(activity.getColorModeToolsDrawer(), RGBToolbarStatus.RGB);
	}

	public void testOnPause() throws Exception {

	}

	public void testOnResume() throws Exception {

	}

	public void testOnConfigurationChanged() throws Exception {

	}

	public void testOnFragmentInteraction() throws Exception {

	}

	public void testOnFragmentInteraction1() throws Exception {

	}

	public void testOnFragmentInteraction2() throws Exception {

	}
}
