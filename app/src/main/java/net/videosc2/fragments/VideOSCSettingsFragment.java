package net.videosc2.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.utilities.VideOSCUI;
import net.videosc2.utilities.VideOSCUIHelpers;

import java.util.ArrayList;

/**
 * Created by stefan on 12.03.17.
 */

public class VideOSCSettingsFragment extends VideOSCBaseFragment {
	private final static String TAG = "VideOSCSettingsFragment";
	private ArrayAdapter<String> itemsAdapter;

	public VideOSCSettingsFragment() {}

	public static VideOSCSettingsFragment newInstance() {
		VideOSCSettingsFragment s = new VideOSCSettingsFragment();
		Bundle args = new Bundle();
		s.setArguments(args);
		return s;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
	                         Bundle savedInstanceState) {
		// the background scrollview - dark transparent, no content
		final ScrollView bg = (ScrollView) inflater.inflate(R.layout.settings_background_scroll, container, false);
		// the view holding the main selection of settings
		View view = inflater.inflate(R.layout.settings_selection, bg, false);
		// the listview finally holding the links to different settings: network, resolution, sensors, about
		final ListView settingsListView = (ListView) view.findViewById(R.id.settings_selection_list);
		// the network settings form
		final View networkSettingsView = inflater.inflate(R.layout.network_settings, bg, false);
		// the resolution settings form
		final View resolutionSettingsView = inflater.inflate(R.layout.resolution_settings, bg, false);
		// the sensor settings form
		final View sensorSettingsView = inflater.inflate(R.layout.sensor_settings, bg, false);
		// about
		final View aboutView = inflater.inflate(R.layout.about, bg, false);
		final WebView webView = (WebView) aboutView.findViewById(R.id.html_about);
		// get the setting items for the main selection list and parse them into the layout
		String[] items = getResources().getStringArray(R.array.settings_select_items);
		itemsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.settings_selection_item, items);
		settingsListView.setAdapter(itemsAdapter);
		// does the fade-in animation really work?...
		VideOSCUIHelpers.setTransitionAnimation(bg);
		// add the scroll view background to the container (camView)
		container.addView(bg);

		settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				VideOSCMainActivity.isSettingsSecondLevel = true;
				VideOSCMainActivity.isSettingsFirstLevel = false;
				switch (i) {
					case 0:
						// network settings
						VideOSCUIHelpers.addView(networkSettingsView, bg);
						break;
					case 1:
						// resolution settings
						VideOSCUIHelpers.addView(resolutionSettingsView, bg);
						break;
					case 2:
						// sensor settings
						VideOSCUIHelpers.addView(sensorSettingsView, bg);
						break;
					case 3:
						// about
						webView.loadUrl("http://pustota.basislager.org/coding/videosc/");
						VideOSCUIHelpers.addView(aboutView, bg);
					default:
				}
				settingsListView.setVisibility(View.INVISIBLE);
			}
		});

		return view;
	}
}
