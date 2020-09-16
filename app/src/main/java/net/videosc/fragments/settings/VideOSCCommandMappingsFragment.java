package net.videosc.fragments.settings;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cleveroad.adaptivetablelayout.AdaptiveTableLayout;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;

import java.util.ArrayList;

public class VideOSCCommandMappingsFragment extends VideOSCBaseFragment {
    private final static String TAG = VideOSCCommandMappingsFragment.class.getSimpleName();

    private AdaptiveTableLayout mTableLayout;
    private VideOSCMainActivity mActivity;
    private SQLiteDatabase mDb;
    private final ArrayList<String> mAddresses = new ArrayList<>();
    private final ArrayList<String> mCommands = new ArrayList<>();
    private VideOSCApplication mApplication;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "VideOSCCommandMappingsFragment onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.address_command_mappings_table, container, false);
        mTableLayout = view.findViewById(R.id.address_command_mappings_table);
        mActivity = (VideOSCMainActivity) getActivity();
        assert mActivity != null;
        mApplication = (VideOSCApplication) mActivity.getApplication();
        mDb = mActivity.getDatabase();

        final long addrCount = DatabaseUtils.queryNumEntries(mDb, SettingsContract.AddressSettingsEntries.TABLE_NAME);
        final Point resolution = mApplication.getResolution();
        final int commandsCount = resolution.x * resolution.y;

        if (addrCount > 1) {
            final String[] addressesFields = new String[]{
                    SettingsContract.AddressSettingsEntries._ID,
                    SettingsContract.AddressSettingsEntries.IP_ADDRESS,
                    SettingsContract.AddressSettingsEntries.PORT,
                    SettingsContract.AddressSettingsEntries.PROTOCOL
            };

            Cursor cursor = mDb.query(
                    SettingsContract.AddressSettingsEntries.TABLE_NAME,
                    addressesFields,
                    null,
                    null,
                    null,
                    null,
                    SettingsContract.AddressSettingsEntries._ID
            );

            while (cursor.moveToNext()) {
                final long rowID = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries._ID));
                final String ip = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.IP_ADDRESS));
                final int port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PORT));
                final String protocol = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PROTOCOL));
                mAddresses.add(ip + ":" + port + "\n(protocol: " + protocol + ")");
            }

            Log.d(TAG, "addresses: " + mAddresses);

            final String[] settingsCmdName = new String[] {
                    SettingsContract.SettingsEntries._ID,
                    SettingsContract.SettingsEntries.ROOT_CMD,
            };

            cursor = mDb.query(
                    SettingsContract.SettingsEntries.TABLE_NAME,
                    settingsCmdName,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();
            final String rootCmdName = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD));

            cursor.close();

            final String[] colors = new String[] {"/red", "/green", "/blue"};

            for (String color : colors) {
                for (int i = 0; i < commandsCount; i++) {
                    mCommands.add("/" + rootCmdName + color + (1 + i % commandsCount));
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }
}
