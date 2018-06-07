package net.videosc2;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.squareup.leakcanary.LeakCanary;

import net.videosc2.db.SettingsDBHelper;
import net.videosc2.utilities.VideOSCOscHandler;
import net.videosc2.utilities.enums.InteractionModes;
import net.videosc2.utilities.enums.RGBModes;

import oscP5.OscMessage;

/**
 * Created by stefan on 05.07.17, package net.videosc2, project VideOSC22.
 */
public class VideOSCApplication extends Application {
	private boolean mIsRGBPositive = true; // always init to true
	private RGBModes mColorMode = RGBModes.RGB;
	private SettingsDBHelper mSettingsHelper;
	private boolean mNormalized = false;
	private boolean mHidePixelImage = false;
	private static boolean mDebugPixelOsc = false;
	private boolean mExposureIsFixed = false;
	private boolean mExposureSettingHasBeenCancelled = false;
	private boolean mBackPressed;
	private float mDensity;
	private boolean mIsColorModePanelOpen = false;
	private boolean mHasTorch;
	private InteractionModes mInterActionMode = InteractionModes.BASIC;
	public VideOSCOscHandler mOscHelper;
	public Point mDimensions;

	// setting dialogs
	// levels within settings dialog
	// 0: no dialog, normal mode
	// 1: first level - selections 'network settings', 'resolution settings', 'sensor settings', 'debug settings', 'about'
	// 2: editor setting details
	// 3: beyond details - e.g. setting exposure lock
	private short settingsLevel = 0;
	private boolean mIsTorchOn = false;
	private boolean mIsFPSCalcPanelOpen = false;
	private boolean mCameraOSCisPlaying;  // send pixel values via OSC
	private int mCurrentCameraId;
	private Point mResolution;

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
		this.mIsRGBPositive = boolVal;
	}

	public boolean getIsRGBPositive() {
		return this.mIsRGBPositive;
	}

	public void setColorMode(RGBModes mColorMode) {
		this.mColorMode = mColorMode;
	}

	public RGBModes getColorMode() {
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

	public boolean getNormalized() {
		return this.mNormalized;
	}

	public void setNormalized(boolean normalized) {
		this.mNormalized = normalized;
	}

	public boolean getPixelImageHidden() {
		return this.mHidePixelImage;
	}

	public void setPixelImageHidden(boolean hide) {
		this.mHidePixelImage = hide;
	}

	public static boolean getDebugPixelOsc() {
		return mDebugPixelOsc;
	}

	public static void setDebugPixelOsc(boolean debug) {
		mDebugPixelOsc = debug;
	}

	public boolean getExposureIsFixed() {
		return this.mExposureIsFixed;
	}

	public void setExposureIsFixed(boolean fixed) {
		this.mExposureIsFixed = fixed;
	}

	public boolean getHasExposureSettingBeenCancelled() {
		return mExposureSettingHasBeenCancelled;
	}

	public void setHasExposureSettingBeenCancelled(boolean cancel) {
		mExposureSettingHasBeenCancelled = cancel;
	}

	public boolean getBackPressed() {
		return mBackPressed;
	}

	public void setBackPressed(boolean backPressed) {
		mBackPressed = backPressed;
	}

	public Point getDimensions() {
		return mDimensions;
	}

	public void setDimensions(Point dimensions) {
		mDimensions = dimensions;
	}

	public void setScreenDensity(float density) {
		mDensity = density;
	}

	public float getScreenDensity() {
		return mDensity;
	}

	public void setIsColorModePanelOpen(boolean open) {
		this.mIsColorModePanelOpen = open;
	}

	public boolean getIsColorModePanelOpen() {
		return this.mIsColorModePanelOpen;
	}

	public void setIsTorchOn(boolean isTorchOn) {
		this.mIsTorchOn = isTorchOn;
	}

	public boolean getIsTorchOn() {
		return this.mIsTorchOn;
	}

	public void setIsFPSCalcPanelOpen(boolean panelOpen) {
		this.mIsFPSCalcPanelOpen = panelOpen;
	}

	public boolean getIsFPSCalcPanelOpen() {
		return this.mIsFPSCalcPanelOpen;
	}

	public void setInteractionMode(InteractionModes mode) {
		this.mInterActionMode = mode;
	}

	public InteractionModes getInteractionMode() {
		return this.mInterActionMode;
	}

	public void setHasTorch(boolean hasTorch) {
		this.mHasTorch = hasTorch;
	}

	public boolean getHasTorch() {
		return this.mHasTorch;
	}

	public void setCameraOSCisPlaying(boolean playing) {
		this.mCameraOSCisPlaying = playing;
	}

	public boolean getCameraOSCisPlaying() {
		return this.mCameraOSCisPlaying;
	}

	public void setCurrentCameraId(int id) {
		this.mCurrentCameraId = id;
	}

	public int getCurrentCameraId() {
		return this.mCurrentCameraId;
	}

	public void setResolution(Point resolution) {
		this.mResolution = resolution;
	}

	public Point getResolution() {
		return this.mResolution;
	}
}
