package net.videosc2.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import net.videosc2.R;
import net.videosc2.db.SettingsContract;

public class SnapshotSelectAdapter extends ResourceCursorAdapter {
	final private static String TAG = "SnapshotSelectAdapter";
	private int mLayout;

	/**
	 * Standard constructor.
	 *
	 * @param context The context where the ListView associated with this adapter is running
	 * @param layout  Resource identifier of a layout file that defines the views
	 *                for this list item.  Unless you override them later, this will
	 *                define both the item views and the drop down views.
	 * @param c       The cursor from which to get the data.
	 * @param flags   Flags used to determine the behavior of the adapter,
	 *                as per {@link CursorAdapter#CursorAdapter(Context, Cursor, int)}.
	 */
	public SnapshotSelectAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
		Log.d(TAG, "cursor: " + c.getCount());
		mLayout = layout;
	}
//	private MergeCursor mCursor;
//	private Context mContext;
	/**
	 * Constructor
	 *
	 * @param context  The current context.
	 * @param cursor The cursor for querying the database
	 */
	/*public SnapshotSelectAdapter(@NonNull Context context, Cursor cursor) {
		super(context, cursor, 0);

		Log.d(TAG, "cursor: " + cursor);
	}*/

	/**
	 * Recommended constructor.
	 *
	 * @param context The context
	 * @param c       The cursor from which to get the data.
	 * @param flags   Flags used to determine the behavior of the adapter; may
	 *                be any combination of {@link #FLAG_AUTO_REQUERY} and
	 *                {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
	 */
	/*public SnapshotSelectAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}*/

	/**
	 * @param position
	 * @param convertView
	 * @param parent
	 * @see ListAdapter#getView(int, View, ViewGroup)
	 */
	/*@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (!mCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position " + position);
		}
		View v;
		if (convertView == null) {
			v = newView(mContext, mCursor, parent);
		} else {
			v = convertView;
		}
		bindView(v, mContext, mCursor);
		return v;
	}*/

	/**
	 * Makes a new view to hold the data pointed to by cursor.
	 *
	 * @param context Interface to application's global information
	 * @param cursor  The cursor from which to get the data. The cursor is already
	 *                moved to the correct position.
	 * @param parent  The parent to which the new view is attached to
	 * @return the newly created view.
	 */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(mLayout, parent, false);
	}

	/**
	 * Bind an existing view to the data pointed to by cursor
	 *
	 * @param view    Existing view, returned earlier by newView
	 * @param context Interface to application's global information
	 * @param cursor  The cursor from which to get the data. The cursor is already
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView row = (TextView) view.findViewById(R.id.snapshot_item);
		final int numPixels = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.PixelSnapshotEntries.SNAPSHOT_SIZE));
		String text = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.PixelSnapshotEntries.SNAPSHOT_NAME));
		if (numPixels > 0)
			text = text.concat(" (" + String.valueOf(numPixels) + " pixels)");
		row.setText(text);
	}

	/**
	 * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
	 * closed.
	 *
	 * @param cursor The new cursor to be used
	 */
	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
	}
}
