package net.videosc.fragments.settings;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
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

    public VideOSCCommandMappingsFragment() { }

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
            AdaptiveTableLayout mTableLayout = view.findViewById(R.id.address_command_mappings_table);
            mTableAdapter = new CommandMappingsTableAdapter(mActivity, mTableDataSource);
            mTableAdapter.setOnItemClickListener(new OnItemClickListener() {
                private boolean firstClick = false;

                @Override
                public void onItemClick(int row, int column) {
                    Point from = new Point(), to = new Point();
                    if (mTableDataSource.rowIsFull(row-1)) {
                        firstClick = !firstClick;
                        Log.d(TAG, "row: " + row + ", column: " + column + ", row is full");
                        mTableDataSource.setFullRowData(row-1, column-1);
                    } else {
                        Log.d(TAG, "row: " + row + ", column: " + column + ", row is not full, getItemData: " + mTableDataSource.getItemData(row-1, column-1));
                        if (mTableDataSource.rowHasAtLeastTwoMappings(row-1)) {
                            firstClick = !firstClick;
                            mTableDataSource.setItemData(row-1, column-1);
                        } else {
                            if (mTableDataSource.getItemData(row-1, column-1) == '0') {
                                firstClick = !firstClick;
                                mTableDataSource.setItemData(row-1, column-1);
                            }
                        }
                    }
                    if (firstClick) {
                        from.x = row;
                        from.y = column;
                    } else {
                        to.x = row;
                        to.y = column;
                        Log.d(TAG, "selected range: " + from + "-" + to);
                    }
                    Log.d(TAG, "first click: " + firstClick);

                    // update mappings from database
                    // store mappings in mTableDataSource.mMappings
                    mTableDataSource.getMappings();
                    mTableAdapter.notifyDataSetChanged();
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
