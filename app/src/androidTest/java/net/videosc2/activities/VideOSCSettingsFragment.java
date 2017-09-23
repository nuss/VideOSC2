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
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VideOSCSettingsFragment {

	@Rule
	public ActivityTestRule<VideOSCMainActivity> mActivityTestRule = new ActivityTestRule<>(VideOSCMainActivity.class);

	@Test
	public void videOSCSettingsFragment() {
		ViewInteraction appCompatImageView = onView(
				allOf(withId(R.id.tool), withContentDescription("a tools menu item"),
						childAtPosition(
								allOf(withId(R.id.drawer),
										withParent(withId(R.id.drawer_layout))),
								6),
						isDisplayed()));
		appCompatImageView.perform(click());

		ViewInteraction scrollView = onView(
				allOf(withId(R.id.settings_background),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								0),
						isDisplayed()));
		scrollView.check(matches(isDisplayed()));

		ViewInteraction linearLayout = onView(
				allOf(withId(R.id.settings_selection),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								2),
						isDisplayed()));
		linearLayout.check(matches(isDisplayed()));

		ViewInteraction listView = onView(
				allOf(withId(R.id.settings_selection_list),
						childAtPosition(
								allOf(withId(R.id.settings_selection),
										childAtPosition(
												withId(R.id.camera_preview),
												2)),
								0),
						isDisplayed()));
		listView.check(matches(isDisplayed()));

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
				allOf(withId(R.id.settings_selection_item), withText("Debug Settings (temporary)"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										childAtPosition(
												withId(R.id.settings_selection),
												0)),
								3),
						isDisplayed()));
		textView3.check(matches(withText("Debug Settings (temporary)")));

		ViewInteraction textView4 = onView(
				allOf(withId(R.id.settings_selection_item), withText("About VideOSC"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										childAtPosition(
												withId(R.id.settings_selection),
												0)),
								4),
						isDisplayed()));
		textView4.check(matches(withText("About VideOSC")));

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

		ViewInteraction appCompatTextView = onView(
				allOf(withId(R.id.settings_selection_item), withText("Network Settings"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										withParent(withId(R.id.settings_selection))),
								0),
						isDisplayed()));
		appCompatTextView.perform(click());

		ViewInteraction scrollView2 = onView(
				allOf(withId(R.id.settings_background),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								0),
						isDisplayed()));
		scrollView2.check(matches(isDisplayed()));

		ViewInteraction linearLayout2 = onView(
				allOf(withId(R.id.network_settings),
						childAtPosition(
								allOf(withId(R.id.settings_background),
										childAtPosition(
												withId(R.id.camera_preview),
												0)),
								0),
						isDisplayed()));
		linearLayout2.check(matches(isDisplayed()));

		ViewInteraction linearLayout3 = onView(
				allOf(withId(R.id.remote_address),
						childAtPosition(
								allOf(withId(R.id.network_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								0),
						isDisplayed()));
		linearLayout3.check(matches(isDisplayed()));

		ViewInteraction linearLayout4 = onView(
				allOf(withId(R.id.remote_ip),
						childAtPosition(
								allOf(withId(R.id.remote_address),
										childAtPosition(
												withId(R.id.network_settings),
												0)),
								0),
						isDisplayed()));
		linearLayout4.check(matches(isDisplayed()));

		ViewInteraction textView6 = onView(
				allOf(withText("Send OSC to IP address:"),
						childAtPosition(
								allOf(withId(R.id.remote_ip),
										childAtPosition(
												withId(R.id.remote_address),
												0)),
								0),
						isDisplayed()));
		textView6.check(matches(withText("Send OSC to IP address:")));

		ViewInteraction editText = onView(
				allOf(withId(R.id.remote_ip_field), withText("192.168.1.1"),
						childAtPosition(
								allOf(withId(R.id.remote_ip),
										childAtPosition(
												withId(R.id.remote_address),
												0)),
								1),
						isDisplayed()));
		editText.check(matches(withText("192.168.1.1")));

		ViewInteraction linearLayout5 = onView(
				allOf(withId(R.id.remote_port),
						childAtPosition(
								allOf(withId(R.id.remote_address),
										childAtPosition(
												withId(R.id.network_settings),
												0)),
								1),
						isDisplayed()));
		linearLayout5.check(matches(isDisplayed()));

		ViewInteraction textView7 = onView(
				allOf(withText("Send OSC to port:"),
						childAtPosition(
								allOf(withId(R.id.remote_port),
										childAtPosition(
												withId(R.id.remote_address),
												1)),
								0),
						isDisplayed()));
		textView7.check(matches(withText("Send OSC to port:")));

		ViewInteraction editText2 = onView(
				allOf(withId(R.id.remote_port_field), withText("57120"),
						childAtPosition(
								allOf(withId(R.id.remote_port),
										childAtPosition(
												withId(R.id.remote_address),
												1)),
								1),
						isDisplayed()));
		editText2.check(matches(withText("57120")));

		ViewInteraction linearLayout6 = onView(
				allOf(withId(R.id.device_address),
						childAtPosition(
								allOf(withId(R.id.network_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								1),
						isDisplayed()));
		linearLayout6.check(matches(isDisplayed()));

		ViewInteraction linearLayout7 = onView(
				allOf(withId(R.id.device_ip),
						childAtPosition(
								allOf(withId(R.id.device_address),
										childAtPosition(
												withId(R.id.network_settings),
												1)),
								0),
						isDisplayed()));
		linearLayout7.check(matches(isDisplayed()));

		ViewInteraction textView8 = onView(
				allOf(withText("Device IP address:"),
						childAtPosition(
								allOf(withId(R.id.device_ip),
										childAtPosition(
												withId(R.id.device_address),
												0)),
								0),
						isDisplayed()));
		textView8.check(matches(withText("Device IP address:")));

		ViewInteraction textView9 = onView(
				allOf(withId(R.id.device_ip_address), withText("192.168.1.2"),
						childAtPosition(
								allOf(withId(R.id.device_ip),
										childAtPosition(
												withId(R.id.device_address),
												0)),
								1),
						isDisplayed()));
		textView9.check(matches(isDisplayed()));

		ViewInteraction linearLayout8 = onView(
				allOf(withId(R.id.device_port),
						childAtPosition(
								allOf(withId(R.id.device_address),
										childAtPosition(
												withId(R.id.network_settings),
												1)),
								1),
						isDisplayed()));
		linearLayout8.check(matches(isDisplayed()));

		ViewInteraction textView10 = onView(
				allOf(withText("Device port:"),
						childAtPosition(
								allOf(withId(R.id.device_port),
										childAtPosition(
												withId(R.id.device_address),
												1)),
								0),
						isDisplayed()));
		textView10.check(matches(withText("Device port:")));

		ViewInteraction editText3 = onView(
				allOf(withId(R.id.device_port_field), withText("32000"),
						childAtPosition(
								allOf(withId(R.id.device_port),
										childAtPosition(
												withId(R.id.device_address),
												1)),
								1),
						isDisplayed()));
		editText3.check(matches(withText("32000")));

		ViewInteraction textView11 = onView(
				allOf(withText("Root command name:"),
						childAtPosition(
								allOf(withId(R.id.network_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								2),
						isDisplayed()));
		textView11.check(matches(withText("Root command name:")));

		ViewInteraction linearLayout9 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.network_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						3),
						isDisplayed()));
		linearLayout9.check(matches(isDisplayed()));

		ViewInteraction textView12 = onView(
				allOf(withText("/"),
						childAtPosition(
								childAtPosition(
										withId(R.id.network_settings),
										3),
								0),
						isDisplayed()));
		textView12.check(matches(withText("/")));

		ViewInteraction editText4 = onView(
				allOf(withId(R.id.root_cmd_name_field), withText("vosc"),
						childAtPosition(
								childAtPosition(
										withId(R.id.network_settings),
										3),
								1),
						isDisplayed()));
		editText4.check(matches(withText("vosc")));

		ViewInteraction textView13 = onView(
				allOf(withText("/<sub_cmd_name>"),
						childAtPosition(
								childAtPosition(
										withId(R.id.network_settings),
										3),
								2),
						isDisplayed()));
		textView13.check(matches(withText("/<sub_cmd_name>")));

		ViewInteraction textView14 = onView(
				allOf(withText("/<sub_cmd_name>"),
						childAtPosition(
								childAtPosition(
										withId(R.id.network_settings),
										3),
								2),
						isDisplayed()));
		textView14.check(matches(withText("/<sub_cmd_name>")));

		pressBack();

		ViewInteraction appCompatTextView2 = onView(
				allOf(withId(R.id.settings_selection_item), withText("Resolution Settings"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										withParent(withId(R.id.settings_selection))),
								1),
						isDisplayed()));
		appCompatTextView2.perform(click());

		ViewInteraction scrollView3 = onView(
				allOf(withId(R.id.settings_background),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								0),
						isDisplayed()));
		scrollView3.check(matches(isDisplayed()));

		ViewInteraction linearLayout10 = onView(
				allOf(withId(R.id.resolution_settings),
						childAtPosition(
								allOf(withId(R.id.settings_background),
										childAtPosition(
												withId(R.id.camera_preview),
												0)),
								0),
						isDisplayed()));
		linearLayout10.check(matches(isDisplayed()));

		ViewInteraction linearLayout11 = onView(
				allOf(withId(R.id.resolution),
						childAtPosition(
								allOf(withId(R.id.resolution_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								0),
						isDisplayed()));
		linearLayout11.check(matches(isDisplayed()));

		ViewInteraction linearLayout12 = onView(
				allOf(withId(R.id.resolution_horizontal),
						childAtPosition(
								allOf(withId(R.id.resolution),
										childAtPosition(
												withId(R.id.resolution_settings),
												0)),
								0),
						isDisplayed()));
		linearLayout12.check(matches(isDisplayed()));

		ViewInteraction textView15 = onView(
				allOf(withText("Horizontal resolution:"),
						childAtPosition(
								allOf(withId(R.id.resolution_horizontal),
										childAtPosition(
												withId(R.id.resolution),
												0)),
								0),
						isDisplayed()));
		textView15.check(matches(withText("Horizontal resolution:")));

		ViewInteraction editText5 = onView(
				allOf(withId(R.id.resolution_horizontal_field), withText("7"),
						childAtPosition(
								allOf(withId(R.id.resolution_horizontal),
										childAtPosition(
												withId(R.id.resolution),
												0)),
								1),
						isDisplayed()));
		editText5.check(matches(withText("7")));

		ViewInteraction linearLayout13 = onView(
				allOf(withId(R.id.resolution_vertical),
						childAtPosition(
								allOf(withId(R.id.resolution),
										childAtPosition(
												withId(R.id.resolution_settings),
												0)),
								1),
						isDisplayed()));
		linearLayout13.check(matches(isDisplayed()));

		ViewInteraction textView16 = onView(
				allOf(withText("Vertical resolution:"),
						childAtPosition(
								allOf(withId(R.id.resolution_vertical),
										childAtPosition(
												withId(R.id.resolution),
												1)),
								0),
						isDisplayed()));
		textView16.check(matches(withText("Vertical resolution:")));

		ViewInteraction editText6 = onView(
				allOf(withId(R.id.resolution_vertical_field), withText("5"),
						childAtPosition(
								allOf(withId(R.id.resolution_vertical),
										childAtPosition(
												withId(R.id.resolution),
												1)),
								1),
						isDisplayed()));
		editText6.check(matches(withText("5")));

		ViewInteraction linearLayout14 = onView(
				allOf(withId(R.id.camera_output_params),
						childAtPosition(
								allOf(withId(R.id.resolution_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								1),
						isDisplayed()));
		linearLayout14.check(matches(isDisplayed()));

		ViewInteraction linearLayout15 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.camera_output_params),
								childAtPosition(
										withId(R.id.resolution_settings),
										1)),
						0),
						isDisplayed()));
		linearLayout15.check(matches(isDisplayed()));

		ViewInteraction textView17 = onView(
				allOf(withText("Set calculation period:"),
						childAtPosition(
								childAtPosition(
										withId(R.id.camera_output_params),
										0),
								0),
						isDisplayed()));
		textView17.check(matches(withText("Set calculation period:")));

		ViewInteraction editText7 = onView(
				allOf(withId(R.id.calulation_period_field), withText("1"),
						childAtPosition(
								childAtPosition(
										withId(R.id.camera_output_params),
										0),
								1),
						isDisplayed()));
		editText7.check(matches(withText("1")));

		ViewInteraction linearLayout16 = onView(
				allOf(withId(R.id.framerate_settings),
						childAtPosition(
								allOf(withId(R.id.camera_output_params),
										childAtPosition(
												withId(R.id.resolution_settings),
												1)),
								1),
						isDisplayed()));
		linearLayout16.check(matches(isDisplayed()));

		ViewInteraction textView18 = onView(
				allOf(withText("Select framerate (min/max fps):"),
						childAtPosition(
								allOf(withId(R.id.framerate_settings),
										childAtPosition(
												withId(R.id.camera_output_params),
												1)),
								0),
						isDisplayed()));
		textView18.check(matches(withText("Select framerate (min/max fps):")));

		ViewInteraction spinner = onView(
				allOf(withId(R.id.framerate_selection),
						childAtPosition(
								allOf(withId(R.id.framerate_settings),
										childAtPosition(
												withId(R.id.camera_output_params),
												1)),
								1),
						isDisplayed()));
		spinner.check(matches(isDisplayed()));

		ViewInteraction textView19 = onView(
				allOf(withId(R.id.framerate_selection_item), withText("15 / 15"),
						childAtPosition(
								allOf(withId(R.id.framerate_selection),
										childAtPosition(
												withId(R.id.framerate_settings),
												1)),
								0),
						isDisplayed()));
		textView19.check(matches(withText("15 / 15")));

		ViewInteraction linearLayout17 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.resolution_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						2),
						isDisplayed()));
		linearLayout17.check(matches(isDisplayed()));

		ViewInteraction switch_ = onView(
				allOf(withId(R.id.fix_exposure_checkbox),
						childAtPosition(
								childAtPosition(
										withId(R.id.resolution_settings),
										2),
								0),
						isDisplayed()));
		switch_.check(matches(isDisplayed()));

		ViewInteraction view = onView(
				allOf(childAtPosition(
						childAtPosition(
								withId(R.id.resolution_settings),
								2),
						1),
						isDisplayed()));
		view.check(matches(isDisplayed()));

		ViewInteraction switch_2 = onView(
				allOf(withId(R.id.normalize_output_checkbox),
						childAtPosition(
								childAtPosition(
										withId(R.id.resolution_settings),
										2),
								2),
						isDisplayed()));
		switch_2.check(matches(isDisplayed()));

		ViewInteraction view2 = onView(
				allOf(childAtPosition(
						childAtPosition(
								withId(R.id.resolution_settings),
								2),
						3),
						isDisplayed()));
		view2.check(matches(isDisplayed()));

		ViewInteraction switch_3 = onView(
				allOf(withId(R.id.remember_activated_checkbox),
						childAtPosition(
								childAtPosition(
										withId(R.id.resolution_settings),
										2),
								4),
						isDisplayed()));
		switch_3.check(matches(isDisplayed()));

		ViewInteraction switch_4 = onView(
				allOf(withId(R.id.fix_exposure_checkbox), withText("Fix exposure temporarily (will be reset on restart)")));
		switch_4.perform(scrollTo(), click());

		ViewInteraction linearLayout18 = onView(
				allOf(withId(R.id.fix_exposure_button_layout),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								2),
						isDisplayed()));
		linearLayout18.check(matches(isDisplayed()));

		ViewInteraction imageButton = onView(
				allOf(withId(R.id.fix_exposure_cancel), withContentDescription("Don't fix exposure"),
						childAtPosition(
								allOf(withId(R.id.fix_exposure_button_layout),
										childAtPosition(
												withId(R.id.camera_preview),
												2)),
								0),
						isDisplayed()));
		imageButton.check(matches(isDisplayed()));

		ViewInteraction imageButton2 = onView(
				allOf(withId(R.id.fix_exposure_button), withContentDescription("Set exposure to fixed"),
						childAtPosition(
								allOf(withId(R.id.fix_exposure_button_layout),
										childAtPosition(
												withId(R.id.camera_preview),
												2)),
								1),
						isDisplayed()));
		imageButton2.check(matches(isDisplayed()));

		ViewInteraction imageButton3 = onView(
				allOf(withId(R.id.fix_exposure_button), withContentDescription("Set exposure to fixed"),
						childAtPosition(
								allOf(withId(R.id.fix_exposure_button_layout),
										childAtPosition(
												withId(R.id.camera_preview),
												2)),
								1),
						isDisplayed()));
		imageButton3.check(matches(isDisplayed()));

		pressBack();

		pressBack();

		ViewInteraction appCompatTextView3 = onView(
				allOf(withId(R.id.settings_selection_item), withText("Sensor Settings"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										withParent(withId(R.id.settings_selection))),
								2),
						isDisplayed()));
		appCompatTextView3.perform(click());

		ViewInteraction scrollView4 = onView(
				allOf(withId(R.id.settings_background),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								0),
						isDisplayed()));
		scrollView4.check(matches(isDisplayed()));

		ViewInteraction linearLayout19 = onView(
				allOf(withId(R.id.sensor_settings),
						childAtPosition(
								allOf(withId(R.id.settings_background),
										childAtPosition(
												withId(R.id.camera_preview),
												0)),
								0),
						isDisplayed()));
		linearLayout19.check(matches(isDisplayed()));

		ViewInteraction switch_5 = onView(
				allOf(withId(R.id.orientation_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								0),
						isDisplayed()));
		switch_5.check(matches(isDisplayed()));

		ViewInteraction view3 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						1),
						isDisplayed()));
		view3.check(matches(isDisplayed()));

		ViewInteraction switch_6 = onView(
				allOf(withId(R.id.accelerometer),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								2),
						isDisplayed()));
		switch_6.check(matches(isDisplayed()));

		ViewInteraction view4 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						3),
						isDisplayed()));
		view4.check(matches(isDisplayed()));

		ViewInteraction switch_7 = onView(
				allOf(withId(R.id.linear_acceleration),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								4),
						isDisplayed()));
		switch_7.check(matches(isDisplayed()));

		ViewInteraction view5 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						5),
						isDisplayed()));
		view5.check(matches(isDisplayed()));

		ViewInteraction switch_8 = onView(
				allOf(withId(R.id.magnetic_field),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								6),
						isDisplayed()));
		switch_8.check(matches(isDisplayed()));

		ViewInteraction view6 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						7),
						isDisplayed()));
		view6.check(matches(isDisplayed()));

		ViewInteraction switch_9 = onView(
				allOf(withId(R.id.gravity_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								8),
						isDisplayed()));
		switch_9.check(matches(isDisplayed()));

		ViewInteraction view7 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						9),
						isDisplayed()));
		view7.check(matches(isDisplayed()));

		ViewInteraction switch_10 = onView(
				allOf(withId(R.id.proximity_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								10),
						isDisplayed()));
		switch_10.check(matches(isDisplayed()));

		ViewInteraction view8 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						11),
						isDisplayed()));
		view8.check(matches(isDisplayed()));

		ViewInteraction switch_11 = onView(
				allOf(withId(R.id.light_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								12),
						isDisplayed()));
		switch_11.check(matches(isDisplayed()));

		ViewInteraction switch_12 = onView(
				allOf(withId(R.id.light_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								12),
						isDisplayed()));
		switch_12.check(matches(isDisplayed()));

		ViewInteraction view9 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						13),
						isDisplayed()));
		view9.check(matches(isDisplayed()));

		ViewInteraction switch_13 = onView(
				allOf(withId(R.id.air_pressure_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								14),
						isDisplayed()));
		switch_13.check(matches(isDisplayed()));

		ViewInteraction view10 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						15),
						isDisplayed()));
		view10.check(matches(isDisplayed()));

		ViewInteraction switch_14 = onView(
				allOf(withId(R.id.temperature_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								16),
						isDisplayed()));
		switch_14.check(matches(isDisplayed()));

		ViewInteraction view11 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						17),
						isDisplayed()));
		view11.check(matches(isDisplayed()));

		ViewInteraction switch_15 = onView(
				allOf(withId(R.id.humidity_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								18),
						isDisplayed()));
		switch_15.check(matches(isDisplayed()));

		ViewInteraction view12 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.sensor_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						19),
						isDisplayed()));
		view12.check(matches(isDisplayed()));

		ViewInteraction switch_16 = onView(
				allOf(withId(R.id.geo_loc_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								20),
						isDisplayed()));
		switch_16.check(matches(isDisplayed()));

		ViewInteraction switch_17 = onView(
				allOf(withId(R.id.geo_loc_sensor),
						childAtPosition(
								allOf(withId(R.id.sensor_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								20),
						isDisplayed()));
		switch_17.check(matches(isDisplayed()));

		pressBack();

		ViewInteraction appCompatTextView4 = onView(
				allOf(withId(R.id.settings_selection_item), withText("Debug Settings (temporary)"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										withParent(withId(R.id.settings_selection))),
								3),
						isDisplayed()));
		appCompatTextView4.perform(click());

		ViewInteraction scrollView5 = onView(
				allOf(withId(R.id.settings_background),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								0),
						isDisplayed()));
		scrollView5.check(matches(isDisplayed()));

		ViewInteraction linearLayout20 = onView(
				allOf(withId(R.id.debug_settings),
						childAtPosition(
								allOf(withId(R.id.settings_background),
										childAtPosition(
												withId(R.id.camera_preview),
												0)),
								0),
						isDisplayed()));
		linearLayout20.check(matches(isDisplayed()));

		ViewInteraction textView20 = onView(
				allOf(withId(R.id.debug_explanation), withText("The following settings will not get stored permanently. They will get reset with every restart of VideOSC."),
						childAtPosition(
								allOf(withId(R.id.debug_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								0),
						isDisplayed()));
		textView20.check(matches(withText("The following settings will not get stored permanently. They will get reset with every restart of VideOSC.")));

		ViewInteraction view13 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.debug_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						1),
						isDisplayed()));
		view13.check(matches(isDisplayed()));

		ViewInteraction switch_18 = onView(
				allOf(withId(R.id.hide_pixel_image),
						childAtPosition(
								allOf(withId(R.id.debug_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								2),
						isDisplayed()));
		switch_18.check(matches(isDisplayed()));

		ViewInteraction view14 = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.debug_settings),
								childAtPosition(
										withId(R.id.settings_background),
										0)),
						3),
						isDisplayed()));
		view14.check(matches(isDisplayed()));

		ViewInteraction switch_19 = onView(
				allOf(withId(R.id.add_packet_drops),
						childAtPosition(
								allOf(withId(R.id.debug_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								4),
						isDisplayed()));
		switch_19.check(matches(isDisplayed()));

		ViewInteraction switch_20 = onView(
				allOf(withId(R.id.add_packet_drops),
						childAtPosition(
								allOf(withId(R.id.debug_settings),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								4),
						isDisplayed()));
		switch_20.check(matches(isDisplayed()));

		pressBack();

		ViewInteraction appCompatTextView5 = onView(
				allOf(withId(R.id.settings_selection_item), withText("About VideOSC"),
						childAtPosition(
								allOf(withId(R.id.settings_selection_list),
										withParent(withId(R.id.settings_selection))),
								4),
						isDisplayed()));
		appCompatTextView5.perform(click());

		ViewInteraction scrollView6 = onView(
				allOf(withId(R.id.settings_background),
						childAtPosition(
								allOf(withId(R.id.camera_preview),
										childAtPosition(
												withId(R.id.drawer_layout),
												0)),
								0),
						isDisplayed()));
		scrollView6.check(matches(isDisplayed()));

		ViewInteraction linearLayout21 = onView(
				allOf(withId(R.id.about),
						childAtPosition(
								allOf(withId(R.id.settings_background),
										childAtPosition(
												withId(R.id.camera_preview),
												0)),
								0),
						isDisplayed()));
		linearLayout21.check(matches(isDisplayed()));

		ViewInteraction webView = onView(
				allOf(withId(R.id.html_about),
						childAtPosition(
								allOf(withId(R.id.about),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								0),
						isDisplayed()));
		webView.check(matches(isDisplayed()));

		ViewInteraction webView2 = onView(
				allOf(withId(R.id.html_about),
						childAtPosition(
								allOf(withId(R.id.about),
										childAtPosition(
												withId(R.id.settings_background),
												0)),
								0),
						isDisplayed()));
		webView2.check(matches(isDisplayed()));

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
