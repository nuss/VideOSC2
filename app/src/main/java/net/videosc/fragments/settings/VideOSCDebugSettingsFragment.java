package net.videosc.fragments.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.fragments.VideOSCBaseFragment;

public class VideOSCDebugSettingsFragment extends VideOSCBaseFragment {
	final private static String TAG = "DebugSettingsFragment";

    public VideOSCDebugSettingsFragment(Context context) {
    	this.mActivity = (VideOSCMainActivity) context;
    }

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
		return inflater.inflate(R.layout.debug_settings, container, false);
	}

	/**
	 * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 * has returned, but before any saved state has been restored in to the view.
	 * This gives subclasses a chance to initialize themselves once
	 * they know their view hierarchy has been completely created.  The fragment's
	 * view hierarchy is not however attached to its parent at this point.
	 *
	 * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
	 * @param savedInstanceState If non-null, this fragment is being re-constructed
	 */
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
		final SwitchCompat hidePixelImageCB = view.findViewById(R.id.hide_pixel_image);
		final SwitchCompat debugPixelOscSendingCB = view.findViewById(R.id.add_packet_drops);
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
	}

	/**
	 * Called when the fragment is no longer attached to its activity.  This
	 * is called after {@link #onDestroy()}.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		this.mActivity = null;
	}
}
