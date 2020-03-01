package net.videosc.fragments.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;

import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddAddressDialog extends DialogFragment {
	final private static String TAG = "AddAddressDialog";
	private ArrayAdapter<String> mProtocolsAdapter;
	private PopupWindow mProtocolsPopUp;
	private Button mProtocolsButton;
	private String mSelectedProtocol;
	private View mView;
	private VideOSCMainActivity mActivity;

	@Override
	public void onDetach() {
		super.onDetach();
		mActivity = null;
	}

	/**
	 * Override to build your own custom Dialog container.  This is typically
	 * used to show an AlertDialog instead of a generic Dialog; when doing so,
	 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} does not need
	 * to be implemented since the AlertDialog takes care of its own content.
	 *
	 * <p>This method will be called after {@link #onCreate(Bundle)} andprotocolsPopUp
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
		mActivity = (VideOSCMainActivity) getActivity();
		assert mActivity != null;
		final SQLiteDatabase db = mActivity.mDb;
		final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		final LayoutInflater inflater = requireActivity().getLayoutInflater();
		mView = inflater.inflate(R.layout.add_address_dialog, null);
		mProtocolsButton = mView.findViewById(R.id.select_protocol);
		final String[] protocols = new String[]{"UDP", "TCP/IP", "MULTICAST"};
		mProtocolsAdapter = new ArrayAdapter<>(mActivity, R.layout.protocols_select_item, protocols);
		mProtocolsPopUp = showProtocolsList(mActivity, mProtocolsAdapter);

		String buttonText = getResources().getString(R.string.protocol_1_s);
		buttonText = String.format(buttonText, "UDP");
		mProtocolsButton.setText(buttonText);
		mProtocolsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mProtocolsPopUp.showAsDropDown(v, 0, 0);
			}
		});

		final ListView protocolsList = (ListView) mProtocolsPopUp.getContentView();
		protocolsList.setOnItemClickListener(new ProtocolsOnItemClickListener());

		builder.setView(mView)
				.setPositiveButton(R.string.add_address, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ContentValues entries = new ContentValues();
						EditText ipAddressField = mView.findViewById(R.id.add_ip_address);
						EditText portField = mView.findViewById(R.id.add_port);
						Log.d(TAG, "IP: " + ipAddressField.getText().toString() + ", port: " + portField.getText().toString() + ", protocol: " + mSelectedProtocol);
						final String ipEntry = ipAddressField.getText().toString();
						final Pattern ipAddressPattern = Patterns.IP_ADDRESS;
						final int portEntry = Integer.parseInt(portField.getText().toString());
						if (!ipEntry.isEmpty() && ipAddressPattern.matcher(ipEntry).matches() && portEntry >= 0 && portEntry <= 65535 && mSelectedProtocol != null) {
							entries.put(SettingsContract.AddressSettingsEntry.IP_ADDRESS, ipAddressField.getText().toString());
							entries.put(SettingsContract.AddressSettingsEntry.PORT, portField.getText().toString());
							entries.put(SettingsContract.AddressSettingsEntry.PROTOCOL, mSelectedProtocol);
							db.insert(SettingsContract.AddressSettingsEntry.TABLE_NAME, null, entries);
							entries.clear();
						}
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

		return builder.create();
	}

	/**
	 * Remove dialog.
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mView = null;
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

	class ProtocolsOnItemClickListener implements AdapterView.OnItemClickListener {

		/**
		 * Callback method to be invoked when an item in this AdapterView has
		 * been clicked.
		 * <p>
		 * Implementers can call getItemAtPosition(position) if they need
		 * to access the data associated with the selected item.
		 *
		 * @param parent   The AdapterView where the click happened.
		 * @param view     The view within the AdapterView that was clicked (this
		 *                 will be a view provided by the adapter)
		 * @param position The position of the view in the adapter.
		 * @param id       The row id of the item that was clicked.
		 */
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mSelectedProtocol = mProtocolsAdapter.getItem(position);
			String buttonText = getResources().getString(R.string.protocol_1_s);
			buttonText = String.format(buttonText, mSelectedProtocol);
			mProtocolsButton.setText(buttonText);
			mProtocolsPopUp.dismiss();
		}
	}
}
