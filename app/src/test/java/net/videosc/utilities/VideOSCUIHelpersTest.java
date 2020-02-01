package net.videosc.utilities;

import android.content.Context;
import android.hardware.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowCamera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by stefan on 25.06.17, package net.videosc.utilities, project VideOSC22.
 */
@RunWith(RobolectricTestRunner.class)
public class VideOSCUIHelpersTest extends ShadowCamera {

	private final static String TAG = "VideOSCUIHelpersTest";

	private View view;
	private FrameLayout frameLayout;
	private View viewGroup;
	private ViewGroup.MarginLayoutParams params;
	private Camera camera;
//	private ShadowCamera.ShadowParameters camParams;

	@Before
	public void setUp() {
		view = new View(RuntimeEnvironment.application);
		int height = ViewGroup.MarginLayoutParams.WRAP_CONTENT;
		int width = ViewGroup.MarginLayoutParams.MATCH_PARENT;
		params = new ViewGroup.MarginLayoutParams(width, height);
		view.setLayoutParams(params);
		frameLayout = new FrameLayout(RuntimeEnvironment.application);
		viewGroup = new TestViewGroup(RuntimeEnvironment.application);
		camera = ShadowCamera.open();
	}

	@Test
	@Config(manifest = Config.NONE)
	public void hasTorch() {
		assertFalse("VideOSCUIHelpers.hasTorch should return false in a local testing environment", VideOSCUIHelpers.hasTorch(camera));
	}

	@Test
	public void testView() {
		assertEquals("view should be of class View", View.class, view.getClass());
		assertNotNull("params should not be null, actual: " + view.getLayoutParams(), view.getLayoutParams());
	}

	@Test
	public void setMargins() {
		VideOSCUIHelpers.setMargins(view, 5, 5, 5, 5);
		assertEquals("bottom margin should equal 5, actual: " + params.bottomMargin, 5, params.bottomMargin);
		assertEquals("top margin should equal 5, actual: " + params.topMargin, 5, params.topMargin);
		assertEquals("left margin should equal 5, actual: " + params.leftMargin, 5, params.leftMargin);
		assertEquals("right margin should equal 5, actual: " + params.rightMargin, 5, params.rightMargin);
	}

	@Test
	public void addView() {
		assertTrue("VideOSCUIHelpers.addView should return true", VideOSCUIHelpers.addView(view, frameLayout));
		assertEquals("FrameLayout frameLayout should have 1 child view, actual: " + frameLayout.getChildCount(), 1, frameLayout.getChildCount());
	}

	@Test
	public void removeView() {
		assertFalse("VideOSCUIHelpers.removeView should return false", VideOSCUIHelpers.removeView(view, frameLayout));
		assertEquals("FrameLayout frameLayout should have no children, actual: " + frameLayout.getChildCount(), 0, frameLayout.getChildCount());
	}

	@Test
	public void addView1() {
		assertTrue("VideOSCUIHelpers.addView should return true", VideOSCUIHelpers.addView(view, (ViewGroup) viewGroup));
		assertEquals("ViewGroup viewGroup should have 1 child, actual: " + ((ViewGroup) viewGroup).getChildCount(), 1, ((ViewGroup) viewGroup).getChildCount());
	}

	@Test
	public void removeView1() {
		assertFalse("VideOSCUIHelpers.removeView should return false", VideOSCUIHelpers.removeView(view, (ViewGroup) viewGroup));
		assertEquals("ViewGroup viewGroup should have no children, actual: " + ((ViewGroup) viewGroup).getChildCount(), 0, ((ViewGroup) viewGroup).getChildCount());
	}

	@Test
	public void setTransitionAnimation() {
		VideOSCUIHelpers.setTransitionAnimation((ViewGroup) viewGroup);
		assertNotNull("VideOSCUIHelpers.setTransitionAnimation should add a LayoutTransition to the given ViewGroup", ((ViewGroup) viewGroup).getLayoutTransition());
	}

	@Test
	public void resetSystemUIState() {
		VideOSCUIHelpers.resetSystemUIState(view);
		assertEquals("view.getSystemUiVisibility() should be : " + View.SYSTEM_UI_FLAG_HIDE_NAVIGATION, View.SYSTEM_UI_FLAG_HIDE_NAVIGATION, view.getSystemUiVisibility());
	}

	@Test
	public void hasFrontsideCamera() {
		assertFalse("VideVideOSCUIHelpers.hasFrontsideCamera() should return false", VideOSCUIHelpers.hasFrontsideCamera());
	}

	@Test
	public void setFormSystemUIState() {
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