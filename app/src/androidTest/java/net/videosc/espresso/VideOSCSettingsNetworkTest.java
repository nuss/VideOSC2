package net.videosc.espresso;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideOSCSettingsNetworkTest {

	@Rule
	public ActivityTestRule<VideOSCMainActivity> mActivityTestRule = new ActivityTestRule<>(VideOSCMainActivity.class);

	@Test
	public void videOSCSettingsNetworkTest() throws InterruptedException {
		Thread.sleep(5000);

		ViewInteraction appCompatImageView = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										withParent(withId(R.id.drawer_layout))),
								6),
						isDisplayed()));
		appCompatImageView.perform(click());

		ViewInteraction appCompatTextView = onView(
				allOf(withId(R.id.settings_selection_item), withText("Network Settings"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										withParent(withId(R.id.settings_selection))),
								0),
						isDisplayed()));
		appCompatTextView.perform(click());

		Thread.sleep(2000);

		ViewInteraction linearLayout = onView(
				allOf(withId(R.id.remote_address),
						childAtPosition(
								allOf(withId(R.id.network_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								0),
						isDisplayed()));
		linearLayout.check(matches(isDisplayed()));

		/*ViewInteraction linearLayout2 = onView(
				allOf(withId(R.id.remote_ip),
						childAtPosition(
								allOf(withId(R.id.remote_address),
										childAtPosition(
												withId(R.id.network_settings),
												0)),
								0),
						isDisplayed()));
		linearLayout2.check(matches(isDisplayed()));*/

		/*ViewInteraction textView = onView(
				allOf(withText("Send OSC to IP address:"),
						childAtPosition(
								allOf(withId(R.id.remote_ip),
										childAtPosition(
												withId(R.id.remote_address),
												0)),
								0),
						isDisplayed()));
		textView.check(matches(withText("Send OSC to IP address:")));*/

		/*ViewInteraction editText = onView(
				allOf(withId(R.id.remote_ip_field), withText("192.168.1.1"),
						childAtPosition(
								allOf(withId(R.id.remote_ip),
										childAtPosition(
												withId(R.id.remote_address),
												0)),
								1),
						isDisplayed()));
		editText.check(matches(withText("192.168.1.1")));*/

		ViewInteraction linearLayout3 = onView(
				allOf(withId(R.id.remote_port),
						childAtPosition(
								allOf(withId(R.id.remote_address),
										childAtPosition(
												withId(R.id.network_settings),
												0)),
								1),
						isDisplayed()));
		linearLayout3.check(matches(isDisplayed()));

		ViewInteraction textView2 = onView(
				allOf(withText("Send OSC to port:"),
						childAtPosition(
								allOf(withId(R.id.remote_port),
										childAtPosition(
												withId(R.id.remote_address),
												1)),
								0),
						isDisplayed()));
		textView2.check(matches(withText("Send OSC to port:")));

		/*ViewInteraction editText2 = onView(
				allOf(withId(R.id.remote_port_field), withText("57120"),
						childAtPosition(
								allOf(withId(R.id.remote_port),
										childAtPosition(
												withId(R.id.remote_address),
												1)),
								1),
						isDisplayed()));
		editText2.check(matches(withText("57120")));*/

		ViewInteraction linearLayout4 = onView(
				allOf(withId(R.id.device_address),
						childAtPosition(
								allOf(withId(R.id.network_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								1),
						isDisplayed()));
		linearLayout4.check(matches(isDisplayed()));

		/*ViewInteraction linearLayout5 = onView(
				allOf(withId(R.id.device_ip),
						childAtPosition(
								allOf(withId(R.id.device_address),
										childAtPosition(
												withId(R.id.network_settings),
												1)),
								0),
						isDisplayed()));
		linearLayout5.check(matches(isDisplayed()));*/

		/*ViewInteraction textView3 = onView(
				allOf(withText("Device IP address:"),
						childAtPosition(
								allOf(withId(R.id.device_ip),
										childAtPosition(
												withId(R.id.device_address),
												0)),
								0),
						isDisplayed()));
		textView3.check(matches(withText("Device IP address:")));*/

		/*ViewInteraction textView4 = onView(
				allOf(withId(R.id.device_ip_address), withText("192.168.1.2"),
						childAtPosition(
								allOf(withId(R.id.device_ip),
										childAtPosition(
												withId(R.id.device_address),
												0)),
								1),
						isDisplayed()));
		textView4.check(matches(isDisplayed()));*/

		/*ViewInteraction linearLayout6 = onView(
				allOf(withId(R.id.device_port),
						childAtPosition(
								allOf(withId(R.id.device_address),
										childAtPosition(
												withId(R.id.network_settings),
												1)),
								1),
						isDisplayed()));
		linearLayout6.check(matches(isDisplayed()));*/

		/*ViewInteraction textView5 = onView(
				allOf(withText("Device port:"),
						childAtPosition(
								allOf(withId(R.id.device_port),
										childAtPosition(
												withId(R.id.device_address),
												1)),
								0),
						isDisplayed()));
		textView5.check(matches(withText("Device port:")));*/

		/*ViewInteraction editText3 = onView(
				allOf(withId(R.id.device_port_field), withText("32000"),
						childAtPosition(
								allOf(withId(R.id.device_port),
										childAtPosition(
												withId(R.id.device_address),
												1)),
								1),
						isDisplayed()));
		editText3.check(matches(withText("32000")));*/

		ViewInteraction textView6 = onView(
				allOf(withText("Root command name:"),
						childAtPosition(
								allOf(withId(R.id.network_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								2),
						isDisplayed()));
		textView6.check(matches(withText("Root command name:")));

		ViewInteraction linearLayout7 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.network_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						3),
						isDisplayed()));
		linearLayout7.check(matches(isDisplayed()));

		ViewInteraction textView7 = onView(
				allOf(withText("/"),
						childAtPosition(
								childAtPosition(
										withId(R.id.network_settings),
										3),
								0),
						isDisplayed()));
		textView7.check(matches(withText("/")));

		ViewInteraction editText4 = onView(
				allOf(withId(R.id.root_cmd_name_field), withText("vosc"),
						childAtPosition(
								childAtPosition(
										withId(R.id.network_settings),
										3),
								1),
						isDisplayed()));
		editText4.check(matches(withText("vosc")));

		ViewInteraction textView8 = onView(
				allOf(withText("/<sub_cmd_name>"),
						childAtPosition(
								childAtPosition(
										withId(R.id.network_settings),
										3),
								2),
						isDisplayed()));
		textView8.check(matches(withText("/<sub_cmd_name>")));

		ViewInteraction textView9 = onView(
				allOf(withText("/<sub_cmd_name>"),
						childAtPosition(
								childAtPosition(
										withId(R.id.network_settings),
										3),
								2),
						isDisplayed()));
		textView9.check(matches(withText("/<sub_cmd_name>")));

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
