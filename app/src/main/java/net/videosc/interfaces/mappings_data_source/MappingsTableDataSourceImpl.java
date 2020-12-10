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
    private final SparseArray<String> mMappings;
    private final SparseArray<String> mAddresses;
    private final ArrayList<String> mCommands;
    private final CommandMappingsSortModes mSortMode;

    public MappingsTableDataSourceImpl(VideOSCMainActivity activity) {
        this.mActivity = activity;
        this.mDb = activity.getDatabase();
        this.mApp = (VideOSCApplication) activity.getApplication();
        this.mSortMode = mApp.getCommandMappingsSortMode();
        this.mMappings = getMappings();
        Log.d(TAG, "mappings: " + mMappings);
        this.mAddresses = getAddresses();
        this.mCommands = getCommands(this.mSortMode);
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
        Log.d(TAG, "address: " + mAddresses.valueAt(index));
        return mAddresses.valueAt(index);
    }

    @Override
    public Character getItemData(int rowIndex, int columnIndex) {
        Log.d(TAG, "getItemData, row: " + rowIndex + ", column: " + columnIndex);
        char itemData;
        if (mMappings.size() > 0) {
            if (mMappings.valueAt(columnIndex).isEmpty()) {
                itemData = '1';
            } else {
                Log.d(TAG, "mapping in row " + rowIndex + ", column " + columnIndex + " is " + mMappings.valueAt(columnIndex).charAt(rowIndex));
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

    private SparseArray<String> getMappings() {
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
            // FIXME: if sort order is SORT_BY_NUM mappings must be reordered before creating table layout
            if (mApp.getCommandMappingsSortMode().equals(CommandMappingsSortModes.SORT_BY_NUM)) {
                char[] mappingsArr = mappingsString.toCharArray();
                // mappings for each color are store in blocks of mappingsArr.length/3
                // first block: red, second: green, third: blue
                int blockLength = mappingsArr.length/3;
                StringBuilder sortedMappings = new StringBuilder();
                for (int i = 0; i < blockLength; i++) {
                    // first we iterate over the block length...
                    for (int j = 0; j < 3; j++) {
                        // ... then we take the element at i + j * blocklength = same number in next color
                        sortedMappings.append(mappingsArr[i+(j*blockLength)]);
                    }
                }
                mappingsString = String.valueOf(sortedMappings);
            }
            mappings.put((int) addrID, mappingsString);
        }

        cursor.close();

//        Log.d(TAG, "mappings: " + mappings);
        return mappings;
    }

    // if a row in VideOSC is sending to all clients
    // a click should select a single cell in a row
    public String setFullRowData(int row, int column) {
        final int numCols = getColumnsCount();
//        Log.d(TAG, "setFullRowData: " + row + "/" + column + ", num columns: " + numCols);
        StringBuilder rowData = new StringBuilder();
        for (int i = 0; i < numCols; i++) {
            char newMapping = '1';
            if (column != i) {
                newMapping = '0';
            }
            rowData.append(newMapping);
            StringBuilder columnData = new StringBuilder(getColumnData(i));
            if (mSortMode.equals(CommandMappingsSortModes.SORT_BY_NUM)) {
                // commands are stored in blocks: first red, second green, then blue
                // each block has a size of numRows/3
                // index is the index within a block
                // for color green add colorBlockSize once
                // for color blue add colorBlocksize twice
                int index = row / 3;
                final int colorBlockSize = getRowsCount() / 3;
                // only need to correct index for colors green and blue
                if (row % 3 == 2) { // green
                    index += colorBlockSize;
                } else if (row % 3 == 0) { // blue
                    index += (colorBlockSize * 2);
                }
                columnData.setCharAt(index, newMapping);
            } else {
                columnData.setCharAt(row, newMapping);
            }
            updateMappings(mAddresses.keyAt(i), String.valueOf(columnData));
        }
        return String.valueOf(rowData);
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
    public String setItemData(int row, int column) {
        // FIXME: do we need rowData at all here?
        StringBuilder rowData = new StringBuilder(getRowData(row));
        StringBuilder columnData = new StringBuilder(getColumnData(column));
        Character itemData = getItemData(row, column);
        int newVal = itemData == '0' ? '1' : '0';
        rowData.setCharAt(column, (char) newVal);
        columnData.setCharAt(row, (char) newVal);
        updateMappings(mAddresses.keyAt(column), String.valueOf(columnData));
        Log.d(TAG, "address at index " + column + ": " + getColumnHeaderData(column) + ", new row data: " + String.valueOf(rowData));

        return String.valueOf(rowData);
    }

    public void updateMappings(long addrID, String mappings) {
        Log.d(TAG, "updateMappings, addrID: " + addrID + ", mappings: " + mappings);
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
        if (mMappings.size() == 0) {
            result = mDb.insert(
                    SettingsContract.AddressCommandsMappings.TABLE_NAME,
                    null,
                    values
            );
            Log.d(TAG, "insert result: " + result);
        } else {
            result = mDb.update(
                    SettingsContract.AddressCommandsMappings.TABLE_NAME,
                    values,
                    SettingsContract.AddressCommandsMappings.ADDRESS + " = " + addrID,
                    null
            );
        }
    }

}
