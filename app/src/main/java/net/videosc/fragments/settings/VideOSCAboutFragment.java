package net.videosc.fragments.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.videosc.R;
import net.videosc.fragments.VideOSCBaseFragment;

import java.util.Calendar;

import androidx.annotation.NonNull;

public class VideOSCAboutFragment extends VideOSCBaseFragment {
	private View mView;

	/**
	 * @param savedInstanceState
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
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.about, container, false);
		setYear(mView);

//		return super.onCreateView(inflater, container, savedInstanceState);
		return mView;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void setYear(View container) {
		Resources res = getResources();
		String dateString = res.getString(R.string.videosc_copyright);
		TextView tv = container.findViewById(R.id.videosc_copyright);
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);

		String formatted = String.format(dateString, String.valueOf(currentYear));
		tv.setText(formatted);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mView = null;
	}

}
