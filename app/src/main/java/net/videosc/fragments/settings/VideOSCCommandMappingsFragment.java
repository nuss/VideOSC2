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

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.adapters.CommandMappingsTableAdapter;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.interfaces.mappings_data_source.MappingsTableDataSourceImpl;

public class VideOSCCommandMappingsFragment extends VideOSCBaseFragment {
    private final static String TAG = VideOSCCommandMappingsFragment.class.getSimpleName();

    private CommandMappingsTableAdapter mTableAdapter;
    private MappingsTableDataSourceImpl mTableDataSource;
    private int mNumAddresses;

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
            final AdaptiveTableLayout mTableLayout = view.findViewById(R.id.address_command_mappings_table);
            mTableAdapter = new CommandMappingsTableAdapter(mActivity, mTableDataSource);
            mTableAdapter.setOnItemClickListener(new OnItemClickListener() {
                private boolean firstClick = false;
                int firstRow, firstColumn;

                @Override
                public void onItemClick(int row, int column) {
                    if (mTableDataSource.rowIsFull(row-1)) {
                        firstClick = !firstClick;
                        if (firstClick) {
                            // cache start row
                            firstRow = row;
                            firstColumn = column;
                            mTableDataSource.setFullRowData(row - 1, column - 1);
                        } else {
                            setRange(firstRow, row, firstColumn, column);
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
/*
                        if (diffH == 0) {
                            // firstColumn == secondcolumn
                            for (int i = 0; i < diffV; i++) {
                                colIndices.add(firstColumn);
                            }
                        }
*/

                        if (firstRow > secondRow) {
                            startRow = secondRow - 1; // first row needs to be considered too
                            endRow = firstRow;
                            startColumn = secondColumn;
                        } else {
                            startRow = firstRow; // first row has already been set
                            endRow = secondRow;
                            startColumn = firstColumn;
                        }

                        float deltaH = diffH/(float) diffV;
                        if ((secondRow > firstRow && firstColumn > secondColumn) || (firstRow > secondRow && secondColumn > firstColumn)) {
                            deltaH *= -1;
                        }

                        for (int i = startRow; i < endRow; i++) {
                            Log.d(TAG, "startRow: " + startRow + ", endRow: " + endRow);
                            if (diffH == 0) {
                                if (mTableDataSource.rowIsFull(i)) {
                                    mTableDataSource.setFullRowData(i, firstColumn);
                                }
                            } else {
                                if (mTableDataSource.rowIsFull(i)) {
//                                    Log.d(TAG, "i: " + i + ", startRow: " + startRow + ", endRow: " + endRow + ", delta: " + Math.round(startColumn - 1 + (i - startRow) * deltaH));
//                                    Log.d(TAG, "diffV: " + diffV + "\nfirst column: " + (firstColumn - 1) + "\ni: " + (i - startRow) + "\ndeltaH:" + deltaH + "\nnext column raw: " + (firstColumn + i * deltaH) + "\nnext column rounded: " + Math.round(firstColumn + i * deltaH));
                                    mTableDataSource.setFullRowData(i, Math.round(startColumn - 1 + (i - startRow) * deltaH));
                                }
                            }
                        }
                    }
                }

                @Override
                public void onRowHeaderClick(int row) {

                }

                @Override
                public void onColumnHeaderClick(int column) {

                }

                @Override
                public void onLeftTopHeaderClick() {

                }
            });
            mTableLayout.setAdapter(mTableAdapter);
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
