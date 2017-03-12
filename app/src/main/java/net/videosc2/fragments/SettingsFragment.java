package net.videosc2.fragments;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by stefan on 12.03.17.
 */

public class SettingsFragment extends Fragment {

	public SettingsFragment() {

	}

	public static SettingsFragment newInstance() {
		SettingsFragment s = new SettingsFragment();
		Bundle args = new Bundle();
		s.setArguments(args);
		return s;
	}


}
