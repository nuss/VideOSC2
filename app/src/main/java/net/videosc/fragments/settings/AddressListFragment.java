package net.videosc.fragments.settings;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.fragments.VideOSCBaseFragment;

public class AddressListFragment extends VideOSCBaseFragment {
    private static final String TAG = "AddressListFragment";
    private Cursor mCursor;
    private String[] mColumns;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
        assert activity != null;
        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.addresses_list, container);

        final int[] to = new int[] {
                R.id.remote_ip_address,
                R.id.remote_port,
                R.id.address_protocol,
                R.id.entry_id
        };
        final SimpleCursorAdapter addressesAdapter = new SimpleCursorAdapter(
                getActivity(), R.layout.address_list_item, mCursor, mColumns, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        final ListView addressesList = view.findViewById(R.id.addresses_list);
        addressesList.setAdapter(addressesAdapter);
//        final AddressesListAdapter addressesListAdapter = new AddressesListAdapter(
//                getActivity(), R.layout.address_list_item, mCursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
//        );
//        addressesList.setAdapter(addressesListAdapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    void setCursor(Cursor cursor) {
        this.mCursor = cursor;
    }

    void setColumns(String[] columns) {
        this.mColumns = columns;
    }
}
