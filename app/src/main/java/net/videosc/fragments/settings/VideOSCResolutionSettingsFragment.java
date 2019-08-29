package net.videosc.fragments.settings;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.utilities.VideOSCUIHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

public class VideOSCResolutionSettingsFragment extends VideOSCBaseFragment {
	final private static String TAG = "ResolutionSettings";

	/**
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate() called");
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
		Log.d(TAG, "onViewCreated() called");
	}

	/**
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		Log.d(TAG, "onCreateView() called");
		// the settings view - can't be final as there are two different layouts possible
		final View view;
		final FragmentManager fragmentManager = getFragmentManager();
		assert fragmentManager != null;
		final VideOSCCameraFragment cameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
		assert cameraView != null;
		final Camera.Parameters params = cameraView.mCamera.getParameters();
		final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();

		assert activity != null;

		final VideOSCApplication app = (VideOSCApplication) activity.getApplication();
		final SQLiteDatabase db = activity.getDatabase();
		final List<VideOSCSettingsListFragment.Settings> settings = new ArrayList<>();
		final ContentValues values = new ContentValues();

		final boolean isAutoExposureLockSupported = cameraView.mCamera.getParameters().isAutoExposureLockSupported();

		if (isAutoExposureLockSupported) {
			view = inflater.inflate(R.layout.resolution_settings, container, false);
		} else
			view = inflater.inflate(R.layout.resolution_settings_no_autoexposure_lock, container, false);


		final String[] settingsFields = new String[]{
				SettingsContract.SettingsEntries._ID,
				SettingsContract.SettingsEntries.RES_H,
				SettingsContract.SettingsEntries.RES_V,
				SettingsContract.SettingsEntries.FRAMERATE_RANGE,
				SettingsContract.SettingsEntries.NORMALIZE,
				SettingsContract.SettingsEntries.REMEMBER_PIXEL_STATES
		};

		final Cursor cursor = db.query(
				SettingsContract.SettingsEntries.TABLE_NAME,
				settingsFields,
				null,
				null,
				null,
				null,
				null
		);

		// clear list of settings before adding new content
		settings.clear();

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
			settings.add(setting);
		}

		cursor.close();

		final EditText resHField = view.findViewById(R.id.resolution_horizontal_field);
		resHField.setText(
				String.format(Locale.getDefault(), "%d", settings.get(0).getResolutionHorizontal()),
				TextView.BufferType.EDITABLE
		);
		final EditText resVField = view.findViewById(R.id.resolution_vertical_field);
		resVField.setText(
				String.format(Locale.getDefault(), "%d", settings.get(0).getResolutionVertical()),
				TextView.BufferType.EDITABLE
		);

		final Spinner selectFramerate = view.findViewById(R.id.framerate_selection);
		List<int[]> supportedPreviewFpsRange = params.getSupportedPreviewFpsRange();
		String[] items = new String[supportedPreviewFpsRange.size()];
		for (int j = 0; j < supportedPreviewFpsRange.size(); j++) {
			int[] item = supportedPreviewFpsRange.get(j);
			items[j] = (item[0] / 1000) + " / " + (item[1] / 1000);
		}
		ArrayAdapter<String> fpsAdapter = new ArrayAdapter<>(activity, R.layout.framerate_selection_item, items);
		selectFramerate.setAdapter(fpsAdapter);
		selectFramerate.setSelection(settings.get(0).getFramerateRange());

		final Switch normalizedCB = view.findViewById(R.id.normalize_output_checkbox);
		normalizedCB.setChecked(settings.get(0).getNormalized());
		final Switch rememberPixelStatesCB = view.findViewById(R.id.remember_activated_checkbox);
		rememberPixelStatesCB.setChecked(settings.get(0).getRememberPixelStates());
		if (isAutoExposureLockSupported) {
			final Switch fixExposureCB = view.findViewById(R.id.fix_exposure_checkbox);
			final View fixExposureButtonLayout = inflater.inflate(R.layout.cancel_ok_buttons, container, false);

			fixExposureCB.setChecked(app.getExposureIsFixed());
			fixExposureCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					final Camera camera = cameraView.mCamera;

					if (!app.getExposureIsFixed() && !app.getHasExposureSettingBeenCancelled() && !app.getBackPressed()) {
						view.setVisibility(View.INVISIBLE);

						Toast toast = Toast.makeText(getActivity(), R.string.exposure_toast_text, Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
						toast.show();
						final View bg = container.findViewById(R.id.settings_container);
						bg.setBackgroundResource(0);
						final ViewGroup containerParent = (ViewGroup) container.getParent();

						containerParent.addView(fixExposureButtonLayout);

						final ImageButton fixExposureButton = fixExposureButtonLayout.findViewById(R.id.ok);
						fixExposureButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								params.setAutoExposureLock(true);
								camera.setParameters(params);
								app.setExposureIsFixed(true);
								VideOSCUIHelpers.removeView(fixExposureButtonLayout, containerParent);
								bg.setBackgroundResource(R.color.colorDarkTransparentBackground);
								new Toast(getActivity());
								view.setVisibility(View.VISIBLE);
							}
						});
						final ImageButton cancelExposureFixed = fixExposureButtonLayout.findViewById(R.id.cancel);
						cancelExposureFixed.setOnClickListener((new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								VideOSCUIHelpers.removeView(fixExposureButtonLayout, containerParent);
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
						String.format(Locale.getDefault(), "%d", settings.get(0).getResolutionHorizontal()))) {
					String resH = resHField.getText().toString();
					values.put(
							SettingsContract.SettingsEntries.RES_H,
							resH
					);
					db.update(
							SettingsContract.SettingsEntries.TABLE_NAME,
							values,
							SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
							null
					);
					values.clear();
					settings.get(0).setResolutionHorizontal(Short.parseShort(resH));
					// update camera preview immediately
					app.setResolution(
							new Point(
									Integer.parseInt(resH),
									app.getResolution().y
							)
					);
				}
			}
		});

		resVField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && !resVField.getText().toString().equals(
						String.format(Locale.getDefault(), "%d", settings.get(0).getResolutionVertical()))) {
					String resV = resVField.getText().toString();
					values.put(
							SettingsContract.SettingsEntries.RES_V,
							resV
					);
					db.update(
							SettingsContract.SettingsEntries.TABLE_NAME,
							values,
							SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
							null
					);
					values.clear();
					settings.get(0).setResolutionVertical(Short.parseShort(resV));
					// update camera preview immediately
					app.setResolution(
							new Point(
									app.getResolution().x,
									Integer.parseInt(resV)
							)
					);
				}
			}
		});

		selectFramerate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				values.put(
						SettingsContract.SettingsEntries.FRAMERATE_RANGE,
						position
				);
				db.update(
						SettingsContract.SettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
						null
				);
				values.clear();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
				}
				// initialize camera with updated framerate
				cameraView.mPreview.switchCamera(cameraView.mCamera);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
				}
			}
		});

		normalizedCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (normalizedCB.isChecked() != settings.get(0).getNormalized()) {
					boolean isNormalized = normalizedCB.isChecked();
					values.put(
							SettingsContract.SettingsEntries.NORMALIZE,
							isNormalized
					);
					db.update(
							SettingsContract.SettingsEntries.TABLE_NAME,
							values,
							SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
							null
					);
					values.clear();
					settings.get(0).setNormalized(isNormalized ? (short) 1 : (short) 0);
					app.setNormalized(isNormalized);
				}
			}
		});

		rememberPixelStatesCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (rememberPixelStatesCB.isChecked() != settings.get(0).getRememberPixelStates()) {
					boolean rememberPixelStates = rememberPixelStatesCB.isChecked();
					values.put(
							SettingsContract.SettingsEntries.REMEMBER_PIXEL_STATES,
							rememberPixelStatesCB.isChecked()
					);
					db.update(
							SettingsContract.SettingsEntries.TABLE_NAME,
							values,
							SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
							null
					);
					values.clear();
					settings.get(0).setRememberPixelStates(rememberPixelStates ? (short) 1 : (short) 0);
					// TODO: this setting must be picked up on app init
				}
			}
		});

		return view;
	}

	/**
	 * Called when the fragment's activity has been created and this
	 * fragment's view hierarchy instantiated.  It can be used to do final
	 * initialization once these pieces are in place, such as retrieving
	 * views or restoring state.  It is also useful for fragments that use
	 * {@link #setRetainInstance(boolean)} to retain their instance,
	 * as this callback tells the fragment when it is fully associated with
	 * the new activity instance.  This is called after {@link #onCreateView}
	 * and before {@link #onViewStateRestored(Bundle)}.
	 *
	 * @param savedInstanceState If the fragment is being re-created from
	 *                           a previous saved state, this is the state.
	 */
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated() called");
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
