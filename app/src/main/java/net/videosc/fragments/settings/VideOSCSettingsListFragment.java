package net.videosc.fragments.settings;

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
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.utilities.VideOSCUIHelpers;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

public class VideOSCSettingsListFragment extends VideOSCBaseFragment {
	private final static String TAG = "VideOSCSettingsList";

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
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final FragmentManager fragmentManager = getFragmentManager();
		final VideOSCNetworkSettingsFragment networkSettingsFragment = new VideOSCNetworkSettingsFragment();
		final VideOSCResolutionSettingsFragment resolutionSettingsFragment = new VideOSCResolutionSettingsFragment();
		final VideOSCSensorSettingsFragment sensorSettingsFragment = new VideOSCSensorSettingsFragment();
		final VideOSCDebugSettingsFragment debugSettingsFragment = new VideOSCDebugSettingsFragment();
		final VideOSCAboutFragment aboutFragment = new VideOSCAboutFragment();
		assert fragmentManager != null;
		final VideOSCCameraFragment cameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
		assert cameraView != null;
		final Camera.Parameters params = cameraView.mCamera.getParameters();
		// the background scrollview - dark transparent, no content
		final ScrollView bg = (ScrollView) inflater.inflate(R.layout.settings_background_scroll, container, false);
		// the view holding the main selection of settings
		final View view = inflater.inflate(R.layout.settings_container, bg, false);
		final ListView settingsListView = view.findViewById(R.id.settings_list);
		final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
		assert activity != null;
		final VideOSCApplication app = (VideOSCApplication) activity.getApplicationContext();

		// get the setting items for the main selection list and parse them into the layout
		final String[] items = getResources().getStringArray(R.array.settings_select_items);
		final ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(activity, R.layout.settings_selection_item, items);
		settingsListView.setAdapter(itemsAdapter);
		// does the fade-in animation really work?...
		VideOSCUIHelpers.setTransitionAnimation(bg);
		// add the scroll view background to the container (camView)
		container.addView(bg);
		view.setVisibility(View.VISIBLE);

		settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				if (!app.getIsTablet())
					settingsListView.setVisibility(View.INVISIBLE);
				switch (i) {
					case 0:
						fragmentManager.beginTransaction()
								.replace(R.id.settings_container, networkSettingsFragment)
								.commit();
						break;
					case 1:
						fragmentManager.beginTransaction()
								.replace(R.id.settings_container, resolutionSettingsFragment)
								.commit();
						break;
					case 2:
						fragmentManager.beginTransaction()
								.replace(R.id.settings_container, sensorSettingsFragment)
								.commit();
						break;
					case 3:
						fragmentManager.beginTransaction()
								.replace(R.id.settings_container, debugSettingsFragment)
								.commit();
						break;
					case 4:
						fragmentManager.beginTransaction()
								.replace(R.id.settings_container, aboutFragment)
								.commit();
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
