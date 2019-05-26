package net.videosc2.fragments;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by stefan on 25.06.17, package net.videosc2.fragments, project VideOSC22.
 */
public class VideOSCSettingsFragmentTest {
	private VideOSCSettingsFragment fragment;

	@Before
	public void setUp() {
		fragment = new VideOSCSettingsFragment();
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