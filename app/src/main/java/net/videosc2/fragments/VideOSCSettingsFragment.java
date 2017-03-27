package net.videosc2.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

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
		View view = inflater.inflate(R.layout.settings_selection, container, false);
		final ListView settingsListView = (ListView) view.findViewById(R.id.settings_selection_list);
		final View networkSettingsView = inflater.inflate(R.layout.network_settings, container, false);
		final View resolutionSettingsView = inflater.inflate(R.layout.resolution_settings, container, false);
		final View sensorSettingsView = inflater.inflate(R.layout.sensor_settings, container, false);
		String[] items = getResources().getStringArray(R.array.settings_select_items);
		itemsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.settings_selection_item, items);
		VideOSCUIHelpers.setTransitionAnimation(container);
		settingsListView.setAdapter(itemsAdapter);

		settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				VideOSCMainActivity.isSettingsSecondLevel = true;
				VideOSCMainActivity.isSettingsFirstLevel = false;
				switch (i) {
					case 0:
						// network settings
						VideOSCUIHelpers.addView(networkSettingsView, container);
						break;
					case 1:
						// resolution settings
						VideOSCUIHelpers.addView(resolutionSettingsView, container);
						break;
					case 2:
						// sensor settings
						VideOSCUIHelpers.addView(sensorSettingsView, container);
						break;
					default:
				}
				settingsListView.setVisibility(View.INVISIBLE);
			}
		});

		return view;
	}
}
