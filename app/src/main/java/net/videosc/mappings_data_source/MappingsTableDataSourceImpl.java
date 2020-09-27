package net.videosc.mappings_data_source;

import android.database.sqlite.SQLiteDatabase;

import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;

public class MappingsTableDataSourceImpl implements MappingsTableDataSource<String, String, String> {
    final private SQLiteDatabase mDb;
    final private VideOSCApplication mApp;

    public MappingsTableDataSourceImpl(VideOSCMainActivity activity) {
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
    public String getRowHeaderDataType(int index) {
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
}
