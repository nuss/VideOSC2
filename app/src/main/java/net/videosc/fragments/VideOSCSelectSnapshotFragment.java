package net.videosc.fragments;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import net.videosc.R;
import net.videosc.adapters.SnapshotSelectAdapter;
import net.videosc.utilities.VideOSCUIHelpers;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

public class VideOSCSelectSnapshotFragment extends VideOSCBaseFragment {
	private final static String TAG = "SelectSnapshotFragment";
	private Cursor mCursor;
	private Cursor mDbCursor;
	private MatrixCursor mExtraCursor;
	private SQLiteDatabase mDb;

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
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "snapshots fragment, on create view");
//		final ScrollView bg = (ScrollView) inflater.inflate(R.layout.settings_background_scroll, container, false);
		final FragmentManager manager = getFragmentManager();
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.snapshots_list, container, false);
		final ListView snapshotsListView = view.findViewById(R.id.snapshots_list);
		final SnapshotSelectAdapter adapter = new SnapshotSelectAdapter(
				getActivity(), R.layout.snapshots_item, mCursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);
		snapshotsListView.setAdapter(adapter);
		VideOSCUIHelpers.setTransitionAnimation(view);
		// prevent underlying view from receiving touch events
		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.performClick();
				return true;
			}
		});
		final ImageButton close = view.findViewById(R.id.close);
		close.bringToFront();
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				assert manager != null;
				manager.beginTransaction()
						.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
						.remove(VideOSCSelectSnapshotFragment.this)
						.commit();
			}
		});
		return view;
	}

	/**
	 * Called when the Fragment is no longer started.  This is generally
	 * tied to  of the containing
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

	public void setDatabase(SQLiteDatabase db) {
		this.mDb = db;
	}

	public SQLiteDatabase getDatabase() {
		return this.mDb;
	}

	/*public MatrixCursor getExtrasCursor() {
		return this.mExtraCursor;
	}

	public void setDbCursor(Cursor cursor) {
		this.mDbCursor = cursor;
	}*/
}
