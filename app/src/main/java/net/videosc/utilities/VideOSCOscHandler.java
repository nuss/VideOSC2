package net.videosc.utilities;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import net.oscP5android.OscEventListener;
import net.oscP5android.OscMessage;
import net.oscP5android.OscP5;
import net.oscP5android.OscProperties;
import net.videosc.VideOSCApplication;

import java.util.ArrayList;
//import oscP5.OscStatus;

/**
 * Created by stefan on 15.07.17, package net.videosc.utilities, project VideOSC22.
 */
public class VideOSCOscHandler/* implements OscEventListener*/ {
	final private static String TAG = "VideOSCOscHandler";

	private final OscP5 mTcpListener, mUdpListener;
//	private final VideOSCMainActivity mActivity;
	//	private NetAddress mBroadcastAddr;
//	private NetAddress mFeedbackAddr;
//	private String mBroadcastIP = "192.168.1.1"; // default IP, updated via settings
	private static int mUDPListeningPort = 32000; // default port to listen on messages sent over UDP, updated via settings
	private static int mTCPListeningPort = 32100; // default port to listen on messages sent over TCP/IP, updated via settings
	private final VideOSCApplication mApp;
	//	private int mBroadcastPort = 57120; // default port to send to, updated via settings
//	private final HashMap<Integer, OscP5> mBroadcastAddresses = new HashMap<>();
	private OscEventListener mUdpEventListener, mTcpEventListener;

	private final SparseArray<SparseArray<ArrayList<String>>> mFbStringsR = new SparseArray<>();
	private final SparseArray<SparseArray<ArrayList<String>>> mFbStringsG = new SparseArray<>();
	private final SparseArray<SparseArray<ArrayList<String>>> mFbStringsB = new SparseArray<>();

	private final ArrayList<String> mUdpFbBroadcasters = new ArrayList<>();

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

	/* public OscBundle makeBundle(OscBundle bundle) {
		if (bundle == null) {
			bundle = new OscBundle();
		}

		return bundle;
	} */

//	public OscP5 getOscP5(int key) {
//		return mBroadcastAddresses.get(key);
//	}

//	public HashMap<Integer, OscP5> getBroadcastAddresses() {
//		return mBroadcastAddresses;
//	}

//	public void addBroadcastAddr(int key, OscP5 oscP5) {
//		mBroadcastAddresses.put(key, oscP5);
//	}

	public void addOscUdpEventListener() {
		mUdpEventListener = new OscEventListener() {
			@Override
			public void oscEvent(OscMessage oscMessage) {
				Log.d(TAG, "osc udp message: " + oscMessage.hostNetAddressName());
				final String fbBroadcasterName = oscMessage.hostNetAddressName();
				if (!mUdpFbBroadcasters.contains(fbBroadcasterName)) {
					mUdpFbBroadcasters.add(fbBroadcasterName);
				}
				createOscFeedbackStrings(oscMessage, mUdpFbBroadcasters.indexOf(fbBroadcasterName) + 1);
			}
		};
		mUdpListener.addListener(mUdpEventListener);
	}

	public void addOscTcpEventListener() {
		mTcpEventListener = new OscEventListener() {
			@Override
			public void oscEvent(OscMessage oscMessage) {
				Log.d(TAG, "osc tcp message: " + oscMessage);
				createOscFeedbackStrings(oscMessage, mApp.getBroadcastClientKey(oscMessage.hostNetAddressName()));
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

//	public void setBroadcastAddr(int key, OscP5 oscP5) {
//		mBroadcastAddresses.put(key, oscP5);
//	}

//	public OscP5 getBroadcastAddr(int key) {
//		return mBroadcastAddresses.get(key);
//	}

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

	public SparseArray<SparseArray<ArrayList<String>>> getRedFeedbackStrings() {
		return mFbStringsR;
	}

	public SparseArray<SparseArray<ArrayList<String>>> getGreenFeedbackStrings() {
		return mFbStringsG;
	}

	public SparseArray<SparseArray<ArrayList<String>>> getBlueFeedbackStrings() {
		return mFbStringsB;
	}

	public void resetFeedbackStrings() {
		mFbStringsR.clear();
		mFbStringsG.clear();
		mFbStringsB.clear();
	}

	private void createOscFeedbackStrings(@NonNull OscMessage fbMessage, int clientId) {
		if (fbMessage.getAddress().matches(
				"^/[a-zA-Z0-9_/]+/(red|green|blue)[0-9]+/name"
		) && fbMessage.get(0) != null) {
			final String fbText = String.valueOf(fbMessage.get(0));
			Log.d(TAG, "sender: " + fbText);
			final String pixel = fbMessage.getAddress().split("/")[2];
			final int index = Integer.parseInt(pixel.replaceAll("^\\D+", "")) - 1;

/*
			if (mFbStringOccurences.get(index) == null) {
				mFbStringOccurences.put(index, new HashMap<String, HashMap<String, Integer>>());
			}
*/
			/*HashMap<String, HashMap<String, Integer>> indexMap = mFbStringOccurences.get(index);
			if (indexMap.get(netAddress) == null) {
				indexMap.put(netAddress, new HashMap<String, Integer>());
			}
			HashMap<String, Integer> cmdMap = indexMap.get(netAddress);
			if (cmdMap.get(sender) == null) {
				cmdMap.put(sender, 1);
			} else {
				int val = cmdMap.get(sender);
				cmdMap.put(sender, val++);
			}*/

			if (pixel.matches("^red[0-9]+")) {
				if (mFbStringsR.get(index) == null) {
					final SparseArray<ArrayList<String>> clientStringsR = new SparseArray<>();
					mFbStringsR.put(index, clientStringsR);
				}
				assert mFbStringsR.get(index) != null;
				if (mFbStringsR.get(index).get(clientId) == null) {
					mFbStringsR.get(index).put(clientId, new ArrayList<String>());
				}
				if (!mFbStringsR.get(index).get(clientId).contains(fbText)) {
					mFbStringsR.get(index).get(clientId).add(fbText);
				}
			} else if (pixel.matches("^green[0-9]+")) {
				if (mFbStringsG.get(index) == null) {
					final SparseArray<ArrayList<String>> clientStringsG = new SparseArray<>();
					mFbStringsG.put(index, clientStringsG);
				}
				assert mFbStringsG.get(index) != null;
				if (mFbStringsG.get(index).get(clientId) == null) {
					mFbStringsG.get(index).put(clientId, new ArrayList<String>());
				}
				if (!mFbStringsG.get(index).get(clientId).contains(fbText)) {
					mFbStringsG.get(index).get(clientId).add(fbText);
				}
			} else if (pixel.matches("^blue[0-9]+")) {
				if (mFbStringsB.get(index) == null) {
					final SparseArray<ArrayList<String>> clientStringsB = new SparseArray<>();
					mFbStringsB.put(index, clientStringsB);
				}
				assert mFbStringsB.get(index) != null;
				if (mFbStringsB.get(index) != null && mFbStringsB.get(index).get(clientId) == null) {
					mFbStringsB.get(index).put(clientId, new ArrayList<String>());
				}
				if (!mFbStringsB.get(index).get(clientId).contains(fbText)) {
					mFbStringsB.get(index).get(clientId).add(fbText);
				}
			}
//			if (index == 3) {
//				Log.d(TAG, "feedback strings for " + pixel + ": " + mFbStringsR);
//			}
		}
	}
}
