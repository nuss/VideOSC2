package net.videosc2.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import net.videosc2.R;

/**
 * Created by Stefan Nussbaumer on 2017-03-24
 */
public class VideOSCDialogHelper {

	/**
	 * Quit dialog, called on back pressed
	 *
	 * @param activity The activity
	 */
	public static void showQuitDialog(final Activity activity) {
		new AlertDialog.Builder(activity)
				.setMessage(R.string.quit_dialog_string)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.finish();
					}

				})
				.setNegativeButton(R.string.no, null)
				.show();
	}
}
