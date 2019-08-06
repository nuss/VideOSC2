package net.videosc.fragments;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.db.SettingsContract;
import net.videosc.utilities.VideOSCUIHelpers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ketai.net.KetaiNet;

//import java.lang.reflect.Method;

/**
 * Created by stefan on 12.03.17.
 */

public class VideOSCSettingsFragment extends VideOSCBaseFragment {
	private final static String TAG = "VideOSCSettingsFragment";

	public VideOSCSettingsFragment() {
	}

/*
	public static VideOSCSettingsFragment newInstance() {
		VideOSCSettingsFragment s = new VideOSCSettingsFragment();
		Bundle args = new Bundle();
		s.setArguments(args);
		return s;
	}
*/
	// TODO: Split this all up into separate Fragments
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "settingsfragment created");
		final FragmentManager fragmentManager = getFragmentManager();
		final VideOSCCameraFragment cameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
		final Camera.Parameters params = cameraView.mCamera.getParameters();
		// the background scrollview - dark transparent, no content
		final ScrollView bg = (ScrollView) inflater.inflate(R.layout.settings_background_scroll, container, false);
		// the view holding the main selection of settings
		final View view = inflater.inflate(R.layout.settings_selection, bg, false);
		// the listview finally holding the links to different settings: network, resolution, sensors, about
		final ListView settingsListView = view.findViewById(R.id.settings_selection_list);
		// the network settings form
		final View networkSettingsView = inflater.inflate(R.layout.network_settings, bg, false);
		// the resolution settings form
		final View resolutionSettingsView;
		// the sensor settings form
		final View sensorSettingsView = inflater.inflate(R.layout.sensor_settings, bg, false);
		// debug settings
		final View debugSettingsView = inflater.inflate(R.layout.debug_settings, bg, false);
		// about
		final View aboutView = inflater.inflate(R.layout.about, bg, false);
