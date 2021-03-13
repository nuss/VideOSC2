package net.videosc.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.db.SettingsContract;

import oscP5.OscP5;

public class AddressesListAdapter extends ResourceCursorAdapter {
    final private static String TAG = "AddressesListAdapter";
    private final int mLayout;
    private SQLiteDatabase mDb;
    private final String[] mAddrFields = new String[]{
            SettingsContract.AddressSettingsEntries.IP_ADDRESS,
            SettingsContract.AddressSettingsEntries.PORT,
            SettingsContract.AddressSettingsEntries.PROTOCOL,
            SettingsContract.AddressSettingsEntries._ID
    };
    final private String mSortOrder = SettingsContract.AddressSettingsEntries._ID + " DESC";

    /**
     * Constructor with default behavior as per
     * {@link CursorAdapter#CursorAdapter(Context, Cursor, boolean)}; it is recommended
     * you not use this, but instead {ResourceCursorAdapter(Context, int, Cursor, int)}.
     * When using this constructor, {@link #FLAG_REGISTER_CONTENT_OBSERVER}
     * will always be set.
     *
     * @param context The context where the ListView associated with this adapter is running
     * @param layout  resource identifier of a layout file that defines the views
     *                for this list item.  Unless you override them later, this will
     *                define both the item views and the drop down views.
     * @param c       The cursor from which to get the data.
     * @param flags
     */
    public AddressesListAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
        this.mLayout = layout;
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
    public void bindView(View view, final Context context, Cursor cursor) {
        String protocolName;

        final VideOSCApplication app = (VideOSCApplication) context.getApplicationContext();
        final TextView ipText = view.findViewById(R.id.remote_ip_address);
        final TextView portText = view.findViewById(R.id.remote_port);
        final TextView protocolText = view.findViewById(R.id.address_protocol);
        final ImageButton deleteButton = view.findViewById(R.id.delete_address);

        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries._ID));
        final String ip = cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.IP_ADDRESS));
        final int port = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PORT));
        final int protocol = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.AddressSettingsEntries.PROTOCOL));

        protocolName = protocol == OscP5.TCP ? "TCP/IP" : "UDP";

        ipText.setText(ip);
        portText.setText(String.valueOf(port));
        protocolText.setText(protocolName);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret = mDb.delete(
                        SettingsContract.AddressSettingsEntries.TABLE_NAME,
                        SettingsContract.AddressSettingsEntries._ID + " = " + id,
                        null
                );
                if (ret > 0) {
                    // we don't know if a mappings entry for the given address exists
                    // so we don't make progress dependent on a successful deletion
                    mDb.delete(
                            SettingsContract.AddressCommandsMappings.TABLE_NAME,
                            SettingsContract.AddressCommandsMappings.ADDRESS + " = " + id,
                            null
                    );

                    Cursor cursor = mDb.query(
                            SettingsContract.AddressSettingsEntries.TABLE_NAME,
                            mAddrFields,
                            null,
                            null,
                            null,
                            null,
                            mSortOrder
                    );

                    changeCursor(cursor);
                    app.removeBroadcastClient((int) id);
                    app.removeCommandMappingsAt((int) id);
                    SparseArray<String> cmdMappings = app.getCommandMappings();
                    Log.d(TAG, "mappings before: " + app.getCommandMappings());

                    if (cmdMappings.size() == 1) {
                        // only one *un-editable* slot left, hence reset all commands
                        int key = cmdMappings.keyAt(0);
                        Point resolution = app.getResolution();
                        StringBuilder mappings = new StringBuilder(resolution.x * resolution.y * 3);
                        for (int i = 0; i < resolution.x * resolution.y * 3; i++) {
                            mappings.append('1');
                        }
                        cmdMappings.setValueAt(0, String.valueOf(mappings));
                        ContentValues values = new ContentValues();
                        values.put(
                                SettingsContract.AddressCommandsMappings.ADDRESS,
                                key
                        );
                        values.put(
                                SettingsContract.AddressCommandsMappings.MAPPINGS,
                                String.valueOf(mappings)
                        );
                        mDb.update(
                                SettingsContract.AddressCommandsMappings.TABLE_NAME,
                                values,
                                SettingsContract.AddressCommandsMappings.ADDRESS + " = " + key,
                                null
                        );
                        values.clear();
                    }
                    Log.d(TAG, "mappings after: " + app.getCommandMappings());
                    notifyDataSetChanged();
                }
            }
        });
    }

    public void setDatabase(SQLiteDatabase db) {
        this.mDb = db;
    }
}
