package net.videosc.fragments.settings;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
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
    private EditText mAddIPAddress;
    private EditText mAddPort;
    private Button mAddProtocol;
    private ArrayAdapter<String> mProtocolsAdapter;
    private PopupWindow mProtocolsPopUp;
    private Cursor mAddressesCursor;
    private SimpleCursorAdapter mAddressesAdapter;
    private ArrayList<VideOSCSettingsListFragment.Address> mAddresses;
    private ArrayList<String[]> mAddressStrings = new ArrayList<>();

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

        mAddIPAddress = mView.findViewById(R.id.add_remote_ip);
        mAddPort = mView.findViewById(R.id.add_remote_port);
        mAddProtocol = mView.findViewById(R.id.set_protocol);
		final String[] protocols = new String[] {"UDP", "TCP/IP"};
		mProtocolsAdapter = new ArrayAdapter<>(mActivity, R.layout.protocols_select_item, protocols);
		mProtocolsPopUp = showProtocolsList(mProtocolsAdapter);
		mAddProtocol.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mProtocolsPopUp.showAsDropDown(v, 0, 0);
			}
		});
		ListView protocolsList = (ListView) mProtocolsPopUp.getContentView();
		protocolsList.setOnItemClickListener(new ProtocolsOnItemClickListener());

        final Button addAddress = mView.findViewById(R.id.add_address_button);

        mAddresses = new ArrayList<>();
        final List<VideOSCSettingsListFragment.Settings> settings = new ArrayList<>();
        final ContentValues values = new ContentValues();

        addAddress.setOnClickListener(new AddAddressButtonOnClickListener(db, values, mAddIPAddress, mAddPort, mAddProtocol));

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
        
        final int[] to = new int[]{
                R.id.remote_ip_address,
                R.id.remote_port,
                R.id.address_protocol,
                R.id.entry_id
        };
        mAddressesAdapter = new SimpleCursorAdapter(
                getActivity(), R.layout.address_list_item, mAddressesCursor, addrFields, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        final ListView addressesList = mView.findViewById(R.id.addresses_list);
        addressesList.setAdapter(mAddressesAdapter);
        mAddressesAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.d(TAG, "data set changed, cursor: " + mAddressesCursor.getCount());
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                Log.d(TAG, "data set invalidated");
            }
        });

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
            // for comparision before submitting current entry
            mAddressStrings.add(new String[]{addressIP, String.valueOf(port), protocol});
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

	private PopupWindow showProtocolsList(ArrayAdapter protocolsAdapter) {
    	final PopupWindow popUp = new PopupWindow(mActivity);
    	final ListView protocolsList = new ListView(mActivity);
    	protocolsList.setAdapter(protocolsAdapter);
    	popUp.setFocusable(true);
    	popUp.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
    	popUp.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
    	popUp.setContentView(protocolsList);

    	return popUp;
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
        private String[] mWarningStrings;

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
            int steps = 0;
            String msg = getString(R.string.warning_address_header);

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
            }

            if (steps == 2) {
                Log.d(TAG, "protocol is set to: " + mSetProtocol.getText());
                mValues.put(
                        SettingsContract.AddressSettingsEntry.PROTOCOL,
                        mSetProtocol.getText().toString()
                );
                String[] compareString = new String[]{addAddressText, addPortVal, (String) mSetProtocol.getText()};
                final short compResult = compare(compareString, mAddressStrings);

                switch (compResult) {
                    case 1:
                    	Log.d(TAG, "case 1");
                        VideOSCDialogHelper.showDialog(
                                mActivity,
                                android.R.style.Theme_Holo_Light_Dialog,
                                String.format(getString(R.string.warning_same_address_different_protocol), mWarningStrings[0], mWarningStrings[1], mWarningStrings[2]),
                                getString(R.string.OK),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        long ret = insertIntoDatabase(mDb, mValues);
                                        resetRemoteClientInputs();
                                        Log.d(TAG, "new database entry with ID " + ret);
                                        mAddressesAdapter.notifyDataSetChanged();
                                    }
                                },
                                getString(R.string.cancel),
                                null
                        );
                        break;
                    case 2:
                    	Log.d(TAG, "case 2");
                        VideOSCDialogHelper.showWarning(
                                mActivity,
                                android.R.style.Theme_Holo_Light_Dialog,
                                String.format(getString(R.string.warning_same_address_same_protocol), mWarningStrings[0], mWarningStrings[1], mWarningStrings[2]),
                                getString(R.string.OK)
                        );
                        break;
                    case 0:
                    	Log.d(TAG, "case 0");
                        long ret = mDb.insert(
                                SettingsContract.AddressSettingsEntry.TABLE_NAME,
                                null,
                                mValues
                        );
                        resetRemoteClientInputs();
                        mValues.clear();
                        Log.d(TAG, "db result: " + ret);
                        mAddressesAdapter.notifyDataSetChanged();
                        break;
                    default:
                }
            } else {
				VideOSCDialogHelper.showWarning(
						mActivity,
						android.R.style.Theme_Holo_Light_Dialog,
						msg,
						getString(R.string.OK)
				);
			}

//            mAddressStrings.clear();
        }

        private short compare(String[] toMatch, ArrayList<String[]> matchStrings) {
            for (String[] addr : matchStrings) {
                if (addr[0].equals(toMatch[0]) && addr[1].equals(toMatch[1])) {
                    if (!addr[2].equals(toMatch[2])) {
                        setWarningStrings(addr[0], addr[1], addr[2]);
                        return 1;
                    } else {
                        setWarningStrings(addr[0], addr[1], addr[2]);
                        return 2;
                    }
                }
            }

            return 0;
        }

        private void setWarningStrings(String ip, String port, String protocol) {
            this.mWarningStrings = new String[]{ip, port, protocol};
        }

        private long insertIntoDatabase(SQLiteDatabase db, ContentValues values) {
            long ret = db.insert(
                    SettingsContract.AddressSettingsEntry.TABLE_NAME,
                    null,
                    values
            );

            values.clear();
            return ret;
        }
    }

    private void resetRemoteClientInputs() {
        mAddIPAddress.setText("");
        mAddPort.setText("");
        mAddProtocol.setText("UDP");
    }

	private class ProtocolsOnItemClickListener implements android.widget.AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Animation fadeInAnimation = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in);
			fadeInAnimation.setDuration(2);
			view.startAnimation(fadeInAnimation);

			String item = mProtocolsAdapter.getItem(position);
			mAddProtocol.setText(item);
			mProtocolsPopUp.dismiss();
		}
	}

}
