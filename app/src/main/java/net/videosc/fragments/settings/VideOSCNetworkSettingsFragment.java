package net.videosc.fragments.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import net.netP5android.NetAddress;
import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.adapters.AddressesListAdapter;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.utilities.VideOSCDialogHelper;
import net.videosc.utilities.VideOSCOscHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ketai.net.KetaiNet;

public class VideOSCNetworkSettingsFragment extends VideOSCBaseFragment {
    final private static String TAG = "NetworkSettingsFragment";
    private EditText mAddIPAddress;
    private EditText mAddPort;
    private Cursor mAddressesCursor;
    private AddressesListAdapter mAddressesAdapter;
    private SQLiteDatabase mDb;
    private final String[] mAddrFields = new String[]{
            SettingsContract.AddressSettingsEntries.IP_ADDRESS,
            SettingsContract.AddressSettingsEntries.PORT,
//            SettingsContract.AddressSettingsEntries.PROTOCOL,
            SettingsContract.AddressSettingsEntries._ID
    };
    private ArrayList<VideOSCSettingsListFragment.Address> mAddresses;

    public VideOSCNetworkSettingsFragment(Context context) {
        this.mActivity = (VideOSCMainActivity) context;
        this.mApp = (VideOSCApplication) mActivity.getApplication();
    }

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
        return inflater.inflate(R.layout.network_settings, container, false);
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
        final FragmentManager fragmentManager = getFragmentManager();
        assert fragmentManager != null;
        // in API 30 getting the cameraView only seems to work with fragmentManager retrieved through getFragmentManager, not getChildFragmentManager
        final VideOSCCameraFragment cameraView = (VideOSCCameraFragment) fragmentManager.findFragmentByTag("CamPreview");
        mDb = mActivity.getDatabase();
        final VideOSCOscHandler oscHelper = mApp.getOscHelper();

        mAddIPAddress = view.findViewById(R.id.add_remote_ip);
        mAddPort = view.findViewById(R.id.add_remote_port);

        final Button addAddress = view.findViewById(R.id.add_address_button);

        mAddresses = new ArrayList<>();
        final List<VideOSCSettingsListFragment.Settings> settings = new ArrayList<>();
        final ContentValues values = new ContentValues();

        addAddress.setOnClickListener(new AddAddressButtonOnClickListener(mDb, values, mAddIPAddress, mAddPort));

        final String[] settingsFields = new String[]{
                SettingsContract.SettingsEntries._ID,
                SettingsContract.SettingsEntries.UDP_RECEIVE_PORT,
                SettingsContract.SettingsEntries.TCP_RECEIVE_PORT,
                SettingsContract.SettingsEntries.ROOT_CMD,
                SettingsContract.SettingsEntries.TCP_PASSWORD
        };

        mAddressesCursor = queryAddresses();

