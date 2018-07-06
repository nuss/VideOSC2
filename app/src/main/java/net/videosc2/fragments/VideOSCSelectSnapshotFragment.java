package net.videosc2.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import net.videosc2.R;
import net.videosc2.adapters.SnapshotSelectAdapter;

public class VideOSCSelectSnapshotFragment extends VideOSCBaseFragment {
	final static String TAG = "SelectSnapshotFragment";
	private SnapshotSelectAdapter mAdapter;
	private Cursor mCursor;
	private Cursor mDbCursor;
	private MatrixCursor mExtraCursor;

	public VideOSCSelectSnapshotFragment() {
		super();
	}

	/**
	 * Called to have the fragment instantiate its user interface view.
	 * This is optional, and non-graphical fragments can return null (which
	 * is the default implementation).  This will be called between
	 * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
	 * <p>
	 * <p>If you return a View from here, you will later be called in
	 * {@link #onDestroyView} when the view is being released.
	 *
	 * @param inflater           The LayoutInflater object that can be used to inflate
	 *                           any views in the fragment,
	 * @param container          If non-null, this is the parent view that the fragment's
	 *                           UI should be attached to.  The fragment should not add the view itself,
	 *                           but this can be used to generate the LayoutParams of the view.
	 * @param savedInstanceState If non-null, this fragment is being re-constructed
	 *                           from a previous saved state as given here.
	 * @return Return the View for the fragment's UI, or null.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "snapshots fragment, on create view");
		final ScrollView bg = (ScrollView) inflater.inflate(R.layout.settings_background_scroll, container, false);
		final View view = inflater.inflate(R.layout.snapshots_list, bg, false);
		final ListView snapshotsListView = (ListView) view.findViewById(R.id.snapshots_list);
		final SnapshotSelectAdapter adapter = new SnapshotSelectAdapter(getActivity(), R.layout.snapshots_item, mCursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		snapshotsListView.setAdapter(adapter);
//		VideOSCUIHelpers.setTransitionAnimation(bg);
//		bg.requestDisallowInterceptTouchEvent(true);
		return view;
	}

	/**
	 * Called when the Fragment is no longer started.  This is generally
	 * tied to {@link Activity#onStop() Activity.onStop} of the containing
	 * Activity's lifecycle.
	 */
	@Override
	public void onStop() {
		super.onStop();
		mCursor.close();
		mDbCursor.close();
		mExtraCursor.close();
	}

	public void setCursors(MergeCursor cursor, Cursor dbCursor, MatrixCursor extras) {
		this.mCursor = cursor;
		this.mDbCursor = dbCursor;
		this.mExtraCursor = extras;
	}
}
