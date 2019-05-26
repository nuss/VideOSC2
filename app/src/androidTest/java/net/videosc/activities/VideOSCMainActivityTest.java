package net.videosc.activities;


import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.videosc.VideOSCApplication;
import net.videosc.utilities.enums.RGBToolbarStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideOSCMainActivityTest {
	private VideOSCMainActivity mActivity;
	private VideOSCApplication mApp;

	@Rule
	public ActivityTestRule<VideOSCMainActivity> mActivityTestRule = new ActivityTestRule<>(VideOSCMainActivity.class);

	@Before
	public void setUp() {
		mActivity = mActivityTestRule.getActivity();
		mApp = (VideOSCApplication) mActivity.getApplication();
	}

	@After
	public void tearDown() {
		mActivity = null;
	}

	@Test
	public void onCreateTest() {
		assertNotNull(mActivity.mCamView);
		assertNotNull(mActivity.mBasicToolbar);
		assertNotNull(mActivity.mDbHelper);
		assertEquals(mActivity.mColorModeToolsDrawer, RGBToolbarStatus.RGB);
		assertNotNull(mActivity.mPixelEditor);
	}
}
