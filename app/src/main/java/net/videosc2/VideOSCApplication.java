package net.videosc2;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import net.videosc2.db.SettingsDBHelper;
import net.videosc2.utilities.enums.RGBModes;

/**
 * Created by stefan on 05.07.17, package net.videosc2, project VideOSC22.
 */
public class VideOSCApplication extends Application {
	private boolean isRGBPositive = true; // always init to true
	private Enum colorMode = RGBModes.RGB;
	private SettingsDBHelper settingshelper;

	@Override
	public void onCreate() {
		super.onCreate();
		if (LeakCanary.isInAnalyzerProcess(this)) {
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}
		LeakCanary.install(this);
		// rather than initializing SettingsDBHelper statically retrieve
		// settingsHelper instance with getSettingshelper (no memory leaks)
		settingshelper = new SettingsDBHelper(getApplicationContext());
	}

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

	public SettingsDBHelper getSettingsHelper() {
		return this.settingshelper;
	}
}
