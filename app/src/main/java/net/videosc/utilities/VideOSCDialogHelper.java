package net.videosc2.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v13.app.ActivityCompat;
import android.view.ContextThemeWrapper;

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
		new AlertDialog.Builder(
				new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Light_Dialog)
		)
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

	public static void showPermissionDialog(final Activity activity, String message, final String[] permissions, final int requestCode) {
		new AlertDialog.Builder(
				new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Light_Dialog)
		)
				.setMessage(message)
				.setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityCompat.requestPermissions(activity, permissions, requestCode);
					}
				})
				.setNegativeButton(R.string.deny, null)
				.show();
	}

	public static void showDialog(
			final Activity activity,
			int style,
			String msg,
			String positive,
			DialogInterface.OnClickListener positiveAction,
			String negative,
			DialogInterface.OnClickListener negativeAction
	) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(activity, style)
		)
				.setMessage(msg);

		if (positive != null)
			builder.setPositiveButton(positive, positiveAction);

		if (negative != null)
			builder.setNegativeButton(negative, negativeAction);

		builder.show();
	}
}
