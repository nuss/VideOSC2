package net.videosc.fragments;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;

/**
 * Created by stefan on 14.07.17, package net.videosc.fragments, project VideOSC22.
 */
public class VideOSCCameraFragmentTest {
	private VideOSCCameraFragment fragment;
	private final static String TAG = "VideOSCCameraFragmentTest";

	private ArrayList<Double> values = new ArrayList<>(5);

	@Before
	public void setUp() {
		fragment = new VideOSCCameraFragment();
		values.add(0.1);
		values.add(0.2);
		values.add(0.3);
		values.add(null);
		values.add(0.5);
//		Log.d(TAG, "values: " + values);
//		for (int i = 0; i < values.size(); i++) {
//			fragment.setRedValue(i, null);
//			fragment.setGreenValue(i, null);
//			fragment.setBlueValue(i, null);
//			fragment.setRedMixValue(i, null);
//			fragment.setGreenMixValue(i, null);
//			fragment.setBlueMixValue(i, null);
//		}
	}

	@Test
	public void onCreateView() {
		assertNotNull(fragment);
	}

	@After
	public void tearDown() {
		fragment = null;
	}

	@Test
	public void safeCameraOpenInView() {
		assertNotNull(fragment.mPreview);
	}

	@Test
	public void onDestroy() {
	}

	@Test
	public void setColorOscCmds() {
	}

	@Test
	public void getColorOscCmds() {
	}

	@Test
	public void getSelectedPixels() {
	}

	@Test
	public void getPixelNumbers() {
	}

	@Test
	public void getRedValues() {
	}

	@Test
	public void setRedValues() {
	}

	@Test
	public void setRedValue() {
	}

	@Test
	public void setRedMixValue() {
	}

	@Test
	public void setGreenValue() {
	}

	@Test
	public void setGreenMixValue() {
	}

	@Test
	public void setBlueValue() {
	}

	@Test
	public void setBlueMixValue() {
	}

	@Test
	public void getRedMixValues() {
	}

	@Test
	public void setRedMixValues() {
	}

	@Test
	public void getGreenValues() {
	}

	@Test
	public void setGreenValues() {
	}

	@Test
	public void getRedResetValues() {
	}

	@Test
	public void getRedMixResetValues() {
	}

	@Test
	public void getGreenResetValues() {
	}

	@Test
	public void getGreenMixResetValues() {
	}

	@Test
	public void getBlueResetValues() {
	}

	@Test
	public void getBlueMixResetValues() {
	}

	@Test
	public void getGreenMixValues() {
	}

	@Test
	public void setGreenMixValues() {
	}

	@Test
	public void getBlueValues() {
	}

	@Test
	public void setBlueValues() {
	}

	@Test
	public void getBlueMixValues() {
	}

	@Test
	public void setBlueMixValues() {
	}
}