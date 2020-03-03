package net.videosc.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import net.videosc.R;
import net.videosc.db.SettingsContract;

public class AddressesListAdapter extends ResourceCursorAdapter {
	final private static String TAG = "AddressesListAdapter";
	private int mLayout;

	/**
	 * Constructor with default behavior as per
	 * {@link CursorAdapter#CursorAdapter(Context, Cursor, boolean)}; it is recommended
	 * you not use this, but instead {@link #ResourceCursorAdapter(Context, int, Cursor, int)}.
	 * When using this constructor, {@link #FLAG_REGISTER_CONTENT_OBSERVER}
	 * will always be set.
	 *
	 * @param context     The context where the ListView associated with this adapter is running
	 * @param layout      resource identifier of a layout file that defines the views
	 *                    for this list item.  Unless you override them later, this will
	 *                    define both the item views and the drop down views.
	 * @param c           The cursor from which to get the data.
	 * @param flags
	 */
	public AddressesListAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
		mLayout = layout;
	}

	/**
	 * Inflates view(s) from the specified XML file.
	 *
	 * @param context
	 * @param cursor
	 * @param parent
	 * @see CursorAdapter#newView(Context,
	 * Cursor, ViewGroup)
	 */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Log.d(TAG, "newView called - cursor: " + cursor + ", parent: " + parent + ", mLayout: " + mLayout);
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
		Log.d(TAG, "bindView called - view: " + view);
		TextView ipText = view.findViewById(R.id.remote_ip_address);
		TextView portText = view.findViewById(R.id.remote_port);
		TextView protocolText = view.findViewById(R.id.address_protocol);
		final int count = cursor.getCount();
		Log.d(TAG, "num entries: " + count);
		final long id = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry._ID));
		final String ip = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.IP_ADDRESS));
		final int port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.PORT));
		final String protocol = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntry.PROTOCOL));

		ipText.setText(ip);
		portText.setText(String.valueOf(port));
		protocolText.setText(protocol);
	}
}
