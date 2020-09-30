package net.videosc.interfaces.mappings_data_source;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;

import java.util.ArrayList;
import java.util.HashMap;

public class MappingsTableDataSourceImpl implements MappingsTableDataSource<String, String, String> {
    final private static String TAG = MappingsTableDataSourceImpl.class.getSimpleName();
    final private SQLiteDatabase mDb;
    final private VideOSCApplication mApp;
    private final VideOSCMainActivity mActivity;

    public MappingsTableDataSourceImpl(VideOSCMainActivity activity) {
        this.mActivity = activity;
        this.mDb = activity.getDatabase();
        this.mApp = (VideOSCApplication) activity.getApplication();
        init();
    }

    private void init() {

    }

    @Override
    public int getRowsCount() {
        return 0;
    }

    @Override
    public int getColumnsCount() {
        return 0;
    }

/*
    @Override
    public Object getFirstHeaderData() {
        return null;
    }
*/

    @Override
    public String getRowHeaderData(int index) {

        return null;
    }

    @Override
    public String getColumnHeaderData(int index) {
        return null;
    }

    @Override
    public String getItemData(int rowIndex, int columnIndex) {

        return null;
    }

    // TODO: Are HashMaps the best solution here?

    private HashMap<Long, String> getAddresses() {
        Resources res = mActivity.getResources();
        final HashMap<Long, String> addresses = new HashMap<>();

        final String[] addrFields = new String[] {
                SettingsContract.AddressSettingsEntries._ID,
                SettingsContract.AddressSettingsEntries.IP_ADDRESS,
                SettingsContract.AddressSettingsEntries.PORT,
                SettingsContract.AddressSettingsEntries.PROTOCOL
        };

        final Cursor cursor = mDb.query(
                SettingsContract.AddressSettingsEntries.TABLE_NAME,
                addrFields,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            final long addrID = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries._ID));
            final String ip = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.IP_ADDRESS));
            final int port = cursor.getInt((cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PORT)));
            final String protocol = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PROTOCOL));
            addresses.put(addrID, ip + ":" + port + String.format(res.getString(R.string.protocol_label), protocol));
        }

        cursor.close();

        return addresses;
    }

    private ArrayList<String> getCommands(int width, int height) {
        final ArrayList<String> commands = new ArrayList<>();
        final String[] colors = new String[] {"red", "green", "blue"};
        final int size = width * height;
        String rootCmd = "";

        final String[] rootCmdFields = new String[] {
                SettingsContract.SettingsEntries._ID,
                SettingsContract.SettingsEntries.ROOT_CMD
        };

        Cursor cursor = mDb.query(
                SettingsContract.SettingsEntries.TABLE_NAME,
                rootCmdFields,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            rootCmd = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD));
        }

        cursor.close();

        final String[] panels = new String[] {
                SettingsContract.Panels._ID,
                SettingsContract.Panels.NAME
        };

        cursor = mDb.query(
                SettingsContract.Panels.TABLE_NAME,
                panels,
                null,
                null,
                null,
                null,
                null
        );

        ArrayList<String> panelNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            final long panelID = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.Panels._ID));
            final String panelName = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.Panels.NAME));
            panelNames.add((int) panelID, panelName);
        }

        for (String panel : panelNames) {
            for (String color : colors) {
                for (int i = 0; i < size; ) {
                    commands.add("/" + rootCmd + "/" + color + (++i) + "/" + panel);
                }
            }
        }

        cursor.close();

        return commands;
    }

    private HashMap<Long, String> getMappings() {
        final HashMap<Long, String> mappings = new HashMap<>();

        String[] mappingsFields = new String[] {
                SettingsContract.AddressCommandsMappings._ID,
                SettingsContract.AddressCommandsMappings.ADDRESS,
                SettingsContract.AddressCommandsMappings.MAPPINGS
        };

        Cursor cursor = mDb.query(
                SettingsContract.AddressCommandsMappings.TABLE_NAME,
                mappingsFields,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            final long addrID = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.AddressCommandsMappings.ADDRESS));
            final String mappingsString = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressCommandsMappings.MAPPINGS));
            mappings.put(addrID, mappingsString);
        }

        cursor.close();

        return mappings;
    }

    public void updateMappings(long addrID, String mappings) {
        ContentValues values = new ContentValues();
        values.put(
                SettingsContract.AddressCommandsMappings.MAPPINGS,
                mappings
        );
        mDb.update(
                SettingsContract.AddressCommandsMappings.TABLE_NAME,
                values,
                SettingsContract.AddressCommandsMappings.ADDRESS + " = " + addrID,
                null
        );
    }

}
