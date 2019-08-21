package net.videosc.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

public class VideOSCDebugSettingsFragment extends VideOSCBaseFragment {
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
		assert fragmentManager != null;
		final VideOSCCameraFragment cameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
		final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
		assert activity != null;
		final VideOSCApplication app = (VideOSCApplication) activity.getApplication();

		final View view = inflater.inflate(R.layout.debug_settings, container, false);

		final Switch hidePixelImageCB = view.findViewById(R.id.hide_pixel_image);
		final Switch debugPixelOscSendingCB = view.findViewById(R.id.add_packet_drops);
		hidePixelImageCB.setChecked(app.getPixelImageHidden());
		debugPixelOscSendingCB.setChecked(VideOSCApplication.getDebugPixelOsc());

		hidePixelImageCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				app.setPixelImageHidden(isChecked);
			}
		});

		debugPixelOscSendingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				VideOSCApplication.setDebugPixelOsc(isChecked);
			}
		});


//		return super.onCreateView(inflater, container, savedInstanceState);
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
