package net.videosc2.fragments;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

public class VideOSCMultiSliderFragmentRGBTest {
	private VideOSCMultiSliderFragmentRGB fragment;

	@Before
	public void setUp() {
		fragment = new VideOSCMultiSliderFragmentRGB();
	}

	@Test
	public void onCreateView() {
		assertNotNull(fragment);
	}

	@After
	public void tearDown() throws Exception {
		fragment = null;
	}
}