        mAddressesAdapter = new AddressesListAdapter(
                getActivity(), R.layout.address_list_item, mAddressesCursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        mAddressesAdapter.setDatabase(mDb);

        final ListView addressesList = view.findViewById(R.id.addresses_list);
        addressesList.setAdapter(mAddressesAdapter);
        mAddressesAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
            }
        });

        mAddresses = getAddresses(mAddressesCursor);

        Cursor cursor = mDb.query(
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
            final int tcpReceivePort = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.TCP_RECEIVE_PORT));
            final String cmd = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD));
            final String tcpPasswd = cursor.getString((cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.TCP_PASSWORD)));
            setting.setRowId(rowId);
            setting.setUdpReceivePort(udpReceivePort);
            setting.setTcpReceivePort(tcpReceivePort);
            setting.setRootCmd(cmd);
            setting.setTcpPassword(tcpPasswd);
            settings.add(setting);
        }

        cursor.close();

        final EditText udpReceivePortField = view.findViewById(R.id.device_udp_port_field);
        udpReceivePortField.setText(
                String.format(Locale.getDefault(), "%d", settings.get(0).getUdpReceivePort()),
                TextView.BufferType.EDITABLE
        );
        final EditText tcpReceivePortField = view.findViewById(R.id.device_tcp_port_field);
        tcpReceivePortField.setText(
                String.format(Locale.getDefault(), "%d", settings.get(0).getTcpReceivePort()),
                TextView.BufferType.EDITABLE
        );
        final EditText rootCmdField = view.findViewById(R.id.root_cmd_name_field);
        rootCmdField.setText(
                settings.get(0).getRootCmd(),
                TextView.BufferType.EDITABLE
        );
        final EditText tcpPasswdField = view.findViewById(R.id.tcp_passwd);
        tcpPasswdField.setText(
                settings.get(0).getTcpPassword(),
                TextView.BufferType.EDITABLE
        );
        final TextView deviceIP = view.findViewById(R.id.device_ip_address);
        deviceIP.setText(KetaiNet.getIP());

        udpReceivePortField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean b) {
                final String udpPortString = udpReceivePortField.getText().toString();
                if (!udpPortString.equals(tcpReceivePortField.getText().toString())) {
                    if (!b && !udpPortString.equals(
                            String.format(Locale.getDefault(), "%d", settings.get(0).getUdpReceivePort()))) {
                        values.put(
                                SettingsContract.SettingsEntries.UDP_RECEIVE_PORT,
                                udpPortString
                        );
                        mDb.update(
                                SettingsContract.SettingsEntries.TABLE_NAME,
                                values,
                                SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
                                null
                        );
                        values.clear();
                        mAddresses.get(0).setUdpReceivePort(Integer.parseInt(udpPortString, 10));
                    }
                } else {
                    VideOSCDialogHelper.showWarning(
                            mActivity,
                            android.R.style.Theme_Holo_Light_Dialog,
                            getString(R.string.udp_receive_port_should_differ),
                            getString(R.string.OK)
                    );
                }
            }
        });

        tcpReceivePortField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final String tcpPortString = tcpReceivePortField.getText().toString();
                if (!tcpPortString.equals(udpReceivePortField.getText().toString())) {
                    if (!hasFocus && !tcpPortString.equals(
                            String.format(Locale.getDefault(), "%d", settings.get(0).getTcpReceivePort()))) {
                        values.put(
                                SettingsContract.SettingsEntries.TCP_RECEIVE_PORT,
                                tcpPortString
                        );
                        mDb.update(
                                SettingsContract.SettingsEntries.TABLE_NAME,
                                values,
                                SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
                                null
                        );
                        values.clear();
                        mAddresses.get(0).setTcpReceivePort(Integer.parseInt(tcpPortString, 10));
                    }
                } else {
                    VideOSCDialogHelper.showWarning(
                            mActivity,
                            android.R.style.Theme_Holo_Light_Dialog,
                            getString(R.string.tcp_receive_port_should_differ),
                            getString(R.string.OK)
                    );
                }
            }
        });

        rootCmdField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean b) {
                if (!b && !rootCmdField.getText().toString().equals(settings.get(0).getRootCmd())) {
                    final String rootCmd = rootCmdField.getText().toString();
                    values.put(
                            SettingsContract.SettingsEntries.ROOT_CMD,
                            rootCmd
                    );
                    mDb.update(
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

        tcpPasswdField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !tcpPasswdField.getText().toString().equals(settings.get(0).getTcpPassword())) {
                    final String passwd = tcpPasswdField.getText().toString();
                    values.put(
                            SettingsContract.SettingsEntries.TCP_PASSWORD,
                            passwd
                    );
                    mDb.update(
                            SettingsContract.SettingsEntries.TABLE_NAME,
                            values,
                            SettingsContract.SettingsEntries._ID + " = " + settings.get(0).getRowId(),
                            null
                    );
                    values.clear();
                    settings.get(0).setTcpPassword(passwd);
                }
            }
        });
    }

    private Cursor queryAddresses() {
        String sortOrder = SettingsContract.AddressSettingsEntries._ID + " DESC";
        return mDb.query(
                SettingsContract.AddressSettingsEntries.TABLE_NAME,
                mAddrFields, null, null, null, null, sortOrder
        );
    }

    private ArrayList<VideOSCSettingsListFragment.Address> getAddresses(Cursor cursor) {
        final ArrayList<VideOSCSettingsListFragment.Address> res = new ArrayList<>();
        while (cursor.moveToNext()) {
            final VideOSCSettingsListFragment.Address address = new VideOSCSettingsListFragment.Address();
            final long addressId = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries._ID));
            final String addressIP = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.IP_ADDRESS));
            final int port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PORT));
            address.setRowId(addressId);
            address.setIP(addressIP);
            address.setPort(port);

            res.add(address);
        }

        return res;
    }

    private ArrayList<String[]> getAddressesCompareStrings(Cursor cursor) {
        final ArrayList<String[]> res = new ArrayList<>();

        while (cursor.moveToNext()) {
            final String addressIP = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.IP_ADDRESS));
            final int port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PORT));
            res.add(new String[]{addressIP, String.valueOf(port)});
        }

        return res;
    }

	@Override
    public void onDetach() {
        super.onDetach();
        this.mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        mAddressesCursor.close();
    }

    private class AddAddressButtonOnClickListener implements View.OnClickListener {
        final private SQLiteDatabase mDb;
        final private ContentValues mValues;
        final private EditText mAddIPAddress;
        final private EditText mAddPort;
        private String[] mWarningStrings;

        AddAddressButtonOnClickListener(SQLiteDatabase db, ContentValues values, EditText addIPAddress, EditText addPort) {
            this.mDb = db;
            this.mValues = values;
            this.mAddIPAddress = addIPAddress;
            this.mAddPort = addPort;
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
            final String addAddressText = mAddIPAddress.getText().toString();
            final String addPortVal = mAddPort.getText().toString();
            int steps = 0;
            String msg = getString(R.string.warning_address_header);

            if (addAddressText.length() > 0) {
                if (Patterns.IP_ADDRESS.matcher(addAddressText).matches()) {
                    steps++;
                    mValues.put(
                            SettingsContract.AddressSettingsEntries.IP_ADDRESS,
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
                            SettingsContract.AddressSettingsEntries.PORT,
                            addPortVal
                    );
                } else {
                    msg = msg.concat(getString(R.string.warning_invalid_port));
                }
            } else {
                msg = msg.concat(getString(R.string.warning_empty_port));
            }

            if (steps == 2) {
//                final String protocolName = mSetProtocol.getText().toString();
//                int protocol;
//
//                if (protocolName.equals("TCP/IP")) {
//                    protocol = OscP5.TCP;
//                } else {
//                    protocol = OscP5.UDP;
//                }
//
//                mValues.put(
//                        SettingsContract.AddressSettingsEntries.PROTOCOL,
//                        protocol
//                );
//
                final String[] compareString = new String[]{addAddressText, addPortVal/*, String.valueOf(protocol)*/};
                mAddressesCursor = queryAddresses();
                final ArrayList<String[]> addressesStrings = getAddressesCompareStrings(mAddressesCursor);
                final short compResult = compare(compareString, addressesStrings);
//                final int innerProtocol = protocol;

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
                                        final long ret = insertIntoDatabase(mDb, mValues);
                                        if (ret > -1) {
                                            addAddressMappings(mDb, ret);
                                            app.putBroadcastClient((int) ret, new NetAddress(addAddressText, Integer.parseInt(addPortVal)));
                                        }
                                        resetRemoteClientInputs();
                                        mAddressesCursor = queryAddresses();
                                        mAddressesAdapter.changeCursor(mAddressesCursor);
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
                        final long ret = insertIntoDatabase(mDb, mValues);
                        if (ret > -1) {
                            addAddressMappings(mDb, ret);
                            app.putBroadcastClient((int) ret, new NetAddress(addAddressText, Integer.parseInt(addPortVal)));
                        }
                        resetRemoteClientInputs();
                        mValues.clear();
                        mAddressesCursor = queryAddresses();
                        mAddressesAdapter.changeCursor(mAddressesCursor);
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
        }

        private short compare(String[] toMatch, ArrayList<String[]> matchStrings) {
            for (String[] addr : matchStrings) {
                if (addr[0].equals(toMatch[0]) && addr[1].equals(toMatch[1])) {
                    setWarningStrings(addr[0], addr[1]);
//                    if (!addr[2].equals(toMatch[2])) {
//                        return 1;
//                    } else {
//                        return 2;
//                    }
                }
            }
            return 0;
        }

        private void setWarningStrings(String ip, String port) {
            this.mWarningStrings = new String[]{ip, port};
        }

        private long insertIntoDatabase(SQLiteDatabase db, ContentValues values) {
            long ret = db.insert(
                    SettingsContract.AddressSettingsEntries.TABLE_NAME,
                    null,
                    values
            );

            values.clear();
            return ret;
        }

        private void addAddressMappings(SQLiteDatabase db, long addressIndex) {
            final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
            final Point resolution = app.getResolution();
            // number of mappings: resolution.x * resolution.y * 3 - we have 3 channels: red, green and blue
            final StringBuilder mappingsBuilder = new StringBuilder(resolution.x * resolution.y * 3);
            for (int i = 0; i < resolution.x * resolution.y * 3; i++) {
                mappingsBuilder.append('1');
            }
            final String mappings = String.valueOf(mappingsBuilder);
            ContentValues values = new ContentValues();
            values.put(
                    SettingsContract.AddressCommandsMappings.ADDRESS,
                    addressIndex
            );
            values.put(
                    SettingsContract.AddressCommandsMappings.MAPPINGS,
                    mappings
            );
            long result = db.insert(
                    SettingsContract.AddressCommandsMappings.TABLE_NAME,
                    null,
                    values
            );
            values.clear();
            app.addCommandMappings((int) result, mappings);
        }
    }

    private void resetRemoteClientInputs() {
        mAddIPAddress.setText("");
        mAddPort.setText("");
    }

}
