package net.videosc.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;

public class VideOSCDBHelpers {
    private final SQLiteDatabase mDb;

    public VideOSCDBHelpers(VideOSCMainActivity activity) {
        this.mDb = activity.getDatabase();
    }

    public int countAddresses() {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + SettingsContract.AddressSettingsEntries.TABLE_NAME + ";", null);
        int count = cursor.getCount();

        cursor.close();

        return count;
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
            String mappingsString = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressCommandsMappings.MAPPINGS));
            mappings.put((int) addrID, mappingsString);
        }

        cursor.close();

        return mappings;
    }
}
