package net.videosc.interfaces.mappings_data_source;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.util.Log;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;

import java.util.ArrayList;

public class MappingsTableDataSourceImpl implements MappingsTableDataSource<String, String, Character> {
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
        return getCommands().size()+1;
    }

    @Override
    public int getColumnsCount() {
        return getAddresses().size()+1;
    }

/*
    @Override
    public Object getFirstHeaderData() {
        return null;
    }
*/

    @Override
    public String getRowHeaderData(int index) {
        final ArrayList<String> commands = getCommands();
//        Log.d(TAG, "row at index " + index + ": " + commands.get(index));
        return commands.get(index);
    }

    @Override
    public String getColumnHeaderData(int index) {
        final ArrayList<String> addresses = getAddresses();
        Log.d(TAG, "address: " + addresses.get(index));
        return addresses.get(index);
    }

    @Override
    public Character getItemData(int rowIndex, int columnIndex) {
        final ArrayList<String> mappings = getMappings();
        char itemData;
        if (mappings.isEmpty()) {
            itemData = '1';
        } else {
            itemData = mappings.get(columnIndex).charAt(rowIndex);
        }
        return itemData;
    }

    private ArrayList<String> getAddresses() {
        Resources res = mActivity.getResources();
        final ArrayList<String> addresses = new ArrayList<>();

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
            addresses.add(ip + ":" + port + String.format(res.getString(R.string.protocol_label), protocol));
        }

        cursor.close();
        Log.d(TAG, "addresses: " + addresses);
        return addresses;
    }

    private ArrayList<String> getCommands() {
        final ArrayList<String> commands = new ArrayList<>();
        final String[] colors = new String[] {"red", "green", "blue"};
        final Point res = mApp.getResolution();
        final int size = res.x * res.y;
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

//        cursor.close();

        final String[] panels = new String[] {
//                SettingsContract.Panels._ID,
//                SettingsContract.Panels.NAME,
                SettingsContract.Panels.CMD
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

        ArrayList<String> panelCmds = new ArrayList<>();
        while (cursor.moveToNext()) {
//            final long panelID = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.Panels._ID));
            final String panelCmd = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.Panels.CMD));
            panelCmds.add(panelCmd);
        }

        for (String panelCmd : panelCmds) {
            for (String color : colors) {
                for (int i = 0; i < size; ) {
                    commands.add("/" + rootCmd + "/" + panelCmd + "/" + color + (++i));
                }
            }
        }

//        Log.d(TAG, "commands: " + commands);

        cursor.close();

        return commands;
    }

    private ArrayList<String> getMappings() {
        final ArrayList<String> mappings = new ArrayList<>();

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
            mappings.add(mappingsString);
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
