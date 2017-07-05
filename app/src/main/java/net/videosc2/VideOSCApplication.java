package net.videosc2;

import android.app.Application;

import net.videosc2.utilities.enums.RGBModes;

/**
 * Created by stefan on 05.07.17, package net.videosc2, project VideOSC22.
 */
public class VideOSCApplication extends Application {
	private boolean isRGBPositive = true; // always init to true
	private Enum colorMode = RGBModes.RGB;

	public void setIsRGBPositive(boolean boolVal) {
		this.isRGBPositive = boolVal;
	}

	public boolean getIsRGBPositive() {
		return this.isRGBPositive;
	}

	public void setColorMode(Enum colorMode) {
		this.colorMode = colorMode;
	}

	public Enum getColorMode() {
		return this.colorMode;
	}
}
