package net.videosc.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;

public class VideOSCDBHelpers {
    private final VideOSCMainActivity mActivity;

    public VideOSCDBHelpers(VideOSCMainActivity activity) {
        this.mActivity = activity;
    }

    public int countAddresses() {
        SQLiteDatabase db = mActivity.getDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + SettingsContract.AddressSettingsEntries.TABLE_NAME + ";", null);
        int count = cursor.getCount();

        cursor.close();

        return count;
    }

}
