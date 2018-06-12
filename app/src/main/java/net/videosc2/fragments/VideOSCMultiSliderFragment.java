package net.videosc2.fragments;

import android.app.FragmentManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.utilities.VideOSCUIHelpers;
import net.videosc2.utilities.enums.RGBModes;
import net.videosc2.views.SliderBar;
import net.videosc2.views.VideOSCMultiSliderView;
import java.util.ArrayList;

/**
 * Created by stefan on 19.05.18, package net.videosc2.views, project VideOSC22.
 */
public class VideOSCMultiSliderFragment extends VideOSCMSBaseFragment {
	private final static String TAG = "MultiSliderFragment";
	private VideOSCMultiSliderView mMSViewRight;
	private VideOSCMultiSliderView mMSViewLeft;

	// empty public constructor
	public VideOSCMultiSliderFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		int color = 0x66ffffff;
		int pixelVal = 255;
		VideOSCApplication app = (VideOSCApplication) getActivity().getApplication();
		Point resolution = app.getResolution();
		int numTotalPixels = resolution.x * resolution.y;

		mManager = getFragmentManager();

		Bundle argsBundle = this.getArguments();
		ArrayList<Integer> sliderNums = argsBundle.getIntegerArrayList("nums");
		int[] allColors = argsBundle.getIntArray("colors");

		View msContainer = inflater.inflate(R.layout.multislider_view, container, false);
		mMSViewLeft = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_left);
		mMSViewLeft.setValuesArray(numTotalPixels);
		mMSViewLeft.setContainerView(container);
		mOkCancel = (ViewGroup) inflater.inflate(R.layout.cancel_ok_buttons, container, false);

		assert allColors != null;
		int[] colors = new int[allColors.length];
		for (int i = 0; i < allColors.length; i++) {
			switch (app.getColorMode()) {
				case R:
					colors[i] = (allColors[i] >> 16) & 0xFF;
					break;
				case G:
					colors[i] = (allColors[i] >> 8) & 0xFF;
					break;
				case B:
					colors[i] = allColors[i] & 0xFF;
					break;
			}
		}
		// colors are determining slider positions on the left
		mMSViewLeft.setColors(colors);

		mMSViewRight = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_right);
		mMSViewRight.setValuesArray(numTotalPixels);
		mMSViewRight.setContainerView(container);
		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mMSViewLeft.getLayoutParams();
		int topMargin = lp.topMargin;
		float density = app.getScreenDensity();

		int displayHeight = app.getDimensions().y;
		mMSViewLeft.setParentTopMargin(topMargin);
		mMSViewLeft.setDisplayHeight(displayHeight);
		mMSViewRight.setParentTopMargin(topMargin);
		mMSViewRight.setDisplayHeight(displayHeight);

		switch (app.getColorMode()) {
			case R:
				color = 0x99ff0000;
				break;
			case G:
				color = 0x9900ff00;
				break;
			case B:
				color = 0x990000ff;
				break;
		}

		assert sliderNums != null;

		for (int i = 0; i < sliderNums.size(); i++) {
			SliderBar barLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barLeft.mScreenDensity = density;
			barLeft.setNum(String.valueOf(sliderNums.get(i)));
			barLeft.setColor(color);
			mMSViewLeft.mBars.add(barLeft);
			mMSViewLeft.addView(barLeft);
			SliderBar barRight = new SliderBar(getActivity());
			barRight.mScreenDensity = density;
			barRight.setNum(String.valueOf(sliderNums.get(i)));
			barRight.setColor(color);
			// TODO: mix value must have been stored internally somewhere, otherwise it should just be 1.0
			mMSViewRight.mBars.add(barRight);
			mMSViewRight.addView(barRight);
		}

		setSliderProps(sliderNums);
		VideOSCUIHelpers.addView(mOkCancel, container);
		Resources res = getResources();
		Drawable shape = res.getDrawable(R.drawable.black_rounded_rect);
		mOkCancel.setBackground(shape);

		mContainer = container;
		mFragment = this;

		return msContainer;
	}

	/*@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setUserVisibleHint(true);
	}

	@Override
	public void setUserVisibleHint(final boolean visible) {
		super.setUserVisibleHint(visible);
		if (visible) {
			Log.d(TAG, "menu visible");
			mOkCancel.bringToFront();
		}
	}*/

	private void setSliderProps(ArrayList<Integer> sliderNums) {
		mMSViewLeft.setSliderNums(sliderNums);
		mMSViewRight.setSliderNums(sliderNums);
	}
}
