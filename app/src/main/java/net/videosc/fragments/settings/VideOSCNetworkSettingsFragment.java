package net.videosc.fragments.settings;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
	private View mView;
	private VideOSCMainActivity mActivity;
	/**
	 * @param savedInstanceState
	 * @deprecated
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	/**
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @deprecated
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final FragmentManager fragmentManager = getChildFragmentManager();
		final VideOSCCameraFragment cameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
		mActivity = (VideOSCMainActivity) getActivity();

		assert mActivity != null;

		final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
		mView = inflater.inflate(R.layout.network_settings, container, false);
		final SQLiteDatabase db = mActivity.getDatabase();

		final Button addAddress = mView.findViewById(R.id.add_address_button);
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

		final EditText udpReceivePortField = mView.findViewById(R.id.device_port_field);
		udpReceivePortField.setText(
				String.format(Locale.getDefault(), "%d", settings.get(0).getUdpReceivePort()),
				TextView.BufferType.EDITABLE
		);
		final EditText rootCmdField = mView.findViewById(R.id.root_cmd_name_field);
		rootCmdField.setText(settings.get(0).getRootCmd(), TextView.BufferType.EDITABLE);
		final TextView deviceIP = mView.findViewById(R.id.device_ip_address);
		deviceIP.setText(KetaiNet.getIP());

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
		return mView;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mActivity = null;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mView = null;
	}

	/**
	 * @deprecated
	 */
	@Override
	public void onPause() {
		super.onPause();
	}
}
