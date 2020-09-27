package net.videosc.fragments.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.fragments.VideOSCBaseFragment;

public class VideOSCDebugSettingsFragment extends VideOSCBaseFragment {
	final private static String TAG = "DebugSettingsFragment";
	private final VideOSCMainActivity mActivity;
	private View mView;

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
		setRetainInstance(true);
	}

	/**
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @deprecated
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();

		mView = inflater.inflate(R.layout.debug_settings, container, false);

		final SwitchCompat hidePixelImageCB = mView.findViewById(R.id.hide_pixel_image);
		final SwitchCompat debugPixelOscSendingCB = mView.findViewById(R.id.add_packet_drops);
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
		return mView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mView = null;
	}

	/**
	 * @deprecated
	 */
	@Override
	public void onPause() {
		super.onPause();
	}
}
