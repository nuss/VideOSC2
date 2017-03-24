package net.videosc2.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.videosc2.R;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.settings_selection, container, false);
		Log.d(TAG, "the activity: " + getActivity());
		String[] items = getResources().getStringArray(R.array.settings_select_items);
		itemsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.settings_selection_item, items);
		VideOSCUIHelpers.setTransitionAnimation(container);
		ListView settingsListView = (ListView) view.findViewById(R.id.settings_selection_list);
		settingsListView.setAdapter(itemsAdapter);

		return view;
	}
}
