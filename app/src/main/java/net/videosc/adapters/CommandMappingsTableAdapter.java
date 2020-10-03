package net.videosc.adapters;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cleveroad.adaptivetablelayout.LinkedAdaptiveTableAdapter;
import com.cleveroad.adaptivetablelayout.ViewHolderImpl;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.interfaces.mappings_data_source.MappingsTableDataSource;

import java.util.Iterator;

public class CommandMappingsTableAdapter extends LinkedAdaptiveTableAdapter<ViewHolderImpl> {
    private static final String TAG = CommandMappingsTableAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final int mColumnWidth;
    private final int mRowHeight;
    private final int mHeaderHeight;
    private final int mHeaderWidth;
    private final SQLiteDatabase mDb;
    private final VideOSCApplication mApp;
    private final Point mResolution;
    private final MappingsTableDataSource<String, String, Character> mTableDataSource;
    private String mAddress;
    private String mCommand;
    private String mMapping;
    private Iterator<String> mAddrIterator;

    public CommandMappingsTableAdapter(VideOSCMainActivity activity, MappingsTableDataSource<String, String, Character> tableDataSource) {
        this.mLayoutInflater = LayoutInflater.from(activity);
        this.mApp = (VideOSCApplication) activity.getApplication();
        this.mResolution = mApp.getResolution();
        Resources res = activity.getResources();
        this.mColumnWidth = res.getDimensionPixelSize(R.dimen.col_width);
        this.mRowHeight = res.getDimensionPixelSize(R.dimen.row_height);
        this.mHeaderHeight = res.getDimensionPixelSize(R.dimen.col_header_height);
        this.mHeaderWidth = res.getDimensionPixelSize(R.dimen.row_header_width);
        this.mTableDataSource = tableDataSource;
        this.mDb = activity.getDatabase();
    }

    @Override
    public int getRowCount() {
        return mTableDataSource.getRowsCount();
    }

    @Override
    public int getColumnCount() {
        Log.d(TAG, "num columns: " + mTableDataSource.getColumnsCount());
        return mTableDataSource.getColumnsCount();
    }

    @NonNull
    @Override
    public ViewHolderImpl onCreateItemViewHolder(@NonNull ViewGroup parent) {
        return new TableViewHolder(mLayoutInflater.inflate(R.layout.table_item_card, parent, false));
    }

    @NonNull
    @Override
    public ViewHolderImpl onCreateColumnHeaderViewHolder(@NonNull ViewGroup parent) {
        return new TableHeaderColumnViewHolder(mLayoutInflater.inflate(R.layout.table_item_header_column, parent, false));
    }

    @NonNull
    @Override
    public ViewHolderImpl onCreateRowHeaderViewHolder(@NonNull ViewGroup parent) {
        return new TableHeaderRowViewHolder(mLayoutInflater.inflate(R.layout.table_item_header_row, parent, false));
    }

