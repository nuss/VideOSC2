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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by stefan on 15.07.17, package net.videosc.utilities, project VideOSC22.
 */
public class VideOSCOscHandler implements Closeable {
    final private static String TAG = "VideOSCOscHandler";

    private OscP5 mTcpListener, mUdpListener;
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
    }

    public void createListeners(int udpPort, int tcpPort) {
        this.mUdpListener = new OscP5(this.mApp, udpPort, OscProperties.UDP);
        this.mTcpListener = new OscP5(this.mApp, tcpPort, OscProperties.TCP);
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

/*
    public void setUdpListenerPort(int port) {
        final OscProperties props = this.mUdpListener.getProperties().setListeningPort(port);
        this.mUdpListener.setProperties(props);
//        mUDPListeningPort = port;
    }

    public void setTcpListenerPort(int port) {
        final OscProperties props = this.mTcpListener.getProperties().setListeningPort(port);
        this.mTcpListener.setProperties(props);
//        mTCPListeningPort = port;
    }
*/

    public void addOscUdpEventListener() {
        mUdpEventListener = oscMessage -> {
            final String fbBroadcasterName = oscMessage.hostNetAddressName();
            final int numSlots = mApp.getResolution().x * mApp.getResolution().y;

            if (!mUdpFbBroadcasters.contains(fbBroadcasterName)) {
                mUdpFbBroadcasters.add(fbBroadcasterName);
            }

            createOrAdjustFeedbackStringArrays(mFbStringsR, mThreshesR, numSlots);
            createOrAdjustFeedbackStringArrays(mFbStringsG, mThreshesG, numSlots);
            createOrAdjustFeedbackStringArrays(mFbStringsB, mThreshesB, numSlots);

            createOscFeedbackStrings(oscMessage, mUdpFbBroadcasters.indexOf(fbBroadcasterName) + 1);
        };
        mUdpListener.addListener(mUdpEventListener);
    }

    private void createOrAdjustFeedbackStringArrays(ArrayList<SparseArray<String>> feedBackStrings, ArrayList<SparseIntArray> threshes, int numSlots) {
        final int numStringSlots = feedBackStrings.size();

        if (numStringSlots == 0) {
            for (int i = 0; i < numSlots; i++) {
                feedBackStrings.add(i, new SparseArray<>());
                threshes.add(i, new SparseIntArray());
            }
        } else if (numStringSlots > numSlots) {
            feedBackStrings.subList(numSlots, numStringSlots).clear();
            threshes.subList(numSlots, numStringSlots).clear();
        } else if (numStringSlots < numSlots) {
            for (int i = numStringSlots; i < numSlots; i++) {
                feedBackStrings.add(i, new SparseArray<>());
                threshes.add(i, new SparseIntArray());
            }
        }
    }

    public void addOscTcpEventListener() {
        mTcpEventListener = oscMessage -> {
            Log.d(TAG, "osc tcp message: " + oscMessage);
//				createOscFeedbackStrings(oscMessage, mApp.getBroadcastClientKey(oscMessage.hostNetAddressName()));
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

    // return a snapshot of current feedback strings
    public ArrayList<SparseArray<String>> getRedFeedbackStringsSnapshot() {
        final ArrayList<SparseArray<String>> res = new ArrayList<>();
        for (SparseArray<String> slot : mFbStringsR) {
            res.add(slot.clone());
        }
        return res;
    }

    public ArrayList<SparseArray<String>> getGreenFeedbackStringsSnapshot() {
        final ArrayList<SparseArray<String>> res = new ArrayList<>();
        for (SparseArray<String> slot : mFbStringsG) {
            res.add(slot.clone());
        }
        return res;
    }

    public ArrayList<SparseArray<String>> getBlueFeedbackStringsSnapshot() {
        final ArrayList<SparseArray<String>> res = new ArrayList<>();
        for (SparseArray<String> slot : mFbStringsB) {
            res.add(slot.clone());
        }
        return res;
    }

    private void createOscFeedbackStrings(@NonNull OscMessage fbMessage, int clientId) {
        SparseArray<String> fbMsgs, checkFbMsgs;
        SparseIntArray fbThreshes;

        if (fbMessage.getAddress().matches(
                "^/[a-zA-Z0-9_/]+/(red|green|blue)[0-9]+/name"
        ) && fbMessage.get(0) != null) {
            String fbText = clientId + ":" + fbMessage.get(0);
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

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *rem
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        mFbStringsR.clear();
        mThreshesR.clear();
        mFbStringsG.clear();
        mThreshesG.clear();
        mFbStringsB.clear();
        mThreshesB.clear();
        mTcpListener.dispose();
        mUdpListener.dispose();
    }
}
