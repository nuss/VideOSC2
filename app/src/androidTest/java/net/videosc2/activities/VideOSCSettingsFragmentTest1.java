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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideOSCSettingsFragmentTest1 {

	@Rule
	public ActivityTestRule<VideOSCMainActivity> mActivityTestRule = new ActivityTestRule<>(VideOSCMainActivity.class);

	@Test
	public void videOSCSettingsFragmentTest1() {
		ViewInteraction appCompatImageView = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										withParent(withId(R.id.drawer_layout))),
								6),
						isDisplayed()));
		appCompatImageView.perform(click());

		ViewInteraction textView = onView(
				allOf(withId(R.id.settings_selection_item), withText("Network Settings"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										childAtPosition(
												withId(R.id.settings_selection),
												0)),
								0),
						isDisplayed()));
		textView.check(matches(withText("Network Settings")));

		ViewInteraction textView2 = onView(
				allOf(withId(R.id.settings_selection_item), withText("Resolution Settings"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										childAtPosition(
												withId(R.id.settings_selection),
												0)),
								1),
						isDisplayed()));
		textView2.check(matches(withText("Resolution Settings")));

		ViewInteraction textView3 = onView(
				allOf(withId(R.id.settings_selection_item), withText("Sensor Settings"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										childAtPosition(
												withId(R.id.settings_selection),
												0)),
								2),
						isDisplayed()));
		textView3.check(matches(withText("Sensor Settings")));

		ViewInteraction textView4 = onView(
				allOf(withId(R.id.settings_selection_item), withText("Debug Settings (temporary)"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										childAtPosition(
												withId(R.id.settings_selection),
												0)),
								3),
						isDisplayed()));
		textView4.check(matches(withText("Debug Settings (temporary)")));

		ViewInteraction textView5 = onView(
				allOf(withId(R.id.settings_selection_item), withText("About VideOSC"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										childAtPosition(
												withId(R.id.settings_selection),
												0)),
								4),
						isDisplayed()));
		textView5.check(matches(withText("About VideOSC")));

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
