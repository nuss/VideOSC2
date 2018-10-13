package net.videosc2.utilities;

import android.content.Context;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;

/**
 * Created by stefan on 15.07.17, package net.videosc2.utilities, project VideOSC22.
 */
public class VideOSCOscHandler {
	private OscP5 mOscP5;
	private NetAddress mBroadcastAddr;
	private NetAddress mFeedbackAddr;
	private String mBroadcastIP = "192.168.1.1"; // default IP, updated via settings
	private int mListeningPort = 32000; // default port to listen on, updated via settings
	private int mBroadcastPort = 57120; // default port to send to, updated via settings

	public VideOSCOscHandler(Context context) {
		mOscP5 = new OscP5(context, mListeningPort);
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
}
