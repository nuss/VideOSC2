package net.videosc.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.adapters.SparseStringsAdapter;
import net.videosc.utilities.VideOSCOscHandler;
import net.videosc.utilities.enums.RGBModes;

import java.util.ArrayList;

public class VideOSCSliderGroupSelectFragment extends VideOSCBaseFragment {
    final private static String TAG = VideOSCSliderGroupSelectFragment.class.getSimpleName();
    private LinearLayout mBlueSelectorsView;
    private LinearLayout mRedSelectorsView;
    private LinearLayout mGreenSelectorsView;

    public VideOSCSliderGroupSelectFragment() { }

    public VideOSCSliderGroupSelectFragment(Context context) {
        this.mActivity = (VideOSCMainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mContainer = container;
        this.mInflater = inflater;
        return inflater.inflate(R.layout.group_sliders_editor_rgb, container, false);
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

        final SparseArray<String> redPixelItems = new SparseArray<>();
        final SparseArray<String> greenPixelItems = new SparseArray<>();
        final SparseArray<String> bluePixelItems = new SparseArray<>();

        Log.d(TAG, " \npixel Ids: " + pixelIds + "\nredFbStrings: " + redFbStrings + "\ngreenFbStrings: " + greenFbStrings + "\nblueFbStrings: " + blueFbStrings);

        this.mRedSelectorsView = view.findViewById(R.id.red_channel);
        this.mGreenSelectorsView = view.findViewById(R.id.green_channel);
        this.mBlueSelectorsView = view.findViewById(R.id.blue_channel);

        final ListView redPixelsList = view.findViewById(R.id.red_pixels_list);
        final ListView greenPixelsList = view.findViewById(R.id.green_pixels_list);
        final ListView bluePixelsList = view.findViewById(R.id.blue_pixels_list);

        assert pixelIds != null;
        for (int index : pixelIds) {
            checkAndFillItemsArray(index, redPixelItems, redFbStrings);
            checkAndFillItemsArray(index, greenPixelItems, greenFbStrings);
            checkAndFillItemsArray(index, bluePixelItems, blueFbStrings);
        }

        final SparseStringsAdapter redAdapter = new SparseStringsAdapter(mActivity, redPixelItems, RGBModes.R);
        final SparseStringsAdapter greenAdapter = new SparseStringsAdapter(mActivity, greenPixelItems, RGBModes.G);
        final SparseStringsAdapter blueAdapter = new SparseStringsAdapter(mActivity, bluePixelItems, RGBModes.B);

        redPixelsList.setAdapter(redAdapter);
        greenPixelsList.setAdapter(greenAdapter);
        bluePixelsList.setAdapter(blueAdapter);

        redPixelsList.setOnItemClickListener((parent, view1, position, id) -> {
            Log.d(TAG, "selected\nparent: " + parent + "\nview: " + view1 + "\nposition: " + position + "\nid: " + id);
        });

        redPixelsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "selected\nparent: " + parent + "\nview: " + view + "\nposition: " + position + "\nid: " + id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "nothing selected\nparent: " + parent);
            }
        });

        greenPixelsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bluePixelsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void checkAndFillItemsArray(int index, SparseArray<String> itemsArray, @NonNull ArrayList<SparseArray<String>> feedbackStrings) {
        SparseArray<String> it = feedbackStrings.get(index - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < it.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(it.valueAt(i));
        }
        if (it.size() > 0) {
            itemsArray.append(index, String.valueOf(sb));
        } else {
            itemsArray.append(index, null);
        }
    }

//    private void setSliderProps(ArrayList<Integer> sliderNums) {
//        mRedSelectorsView.setSliderNums(sliderNums);
//        mGreenSelectorsView.setSliderNums(sliderNums);
//        mBlueSelectorsView.setSliderNums(sliderNums);
//    }
}
