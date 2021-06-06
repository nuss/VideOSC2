package net.videosc.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;

import androidx.fragment.app.FragmentManager;

import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.fragments.VideOSCCameraFragment;
import net.videosc.fragments.VideOSCSelectGroupFragment;

public class GroupSelectAdapter extends ResourceCursorAdapter {
    final private static String TAG = GroupSelectAdapter.class.getSimpleName();
    private final VideOSCMainActivity mActivity;
    private final VideOSCApplication mApp;
    private final int mLayout;
    private FragmentManager mManager;
    private VideOSCCameraFragment mCameraFragment;
    private VideOSCSelectGroupFragment mGroupsListFragment;
    private ViewGroup mParent;

    public GroupSelectAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
        this.mActivity = (VideOSCMainActivity) context;
        this.mApp = (VideOSCApplication) mActivity.getApplication();
        this.mLayout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        this.mManager = mActivity.getSupportFragmentManager();
        this.mCameraFragment = (VideOSCCameraFragment) mManager.findFragmentByTag("CamPreview");
        this.mGroupsListFragment = (VideOSCSelectGroupFragment) mManager.findFragmentByTag("group select");
        this.mParent = parent;
        return LayoutInflater.from(context).inflate(mLayout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }
}
