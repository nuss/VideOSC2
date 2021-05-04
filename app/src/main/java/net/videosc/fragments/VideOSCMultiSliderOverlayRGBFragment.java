package net.videosc.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.views.SliderBarOverlay;

import java.util.ArrayList;

public class VideOSCMultiSliderOverlayRGBFragment extends VideOSCBaseFragment {
    private final static String TAG = VideOSCMultiSliderOverlayRGBFragment.class.getSimpleName();
    private ViewGroup mMSOverlaysR, mMSOverlaysG, mMSOverlaysB;
    private ArrayList<Integer> mSliderNums;

    public VideOSCMultiSliderOverlayRGBFragment() { }

    public VideOSCMultiSliderOverlayRGBFragment(Context context) {
        this.mActivity = (VideOSCMainActivity) context;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null. This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>A default View can be returned by calling  in your
     * constructor. Otherwise, this method returns null.
     *
     * <p>It is recommended to <strong>only</strong> inflate the layout in this method and move
     * logic that operates on the returned View to {@link #onViewCreated(View, Bundle)}.
     *
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.multislider_overlay_rgb, container, false);
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

        final Bundle argsBundle = this.getArguments();
        assert argsBundle != null;
        this.mSliderNums = argsBundle.getIntegerArrayList("nums");

        this.mMSOverlaysR = view.findViewById(R.id.multislider_overlay_r_left);
        this.mMSOverlaysG = view.findViewById(R.id.multislider_overlay_g_left);
        this.mMSOverlaysB = view.findViewById(R.id.multislider_overlay_b_left);

        final int overlayWidth = mMSOverlaysR.getWidth();
        final int overlayHeight = mMSOverlaysR.getHeight();
        final int totalGaps = mSliderNums.size() - 1;
        final int sliderWidth = (overlayWidth - totalGaps) / mSliderNums.size();

        Log.d(TAG, " \noverlay width: " + overlayWidth + "\noverlay height: " + overlayHeight + "\ntotal gaps: " + totalGaps + "\nslider width: " + sliderWidth);

        for (int i = 0; i < mSliderNums.size(); i++) {
            SliderBarOverlay sliderOverlayR = new SliderBarOverlay(mActivity);
            sliderOverlayR.layout(i * sliderWidth, 0, (i + 1) * sliderWidth, overlayHeight);
            SliderBarOverlay sliderOverlayG = new SliderBarOverlay(mActivity);
            sliderOverlayG.layout(i * sliderWidth, 0, (i + 1) * sliderWidth, overlayHeight);
            SliderBarOverlay sliderOverlayB = new SliderBarOverlay(mActivity);
            sliderOverlayB.layout(i * sliderWidth, 0, (i + 1) * sliderWidth, overlayHeight);
            mMSOverlaysR.addView(sliderOverlayR);
            mMSOverlaysG.addView(sliderOverlayG);
            mMSOverlaysB.addView(sliderOverlayB);
        }
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
        super.onDetach();
    }
}