    @NonNull
    @Override
    public ViewHolderImpl onCreateLeftTopHeaderViewHolder(@NonNull ViewGroup parent) {
        return new TableLeftTopViewHolder(mLayoutInflater.inflate(R.layout.table_item_header_left_top, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderImpl viewHolder, int row, int column) {
//        Log.d(TAG, "onBindViewHolder: row: " + row + ", column: " + column);
        String itemData;
        int bgColor, textColor;
        final TableViewHolder vh = (TableViewHolder) viewHolder;
        int rawMapping = Integer.parseInt(String.valueOf(mTableDataSource.getItemData(row-1, column-1)));
        if (rawMapping > 0) {
            itemData = "ON";
            bgColor = 0xffffffff;
            textColor = 0xff000000;
        } else {
            itemData = "OFF";
            bgColor = 0xff000000;
            textColor = 0xffffffff;
        }
        vh.cellText.setVisibility(View.VISIBLE);
        vh.cellText.setBackgroundColor(bgColor);
        vh.cellText.setTextColor(textColor);
        vh.cellText.setText(itemData);
    }

    @Override
    public void onBindHeaderColumnViewHolder(@NonNull ViewHolderImpl viewHolder, int column) {
        Log.d(TAG, "onBindHeaderColumnViewHolder, column: " + column);
        final TableHeaderColumnViewHolder vh = (TableHeaderColumnViewHolder) viewHolder;
        String itemData = mTableDataSource.getColumnHeaderData(column-1);
        vh.cellText.setText(itemData);
    }

    @Override
    public void onBindHeaderRowViewHolder(@NonNull ViewHolderImpl viewHolder, int row) {
        final TableHeaderRowViewHolder vh = (TableHeaderRowViewHolder) viewHolder;
        final String itemData = mTableDataSource.getRowHeaderData(row-1);
        vh.cellText.setText(itemData);
    }

    @Override
    public void onBindLeftTopHeaderViewHolder(@NonNull ViewHolderImpl viewHolder) {
        final TableLeftTopViewHolder vh = (TableLeftTopViewHolder) viewHolder;
        final String itemData = "scroll on/off";
        vh.cellText.setText(itemData);
    }

    @Override
    public int getColumnWidth(int column) {
        return mColumnWidth;
    }

    @Override
    public int getHeaderColumnHeight() {
        return mHeaderHeight;
    }

    @Override
    public int getRowHeight(int row) {
        return mRowHeight;
    }

    @Override
    public int getHeaderRowWidth() {
        return mHeaderWidth;
    }

//    private void initTableData(VideOSCMainActivity activity) {
//        final VideOSCApplication app = (VideOSCApplication) activity.getApplication();
//        final int width = app.getResolution().x;
//        final int height = app.getResolution().y;
////        mAddress = mTableDataSource.get();
////        mCommand = getCommands(width, height);
////        mMapping = getMappings();
//    }

 /*   private HashMap<Long, String> getAddresses() {
        final HashMap<Long, String> addresses = new HashMap<>();

        final String[] addrFields = new String[] {
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
            addresses.put(addrID, ip + ":" + port + "\n(protocol: " + protocol + ")");
        }

        cursor.close();

        return addresses;
    }

    private ArrayList<String> getCommands(int width, int height) {
        final ArrayList<String> commands = new ArrayList<>();
        final String[] colors = new String[] {"red", "green", "blue"};
        final int size = width * height;
        String rootCmd = "";

        final String[] rootCmdFields = new String[] {
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

        for (String color : colors) {
            for (int i = 0; i < size;) {
                commands.add("/" + rootCmd + "/" + color + (++i));
            }
        }

        cursor.close();

        return commands;
    } */

   /* private HashMap<Long, String> getMappings() {
        final HashMap<Long, String> mappings = new HashMap<>();

        String[] mappingsFields = new String[] {
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
            mappings.put(addrID, mappingsString);
        }

        cursor.close();

        return mappings;
    }*/

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

    /* ---------------- view holders --------------------------- */
    private static class TableViewHolder extends ViewHolderImpl {
        TextView cellText;

        private TableViewHolder(@NonNull View itemView) {
            super(itemView);
            cellText = itemView.findViewById(R.id.cell_text);
//            Log.d(TAG, "new TableViewHolder, cellText: " + cellText);
        }
    }

    private static class TableHeaderColumnViewHolder extends ViewHolderImpl {
        TextView cellText;

        private TableHeaderColumnViewHolder(@NonNull View itemView) {
            super(itemView);
            cellText = itemView.findViewById(R.id.cell_text);
            Log.d(TAG, "new TableHeaderColumnViewHolder, celltext: " + cellText);
        }
    }

    private static class TableHeaderRowViewHolder extends ViewHolderImpl {
        TextView cellText;

        private TableHeaderRowViewHolder(@NonNull View itemView) {
            super(itemView);
            cellText = itemView.findViewById(R.id.cell_text);
//            Log.d(TAG, "new TableHeaderRowViewHolder, cellText: " + cellText);
        }
    }

    private  static class TableLeftTopViewHolder extends ViewHolderImpl {
        TextView cellText;

        private TableLeftTopViewHolder(@NonNull View itemView) {
            super(itemView);
            cellText = itemView.findViewById(R.id.cell_text);
//            Log.d(TAG, "new TableLeftTopViewHolder, cellText: " + cellText);
        }
    }
}
