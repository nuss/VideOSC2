package net.videosc2;

import android.app.Application;

/**
 * Created by stefan on 05.07.17, package net.videosc2, project VideOSC22.
 */
public class VideOSCApplication extends Application {
	private boolean isRGBPositive;

	public void setIsRGBPositive(boolean boolVal) {
		this.isRGBPositive = boolVal;
	}

	public boolean getIsRGBPositive() {
		return this.isRGBPositive;
	}
}
