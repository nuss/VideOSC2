package net.videosc2;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import net.videosc2.db.SettingsDBHelper;
import net.videosc2.utilities.VideOSCOscHandler;
import net.videosc2.utilities.enums.RGBModes;

import oscP5.OscMessage;

/**
 * Created by stefan on 05.07.17, package net.videosc2, project VideOSC22.
 */
public class VideOSCApplication extends Application {
	private boolean isRGBPositive = true; // always init to true
	private Enum mColorMode = RGBModes.RGB;
	private SettingsDBHelper mSettingsHelper;
	private boolean play = false; // send pixel values via OSC
	private boolean normalized = false;
	public VideOSCOscHandler mOscHelper;

	// setting dialogs
	// levels within settings dialog
	// 0: no dialog, normal mode
	// 1: first level - selections 'network settings', 'resolution settings', 'sensor settings', 'about'
	// 2: editor setting details
	private short settingsLevel = 0;

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
		mSettingsHelper = new SettingsDBHelper(getApplicationContext());
		mOscHelper = new VideOSCOscHandler(getApplicationContext());
	}

	public void setIsRGBPositive(boolean boolVal) {
		this.isRGBPositive = boolVal;
	}

	public boolean getIsRGBPositive() {
		return this.isRGBPositive;
	}

	public void setColorMode(Enum mColorMode) {
		this.mColorMode = mColorMode;
	}

	public Enum getColorMode() {
		return this.mColorMode;
	}

	public short getSettingsLevel() {
		return this.settingsLevel;
	}

	public void setSettingsLevel(int level) {
		this.settingsLevel = (short) level;
	}

	public SettingsDBHelper getSettingsHelper() {
		return this.mSettingsHelper;
	}

	public boolean getPlay() {
		return this.play;
	}

	public void setPlay(boolean play) {
		this.play = play;
	}

	public boolean getNormalized() {
		return this.normalized;
	}

	public void setNormalized(boolean normalized) {
		this.normalized = normalized;
	}
}
