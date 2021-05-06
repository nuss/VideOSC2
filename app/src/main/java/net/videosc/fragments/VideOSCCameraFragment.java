/*
 * Display the down-scaled preview, calculated
 * from the smallest possible preview size
 * Created by Stefan Nussbaumer
 * after a piece of code by
 * Rex St. John (on behalf of AirPair.com) on 3/4/14.
 *
 * Copyright (c) 2014 Rex St. John on behalf of AirPair.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.videosc.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import net.netP5android.NetAddress;
import net.oscP5android.OscBundle;
import net.oscP5android.OscMessage;
import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.db.SettingsContract;
import net.videosc.utilities.VideOSCDialogHelper;
import net.videosc.utilities.VideOSCOscHandler;
import net.videosc.utilities.VideOSCUIHelpers;
import net.videosc.utilities.enums.InteractionModes;
import net.videosc.utilities.enums.PixelEditModes;
import net.videosc.utilities.enums.RGBModes;
import net.videosc.views.TileOverlayView;
import net.videosc.views.VideOSCMultiSliderView;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;

public class VideOSCCameraFragment extends VideOSCBaseFragment {
    private final static String TAG = "VideOSCCameraFragment";

    private long mNow, mPrev = 0;
    private float mFrameRate;

    // Native camera.
    public Camera mCamera;
    private Camera.Parameters mCameraParams;
    private List<int[]> mSupportedPreviewFpsRanges;


    // View to display the camera output.
    public CameraPreview mPreview;
    // the toolsDrawer on the right
    private DrawerLayout mToolsDrawer;

    // Reference to the ImageView containing the downscaled video frame
    private ImageView mImage;

    private float mCamZoom = 1f;

    private int[] mFrameRateRange;

    private String mRed, mGreen, mBlue;

    private VideOSCApplication mApp;
//	private static OscP5 mOscP5;

    // pixels set by multislider
    // these arrays shouldn't get get reinitialized
    // when switching the camera
    final private ArrayList<Double> mRedValues = new ArrayList<>();
    final private ArrayList<Double> mGreenValues = new ArrayList<>();
    final private ArrayList<Double> mBlueValues = new ArrayList<>();

    final private ArrayList<Double> mRedMixValues = new ArrayList<>();
    final private ArrayList<Double> mGreenMixValues = new ArrayList<>();
    final private ArrayList<Double> mBlueMixValues = new ArrayList<>();

    final private ArrayList<Rect> mSelectedPixels = new ArrayList<>();
    final private List<Integer> mPixelIds = new ArrayList<>();
    // lock pixels on select or deselect in pixel edit mode EDIT_PIXELS
    final private ArrayList<Boolean> mLockedPixels = new ArrayList<>();
    final private ArrayList<Double> mPrevRedValues = new ArrayList<>();
    final private ArrayList<Double> mPrevGreenValues = new ArrayList<>();
    final private ArrayList<Double> mPrevBlueValues = new ArrayList<>();

    private SparseArray<Double> mResetRedVals, mResetRedMixVals, mResetGreenVals,
            mResetGreenMixVals, mResetBlueVals, mResetBlueMixVals;

    // must be owned by the fragment - no idea why
    private Bitmap mBmp;
    private TileOverlayView mOverlayView;
    private ViewGroup mPixelEditor;
    private ViewGroup mSnapshotsBar;

    // debugging
    private OscMessage mDebugRed, mDebugGreen, mDebugBlue;

//    private Bitmap debugPrevBmp;

    /**
     * Default empty constructor.
     */
    public VideOSCCameraFragment() {
    }

    public VideOSCCameraFragment(VideOSCMainActivity activity) {
        super();
        this.mActivity = activity;
    }

    /**
     * OnCreateView fragment override
     *
     * @param inflater           the layout inflater inflating the layout for the view
     * @param container          the layout's container
     * @param savedInstanceState a Bundle instance
     * @return a View
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // store the container for later re-use
        this.mContainer = container;
        this.mInflater = inflater;
        return inflater.inflate(R.layout.fragment_native_camera, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mApp = (VideOSCApplication) mActivity.getApplication();
        this.mToolsDrawer = mActivity.mToolsDrawerLayout;
        this.mImage = view.findViewById(R.id.camera_downscaled);

        // Create our Preview view and set it as the content of our activity.
        safeCameraOpenInView(view);
    }

    /**
     * Recommended "safe" way to open the camera.
     *
     * @param view the view on which the camera is going to be displayed to the user
     */
    public void safeCameraOpenInView(View view) {
        FrameLayout preview;
        // cache current zoom
        int zoom = mPreview != null ? mPreview.getCurrentZoom() : 0;
        releaseCameraAndPreview();
        mCamera = getCameraInstance(mApp);
        if (mCamera != null) {
            mCameraParams = mCamera.getParameters();
            mSupportedPreviewFpsRanges = mCameraParams.getSupportedPreviewFpsRange();

            if (mPreview == null) {
                mPreview = new CameraPreview(mActivity, mCamera);
                if (view.findViewById(R.id.camera_preview) != null) {
                    preview = view.findViewById(R.id.camera_preview);
                    preview.addView(mPreview);
                    Log.d(TAG, "view found and set");
                } else Log.d(TAG, "FrameLayout is null");
            } else {
                mPreview.switchCamera(mCamera);
                // set camera zoom to the zoom value of the old camera
                mPreview.setZoom(zoom);
                Log.d(TAG, "switch camera");
            }
        } else {
            VideOSCDialogHelper.showDialog(
                    mActivity,
                    android.R.style.Theme_Holo_Light_Dialog,
                    getString(R.string.msg_on_camera_open_fail),
                    getString(R.string.OK),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }

                    }, null, null
            );
        }
    }

    /**
     * Safe method for getting a camera instance.
     *
     * @return Camera instance
     */
    private static Camera getCameraInstance(VideOSCApplication app) {
        Camera c = null;

        try {
            c = Camera.open(app.getCurrentCameraId()); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        releaseCameraAndPreview();
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        this.mActivity = null;
    }

    /**
     * Clear any existing preview / camera.
     */
    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            // hack: set a null callback as the already set callback
            // otherwise prevails even after camera.release() and
            // causes a crash on quit
            mCamera.setPreviewCallback(null);
            Log.d(TAG, "camera.release now");
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.destroyDrawingCache();
            mPreview.mViewCamera = null;
        }
    }

    public void setColorOscCmds(String cmd) {
        mRed = String.format("/%1$s/red", cmd);
        mGreen = String.format("/%1$s/green", cmd);
        mBlue = String.format("/%1$s/blue", cmd);
    }

    private void setFramerateRange(int index) {
        mFrameRateRange = mSupportedPreviewFpsRanges.get(index);
    }

    private int[] getFramerateRange() {
        return mFrameRateRange;
    }

    public ArrayList<Rect> getSelectedPixels() {
        return this.mSelectedPixels;
    }

    public List<Integer> getPixelNumbers() {
        return this.mPixelIds;
    }

    /* color values */

    public ArrayList<Double> getRedValues() {
        return this.mRedValues;
    }

    public void setRedValues(ArrayList<Double> values, int size) {
        if (size == mRedValues.size()) {
            for (int i = 0; i < size; i++) {
                if (i < values.size())
                    mRedValues.set(i, values.get(i));
                else mRedValues.set(i, null);
            }
        }
    }

    public ArrayList<Double> getRedMixValues() {
        return this.mRedMixValues;
    }

    public void setRedMixValues(ArrayList<Double> values, int size) {
        if (size == mRedMixValues.size()) {
            for (int i = 0; i < size; i++) {
                if (i < values.size())
                    mRedMixValues.set(i, values.get(i));
                else mRedMixValues.set(i, null);
            }
        }
    }

    public ArrayList<Double> getGreenValues() {
        return this.mGreenValues;
    }

    public void setGreenValues(ArrayList<Double> values, int size) {
        if (size == mGreenValues.size()) {
            for (int i = 0; i < size; i++) {
                if (i < values.size())
                    mGreenValues.set(i, values.get(i));
                else mGreenValues.set(i, null);
            }
        }
    }

    public ArrayList<Double> getGreenMixValues() {
        return this.mGreenMixValues;
    }

    public void setGreenMixValues(ArrayList<Double> values, int size) {
        if (size == mGreenMixValues.size()) {
            for (int i = 0; i < size; i++) {
                if (i < values.size())
                    mGreenMixValues.set(i, values.get(i));
                else mGreenMixValues.set(i, null);
            }
        }
    }

    public ArrayList<Double> getBlueValues() {
        return this.mBlueValues;
    }

    public void setBlueValues(ArrayList<Double> values, int size) {
        if (size == mBlueValues.size()) {
            for (int i = 0; i < size; i++) {
                if (i < values.size())
                    mBlueValues.set(i, values.get(i));
                else mBlueValues.set(i, null);
            }
        }
    }

    public ArrayList<Double> getBlueMixValues() {
        return this.mBlueMixValues;
    }

    public void setBlueMixValues(ArrayList<Double> values, int size) {
        if (size == mBlueMixValues.size()) {
            for (int i = 0; i < size; i++) {
                if (i < values.size())
                    mBlueMixValues.set(i, values.get(i));
                else mBlueMixValues.set(i, null);
            }
        }
    }

    public void setRedValue(int index, Double value) {
        mRedValues.set(index, value);
    }

    public void setRedMixValue(int index, Double value) {
        mRedMixValues.set(index, value);
    }

    public void setGreenValue(int index, Double value) {
        mGreenValues.set(index, value);
    }

    public void setGreenMixValue(int index, Double value) {
        mGreenMixValues.set(index, value);
    }

    public void setBlueValue(int index, Double value) {
        mBlueValues.set(index, value);
    }

    public void setBlueMixValue(int index, Double value) {
        mBlueMixValues.set(index, value);
    }

    /* reset values, needed to restore a setup if a pixel edit gets canceled by the user */

    public SparseArray<Double> getRedResetValues() {
        return this.mResetRedVals;
    }

    public SparseArray<Double> getRedMixResetValues() {
        return this.mResetRedMixVals;
    }

    public SparseArray<Double> getGreenResetValues() {
        return this.mResetGreenVals;
    }

    public SparseArray<Double> getGreenMixResetValues() {
        return this.mResetGreenMixVals;
    }

    public SparseArray<Double> getBlueResetValues() {
        return this.mResetBlueVals;
    }

    public SparseArray<Double> getBlueMixResetValues() {
        return this.mResetBlueMixVals;
    }


    /**
     * Surface on which the camera projects it's capture results. This is derived both from Google's docs and the
     * excellent StackOverflow answer provided below.
     * <p>
     * Reference / Credit: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
     */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, VideOSCMSBaseFragment.OnCreateViewCallback {

        private final VideOSCMainActivity mActivity;
        private final ArrayList<OscBundle> mOscBundlesR, mOscBundlesG, mOscBundlesB;
        private final VideOSCOscHandler mOscHelper;
        // SurfaceHolder
        private SurfaceHolder mHolder;

        // Our Camera.
        private Camera mViewCamera;

        // Camera Sizing (For rotation, orientation changes)
        private Camera.Size mPreviewSize;

        private double mOldFingerDistance = 0.0;
        private final Point mPixelSize = new Point();

        private RedOscRunnable mRedOscRunnable;
        private GreenOscRunnable mGreenOscRunnable;
        private BlueOscRunnable mBlueOscRunnable;
        private Thread mRedOscSender;
        private Thread mGreenOscSender;
        private Thread mBlueOscSender;

        private SparseArray<NetAddress> mOscClients;

        private final FragmentManager mManager;

        private SparseArray<String> mMappings;

        // debugging
        private long mCountR = 0, mCountG = 0, mCountB = 0;

        /**
         * @param context the context of the application
         * @param camera  an instance of Camera, to be used throughout CameraPreview
         */
        CameraPreview(Context context, Camera camera) {
            super(context);

            this.mActivity = (VideOSCMainActivity) context;
            this.mOscBundlesR = new ArrayList<>();
            this.mOscBundlesG = new ArrayList<>();
            this.mOscBundlesB = new ArrayList<>();
            this.mOscHelper = mApp.getOscHelper();

            Log.d(TAG, "CameraPreview(): " + camera);
            // Capture the context
            setCamera(camera);
            // explicitely trigger drawing (onDraw)
            setWillNotDraw(false);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setKeepScreenOn(true);
            // deprecated setting, but required on Android versions prior to 3.0
//			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            if (mRedOscSender == null) {
                mRedOscRunnable = new RedOscRunnable();
//				RedOscRunnable.setOscHelper(mOscHelper);
                mRedOscSender = new Thread(mRedOscRunnable);
                mRedOscSender.start();
            }

            if (mGreenOscSender == null) {
                mGreenOscRunnable = new GreenOscRunnable();
//				GreenOscRunnable.setOscHelper(mOscHelper);
                mGreenOscSender = new Thread(mGreenOscRunnable);
                mGreenOscSender.start();
            }

            if (mBlueOscSender == null) {
                mBlueOscRunnable = new BlueOscRunnable();
//				BlueOscRunnable.setOscHelper(mOscHelper);
                mBlueOscSender = new Thread(mBlueOscRunnable);
                mBlueOscSender.start();
            }

            // get initial settings from database
            String[] settingsFields = new String[]{
                    SettingsContract.SettingsEntries.RES_H,
                    SettingsContract.SettingsEntries.RES_V,
                    SettingsContract.SettingsEntries.FRAMERATE_RANGE,
                    SettingsContract.SettingsEntries.ROOT_CMD
            };

            final SQLiteDatabase db = ((VideOSCApplication) mActivity.getApplicationContext()).getSettingsHelper().getReadableDatabase();

            Cursor cursor = db.query(
                    SettingsContract.SettingsEntries.TABLE_NAME,
                    settingsFields,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                mApp.setResolution(
                        new Point(
                                cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.RES_H)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.RES_V))
                        )
                );
                setFramerateRange(
                        cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.FRAMERATE_RANGE))
                );
                setColorOscCmds(
                        cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD))
                );
            }

            cursor.close();

            // triplets of booleans, to be added to lockList, offPxls;
            Point res = mApp.getResolution();
            int numPixels = res.x * res.y;

            // TODO: if user has set to start app with settings stored on last quit
            // values should be set to was stored on quit
            for (int i = 0; i < numPixels; i++) {
                if (mRedValues.size() < numPixels)
                    mRedValues.add(null);
                if (mGreenValues.size() < numPixels)
                    mGreenValues.add(null);
                if (mBlueValues.size() < numPixels)
                    mBlueValues.add(null);
                if (mRedMixValues.size() < numPixels)
                    mRedMixValues.add(null);
                if (mGreenMixValues.size() < numPixels)
                    mGreenMixValues.add(null);
                if (mBlueMixValues.size() < numPixels)
                    mBlueMixValues.add(null);

                if (mPrevRedValues.size() < numPixels)
                    mPrevRedValues.add(null);
                if (mPrevGreenValues.size() < numPixels)
                    mPrevGreenValues.add(null);
                if (mPrevBlueValues.size() < numPixels)
                    mPrevBlueValues.add(null);

                // mark deselected pixels
                if (mLockedPixels.size() < numPixels)
                    mLockedPixels.add(false);
            }

            mManager = getFragmentManager();
        }

        /**
         * switch backside to frontside camera and vice versa
         * called within safeCameraOpenInView(View view)
         *
         * @param camera an instance of Camera
         */
        public void switchCamera(Camera camera) {
            Log.d(TAG, "switch camera, mViewCamera: " + camera);
            mViewCamera = camera;
            mHolder.removeCallback(this);
            ViewGroup parent = (ViewGroup) mPreview.getParent();
            // cache new preview locally and remove old preview later
            // removing old preview immediately caused surfaceDestroyed to be called
            // and switching wasn't finished unless when switching back from front- to backside
            // camera...
            CameraPreview preview = new CameraPreview(mActivity, camera);
            SurfaceHolder holder = preview.getHolder();
            holder.addCallback(this);
            parent.addView(preview);
            parent.removeView(mPreview);
            mPreview = preview;
            mHolder = holder;
        }

        /**
         * Extract supported preview and flash modes from the camera.
         *
         * @param camera an instance of Camera
         */
        private void setCamera(Camera camera) {
            mViewCamera = camera;
            mCameraParams = camera.getParameters();
            // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
            // List of supported preview sizes
            List<Camera.Size> mSupportedPreviewSizes = mCameraParams.getSupportedPreviewSizes();
            mPreviewSize = getSmallestPreviewSize(mSupportedPreviewSizes);
            mCameraParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            // Flash modes supported by this camera
            List<String> mSupportedFlashModes = mCameraParams.getSupportedFlashModes();

            // Set the camera to Auto Flash mode.
            if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                mCameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            camera.setParameters(mCameraParams);

            requestLayout();
        }

        /**
         * The Surface has been created, now tell the camera where to draw the preview.
         *
         * @param holder the surface holder
         */
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated: " + mViewCamera);
            final ViewGroup indicatorPanel = mContainer.findViewById(R.id.indicator_panel);

            try {
                mViewCamera.setPreviewDisplay(holder);
                mViewCamera.startPreview();
                View menuButton = mContainer.findViewById(R.id.show_menu);
                if (mApp.getSettingsContainerID() < 0) {
                    menuButton.bringToFront();
                    if (indicatorPanel != null)
                        indicatorPanel.bringToFront();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mOverlayView == null) {
                ViewGroup overlay = (ViewGroup) mInflater.inflate(R.layout.tile_overlay_view, mContainer, false);
                mOverlayView = overlay.findViewById(R.id.tile_draw_view);
                VideOSCUIHelpers.addView(mOverlayView, mContainer);
            }

            mPixelEditor = mActivity.mPixelEditor;
            mSnapshotsBar = mActivity.mBasicToolbar;
            ViewGroup snapshotsBar = mActivity.mBasicToolbar;
            ImageButton applySelection = mPixelEditor.findViewById(R.id.apply_pixel_selection);
            applySelection.setOnClickListener(v -> {
                if (mSelectedPixels.size() > 0) {
                    mSelectedPixels.clear();
                    mSnapshotsBar.setVisibility(View.INVISIBLE);
                    mPixelEditor.setVisibility(View.INVISIBLE);
                    if (mApp.getPixelEditMode().equals(PixelEditModes.EDIT_PIXELS))
                        createMultiSliders();
                }
            });

            if (mApp.getSettingsContainerID() < 0)
                snapshotsBar.bringToFront();
        }

        /**
         * Dispose of the camera preview.
         *
         * @param holder the surface holder
         */
        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed");
            // prevent errors resulting from camera being used after Camera.release() has been
            // called. Seems to work...
            if (mViewCamera != null) try {
                mViewCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * React to surface changed events
         *
         * @param holder the surface holder
         * @param format the pixel format of the surface
         * @param w      the surface width
         * @param h      the surface height
         */
        @Override
        public void surfaceChanged(@NonNull final SurfaceHolder holder, int format, final int w, final int h) {
            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            final ViewGroup colorModePanel = mContainer.findViewById(R.id.color_mode_panel);
            final ViewGroup fpsRateCalcPanel = mContainer.findViewById(R.id.fps_calc_period_indicator);
            final ViewGroup indicators = mContainer.findViewById(R.id.indicator_panel);

            // memorize current pixel size
            setPixelSize(holder);

            if (mApp.getIsMultiSliderActive()) {
                if (mApp.getIsFPSCalcPanelOpen())
                    VideOSCUIHelpers.removeView(fpsRateCalcPanel, mContainer);
                if (mApp.getIsColorModePanelOpen())
                    VideOSCUIHelpers.removeView(colorModePanel, mContainer);
                if (mApp.getIsIndicatorPanelOpen()) {
                    mApp.setIsIndicatorPanelOpen(VideOSCUIHelpers.removeView(indicators, mContainer));
                }
            }

            // stop preview before making changes
            try {
                final Camera.Parameters parameters = mViewCamera.getParameters();
                int[] frameRates = getFramerateRange();
                parameters.setPreviewFpsRange(frameRates[0], frameRates[1]);

                mViewCamera.setParameters(parameters);
                mViewCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Point resolution = mApp.getResolution();
                        int previewSize = resolution.x * resolution.y;
                        mOscClients = mApp.getBroadcastClients();
                        mMappings = mApp.getCommandMappings();
                        int diff = previewSize - mRedValues.size();
                        if (diff != 0) pad(diff);
                        mNow = System.currentTimeMillis();
                        mFrameRate = Math.round(1000.0f / (mNow - mPrev) * 10.0f) / 10.0f;
                        mPrev = mNow;
                        TextView frameRateText = mContainer.findViewById(R.id.fps);
                        if (frameRateText != null)
                            frameRateText.setText(String.format(Locale.getDefault(), "%.1f", mFrameRate));
                        TextView zoomText = mContainer.findViewById(R.id.zoom);
                        if (zoomText != null)
                            zoomText.setText(String.format(Locale.getDefault(), "%.1f", mCamZoom));
                        Bitmap.Config inPreferredConfig = Bitmap.Config.ARGB_8888;
                        int[] out = new int[mPreviewSize.width * mPreviewSize.height];
                        GPUImageNativeLibrary.YUVtoRBGA(data, mPreviewSize.width, mPreviewSize.height, out);
                        Bitmap bmp = Bitmap.createBitmap(mPreviewSize.width, mPreviewSize.height, inPreferredConfig);
                        bmp.copyPixelsFromBuffer(IntBuffer.wrap(out));
                        mBmp = drawFrame(Bitmap.createScaledBitmap(bmp, resolution.x, resolution.y, true), resolution.x, resolution.y);
//                        Log.d(TAG, "\nnew bitmap is identical to old bitmap: " + (mBmp.sameAs(debugPrevBmp)) + "\n");
//                        debugPrevBmp = mBmp;
                        BitmapDrawable bmpDraw = new BitmapDrawable(getResources(), mBmp);
                        bmpDraw.setAntiAlias(false);
                        bmpDraw.setDither(false);
                        bmpDraw.setFilterBitmap(false);
                        mImage.bringToFront();
                        mImage.setImageDrawable(bmpDraw);
                        Point dimensions = mApp.getDimensions();
                        // provide neccessary information for overlay
                        mOverlayView.setRedMixValues(mRedMixValues);
                        mOverlayView.setGreenMixValues(mGreenMixValues);
                        mOverlayView.setBlueMixValues(mBlueMixValues);

                        mOverlayView.layout(0, 0, dimensions.x, dimensions.y);
                        mOverlayView.invalidate();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * @param sizes the ArrayList of possible preview sizes
         * @return the smallest possible preview size
         */
        private Camera.Size getSmallestPreviewSize(List<Camera.Size> sizes) {
            Camera.Size optimalSize;
            ArrayList<Integer> productList = new ArrayList<>();

            for (Camera.Size size : sizes) {
                productList.add(size.width * size.height);
            }

            int minIndex = productList.indexOf(Collections.min(productList));
            optimalSize = sizes.get(minIndex);

            return optimalSize;
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            performClick();
            final Camera.Parameters params = mViewCamera.getParameters();
            final ViewGroup colorModePanel = mContainer.findViewById(R.id.color_mode_panel);
            final ViewGroup fpsRateCalcPanel = mContainer.findViewById(R.id.fps_calc_period_indicator);
            final ViewGroup indicators = mContainer.findViewById(R.id.indicator_panel);

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                VideOSCUIHelpers.removeView(colorModePanel, mContainer);
                if (mApp.getInteractionMode().equals(InteractionModes.SINGLE_PIXEL)) {
                    if (fpsRateCalcPanel != null)
                        fpsRateCalcPanel.setVisibility(View.INVISIBLE);
                    indicators.setVisibility(View.INVISIBLE);
                    mPixelEditor.setVisibility(View.INVISIBLE);
                    mSnapshotsBar.setVisibility(View.INVISIBLE);
                }
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (mApp.getInteractionMode().equals(InteractionModes.SINGLE_PIXEL)
                        && mApp.getPixelEditMode().equals(PixelEditModes.DELETE_EDITS)) {
                    mPixelEditor.setVisibility(View.VISIBLE);
                    mSnapshotsBar.setVisibility(View.VISIBLE);
                }
                // clear selected pixels on up
                if (mApp.getPixelEditMode().equals(PixelEditModes.QUICK_EDIT_PIXELS)) {
                    mSelectedPixels.clear();
                } else if (mApp.getPixelEditMode().equals(PixelEditModes.EDIT_PIXELS)) {
                    for (int i = 0; i < mLockedPixels.size(); i++)
                        mLockedPixels.set(i, false);
                    mPixelEditor.setVisibility(View.VISIBLE);
                    mSnapshotsBar.setVisibility(View.VISIBLE);
                }
                mOverlayView.setSelectedRects(mSelectedPixels);
                mOverlayView.invalidate();

                if (mApp.getInteractionMode().equals(InteractionModes.SINGLE_PIXEL)) {
                    // mPixelIds holds the indices of the selected pixels (resp. index + 1, as we display pixel at index 0 as "1")
                    // colors in createMultiSliders() keeps the integer color values of the pixels denoted in mPixelIds
                    if (mApp.getPixelEditMode().equals(PixelEditModes.QUICK_EDIT_PIXELS) && mPixelIds.size() > 0)
                        createMultiSliders();

                    if (!mApp.getIsMultiSliderActive()) {
                        if (fpsRateCalcPanel != null)
                            fpsRateCalcPanel.setVisibility(View.VISIBLE);
                        if (indicators != null)
                            indicators.setVisibility(View.VISIBLE);
                    }
                }
            }

            if (motionEvent.getPointerCount() > 1 && params.isZoomSupported()) {
                if (mApp.getInteractionMode().equals(InteractionModes.BASIC)) {
                    int zoom = params.getZoom();

                    if (mOldFingerDistance == 0.0)
                        mOldFingerDistance = getFingerSpacing(motionEvent);
                    double currFingerDistance = getFingerSpacing(motionEvent);

                    if (mOldFingerDistance != currFingerDistance) {
                        if (mOldFingerDistance < currFingerDistance) {
                            if (zoom + 1 <= params.getMaxZoom()) zoom++;
                        } else {
                            if (zoom - 1 >= 0) zoom--;
                        }
                        params.setZoom(zoom);
                        mOldFingerDistance = currFingerDistance;
                        mViewCamera.setParameters(params);
                        mCamZoom = (float) (params.getZoomRatios().get(params.getZoom()) / 100.0);
                    }
                }
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (mApp.getInteractionMode().equals(InteractionModes.SINGLE_PIXEL)) {
                    int currPixel = getHoverPixel(motionEvent.getX(), motionEvent.getY());
                    Rect currRect = getCurrentPixelRect(currPixel);
                    final boolean isInQuickEditMode = mApp.getPixelEditMode().equals(PixelEditModes.QUICK_EDIT_PIXELS);
                    final boolean isInEditMode = mApp.getPixelEditMode().equals(PixelEditModes.EDIT_PIXELS);

                    if (!mApp.getPixelEditMode().equals(PixelEditModes.DELETE_EDITS)) {
                        if (!mPixelIds.contains(currPixel + 1)) {
                            if (isInQuickEditMode || (isInEditMode && !mLockedPixels.get(currPixel))) {
                                mPixelIds.add(currPixel + 1);
                            }
                            Collections.sort(mPixelIds);
                        }
                        if (!containsRect(mSelectedPixels, currRect)) {
                            if (isInQuickEditMode || (isInEditMode && !mLockedPixels.get(currPixel))) {
                                mSelectedPixels.add(currRect);
                                if (isInEditMode && !mLockedPixels.get(currPixel))
                                    mLockedPixels.set(currPixel, true);
                            }
                        } else {
                            // only if pixels have been selected once resp. after UP and DOWN again one can deselect a pixel
                            if (isInEditMode && !mLockedPixels.get(currPixel)) {
                                mPixelIds.remove(Integer.valueOf(currPixel + 1));
                                removeRect(mSelectedPixels, currRect);
                                mLockedPixels.set(currPixel, true);
                            }
                        }
                        mOverlayView.setSelectedRects(mSelectedPixels);
                        mOverlayView.measure(getMeasuredWidth(), getMeasuredHeight());
                        mOverlayView.invalidate();
                    } else {
                        if (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(RGBModes.R)) {
                            mRedValues.set(currPixel, null);
                            mRedMixValues.set(currPixel, null);
                        }
                        if (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(RGBModes.G)) {
                            mGreenValues.set(currPixel, null);
                            mGreenMixValues.set(currPixel, null);
                        }
                        if (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(RGBModes.B)) {
                            mBlueValues.set(currPixel, null);
                            mBlueMixValues.set(currPixel, null);
                        }
                    }
                }
            }
            return true;
        }

        private void removeRect(ArrayList<Rect> rects, Rect rect) {
            for (Rect rectInList : rects) {
                if (rectInList.equals(rect)) {
                    rects.remove(rect);
                    break;
                }
            }
        }

        public void createMultiSliders() {
            final ViewGroup indicators = mContainer.findViewById(R.id.indicator_panel);
            final ViewGroup fpsRateCalcPanel = mContainer.findViewById(R.id.fps_calc_period_indicator);
            final ViewGroup modePanel = mContainer.findViewById(R.id.color_mode_panel);
            short numSelectedPixels = (short) mPixelIds.size();
            int[] colors = new int[numSelectedPixels];
            double[] redVals = new double[numSelectedPixels];
            double[] redMixVals = new double[numSelectedPixels];
            double[] greenVals = new double[numSelectedPixels];
            double[] greenMixVals = new double[numSelectedPixels];
            double[] blueVals = new double[numSelectedPixels];
            double[] blueMixVals = new double[numSelectedPixels];
            Point res = mApp.getResolution();

            mResetRedVals = new SparseArray<>();
            mResetRedMixVals = new SparseArray<>();
            mResetGreenVals = new SparseArray<>();
            mResetGreenMixVals = new SparseArray<>();
            mResetBlueVals = new SparseArray<>();
            mResetBlueMixVals = new SparseArray<>();

            for (int i = 0; i < numSelectedPixels; i++) {
                int id = mPixelIds.get(i) - 1;
                // FIXME: some bug lurking here: "y must be < bitmap.height()"
                colors[i] = mBmp.getPixel(id % res.x, id / res.x);
                // once a value has been set manually the value should not get reset
                // when editing the same pixel again
                if (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(RGBModes.R)) {
                    // in case editing gets canceled, store current values
                    mResetRedVals.put(id, mRedValues.get(id));
                    if (mRedValues.get(id) == null)
                        mRedValues.set(id, ((colors[i] >> 16) & 0xFF) / 255.0);
                    redVals[i] = mRedValues.get(id);
                    redMixVals[i] = mRedMixValues.get(id) == null ? 1.0 : mRedMixValues.get(id);
                    // in case editing gets canceled, store current mix values
                    mResetRedMixVals.put(id, mRedMixValues.get(id));
                }
                if (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(RGBModes.G)) {
                    mResetGreenVals.put(id, mGreenValues.get(id));
                    if (mGreenValues.get(id) == null)
                        mGreenValues.set(id, ((colors[i] >> 8) & 0xFF) / 255.0);
                    greenVals[i] = mGreenValues.get(id);
                    greenMixVals[i] = mGreenMixValues.get(id) == null ? 1.0 : mGreenMixValues.get(id);
                    mResetGreenMixVals.put(id, mGreenMixValues.get(id));
                }
                if (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(RGBModes.B)) {
                    mResetBlueVals.put(id, mBlueValues.get(id));
                    if (mBlueValues.get(id) == null)
                        mBlueValues.set(id, (colors[i] & 0xFF) / 255.0);
                    blueVals[i] = mBlueValues.get(id);
                    blueMixVals[i] = mBlueMixValues.get(id) == null ? 1.0 : mBlueMixValues.get(id);
                    mResetBlueMixVals.put(id, mBlueMixValues.get(id));
                }
            }

            Bundle msArgsBundle = new Bundle();
            msArgsBundle.putIntegerArrayList("nums", (ArrayList<Integer>) mPixelIds);
            if (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(RGBModes.R)) {
                msArgsBundle.putDoubleArray("redVals", redVals);
                msArgsBundle.putDoubleArray("redMixVals", redMixVals);
            }
            if (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(RGBModes.G)) {
                msArgsBundle.putDoubleArray("greenVals", greenVals);
                msArgsBundle.putDoubleArray("greenMixVals", greenMixVals);
            }
            if (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(RGBModes.B)) {
                msArgsBundle.putDoubleArray("blueVals", blueVals);
                msArgsBundle.putDoubleArray("blueMixVals", blueMixVals);
            }

            if (mManager.findFragmentByTag("MultiSliderView") == null) {
                if (!mApp.getColorMode().equals(RGBModes.RGB)) {
                    VideOSCMultiSliderFragment multiSliderFragment = new VideOSCMultiSliderFragment(mActivity);
                    mManager.beginTransaction()
                            .add(R.id.camera_preview, multiSliderFragment, "MultiSliderView")
                            .commit();
                    multiSliderFragment.setArguments(msArgsBundle);
                    multiSliderFragment.setParentContainer(mContainer);
                    if (multiSliderFragment.getView() == null) {
                        multiSliderFragment.setCreateViewCallback(new VideOSCMultiSliderFragmentRGB.OnCreateViewCallback() {
                            @Override
                            public void onCreateView() {
                                mPixelIds.clear();
                            }
                        });
                    } else mPixelIds.clear();
                } else {
                    final VideOSCMultiSliderFragmentRGB multiSliderFragment = new VideOSCMultiSliderFragmentRGB(mActivity);
                    mManager.beginTransaction()
                            .add(R.id.camera_preview, multiSliderFragment, "MultiSliderView")
                            .commit();
                    multiSliderFragment.setArguments(msArgsBundle);
                    multiSliderFragment.setParentContainer(mContainer);
                    if (multiSliderFragment.getView() == null) {
                        multiSliderFragment.setCreateViewCallback(new VideOSCMultiSliderFragmentRGB.OnCreateViewCallback() {
                            @Override
                            public void onCreateView() {
                                mPixelIds.clear();
                            }
                        });
                    } else mPixelIds.clear();
                }

                mApp.setIsMultiSliderActive(true);
                indicators.setVisibility(View.INVISIBLE);
                if (fpsRateCalcPanel != null)
                    fpsRateCalcPanel.setVisibility(View.INVISIBLE);
                mApp.setIsColorModePanelOpen(VideOSCUIHelpers.removeView(modePanel, mContainer));
                mToolsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }

        private boolean containsRect(ArrayList<Rect> rectList, Rect rect) {
            for (Rect rectInList : rectList) {
                if (rectInList.equals(rect)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return false;
        }

        /**
         * Determine the space between the first two fingers
         */
        private double getFingerSpacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);

            return Math.sqrt(x * x + y * y);
        }

        /**
         * get current zoom
         */
        private int getCurrentZoom() {
            Camera.Parameters params = mViewCamera.getParameters();
            return params.getZoom();
        }

        /**
         * set zoom
         */
        private void setZoom(int zoom) {
            Camera.Parameters params = mViewCamera.getParameters();
            params.setZoom(zoom);
            mViewCamera.setParameters(params);
        }

        private void setPixelSize(SurfaceHolder holder) {
            Rect surfaceFrame = holder.getSurfaceFrame();
            Point resolution = mApp.getResolution();
            mPixelSize.x = surfaceFrame.width() / resolution.x;
            mPixelSize.y = surfaceFrame.height() / resolution.y;
            mApp.setPixeSize(mPixelSize);
        }

        private int getHoverPixel(float x, float y) {
            int hIndex = (int) x / mPixelSize.x;
            int vIndex = (int) y / mPixelSize.y;

            return vIndex * mApp.getResolution().x + hIndex;
        }

        private Rect getCurrentPixelRect(int pixelId) {
            Point resolution = mApp.getResolution();
            int left = pixelId % resolution.x * mPixelSize.x;
            int top = pixelId / resolution.x * mPixelSize.y;
            int right = left + mPixelSize.x;
            int bottom = top + mPixelSize.y;
            return new Rect(left, top, right, bottom);
        }

        // set the preview fps range and update framerate immediately
        // format, w, h must be passed in explicitely as they're demanded by surfaceChanged()
        private void setPreviewFpsRange(Camera.Parameters params, int[] range, int format, int w, int h) {
            params.setPreviewFpsRange(range[0], range[1]);
            surfaceChanged(mHolder, format, w, h);
        }

        private void pad(int diff) {
            if (diff > 0) {
                for (int i = 0; i < diff; i++) {
                    mRedValues.add(null);
                    mGreenValues.add(null);
                    mBlueValues.add(null);
                    mRedMixValues.add(null);
                    mGreenMixValues.add(null);
                    mBlueMixValues.add(null);

                    mLockedPixels.add(false);

                    mPrevRedValues.add(null);
                    mPrevGreenValues.add(null);
                    mPrevBlueValues.add(null);
                }
            } else if (diff < 0) {
                mRedValues.subList(mRedValues.size() - 1 + diff, mRedValues.size() - 1).clear();
                mGreenValues.subList(mGreenValues.size() - 1 + diff, mGreenValues.size() - 1).clear();
                mBlueValues.subList(mBlueValues.size() - 1 + diff, mBlueValues.size() - 1).clear();
                mRedMixValues.subList(mRedMixValues.size() - 1 + diff, mRedMixValues.size() - 1).clear();
                mGreenMixValues.subList(mGreenMixValues.size() - 1 + diff, mGreenMixValues.size() - 1).clear();
                mBlueMixValues.subList(mBlueMixValues.size() - 1 + diff, mBlueMixValues.size() - 1).clear();

                mLockedPixels.subList(mLockedPixels.size() - 1 + diff, mLockedPixels.size() - 1).clear();

                mPrevRedValues.subList(mPrevRedValues.size() - 1 + diff, mPrevRedValues.size() - 1).clear();
                mPrevGreenValues.subList(mPrevGreenValues.size() - 1 + diff, mPrevGreenValues.size() - 1).clear();
                mPrevBlueValues.subList(mPrevBlueValues.size() - 1 + diff, mPrevBlueValues.size() - 1).clear();
            }
        }

        private Bitmap drawFrame(Bitmap bmp, int width, int height) {
            double rValue, gValue, bValue;
            double mixPowered, mixReciprPowered, mult;
            Double redSliderVal, greenSliderVal, blueSliderVal;
            Point resolution = mApp.getResolution();
            int dimensions = resolution.x * resolution.y;
            int[] pixels = new int[width * height];

            bmp.getPixels(pixels, 0, width, 0, 0, width, height);

            // color mode RGB (or RGB inverted)
            VideOSCMultiSliderView msRedLeft = mContainer.findViewById(R.id.multislider_view_r_left);
            VideOSCMultiSliderView msRedRight = mContainer.findViewById(R.id.multislider_view_r_right);
            VideOSCMultiSliderView msGreenLeft = mContainer.findViewById(R.id.multislider_view_g_left);
            VideOSCMultiSliderView msGreenRight = mContainer.findViewById(R.id.multislider_view_g_right);
            VideOSCMultiSliderView msBlueLeft = mContainer.findViewById(R.id.multislider_view_b_left);
            VideOSCMultiSliderView msBlueRight = mContainer.findViewById(R.id.multislider_view_b_right);
            // color mode R, G or B
            VideOSCMultiSliderView msLeft = mContainer.findViewById(R.id.multislider_view_left);
            VideOSCMultiSliderView msRight = mContainer.findViewById(R.id.multislider_view_right);

            for (int i = 0; i < dimensions; i++) {
                Double mixVal;

                // only the downsampled image gets inverted as inverting the original would slow
                // down the application considerably
                int rPixVal = (!mApp.getIsRGBPositive()) ? 0xFF - ((pixels[i] >> 16) & 0xFF)
                        : (pixels[i] >> 16) & 0xFF;
                int gPixVal = (!mApp.getIsRGBPositive()) ? 0xFF - ((pixels[i] >> 8) & 0xFF)
                        : (pixels[i] >> 8) & 0xFF;
                int bPixVal = (!mApp.getIsRGBPositive()) ? 0xFF - (pixels[i] & 0xFF)
                        : pixels[i] & 0xFF;

                if (mApp.getColorMode().equals(RGBModes.RGB)
                        && msRedLeft != null
                        && msRedRight != null
                        && msGreenLeft != null
                        && msGreenRight != null
                        && msBlueLeft != null
                        && msBlueRight != null) {

                    // color values
                    redSliderVal = msRedLeft.getSliderValueAt(i);
                    if (redSliderVal != null) {
                        mRedValues.set(i, redSliderVal);
                        // mix values: once a mix value has been set it should be remembered until it's set
                        // to a new value (by moving the slider. Next time the regarding pixel is edited
                        // the slider should be set to the value that has been stored on the last edit
                        // TODO: default value should maybe be settable in preferences to 1.0 or 0.0
                        mixVal = msRedRight.getSliderValueAt(i);
                        mRedMixValues.set(i, mixVal == null ? 1.0 : mixVal);
                    }
                    greenSliderVal = msGreenLeft.getSliderValueAt(i);
                    if (greenSliderVal != null) {
                        mGreenValues.set(i, greenSliderVal);
                        mixVal = msGreenRight.getSliderValueAt(i);
                        mGreenMixValues.set(i, mixVal == null ? 1.0 : mixVal);
                    }
                    blueSliderVal = msBlueLeft.getSliderValueAt(i);
                    if (blueSliderVal != null) {
                        mBlueValues.set(i, blueSliderVal);
                        mixVal = msBlueRight.getSliderValueAt(i);
                        mBlueMixValues.set(i, mixVal == null ? 1.0 : mixVal);
                    }
                } else if (!mApp.getColorMode().equals(RGBModes.RGB)
                        && msLeft != null
                        && msRight != null) {
                    switch (mApp.getColorMode()) {
                        case R:
                            redSliderVal = msLeft.getSliderValueAt(i);
                            if (redSliderVal != null) {
                                mRedValues.set(i, redSliderVal);
                                mixVal = msRight.getSliderValueAt(i);
                                mRedMixValues.set(i, mixVal == null ? 1.0 : mixVal);
                            }
                            break;
                        case G:
                            greenSliderVal = msLeft.getSliderValueAt(i);
                            if (greenSliderVal != null) {
                                mGreenValues.set(i, greenSliderVal);
                                mixVal = msRight.getSliderValueAt(i);
                                mGreenMixValues.set(i, mixVal == null ? 1.0 : mixVal);
                            }
                            break;
                        case B:
                            blueSliderVal = msLeft.getSliderValueAt(i);
                            if (blueSliderVal != null) {
                                mBlueValues.set(i, blueSliderVal);
                                mixVal = msRight.getSliderValueAt(i);
                                mBlueMixValues.set(i, mixVal == null ? 1.0 : mixVal);
                            }
                            break;
                    }
                }

                // set values considering values coming from the 'mix' multislider
                // should allow a non-linear, exponential crossfade

                // default values before being set through sliders
                rValue = rPixVal / 255.0;
                gValue = gPixVal / 255.0;
                bValue = bPixVal / 255.0;

                if (mRedValues.get(i) != null) {
                    if (mRedMixValues.get(i) != null && mRedMixValues.get(i) < 1.0) {
                        mixPowered = Math.pow(mRedMixValues.get(i), 2);
                        mixReciprPowered = Math.pow(1.0 - mRedMixValues.get(i), 2);
                        mult = 1.0 / (mixPowered + mixReciprPowered);
                        rValue = (rPixVal / 255.0 * mixReciprPowered + mRedValues.get(i) * mixPowered) * mult;
                    } else rValue = mRedValues.get(i);
                    // if colors are inverted
                    if (!mApp.getIsRGBPositive()) rValue = 1 - rValue;
                }

                if (mGreenValues.get(i) != null) {
                    if (mGreenMixValues.get(i) != null && mGreenMixValues.get(i) < 1.0) {
                        mixPowered = Math.pow(mGreenMixValues.get(i), 2);
                        mixReciprPowered = Math.pow(1.0 - mGreenMixValues.get(i), 2);
                        mult = 1.0 / (mixPowered + mixReciprPowered);
                        gValue = (gPixVal / 255.0 * mixReciprPowered + mGreenValues.get(i) * mixPowered) * mult;
                    } else gValue = mGreenValues.get(i);
                    // if colors are inverted
                    if (!mApp.getIsRGBPositive()) gValue = 1 - gValue;
                }

                if (mBlueValues.get(i) != null) {
                    if (mBlueMixValues.get(i) != null && mBlueMixValues.get(i) < 1.0) {
                        mixPowered = Math.pow(mBlueMixValues.get(i), 2);
                        mixReciprPowered = Math.pow(1.0 - mBlueMixValues.get(i), 2);
                        mult = 1.0 / (mixPowered + mixReciprPowered);
                        bValue = (bPixVal / 255.0 * mixReciprPowered + mBlueValues.get(i) * mixPowered) * mult;
                    } else bValue = mBlueValues.get(i);
                    // if colors are inverted
                    if (!mApp.getIsRGBPositive()) bValue = 1 - bValue;
                }

                // pixels can only be set to ints in a range from 0-255
                if (mRedValues.get(i) != null) rPixVal = (int) Math.round(rValue * 255);
                if (mGreenValues.get(i) != null) gPixVal = (int) Math.round(gValue * 255);
                if (mBlueValues.get(i) != null) bPixVal = (int) Math.round(bValue * 255);

                // set pixels
                if (!mApp.getPixelImageHidden()) {
                    if (mApp.getColorMode().equals(RGBModes.RGB)) {
                        pixels[i] = Color.argb(255, rPixVal, gPixVal, bPixVal);
                    } else if (mApp.getColorMode().equals(RGBModes.R)) {
                        pixels[i] = Color.argb(255, rPixVal, 0, 0);
                    } else if (mApp.getColorMode().equals(RGBModes.G)) {
                        pixels[i] = Color.argb(255, 0, gPixVal, 0);
                    } else if (mApp.getColorMode().equals(RGBModes.B)) {
                        pixels[i] = Color.argb(255, 0, 0, bPixVal);
                    }
                } else {
                    // all pixels fully transparent
                    pixels[i] = Color.argb(0, 0, 0, 0);
                }

                // compose basic OSC message for slot
                if (mApp.getCameraOSCisPlaying()) {
                    if (!mApp.getNormalized()) {
                        rValue *= 255.0;
                        gValue *= 255.0;
                        bValue *= 255.0;
                    }

                    // all OSC messaging (message construction sending) must happen synchronized
                    // otherwise messages easily get overwritten during processing
                    doSendRedOSC(rValue, i, dimensions);
                    doSendGreenOSC(gValue, i, dimensions);
                    doSendBlueOSC(bValue, i, dimensions);
                }
            }

            bmp.setPixels(pixels, 0, width, 0, 0, width, height);
            return bmp;
        }

        private void doSendRedOSC(double value, int count, int dimensions) {
            String cmd;

            for (int i = 0; i < mMappings.size(); i++) {
                final String mappingString = mMappings.valueAt(i);
                if (count == 0) {
                    if (mOscBundlesR.size() > i) {
                        mOscBundlesR.set(i, new OscBundle());
                    } else {
                        mOscBundlesR.add(i, new OscBundle());
                    }
                }
                cmd = mRed + (count + 1);
                if (mPrevRedValues.get(count) == null || mPrevRedValues.get(count) != value) {
                    if (mappingString.charAt(count) == '1') {
                        OscMessage oscR = new OscMessage(cmd).add(value);
                        mOscBundlesR.get(i).add(oscR);
                        // set previous values only once
                        // we're in the same frame, hence we
                        // don't need to set the same value again for every client address
                        if (i == mMappings.size() - 1) {
                            mPrevRedValues.set(count, value);
                        }
                    }
                }
                if (count + 1 == dimensions) {
                    if (VideOSCApplication.getDebugPixelOsc()) {
                        RedOscRunnable.setDebugPixelOsc(true);
                        mDebugRed = mOscHelper.makeMessage(mDebugRed, "/num_red_bundles").add(++mCountR);
                        mRedOscRunnable.mDebugMsg = mDebugRed;
                    } else {
                        RedOscRunnable.setDebugPixelOsc(false);
                    }
                    mRedOscRunnable.mOscClients = mOscClients;
                    // send bundles once we've iterated over all client addresses
                    if (i == mMappings.size() - 1) {
                        synchronized (mRedOscRunnable.mOscLock) {
                            mRedOscRunnable.mOscHelper = mOscHelper;
                            mRedOscRunnable.mBundles = mOscBundlesR;
                            mRedOscRunnable.mOscLock.notify();
                        }
                    }
                }
            }
        }

        private void doSendGreenOSC(double value, int count, int dimensions) {
            String cmd;

            for (int i = 0; i < mMappings.size(); i++) {
                final String mappingString = mMappings.valueAt(i);
                if (count == 0) {
                    if (mOscBundlesG.size() > i) {
                        mOscBundlesG.set(i, new OscBundle());
                    } else {
                        mOscBundlesG.add(i, new OscBundle());
                    }
                }
                cmd = mGreen + (count + 1);
                if (mPrevGreenValues.get(count) == null || value != mPrevGreenValues.get(count)) {
                    if (mappingString.charAt(count + mappingString.length() / 3) == '1') {
                        OscMessage oscG = new OscMessage(cmd).add(value);
                        mOscBundlesG.get(i).add(oscG);
                        if (i == mMappings.size() - 1) {
                            mPrevGreenValues.set(count, value);
                        }
                    }
                }
                if (count + 1 == dimensions) {
                    if (VideOSCApplication.getDebugPixelOsc()) {
                        GreenOscRunnable.setDebugPixelOsc(true);
                        mDebugGreen = mOscHelper.makeMessage(mDebugGreen, "/num_green_bundles").add(++mCountG);
                        mGreenOscRunnable.mDebugMsg = mDebugGreen;
                    } else {
                        GreenOscRunnable.setDebugPixelOsc(false);
                    }
                    mGreenOscRunnable.mOscClients = mOscClients;
                    if (i == mMappings.size() - 1) {
                        synchronized (mGreenOscRunnable.mOscLock) {
                            mGreenOscRunnable.mOscHelper = mOscHelper;
                            mGreenOscRunnable.mBundles = mOscBundlesG;
                            mGreenOscRunnable.mOscLock.notify();
                        }
                    }
                }
            }
        }

        private void doSendBlueOSC(double value, int count, int dimensions) {
            String cmd;

            for (int i = 0; i < mMappings.size(); i++) {
                final String mappingString = mMappings.valueAt(i);
                if (count == 0) {
                    if (mOscBundlesB.size() > i) {
                        mOscBundlesB.set(i, new OscBundle());
                    } else {
                        mOscBundlesB.add(i, new OscBundle());
                    }
                }
                cmd = mBlue + (count + 1);
                if (mPrevBlueValues.get(count) == null || value != mPrevBlueValues.get(count)) {
                    if (mappingString.charAt(count + mappingString.length() * 2 / 3) == '1') {
                        OscMessage mOscB = new OscMessage(cmd).add(value);
                        mOscBundlesB.get(i).add(mOscB);
                        if (i == mMappings.size() - 1) {
                            mPrevBlueValues.set(count, value);
                        }
                    }
                }
                if (count + 1 == dimensions) {
                    if (VideOSCApplication.getDebugPixelOsc()) {
                        BlueOscRunnable.setDebugPixelOsc(true);
                        mDebugBlue = mOscHelper.makeMessage(mDebugBlue, "/num_blue_bundles").add(++mCountB);
                        mBlueOscRunnable.mDebugMsg = mDebugBlue;
                    } else {
                        BlueOscRunnable.setDebugPixelOsc(false);
                    }
                    mBlueOscRunnable.mOscClients = mOscClients;
                    if (i == mMappings.size() - 1) {
                        synchronized (mBlueOscRunnable.mOscLock) {
                            mBlueOscRunnable.mOscHelper = mOscHelper;
                            mBlueOscRunnable.mBundles = mOscBundlesB;
                            mBlueOscRunnable.mOscLock.notify();
                        }
                    }
                }
            }
        }

        // needed by the VideOSCMultiSliderFragmentRGB.OnCreateViewCallback
        @Override
        public void onCreateView() {
        }
    }

    // prevent memory leaks by declaring Runnable static
    // see also https://stackoverflow.com/questions/29694222/is-this-runnable-safe-from-memory-leak
    // or http://www.androiddesignpatterns.com/2013/04/activitys-threads-memory-leaks.html
    private static class RedOscRunnable implements Runnable {
        private VideOSCOscHandler mOscHelper;
        private SparseArray<NetAddress> mOscClients;
        //		private int mAddrKey;
        private ArrayList<OscBundle> mBundles;
        private OscMessage mDebugMsg;
        private final Object mOscLock = new Object();
        private long mCountSentR = 0;
        private static boolean mDebugPixel = false;

        private static void setDebugPixelOsc(boolean debugPixel) {
            mDebugPixel = debugPixel;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         */
        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            while (true) {
                synchronized (mOscLock) {
                    try {
                        if (mBundles != null && mBundles.size() > 0) {
                            for (int i = 0; i < mBundles.size(); i++) {
                                OscBundle bundle = mBundles.get(i);
                                if (bundle.size() > 0) {
                                    if (mDebugPixel) {
                                        mDebugMsg.add(++mCountSentR);
                                        bundle.add(mDebugMsg);
                                    }
                                    final NetAddress client = mOscClients.valueAt(i);
                                    mOscHelper.getUdpListener().send(bundle, client.address(), client.port());
                                }
                            }
                        }
                        mOscLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class GreenOscRunnable implements Runnable {
        private VideOSCOscHandler mOscHelper;
        private SparseArray<NetAddress> mOscClients;
        private ArrayList<OscBundle> mBundles;
        private OscMessage mDebugMsg;
        private final Object mOscLock = new Object();
        private long mCountSentG = 0;
        private static boolean mDebugPixel = false;

        private static void setDebugPixelOsc(boolean debugPixel) {
            mDebugPixel = debugPixel;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         */
        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            while (true) {
                synchronized (mOscLock) {
                    try {
                        if (mBundles != null && mBundles.size() > 0) {
                            for (int i = 0; i < mBundles.size(); i++) {
                                OscBundle bundle = mBundles.get(i);
                                if (bundle.size() > 0) {
                                    if (mDebugPixel) {
                                        mDebugMsg.add(++mCountSentG);
                                        bundle.add(mDebugMsg);
                                    }
                                    final NetAddress client = mOscClients.valueAt(i);
                                    mOscHelper.getUdpListener().send(bundle, client.address(), client.port());
                                }
                            }
                        }
                        mOscLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class BlueOscRunnable implements Runnable {
        private VideOSCOscHandler mOscHelper;
        private SparseArray<NetAddress> mOscClients;
        private ArrayList<OscBundle> mBundles;
        private volatile OscMessage mDebugMsg;
        private final Object mOscLock = new Object();
        private long mCountSentB = 0;
        private static boolean mDebugPixel = false;

        private static void setDebugPixelOsc(boolean debugPixel) {
            mDebugPixel = debugPixel;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         */
        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            while (true) {
                synchronized (mOscLock) {
                    try {
                        if (mBundles != null && mBundles.size() > 0) {
                            for (int i = 0; i < mBundles.size(); i++) {
                                OscBundle bundle = mBundles.get(i);
                                if (bundle.size() > 0) {
                                    if (mDebugPixel) {
                                        mDebugMsg.add(++mCountSentB);
                                        bundle.add(mDebugMsg);
                                    }
                                    final NetAddress client = mOscClients.valueAt(i);
                                    mOscHelper.getUdpListener().send(bundle, client.address(), client.port());
                                }
                            }
                        }
                        mOscLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
