package net.videosc.adapters;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
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
import net.videosc.utilities.enums.CommandMappingsSortModes;

public class CommandMappingsTableAdapter extends LinkedAdaptiveTableAdapter<ViewHolderImpl> {
    private static final String TAG = CommandMappingsTableAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final int mColumnWidth;
    private final int mRowHeight;
    private final int mHeaderHeight;
    private final int mHeaderWidth;
    private final SQLiteDatabase mDb;
    private final MappingsTableDataSource<String, String, Character> mTableDataSource;
    private final CommandMappingsSortModes mSortMode;
    private final int mNumRows;

    public CommandMappingsTableAdapter(VideOSCMainActivity activity, CommandMappingsSortModes sortMode, MappingsTableDataSource<String, String, Character> tableDataSource) {
        this.mLayoutInflater = LayoutInflater.from(activity);
        Resources res = activity.getResources();
        this.mColumnWidth = res.getDimensionPixelSize(R.dimen.col_width);
        this.mRowHeight = res.getDimensionPixelSize(R.dimen.row_height);
        this.mHeaderHeight = res.getDimensionPixelSize(R.dimen.col_header_height);
        this.mHeaderWidth = res.getDimensionPixelSize(R.dimen.row_header_width);
        this.mTableDataSource = tableDataSource;
        this.mDb = activity.getDatabase();
        VideOSCApplication app = (VideOSCApplication) activity.getApplication();
        this.mSortMode = app.getCommandMappingsSortMode();
        this.mNumRows = getRowCount();
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
        String itemData;
        int bgColor, textColor;
        final TableViewHolder vh = (TableViewHolder) viewHolder;
        int rawMapping = Integer.parseInt(String.valueOf(mTableDataSource.getItemData(row-1, column-1)));
        if (rawMapping > 0) {
            itemData = "ON";
//            TODO: color me rgb!
            if (mSortMode.equals(CommandMappingsSortModes.SORT_BY_COLOR)) {
                if (row <= mNumRows / 3) {
                    bgColor = 0xffff0000;
                } else if (row > mNumRows / 3 && row <= mNumRows / 3 * 2) {
                    bgColor = 0xff00ff00;
                } else if (row > mNumRows / 3 * 2) {
                    bgColor = 0xff0000ff;
                } else {
                    bgColor = 0xffffffff;
                }
            } else if (mSortMode.equals(CommandMappingsSortModes.SORT_BY_NUM)) {
                if ((row-1)%3 == 0) {
                    bgColor = 0xffff0000;
                } else if ((row-1)%3 == 1) {
                    bgColor = 0xff00ff00;
                } else if ((row-1)%3 == 2) {
                    bgColor = 0xff0000ff;
                } else {
                    bgColor = 0xffffffff;
                }
            } else {
                bgColor = 0xffffffff;
            }
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
        switch (mSortMode) {
            case SORT_BY_NUM:
                vh.cellText.setText(R.string.sort_by_color);
                break;
            case SORT_BY_COLOR:
                vh.cellText.setText(R.string.sort_by_cmd_num);
                break;
        }
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
        }
    }
}
