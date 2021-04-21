package net.videosc.utilities;

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

public class VideOSCDBHelpers {
    private final static String TAG = VideOSCDBHelpers.class.getSimpleName();
    private final SettingsDBHelper mDbHelper;
    private final SQLiteDatabase mDb;
    private final VideOSCApplication mApp;

    public VideOSCDBHelpers(VideOSCMainActivity activity) {
        this.mApp = (VideOSCApplication) activity.getApplication();
        this.mDbHelper = new SettingsDBHelper(activity);
//        this.mDb = activity.getDatabase();
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
    
    public void setBroadcastClients() {
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
}
