package net.videosc.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.videosc.R;
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

		final View view = inflater.inflate(R.layout.debug_settings, container, false);

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
