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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.adapters.AddressesListAdapter;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.utilities.TcpAddress;
import net.videosc.utilities.UdpAddress;
import net.videosc.utilities.VideOSCDialogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ketai.net.KetaiNet;
import oscP5.OscP5;

public class VideOSCNetworkSettingsFragment extends VideOSCBaseFragment {
    final private static String TAG = "NetworkSettingsFragment";
    private EditText mAddIPAddress;
    private EditText mAddPort;
    private Button mAddProtocol;
    private ArrayAdapter<String> mProtocolsAdapter;
    private PopupWindow mProtocolsPopUp;
    private Cursor mAddressesCursor;
    private AddressesListAdapter mAddressesAdapter;
    private SQLiteDatabase mDb;
    private final String[] mAddrFields = new String[]{
            SettingsContract.AddressSettingsEntries.IP_ADDRESS,
            SettingsContract.AddressSettingsEntries.PORT,
            SettingsContract.AddressSettingsEntries.PROTOCOL,
            SettingsContract.AddressSettingsEntries._ID
    };
    private ArrayList<VideOSCSettingsListFragment.Address> mAddresses;
    private final ArrayList<String[]> mAddressStrings = new ArrayList<>();

    public VideOSCNetworkSettingsFragment(Context context) {
        this.mActivity = (VideOSCMainActivity) context;
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

        mAddIPAddress = view.findViewById(R.id.add_remote_ip);
        mAddPort = view.findViewById(R.id.add_remote_port);
        mAddProtocol = view.findViewById(R.id.set_protocol);
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

        final Button addAddress = view.findViewById(R.id.add_address_button);

        mAddresses = new ArrayList<>();
        final List<VideOSCSettingsListFragment.Settings> settings = new ArrayList<>();
        final ContentValues values = new ContentValues();

        addAddress.setOnClickListener(new AddAddressButtonOnClickListener(mDb, values, mAddIPAddress, mAddPort, mAddProtocol));

        final String[] settingsFields = new String[]{
                SettingsContract.SettingsEntries._ID,
                SettingsContract.SettingsEntries.UDP_RECEIVE_PORT,
                SettingsContract.SettingsEntries.TCP_RECEIVE_PORT,
                SettingsContract.SettingsEntries.ROOT_CMD
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

        mAddresses.clear();

        while (mAddressesCursor.moveToNext()) {
            final VideOSCSettingsListFragment.Address address = new VideOSCSettingsListFragment.Address();
            final long addressId = mAddressesCursor.getLong(mAddressesCursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries._ID));
            final String addressIP = mAddressesCursor.getString(mAddressesCursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.IP_ADDRESS));
            final int port = mAddressesCursor.getInt(mAddressesCursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PORT));
            final int protocol = mAddressesCursor.getInt(mAddressesCursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PROTOCOL));
            address.setRowId(addressId);
            address.setIP(addressIP);
            address.setPort(port);
            address.setProtocol(protocol);
            // for comparision before submitting current entry
            mAddressStrings.add(new String[]{addressIP, String.valueOf(port), String.valueOf(protocol)});
            mAddresses.add(address);
        }

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
            setting.setRowId(rowId);
            setting.setUdpReceivePort(udpReceivePort);
            setting.setTcpReceivePort(tcpReceivePort);
            setting.setRootCmd(cmd);
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
        rootCmdField.setText(settings.get(0).getRootCmd(), TextView.BufferType.EDITABLE);
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
                    String rootCmd = rootCmdField.getText().toString();
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
    }

    private Cursor queryAddresses() {
        String sortOrder = SettingsContract.AddressSettingsEntries._ID + " DESC";
        return mDb.query(
                SettingsContract.AddressSettingsEntries.TABLE_NAME,
                mAddrFields, null, null, null, null, sortOrder
        );
    }

    private PopupWindow showProtocolsList(ArrayAdapter<String> protocolsAdapter) {
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
        this.mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAddressesCursor.close();
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
                final String protocolName = mSetProtocol.getText().toString();
                int protocol;

                if (protocolName.equals("TCP/IP")) {
                    protocol = OscP5.TCP;
                } else {
                    protocol = OscP5.UDP;
                }

                mValues.put(
                        SettingsContract.AddressSettingsEntries.PROTOCOL,
                        protocol
                );

                String[] compareString = new String[]{addAddressText, addPortVal, String.valueOf(protocol)};
                final short compResult = compare(compareString, mAddressStrings);

                switch (compResult) {
                    case 1:
                    	Log.d(TAG, "case 1");
                    	final int innerProtocol = protocol;
                        VideOSCDialogHelper.showDialog(
                                mActivity,
                                android.R.style.Theme_Holo_Light_Dialog,
                                String.format(getString(R.string.warning_same_address_different_protocol), mWarningStrings[0], mWarningStrings[1], mWarningStrings[2]),
                                getString(R.string.OK),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        long ret = insertIntoDatabase(mDb, mValues);
                                        if (ret > -1) {
                                            addAddressMappings(mDb, ret);
                                            if (innerProtocol == OscP5.TCP) {
                                                app.putBroadcastClient((int) ret, new TcpAddress(addAddressText, Integer.parseInt(addPortVal)));
                                            } else {
                                                app.putBroadcastClient((int) ret, new UdpAddress(addAddressText, Integer.parseInt(addPortVal)));
                                            }
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
                        long ret = insertIntoDatabase(mDb, mValues);
                        if (ret > -1) {
                            addAddressMappings(mDb, ret);
                            if (protocol == OscP5.TCP) {
                                app.putBroadcastClient((int) ret, new TcpAddress(addAddressText, Integer.parseInt(addPortVal)));
                            } else {
                                app.putBroadcastClient((int) ret, new UdpAddress(addAddressText, Integer.parseInt(addPortVal)));
                            }
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

//            mAddressStrings.clear();
        }

        private short compare(String[] toMatch, ArrayList<String[]> matchStrings) {
            String protocol;
            for (String[] addr : matchStrings) {
                if (Integer.parseInt(addr[2]) == OscP5.TCP) {
                    protocol = "TCP/IP";
                } else {
                    protocol = "UDP";
                }
                if (addr[0].equals(toMatch[0]) && addr[1].equals(toMatch[1])) {
                    setWarningStrings(addr[0], addr[1], protocol);
                    if (!addr[2].equals(toMatch[2])) {
                        return 1;
                    } else {
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
                    SettingsContract.AddressSettingsEntries.TABLE_NAME,
                    null,
                    values
            );

            values.clear();
            return ret;
        }

        private void addAddressMappings(SQLiteDatabase db, long addressIndex) {
            VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
            Point resolution = app.getResolution();
            // number of mappings: resolution.x * resolution.y * 3 - we have 3 channels: red, green and blue
            StringBuilder mappings = new StringBuilder(resolution.x * resolution.y * 3);
            for (int i = 0; i < resolution.x * resolution.y * 3; i++) {
                mappings.append('1');
            }
            ContentValues values = new ContentValues();
            values.put(
                    SettingsContract.AddressCommandsMappings.ADDRESS,
                    addressIndex
            );
            values.put(
                    SettingsContract.AddressCommandsMappings.MAPPINGS,
                    String.valueOf(mappings)
            );
            db.insert(
                    SettingsContract.AddressCommandsMappings.TABLE_NAME,
                    null,
                    values
            );
            values.clear();
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
