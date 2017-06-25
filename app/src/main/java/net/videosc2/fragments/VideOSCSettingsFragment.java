package net.videosc2.fragments;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.db.SettingsContract;
import net.videosc2.utilities.VideOSCUIHelpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefan on 12.03.17.
 */

public class VideOSCSettingsFragment extends VideOSCBaseFragment {
	private final static String TAG = "VideOSCSettingsFragment";
	//	private ArrayAdapter<String> itemsAdapter;
	private Method setSettingsLevel;

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

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
	                         Bundle savedInstanceState) {
		// the background scrollview - dark transparent, no content
		final ScrollView bg = (ScrollView) inflater.inflate(R.layout.settings_background_scroll, container, false);
		// the view holding the main selection of settings
		View view = inflater.inflate(R.layout.settings_selection, bg, false);
		// the listview finally holding the links to different settings: network, resolution, sensors, about
		final ListView settingsListView = (ListView) view.findViewById(R.id.settings_selection_list);
		// the network settings form
		final View networkSettingsView = inflater.inflate(R.layout.network_settings, bg, false);
		// the resolution settings form
		final View resolutionSettingsView = inflater.inflate(R.layout.resolution_settings, bg, false);
		// the sensor settings form
		final View sensorSettingsView = inflater.inflate(R.layout.sensor_settings, bg, false);
		// about
		final View aboutView = inflater.inflate(R.layout.about, bg, false);
		final WebView webView = (WebView) aboutView.findViewById(R.id.html_about);
		final SQLiteDatabase db = VideOSCMainActivity.mDbHelper.getReadableDatabase();

		try {
			Class[] lArg = new Class[1];
			lArg[0] = int.class;
			setSettingsLevel = getActivity().getClass().getMethod("setSettingsLevel", lArg);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}


		// get the setting items for the main selection list and parse them into the layout
		String[] items = getResources().getStringArray(R.array.settings_select_items);
		ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.settings_selection_item, items);
		settingsListView.setAdapter(itemsAdapter);
		// does the fade-in animation really work?...
		VideOSCUIHelpers.setTransitionAnimation(bg);
		// add the scroll view background to the container (camView)
		container.addView(bg);

		settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				try {
					setSettingsLevel.invoke(getActivity(), 2);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}

				switch (i) {
					case 0:
						// network settings
						String[] addrFields = {
								SettingsContract.AddressSettingsEntry._ID,
								SettingsContract.AddressSettingsEntry.IP_ADDRESS,
								SettingsContract.AddressSettingsEntry.PORT
						};
						String sortOrder =
								SettingsContract.AddressSettingsEntry.IP_ADDRESS + " DESC";

						Cursor count = db.rawQuery("select count(*) from " + SettingsContract.AddressSettingsEntry.TABLE_NAME, null);
						count.moveToFirst();
						Log.d(TAG, "numrows: " + count.getInt(0));
						count.close();

						Cursor cursor = db.query(
								SettingsContract.AddressSettingsEntry.TABLE_NAME,
								addrFields,
								null,
								null,
								null,
								null,
								sortOrder
						);

						List<Address> addresses = new ArrayList<>();

						while (cursor.moveToNext()) {
							Address address = new Address();
							String ip = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.IP_ADDRESS));
							int port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.PORT));
							String protocol = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.PROTOCOL));
							Log.d(TAG, "ip: " + ip + ", port: " + port + ", protocol: " + protocol);
							address.setIP(ip);
							address.setPort(port);
							address.setProtocol(protocol);
							addresses.add(address);
						}

						cursor.close();

						Log.d(TAG, "addresses: " + addresses);
						VideOSCUIHelpers.addView(networkSettingsView, bg);
						EditText remoteIPField = (EditText) networkSettingsView.findViewById(R.id.remote_ip_field);
						remoteIPField.setText(addresses.get(0).getIP(), TextView.BufferType.EDITABLE);
						EditText remotePortField = (EditText) networkSettingsView.findViewById(R.id.remote_port_field);
						remotePortField.setText(addresses.get(0).getPort(), TextView.BufferType.EDITABLE);
						break;
					case 1:
						// resolution settings
						VideOSCUIHelpers.addView(resolutionSettingsView, bg);
						break;
					case 2:
						// sensor settings
						VideOSCUIHelpers.addView(sensorSettingsView, bg);
						setPlaceholder(bg);
						break;
					case 3:
						// about
						webView.loadUrl("http://pustota.basislager.org/coding/videosc/");
						VideOSCUIHelpers.addView(aboutView, bg);
					default:
				}
				settingsListView.setVisibility(View.INVISIBLE);
			}
		});

		return view;
	}

	// preliminary - replacement for placeholders should come from stored settings
	public void setPlaceholder(View container) {
		Resources res = getResources();
		SparseIntArray idsAndStrings = new SparseIntArray(11);

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
			TextView tv = (TextView) container.findViewById(idsAndStrings.keyAt(i));
			String text = String.format(res.getString(idsAndStrings.valueAt(i)), "vosc");
			tv.setText(text);
		}
	}

	private class Address {
		String ip;
		int port;
		String protocol;

		Address() {};

		void setIP(String ip) {
			this.ip = ip;
		}

		void setPort(int port) {
			this.port = port;
		}

		void setProtocol(String protocol) {
			this.protocol = protocol;
		}

		String getIP() {
			return this.ip;
		}

		int getPort() {
			return this.port;
		}

		String getProtocol() {
			return this.protocol;
		}
	}
}
