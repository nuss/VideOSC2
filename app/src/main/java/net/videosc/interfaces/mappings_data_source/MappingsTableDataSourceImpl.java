package net.videosc.interfaces.mappings_data_source;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.util.Log;
import android.util.SparseArray;

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
    private SparseArray<String> mMappings;
    private final SparseArray<String> mAddresses;
    private final ArrayList<String> mCommands;
    private final CommandMappingsSortModes mSortMode;

    public MappingsTableDataSourceImpl(VideOSCMainActivity activity) {
        this.mActivity = activity;
        this.mDb = activity.getDatabase();
        this.mApp = (VideOSCApplication) activity.getApplication();
        this.mSortMode = mApp.getCommandMappingsSortMode();
        this.mAddresses = getAddresses();
        this.mCommands = getCommands(this.mSortMode);
        initSortMode();
    }

    private void initSortMode() {
        // get mMappings from database
        // use getCachedMappings to retrieve current mappings
        // cached mappings should be treated only by their row indexes
        // independently from sort mode
        // mMappings must be reordered before updating database
        getMappings();
        // TODO: check for added or deleted addresses

        if (mAddresses.size() > mMappings.size()) {
            Log.d(TAG, "mappings before: " + mMappings);
            Point res = mApp.getResolution();
            StringBuilder mappingsStringB = new StringBuilder(res.x * res.y * 3);
            for (int i = 0; i < res.x * res.y * 3; i++) {
                mappingsStringB.append('1');
            };
            String mappingString = String.valueOf(mappingsStringB);
            for (int i = 0; i < mAddresses.size(); i++) {
                int addrKey = mAddresses.keyAt(i);
                if (mMappings.get(addrKey) == null) {
                    mMappings.put(addrKey, mappingString);
                    Log.d(TAG, "mappings after: " + mMappings);
                }
            }
        }

        if (mApp.getCommandMappingsSortMode().equals(CommandMappingsSortModes.SORT_BY_NUM)) {
            for (int i = 0; i < mMappings.size(); i++) {
                int key = mMappings.keyAt(i);
                String oldMappings = mMappings.valueAt(i);
//                Log.d(TAG, "mappings at '" + key + "', length: " + oldMappings.length() + ", mappings: " + oldMappings);
                StringBuilder newMappings = new StringBuilder();
                int colorBlockSize = oldMappings.length()/3;
                for (int j = 0; j < colorBlockSize; j++) {
                    // first we iterate over the block length...
                    for (int k = 0; k < 3; k++) {
                        // ... then we take the element at i + j * blocklength = same number in next color
//                        Log.d(TAG, "next row: " + (j+(k*colorBlockSize)));
                        newMappings.append(oldMappings.charAt(j+(k*colorBlockSize)));
                    }
                }
                mMappings.put(key, String.valueOf(newMappings));
            }
        }
    }

    public SparseArray<String> getCachedMappings() {
        return this.mMappings;
    }

    @Override
    public int getRowsCount() {
        return mCommands.size();
    }

    @Override
    public int getColumnsCount() {
        return mAddresses.size();
    }

/*
    @Override
    public Button getFirstHeaderData() {
        return null;
    }
*/

    @Override
    public String getRowHeaderData(int index) {
//        Log.d(TAG, "row at index " + index + ": " + commands.get(index));
        return mCommands.get(index);
    }

    @Override
    public String getColumnHeaderData(int index) {
//        Log.d(TAG, "address: " + mAddresses.valueAt(index));
        return mAddresses.valueAt(index);
    }

    @Override
    public Character getItemData(int rowIndex, int columnIndex) {
        Log.d(TAG, "mMappings size: " + mMappings.size() + "\nmMappings at column " + columnIndex + ": " + mMappings.keyAt(columnIndex) + ", " + mMappings.valueAt(columnIndex) + "\ngetItemData, row: " + rowIndex + ", column: " + columnIndex);
        char itemData;
        if (mMappings.size() > 0) {
            if (mMappings.valueAt(columnIndex).isEmpty()) {
                itemData = '1';
            } else {
//                Log.d(TAG, "mapping in row " + rowIndex + ", column " + columnIndex + " is " + mMappings.valueAt(columnIndex).charAt(rowIndex));
                itemData = mMappings.valueAt(columnIndex).charAt(rowIndex);
            }
        } else {
            itemData = '1';
        }
        return itemData;
    }

    public boolean rowIsFull(int rowIndex) {
        boolean isFull = true;
        final int numCols = getColumnsCount();
        for (int i = 0; i < numCols; i++) {
            if (getItemData(rowIndex, i) == '0') {
                isFull = false;
                break;
            }
        }
//        Log.d(TAG, "rowIsFull: " + isFull);
        return isFull;
    }

    public boolean rowHasAtLeastTwoMappings(int row) {
        final int numCols = getColumnsCount();
        int count = 0;
        for (int i = 0; i < numCols; i++) {
            if (getItemData(row, i) == '1') count++;
        }
        return count > 1;
    }

    private SparseArray<String> getAddresses() {
        Resources res = mActivity.getResources();
        final SparseArray<String> addresses = new SparseArray<>();

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
            addresses.put((int) addrID, ip + ":" + port + String.format(res.getString(R.string.protocol_label), protocol));
        }

        cursor.close();

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

    @Override
    public void getMappings() {
        final SparseArray<String> mappings = new SparseArray<>();

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
            String mappingsString = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressCommandsMappings.MAPPINGS));
            mappings.put((int) addrID, mappingsString);
        }

        cursor.close();

