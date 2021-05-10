package net.videosc.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.utilities.VideOSCOscHandler;
import net.videosc.views.SliderSelectorView;
import net.videosc.views.VideOSCSliderGroupView;

import java.util.ArrayList;

public class VideOSCSliderGroupSelectFragment extends VideOSCBaseFragment {
    final private static String TAG = VideOSCSliderGroupSelectFragment.class.getSimpleName();
    private VideOSCSliderGroupView mBlueSelectorsView;
    private VideOSCSliderGroupView mRedSelectorsView;
    private VideOSCSliderGroupView mGreenSelectorsView;

    public VideOSCSliderGroupSelectFragment() { }

    public VideOSCSliderGroupSelectFragment(Context context) {
        this.mActivity = (VideOSCMainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mContainer = container;
        this.mInflater = inflater;
        return inflater.inflate(R.layout.group_sliders_overlay_rgb, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mApp = (VideOSCApplication) mActivity.getApplication();
        final Bundle argsBundle = this.getArguments();
        assert argsBundle != null;
        final ArrayList<Integer> pixelIds = argsBundle.getIntegerArrayList("pixelIds");
        final int numPixels = argsBundle.getInt("numPixels");

        final VideOSCOscHandler oscHelper = mApp.getOscHelper();
        final ArrayList<SparseArray<String>> redFbStrings = oscHelper.getRedFeedbackStringsSnapshot();
        final ArrayList<SparseArray<String>> greenFbStrings = oscHelper.getGreenFeedbackStringsSnapshot();
        final ArrayList<SparseArray<String>> blueFbStrings = oscHelper.getBlueFeedbackStringsSnapshot();

        this.mRedSelectorsView = view.findViewById(R.id.red_channel);
        this.mGreenSelectorsView = view.findViewById(R.id.green_channel);
        this.mBlueSelectorsView = view.findViewById(R.id.blue_channel);

        assert pixelIds != null;
        for (int id : pixelIds) {
            SliderSelectorView sliderSelectorRed = new SliderSelectorView(mActivity);
            sliderSelectorRed.setNum(id);
            if (redFbStrings.size() == numPixels && redFbStrings.get(id - 1) != null) {
                sliderSelectorRed.setStrings(redFbStrings.get(id - 1));
            }
            mRedSelectorsView.addView(sliderSelectorRed);

            SliderSelectorView sliderSelectorGreen = new SliderSelectorView(mActivity);
            sliderSelectorGreen.setNum(id);
            if (greenFbStrings.size() == numPixels && greenFbStrings.get(id - 1) != null) {
                sliderSelectorGreen.setStrings(greenFbStrings.get(id - 1));
            }
            mGreenSelectorsView.addView(sliderSelectorGreen);

            SliderSelectorView sliderSelectorBlue = new SliderSelectorView(mActivity);
            sliderSelectorBlue.setNum(id);
            if (blueFbStrings.size() == numPixels && blueFbStrings.get(id - 1) != null) {
                sliderSelectorBlue.setStrings(blueFbStrings.get(id - 1));
            }
            mBlueSelectorsView.addView(sliderSelectorBlue);
        }
        setSliderProps(pixelIds);
    }

    private void setSliderProps(ArrayList<Integer> sliderNums) {
        mRedSelectorsView.setSliderNums(sliderNums);
        mGreenSelectorsView.setSliderNums(sliderNums);
        mBlueSelectorsView.setSliderNums(sliderNums);
    }
}
