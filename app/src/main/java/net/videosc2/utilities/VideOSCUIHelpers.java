package net.videosc2.utilities;

import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by stefan on 18.03.17.
 */

public class VideOSCUIHelpers {
	final static String TAG = "VideOSCUIHelpers";

	/**
	 * Does the device have a torch?
	 *
	 * @return a boolean indicating whether the device has an inbuilt flashlight
	 */
	public static boolean hasTorch() {
		Camera camera = null;

		try {
			camera = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			e.printStackTrace();
		}


		if (camera == null) {
			return false;
		}

		Camera.Parameters parameters = camera.getParameters();
		camera.release();

		if (parameters.getFlashMode() == null) {
			return false;
		}

		List<String> supportedFlashModes = parameters.getSupportedFlashModes();

		return !(supportedFlashModes == null || supportedFlashModes.isEmpty()) && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH);
	}

	/**
	 * Set the margin for a view
	 *
	 * @param v a View object respectively an object of one of its subclasses
	 * @param l an integer denoting the left margin
	 * @param t an integer denoting the top margin
	 * @param r an integer denoting the right margin
	 * @param b an integer denoting the bottom margin
	 */
	public static void setMargins (View v, int l, int t, int r, int b) {
		if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			p.setMargins(l, t, r, b);
			v.requestLayout();
		}
	}
}
