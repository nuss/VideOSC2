package net.videosc2.utilities;

import android.animation.LayoutTransition;
import android.hardware.Camera;
import android.os.Build;
import android.support.compat.BuildConfig;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

/**
 * Created by stefan on 18.03.17.
 */

public class VideOSCUIHelpers {
	final static String TAG = "VideOSCUIHelpers";

//	private LayoutTransition transition = new LayoutTransition();

	/**
	 * Does the device have a torch?
	 *
	 * @return a boolean indicating whether the device has an inbuilt flashlight
	 */
	public static boolean hasTorch(Camera camera) {
		if (camera == null) {
			return false;
		}

		try {
			Camera.Parameters parameters = camera.getParameters();
			camera.release();
			List<String> supportedFlashModes = parameters.getSupportedFlashModes();
			return !(supportedFlashModes == null || supportedFlashModes.isEmpty()) && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH);
		} catch(RuntimeException e) {
//			e.printStackTrace();
			return false;
		}

//		return false;
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

	/**
	 * Add a child view to a FrameLayout
	 * Use the return value to set a status variable
	 *
	 * @param view a View
	 * @param parent a FrameLayout
	 * @return true
	 */
	public static boolean addView(View view, FrameLayout parent) {
		parent.addView(view);
		return true;
	}

	/**
	 * Remove a view from a FrameLayout
	 * Use the return value to set a status variable
	 *
	 * @param view a View
	 * @param parent a FrameLayout
	 * @return false
	 */
	public static boolean removeView(View view, FrameLayout parent) {
		parent.removeView(view);
		return false;
	}

	/**
	 * Add a child view to a ViewGroup
	 * Use the return value to set a status variable
	 *
	 * @param view a View
	 * @param parent a FrameLayout
	 * @return true
	 */
	public static boolean addView(View view, ViewGroup parent) {
		parent.addView(view);
		return true;
	}

	/**
	 * Remove a view from a ViewGroup
	 * Use the return value to set a status variable
	 *
	 * @param view a View
	 * @param parent a ViewGroup
	 * @return false
	 */
	public static boolean removeView(View view, ViewGroup parent) {
		parent.removeView(view);
		return false;
	}

	/**
	 * Fade in a ViewGroup
	 *
	 * @param view a ViewGroup, must already have been inflated but not displayed
	 */
	public static void setTransitionAnimation(ViewGroup view) {
//		AnimatorSet InAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.fade_in);
//		InAnimator.playTogether(InAnimator);
		// rather than defining transition statically in a class-wide context
		// define it here and avoid a memory leak
		LayoutTransition transition = new LayoutTransition();
//		transition.setAnimator(LayoutTransition.APPEARING, InAnimator);
		transition.setAnimator(LayoutTransition.APPEARING, null);
		transition.setAnimator(LayoutTransition.DISAPPEARING, null);
		view.setLayoutTransition(transition);
	}

	/**
	 * Reset the app appearance in dependency of the build version
	 *
	 * @param view the view on which 'setSystemVisibility()' is called
	 */
	public static void resetSystemUIState(View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			Log.d(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT");
			view.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		} else {
//			Log.d(TAG, "else branch");
			view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}

	/**
	 * Check if a frontside camera exists
	 * @return boolean indicating whether a frontside camera exists
	 */
	public static boolean hasFrontsideCamera() {
		Camera.CameraInfo ci = new Camera.CameraInfo();
		for (int i = 0 ; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, ci);
			if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) return true;
		}
		return false; // No front-facing camera found
	}

	/**
	 * Set to fullscreen but show action bar
	 *
	 * @param view the view on which 'setSystemVisibility()' is called
	 */
	static void setFormSystemUIState(View view) {
		view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
	}
}
