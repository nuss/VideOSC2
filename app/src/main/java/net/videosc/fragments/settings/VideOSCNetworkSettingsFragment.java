package net.videosc.fragments.settings;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import ketai.net.KetaiNet;

public class VideOSCNetworkSettingsFragment extends VideOSCBaseFragment {
	/**
	 * @param savedInstanceState
	 * @deprecated
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @deprecated
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final FragmentManager fragmentManager = getFragmentManager();
		assert fragmentManager != null;
		final VideOSCCameraFragment cameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
		final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();

		assert activity != null;

		final VideOSCApplication app = (VideOSCApplication) activity.getApplication();
		final View view = inflater.inflate(R.layout.network_settings, container, false);
		final SQLiteDatabase db = activity.getDatabase();

		final List<VideOSCSettingsListFragment.Address> addresses = new ArrayList<>();
		final List<VideOSCSettingsListFragment.Settings> settings = new ArrayList<>();
		final ContentValues values = new ContentValues();

		final String[] settingsFields = new String[]{
				SettingsContract.SettingsEntries._ID,
				SettingsContract.SettingsEntries.UDP_RECEIVE_PORT,
				SettingsContract.SettingsEntries.ROOT_CMD
		};

		final String[] addrFields = new String[]{
				SettingsContract.AddressSettingsEntry._ID,
				SettingsContract.AddressSettingsEntry.IP_ADDRESS,
				SettingsContract.AddressSettingsEntry.PORT
		};
		final String sortOrder = SettingsContract.AddressSettingsEntry.IP_ADDRESS + " DESC";

		Cursor cursor = db.query(
				SettingsContract.AddressSettingsEntry.TABLE_NAME,
				addrFields,
				null,
				null,
				null,
				null,
				sortOrder
		);

		addresses.clear();

		while (cursor.moveToNext()) {
			final VideOSCSettingsListFragment.Address address = new VideOSCSettingsListFragment.Address();
			final long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry._ID));
			final String ip = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.IP_ADDRESS));
			final int port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.PORT));
			address.setRowId(rowId);
			address.setIP(ip);
			address.setPort(port);
			addresses.add(address);
		}

		cursor.close();

		final EditText remoteIPField = view.findViewById(R.id.remote_ip_field);
		remoteIPField.setText(addresses.get(0).getIP(), TextView.BufferType.EDITABLE);
		final EditText remotePortField = view.findViewById(R.id.remote_port_field);
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

		settings.clear();

		while (cursor.moveToNext()) {
			final VideOSCSettingsListFragment.Settings setting = new VideOSCSettingsListFragment.Settings();
			final long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries._ID));
			final int udpReceivePort = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.UDP_RECEIVE_PORT));
			final String cmd = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD));
			setting.setRowId(rowId);
			setting.setUdpReceivePort(udpReceivePort);
			setting.setRootCmd(cmd);
			settings.add(setting);
		}

		cursor.close();

		final EditText udpReceivePortField = view.findViewById(R.id.device_port_field);
		udpReceivePortField.setText(
				String.format(Locale.getDefault(), "%d", settings.get(0).getUdpReceivePort()),
				TextView.BufferType.EDITABLE
		);
		final EditText rootCmdField = view.findViewById(R.id.root_cmd_name_field);
		rootCmdField.setText(settings.get(0).getRootCmd(), TextView.BufferType.EDITABLE);
		final TextView deviceIP = view.findViewById(R.id.device_ip_address);
		deviceIP.setText(KetaiNet.getIP());

		remoteIPField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				if (!b && !remoteIPField.getText().toString().equals(addresses.get(0).getIP())) {
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
			public void onFocusChange(View view, boolean b) {
				if (!b && !remotePortField.getText().toString().equals(String.format(Locale.getDefault(), "%d", addresses.get(0).getPort()))) {
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
			public void onFocusChange(View v, boolean b) {
				if (!b && !udpReceivePortField.getText().toString().equals(
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
				}
			}
		});

		rootCmdField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean b) {
				if (!b && !rootCmdField.getText().toString().equals(settings.get(0).getRootCmd())) {
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
					assert cameraView != null;
					cameraView.setColorOscCmds(rootCmd);
				}
			}
		});

		//		return super.onCreateView(inflater, container, savedInstanceState);
		return view;
	}

	/**
	 * @deprecated
	 */
	@Override
	public void onPause() {
		super.onPause();
	}
}
