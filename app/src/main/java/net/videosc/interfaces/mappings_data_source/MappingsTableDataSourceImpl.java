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
import net.videosc.utilities.enums.CommandMappingsSortModes;

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
        return getCommands(mApp.getCommandMappingsSortMode()).size() + 1;
    }

    @Override
    public int getColumnsCount() {
        return getAddresses().size() + 1;
    }

/*
    @Override
    public Button getFirstHeaderData() {
        return null;
    }
*/

    @Override
    public String getRowHeaderData(int index) {
        final ArrayList<String> commands = getCommands(mApp.getCommandMappingsSortMode());
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

    public boolean rowIsFull(int rowIndex) {
        boolean isFull = true;
        final int numCols = getColumnsCount()-1;
        for (int i = 0; i < numCols; i++) {
            if (getItemData(rowIndex, i+1) == '0') {
                isFull = false;
                break;
            }
        }
        return isFull;
    }

    public boolean rowHasAtLeastTwoMappings(int row) {
        final int numCols = getColumnsCount()-1;
        int count = 0;
        for (int i = 0; i < numCols; i++) {
            if (getItemData(row, i+1) == '1') count++;
        }
        return count > 1;
    }

    private ArrayList<String> getAddresses() {
        Resources res = mActivity.getResources();
        final ArrayList<String> addresses = new ArrayList<>();

        final String[] addrFields = new String[]{
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

    private ArrayList<String> getCommands(CommandMappingsSortModes sortMode) {
        final ArrayList<String> commands = new ArrayList<>();
        final String[] colors = new String[]{"red", "green", "blue"};
        final Point res = mApp.getResolution();
        final int size = res.x * res.y;
        String rootCmd = "";

        final String[] rootCmdFields = new String[]{
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

        if (sortMode.equals(CommandMappingsSortModes.SORT_BY_COLOR)) {
            for (String color : colors) {
                for (int i = 0; i < size;) {
                    commands.add("/" + rootCmd + "/" + color + (++i));
                }
            }
        } else if (sortMode.equals(CommandMappingsSortModes.SORT_BY_NUM)) {
            for (int i = 0; i < size; i++) {
                for (String color : colors) {
                    commands.add("/" + rootCmd + "/" + color + (i+1));
                }
            }
        }

        return commands;
    }

    private ArrayList<String> getMappings() {
        final ArrayList<String> mappings = new ArrayList<>();

        String[] mappingsFields = new String[]{
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

    // if a row in VideOSC is sending to all clients
    // a click should select a single cell in a row
    public String setFullRowData(int row, int column) {
        final int numCols = getColumnsCount()-1;
        StringBuilder rowData = new StringBuilder();
        for (int i = 0; i < numCols; i++) {
            if (i + 1 == column) {
                rowData.append(1);
            } else {
                rowData.append(0);
            }
        }
        return String.valueOf(rowData);
    }

    // get mappings in specified row
    public String getRowData(int row) {
        StringBuilder rowData = new StringBuilder();
        final int numCols = getColumnsCount()-1;
        for (int i = 0; i < numCols; i++) {
            rowData.append(getItemData(row, i+1));
        }

        return String.valueOf(rowData);
    }

    // one cell in a row should always remain selected
    public String setItemData(int row, int column) {
        StringBuilder rowData = new StringBuilder(getRowData(row));
        Character itemData = getItemData(row, column);
        int newVal = itemData == '0' ? 1 : 0;
        rowData.setCharAt(column+1, (char) newVal);

        return String.valueOf(rowData);
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
