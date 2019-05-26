package net.videosc.fragments;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by stefan on 14.07.17, package net.videosc.fragments, project VideOSC22.
 */
public class VideOSCCameraFragmentTest {
	private VideOSCCameraFragment fragment;

	@Before
	public void setUp() {
		fragment = new VideOSCCameraFragment();
	}

	@Test
	public void onCreateView() {
		assertNotNull(fragment);
	}

	@After
	public void tearDown() {
		fragment = null;
	}
}