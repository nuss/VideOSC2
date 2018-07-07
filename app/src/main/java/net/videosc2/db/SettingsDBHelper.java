package net.videosc2.db;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.videosc2.fragments.VideOSCCameraFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stefan on 22.06.17, package net.videosc2.db, project VideOSC22.
 */
public class SettingsDBHelper extends SQLiteOpenHelper {
	private static final String TAG = "SettingsDBHelper";

	private static final String SQL_ADDRESSES_CREATE_ENTRIES =
			"CREATE TABLE " + SettingsContract.AddressSettingsEntry.TABLE_NAME + " (" +
					SettingsContract.AddressSettingsEntry._ID + " INTEGER PRIMARY KEY," +
					SettingsContract.AddressSettingsEntry.IP_ADDRESS + " TEXT NOT NULL DEFAULT '192.168.1.1'," +
					SettingsContract.AddressSettingsEntry.PORT + " INTEGER NOT NULL DEFAULT '57120'," +
					SettingsContract.AddressSettingsEntry.PROTOCOL + " TEXT NOT NULL DEFAULT 'UDP')";

	private static final String SQL_ADDRESSES_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + SettingsContract.AddressSettingsEntry.TABLE_NAME;

	private static final String SQL_SETTINGS_CREATE_ENTRIES =
			"CREATE TABLE " + SettingsContract.SettingsEntries.TABLE_NAME + " (" +
					SettingsContract.SettingsEntries._ID + " INTEGER PRIMARY KEY," +
					SettingsContract.SettingsEntries.RES_H + " INTEGER NOT NULL DEFAULT '6'," +
					SettingsContract.SettingsEntries.RES_V + " INTEGER NOT NULL DEFAULT '4'," +
					SettingsContract.SettingsEntries.FRAMERATE_RANGE + " INTEGER NOT NULL DEFAULT '1'," +
					SettingsContract.SettingsEntries.NORMALIZE + " INTEGER NOT NULL DEFAULT '0'," +
					SettingsContract.SettingsEntries.REMEMBER_PIXEL_STATES + " INTEGER NOT NULL DEFAULT '0'," +
					SettingsContract.SettingsEntries.CALC_PERIOD + " INTEGER NOT NULL DEFAULT '1'," +
					SettingsContract.SettingsEntries.ROOT_CMD + " TEXT NOT NULL DEFAULT 'vosc'," +
					SettingsContract.SettingsEntries.UDP_RECEIVE_PORT + " INTEGER NOT NULL DEFAULT '32000'," +
					SettingsContract.SettingsEntries.TCP_RECEIVE_PORT + " INTEGER NOT NULL DEFAULT '32001')";

	private static final String SQL_SETTINGS_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + SettingsContract.SettingsEntries.TABLE_NAME;

	private static final String SQL_SENSOR_SETTINGS_CREATE =
			"CREATE TABLE " + SettingsContract.SensorSettingsEntries.TABLE_NAME + " (" +
					SettingsContract.SensorSettingsEntries._ID + " INTEGER PRIMARY KEY," +
					SettingsContract.SensorSettingsEntries.SENSOR + " TEXT," +
					SettingsContract.SensorSettingsEntries.VALUE + " INTEGER)";

	private static final String SQL_SENSOR_SETTINGS_DELETE =
			"DROP TABLE IF EXISTS " + SettingsContract.SensorSettingsEntries.TABLE_NAME;

	private static final String SQL_PIXEL_SNAPSHOTS_CREATE =
			"CREATE TABLE " + SettingsContract.PixelSnapshotEntries.TABLE_NAME + " (" +
					SettingsContract.PixelSnapshotEntries._ID + " INTEGER PRIMARY KEY," +
					SettingsContract.PixelSnapshotEntries.SNAPSHOT_NAME + " TEXT NOT NULL," +
					SettingsContract.PixelSnapshotEntries.SNAPSHOT_RED_VALUES + " TEXT NOT NULL, " +
					SettingsContract.PixelSnapshotEntries.SNAPSHOT_RED_MIX_VALUES + " TEXT NOT NULL, " +
					SettingsContract.PixelSnapshotEntries.SNAPSHOT_GREEN_VALUES + " TEXT NOT NULL, " +
					SettingsContract.PixelSnapshotEntries.SNAPSHOT_GREEN_MIX_VALUES + " TEXT NOT NULL, " +
					SettingsContract.PixelSnapshotEntries.SNAPSHOT_BLUE_VALUES + " TEXT NOT NULL, " +
					SettingsContract.PixelSnapshotEntries.SNAPSHOT_BLUE_MIX_VALUES + " TEXT NOT NULL, " +
					SettingsContract.PixelSnapshotEntries.SNAPSHOT_SIZE + " INTEGER NOT NULL)";

