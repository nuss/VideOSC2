package net.videosc.fragments.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.videosc.R;
import net.videosc.fragments.VideOSCBaseFragment;

import java.util.Calendar;

public class VideOSCAboutFragment extends VideOSCBaseFragment {

	public VideOSCAboutFragment() { }

	/**
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setRetainInstance(true);
	}

	/**
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.about, container, false);
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
		setYear(view);
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
}
