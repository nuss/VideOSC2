package net.videosc.fragments.settings;

import android.app.FragmentManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.utilities.VideOSCUIHelpers;

public class VideOSCSettingsListFragment extends VideOSCBaseFragment {
	private final static String TAG = "VideOSCSettingsList";

	public VideOSCSettingsListFragment() {}

	/**
	 * @param savedInstanceState
	 * @deprecated
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @deprecated
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final FragmentManager fragmentManager = getFragmentManager();
		final VideOSCNetworkSettingsFragment networkSettingsFragment = new VideOSCNetworkSettingsFragment();
		final VideOSCCameraFragment cameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
		final Camera.Parameters params = cameraView.mCamera.getParameters();
		// the background scrollview - dark transparent, no content
		final ScrollView bg = (ScrollView) inflater.inflate(R.layout.settings_background_scroll, container, false);
		// the view holding the main selection of settings
		final View view = inflater.inflate(R.layout.settings_container, bg, false);
		final ListView settingsListView = view.findViewById(R.id.settings_list);
		final VideOSCApplication app = (VideOSCApplication) getActivity().getApplicationContext();

		// get the setting items for the main selection list and parse them into the layout
		final String[] items = getResources().getStringArray(R.array.settings_select_items);
		final ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.settings_selection_item, items);
		settingsListView.setAdapter(itemsAdapter);
		// does the fade-in animation really work?...
		VideOSCUIHelpers.setTransitionAnimation(bg);
		// add the scroll view background to the container (camView)
		container.addView(bg);
		view.setVisibility(View.VISIBLE);

		settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				int container;
				if (app.getIsTablet())
					container = R.id.settings_container;
				else container = R.id.settings_list;

				switch (i) {
					case 0:
						fragmentManager.beginTransaction()
								.replace(container, networkSettingsFragment)
								.commit();
						break;
					case 1:
						break;
					case 2:
						break;
					case 3:
						break;
					case 4:
						break;
					case 5:
						break;
					default:
				}
			}
		});

		return view;
	}

	/**
	 * @deprecated
	 */
	@Override
	public void onPause() {
		super.onPause();
	}


}
