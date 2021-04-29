package net.videosc.fragments;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.utilities.VideOSCUIHelpers;
import net.videosc.views.SliderBar;
import net.videosc.views.VideOSCMultiSliderView;

import java.util.ArrayList;

/**
 * Created by stefan on 19.05.18, package net.videosc.views, project VideOSC22.
 */
public class VideOSCMultiSliderFragment extends VideOSCMSBaseFragment {
	private final static String TAG = "MultiSliderFragment";
	private VideOSCMultiSliderView mMSViewRight;
	private VideOSCMultiSliderView mMSViewLeft;

	// empty public constructor
	public VideOSCMultiSliderFragment() { }

	public VideOSCMultiSliderFragment(Context context) {
		super(context);
		this.mActivity = (VideOSCMainActivity) context;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		this.mContainer = container;
		this.mInflater = inflater;
		return inflater.inflate(R.layout.multislider_view, container, false);
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
		int color = 0x66ffffff;
		double[] vals = new double[]{};
		double[] mixVals = new double[]{};
		final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
		final Point resolution = app.getResolution();
		final int numTotalPixels = resolution.x * resolution.y;

		mManager = getFragmentManager();

		final Bundle argsBundle = this.getArguments();
		assert argsBundle != null;
		final ArrayList<Integer> sliderNums = argsBundle.getIntegerArrayList("nums");
		assert sliderNums != null;
		mNumSliders = sliderNums.size();
		switch (app.getColorMode()) {
			case R:
				vals = argsBundle.getDoubleArray("redVals");
				mixVals = argsBundle.getDoubleArray("redMixVals");
				break;
			case G:
				vals = argsBundle.getDoubleArray("greenVals");
				mixVals = argsBundle.getDoubleArray("greenMixVals");
				break;
			case B:
				vals = argsBundle.getDoubleArray("blueVals");
				mixVals = argsBundle.getDoubleArray("blueMixVals");
				break;
		}

		mMSViewLeft = view.findViewById(R.id.multislider_view_left);
		mMSViewLeft.setValuesArray(numTotalPixels);
		mMSViewLeft.setContainerView(mContainer);

		mMSButtons = mInflater.inflate(R.layout.multislider_buttons, mContainer, false);
		mLabelsView = mInflater.inflate(R.layout.multislider_labels, mContainer, false);

		mMSViewRight = view.findViewById(R.id.multislider_view_right);
		mMSViewRight.setValuesArray(numTotalPixels);
		mMSViewRight.setContainerView(mContainer);

		// colors are determining slider positions on the left
		mMSViewLeft.setValues(vals);
		mMSViewRight.setValues(mixVals);

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

		for (int num : sliderNums) {
			SliderBar barLeft = new SliderBar(mActivity);
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barLeft.mScreenDensity = density;
			barLeft.setNum(String.valueOf(num));
			barLeft.setColor(color);
			mMSViewLeft.mBars.add(barLeft);
			mMSViewLeft.addView(barLeft);
			SliderBar barRight = new SliderBar(mActivity);
			barRight.mScreenDensity = density;
			barRight.setNum(String.valueOf(num));
			barRight.setColor(color);
			mMSViewRight.mBars.add(barRight);
			mMSViewRight.addView(barRight);
		}

		setSliderProps(sliderNums);

		VideOSCUIHelpers.addView(mMSButtons, mContainer);
		VideOSCUIHelpers.addView(mLabelsView, mContainer);

		mFragment = this;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (createViewCallback != null) {
			createViewCallback.onCreateView();
			createViewCallback = null;
		}
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

	private void setSliderProps(ArrayList<Integer> sliderNums) {
		mMSViewLeft.setSliderNums(sliderNums);
		mMSViewRight.setSliderNums(sliderNums);
	}
}
