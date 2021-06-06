package net.videosc.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.adapters.GroupSelectAdapter;
import net.videosc.utilities.VideOSCUIHelpers;

public class VideOSCSelectGroupFragment extends VideOSCBaseFragment {

    private SQLiteDatabase mDb;
    private Cursor mCursor;

    public VideOSCSelectGroupFragment() {
    }

    public VideOSCSelectGroupFragment(VideOSCMainActivity activity) {
        this.mActivity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.groups_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final FragmentManager manager = getFragmentManager();
        final ListView groupsListView = view.findViewById(R.id.groups_list);
        final GroupSelectAdapter adapter = new GroupSelectAdapter(
                mActivity, R.layout.groups_list, mCursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        groupsListView.setAdapter(adapter);
        VideOSCUIHelpers.setTransitionAnimation((ViewGroup) view);
        // prevent underlying view from receiving touch events
        view.setOnTouchListener((v, event) -> {
            v.performClick();
            return true;
        });
        final ImageButton close = view.findViewById(R.id.close);
        close.bringToFront();
        close.setOnClickListener(v -> {
            assert manager != null;
            manager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .remove(VideOSCSelectGroupFragment.this)
                    .commit();
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setDatabase(SQLiteDatabase db) {
        this.mDb = db;
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
    }
}
