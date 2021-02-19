package net.videosc.utilities;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import net.videosc.activities.VideOSCMainActivity;

import java.util.ArrayList;
import java.util.HashMap;

import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
//import oscP5.OscStatus;

/**
 * Created by stefan on 15.07.17, package net.videosc.utilities, project VideOSC22.
 */
public class VideOSCOscHandler/* implements OscEventListener*/ {
	final private static String TAG = "VideOSCOscHandler";

	private final OscP5 mTcpListener, mUdpListener;
	private final VideOSCMainActivity mActivity;
	//	private NetAddress mBroadcastAddr;
//	private NetAddress mFeedbackAddr;
//	private String mBroadcastIP = "192.168.1.1"; // default IP, updated via settings
	private final int mUDPListeningPort = 32000; // default port to listen on messages sent over UDP, updated via settings
	private final int mTCPListeningPort = 32100; // default port to listen on messages sent over TCP/IP, updated via settings
//	private int mBroadcastPort = 57120; // default port to send to, updated via settings
	private final HashMap<Integer, OscP5> mBroadcastAddresses = new HashMap<>();
	private OscEventListener mOscEventListener;

	private final SparseArray<ArrayList<String>> mFbStringsR = new SparseArray<>();
	private final SparseArray<ArrayList<String>> mFbStringsG = new SparseArray<>();
	private final SparseArray<ArrayList<String>> mFbStringsB = new SparseArray<>();

	public VideOSCOscHandler(VideOSCMainActivity activity) {
		this.mActivity = activity;
		this.mUdpListener = new OscP5(activity, mUDPListeningPort, OscP5.UDP);
		this.mTcpListener = new OscP5(activity, mTCPListeningPort, OscP5.TCP);
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

	/* public OscBundle makeBundle(OscBundle bundle) {
		if (bundle == null) {
			bundle = new OscBundle();
		}

		return bundle;
	} */

	public OscP5 getOscP5(int key) {
		return mBroadcastAddresses.get(key);
	}

	public void addBroadcastAddr(int key, OscP5 oscP5) {
		mBroadcastAddresses.put(key, oscP5);
	}

	public void addOscUdpEventListener() {
		mOscEventListener = new OscEventListener() {
			@Override
			public void oscEvent(OscMessage oscMessage) {
				Log.d(TAG, "osc udp message: " + oscMessage);
				createOscFeedbackStrings(oscMessage);
			}
		};
		mUdpListener.addListener(mOscEventListener);
	}

	public void addOscTcpEventListener() {
		mOscEventListener = new OscEventListener() {
			@Override
			public void oscEvent(OscMessage oscMessage) {
				Log.d(TAG, "osc tcp message: " + oscMessage);
				createOscFeedbackStrings(oscMessage);
			}
		};
		mTcpListener.addListener(mOscEventListener);
	}

	public void removeOscUdpEventListener() {
		mUdpListener.removeListener(mOscEventListener);
	}

	public void removeOscTcpEventListener() {
		mTcpListener.removeListener(mOscEventListener);
	}

	public void setBroadcastAddr(int key, OscP5 oscP5) {
		mBroadcastAddresses.put(key, oscP5);
	}

	public OscP5 getBroadcastAddr(int key) {
		return mBroadcastAddresses.get(key);
	}

/*
	public void setBroadcastIP(String ip) {
		mBroadcastIP = ip;
	}

	public String getBroadcastIP() {
		return mBroadcastIP;
	}

	public void setBroadcastPort(int port) {
		mBroadcastPort = port;
	}

	public int getBroadcastPort() {
		return mBroadcastPort;
	}
*/

	public SparseArray<ArrayList<String>> getRedFeedbackStrings() {
		return mFbStringsR;
	}

	public SparseArray<ArrayList<String>> getGreenFeedbackStrings() {
		return mFbStringsG;
	}

	public SparseArray<ArrayList<String>> getBlueFeedbackStrings() {
		return mFbStringsB;
	}

	public void resetFeedbackStrings() {
		mFbStringsR.clear();
		mFbStringsG.clear();
		mFbStringsB.clear();
	}

	private void createOscFeedbackStrings(@NonNull OscMessage fbMessage) {
		if (fbMessage.addrPattern().matches(
				"^/[a-zA-Z0-9_/]+/(red|green|blue)[0-9]+/name"
		) && fbMessage.get(0) != null) {
			String sender = fbMessage.get(0).stringValue();
			String pixel = fbMessage.addrPattern().split("/")[2];
			int index = Integer.parseInt(pixel.replaceAll("^\\D+", "")) - 1;

			if (pixel.matches("^red[0-9]+")) {
				if (mFbStringsR.get(index) == null)
					mFbStringsR.put(index, new ArrayList<String>());
				if (!mFbStringsR.get(index).contains(sender))
					mFbStringsR.get(index).add(sender);
			} else if (pixel.matches("^green[0-9]+")) {
				if (mFbStringsG.get(index) == null)
					mFbStringsG.put(index, new ArrayList<String>());
				if (!mFbStringsG.get(index).contains(sender))
					mFbStringsG.get(index).add(sender);
			} else if (pixel.matches("^blue[0-9]+")) {
				if (mFbStringsB.get(index) == null)
					mFbStringsB.put(index, new ArrayList<String>());
				if (!mFbStringsB.get(index).contains(sender))
					mFbStringsB.get(index).add(sender);
			}
		}
	}
}
