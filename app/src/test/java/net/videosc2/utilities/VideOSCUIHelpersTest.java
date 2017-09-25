package net.videosc2.utilities;

import android.content.Context;
import android.hardware.Camera;
import android.os.HardwarePropertiesManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by stefan on 25.06.17, package net.videosc2.utilities, project VideOSC22.
 */
@RunWith(RobolectricTestRunner.class)
public class VideOSCUIHelpersTest {

	private final static String TAG = "VideOSCUIHelpersTest";

//	@Rule
//	public MockitoRule rule = MockitoJUnit.rule();
//
//	@Mock
//	Context mMockContext;
//
//	@Mock
//	View view = new View(mMockContext);
//
//	@Mock
//	FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();

	private View view;
	private ViewGroup.MarginLayoutParams params;

	@Before
	public void setUp() throws Exception {
		view = new View(RuntimeEnvironment.application);
		int height = ViewGroup.MarginLayoutParams.WRAP_CONTENT;
		int width = ViewGroup.MarginLayoutParams.MATCH_PARENT;
		params = new ViewGroup.MarginLayoutParams(width, height);
		view.setLayoutParams(params);
	}

	@Test
	public void hasTorch() throws Exception {
		Camera camera = Camera.open();
		Camera.Parameters camParams = camera.getParameters();
		camera.release();
		assertNotNull("camParams should not be null, actual: " + camParams, camParams);
		assertNotNull("camParams.getFlashMode() should not be null, actual: " + camParams.getFlashMode(), camParams.getFlashMode());
//		assertTrue(VideOSCUIHelpers.hasTorch());
	}

	@Test
	public void testView() throws Exception {
		assertEquals("view should be of class View", View.class, view.getClass());
		assertNotNull("params should not be null, actual: " + view.getLayoutParams(), view.getLayoutParams());
	}

	@Test
	public void setMargins() throws Exception {
		VideOSCUIHelpers.setMargins(view, 5, 5, 5, 5);
		assertEquals("bottom margin should equal 5, actual: " + params.bottomMargin, 5, params.bottomMargin);
		assertEquals("top margin should equal 5, actual: " + params.topMargin, 5, params.topMargin);
		assertEquals("left margin should equal 5, actual: " + params.leftMargin, 5, params.leftMargin);
		assertEquals("right margin should equal 5, actual: " + params.rightMargin, 5, params.rightMargin);
	}

	@Test
	public void addView() throws Exception {

	}

	@Test
	public void removeView() throws Exception {

	}

	@Test
	public void addView1() throws Exception {

	}

	@Test
	public void removeView1() throws Exception {

	}

	@Test
	public void setTransitionAnimation() throws Exception {

	}

	@Test
	public void resetSystemUIState() throws Exception {

	}

	@Test
	public void hasFrontsideCamera() throws Exception {

	}

	@Test
	public void setFormSystemUIState() throws Exception {

	}
}