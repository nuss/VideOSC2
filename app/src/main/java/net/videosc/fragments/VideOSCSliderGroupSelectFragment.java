package net.videosc.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.adapters.SparseStringsAdapter;
import net.videosc.utilities.VideOSCDBHelpers;
import net.videosc.utilities.VideOSCOscHandler;
import net.videosc.utilities.VideOSCUIHelpers;
import net.videosc.utilities.enums.RGBModes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideOSCSliderGroupSelectFragment extends VideOSCBaseFragment {
    final private static String TAG = VideOSCSliderGroupSelectFragment.class.getSimpleName();
    private ViewGroup mParentContainer;

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
        final VideOSCDBHelpers dbHelper = mActivity.getDbHelper();
        final FragmentManager manager = getFragmentManager();
        final Bundle argsBundle = this.getArguments();
        assert argsBundle != null;
        final ArrayList<Integer> pixelIds = argsBundle.getIntegerArrayList("pixelIds");

        final VideOSCOscHandler oscHelper = mApp.getOscHelper();
        final ArrayList<SparseArray<String>> redFbStrings = oscHelper.getRedFeedbackStringsSnapshot();
        final ArrayList<SparseArray<String>> greenFbStrings = oscHelper.getGreenFeedbackStringsSnapshot();
        final ArrayList<SparseArray<String>> blueFbStrings = oscHelper.getBlueFeedbackStringsSnapshot();

        final SparseArray<String> redPixelItems = new SparseArray<>();
        final SparseArray<String> greenPixelItems = new SparseArray<>();
        final SparseArray<String> bluePixelItems = new SparseArray<>();

        Log.d(TAG, " \npixel Ids: " + pixelIds + "\nredFbStrings: " + redFbStrings + "\ngreenFbStrings: " + greenFbStrings + "\nblueFbStrings: " + blueFbStrings);

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

        final ViewGroup fpsCalcPanel = mParentContainer.findViewById(R.id.fps_calc_period_indicator);
        final ViewGroup indicatorPanel = mParentContainer.findViewById(R.id.indicator_panel);
        final ViewGroup pixelEditorToolbox = mParentContainer.findViewById(R.id.pixel_editor_toolbox);
        final ViewGroup snapshotsBar = mParentContainer.findViewById(R.id.snapshots_bar);

        final List<SparseArray<String>> group = Arrays.asList(new SparseArray<>(), new SparseArray<>(), new SparseArray<>());

        redPixelsList.setOnItemClickListener((parent, view1, position, id) -> {
            final TextView tf = view1.findViewById(R.id.pixel_text);
            final SparseArray<String> redSlot = group.get(0);
            if (view1.isActivated()) {
                tf.setBackgroundColor(Color.BLACK);
                redSlot.put((int) id, tf.getText().toString());
            } else {
                tf.setBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
                redSlot.delete((int) id);
            }
        });

        greenPixelsList.setOnItemClickListener((parent, view1, position, id) -> {
            final TextView tf = view1.findViewById(R.id.pixel_text);
            final SparseArray<String> greenSlot = group.get(1);
            if (view1.isActivated()) {
                tf.setBackgroundColor(Color.BLACK);
                greenSlot.put((int) id, tf.getText().toString());
            } else {
                tf.setBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
                greenSlot.delete((int) id);
            }
        });

        bluePixelsList.setOnItemClickListener((parent, view1, position, id) -> {
            Log.d(TAG, "clicked\nparent: " + parent + "\nview1: " + view1 + "\nposition: " + position + "\nid: " + id);
            final TextView tf = view1.findViewById(R.id.pixel_text);
            final SparseArray<String> blueSlot = group.get(2);
            if (view1.isActivated()) {
                tf.setBackgroundColor(Color.BLACK);
                blueSlot.put((int) id, tf.getText().toString());
            } else {
                tf.setBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
                blueSlot.delete((int) id);
            }
        });

        final ViewGroup buttons = (ViewGroup) mInflater.inflate(R.layout.multislider_buttons, mContainer, false);
        VideOSCUIHelpers.addView(buttons, mContainer);
        buttons.setOnTouchListener(mActivity);
        buttons.bringToFront();

        final ImageButton ok = buttons.findViewById(R.id.ok);
        final ImageButton cancel = buttons.findViewById(R.id.cancel);
        final DrawerLayout toolsDrawer = mActivity.mToolsDrawerLayout;

        ok.setOnClickListener(v -> {
            assert manager != null;
            manager.beginTransaction().remove(this).commit();
            mContainer.removeView(buttons);
            indicatorPanel.setVisibility(View.VISIBLE);
            pixelEditorToolbox.setVisibility(View.VISIBLE);
            snapshotsBar.setVisibility(View.VISIBLE);
            if (mApp.getIsFPSCalcPanelOpen())
                fpsCalcPanel.setVisibility(View.VISIBLE);
            toolsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            dbHelper.addSliderGroup(group);
        });

        cancel.setOnClickListener(v -> {
            assert manager != null;
            manager.beginTransaction().remove(this).commit();
            mContainer.removeView(buttons);
            indicatorPanel.setVisibility(View.VISIBLE);
            pixelEditorToolbox.setVisibility(View.VISIBLE);
            snapshotsBar.setVisibility(View.VISIBLE);
            if (mApp.getIsFPSCalcPanelOpen())
                fpsCalcPanel.setVisibility(View.VISIBLE);
            toolsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        });
    }

    private void checkAndFillItemsArray(int index, SparseArray<String> itemsArray, @NonNull ArrayList<SparseArray<String>> feedbackStrings) {
        if (feedbackStrings.size() > index - 1) {
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
        } else {
            itemsArray.append(index, null);
        }
    }

    void setParentContainer(ViewGroup container) {
        this.mParentContainer = container;
    }

//    private void setSliderProps(ArrayList<Integer> sliderNums) {
//        mRedSelectorsView.setSliderNums(sliderNums);
//        mGreenSelectorsView.setSliderNums(sliderNums);
//        mBlueSelectorsView.setSliderNums(sliderNums);
//    }
}