//        Log.d(TAG, "mappings: " + mappings);
        this.mMappings = mappings;
    }

    // if a row in VideOSC is sending to all clients
    // a click should select a single cell in a row
    public void setFullRowData(int row, int column) {
        final int numCols = getColumnsCount();
        Log.d(TAG, "setFullRowData: " + row + "/" + column + ", num columns: " + numCols);
        StringBuilder rowData = new StringBuilder();
        for (int i = 0; i < numCols; i++) {
            char newMapping = '1';
            if (column != i) {
                newMapping = '0';
            }
            rowData.append(newMapping);
            StringBuilder columnData = new StringBuilder(getColumnData(i));
            columnData.setCharAt(row, newMapping);
            mMappings.put(mAddresses.keyAt(i), String.valueOf(columnData));
        }
    }

    // get mappings in specified row
    public String getRowData(int row) {
        // TODO: consider sort order?
        final StringBuilder rowData = new StringBuilder();
        final int numCols = getColumnsCount();
        for (int i = 0; i < numCols; i++) {
            rowData.append(getItemData(row, i));
        }

        return String.valueOf(rowData);
    }

    // get mappings in specified column
    public String getColumnData(int column) {
        // TODO: consider sort order?
        final StringBuilder columnData = new StringBuilder();
        final int numRows = getRowsCount();
        for (int i = 0; i < numRows; i++) {
            columnData.append(getItemData(i, column));
        }

        return String.valueOf(columnData);
    }

    // one cell in a row should always remain selected
    public void setItemData(int row, int column) {
        // FIXME: do we need rowData at all here?
//        StringBuilder rowData = new StringBuilder(getRowData(row));
        StringBuilder columnData = new StringBuilder(getColumnData(column));
        Character itemData = getItemData(row, column);
        int newVal = itemData == '0' ? '1' : '0';
//        rowData.setCharAt(column, (char) newVal);
        columnData.setCharAt(row, (char) newVal);

        for (int i = 0; i < getColumnsCount(); i++) {
            if (column == i)
            mMappings.put(mMappings.keyAt(i), String.valueOf(columnData));
        }

//        Log.d(TAG, "address at index " + column + ": " + getColumnHeaderData(column) + ", new row data: " + String.valueOf(rowData));
    }

    private String revertSort(String mappings) {
        StringBuilder colData = new StringBuilder(mappings);
        char[] mappingsArr = mappings.toCharArray();
        int blockLength = mappings.length()/3;
        for (int i = 0; i < blockLength; i++) {
            // first we iterate over the block length...
            for (int j = 0; j < 3; j++) {
                // ... then we take the element at i + j * blocklength = same number in next color
                colData.append(mappingsArr[i+(j*blockLength)]);
            }
        }
        return String.valueOf(mappingsArr);
    }

    public void updateMappings(long addrID, String mappings) {
//        Log.d(TAG, "updateMappings, addrID: " + addrID + ", mappings: " + mappings);
        if (mSortMode.equals(CommandMappingsSortModes.SORT_BY_NUM)) {
            mappings = revertSort(mappings);
        }

        ContentValues values = new ContentValues();
        values.put(
                SettingsContract.AddressCommandsMappings.ADDRESS,
                addrID
        );
        values.put(
                SettingsContract.AddressCommandsMappings.MAPPINGS,
                mappings
        );
        long result;
        if (mMappings.size() == 0 || !checkIfEntryExists(addrID)) {
            result = mDb.insert(
                    SettingsContract.AddressCommandsMappings.TABLE_NAME,
                    null,
                    values
            );
//            Log.d(TAG, "insert result: " + result);
        } else {
            result = mDb.update(
                    SettingsContract.AddressCommandsMappings.TABLE_NAME,
                    values,
                    SettingsContract.AddressCommandsMappings.ADDRESS + " = " + addrID,
                    null
            );
        }
    }

    private boolean checkIfEntryExists(long addrId) {
        String query = "Select * from " + SettingsContract.AddressCommandsMappings.TABLE_NAME + " where " + SettingsContract.AddressCommandsMappings.ADDRESS + " = " + addrId + ";";
        Cursor cursor = mDb.rawQuery(query, null);
        boolean ret = cursor.getCount() > 0;
        cursor.close();

        return ret;
    }

}
