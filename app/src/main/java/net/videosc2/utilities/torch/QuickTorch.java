package net.videosc2.utilities.torch;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class QuickTorch extends Activity {

	private static final String TAG = QuickTorch.class.getSimpleName();

	/**
	 * Start Torch when triggered
	 */
	@Override
	public void onStart() {
		super.onStart();

		Torch torch = new Torch();

		Log.d(TAG, "onStart()");
		if (torch.getTorch() == null) {
			Log.d(TAG, "torch == null");
			Intent intent = new Intent(this, Torch.class);
			startActivity(intent);
		} else {
			Log.d(TAG, "torch != null");
		}
		finish();
	}
}
