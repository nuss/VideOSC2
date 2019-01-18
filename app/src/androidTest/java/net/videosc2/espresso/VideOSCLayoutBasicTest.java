package net.videosc2.espresso;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideOSCLayoutBasicTest {

	@Rule
	public ActivityTestRule<VideOSCMainActivity> mActivityTestRule = new ActivityTestRule<>(VideOSCMainActivity.class);

	@Rule
	public GrantPermissionRule mGrantPermissionRule =
			GrantPermissionRule.grant(
					"android.permission.CAMERA");

	@Test
	public void videOSCLayoutBasicTest() {
		/*ViewInteraction frameLayout = onView(
				allOf(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class), isDisplayed()));
		frameLayout.check(matches(isDisplayed()));

		ViewInteraction linearLayout = onView(
				allOf(childAtPosition(
						IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
						0),
						isDisplayed()));
		linearLayout.check(matches(isDisplayed()));

		ViewInteraction frameLayout2 = onView(
				allOf(childAtPosition(
						childAtPosition(
								IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
								0),
						0),
						isDisplayed()));
		frameLayout2.check(matches(isDisplayed()));

		ViewInteraction linearLayout2 = onView(
				allOf(withId(R.id.action_bar_root),
						childAtPosition(
								childAtPosition(
										IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
										0),
								0),
						isDisplayed()));
		linearLayout2.check(matches(isDisplayed()));

		ViewInteraction frameLayout3 = onView(
				allOf(withId(android.R.id.content),
						childAtPosition(
								allOf(withId(R.id.action_bar_root),
										childAtPosition(
												IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
												0)),
								0),
						isDisplayed()));
		frameLayout3.check(matches(isDisplayed()));

		ViewInteraction drawerLayout = onView(
				allOf(withId(R.id.drawer_layout),
						childAtPosition(
								allOf(withId(android.R.id.content),
										childAtPosition(
												withId(R.id.action_bar_root),
												0)),
								0),
						isDisplayed()));
		drawerLayout.check(matches(isDisplayed()));*/

		ViewInteraction frameLayout4 = onView(
				allOf(withId(R.id.camera_preview),
						childAtPosition(
								allOf(withId(R.id.drawer_layout),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								0),
						isDisplayed()));
		frameLayout4.check(matches(isDisplayed()));

		/*ViewInteraction view = onView(
				allOf(withId(R.id.tile_draw_view),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								0),
						isDisplayed()));
		view.check(matches(isDisplayed()));

		ViewInteraction frameLayout5 = onView(
				allOf(withId(R.id.camera_preview),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								1),
						isDisplayed()));
		frameLayout5.check(matches(isDisplayed()));

		ViewInteraction imageView = onView(
				allOf(withId(R.id.camera_downscaled), withContentDescription("The view for the down-scaled preview image"),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								0),
						isDisplayed()));
		imageView.check(matches(isDisplayed()));

		ViewInteraction view2 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.camera_preview),
								childAtPosition(
										withId(R.id.camera_preview),
										1)),
						1),
						isDisplayed()));
		view2.check(matches(isDisplayed()));

		ViewInteraction linearLayout3 = onView(
				allOf(withId(R.id.basic_tools_bar),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								2),
						isDisplayed()));
		linearLayout3.check(matches(isDisplayed()));

		ViewInteraction relativeLayout = onView(
				allOf(withId(R.id.saved_snapshots_wrapper),
						childAtPosition(
								allOf(withId(R.id.basic_tools_bar),
										childAtPosition(
												withId(R.id.camera_preview),
												2)),
								0),
						isDisplayed()));
		relativeLayout.check(matches(isDisplayed()));*/

		ViewInteraction imageButton = onView(
				allOf(withId(R.id.saved_snapshots_button), withContentDescription("load a saved snapshot"),
						childAtPosition(
								allOf(withId(R.id.saved_snapshots_wrapper),
										childAtPosition(
												withId(R.id.snapshots_bar),
												0)),
								0),
						isDisplayed()));
		imageButton.check(matches(isDisplayed()));

		ViewInteraction textView = onView(
				allOf(withId(R.id.num_snapshots), withText("0"),
						childAtPosition(
								allOf(withId(R.id.saved_snapshots_wrapper),
										childAtPosition(
												withId(R.id.snapshots_bar),
												0)),
								1),
						isDisplayed()));
		textView.check(matches(withText("0")));

		/*ViewInteraction imageButton2 = onView(
				allOf(withId(R.id.save_snapshot), withContentDescription("make a snapshot"),
						childAtPosition(
								allOf(withId(R.id.basic_tools_bar),
										childAtPosition(
												withId(R.id.camera_preview),
												2)),
								1),
						isDisplayed()));
		imageButton2.check(matches(isDisplayed()));

		ViewInteraction imageView2 = onView(
				allOf(withId(R.id.move_snapshots_bar), withContentDescription("move the snapshots bar"),
						childAtPosition(
								allOf(withId(R.id.basic_tools_bar),
										childAtPosition(
												withId(R.id.camera_preview),
												2)),
								2),
						isDisplayed()));
		imageView2.check(matches(isDisplayed()));

		ViewInteraction linearLayout4 = onView(
				allOf(withId(R.id.indicator_panel),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								3),
						isDisplayed()));
		linearLayout4.check(matches(isDisplayed()));

		ViewInteraction imageView3 = onView(
				allOf(withId(R.id.indicator_osc), withContentDescription("The current OSC status"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												3)),
								0),
						isDisplayed()));
		imageView3.check(matches(isDisplayed()));

		ViewInteraction imageView4 = onView(
				allOf(withId(R.id.indicator_color), withContentDescription("The current color mode"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												3)),
								1),
						isDisplayed()));
		imageView4.check(matches(isDisplayed()));

		ViewInteraction imageView5 = onView(
				allOf(withId(R.id.indicator_interaction), withContentDescription("The current interaction mode"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												3)),
								2),
						isDisplayed()));
		imageView5.check(matches(isDisplayed()));

		ViewInteraction imageView6 = onView(
				allOf(withId(R.id.indicator_camera), withContentDescription("The currently active camera"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												3)),
								3),
						isDisplayed()));
		imageView6.check(matches(isDisplayed()));

		ViewInteraction imageView7 = onView(
				allOf(withId(R.id.torch_status_indicator), withContentDescription("The current torch status"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												3)),
								4),
						isDisplayed()));
		imageView7.check(matches(isDisplayed()));

		ViewInteraction imageButton3 = onView(
				allOf(withId(R.id.show_menu), withContentDescription("Show/Hide the tools menu"),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								4),
						isDisplayed()));
		imageButton3.check(matches(isDisplayed()));*/

		ViewInteraction listView = onView(
				allOf(withId(R.id.drawer),
						childAtPosition(
								allOf(withId(R.id.drawer_layout),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								1),
						isDisplayed()));
		listView.check(matches(isDisplayed()));

		ViewInteraction imageView8 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								0),
						isDisplayed()));
		imageView8.check(matches(isDisplayed()));

		ViewInteraction imageView9 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								1),
						isDisplayed()));
		imageView9.check(matches(isDisplayed()));

		ViewInteraction imageView10 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								2),
						isDisplayed()));
		imageView10.check(matches(isDisplayed()));

		ViewInteraction imageView11 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								3),
						isDisplayed()));
		imageView11.check(matches(isDisplayed()));

		ViewInteraction imageView12 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								4),
						isDisplayed()));
		imageView12.check(matches(isDisplayed()));

		ViewInteraction imageView13 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								5),
						isDisplayed()));
		imageView13.check(matches(isDisplayed()));

		ViewInteraction imageView14 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								6),
						isDisplayed()));
		imageView14.check(matches(isDisplayed()));

		ViewInteraction imageView15 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								6),
						isDisplayed()));
		imageView15.check(matches(isDisplayed()));

		ViewInteraction appCompatImageButton = onView(
				allOf(withId(R.id.save_snapshot), withContentDescription("make a snapshot"),
						childAtPosition(
								allOf(withId(R.id.snapshots_bar),
										childAtPosition(
												withId(R.id.camera_preview),
												4)),
								1),
						isDisplayed()));
		appCompatImageButton.perform(click());

		/*ViewInteraction frameLayout6 = onView(
				allOf(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class), isDisplayed()));
		frameLayout6.check(matches(isDisplayed()));

		ViewInteraction frameLayout7 = onView(
				allOf(childAtPosition(
						IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
						0),
						isDisplayed()));
		frameLayout7.check(matches(isDisplayed()));

		ViewInteraction frameLayout8 = onView(
				allOf(withId(android.R.id.content),
						childAtPosition(
								childAtPosition(
										IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
										0),
								0),
						isDisplayed()));
		frameLayout8.check(matches(isDisplayed()));

		ViewInteraction linearLayout5 = onView(
				allOf(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
						childAtPosition(
								allOf(withId(android.R.id.content),
										childAtPosition(
												IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
												0)),
								0),
						isDisplayed()));
		linearLayout5.check(matches(isDisplayed()));

		ViewInteraction frameLayout9 = onView(
				allOf(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
						childAtPosition(
								allOf(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								0),
						isDisplayed()));
		frameLayout9.check(matches(isDisplayed()));

		ViewInteraction frameLayout10 = onView(
				allOf(withId(android.R.id.custom),
						childAtPosition(
								allOf(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
										childAtPosition(
												IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
												0)),
								0),
						isDisplayed()));
		frameLayout10.check(matches(isDisplayed()));

		ViewInteraction linearLayout6 = onView(
				allOf(withId(R.id.save_snapshot_dialog),
						childAtPosition(
								allOf(withId(android.R.id.custom),
										childAtPosition(
												IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
												0)),
								0),
						isDisplayed()));
		linearLayout6.check(matches(isDisplayed()));

		ViewInteraction editText = onView(
				allOf(withId(R.id.save_snapshot_name), withText("2018-10-13 21:55:02"),
						childAtPosition(
								allOf(withId(R.id.save_snapshot_dialog),
										childAtPosition(
												withId(android.R.id.custom),
												0)),
								0),
						isDisplayed()));
		editText.check(matches(isDisplayed()));

		ViewInteraction linearLayout7 = onView(
				allOf(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
						childAtPosition(
								allOf(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
										childAtPosition(
												withId(android.R.id.content),
												0)),
								1),
						isDisplayed()));
		linearLayout7.check(matches(isDisplayed()));

		ViewInteraction button = onView(
				allOf(withId(android.R.id.button2),
						childAtPosition(
								allOf(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
										childAtPosition(
												IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
												1)),
								0),
						isDisplayed()));
		button.check(matches(isDisplayed()));

		ViewInteraction button2 = onView(
				allOf(withId(android.R.id.button1),
						childAtPosition(
								allOf(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
										childAtPosition(
												IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
												1)),
								1),
						isDisplayed()));
		button2.check(matches(isDisplayed()));

		ViewInteraction button3 = onView(
				allOf(withId(android.R.id.button1),
						childAtPosition(
								allOf(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
										childAtPosition(
												IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
												1)),
								1),
						isDisplayed()));
		button3.check(matches(isDisplayed()));

		ViewInteraction appCompatButton = onView(
				allOf(withId(android.R.id.button1), withText("save snapshot"),
						childAtPosition(
								allOf(withClassName(is("com.android.internal.widget.ButtonBarLayout")),
										childAtPosition(
												withClassName(is("android.widget.LinearLayout")),
												3)),
								3),
						isDisplayed()));
		appCompatButton.perform(click());

		ViewInteraction textView2 = onView(
				allOf(withId(R.id.num_snapshots), withText("1"),
						childAtPosition(
								allOf(withId(R.id.saved_snapshots_wrapper),
										childAtPosition(
												withId(R.id.basic_tools_bar),
												0)),
								1),
						isDisplayed()));
		textView2.check(matches(withText("1")));*/
	}

	private static Matcher<View> childAtPosition(
			final Matcher<View> parentMatcher, final int position) {

		return new TypeSafeMatcher<View>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("Child at position " + position + " in parent ");
				parentMatcher.describeTo(description);
			}

			@Override
			public boolean matchesSafely(View view) {
				ViewParent parent = view.getParent();
				return parent instanceof ViewGroup && parentMatcher.matches(parent)
						&& view.equals(((ViewGroup) parent).getChildAt(position));
			}
		};
	}
}
