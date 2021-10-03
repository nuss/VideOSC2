package net.videosc.fragments.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;

public class VideOSCSensorSettingsFragment extends VideOSCBaseFragment {

	public VideOSCSensorSettingsFragment() { }

	public VideOSCSensorSettingsFragment(Context context) {
    	this.mActivity = (VideOSCMainActivity) context;
    	this.mDbHelper = mActivity.getDbHelper();
    }

	/**
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.sensor_settings, container, false);
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
		final SQLiteDatabase db = mDbHelper.getDatabase();
		final VideOSCSettingsListFragment.Sensors sensors = new VideOSCSettingsListFragment.Sensors();

		final Cursor cursor = db.rawQuery("SELECT * FROM " + SettingsContract.SensorSettingsEntries.TABLE_NAME, null);
		final ContentValues values = new ContentValues();

		setPlaceholder(view, db);

		while (cursor.moveToNext()) {
			switch (cursor.getString(cursor.getColumnIndex(SettingsContract.SensorSettingsEntries.SENSOR))) {
				case "ori":
					sensors.setOrientationSensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "acc":
					sensors.setAccelerationSensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "lin_acc":
					sensors.setLinAccelerationSensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "mag":
					sensors.setMagneticSensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "grav":
					sensors.setGravitySensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "prox":
					sensors.setProximitySensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "light":
					sensors.setLightSensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "press":
					sensors.setPressureSensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "temp":
					sensors.setTemperatureSensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "hum":
					sensors.setHumiditySensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				case "loc":
					sensors.setLocationSensorActivated(Short.parseShort(
							cursor.getString(
									cursor.getColumnIndexOrThrow(SettingsContract.SensorSettingsEntries.VALUE)
							)
					));
					break;
				default:
			}
		}

		cursor.close();

		final SwitchCompat oriCB = view.findViewById(R.id.orientation_sensor);
		final SwitchCompat accCB = view.findViewById(R.id.accelerometer);
		final SwitchCompat linAccCB = view.findViewById(R.id.linear_acceleration);
		final SwitchCompat magCB = view.findViewById(R.id.magnetic_field);
		final SwitchCompat gravCB = view.findViewById(R.id.gravity_sensor);
		final SwitchCompat proxCB = view.findViewById(R.id.proximity_sensor);
		final SwitchCompat lightCB = view.findViewById(R.id.light_sensor);
		final SwitchCompat pressCB = view.findViewById(R.id.air_pressure_sensor);
		final SwitchCompat tempCB = view.findViewById(R.id.temperature_sensor);
		final SwitchCompat humCB = view.findViewById(R.id.humidity_sensor);
		final SwitchCompat locCB = view.findViewById(R.id.geo_loc_sensor);

		oriCB.setChecked(sensors.getOrientationSensorActivated());
		accCB.setChecked(sensors.getAccelerationSensorActivated());
		linAccCB.setChecked(sensors.getLinAccelerationSensorActivated());
		magCB.setChecked(sensors.getMagneticSensorActivated());
		gravCB.setChecked(sensors.getGravitySensorActivated());
		proxCB.setChecked(sensors.getProximitySensorActivated());
		lightCB.setChecked(sensors.getLightSensorActivated());
		pressCB.setChecked(sensors.getPressureSensorActivated());
		tempCB.setChecked(sensors.getTemperatureSensorActivated());
		humCB.setChecked(sensors.getHumiditySensorActivated());
		locCB.setChecked(sensors.getLocationSensorActivated());

		oriCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (oriCB.isChecked() != sensors.getOrientationSensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, oriCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'ori'",
						null
				);
				values.clear();
			}
		});
		accCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (accCB.isChecked() != sensors.getAccelerationSensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, accCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'acc'",
						null
				);
				values.clear();
			}
		});
		linAccCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (linAccCB.isChecked() != sensors.getLinAccelerationSensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, linAccCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'lin_acc'",
						null
				);
				values.clear();
			}
		});
		magCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (magCB.isChecked() != sensors.getMagneticSensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, magCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'mag'",
						null
				);
				values.clear();
			}
		});
		gravCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (gravCB.isChecked() != sensors.getGravitySensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, gravCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'grav'",
						null
				);
				values.clear();
			}
		});
		proxCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (proxCB.isChecked() != sensors.getProximitySensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, proxCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'prox'",
						null
				);
				values.clear();
			}
		});
		lightCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (lightCB.isChecked() != sensors.getLightSensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, lightCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'light'",
						null
				);
				values.clear();
			}
		});
		pressCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (pressCB.isChecked() != sensors.getPressureSensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, pressCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'press'",
						null
				);
				values.clear();
			}
		});
		tempCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (tempCB.isChecked() != sensors.getTemperatureSensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, tempCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'temp'",
						null
				);
				values.clear();
			}
		});
		humCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (humCB.isChecked() != sensors.getHumiditySensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, humCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'hum'",
						null
				);
				values.clear();
			}
		});
		locCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (locCB.isChecked() != sensors.getLocationSensorActivated()) {
				values.put(SettingsContract.SensorSettingsEntries.VALUE, locCB.isChecked());
				db.update(
						SettingsContract.SensorSettingsEntries.TABLE_NAME,
						values,
						SettingsContract.SensorSettingsEntries.SENSOR + " = 'loc'",
						null
				);
				values.clear();
			}
		});

	}

	private void setPlaceholder(View container, SQLiteDatabase db) {
		Resources res = getResources();
		SparseIntArray idsAndStrings = new SparseIntArray(11);
		String rootCmd = mDbHelper.getRootCmd();

		idsAndStrings.append(R.id.orientation_sensor, R.string.orientation_sensor);
		idsAndStrings.append(R.id.accelerometer, R.string.accelerometer);
		idsAndStrings.append(R.id.linear_acceleration, R.string.linear_acceleration);
		idsAndStrings.append(R.id.magnetic_field, R.string.magnetic_field_sensor);
		idsAndStrings.append(R.id.gravity_sensor, R.string.gravity_sensor);
		idsAndStrings.append(R.id.proximity_sensor, R.string.proximity_sensor);
		idsAndStrings.append(R.id.light_sensor, R.string.light_sensor);
		idsAndStrings.append(R.id.air_pressure_sensor, R.string.air_pressure);
		idsAndStrings.append(R.id.temperature_sensor, R.string.temperature_sensor);
		idsAndStrings.append(R.id.humidity_sensor, R.string.humidity_sensor);
		idsAndStrings.append(R.id.geo_loc_sensor, R.string.geo_location_sensor);

		for (int i = 0; i < idsAndStrings.size(); i++) {
			// FIXME: keyAt seems wrong
			TextView tv = container.findViewById(idsAndStrings.keyAt(i));
			// FIXME: valueAt ...
			String text = String.format(res.getString(idsAndStrings.valueAt(i)), rootCmd);
			tv.setText(text);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.mActivity = null;
	}
}