//		final WebView webView = aboutView.findViewById(R.id.html_about);

		// get application methods and avoid reflection
		final VideOSCApplication app = (VideOSCApplication) getActivity().getApplicationContext();
		// the database
		final SQLiteDatabase db = app.getSettingsHelper().getReadableDatabase();

		// allow setting the exposure lock only if auto exposure lock is supported
		final boolean isAutoExposureLockSupported = cameraView.mCamera.getParameters().isAutoExposureLockSupported();
		if (isAutoExposureLockSupported) {
			resolutionSettingsView = inflater.inflate(R.layout.resolution_settings, bg, false);
		} else {
			resolutionSettingsView = inflater.inflate(R.layout.resolution_settings_no_autoexposure_lock, bg, false);
		}

		// get the setting items for the main selection list and parse them into the layout
		String[] items = getResources().getStringArray(R.array.settings_select_items);
		ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.settings_selection_item, items);
		settingsListView.setAdapter(itemsAdapter);
		// does the fade-in animation really work?...
		VideOSCUIHelpers.setTransitionAnimation(bg);
		// add the scroll view background to the container (camView)
		container.addView(bg);
		final View mCamView = container.findViewById(R.id.camera_preview);
		final ViewGroup fixExposureButtonLayout = (ViewGroup) inflater.inflate(R.layout.cancel_ok_buttons, (FrameLayout) mCamView, false);

		settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				String[] settingsFields;
				String[] addrFields;
				final List<Address> addresses = new ArrayList<>();
				final List<Settings> settings = new ArrayList<>();
				final ContentValues values = new ContentValues();

				app.setSettingsLevel(2);

				switch (i) {
					case 0:
						// network settings
						VideOSCUIHelpers.addView(networkSettingsView, bg);

						// table vosc_client_addresses
						addrFields = new String[]{
								SettingsContract.AddressSettingsEntry._ID,
								SettingsContract.AddressSettingsEntry.IP_ADDRESS,
								SettingsContract.AddressSettingsEntry.PORT
						};
						String sortOrder =
								SettingsContract.AddressSettingsEntry.IP_ADDRESS + " DESC";

						// receive port and root cmd are store stored within regular settings
						// table vosc_settings
						settingsFields = new String[]{
								SettingsContract.SettingsEntries._ID,
								SettingsContract.SettingsEntries.UDP_RECEIVE_PORT,
								SettingsContract.SettingsEntries.ROOT_CMD
						};

						Cursor cursor = db.query(
								SettingsContract.AddressSettingsEntry.TABLE_NAME,
								addrFields,
								null,
								null,
								null,
								null,
								sortOrder
						);

/*
						String[] columns = cursor.getColumnNames();
						for (String colName : columns) {
							Log.d(TAG, "column: " + colName);
						}
*/
						// clear list of addresses before adding new content
						addresses.clear();

						while (cursor.moveToNext()) {
							Address address = new Address();
							long rowId =
									cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry._ID));
							String ip =
									cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.IP_ADDRESS));
							int port =
									cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.PORT));
							address.setRowId(rowId);
							address.setIP(ip);
							address.setPort(port);
							addresses.add(address);
						}

						cursor.close();

						final EditText remoteIPField = networkSettingsView.findViewById(R.id.remote_ip_field);
						remoteIPField.setText(addresses.get(0).getIP(), TextView.BufferType.EDITABLE);
						final EditText remotePortField = networkSettingsView.findViewById(R.id.remote_port_field);
						remotePortField.setText(
								String.format(Locale.getDefault(), "%d", addresses.get(0).getPort()),
								TextView.BufferType.EDITABLE
						);

						cursor = db.query(
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
							Settings setting = new Settings();
							long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries._ID));
							int udpReceivePort =
									cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.UDP_RECEIVE_PORT));
							String cmd =
									cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD));
							setting.setRowId(rowId);
							setting.setUdpReceivePort(udpReceivePort);
							setting.setRootCmd(cmd);
							settings.add(setting);
						}

						cursor.close();

						final EditText udpReceivePortField = networkSettingsView.findViewById(R.id.device_port_field);
						udpReceivePortField.setText(
								String.format(Locale.getDefault(), "%d", settings.get(0).getUdpReceivePort()),
								TextView.BufferType.EDITABLE
						);
						final EditText rootCmdField = networkSettingsView.findViewById(R.id.root_cmd_name_field);
						rootCmdField.setText(settings.get(0).getRootCmd(), TextView.BufferType.EDITABLE);
						final TextView deviceIP = networkSettingsView.findViewById(R.id.device_ip_address);
						deviceIP.setText(KetaiNet.getIP());

						remoteIPField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
							@Override
							public void onFocusChange(View v, boolean hasFocus) {
								if (!hasFocus && !remoteIPField.getText().toString().equals(addresses.get(0).getIP())) {
									String remoteIP = remoteIPField.getText().toString();
									values.put(
											SettingsContract.AddressSettingsEntry.IP_ADDRESS,
											remoteIP
									);
									db.update(
											SettingsContract.AddressSettingsEntry.TABLE_NAME,
											values,
											SettingsContract.AddressSettingsEntry._ID + " = " + addresses.get(0).getRowId(),
											null
									);
									values.clear();
									// update addresses immediately, so above if clause works correctly
									// next time we try to set the IP address
									addresses.get(0).setIP(remoteIP);
									app.mOscHelper.setBroadcastAddr(remoteIP, app.mOscHelper.getBroadcastPort());
								}
							}
						});
						remotePortField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
							@Override
							public void onFocusChange(View v, boolean hasFocus) {
								if (!hasFocus && !remotePortField.getText().toString().equals(String.format(Locale.getDefault(), "%d", addresses.get(0).getPort()))) {
									String remotePort = remotePortField.getText().toString();
									values.put(
											SettingsContract.AddressSettingsEntry.PORT,
											remotePort
									);
									db.update(
											SettingsContract.AddressSettingsEntry.TABLE_NAME,
											values,
											SettingsContract.AddressSettingsEntry._ID + " = " + addresses.get(0).getRowId(),
											null
									);
									values.clear();
									addresses.get(0).setPort(Integer.parseInt(remotePort, 10));
									app.mOscHelper.setBroadcastAddr(app.mOscHelper.getBroadcastIP(), Integer.parseInt(remotePort, 10));
								}
							}
						});
						udpReceivePortField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
							@Override
							public void onFocusChange(View v, boolean hasFocus) {
								if (!hasFocus && !udpReceivePortField.getText().toString().equals(
										String.format(Locale.getDefault(), "%d", settings.get(0).getUdpReceivePort()))) {
									String receivePort = udpReceivePortField.getText().toString();
									values.put(
											SettingsContract.SettingsEntries.UDP_RECEIVE_PORT,
											receivePort
									);
									db.update(
											SettingsContract.SettingsEntries.TABLE_NAME,
											values,
											SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
											null
									);
									values.clear();
									addresses.get(0).setReceivePort(Integer.parseInt(receivePort, 10));
									// TODO: handle feedback OSC
								}
							}
						});
						rootCmdField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
							@Override
							public void onFocusChange(View v, boolean hasFocus) {
								if (!hasFocus && !rootCmdField.getText().toString().equals(settings.get(0).getRootCmd())) {
									String rootCmd = rootCmdField.getText().toString();
									values.put(
											SettingsContract.SettingsEntries.ROOT_CMD,
											rootCmd
									);
									db.update(
											SettingsContract.SettingsEntries.TABLE_NAME,
											values,
											SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
											null
									);
									values.clear();
									settings.get(0).setRootCmd(rootCmd);
									cameraView.setColorOscCmds(rootCmd);
								}
							}
						});
						break;
					case 1:
						// resolution settings
						VideOSCUIHelpers.addView(resolutionSettingsView, bg);

						// table vosc_settings
						settingsFields = new String[]{
								SettingsContract.SettingsEntries._ID,
								SettingsContract.SettingsEntries.RES_H,
								SettingsContract.SettingsEntries.RES_V,
								SettingsContract.SettingsEntries.CALC_PERIOD,
								SettingsContract.SettingsEntries.FRAMERATE_RANGE,
								SettingsContract.SettingsEntries.NORMALIZE,
								SettingsContract.SettingsEntries.REMEMBER_PIXEL_STATES
						};

						cursor = db.query(
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
							Settings setting = new Settings();
							long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries._ID));
							short resH = cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.RES_H));
							short resV = cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.RES_V));
							short calcPeriod =
									cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.CALC_PERIOD));
							short framerateRange =
									cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.FRAMERATE_RANGE));
							short normalized =
									cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.NORMALIZE));
							short rememberPixelStates =
									cursor.getShort(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.REMEMBER_PIXEL_STATES));

							setting.setRowId(rowId);
							setting.setResolutionHorizontal(resH);
							setting.setResolutionVertical(resV);
							setting.setCalculationPeriod(calcPeriod);
							setting.setFramerateRange(framerateRange);
							setting.setNormalized(normalized);
							setting.setRememberPixelStates(rememberPixelStates);
							settings.add(setting);
						}

						cursor.close();

						final EditText resHField =
								resolutionSettingsView.findViewById(R.id.resolution_horizontal_field);
						resHField.setText(
								String.format(Locale.getDefault(), "%d", settings.get(0).getResolutionHorizontal()),
								TextView.BufferType.EDITABLE
						);
						final EditText resVField =
								resolutionSettingsView.findViewById(R.id.resolution_vertical_field);
						resVField.setText(
								String.format(Locale.getDefault(), "%d", settings.get(0).getResolutionVertical()),
								TextView.BufferType.EDITABLE
						);
						final EditText calcPeriodField =
								resolutionSettingsView.findViewById(R.id.calulation_period_field);
						calcPeriodField.setText(
								String.format(Locale.getDefault(), "%d", settings.get(0).getCalculationPeriod()),
								TextView.BufferType.EDITABLE
						);

						final Spinner selectFramerate =
								resolutionSettingsView.findViewById(R.id.framerate_selection);
						List<int[]> supportedPreviewFpsRange = params.getSupportedPreviewFpsRange();
						String[] items = new String[supportedPreviewFpsRange.size()];
						for (int j = 0; j < supportedPreviewFpsRange.size(); j++) {
							int[] item = supportedPreviewFpsRange.get(j);
							items[j] = (item[0] / 1000) + " / " + (item[1] / 1000);
						}
						ArrayAdapter<String> fpsAdapter = new ArrayAdapter<>(getActivity(), R.layout.framerate_selection_item, items);
						selectFramerate.setAdapter(fpsAdapter);
						selectFramerate.setSelection(settings.get(0).getFramerateRange());

						final Switch normalizedCB =
								resolutionSettingsView.findViewById(R.id.normalize_output_checkbox);
						normalizedCB.setChecked(settings.get(0).getNormalized());
						final Switch rememberPixelStatesCB =
								resolutionSettingsView.findViewById(R.id.remember_activated_checkbox);
						rememberPixelStatesCB.setChecked(settings.get(0).getRememberPixelStates());
						if (isAutoExposureLockSupported) {
//							Log.d(TAG, "auto exposure locked? " + cameraView.mCamera.getParameters().getAutoExposureLock());
							final Switch fixExposureCB =
									resolutionSettingsView.findViewById(R.id.fix_exposure_checkbox);
							fixExposureCB.setChecked(app.getExposureIsFixed());
							fixExposureCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
								@Override
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
									// we're going beyond details level
									Log.d(TAG, "exposure is fixed? " + app.getExposureIsFixed());

									final Camera camera = cameraView.mCamera;

									if (!app.getExposureIsFixed() && !app.getHasExposureSettingBeenCancelled() && !app.getBackPressed()) {
										Log.d(TAG, "exposure is not fixed");

										resolutionSettingsView.setVisibility(View.INVISIBLE);
										bg.setVisibility(View.INVISIBLE);
										app.setSettingsLevel(3);

										Toast toast = Toast.makeText(getActivity(), R.string.exposure_toast_text, Toast.LENGTH_LONG);
										toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//										toast.setView(mCamView);
										toast.show();

										((FrameLayout) mCamView).addView(fixExposureButtonLayout);
										final ImageButton fixExposureButton = fixExposureButtonLayout.findViewById(R.id.ok);
										fixExposureButton.setOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												params.setAutoExposureLock(true);
												camera.setParameters(params);
												app.setExposureIsFixed(true);
												VideOSCUIHelpers.removeView(fixExposureButtonLayout, (FrameLayout) mCamView);
												bg.setVisibility(View.VISIBLE);
												new Toast(getActivity());
												resolutionSettingsView.setVisibility(View.VISIBLE);
												app.setSettingsLevel(2);
											}
										});
										final ImageButton cancelExposureFixed = fixExposureButtonLayout.findViewById(R.id.cancel);
										cancelExposureFixed.setOnClickListener((new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												VideOSCUIHelpers.removeView(fixExposureButtonLayout, (FrameLayout) mCamView);
												bg.setVisibility(View.VISIBLE);
												resolutionSettingsView.setVisibility(View.VISIBLE);
												app.setSettingsLevel(2);
												// setting exposure is only possible if exposure
												// isn't already fixed. As a consequence cancelling
												// setting exposure can only result in *not* fixing
												// exposure
												app.setHasExposureSettingBeenCancelled(true);
												fixExposureCB.setChecked(false);
											}
										}));
									} else {
										Log.d(TAG, "exposure is fixed");
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

						calcPeriodField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
							@Override
							public void onFocusChange(View v, boolean hasFocus) {
								if (!hasFocus && !calcPeriodField.getText().toString().equals(
										String.format(Locale.getDefault(), "%d", settings.get(0).getCalculationPeriod()))) {
									String calcPeriod = calcPeriodField.getText().toString();
									values.put(
											SettingsContract.SettingsEntries.CALC_PERIOD,
											calcPeriod
									);
									db.update(
											SettingsContract.SettingsEntries.TABLE_NAME,
											values,
											SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
											null
									);
									values.clear();
									settings.get(0).setCalculationPeriod(Short.parseShort(calcPeriod, 10));
									// FIXME: framerate doesn't always seem to get set correctly
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
									mCamView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
								}
								// initialize camera with updated framerate
								cameraView.mPreview.switchCamera(cameraView.mCamera);
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
									mCamView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
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
						break;
					case 2:
						// sensor settings
						VideOSCUIHelpers.addView(sensorSettingsView, bg);
						setPlaceholder(bg);

						final Sensors sensors = new Sensors();

						cursor = db.rawQuery("SELECT * FROM " + SettingsContract.SensorSettingsEntries.TABLE_NAME, null);

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

						final Switch oriCB = sensorSettingsView.findViewById(R.id.orientation_sensor);
						final Switch accCB = sensorSettingsView.findViewById(R.id.accelerometer);
						final Switch linAccCB = sensorSettingsView.findViewById(R.id.linear_acceleration);
						final Switch magCB = sensorSettingsView.findViewById(R.id.magnetic_field);
						final Switch gravCB = sensorSettingsView.findViewById(R.id.gravity_sensor);
						final Switch proxCB = sensorSettingsView.findViewById(R.id.proximity_sensor);
						final Switch lightCB = sensorSettingsView.findViewById(R.id.light_sensor);
						final Switch pressCB = sensorSettingsView.findViewById(R.id.air_pressure_sensor);
						final Switch tempCB = sensorSettingsView.findViewById(R.id.temperature_sensor);
						final Switch humCB = sensorSettingsView.findViewById(R.id.humidity_sensor);
						final Switch locCB = sensorSettingsView.findViewById(R.id.geo_loc_sensor);

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

						oriCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						accCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						linAccCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						magCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						gravCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						proxCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						lightCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						pressCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						tempCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						humCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});
						locCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
							}
						});

						break;
					case 3:
						VideOSCUIHelpers.addView(debugSettingsView, bg);
						final Switch hidePixelImageCB = debugSettingsView.findViewById(R.id.hide_pixel_image);
						final Switch debugPixelOscSendingCB = debugSettingsView.findViewById(R.id.add_packet_drops);
						hidePixelImageCB.setChecked(app.getPixelImageHidden());
						debugPixelOscSendingCB.setChecked(VideOSCApplication.getDebugPixelOsc());

						hidePixelImageCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								app.setPixelImageHidden(isChecked);
							}
						});

						debugPixelOscSendingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								VideOSCApplication.setDebugPixelOsc(isChecked);
							}
						});
						break;
					case 4:
						// about
