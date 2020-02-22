package net.videosc.fragments.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddAddressDialog extends DialogFragment {
	final private static String TAG = "AddAddressDialog";
	/**
	 * Override to build your own custom Dialog container.  This is typically
	 * used to show an AlertDialog instead of a generic Dialog; when doing so,
	 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} does not need
	 * to be implemented since the AlertDialog takes care of its own content.
	 *
	 * <p>This method will be called after {@link #onCreate(Bundle)} and
	 * before {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.  The
	 * default implementation simply instantiates and returns a {@link Dialog}
	 * class.
	 *
	 * <p><em>Note: DialogFragment own the {@link Dialog#setOnCancelListener
	 * Dialog.setOnCancelListener} and {@link Dialog#setOnDismissListener
	 * Dialog.setOnDismissListener} callbacks.  You must not set them yourself.</em>
	 * To find out about these events, override {@link #onCancel(DialogInterface)}
	 * and {@link #onDismiss(DialogInterface)}.</p>
	 *
	 * @param savedInstanceState The last saved instance state of the Fragment,
	 *                           or null if this is a freshly created Fragment.
	 * @return Return a new Dialog instance to be displayed by the Fragment.
	 */
	@SuppressLint("InflateParams")
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
		assert activity != null;
		final SQLiteDatabase db = activity.mDb;
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final LayoutInflater inflater = requireActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.add_address_dialog, null);
		final Button protocolsButton = view.findViewById(R.id.select_protocol);
		final String[] protocols = new String[]{"UDP", "TCP/IP", "MULTICAST"};
		final ArrayAdapter<String> protocolsAdapter = new ArrayAdapter<>(activity, R.layout.protocols_select_item, protocols);
		final PopupWindow protocolsPopUp = showProtocolsList(activity, protocolsAdapter);

		protocolsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				protocolsPopUp.showAsDropDown(v, 0, 0);
			}
		});

		builder.setView(view)
				.setPositiveButton(R.string.add_address, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ContentValues entries = new ContentValues();
						EditText ipAddressField = view.findViewById(R.id.add_ip_address);
						EditText portField = view.findViewById(R.id.add_port);
						entries.put(SettingsContract.AddressSettingsEntry.IP_ADDRESS, ipAddressField.getText().toString());
						entries.put(SettingsContract.AddressSettingsEntry.PORT, portField.getText().toString());
						entries.put(SettingsContract.AddressSettingsEntry.PROTOCOL, "UDP");
						db.insert(SettingsContract.AddressSettingsEntry.TABLE_NAME, null, entries);

						entries.clear();
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});


		return builder.create();
	}

	private PopupWindow showProtocolsList(VideOSCMainActivity activity, ArrayAdapter<String> adapter) {
		final PopupWindow popUp = new PopupWindow(activity);
		final ListView frameratesList = new ListView(activity);
		frameratesList.setAdapter(adapter);
		popUp.setFocusable(true);
		popUp.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		popUp.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popUp.setContentView(frameratesList);

		return popUp;
	}

}
