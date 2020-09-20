package net.videosc.interfaces;

public class TableDataSourceImpl implements TableDataSource<String, String, String, String> {

    private int mColumnsCount;
    private int mRowsCount;

    @Override
    public int getRowsCount() {
        return mRowsCount;
    }

    @Override
    public int getColumnsCount() {
        return mColumnsCount;
    }

    @Override
    public String getFirstHeaderData() {
        return getItemData(0, 0);
    }

    @Override
    public String getRowHeaderData(int index) {
        return getItemData(index, 0);
    }

    @Override
    public String getColumnHeaderData(int index) {
        return getItemData(0, index);
    }

    @Override
    public String getItemData(int rowIndex, int columnIndex) {
        // TODO
        return null;
    }

    /* private void init() {
        VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
        assert mActivity != null;
        mApplication = (VideOSCApplication) mActivity.getApplication();
        mDb = mActivity.getDatabase();

        final long addrCount = DatabaseUtils.queryNumEntries(mDb, SettingsContract.AddressSettingsEntries.TABLE_NAME);
        final Point resolution = mApplication.getResolution();
        final int commandsCount = resolution.x * resolution.y;

        if (addrCount > 1) {
            final String[] addressesFields = new String[]{
                    SettingsContract.AddressSettingsEntries._ID,
                    SettingsContract.AddressSettingsEntries.IP_ADDRESS,
                    SettingsContract.AddressSettingsEntries.PORT,
                    SettingsContract.AddressSettingsEntries.PROTOCOL
            };

            Cursor cursor = mDb.query(
                    SettingsContract.AddressSettingsEntries.TABLE_NAME,
                    addressesFields,
                    null,
                    null,
                    null,
                    null,
                    SettingsContract.AddressSettingsEntries._ID
            );

            while (cursor.moveToNext()) {
                final long rowID = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries._ID));
                final String ip = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.IP_ADDRESS));
                final int port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PORT));
                final String protocol = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PROTOCOL));
                mAddresses.add(ip + ":" + port + "\n(protocol: " + protocol + ")");
            }

            Log.d(TAG, "addresses: " + mAddresses);

            final String[] settingsCmdName = new String[] {
                    SettingsContract.SettingsEntries._ID,
                    SettingsContract.SettingsEntries.ROOT_CMD,
            };

            cursor = mDb.query(
                    SettingsContract.SettingsEntries.TABLE_NAME,
                    settingsCmdName,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();
            final String rootCmdName = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD));

            cursor.close();

            final String[] colors = new String[] {"/red", "/green", "/blue"};

            for (String color : colors) {
                for (int i = 0; i < commandsCount; i++) {
                    mCommands.add("/" + rootCmdName + color + (1 + i % commandsCount));
                }
            }
        }

    } */
}
