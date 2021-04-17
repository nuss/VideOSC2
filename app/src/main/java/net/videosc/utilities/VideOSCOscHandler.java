package net.videosc.utilities;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

import net.oscP5android.OscEventListener;
import net.oscP5android.OscMessage;
import net.oscP5android.OscP5;
import net.oscP5android.OscProperties;
import net.videosc.VideOSCApplication;

import java.util.ArrayList;

/**
 * Created by stefan on 15.07.17, package net.videosc.utilities, project VideOSC22.
 */
public class VideOSCOscHandler {
	final private static String TAG = "VideOSCOscHandler";

	private final OscP5 mTcpListener, mUdpListener;
	private static int mUDPListeningPort = 32000; // default port to listen on messages sent over UDP, updated via settings
	private static int mTCPListeningPort = 32100; // default port to listen on messages sent over TCP/IP, updated via settings
	private final VideOSCApplication mApp;
	private OscEventListener mUdpEventListener, mTcpEventListener;

	private final ArrayList<String> mUdpFbBroadcasters = new ArrayList<>();

	private final ArrayList<SparseArray<String>> mFbStringsR = new ArrayList<>();
	private final ArrayList<SparseIntArray> mThreshesR = new ArrayList<>();
	private final ArrayList<SparseArray<String>> mFbStringsG = new ArrayList<>();
	private final ArrayList<SparseIntArray> mThreshesG = new ArrayList<>();
	private final ArrayList<SparseArray<String>> mFbStringsB = new ArrayList<>();
	private final ArrayList<SparseIntArray> mThreshesB = new ArrayList<>();

	public VideOSCOscHandler(Context context) {
		this.mApp = (VideOSCApplication) context; 
		this.mUdpListener = new OscP5(context, mUDPListeningPort, OscProperties.UDP);
		this.mTcpListener = new OscP5(context, mTCPListeningPort, OscProperties.TCP);
	}

	public OscMessage makeMessage(OscMessage msg, String cmd) {
		if (msg == null) {
			msg = new OscMessage(cmd);
		} else {
			msg.clear();
			msg.setAddress(cmd);
		}

		return msg;
	}

	public OscP5 getUdpListener() {
		return this.mUdpListener;
	}

	public OscP5 getTcpListener() {
		return this.mTcpListener;
	}

	public static void setUdpListenerPort(int port) {
		mUDPListeningPort = port;
	}

	public static void setTcpListenerPort(int port) {
		mTCPListeningPort = port;
	}

	public void addOscUdpEventListener() {
		final int numSlots = mApp.getResolution().x * mApp.getResolution().y;
		mUdpEventListener = new OscEventListener() {
			@Override
			public void oscEvent(OscMessage oscMessage) {
				final String fbBroadcasterName = oscMessage.hostNetAddressName();

				if (!mUdpFbBroadcasters.contains(fbBroadcasterName)) {
					mUdpFbBroadcasters.add(fbBroadcasterName);
				}

				createOrAdjustFeedbackStringArrays(mFbStringsR, mThreshesR, numSlots);
				createOrAdjustFeedbackStringArrays(mFbStringsG, mThreshesG, numSlots);
				createOrAdjustFeedbackStringArrays(mFbStringsB, mThreshesB, numSlots);

				createOscFeedbackStrings(oscMessage, mUdpFbBroadcasters.indexOf(fbBroadcasterName) + 1);
			}
		};
		mUdpListener.addListener(mUdpEventListener);
	}

	private void createOrAdjustFeedbackStringArrays(ArrayList<SparseArray<String>> feedBackStrings, ArrayList<SparseIntArray> threshes, int numSlots) {
		final int numStringSlots = feedBackStrings.size();

		if (numStringSlots == 0) {
			for (int i = 0; i < numSlots; i++) {
				feedBackStrings.add(i, new SparseArray<String>());
				threshes.add(i, new SparseIntArray());
			}
		} else if (numStringSlots > numSlots) {
			if (numStringSlots > numStringSlots - numSlots) {
				feedBackStrings.subList(numStringSlots - numSlots, numStringSlots).clear();
				threshes.subList(numStringSlots - numSlots, numStringSlots).clear();
			}
		} else if (numStringSlots < numSlots) {
			for (int i = numStringSlots; i < numSlots; i++) {
				feedBackStrings.add(i, new SparseArray<String>());
				threshes.add(i, new SparseIntArray());
			}
		}
	}

	public void addOscTcpEventListener() {
		mTcpEventListener = new OscEventListener() {
			@Override
			public void oscEvent(OscMessage oscMessage) {
				Log.d(TAG, "osc tcp message: " + oscMessage);
//				createOscFeedbackStrings(oscMessage, mApp.getBroadcastClientKey(oscMessage.hostNetAddressName()));
			}
		};
		mTcpListener.addListener(mTcpEventListener);
	}

	public void removeOscUdpEventListener() {
		mUdpListener.removeListener(mUdpEventListener);
	}

	public void removeOscTcpEventListener() {
		mTcpListener.removeListener(mTcpEventListener);
	}


	public ArrayList<SparseArray<String>> getRedFeedbackStrings() {
		return mFbStringsR;
	}

	public ArrayList<SparseArray<String>> getGreenFeedbackStrings() {
		return mFbStringsG;
	}

	public ArrayList<SparseArray<String>> getBlueFeedbackStrings() {
		return mFbStringsB;
	}

	public ArrayList<SparseIntArray> getRedThresholds() {
		return mThreshesR;
	}

	public ArrayList<SparseIntArray> getGreenThresholds() {
		return mThreshesG;
	}

	public ArrayList<SparseIntArray> getBlueThresholds() {
		return mThreshesB;
	}

	private void createOscFeedbackStrings(@NonNull OscMessage fbMessage, int clientId) {
		SparseArray<String> fbMsgs, checkFbMsgs;
		SparseIntArray fbThreshes;

		if (fbMessage.getAddress().matches(
				"^/[a-zA-Z0-9_/]+/(red|green|blue)[0-9]+/name"
		) && fbMessage.get(0) != null) {
			String fbText = clientId + ": " + fbMessage.get(0);
			final String pixel = fbMessage.getAddress().split("/")[2];
			final int index = Integer.parseInt(pixel.replaceAll("^\\D+", "")) - 1;

			if (pixel.matches("^red[0-9]+")) {
				fbMsgs = mFbStringsR.get(index);
				fbThreshes = mThreshesR.get(index);
				checkAndAddText(fbMsgs, fbThreshes, fbText);
			} else if (pixel.matches("^green[0-9]+")) {
				fbMsgs = mFbStringsG.get(index);
				fbThreshes = mThreshesG.get(index);
				checkAndAddText(fbMsgs, fbThreshes, fbText);
			} else if (pixel.matches("^blue[0-9]+")) {
				fbMsgs = mFbStringsB.get(index);
				fbThreshes = mThreshesB.get(index);
				checkAndAddText(fbMsgs, fbThreshes, fbText);
			}
		}
	}

	private void checkAndAddText(@NonNull SparseArray<String> list, @NonNull SparseIntArray threshes, @NonNull String text) {
		final int index = list.indexOfValue(text.intern());
		if (index < 0) {
			final int i = list.size();
			list.put(i, text.intern());
			threshes.put(i, 100);
		} else {
			final int thresh = threshes.valueAt(index);
			final int key = threshes.keyAt(index);
			if (thresh < 100) {
				threshes.delete(key);
				threshes.put(key, thresh + 1);
			}
		}
	}
}