	private static final  String SQL_PIXEL_SNAPSHOTS_DELETE =
			"DROP TABLE IF EXISTS " + SettingsContract.PixelSnapshotEntries.TABLE_NAME;

	// If you change the database schema, you must increment the database version.
	private static final int DATABASE_VERSION = 23;
	private static final String DATABASE_NAME = "VOSCSettings.db";

	public SettingsDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	/**
	 * Called when the database is created for the first time. This is where the
	 * creation of tables and the initial population of the tables should happen.
	 *
	 * @param db The database.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		long newRowId;

		Log.d(TAG, "onCreate");
		// create table for addresses of remote clients
		db.execSQL(SQL_ADDRESSES_CREATE_ENTRIES);

		ContentValues values = new ContentValues();

		// net address(es)
		values.put(SettingsContract.AddressSettingsEntry.IP_ADDRESS, "192.168.1.1");
		values.put(SettingsContract.AddressSettingsEntry.PORT, 57120);
		values.put(SettingsContract.AddressSettingsEntry.PROTOCOL, "UDP");
		newRowId = db.insert(SettingsContract.AddressSettingsEntry.TABLE_NAME, null, values);
		Log.d(TAG, "new row ID: " + newRowId);

		// reset
		values.clear();

		// create table for single value settings
		db.execSQL(SQL_SETTINGS_CREATE_ENTRIES);

		// single value settings
		values.put(SettingsContract.SettingsEntries.RES_H, 7);
		values.put(SettingsContract.SettingsEntries.RES_V, 5);
		values.put(SettingsContract.SettingsEntries.FRAMERATE_RANGE, 0);
		values.put(SettingsContract.SettingsEntries.NORMALIZE, 0);
		values.put(SettingsContract.SettingsEntries.REMEMBER_PIXEL_STATES, 0);
		values.put(SettingsContract.SettingsEntries.CALC_PERIOD, 1);
		values.put(SettingsContract.SettingsEntries.ROOT_CMD, "vosc");
		values.put(SettingsContract.SettingsEntries.UDP_RECEIVE_PORT, 32000);
		values.put(SettingsContract.SettingsEntries.TCP_RECEIVE_PORT, 32001);
		newRowId = db.insert(SettingsContract.SettingsEntries.TABLE_NAME, null, values);
		Log.d(TAG, "new row ID: " + newRowId);

		values.clear();

		// create table for sensor settings
		db.execSQL(SQL_SENSOR_SETTINGS_CREATE);

		Map<String, Integer> initSensors = new HashMap<>();
		initSensors.put("ori", 0);
		initSensors.put("acc", 0);
		initSensors.put("lin_acc", 0);
		initSensors.put("mag", 0);
		initSensors.put("grav", 0);
		initSensors.put("prox", 0);
		initSensors.put("light", 0);
		initSensors.put("press", 0);
		initSensors.put("temp", 0);
		initSensors.put("hum", 0);
		initSensors.put("loc", 0);

		for (String key : initSensors.keySet()) {
			values.put(SettingsContract.SensorSettingsEntries.SENSOR, key);
			values.put(SettingsContract.SensorSettingsEntries.VALUE, initSensors.get(key));
			newRowId = db.insert(SettingsContract.SensorSettingsEntries.TABLE_NAME, null, values);
			Log.d(TAG, "new row ID: " + newRowId);
			values.clear();
		}

		// create snapshots table
		db.execSQL(SQL_PIXEL_SNAPSHOTS_CREATE);
	}

	/**
	 * Called when the database needs to be upgraded. The implementation
	 * should use this method to drop tables, add tables, or do anything else it
	 * needs to upgrade to the new schema version.
	 * <p>
	 * <p>
	 * The SQLite ALTER TABLE documentation can be found
	 * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
	 * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
	 * you can use ALTER TABLE to rename the old table, then create the new table and then
	 * populate the new table with the contents of the old table.
	 * </p><p>
	 * This method executes within a transaction.  If an exception is thrown, all changes
	 * will automatically be rolled back.
	 * </p>
	 *
	 * @param db         The database.
	 * @param oldVersion The old database version.
	 * @param newVersion The new database version.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "on upgrade");
		db.execSQL(SQL_ADDRESSES_DELETE_ENTRIES);
		db.execSQL(SQL_SETTINGS_DELETE_ENTRIES);
		db.execSQL(SQL_SENSOR_SETTINGS_DELETE);
		db.execSQL(SQL_PIXEL_SNAPSHOTS_DELETE);
		onCreate(db);
	}

	/**
	 * Called when the database needs to get downgraded
	 * @param db            The database
	 * @param oldVersion    The old database version
	 * @param newVersion    The new database version
	 */
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}
