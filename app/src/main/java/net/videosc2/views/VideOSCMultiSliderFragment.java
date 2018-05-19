package net.videosc2.views;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.fragments.VideOSCBaseFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by stefan on 19.05.18, package net.videosc2.views, project VideOSC22.
 */
public class VideOSCMultiSliderFragment extends VideOSCBaseFragment {
	private VideOSCApplication mApp;

	// empty public constructor
	public VideOSCMultiSliderFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mApp = (VideOSCApplication) getActivity().getApplication();
		return inflater.inflate(R.layout.multislider_view, container, false);
	}

	public void addSliders(HashMap<SliderBar, Integer> sliders) {
		Point dimensions = mApp.getDimensions();

		Iterator iterator = sliders.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			// TODO

			iterator.remove();
		}
	}
}
