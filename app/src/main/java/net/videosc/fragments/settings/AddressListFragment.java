package net.videosc.fragments.settings;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.videosc.R;
import net.videosc.adapters.AddressesListAdapter;
import net.videosc.fragments.VideOSCBaseFragment;

public class AddressListFragment extends VideOSCBaseFragment {
    private static final String TAG = "AddressListFragment";
    private Cursor mCursor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.addresses_list, container);
        final ListView addressesList = view.findViewById(R.id.addresses_list);
        final AddressesListAdapter addressesListAdapter = new AddressesListAdapter(
                getActivity(), R.layout.address_list_item, mCursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        addressesList.setAdapter(addressesListAdapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    void setCursor(Cursor cursor) {
        this.mCursor = cursor;
    }
}
