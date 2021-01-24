package net.videosc.fragments.settings;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cleveroad.adaptivetablelayout.AdaptiveTableLayout;
import com.cleveroad.adaptivetablelayout.OnItemClickListener;
import com.cleveroad.adaptivetablelayout.OnItemLongClickListener;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.adapters.CommandMappingsTableAdapter;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.interfaces.mappings_data_source.MappingsTableDataSourceImpl;
import net.videosc.utilities.MapHelper;

import java.util.Map;

public class VideOSCCommandMappingsFragment extends VideOSCBaseFragment {
    private final static String TAG = VideOSCCommandMappingsFragment.class.getSimpleName();

    private CommandMappingsTableAdapter mTableAdapter;
    private MappingsTableDataSourceImpl mTableDataSource;
    private int mNumAddresses;
    private Map<Integer, Integer> mRowChanges;
    private Map<Integer, Integer> mColumnChanges;

    public VideOSCCommandMappingsFragment() {
    }

    public VideOSCCommandMappingsFragment(Context context) {
        this.mActivity = (VideOSCMainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "VideOSCCommandMappingsFragment onCreate");
        super.onCreate(savedInstanceState);
        this.mTableDataSource = new MappingsTableDataSourceImpl(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        this.mNumAddresses = countAddresses();
        if (mNumAddresses > 1) {
            view = inflater.inflate(R.layout.address_command_mappings_table, container, false);
        } else {
            view = inflater.inflate(R.layout.no_addresses_defined, container, false);
        }
        return view;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mNumAddresses > 1) {
            final AdaptiveTableLayout tableLayout = view.findViewById(R.id.address_command_mappings_table);
            mTableAdapter = new CommandMappingsTableAdapter(mActivity, mTableDataSource);
            mTableAdapter.setOnItemClickListener(new OnItemClickListener() {
                private boolean firstClick = false;
                int firstRow, firstColumn;

                @Override
                public void onItemClick(int row, int column) {
                    // references to row/column order changes
                    mRowChanges = tableLayout.getLinkedAdapterRowsModifications();
                    mColumnChanges = tableLayout.getLinkedAdapterColumnsModifications();
                    final Integer rowCurrentPosition = MapHelper.getKeyByValue(mRowChanges, row);
                    final Integer columnCurrentPosition = MapHelper.getKeyByValue(mColumnChanges, column);
//                    Log.d(TAG, "row " + row + " now at position " + MapHelper.getKeyByValue(mRowChanges, row) + "\nrow changes: " + mRowChanges);
                    if (mTableDataSource.rowIsFull(row - 1)) {
                        firstClick = !firstClick;
                        if (firstClick) {
                            // cache start row
                            // consider changes in row order
                            firstRow = rowCurrentPosition == null ? row : rowCurrentPosition;
                            firstColumn = columnCurrentPosition == null ? column : columnCurrentPosition;
                            mTableDataSource.setFullRowData(row - 1, column - 1);
                        } else {
                            final int currentRow = rowCurrentPosition == null ? row : rowCurrentPosition;
                            final int currentColumn = columnCurrentPosition == null ? column : columnCurrentPosition;
                            Log.d(TAG, "setRange(" + firstRow + ", " + currentRow + ", " + firstColumn + ", " + currentColumn + ")");
                            setRange(firstRow, currentRow, firstColumn, currentColumn);
                        }
                    } else {
//                        Log.d(TAG, "row: " + row + ", column: " + column + ", row is not full, getItemData: " + mTableDataSource.getItemData(row-1, column-1));
                        if (mTableDataSource.rowHasAtLeastTwoMappings(row - 1)) {
                            mTableDataSource.setItemData(row - 1, column - 1);
                        } else {
                            if (mTableDataSource.getItemData(row - 1, column - 1) == '0') {
                                mTableDataSource.setItemData(row - 1, column - 1);
                            }
                        }
                    }

                    SparseArray<String> mappings = mTableDataSource.getCachedMappings();
                    for (int i = 0; i < mTableDataSource.getColumnsCount(); i++) {
                        mTableDataSource.updateMappings(mappings.keyAt(i), mappings.valueAt(i));
                    }

                    // update mappings from database
                    // store mappings in mTableDataSource.mMappings
                    mTableDataSource.getMappings();
                    mTableAdapter.notifyDataSetChanged();
                }

                private void setRange(int firstRow, int secondRow, int firstColumn, int secondColumn) {
                    Point entry = new Point(firstColumn, firstRow);
                    Point outro = new Point(secondColumn, secondRow);
                    int diffH = Math.abs(outro.x - entry.x);
                    int diffV = Math.abs(outro.y - entry.y);
                    int startColumn;
                    int startRow, endRow;

                    if (diffV > 0) {
                        if (firstRow > secondRow) {
//                            Log.d(TAG, "first row > second row");
                            startRow = secondRow - 1; // first row needs to be considered too
                            endRow = firstRow;
                            startColumn = secondColumn;
                        } else {
//                            Log.d(TAG, "second row > first row");
                            startRow = firstRow; // first row has already been set
                            endRow = secondRow;
                            startColumn = firstColumn;
                        }

                        float deltaH = diffH / (float) diffV;
                        if ((secondRow > firstRow && firstColumn > secondColumn) || (firstRow > secondRow && secondColumn > firstColumn)) {
                            deltaH *= -1;
                        }

                        float roundingFix = secondRow > firstRow ? deltaH : 0;

/*
                        SparseIntArray changedRows = new SparseIntArray();
                        for (Map.Entry<Integer, Integer> mapEntry : mRowChanges.entrySet()) {
                            changedRows.append(mapEntry.getKey(), mapEntry.getValue());
                        }
*/

//                        Log.d(TAG, "row changes: " + mRowChanges + "\nstart row: " + startRow + ", end row: " + endRow);

//                        Log.d(TAG, "startRow: " + startRow + ", endRow: " + endRow);
                        for (int i = startRow; i < endRow; i++) {
//                            Log.d(TAG, "i: " + i);
//                            final Integer currentRowValue = MapHelper.getKeyByValue(mRowChanges, i);
                            final Integer currentRowValue = mRowChanges.get(i+1);
//                            Log.d(TAG, "i: " + i + ", current row value: " + currentRowValue);
                            final int currentRow = currentRowValue == null ? i+1 : currentRowValue;
//                            Log.d(TAG, "i: " + i + ", current row: " + currentRow + ", deltaH: " + deltaH);

                            if (diffH == 0) {
//                                if (mTableDataSource.rowIsFull(i)) {
//                                    mTableDataSource.setFullRowData(i, firstColumn);
//                                }
                                if (mTableDataSource.rowIsFull(currentRow)) {
                                    mTableDataSource.setFullRowData(currentRow, firstColumn);
                                }
                            } else {
//                                if (mTableDataSource.rowIsFull(i)) {
//                                    mTableDataSource.setFullRowData(i, Math.round((startColumn - 1 + (i - startRow) * deltaH) + roundingFix));
//                                }
                                if (mTableDataSource.rowIsFull(currentRow-1)) {
                                    Log.d(TAG, "setFullRowData(" + currentRow + ", " + Math.round((startColumn - 1 + (i - startRow) * deltaH) + roundingFix) + ")");
                                    mTableDataSource.setFullRowData(currentRow-1, Math.round((startColumn - 1 + (i - startRow) * deltaH) + roundingFix));
                                } else {
                                    Log.d(TAG, "row is full: " + currentRow);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onRowHeaderClick(int row) {
                    Log.d(TAG, "click, row: " + row + "\nrow changes: " + tableLayout.getLinkedAdapterRowsModifications() + "\ncolumn changes: " + tableLayout.getLinkedAdapterColumnsModifications());
                }

                @Override
                public void onColumnHeaderClick(int column) {

                }

                @Override
                public void onLeftTopHeaderClick() {

                }
            });
            mTableAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public void onItemLongClick(int row, int column) {
                    Log.d(TAG, "long click, row: " + row);
                }

                @Override
                public void onLeftTopHeaderLongClick() {

                }
            });
            tableLayout.setAdapter(mTableAdapter);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    private int countAddresses() {
        int count = 0;
        SQLiteDatabase db = mActivity.getDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + SettingsContract.AddressSettingsEntries.TABLE_NAME + ";", null);
        count = cursor.getCount();

        cursor.close();

        return count;
    }
}
