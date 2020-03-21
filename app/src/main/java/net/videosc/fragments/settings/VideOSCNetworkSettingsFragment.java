package net.videosc.fragments.settings;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.utilities.VideOSCDialogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ketai.net.KetaiNet;

public class VideOSCNetworkSettingsFragment extends VideOSCBaseFragment {
	final private static String TAG = "NetworkSettingsFragment";
	private View mView;
	private VideOSCMainActivity mActivity;
	private Cursor mAddressesCursor;
	private ArrayList<VideOSCSettingsListFragment.Address> mAddresses;

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

		final EditText addIPAddress = mView.findViewById(R.id.add_remote_ip);
		final EditText addPort = mView.findViewById(R.id.add_remote_port);
		final Button addProtocol = mView.findViewById(R.id.set_protocol);

		final Button addAddress = mView.findViewById(R.id.add_address_button);
//		final ListView addressesList = mView.findViewById(R.id.addresses_list);

		mAddresses = new ArrayList<>();
		final List<VideOSCSettingsListFragment.Settings> settings = new ArrayList<>();
		final ContentValues values = new ContentValues();

		addAddress.setOnClickListener(new AddAddressButtonOnClickListener(db, values, addIPAddress, addPort, addProtocol));

		final String[] settingsFields = new String[]{
				SettingsContract.SettingsEntries._ID,
				SettingsContract.SettingsEntries.UDP_RECEIVE_PORT,
				SettingsContract.SettingsEntries.ROOT_CMD
		};

		final String[] addrFields = new String[]{
				SettingsContract.AddressSettingsEntry.IP_ADDRESS,
				SettingsContract.AddressSettingsEntry.PORT,
				SettingsContract.AddressSettingsEntry.PROTOCOL,
				SettingsContract.AddressSettingsEntry._ID
		};
		final String sortOrder = SettingsContract.AddressSettingsEntry.IP_ADDRESS + " ASC";

		mAddressesCursor = db.query(
				SettingsContract.AddressSettingsEntry.TABLE_NAME,
				addrFields,
				null,
				null,
				null,
				null,
				sortOrder
		);

		final int[] to = new int[] {
				R.id.remote_ip_address,
				R.id.remote_port,
				R.id.address_protocol,
				R.id.entry_id
		};
		final SimpleCursorAdapter addressesAdapter = new SimpleCursorAdapter(
				getActivity(), R.layout.address_list_item, mAddressesCursor, addrFields, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);

		final ListView addressesList = mView.findViewById(R.id.addresses_list);
		addressesList.setAdapter(addressesAdapter);

		mAddresses.clear();

		while (mAddressesCursor.moveToNext()) {
			final VideOSCSettingsListFragment.Address address = new VideOSCSettingsListFragment.Address();
			final long addressId = mAddressesCursor.getLong(mAddressesCursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry._ID));
			final String addressIP = mAddressesCursor.getString(mAddressesCursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.IP_ADDRESS));
			final int port = mAddressesCursor.getInt(mAddressesCursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.PORT));
			final String protocol = mAddressesCursor.getString(mAddressesCursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.PROTOCOL));
			address.setRowId(addressId);
			address.setIP(addressIP);
			address.setPort(port);
			address.setProtocol(protocol);
			mAddresses.add(address);
		}

		Cursor cursor = db.query(
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
					mAddresses.get(0).setReceivePort(Integer.parseInt(receivePort, 10));
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
	public void onDestroy() {
		super.onDestroy();
		mAddressesCursor.close();
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

	private class AddAddressButtonOnClickListener implements View.OnClickListener {
		final private SQLiteDatabase mDb;
		final private ContentValues mValues;
		final private EditText mAddIPAddress;
		final private EditText mAddPort;
		final private Button mSetProtocol;

		AddAddressButtonOnClickListener(SQLiteDatabase db, ContentValues values, EditText addIPAddress, EditText addPort, Button setProtocol) {
			this.mDb = db;
			this.mValues = values;
			this.mAddIPAddress = addIPAddress;
			this.mAddPort = addPort;
			this.mSetProtocol = setProtocol;
		}

		/**
		 * Called when a view has been clicked.
		 *
		 * @param v The view that was clicked.
		 */
		@Override
		public void onClick(View v) {
			final String addAddressText = mAddIPAddress.getText().toString();
			final String addPortVal = mAddPort.getText().toString();
			final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
			int steps = 0;
			String msg = getString(R.string.warning_address_header);
			Log.d(TAG, "CLICK! " + steps);


			if (addAddressText.length() > 0) {
				if (Patterns.IP_ADDRESS.matcher(addAddressText).matches()) {
					steps++;
					mValues.put(
							SettingsContract.AddressSettingsEntry.IP_ADDRESS,
							addAddressText
					);
				} else {
					msg = msg.concat(getString(R.string.warning_invalid_ip));
				}
			} else {
				msg = msg.concat(getString(R.string.warning_empty_ip));
			}

			if (addPortVal.length() > 0) {
				int portVal = Integer.parseInt(mAddPort.getText().toString(), 10);
				if (portVal >= 0 && portVal <= 65535) {
					steps++;
					mValues.put(
							SettingsContract.AddressSettingsEntry.PORT,
							addPortVal
					);
				} else {
					msg = msg.concat(getString(R.string.warning_invalid_port));
				}
			} else {
				msg = msg.concat(getString(R.string.warning_empty_port));
				VideOSCDialogHelper.showWarning(
						activity,
						android.R.style.Theme_Holo_Light_Dialog,
						msg,
						"ok"
				);
			}

			if (steps == 2) {
				mValues.put(
						SettingsContract.AddressSettingsEntry.PROTOCOL,
						mSetProtocol.getText().toString()
				);
				if (mAddresses.contains(mValues)) {
					// TODO: create warning
				}
				long ret = mDb.insert(
						SettingsContract.AddressSettingsEntry.TABLE_NAME,
						null,
						mValues
				);

				Log.d(TAG, "db result: " + ret);
			}

			mValues.clear();
		}
	}
}
