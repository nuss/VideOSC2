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
import java.util.Objects;

public class VideOSCMultiSliderGroupFragment extends VideOSCMSBaseFragment {
    private final static String TAG = VideOSCMultiSliderGroupFragment.class.getSimpleName();
    private VideOSCMultiSliderView mMSViewRight;
    private VideOSCMultiSliderView mMSViewLeft;

    public VideOSCMultiSliderGroupFragment() { }

    public VideOSCMultiSliderGroupFragment(Context context) {
        super(context);
        this.mActivity = (VideOSCMainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mContainer = container;
        this.mInflater = inflater;
        return inflater.inflate(R.layout.multislider_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
        final Point resolution = app.getResolution();
        final int numTotalPixels = resolution.x * resolution.y;
        final int red = 0x99ff0000;
        final int green = 0x9900ff00;
        final int blue = 0x990000ff;

        mManager = getParentFragmentManager();

        final Bundle argsBundle = this.getArguments();
        assert argsBundle != null;

        final double[] vals = argsBundle.getDoubleArray("values");
        final double[] mixVals = argsBundle.getDoubleArray("mixValues");

        final ArrayList<Integer> sliderNums = argsBundle.getIntegerArrayList("pixelIds");
        final ArrayList<String> sliderLabels = argsBundle.getStringArrayList("labels");
        final ArrayList<Integer> sliderOrder = argsBundle.getIntegerArrayList("sliderOrder");
        final ArrayList<Integer> colorChannels = argsBundle.getIntegerArrayList("colorChannels");

        mMSViewLeft = view.findViewById(R.id.multislider_view_left);
        mMSViewLeft.setValuesArray(numTotalPixels);
        mMSViewLeft.setContainerView(mContainer);

        mMSViewRight = view.findViewById(R.id.multislider_view_right);
        mMSViewRight.setValuesArray(numTotalPixels);
        mMSViewRight.setContainerView(mContainer);

        mMSButtons = mInflater.inflate(R.layout.multislider_buttons, mContainer, false);
        mLabelsView = mInflater.inflate(R.layout.multislider_labels, mContainer, false);

        // colors are determining slider positions on the left
        mMSViewLeft.setValues(vals);
        mMSViewRight.setValues(mixVals);

        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mMSViewLeft.getLayoutParams();
        final int topMargin = lp.topMargin;
        final float density = app.getScreenDensity();

        final int displayHeight = app.getDimensions().y;
        mMSViewLeft.setParentTopMargin(topMargin);
        mMSViewLeft.setDisplayHeight(displayHeight);
        mMSViewRight.setParentTopMargin(topMargin);
        mMSViewRight.setDisplayHeight(displayHeight);

        assert sliderNums != null;
        assert sliderOrder != null;

        for (int order : sliderOrder) {
            final SliderBar barLeft = new SliderBar(mActivity);
            barLeft.mScreenDensity = density;
            barLeft.setNum(String.valueOf(sliderNums.get(order)));
            final SliderBar barRight = new SliderBar(mActivity);
            barRight.mScreenDensity = density;
            barRight.setNum(String.valueOf(sliderNums.get(order)));
            switch (Objects.requireNonNull(colorChannels).get(order)) {
                case 0:
                    barLeft.setColor(red);
                    barRight.setColor(red);
                    break;
                case 1:
                    barLeft.setColor(green);
                    barRight.setColor(green);
                    break;
                case 2:
                    barLeft.setColor(blue);
                    barRight.setColor(blue);
            }
            mMSViewLeft.mBars.add(barLeft);
            mMSViewLeft.addView(barLeft);
            mMSViewRight.mBars.add(barRight);
            mMSViewRight.addView(barRight);

            setSliderProps(sliderNums, sliderOrder);

            VideOSCUIHelpers.addView(mMSButtons, mContainer);
            VideOSCUIHelpers.addView(mLabelsView, mContainer);

            mFragment = this;
        }
    }

    private void setSliderProps(ArrayList<Integer> sliderNums, ArrayList<Integer> order) {
        final ArrayList<Integer> resNumbers = new ArrayList<>(sliderNums.size());

        for (int i = 0; i < sliderNums.size(); i++) {
            resNumbers.add(order.get(i), sliderNums.get(i));
        }

        mMSViewLeft.setSliderNums(resNumbers);
        mMSViewRight.setSliderNums(resNumbers);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mApp.setSliderGroupEditMode(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCreateViewCallback != null) {
            mCreateViewCallback.onCreateView();
            mCreateViewCallback = null;
        }
    }

}
