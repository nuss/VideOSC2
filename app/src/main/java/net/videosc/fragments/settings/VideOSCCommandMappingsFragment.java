package net.videosc.fragments.settings;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cleveroad.adaptivetablelayout.AdaptiveTableLayout;
import com.cleveroad.adaptivetablelayout.OnItemClickListener;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.adapters.CommandMappingsTableAdapter;
import net.videosc.db.SettingsContract;
import net.videosc.fragments.VideOSCBaseFragment;
import net.videosc.interfaces.mappings_data_source.MappingsTableDataSourceImpl;
import net.videosc.utilities.MapHelper;
import net.videosc.utilities.enums.CommandMappingsSortModes;

import java.util.Map;

public class VideOSCCommandMappingsFragment extends VideOSCBaseFragment {
    private final static String TAG = VideOSCCommandMappingsFragment.class.getSimpleName();

    private CommandMappingsTableAdapter mTableAdapter;
    private MappingsTableDataSourceImpl mTableDataSource;
    private int mNumAddresses;
    private Map<Integer, Integer> mRowChanges;
    private Map<Integer, Integer> mColumnChanges;
    private VideOSCApplication mApp;
    private ArrayAdapter<String> mSortSwitchAdapter;
    private PopupWindow mSortModesPopUp;
    private Button mSortSwitcher;

    public VideOSCCommandMappingsFragment() { }

    public VideOSCCommandMappingsFragment(Context context) {
        this.mActivity = (VideOSCMainActivity) context;
        this.mApp = (VideOSCApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
            mSortSwitcher = view.findViewById(R.id.sort_mode_switch);
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

                    if (mTableDataSource.rowIsFull(row - 1)) {
                        firstClick = !firstClick;
                        if (firstClick) {
                            // disable drag and drop while firstClick is true
                            tableLayout.setDragAndDropEnabled(false);
                            // cache start row
                            // consider changes in row order
                            firstRow = rowCurrentPosition == null ? row : rowCurrentPosition;
                            firstColumn = columnCurrentPosition == null ? column : columnCurrentPosition;
                            mTableDataSource.setFullRowData(row - 1, column - 1);
                        } else {
                            final int currentRow = rowCurrentPosition == null ? row : rowCurrentPosition;
                            final int currentColumn = columnCurrentPosition == null ? column : columnCurrentPosition;
                            setRange(firstRow, currentRow, firstColumn, currentColumn);
                            // re-enable drag and drop
                            tableLayout.setDragAndDropEnabled(true);
                        }
                    } else {
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
                    // getMappings() will retrieve mappings as ordered in mode SORT_BY_COLOR
                    // hence, we need to reorder them before displaying them if mode is SORT_BY_NUM
                    if (mApp.getCommandMappingsSortMode().equals(CommandMappingsSortModes.SORT_BY_NUM))
                        mTableDataSource.initSortMode();
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
                            startRow = secondRow - 1; // first row needs to be considered too
                            endRow = firstRow;
                            startColumn = secondColumn;
                        } else {
                            startRow = firstRow; // first row has already been set
                            endRow = secondRow;
                            startColumn = firstColumn;
                        }

                        float deltaH = diffH / (float) diffV;
                        if ((secondRow > firstRow && firstColumn > secondColumn) || (firstRow > secondRow && secondColumn > firstColumn)) {
                            deltaH *= -1;
                        }

                        float roundingFix = secondRow > firstRow ? deltaH : 0;

                        for (int i = startRow; i < endRow; i++) {
                            // if rows have been shifted get the original order
                            // will return null if index doesn't exist in mRowChanges
                            // we need i+1 due header row being first row (first table row has index 1)
                            final Integer currentRowValue = mRowChanges.get(i+1);
                            // if row not contained in mRowChanges just use i+1 as next row
                            final int currentRow = currentRowValue == null ? i+1 : currentRowValue;

                            if (diffH == 0) {
                                if (mTableDataSource.rowIsFull(currentRow-1)) {
                                    mTableDataSource.setFullRowData(currentRow-1, firstColumn-1);
                                }
                            } else {
                                if (mTableDataSource.rowIsFull(currentRow-1)) {
                                    // calculate next column based on number of rows to be considered
                                    // since number of rows may be arbitrary and column index must be integer round the result
                                    // depending an whether first row has a lower or higher index than the last row in the selected range
                                    // we must supply a rounding fix that can be 0 or deltaH
                                    final int columnIndex = Math.round((startColumn - 1 + (i - startRow) * deltaH) + roundingFix);
                                    // analog currentRow we have to check for next column in mColumnChanges
                                    final Integer column = mColumnChanges.get(columnIndex+1);
                                    final int currentColumn = column == null ? columnIndex+1 : column;
                                    mTableDataSource.setFullRowData(currentRow-1, currentColumn-1);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onRowHeaderClick(int row) { }

                @Override
                public void onColumnHeaderClick(int column) { }

                @Override
                public void onLeftTopHeaderClick() { }
            });

            tableLayout.setAdapter(mTableAdapter);

            // sort switch
            final Resources res = getResources();
            final String[] sortModes = new String[]{
                    res.getString(R.string.sort_by_color),
                    res.getString(R.string.sort_by_cmd_num)
            };
            mSortSwitchAdapter = new ArrayAdapter<>(mActivity, R.layout.sort_mode_item, sortModes);
            mSortModesPopUp = showSortModesList(mSortSwitchAdapter);
            mSortSwitcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSortModesPopUp.showAsDropDown(v, 0, 0);
                }
            });
            ListView sortModesList = (ListView) mSortModesPopUp.getContentView();
            sortModesList.setOnItemClickListener(new SortModesOnItemClickListener());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mApp.setCommandMappingsSortmode(CommandMappingsSortModes.SORT_BY_COLOR);
        mActivity = null;
    }

    private PopupWindow showSortModesList(ArrayAdapter<String> sortModesAdapter) {
        final PopupWindow popUp = new PopupWindow(mActivity);
        final ListView sortModesList = new ListView(mActivity);
        sortModesList.setAdapter(sortModesAdapter);
        popUp.setFocusable(true);
        popUp.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popUp.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popUp.setContentView(sortModesList);

        return popUp;
    }

    private int countAddresses() {
        SQLiteDatabase db = mActivity.getDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + SettingsContract.AddressSettingsEntries.TABLE_NAME + ";", null);
        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    private class SortModesOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Animation fadeInAnimation = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in);
            fadeInAnimation.setDuration(2);
            view.startAnimation(fadeInAnimation);

            String item = mSortSwitchAdapter.getItem(position);
            mSortSwitcher.setText(item);
            String[] sortEnums = CommandMappingsSortModes.getNames(CommandMappingsSortModes.class);
            mApp.setCommandMappingsSortmode(CommandMappingsSortModes.valueOf(sortEnums[position]));
            Log.d(TAG, "mappings before: " + mTableDataSource.getCachedMappings());
            mTableDataSource.initSortMode();
            Log.d(TAG, "mappings after: " + mTableDataSource.getCachedMappings());
            mTableDataSource.sortCommands(mApp.getCommandMappingsSortMode());
            mTableAdapter.notifyLayoutChanged();
            mSortModesPopUp.dismiss();
        }
    }
}
