package net.videosc2.utilities;

import android.content.Context;
import android.hardware.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by stefan on 25.06.17, package net.videosc2.utilities, project VideOSC22.
 */
@RunWith(RobolectricTestRunner.class)
public class VideOSCUIHelpersTest {

	private final static String TAG = "VideOSCUIHelpersTest";

	private View view;
	private FrameLayout frameLayout;
	private View viewGroup;
	private ViewGroup.MarginLayoutParams params;
	// FIXME: mock camera
	private Camera _camera;

	@Before
	public void setUp() throws Exception {
		view = new View(RuntimeEnvironment.application);
		int height = ViewGroup.MarginLayoutParams.WRAP_CONTENT;
		int width = ViewGroup.MarginLayoutParams.MATCH_PARENT;
		params = new ViewGroup.MarginLayoutParams(width, height);
		view.setLayoutParams(params);
		frameLayout = new FrameLayout(RuntimeEnvironment.application);
		viewGroup = new TestViewGroup(RuntimeEnvironment.application);
		// FIXME
		_camera = Mockito.mock(Camera.class);
	}

	@Test
	public void hasTorch() throws Exception {
		// FIXME
		Camera camera = _camera.open();
		Camera.Parameters camParams = camera.getParameters();
		camera.release();
		assertNotNull("camParams should not be null, actual: " + camParams, camParams);
		// FIXME
//		assertNotNull("camParams.getFlashMode() should not be null, actual: " + camParams.getFlashMode(), camParams.getFlashMode());
//		assertFalse("VideOSCUIHelpers.hasTorch should return false in a local testing environment", VideOSCUIHelpers.hasTorch());
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
		assertTrue("VideOSCUIHelpers.addView should return true", VideOSCUIHelpers.addView(view, frameLayout));
		assertEquals("FrameLayout frameLayout should have 1 child view, actual: " + frameLayout.getChildCount(), 1, frameLayout.getChildCount());
	}

	@Test
	public void removeView() throws Exception {
		assertFalse("VideOSCUIHelpers.removeView should return false", VideOSCUIHelpers.removeView(view, frameLayout));
		assertEquals("FrameLayout frameLayout should have no children, actual: " + frameLayout.getChildCount(), 0, frameLayout.getChildCount());
	}

	@Test
	public void addView1() throws Exception {
		assertTrue("VideOSCUIHelpers.addView should return true", VideOSCUIHelpers.addView(view, (ViewGroup) viewGroup));
		assertEquals("ViewGroup viewGroup should have 1 child, actual: " + ((ViewGroup) viewGroup).getChildCount(), 1, ((ViewGroup) viewGroup).getChildCount());
	}

	@Test
	public void removeView1() throws Exception {
		assertFalse("VideOSCUIHelpers.removeView should return false", VideOSCUIHelpers.removeView(view, (ViewGroup) viewGroup));
		assertEquals("ViewGroup viewGroup should have no children, actual: " + ((ViewGroup) viewGroup).getChildCount(), 0, ((ViewGroup) viewGroup).getChildCount());
	}

	@Test
	public void setTransitionAnimation() throws Exception {
		VideOSCUIHelpers.setTransitionAnimation((ViewGroup) viewGroup);
		assertNotNull("VideOSCUIHelpers.setTransitionAnimation should add a LayoutTransition to the given ViewGroup", ((ViewGroup) viewGroup).getLayoutTransition());
	}

	@Test
	public void resetSystemUIState() throws Exception {
		VideOSCUIHelpers.resetSystemUIState(view);
		assertEquals("view.getSystemUiVisibility() should be : " + View.SYSTEM_UI_FLAG_HIDE_NAVIGATION, View.SYSTEM_UI_FLAG_HIDE_NAVIGATION, view.getSystemUiVisibility());
	}

	@Test
	public void hasFrontsideCamera() throws Exception {
		assertFalse("VideVideOSCUIHelpers.hasFrontsideCamera() should return false", VideOSCUIHelpers.hasFrontsideCamera());
	}

	@Test
	public void setFormSystemUIState() throws Exception {
		VideOSCUIHelpers.setFormSystemUIState(view);
		assertEquals("VideOSCUIHelpers.setFormSystemUIState() should set system UI visibility to View.SYSTEM_UI_FLAG_FULLSCREEN, actual: " + view.getSystemUiVisibility(), View.SYSTEM_UI_FLAG_FULLSCREEN, view.getSystemUiVisibility());
	}

	private class TestViewGroup extends ViewGroup {

		public TestViewGroup(Context context) {
			super(context);
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {

		}
	}
}