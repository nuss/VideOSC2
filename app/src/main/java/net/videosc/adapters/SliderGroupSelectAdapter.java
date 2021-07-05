package net.videosc.adapters;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.fragments.VideOSCMultiSliderGroupFragment;
import net.videosc.fragments.VideOSCSelectSliderGroupFragment;
import net.videosc.utilities.VideOSCDBHelpers;
import net.videosc.utilities.VideOSCUIHelpers;

import java.util.ArrayList;

public class SliderGroupSelectAdapter extends ResourceCursorAdapter {
    final private static String TAG = SliderGroupSelectAdapter.class.getSimpleName();

    private final VideOSCMainActivity mActivity;
    private final VideOSCApplication mApp;
    private final int mLayout;
    private FragmentManager mManager;
    private VideOSCCameraFragment mCameraFragment;
    private VideOSCSelectSliderGroupFragment mGroupsListFragment;
    private ViewGroup mParent;

    public SliderGroupSelectAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
        this.mActivity = (VideOSCMainActivity) context;
        this.mApp = (VideOSCApplication) mActivity.getApplication();
        this.mLayout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        this.mManager = mActivity.getSupportFragmentManager();
        this.mCameraFragment = (VideOSCCameraFragment) mManager.findFragmentByTag("CamPreview");
        this.mGroupsListFragment = (VideOSCSelectSliderGroupFragment) mManager.findFragmentByTag("group select");
        this.mParent = parent;
        return LayoutInflater.from(context).inflate(mLayout, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final TextView row = view.findViewById(R.id.slider_group_item);
        final ViewGroup container = mCameraFragment.getContainer();
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.SliderGroups._ID));
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SliderGroups.GROUP_NAME));
        row.setText(name);
        final VideOSCDBHelpers dbHelper = mActivity.getDbHelper();

        row.setOnClickListener(v -> {
            final Cursor groupCursor = dbHelper.getSliderGroupPropertiesCursor(id);
            final Bundle msArgsBundle = new Bundle();
            final ArrayList<Integer> pixelIds = new ArrayList<>();
            final ArrayList<String> sliderLabels = new ArrayList<>();
            final ArrayList<Integer> colorChannels = new ArrayList<>();
            final ArrayList<Integer> sliderOrder = new ArrayList<>();
            final ViewGroup indicators = container.findViewById(R.id.indicator_panel);
            final ViewGroup fpsRateCalcPanel = container.findViewById(R.id.fps_calc_period_indicator);
            final ViewGroup modePanel = container.findViewById(R.id.color_mode_panel);
            final DrawerLayout toolsDrawer = mActivity.mToolsDrawerLayout;

            while (groupCursor.moveToNext()) {
                pixelIds.add(groupCursor.getInt(groupCursor.getColumnIndexOrThrow(SettingsContract.SliderGroupProperties.PIXEL_ID)));
                sliderLabels.add(groupCursor.getString(groupCursor.getColumnIndexOrThrow(SettingsContract.SliderGroupProperties.LABEL_TEXT)));
                colorChannels.add(groupCursor.getInt(groupCursor.getColumnIndexOrThrow(SettingsContract.SliderGroupProperties.COLOR_CHANNEL)));
                sliderOrder.add(groupCursor.getInt(groupCursor.getColumnIndexOrThrow(SettingsContract.SliderGroupProperties.SLIDER_ORDER)));
            }

            groupCursor.close();

            final double[] values = new double[pixelIds.size()];
            final double[] mixValues = new double[pixelIds.size()];

            for (int i = 0; i < pixelIds.size(); i++) {
                switch (colorChannels.get(i)) {
                    case 0:
                        values[i] = mCameraFragment.mPreview.getColorChannelValueAt(0, id - 1);
                        mixValues[i] = mCameraFragment.mPreview.getColorChannelMixValueAt(0, id - 1);
                        break;
                    case 1:
                        values[i] = mCameraFragment.mPreview.getColorChannelValueAt(1, id - 1);
                        mixValues[i] = mCameraFragment.mPreview.getColorChannelMixValueAt(1, id - 1);
                        break;
                    case 2:
                        values[i] = mCameraFragment.mPreview.getColorChannelValueAt(2, id - 1);
                        mixValues[i] = mCameraFragment.mPreview.getColorChannelMixValueAt(2, id - 1);
                }
            }

            msArgsBundle.putDoubleArray("values", values);
            msArgsBundle.putDoubleArray("mixValues", mixValues);
            msArgsBundle.putIntegerArrayList("pixelIds", pixelIds);
            msArgsBundle.putIntegerArrayList("colorChannels", colorChannels);
            msArgsBundle.putStringArrayList("labels", sliderLabels);
            msArgsBundle.putIntegerArrayList("sliderOrder", sliderOrder);

//            Log.d(TAG, " \nvalues: " + Arrays.toString(values) + "\nmixValues: " + Arrays.toString(mixValues));

            if (mManager.findFragmentByTag("MultiSliderView") == null) {
                final VideOSCMultiSliderGroupFragment multiSliderFragment = new VideOSCMultiSliderGroupFragment(mActivity);
                mManager.beginTransaction()
                        .add(R.id.camera_preview, multiSliderFragment, "MultiSliderView")
                        .remove(mGroupsListFragment)
                        .commit();
                multiSliderFragment.setArguments(msArgsBundle);
                multiSliderFragment.setParentContainer(container);
                mApp.setIsMultiSliderActive(true);
                indicators.setVisibility(View.INVISIBLE);
                if (fpsRateCalcPanel != null)
                    fpsRateCalcPanel.setVisibility(View.INVISIBLE);
                mApp.setIsColorModePanelOpen(VideOSCUIHelpers.removeView(modePanel, container));
                toolsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//                if (multiSliderFragment.getView() == null) {
//                    multiSliderFragment.setCreateViewCallback(mPixelIds::clear);
//                }
            }
        });

        row.setOnLongClickListener(v -> {
            final LayoutInflater inflater = LayoutInflater.from(context);
            final ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.slider_group_dialogs, mParent, false);
            final ImageButton savedSliderGroupsButton = container.findViewById(R.id.saved_slider_groups_button);

            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setView(dialogView);
            final EditText nameInput = dialogView.findViewById(R.id.save_slider_group_name);
            nameInput.setText(name);

            final String[] fields = new String[]{
                    SettingsContract.SliderGroups._ID,
                    SettingsContract.SliderGroups.GROUP_NAME
            };

            final SQLiteDatabase db = mGroupsListFragment.getDatabase();

            dialogBuilder
                    .setCancelable(true)
                    .setPositiveButton(R.string.rename_slider_group,
                            (dialog, which) -> {
                                final ContentValues values = new ContentValues();
                                values.put(SettingsContract.SliderGroups.GROUP_NAME, nameInput.getText().toString());
                                final int result = db.update(
                                        SettingsContract.SliderGroups.TABLE_NAME,
                                        values,
                                        SettingsContract.SliderGroups._ID + " = " + id,
                                        null
                                );
                                if (result > 0) {
                                    final Cursor newCursor = db.query(
                                            SettingsContract.SliderGroups.TABLE_NAME,
                                            fields,
                                            null,
                                            null,
                                            null,
                                            null,
                                            SettingsContract.SliderGroups._ID + " DESC"
                                    );
                                    changeCursor(newCursor);
                                    notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(R.string.delete_slider_group,
                            (dialog, which) -> {
                                int result = db.delete(
                                        SettingsContract.SliderGroupProperties.TABLE_NAME,
                                        SettingsContract.SliderGroupProperties.GROUP_ID + " = " + id,
                                        null
                                );
                                if (result > 0) {
                                    result = db.delete(
                                            SettingsContract.SliderGroups.TABLE_NAME,
                                            SettingsContract.SliderGroups._ID + " = " + id,
                                            null
                                    );
                                    if (result > 0) {
                                        final Cursor newCursor = db.query(
                                                SettingsContract.SliderGroups.TABLE_NAME,
                                                fields,
                                                null,
                                                null,
                                                null,
                                                null,
                                                SettingsContract.SliderGroups._ID + " DESC"
                                        );
                                        changeCursor(newCursor);
                                        notifyDataSetChanged();
                                        dialog.dismiss();

                                        final long numGroups = DatabaseUtils.queryNumEntries(db, SettingsContract.SliderGroups.TABLE_NAME);
                                        final TextView numGroupsIndicator = mActivity.mCamView.findViewById(R.id.num_slider_groups);
                                        if (numGroupsIndicator != null) {
                                            numGroupsIndicator.setText(String.valueOf(numGroups));
                                            if (numGroups > 0) {
                                                numGroupsIndicator.setActivated(true);
                                                numGroupsIndicator.setTextColor(0xffffffff);
                                            } else {
                                                numGroupsIndicator.setActivated(false);
                                                numGroupsIndicator.setTextColor(0x00ffffff);
                                                mManager.beginTransaction()
                                                        .remove(mGroupsListFragment)
                                                        .commit();
                                                savedSliderGroupsButton.setEnabled(false);
                                                savedSliderGroupsButton.setAlpha(0.3f);
                                            }
                                        }
                                    }
                                }
                            });

            final AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            return true;
        });
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }
}
