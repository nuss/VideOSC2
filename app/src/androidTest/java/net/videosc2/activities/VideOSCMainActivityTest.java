package net.videosc2.activities;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import net.videosc2.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideOSCMainActivityTest {

	@Rule
	public ActivityTestRule<VideOSCMainActivity> mActivityTestRule = new ActivityTestRule<>(VideOSCMainActivity.class);

	@Test
	public void videOSCMainActivityTest() {
		ViewInteraction imageView = onView(
				allOf(withId(R.id.camera_downscaled), withContentDescription("The view for the down-scaled preview image"),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.camera_preview),
												0)),
								0),
						isDisplayed()));
		imageView.check(matches(isDisplayed()));

		ViewInteraction view = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.camera_preview),
								childAtPosition(
										withId(R.id.camera_preview),
										0)),
						1),
						isDisplayed()));
		view.check(matches(isDisplayed()));

		ViewInteraction imageView2 = onView(
				allOf(withId(R.id.indicator_osc), withContentDescription("The current OSC status"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								0),
						isDisplayed()));
		imageView2.check(matches(isDisplayed()));

		ViewInteraction imageView3 = onView(
				allOf(withId(R.id.indicator_color), withContentDescription("The current color mode"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								1),
						isDisplayed()));
		imageView3.check(matches(isDisplayed()));

		ViewInteraction imageView4 = onView(
				allOf(withId(R.id.indicator_interaction), withContentDescription("The current interaction mode"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								2),
						isDisplayed()));
		imageView4.check(matches(isDisplayed()));

		ViewInteraction imageView5 = onView(
				allOf(withId(R.id.indicator_camera), withContentDescription("The currently active camera"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								3),
						isDisplayed()));
		imageView5.check(matches(isDisplayed()));

		ViewInteraction imageView6 = onView(
				allOf(withId(R.id.torch_status_indicator), withContentDescription("The current torch status"),
						childAtPosition(
								allOf(withId(R.id.indicator_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								4),
						isDisplayed()));
		imageView6.check(matches(isDisplayed()));

		ViewInteraction imageButton = onView(
				allOf(withId(R.id.show_menu), withContentDescription("Show/Hide the tools menu"),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								2),
						isDisplayed()));
		imageButton.check(matches(isDisplayed()));

		ViewInteraction imageView7 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								0),
						isDisplayed()));
		imageView7.check(matches(isDisplayed()));

		ViewInteraction imageView8 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								1),
						isDisplayed()));
		imageView8.check(matches(isDisplayed()));

		ViewInteraction imageView9 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								2),
						isDisplayed()));
		imageView9.check(matches(isDisplayed()));

		ViewInteraction imageView10 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								3),
						isDisplayed()));
		imageView10.check(matches(isDisplayed()));

		ViewInteraction imageView11 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								4),
						isDisplayed()));
		imageView11.check(matches(isDisplayed()));

		ViewInteraction imageView12 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								5),
						isDisplayed()));
		imageView12.check(matches(isDisplayed()));

		ViewInteraction imageView13 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								6),
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

		ViewInteraction appCompatImageView = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										withParent(withId(R.id.drawer_layout))),
								2),
						isDisplayed()));
		appCompatImageView.perform(click());

		ViewInteraction imageButton2 = onView(
				allOf(withId(R.id.mode_rgb), withContentDescription("Set color mode to RGB"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								0),
						isDisplayed()));
		imageButton2.check(matches(isDisplayed()));

		ViewInteraction imageButton3 = onView(
				allOf(withId(R.id.mode_rgb_inv), withContentDescription("Set color mode to RGB negative"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								1),
						isDisplayed()));
		imageButton3.check(matches(isDisplayed()));

		ViewInteraction imageButton4 = onView(
				allOf(withId(R.id.mode_r), withContentDescription("Set color mode to red"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								2),
						isDisplayed()));
		imageButton4.check(matches(isDisplayed()));

		ViewInteraction imageButton5 = onView(
				allOf(withId(R.id.mode_g), withContentDescription("Set color mode to green"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								3),
						isDisplayed()));
		imageButton5.check(matches(isDisplayed()));

		ViewInteraction imageButton6 = onView(
				allOf(withId(R.id.mode_b), withContentDescription("Set color mode to blue"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								4),
						isDisplayed()));
		imageButton6.check(matches(isDisplayed()));

		ViewInteraction imageButton7 = onView(
				allOf(withId(R.id.mode_rgb), withContentDescription("Set color mode to RGB"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								0),
						isDisplayed()));
		imageButton7.check(doesNotExist());

		ViewInteraction imageButton8 = onView(
				allOf(withId(R.id.mode_rgb_inv), withContentDescription("Set color mode to RGB negative"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								1),
						isDisplayed()));
		imageButton8.check(doesNotExist());

		ViewInteraction imageButton9 = onView(
				allOf(withId(R.id.mode_r), withContentDescription("Set color mode to red"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								2),
						isDisplayed()));
		imageButton9.check(doesNotExist());

		ViewInteraction imageButton10 = onView(
				allOf(withId(R.id.mode_g), withContentDescription("Set color mode to green"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								3),
						isDisplayed()));
		imageButton10.check(doesNotExist());

		ViewInteraction imageButton11 = onView(
				allOf(withId(R.id.mode_b), withContentDescription("Set color mode to blue"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								4),
						isDisplayed()));
		imageButton11.check(doesNotExist());

		ViewInteraction imageButton12 = onView(
				allOf(withId(R.id.mode_b), withContentDescription("Set color mode to blue"),
						childAtPosition(
								allOf(withId(R.id.color_mode_panel),
										childAtPosition(
												withId(R.id.camera_preview),
												1)),
								4),
						isDisplayed()));
		imageButton12.check(doesNotExist());

		ViewInteraction appCompatImageButton = onView(
				allOf(withId(R.id.mode_rgb), withContentDescription("Set color mode to RGB"),
						withParent(allOf(withId(R.id.color_mode_panel),
								withParent(withId(R.id.camera_preview)))),
						isDisplayed()));
		appCompatImageButton.perform(click());

		ViewInteraction appCompatImageButton2 = onView(
				allOf(withId(R.id.show_menu), withContentDescription("Show/Hide the tools menu"),
						withParent(allOf(withId(R.id.camera_preview),
								withParent(withId(R.id.drawer_layout)))),
						isDisplayed()));
		appCompatImageButton2.perform(click());

		ViewInteraction imageView15 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								0),
						isDisplayed()));
		imageView15.check(matches(isDisplayed()));

		ViewInteraction imageView16 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								1),
						isDisplayed()));
		imageView16.check(matches(isDisplayed()));

		ViewInteraction imageView17 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								2),
						isDisplayed()));
		imageView17.check(matches(isDisplayed()));

		ViewInteraction imageView18 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								3),
						isDisplayed()));
		imageView18.check(matches(isDisplayed()));

		ViewInteraction imageView19 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								4),
						isDisplayed()));
		imageView19.check(matches(isDisplayed()));

		ViewInteraction imageView20 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								5),
						isDisplayed()));
		imageView20.check(matches(isDisplayed()));

		ViewInteraction imageView21 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								6),
						isDisplayed()));
		imageView21.check(matches(isDisplayed()));

		ViewInteraction imageView22 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								0),
						isDisplayed()));
		imageView22.check(doesNotExist());

		ViewInteraction imageView23 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								1),
						isDisplayed()));
		imageView23.check(doesNotExist());

		ViewInteraction imageView24 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								2),
						isDisplayed()));
		imageView24.check(doesNotExist());

		ViewInteraction imageView25 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								3),
						isDisplayed()));
		imageView25.check(doesNotExist());

		ViewInteraction imageView26 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								4),
						isDisplayed()));
		imageView26.check(doesNotExist());

		ViewInteraction imageView27 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								5),
						isDisplayed()));
		imageView27.check(doesNotExist());

		ViewInteraction imageView28 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								6),
						isDisplayed()));
		imageView28.check(doesNotExist());

		ViewInteraction imageView29 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										childAtPosition(
												withId(R.id.drawer_layout),
												1)),
								6),
						isDisplayed()));
		imageView29.check(doesNotExist());

		ViewInteraction appCompatImageView2 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										withParent(withId(R.id.drawer_layout))),
								2),
						isDisplayed()));
		appCompatImageView2.perform(click());

		ViewInteraction appCompatImageButton3 = onView(
				allOf(withId(R.id.mode_rgb_inv), withContentDescription("Set color mode to RGB negative"),
						withParent(allOf(withId(R.id.color_mode_panel),
								withParent(withId(R.id.camera_preview)))),
						isDisplayed()));
		appCompatImageButton3.perform(click());

		ViewInteraction appCompatImageButton4 = onView(
				allOf(withId(R.id.show_menu), withContentDescription("Show/Hide the tools menu"),
						withParent(allOf(withId(R.id.camera_preview),
								withParent(withId(R.id.drawer_layout)))),
						isDisplayed()));
		appCompatImageButton4.perform(click());

		ViewInteraction appCompatImageView3 = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										withParent(withId(R.id.drawer_layout))),
								5),
						isDisplayed()));
		appCompatImageView3.perform(click());

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