//						webView.loadUrl("http://pustota.basislager.org/coding/videosc/");
						VideOSCUIHelpers.addView(aboutView, bg);
						setYear(aboutView);
						break;
					default:
				}
				settingsListView.setVisibility(View.INVISIBLE);
			}
		});

		return view;
	}

	public void setPlaceholder(View container) {
		Resources res = getResources();
		SparseIntArray idsAndStrings = new SparseIntArray(11);
		String rootCmd = "vosc";
		String[] settingsFields = new String[]{
				SettingsContract.SettingsEntries.ROOT_CMD
		};

		final SQLiteDatabase db = ((VideOSCApplication) getActivity().getApplicationContext()).getSettingsHelper().getReadableDatabase();

		Cursor cursor = db.query(
				SettingsContract.SettingsEntries.TABLE_NAME,
				settingsFields,
				null,
				null,
				null,
				null,
				null
		);

		if (cursor.moveToFirst())
			rootCmd = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD));

		cursor.close();

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

	private void setYear(View container) {
		Resources res = getResources();
		String dateString = res.getString(R.string.videosc_copyright);
		TextView tv = container.findViewById(R.id.videosc_copyright);
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);

		String formatted = String.format(dateString, String.valueOf(currentYear));
		tv.setText(formatted);
	}

	/* @Override
	public void onDetach() {
		Log.d(TAG, "settings fragment on detach");
	} */

	/* @Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "settings fragment on destroy");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(TAG, "settings fragment on destroy view");
	} */

	private class Address {
		private long mRowId;
		private String mIp;
		private int mPort;
		private int mReceivePort;
		private String mProtocol;

		Address() {
		}

		void setRowId(long id) {
			this.mRowId = id;
		}

		void setIP(String ip) {
			this.mIp = ip;
		}

		void setPort(int port) {
			this.mPort = port;
		}

		void setReceivePort(int port) {
			this.mReceivePort = port;
		}

		void setProtocol(String protocol) {
			this.mProtocol = protocol;
		}

		long getRowId() {
			return this.mRowId;
		}

		String getIP() {
			return this.mIp;
		}

		int getPort() {
			return this.mPort;
		}

		int getReceivePort() {
			return this.mReceivePort;
		}

		String getProtocol() {
			return this.mProtocol;
		}
	}

	private class Settings {
		private long mRowId;
		private short mResolutionHorizontal;
		private short mResolutionVertical;
		private short mFramerateRange;
		private boolean mNormalized;
		private boolean mRememberPixelStates;
		private short mCalculationPeriod;
		private String mRootCmd;
		private int mUdpReceivePort;
		private int mTcpReceivePort;

		Settings() {
		}

		void setRowId(long id) {
			this.mRowId = id;
		}

		void setResolutionHorizontal(short resolutionH) {
			this.mResolutionHorizontal = resolutionH;
		}

		void setResolutionVertical(short resolutionV) {
			this.mResolutionVertical = resolutionV;
		}

		void setFramerateRange(short index) {
			this.mFramerateRange = index;
		}

		void setNormalized(short boolVal) {
			this.mNormalized = boolVal > 0;
		}

		void setRememberPixelStates(short boolVal) {
			this.mRememberPixelStates = boolVal > 0;
		}

		void setCalculationPeriod(short calcPeriod) {
			this.mCalculationPeriod = calcPeriod;
		}

		void setRootCmd(String cmdName) {
			this.mRootCmd = cmdName;
		}

		void setUdpReceivePort(int port) {
			this.mUdpReceivePort = port;
		}

		void setTcpReceivePort(int port) {
			this.mTcpReceivePort = port;
		}

		long getRowId() {
			return this.mRowId;
		}

		short getResolutionHorizontal() {
			return this.mResolutionHorizontal;
		}

		short getResolutionVertical() {
			return this.mResolutionVertical;
		}

		short getFramerateRange() {
			return this.mFramerateRange;
		}

		boolean getNormalized() {
			return this.mNormalized;
		}

		boolean getRememberPixelStates() {
			return this.mRememberPixelStates;
		}

		short getCalculationPeriod() {
			return this.mCalculationPeriod;
		}

		String getRootCmd() {
			return this.mRootCmd;
		}

		int getUdpReceivePort() {
			return this.mUdpReceivePort;
		}

		int getTcpReceivePort() {
			return this.mTcpReceivePort;
		}
	}

	private class Sensors {
		private long mRowId;
		private boolean mOrientationSensorActivated;
		private boolean mAccelerationSensorActivated;
		private boolean mLinAccelerationSensorActivated;
		private boolean mMagneticSensorActivated;
		private boolean mGravitySensorActivated;
		private boolean mProximitySensorActivated;
		private boolean mLightSensorActivated;
		private boolean mPressureSensorActivated;
		private boolean mTemperatureSensorActivated;
		private boolean mHumiditySensorActivated;
		private boolean mLocationSensorActivated;

		Sensors() {
		}

		void setRowId(long rowId) {
			this.mRowId = rowId;
		}

		void setOrientationSensorActivated(short boolVal) {
			this.mOrientationSensorActivated = boolVal > 0;
		}

		void setAccelerationSensorActivated(short boolVal) {
			this.mAccelerationSensorActivated = boolVal > 0;
		}

		void setLinAccelerationSensorActivated(short boolVal) {
			this.mLinAccelerationSensorActivated = boolVal > 0;
		}

		void setMagneticSensorActivated(short boolVal) {
			this.mMagneticSensorActivated = boolVal > 0;
		}

		void setGravitySensorActivated(short boolVal) {
			this.mGravitySensorActivated = boolVal > 0;
		}

		void setProximitySensorActivated(short boolVal) {
			this.mProximitySensorActivated = boolVal > 0;
		}

		void setLightSensorActivated(short boolVal) {
			this.mLightSensorActivated = boolVal > 0;
		}

		void setPressureSensorActivated(short boolVal) {
			this.mPressureSensorActivated = boolVal > 0;
		}

		void setTemperatureSensorActivated(short boolVal) {
			this.mTemperatureSensorActivated = boolVal > 0;
		}

		void setHumiditySensorActivated(short boolVal) {
			this.mHumiditySensorActivated = boolVal > 0;
		}

		void setLocationSensorActivated(short boolVal) {
			this.mLocationSensorActivated = boolVal > 0;
		}

		long getRowId() {
			return this.mRowId;
		}

		boolean getOrientationSensorActivated() {
			return this.mOrientationSensorActivated;
		}

		boolean getAccelerationSensorActivated() {
			return this.mAccelerationSensorActivated;
		}

		boolean getLinAccelerationSensorActivated() {
			return this.mLinAccelerationSensorActivated;
		}

		boolean getMagneticSensorActivated() {
			return this.mMagneticSensorActivated;
		}

		boolean getGravitySensorActivated() {
			return this.mGravitySensorActivated;
		}

		boolean getProximitySensorActivated() {
			return this.mProximitySensorActivated;
		}

		boolean getLightSensorActivated() {
			return this.mLightSensorActivated;
		}

		boolean getPressureSensorActivated() {
			return this.mPressureSensorActivated;
		}

		boolean getTemperatureSensorActivated() {
			return this.mTemperatureSensorActivated;
		}

		boolean getHumiditySensorActivated() {
			return this.mHumiditySensorActivated;
		}

		boolean getLocationSensorActivated() {
			return this.mLocationSensorActivated;
		}
	}
}