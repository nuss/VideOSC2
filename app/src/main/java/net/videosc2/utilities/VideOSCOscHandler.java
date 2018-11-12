package net.videosc2.utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.ArrayList;

import netP5.NetAddress;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;

/**
 * Created by stefan on 15.07.17, package net.videosc2.utilities, project VideOSC22.
 */
public class VideOSCOscHandler/* implements OscEventListener*/ {
	final private static String TAG = "VideOSCOscHandler";

	private OscP5 mOscP5;
	private NetAddress mBroadcastAddr;
	private NetAddress mFeedbackAddr;
	private String mBroadcastIP = "192.168.1.1"; // default IP, updated via settings
	private int mListeningPort = 32000; // default port to listen on, updated via settings
	private int mBroadcastPort = 57120; // default port to send to, updated via settings
	private OscEventListener mOscEventListener;

	private final SparseArray<ArrayList<String>> mFbStringsR = new SparseArray<>();
	private final SparseArray<ArrayList<String>> mFbStringsG = new SparseArray<>();
	private final SparseArray<ArrayList<String>> mFbStringsB = new SparseArray<>();

	public VideOSCOscHandler(Context context) {
		mOscP5 = new OscP5(context, mListeningPort);
		// intermediate - should be invoked through user interaction
		mBroadcastAddr = new NetAddress(mBroadcastIP, mBroadcastPort);
	}

	public OscMessage makeMessage(OscMessage msg, String cmd) {
		if (msg == null) {
			msg = new OscMessage(cmd);
		} else {
			msg.clear();
			msg.setAddrPattern(cmd);
		}

		return msg;
	}

	public OscP5 getOscP5() {
		return mOscP5;
	}

	public void setBroadcastAddr(String ip, int port) {
		mBroadcastIP = ip;
		mBroadcastPort = port;
		mBroadcastAddr = new NetAddress(ip, port);
	}

	public void addOscEventListener() {
		mOscEventListener = new OscEventListener() {
			@Override
			public void oscEvent(OscMessage oscMessage) {
//				Log.d(TAG, "osc message: " + oscMessage);
				createOscFeedbackStrings(oscMessage);
			}

			@Override
			public void oscStatus(OscStatus oscStatus) {

			}
		};
		mOscP5.addListener(mOscEventListener);
	}

	public void removeOscEventListener() {
		mOscP5.removeListener(mOscEventListener);
	}

	public NetAddress getBroadcastAddr() {
		return mBroadcastAddr;
	}

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

	public SparseArray<ArrayList<String>> getRedFeedbackStrings() {
		return mFbStringsR;
	}

	public SparseArray<ArrayList<String>> getGreenFeedbackStrings() {
		return mFbStringsG;
	}

	public SparseArray<ArrayList<String>> getBlueFeedbackStrings() {
		return mFbStringsB;
	}

	private void createOscFeedbackStrings(@NonNull OscMessage fbMessage) {
		if (fbMessage.get(0) != null) {
			String sender = fbMessage.get(0).stringValue();
			if (fbMessage.addrPattern().matches(
					"^/[a-zA-Z0-9_/]+/(red|green|blue)[0-9]+/name"
			)) {
				String pixel = fbMessage.addrPattern().split("/")[2];
				int index = Integer.parseInt(pixel.replaceAll("^\\D+", "")) - 1;

				if (pixel.matches("^red[0-9]+")) {
					if (mFbStringsR.get(index) == null)
						mFbStringsR.put(index, new ArrayList<String>());
					if (mFbStringsR.get(index).indexOf(sender) < 0)
						mFbStringsR.get(index).add(sender);
				} else if (pixel.matches("^green[0-9]+")) {
					if (mFbStringsG.get(index) == null)
						mFbStringsG.put(index, new ArrayList<String>());
					if (mFbStringsG.get(index).indexOf(sender) < 0)
						mFbStringsG.get(index).add(sender);
				} else if (pixel.matches("^blue[0-9]+")) {
					if (mFbStringsB.get(index) == null)
						mFbStringsB.put(index, new ArrayList<String>());
					if (mFbStringsB.get(index).indexOf(sender) < 0)
						mFbStringsB.get(index).add(sender);
				}
			}
		}
	}
}
