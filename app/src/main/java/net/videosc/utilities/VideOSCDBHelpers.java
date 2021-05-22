package net.videosc.utilities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import net.netP5android.NetAddress;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.db.SettingsDBHelper;

import java.util.List;

public class VideOSCDBHelpers {
    private final static String TAG = VideOSCDBHelpers.class.getSimpleName();
    private final SettingsDBHelper mDbHelper;
    private final SQLiteDatabase mDb;
    private final VideOSCApplication mApp;

    public VideOSCDBHelpers(VideOSCMainActivity activity) {
        this.mApp = (VideOSCApplication) activity.getApplication();
        this.mDbHelper = new SettingsDBHelper(activity);
        this.mDb = mDbHelper.getReadableDatabase();
    }

    public SQLiteDatabase getDatabase() {
        return this.mDb;
    }

    public void close() {
        mDb.close();
        mDbHelper.close();
    }

    public int countAddresses() {
        return (int) DatabaseUtils.queryNumEntries(mDb, SettingsContract.AddressSettingsEntries.TABLE_NAME);
    }

    public int countSliderGroups() {
        return (int) DatabaseUtils.queryNumEntries(mDb, SettingsContract.SliderGroups.TABLE_NAME);
    }

    public SparseArray<String> getAddresses() {
        final SparseArray<String> addresses = new SparseArray<>();

        final String[] addrFields = new String[]{
                SettingsContract.AddressSettingsEntries._ID,
                SettingsContract.AddressSettingsEntries.IP_ADDRESS,
                SettingsContract.AddressSettingsEntries.PORT
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
            addresses.put((int) addrID, ip + ":" + port);
        }

        cursor.close();

        return addresses;
    }

