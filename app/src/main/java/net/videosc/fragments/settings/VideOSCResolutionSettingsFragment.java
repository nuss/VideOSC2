package net.videosc.fragments.settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.utilities.VideOSCDBHelpers;
import net.videosc.utilities.VideOSCStringHelpers;
import net.videosc.utilities.VideOSCUIHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VideOSCResolutionSettingsFragment extends VideOSCBaseFragment {
	final private static String TAG = "ResolutionSettings";

	private PopupWindow mFrameRatePopUp;
	private ContentValues mValues;
	private SQLiteDatabase mDb;
	final private List<VideOSCSettingsListFragment.Settings> mSettings = new ArrayList<>();
	private VideOSCCameraFragment mCameraView;
	private Button mSelectFramerate;
	private ArrayAdapter<String> mFpsAdapter;
	private LayoutInflater mInflater;
	private boolean mIsAutoExposureLockSupported;

	public VideOSCResolutionSettingsFragment() { }

	public VideOSCResolutionSettingsFragment(Context context) {
		this.mActivity = (VideOSCMainActivity) context;
	}

	/**
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate() called");
	}

	/**
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view;
//		Log.d(TAG, "onCreateView() called");
		this.mContainer = container;
		this.mInflater = inflater;
		// the settings view - can't be final as there are two different layouts possible
		final FragmentManager fragmentManager = getFragmentManager();
		assert fragmentManager != null;
		mCameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
		assert mCameraView != null;
		this.mIsAutoExposureLockSupported = mCameraView.mCamera.getParameters().isAutoExposureLockSupported();
		if (mIsAutoExposureLockSupported) {
			view = inflater.inflate(R.layout.resolution_settings, mContainer, false);
		} else {
			view = inflater.inflate(R.layout.resolution_settings_no_autoexposure_lock, mContainer, false);
		}

		return view;
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
	public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
//		Log.d(TAG, "onViewCreated() called");
		final Camera.Parameters params = mCameraView.mCamera.getParameters();
		final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
		final VideOSCDBHelpers dbHelper = mActivity.getDbHelper();
		mDb = dbHelper.getDatabase();
		mValues = new ContentValues();

		final Cursor cursor = dbHelper.queryResolutionSettings();
		// clear list of settings before adding new content
		mSettings.clear();

		while (cursor.moveToNext()) {
			VideOSCSettingsListFragment.Settings setting = new VideOSCSettingsListFragment.Settings();
			long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries._ID));
			short resH = cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.RES_H));
			short resV = cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.RES_V));
			short framerateRange = cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.FRAMERATE_RANGE));
			short normalized = cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.NORMALIZE));
			short rememberPixelStates = cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.REMEMBER_PIXEL_STATES));

			setting.setRowId(rowId);
			setting.setResolutionHorizontal(resH);
			setting.setResolutionVertical(resV);
			setting.setFramerateRange(framerateRange);
			setting.setNormalized(normalized);
			setting.setRememberPixelStates(rememberPixelStates);
			mSettings.add(setting);
		}

		cursor.close();

		final EditText resHField = view.findViewById(R.id.resolution_horizontal_field);
		resHField.setText(
				String.format(Locale.getDefault(), "%d", mSettings.get(0).getResolutionHorizontal()),
				TextView.BufferType.EDITABLE
		);
		final EditText resVField = view.findViewById(R.id.resolution_vertical_field);
		resVField.setText(
				String.format(Locale.getDefault(), "%d", mSettings.get(0).getResolutionVertical()),
				TextView.BufferType.EDITABLE
		);

		mSelectFramerate = view.findViewById(R.id.framerate_selection);
		String buttonText = getResources().getString(R.string.select_framerate_min_max_1_s);
		final short frameRateRangeIndex = mSettings.get(0).getFramerateRange();

		List<int[]> supportedPreviewFpsRange = params.getSupportedPreviewFpsRange();
		final int[] actualFrameRateRange = supportedPreviewFpsRange.get(frameRateRangeIndex);
		buttonText = String.format(buttonText, actualFrameRateRange[0] / 1000 + " / " + actualFrameRateRange[1] / 1000);
		mSelectFramerate.setText(buttonText);

		String[] items = new String[supportedPreviewFpsRange.size()];
		for (int j = 0; j < supportedPreviewFpsRange.size(); j++) {
			int[] item = supportedPreviewFpsRange.get(j);
			items[j] = (item[0] / 1000) + " / " + (item[1] / 1000);
		}
		mFpsAdapter = new ArrayAdapter<>(mActivity, R.layout.framerate_selection_item, items);
		mFrameRatePopUp = showFrameRatesList(mFpsAdapter);
		mSelectFramerate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFrameRatePopUp.showAsDropDown(view, 0, 0);
			}
		});
		ListView frameRatesList = (ListView) mFrameRatePopUp.getContentView();
		frameRatesList.setOnItemClickListener(new FrameRateOnItemClickListener());

		final SwitchCompat normalizedCB = view.findViewById(R.id.normalize_output_checkbox);
		normalizedCB.setChecked(mSettings.get(0).getNormalized());
		final SwitchCompat rememberPixelStatesCB = view.findViewById(R.id.remember_activated_checkbox);
		rememberPixelStatesCB.setChecked(mSettings.get(0).getRememberPixelStates());
		if (mIsAutoExposureLockSupported) {
			final SwitchCompat fixExposureCB = view.findViewById(R.id.fix_exposure_checkbox);
			final View fixExposureButtonLayout = mInflater.inflate(R.layout.cancel_ok_buttons, mContainer, false);

			fixExposureCB.setChecked(app.getExposureIsFixed());
			fixExposureCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					final Camera camera = mCameraView.mCamera;

					if (!app.getExposureIsFixed() && !app.getHasExposureSettingBeenCancelled() && !app.getBackPressed()) {
						view.setVisibility(View.INVISIBLE);

						Toast toast = Toast.makeText(getActivity(), R.string.exposure_toast_text, Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
						toast.show();
						final ViewGroup bg = (ViewGroup) mContainer.getParent();
						bg.setBackgroundResource(0);

						bg.addView(fixExposureButtonLayout);

						final ImageButton fixExposureButton = fixExposureButtonLayout.findViewById(R.id.ok);
						fixExposureButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								params.setAutoExposureLock(true);
								camera.setParameters(params);
								app.setExposureIsFixed(true);
								VideOSCUIHelpers.removeView(fixExposureButtonLayout, bg);
								bg.setBackgroundResource(R.color.colorDarkTransparentBackground);
								new Toast(getActivity());
								view.setVisibility(View.VISIBLE);
							}
						});
						final ImageButton cancelExposureFixed = fixExposureButtonLayout.findViewById(R.id.cancel);
						cancelExposureFixed.setOnClickListener((new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								VideOSCUIHelpers.removeView(fixExposureButtonLayout, bg);
								view.setVisibility(View.VISIBLE);
								// setting exposure is only possible if exposure
								// isn't already fixed. As a consequence cancelling
								// setting exposure can only result in *not* fixing
								// exposure
								app.setHasExposureSettingBeenCancelled(true);
								bg.setBackgroundResource(R.color.colorDarkTransparentBackground);
								fixExposureCB.setChecked(false);
							}
						}));
					} else {
						params.setAutoExposureLock(false);
						camera.setParameters(params);
						app.setExposureIsFixed(false);
						app.setHasExposureSettingBeenCancelled(false);
					}
				}
			});
		}

		resHField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && !resHField.getText().toString().equals(
						String.format(Locale.getDefault(), "%d", mSettings.get(0).getResolutionHorizontal()))) {
					String resH = resHField.getText().toString();
					mValues.put(
							SettingsContract.SettingsEntries.RES_H,
							resH
					);
					mDb.update(
							SettingsContract.SettingsEntries.TABLE_NAME,
							mValues,
							SettingsContract.SettingsEntries._ID + " = " + mSettings.get(0).getRowId(),
							null
					);
					mValues.clear();
					mSettings.get(0).setResolutionHorizontal(Short.parseShort(resH));
					// update camera preview immediately
					app.setResolution(
							new Point(
									Integer.parseInt(resH),
									app.getResolution().y
							)
					);
					// adjust mappings to new resolution
					SparseArray<String> mappings = app.getCommandMappings();
					Log.d(TAG, "mappings before setting width: " + mappings);
					Point resolution = app.getResolution();
					for (int i = 0; i < mappings.size(); i++) {
						final String paddedMappings = VideOSCStringHelpers.padMappingsString(mappings.valueAt(i), resolution.x * resolution.y, '1');
						mValues.put(
								SettingsContract.AddressCommandsMappings.MAPPINGS,
								paddedMappings
						);
						mDb.update(
								SettingsContract.AddressCommandsMappings.TABLE_NAME,
								mValues,
								SettingsContract.AddressCommandsMappings.ADDRESS + " = " + mappings.keyAt(i),
								null
						);
						mValues.clear();
						mappings.put(mappings.keyAt(i), paddedMappings);
					}
					app.setCommandMappings(mappings);
					Log.d(TAG, "mappings after setting width: " + app.getCommandMappings());
				}
			}
		});

		resVField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && !resVField.getText().toString().equals(
						String.format(Locale.getDefault(), "%d", mSettings.get(0).getResolutionVertical()))) {
					String resV = resVField.getText().toString();
					mValues.put(
							SettingsContract.SettingsEntries.RES_V,
							resV
					);
					mDb.update(
							SettingsContract.SettingsEntries.TABLE_NAME,
							mValues,
							SettingsContract.SettingsEntries._ID + " = " + mSettings.get(0).getRowId(),
							null
					);
					mValues.clear();
					mSettings.get(0).setResolutionVertical(Short.parseShort(resV));
					// update camera preview immediately
					app.setResolution(
							new Point(
									app.getResolution().x,
									Integer.parseInt(resV)
							)
					);
					// adjust mappings to new resolution
					SparseArray<String> mappings = app.getCommandMappings();
					Point resolution = app.getResolution();
					for (int i = 0; i < mappings.size(); i++) {
						final String paddedMappings = VideOSCStringHelpers.padMappingsString(mappings.valueAt(i), resolution.x * resolution.y, '1');
						mValues.put(
								SettingsContract.AddressCommandsMappings.MAPPINGS,
								paddedMappings
						);
						mDb.update(
								SettingsContract.AddressCommandsMappings.TABLE_NAME,
								mValues,
								SettingsContract.AddressCommandsMappings.ADDRESS + " = " + mappings.keyAt(i),
								null
						);
						mValues.clear();
						mappings.put(mappings.keyAt(i), paddedMappings);
					}
					app.setCommandMappings(mappings);
				}
			}
		});

		normalizedCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (normalizedCB.isChecked() != mSettings.get(0).getNormalized()) {
					boolean isNormalized = normalizedCB.isChecked();
					mValues.put(
							SettingsContract.SettingsEntries.NORMALIZE,
							isNormalized
					);
					mDb.update(
							SettingsContract.SettingsEntries.TABLE_NAME,
							mValues,
							SettingsContract.SettingsEntries._ID + " = " + mSettings.get(0).getRowId(),
							null
					);
					mValues.clear();
					mSettings.get(0).setNormalized(isNormalized ? (short) 1 : (short) 0);
					app.setNormalized(isNormalized);
				}
			}
		});

		rememberPixelStatesCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (rememberPixelStatesCB.isChecked() != mSettings.get(0).getRememberPixelStates()) {
					boolean rememberPixelStates = rememberPixelStatesCB.isChecked();
					mValues.put(
							SettingsContract.SettingsEntries.REMEMBER_PIXEL_STATES,
							rememberPixelStatesCB.isChecked()
					);
					mDb.update(
							SettingsContract.SettingsEntries.TABLE_NAME,
							mValues,
							SettingsContract.SettingsEntries._ID + " = " + mSettings.get(0).getRowId(),
							null
					);
					mValues.clear();
					mSettings.get(0).setRememberPixelStates(rememberPixelStates ? (short) 1 : (short) 0);
					// TODO: this setting must be picked up on app init
				}
			}
		});
	}

	private PopupWindow showFrameRatesList(ArrayAdapter<String> adapter) {
		final PopupWindow popUp = new PopupWindow(mActivity);
		final ListView frameratesList = new ListView(mActivity);
		frameratesList.setAdapter(adapter);
		popUp.setFocusable(true);
		popUp.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		popUp.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popUp.setContentView(frameratesList);

		return popUp;
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause() called");
		super.onPause();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.d(TAG, "onLowMemory() called");
	}

	/**
	 * Called when the fragment is no longer attached to its activity.  This
	 * is called after {@link #onDestroy()}.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		mActivity = null;
		Log.d(TAG, "onDetach() called");
	}

	class FrameRateOnItemClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			Animation fadeInAnimation = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in);
			fadeInAnimation.setDuration(2);
			view.startAnimation(fadeInAnimation);

			mValues.put(SettingsContract.SettingsEntries.FRAMERATE_RANGE, i);
			mDb.update(
					SettingsContract.SettingsEntries.TABLE_NAME,
					mValues,
					SettingsContract.SettingsEntries._ID + " = " + mSettings.get(0).getRowId(),
					null
			);
			mValues.clear();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
			}
			// initialize camera with updated framerate
			mCameraView.mPreview.switchCamera(mCameraView.mCamera);

			String item = mFpsAdapter.getItem(i);

			String buttonText = getResources().getString(R.string.select_framerate_min_max_1_s);
			buttonText = String.format(buttonText, item);
			mSelectFramerate.setText(buttonText);

			mFrameRatePopUp.dismiss();
		}
	}
}
