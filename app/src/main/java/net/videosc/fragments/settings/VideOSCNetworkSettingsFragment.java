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
import android.widget.EditText;
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

		final List<VideOSCSettingsListFragment.Address> addresses = new ArrayList<>();
		final List<VideOSCSettingsListFragment.Settings> settings = new ArrayList<>();
		final ContentValues values = new ContentValues();

		addAddress.setOnClickListener(new AddAddressButtonOnClickListener(db, values, addIPAddress, addPort, addProtocol));

		final String[] settingsFields = new String[]{
				SettingsContract.SettingsEntries._ID,
				SettingsContract.SettingsEntries.UDP_RECEIVE_PORT,
				SettingsContract.SettingsEntries.ROOT_CMD
		};

		final String[] addrFields = new String[]{
				SettingsContract.AddressSettingsEntry._ID,
				SettingsContract.AddressSettingsEntry.IP_ADDRESS,
				SettingsContract.AddressSettingsEntry.PORT,
				SettingsContract.AddressSettingsEntry.PROTOCOL
		};
		final String sortOrder = SettingsContract.AddressSettingsEntry.IP_ADDRESS + " DESC";

		Cursor addressesCursor = db.query(
				SettingsContract.AddressSettingsEntry.TABLE_NAME,
				addrFields,
				null,
				null,
				null,
				null,
				sortOrder
		);

		final AddressListFragment addressesListFragment = new AddressListFragment();
		addressesListFragment.setCursor(addressesCursor);
		fragmentManager.beginTransaction().add(R.id.address_list_fragment, addressesListFragment).commit();

		addresses.clear();

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
			boolean goOn = false;

			if (addAddressText.length() > 0) {
				if (Patterns.IP_ADDRESS.matcher(addAddressText).matches()) {
					goOn = true;
					mValues.put(
							SettingsContract.AddressSettingsEntry.IP_ADDRESS,
							addAddressText
					);
				} else {
					VideOSCDialogHelper.showWarning(
							activity,
							android.R.style.Theme_Holo_Light_Dialog,
							"The given IP address is invalid!",
							"ok"
					);
				}
			} else {
				VideOSCDialogHelper.showWarning(
						activity,
						android.R.style.Theme_Holo_Light_Dialog,
						"The IP address must not be empty!",
						"ok"
				);
			}

			if (goOn) {
				if (addPortVal.length() > 0) {
					int portVal = Integer.parseInt(mAddPort.getText().toString(), 10);
					if (portVal >= 0 && portVal <= 65535) {
						mValues.put(
								SettingsContract.AddressSettingsEntry.PORT,
								addPortVal
						);
					} else {
						goOn = false;
						VideOSCDialogHelper.showWarning(
								activity,
								android.R.style.Theme_Holo_Light_Dialog,
								"The given port is invalid!",
								"ok"
						);
					}
				} else {
					goOn = false;
					VideOSCDialogHelper.showWarning(
							activity,
							android.R.style.Theme_Holo_Light_Dialog,
							"Port must be an integer value between 0 and 65535",
							"ok"
					);
				}
			}

			if (goOn) {
				mValues.put(
						SettingsContract.AddressSettingsEntry.PROTOCOL,
						mSetProtocol.getText().toString()
				);
			}

			Log.d(TAG, "values: " + mValues);

			if (goOn) {
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