    public SparseArray<String> getMappings() {
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
            final String mappingsString = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressCommandsMappings.MAPPINGS));
            mappings.put((int) addrID, mappingsString);
        }

        cursor.close();

        return mappings;
    }

    public void getBroadcastClients() {
        final String[] settingsFields = new String[]{
                SettingsContract.AddressSettingsEntries._ID,
                SettingsContract.AddressSettingsEntries.IP_ADDRESS,
                SettingsContract.AddressSettingsEntries.PORT
        };

        final Cursor cursor = mDb.query(
                SettingsContract.AddressSettingsEntries.TABLE_NAME,
                settingsFields,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            final int key = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries._ID));
            final NetAddress client = new NetAddress(
                    cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.IP_ADDRESS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PORT))
            );
            mApp.putBroadcastClient(key, client);
            mApp.putBroadcastClientKeys(client.toString(), key);
        }

        cursor.close();
    }

    public int getUdpReceivePort() {
        int port = 0;

        String[] settingsFields = new String[]{
                SettingsContract.SettingsEntries.UDP_RECEIVE_PORT
        };

        Cursor cursor = mDb.query(
                SettingsContract.SettingsEntries.TABLE_NAME,
                settingsFields,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.UDP_RECEIVE_PORT));
        }

        cursor.close();

        return port;
    }

    public int getTcpReceivePort() {
        int port = 0;

        String[] settingsFields = new String[]{
                SettingsContract.SettingsEntries.TCP_RECEIVE_PORT
        };

        Cursor cursor = mDb.query(
                SettingsContract.SettingsEntries.TABLE_NAME,
                settingsFields,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.TCP_RECEIVE_PORT));
        }

        cursor.close();

        return port;
    }

    public MatrixCursor getSnapshotsMatrixCursor() {
        final MatrixCursor extras = new MatrixCursor(new String[]{
                SettingsContract.PixelSnapshotEntries._ID,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_RED_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_RED_MIX_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_GREEN_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_GREEN_MIX_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_BLUE_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_BLUE_MIX_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_NAME,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_SIZE
        });
        extras.addRow(new String[]{"-1", null, null, null, null, null, null, "export snapshots set...", null});
        extras.addRow(new String[]{"-2", null, null, null, null, null, null, "load snapshots set...", null});

        return extras;
    }

    public Cursor getSnapshotsCursor() {
        final String[] settingsFields = new String[]{
                SettingsContract.PixelSnapshotEntries._ID,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_RED_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_RED_MIX_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_GREEN_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_GREEN_MIX_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_BLUE_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_BLUE_MIX_VALUES,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_NAME,
                SettingsContract.PixelSnapshotEntries.SNAPSHOT_SIZE
        };

        return mDb.query(
                SettingsContract.PixelSnapshotEntries.TABLE_NAME,
                settingsFields,
                null, null, null, null, SettingsContract.PixelSnapshotEntries._ID + " DESC"
        );
    }

    public Cursor queryAddresses(String[] fields) {
        String sortOrder = SettingsContract.AddressSettingsEntries._ID + " DESC";
        return mDb.query(
                SettingsContract.AddressSettingsEntries.TABLE_NAME,
                fields,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    public Cursor queryNetworkSettings() {
        final String[] settingsFields = new String[]{
                SettingsContract.SettingsEntries._ID,
                SettingsContract.SettingsEntries.UDP_RECEIVE_PORT,
                SettingsContract.SettingsEntries.TCP_RECEIVE_PORT,
                SettingsContract.SettingsEntries.ROOT_CMD,
                SettingsContract.SettingsEntries.TCP_PASSWORD
        };

        return mDb.query(
                SettingsContract.SettingsEntries.TABLE_NAME,
                settingsFields,
                null,
                null,
                null,
                null,
                null
        );
    }

    public Cursor queryResolutionSettings() {
        final String[] settingsFields = new String[]{
                SettingsContract.SettingsEntries._ID,
                SettingsContract.SettingsEntries.RES_H,
                SettingsContract.SettingsEntries.RES_V,
                SettingsContract.SettingsEntries.FRAMERATE_RANGE,
                SettingsContract.SettingsEntries.NORMALIZE,
                SettingsContract.SettingsEntries.REMEMBER_PIXEL_STATES
        };

        return mDb.query(
                SettingsContract.SettingsEntries.TABLE_NAME,
                settingsFields,
                null,
                null,
                null,
                null,
                null
        );
    }

    public String getRootCmd() {
        String rootCmd = "vosc";

        String[] settingsFields = new String[]{
                SettingsContract.SettingsEntries.ROOT_CMD
        };

        Cursor cursor = mDb.query(
                SettingsContract.SettingsEntries.TABLE_NAME,
                settingsFields,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst())
            rootCmd = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD));

        cursor.close();

        return rootCmd;
    }

    public void addSliderGroup(String groupName, List<SparseArray<String>> group) {
        final ContentValues values = new ContentValues();
        values.put(SettingsContract.SliderGroups.GROUP_NAME, groupName);
        long result = mDb.insert(
                SettingsContract.SliderGroups.TABLE_NAME,
                null,
                values
        );
        if (result > -1) {
            values.clear();
            int order = 0;
            // group size will always be 3:
            // slot 0: red channel
            // slot 1: green channel
            // slot 2: blue channel
            for (int i = 0; i < group.size(); i++) {
                SparseArray<String> colChan = group.get(i);
                for (int j = 0; j < colChan.size(); j++) {
                    values.put(SettingsContract.SliderGroupProperties.COLOR_CHANNEL, i);
                    values.put(SettingsContract.SliderGroupProperties.GROUP_ID, result);
                    values.put(SettingsContract.SliderGroupProperties.LABEL_TEXT, colChan.valueAt(j));
                    values.put(SettingsContract.SliderGroupProperties.PIXEL_ID, colChan.keyAt(j));
                    values.put(SettingsContract.SliderGroupProperties.SLIDER_ORDER, order);
                    long propsInsertResult = mDb.insertOrThrow(
                            SettingsContract.SliderGroupProperties.TABLE_NAME,
                            null,
                            values
                    );
                    values.clear();
                    // something went wrong - undo everything
                    if (propsInsertResult < 0) {
                        mDb.delete(
                                SettingsContract.SliderGroups.TABLE_NAME,
                                SettingsContract.SliderGroups._ID + " = " + result,
                                null
                        );
                        mDb.delete(
                                SettingsContract.SliderGroupProperties.TABLE_NAME,
                                SettingsContract.SliderGroupProperties.GROUP_ID + " = " + result,
                                null
                        );
                        break;
                    }
                    order++;
                }
            }
        }
    }
}
