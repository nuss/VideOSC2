package net.videosc2.fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.db.SettingsContract;
import net.videosc2.db.SettingsDBHelper;

public class VideOSCSelectSnapShotFragment extends VideOSCBaseFragment {

	public VideOSCSelectSnapShotFragment() {
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
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		// we need the database where snapshots are stored
		VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
		final SettingsDBHelper dbHelper = activity.mDbHelper;
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		// the background scrollview - dark transparent, no content
		final ScrollView bg = (ScrollView) inflater.inflate(R.layout.settings_background_scroll, container, false);
		final ViewGroup snapshotsListView = (ViewGroup) inflater.inflate(R.layout.snapshots_list, container, false);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
}
