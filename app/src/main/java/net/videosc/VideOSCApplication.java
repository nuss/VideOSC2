package net.videosc;

import android.app.Application;
import android.graphics.Point;
import android.util.SparseArray;

import net.netP5android.NetAddress;
import net.videosc.db.SettingsDBHelper;
import net.videosc.utilities.VideOSCOscHandler;
import net.videosc.utilities.enums.CommandMappingsSortModes;
import net.videosc.utilities.enums.InteractionModes;
import net.videosc.utilities.enums.PixelEditModes;
import net.videosc.utilities.enums.RGBModes;

import java.util.HashMap;

/**
 * Created by stefan on 05.07.17, package net.videosc, project VideOSC22.
 */
public class VideOSCApplication extends Application {
	private boolean mIsRGBPositive = true; // always init to true
	private boolean mRGBHasChanged = false; // when switching RGB mode from negative to positive and vice versa
	private RGBModes mColorMode = RGBModes.RGB;
	private boolean mIsInSliderGroupEditMode = false;
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
	private boolean mIsTablet;
	private InteractionModes mInterActionMode = InteractionModes.BASIC;
	private final SparseArray<NetAddress> mBroadcastClients = new SparseArray<>();
	private final HashMap<String, Integer> mBroadcastClientKeys = new HashMap<>();

	public Point mDimensions;

	private boolean mIsTorchOn = false;
	private boolean mIsFPSCalcPanelOpen = false;
	private boolean mCameraOSCisPlaying;  // send pixel values via OSC
	private int mCurrentCameraId;
	private Point mResolution;
	private boolean mIsMultiSliderActive;
	private boolean mIndicatorPanelOpen;
	private Point mPixelSize;
	private PixelEditModes mPixelEditMode;
	private VideOSCOscHandler mOscHelper;
	private boolean mOSCFeedbackActivated = false;

	private CommandMappingsSortModes mCommandMappingsSortMode = CommandMappingsSortModes.SORT_BY_COLOR;
	private SparseArray<String> mCommandMappings;

	private int mSettingsContainerID = -1;
	private int mNetworkSettingsID = -1;
	private int mResolutionSettingsID = -1;
	private int mSensorSettingsID = -1;
	private int mDebugSettingsID = -1;
	private int mAboutSettingsID = -1;
	private int mCommandMappingsID = -1;

	@Override
	public void onCreate() {
		super.onCreate();
		// rather than initializing SettingsDBHelper statically retrieve
		// settingsHelper instance with getSettingshelper (no memory leaks)
		this.mSettingsHelper = new SettingsDBHelper(this);
		this.mOscHelper = new VideOSCOscHandler(this);
	}

	public SparseArray<NetAddress> getBroadcastClients() {
		return this.mBroadcastClients;
	}

	public void putBroadcastClient(int key, NetAddress client) {
		this.mBroadcastClients.put(key, client);
	}

	public void removeBroadcastClient(int key) {
		this.mBroadcastClients.delete(key);
	}

	public NetAddress getBroadcastClient(int key) {
		return this.mBroadcastClients.get(key);
	}

	public void putBroadcastClientKeys(String netAddressString, int key) {
		this.mBroadcastClientKeys.put(netAddressString, key);
	}

	public void setOscHelper(VideOSCOscHandler oscHelper) {
		this.mOscHelper = oscHelper;
	}

	public VideOSCOscHandler getOscHelper() {
		return this.mOscHelper;
	}

	public void setCommandMappings(SparseArray<String> mappings) {
		this.mCommandMappings = mappings;
	}

	public void addCommandMappings(int key, String mappings) {
		this.mCommandMappings.put(key, mappings);
	}

	public void removeCommandMappingsAt(int key) {
		this.mCommandMappings.delete(key);
	}

	public SparseArray<String> getCommandMappings() {
		return this.mCommandMappings;
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

	public void setIsMultiSliderActive(boolean active) {
		this.mIsMultiSliderActive = active;
	}

	public boolean getIsMultiSliderActive() {
		return this.mIsMultiSliderActive;
	}

	public void setIsIndicatorPanelOpen(boolean open) {
		this.mIndicatorPanelOpen = open;
	}

	public boolean getIsIndicatorPanelOpen() {
		return this.mIndicatorPanelOpen;
	}

	public void setPixeSize(Point size) {
		this.mPixelSize = size;
	}

	public Point getPixelSize() {
		return this.mPixelSize;
	}

	public void setPixelEditMode(PixelEditModes mode) {
		this.mPixelEditMode = mode;
	}

	public PixelEditModes getPixelEditMode() {
		return this.mPixelEditMode;
	}

	public void setRGBHasChanged(boolean changed) {
		this.mRGBHasChanged = changed;
	}

	public boolean getRGBHasChanged() {
		return mRGBHasChanged;
	}

	public void setOSCFeedbackActivated(boolean activated) {
		this.mOSCFeedbackActivated = activated;
	}

	public boolean getOSCFeedbackActivated() {
		return this.mOSCFeedbackActivated;
	}

	public void setIsTablet(boolean isTablet) {
		this.mIsTablet = isTablet;
	}

	public boolean getIsTablet() {
		return this.mIsTablet;
	}

	public void setSettingsContainerID(int id) {
		this.mSettingsContainerID = id;
	}

	public int getSettingsContainerID() {
		return this.mSettingsContainerID;
	}

	public void setNetworkSettingsID(int id) {
		this.mNetworkSettingsID = id;
	}

	public int getNetworkSettingsID() {
		return this.mNetworkSettingsID;
	}

	public void setResolutionSettingsID(int id) {
		this.mResolutionSettingsID = id;
	}

	public int getResolutionSettingsID() {
		return this.mResolutionSettingsID;
	}

	public void setCommandMappingsID(int id) {
		this.mCommandMappingsID = id;
	}

	public int getCommandMappingsID() {
		return this.mCommandMappingsID;
	}

	public void setSensorSettingsID(int id) {
		this.mSensorSettingsID = id;
	}

	public int getSensorSettingsID() {
		return this.mSensorSettingsID;
	}

	public void setDebugSettingsID(int id) {
		this.mDebugSettingsID = id;
	}

	public int getDebugSettingsID() {
		return this.mDebugSettingsID;
	}

	public void setAboutSettingsID(int id) {
		this.mAboutSettingsID = id;
	}

	public int getAboutSettingsID() {
		return this.mAboutSettingsID;
	}

	public void setCommandMappingsSortmode(CommandMappingsSortModes sortMode) {
		this.mCommandMappingsSortMode = sortMode;
	}

	public CommandMappingsSortModes getCommandMappingsSortMode() {
		return this.mCommandMappingsSortMode;
	}

	public void setSliderGroupEditMode(boolean active) {
		this.mIsInSliderGroupEditMode = active;
	}

	public boolean getSliderGroupEditMode() {
		return this.mIsInSliderGroupEditMode;
	}
}
