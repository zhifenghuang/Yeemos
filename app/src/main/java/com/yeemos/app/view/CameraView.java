package com.yeemos.app.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.gbsocial.preferences.GBSPreferences;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.glsurface.utils.OnCameraUseListener;
import com.yeemos.app.hardwrare.CameraManager;
import com.yeemos.app.hardwrare.SensorControler;
import com.yeemos.app.interfaces.IActivityLifiCycle;
import com.yeemos.app.interfaces.ICameraOperation;
import com.yeemos.app.interfaces.OnCameraListener;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 相机控件
 *
 * @author jerry
 * @date 2015-09-24
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback, ICameraOperation, IActivityLifiCycle {
    public static final String TAG = "CameraView";

    private CameraManager.CameraDirection mCameraId; //0后置  1前置
    private Camera mCamera;
    private Camera.Parameters parameters = null;
    private CameraManager mCameraManager;
    private Context mContext;
    private SensorControler mSensorControler;
    private SwitchCameraCallBack mSwitchCameraCallBack;

    private int mDisplayOrientation;
    private int mLayoutOrientation;
    private CameraOrientationListener mOrientationListener;

    private String mStrVideoPath;
    private boolean mIsRecording;
    private MediaRecorder mMediaRecorder;
    public static final int MAX_DURATION = 11 * 1000;
    /**
     * 当前缩放
     */
    private int mZoom;
    /**
     * 当前屏幕旋转角度
     */
    private int mOrientation = 0;

    private int mRotation;

    private OnCameraPrepareListener onCameraPrepareListener;
    private Camera.PictureCallback callback;

    private Activity mActivity;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mCameraManager = CameraManager.getInstance(context);
        mCameraId = mCameraManager.getCameraDirection();

        setFocusable(true);
        getHolder().addCallback(this);//为SurfaceView的句柄添加一个回调函数

        mSensorControler = SensorControler.getInstance();
        mOrientationListener = new CameraOrientationListener(mContext);
        mOrientationListener.enable();
    }

    public void bindActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void setOnCameraPrepareListener(OnCameraPrepareListener onCameraPrepareListener) {
        this.onCameraPrepareListener = onCameraPrepareListener;
    }

    public void setPictureCallback(Camera.PictureCallback callback) {
        this.callback = callback;
    }

    public void setSwitchCameraCallBack(SwitchCameraCallBack mSwitchCameraCallBack) {
        this.mSwitchCameraCallBack = mSwitchCameraCallBack;
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        parameters = mCamera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);

        List<String> focusModes = parameters.getSupportedFocusModes();

        //设置对焦模式
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int screenWidth = ((BaseActivity) getContext()).getDisplaymetrics().widthPixels;
        int screenHeight = ((BaseActivity) getContext()).getDisplaymetrics().heightPixels;
        mRate = screenHeight * 1.0f / screenWidth;

        Camera.Size previewSize = getPreviewSize(parameters.getSupportedPreviewSizes(), screenWidth / 2);
        boolean isSetPictureSize = false;
        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            int width = previewSize.width;
            int height = previewSize.height;
            List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
            for (Camera.Size size : pictureSizeList) {
                if (size.width == width && size.height == height) {
                    parameters.setPictureSize(width, height);
                    isSetPictureSize = true;
                    break;
                }
            }
        }

        if (!isSetPictureSize) {
            Camera.Size pictureSize = getPictureSize(parameters.getSupportedPictureSizes(), screenWidth / 2);
            if (pictureSize != null) {
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
            }
        }
        determineDisplayOrientation();
        mCamera.setParameters(parameters);
        mCamera.startPreview();
        turnLight(CameraManager.FlashLigthStatus.LIGHT_OFF);  //设置闪光灯
        mCameraManager.setActivityCamera(mCamera);

    }

    private float mRate;
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();

    public Camera.Size getPreviewSize(List<Camera.Size> list, int th) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        DisplayMetrics dis = ((BaseActivity) getContext()).getDisplaymetrics();
        if (dis.widthPixels == 480 && dis.heightPixels == 800) {
            for (Camera.Size s : list) {
                if (s.width == 640 && s.height == 480) {
                    return s;
                }
            }
        }

        for (Camera.Size s : list) {
            if (s.height == dis.widthPixels) {
                if (dis.widthPixels == s.width || equalRate(s, mRate)) {
                    return s;
                }
            }
        }

        for (Camera.Size s : list) {
            if ((s.height >= th) && equalRate(s, mRate)) {
                break;
            }
            i++;
        }
        if (i >= list.size()) {
            return null;
        }
        return list.get(i);
    }

    public Camera.Size getPictureSize(List<Camera.Size> list, int th) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        DisplayMetrics dis = ((BaseActivity) getContext()).getDisplaymetrics();
        if (dis.widthPixels == 480 && dis.heightPixels == 800) {
            for (Camera.Size s : list) {
                if (s.width == 640 && s.height == 480) {
                    return s;
                }
            }
        }

        for (Camera.Size s : list) {
            if (s.height == dis.widthPixels) {
                if (dis.widthPixels == s.width || equalRate(s, mRate)) {
                    return s;
                }
            }
        }

        for (Camera.Size s : list) {
            if ((s.height >= th) && equalRate(s, mRate)) {
                break;
            }
            i++;
        }
        if (i >= list.size()) {
            return null;
        }
        return list.get(i);
    }

    private boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.2;
    }

    public class CameraSizeComparator implements Comparator<Camera.Size> {
        //按升序排列
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // TODO Auto-generated method stub
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    public void startPreview() {
        if (mCamera != null) {
            try {
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放相机
     */
    public void releaseCamera() {
        mCameraManager.releaseCamera(mCamera);
        mCamera = null;
    }


    /**
     * 切换摄像头
     */
    @Override
    public void switchCamera() {
        mCameraId = mCameraId.next();
        releaseCamera();

        setUpCamera(mCameraId, mCameraId == CameraManager.CameraDirection.CAMERA_BACK);
    }

    @Override
    public void switchFlashMode() {
        turnLight(CameraManager.FlashLigthStatus.LIGHT_OFF);
    }

    public boolean isBackCamera() {
        return mCameraId == CameraManager.CameraDirection.CAMERA_BACK;
    }

    @Override
    public boolean takePicture() {
        try {
            mSensorControler.lockFocus();
            mCamera.takePicture(null, null, callback);
            mOrientationListener.rememberOrientation();
        } catch (Exception t) {
            t.printStackTrace();
            Log.e(TAG, "photo fail after Photo Clicked");

            try {
                mCamera.startPreview();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return false;
        }
        return true;
    }

    @Override
    public boolean takePicture(OnCameraUseListener onCameraUseListener) {
        return false;
    }

    @Override
    public int getMaxZoom() {
        if (mCamera == null) return -1;
        Camera.Parameters parameters = mCamera.getParameters();
        if (!parameters.isZoomSupported()) return -1;
        return parameters.getMaxZoom() > 40 ? 40 : parameters.getMaxZoom();
    }

    @Override
    public void setZoom(int zoom) {
        if (mCamera == null) return;
        Camera.Parameters parameters;
        //注意此处为录像模式下的setZoom方式。在Camera.unlock之后，调用getParameters方法会引起android框架底层的异常
        //stackoverflow上看到的解释是由于多线程同时访问Camera导致的冲突，所以在此使用录像前保存的mParameters。
        parameters = mCamera.getParameters();

        if (!parameters.isZoomSupported()) return;
        parameters.setZoom(zoom);
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {

        }
        mZoom = zoom;
    }

    @Override
    public int getZoom() {
        return mZoom;
    }


    /**
     * 闪光灯开关   开->关->自动
     */
    public void turnLight(CameraManager.FlashLigthStatus ligthStatus) {
        if (CameraManager.mFlashLightNotSupport.contains(ligthStatus)) {
            turnLight(ligthStatus.next());
            return;
        }
        if (mCamera == null || mCamera.getParameters() == null
                || mCamera.getParameters().getSupportedFlashModes() == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> supportedModes = mCamera.getParameters().getSupportedFlashModes();
        switch (ligthStatus) {
//            case LIGHT_AUTO:
//                if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
//                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//                }
//                break;
            case LIGHT_OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case LIGHT_ON:
                if (supportedModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                break;
        }
        mCamera.setParameters(parameters);
        mCameraManager.setLightStatus(ligthStatus);
    }

    public int getPicRotation() {
        return (mDisplayOrientation
                + mOrientationListener.getRememberedNormalOrientation()
                + mLayoutOrientation
        ) % 360;
    }

    /**
     * 设置当前的Camera 并进行参数设置
     *
     * @param mCameraId
     */
    private void setUpCamera(CameraManager.CameraDirection mCameraId, boolean isSwitchFromFront) {
        int facing = mCameraId.ordinal();
        try {
            mCamera = mCameraManager.openCameraFacing(facing);
            //重置对焦计数
            mSensorControler.restFoucs();
        } catch (Exception e) {
            //         Utils.displayToastCenter((Activity) mContext, R.string.tips_camera_forbidden);
            e.printStackTrace();
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(getHolder());
                initCamera();

                mCameraManager.setCameraDirection(mCameraId);

                if (mCameraId == CameraManager.CameraDirection.CAMERA_FRONT) {
                    mSensorControler.lockFocus();
                } else {
                    mSensorControler.unlockFocus();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mSwitchCameraCallBack != null) {
            mSwitchCameraCallBack.switchCamera(isSwitchFromFront);
        }
    }

    /**
     * 手动聚焦
     *
     * @param point 触屏坐标
     */
    protected boolean onFocus(Point point, Camera.AutoFocusCallback callback) {
        if (mCamera == null) {
            return false;
        }

        Camera.Parameters parameters = null;
        try {
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回

        if (Build.VERSION.SDK_INT >= 14) {

            if (parameters.getMaxNumFocusAreas() <= 0) {
                return focus(callback);
            }

            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            int left = point.x - 300;
            int top = point.y - 300;
            int right = point.x + 300;
            int bottom = point.y + 300;
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
            parameters.setFocusAreas(areas);
            try {
                //本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
                //目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return false;
            }
        }


        return focus(callback);
    }

    private boolean focus(Camera.AutoFocusCallback callback) {
        try {
            mCamera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mCameraManager.releaseStartTakePhotoCamera();
                if (null == mCamera) {
                    //打开默认的摄像头
                    setUpCamera(mCameraId, false);
                    if (onCameraPrepareListener != null) {
                        onCameraPrepareListener.onPrepare(mCameraId);
                    }
                    if (mCamera != null) {
                        startOrientationChangeListener();
                    }
                }
            }
        }, 500);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            releaseCamera();

            //释放资源
            if (holder != null) {
                if (Build.VERSION.SDK_INT >= 14) {
                    holder.getSurface().release();
                }
            }
        } catch (Exception e) {
            //相机已经关了
            e.printStackTrace();
        }
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId.ordinal(), cameraInfo);

        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;

        // Camera direction
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mDisplayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        mLayoutOrientation = degrees;

        mCamera.setDisplayOrientation(displayOrientation);
    }

    /**
     * 启动屏幕朝向改变监听函数 用于在屏幕横竖屏切换时改变保存的图片的方向
     */
    private void startOrientationChangeListener() {
        OrientationEventListener mOrEventListener = new OrientationEventListener(getContext()) {
            @Override
            public void onOrientationChanged(int rotation) {

                if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
                    rotation = 0;
                } else if ((rotation > 45) && (rotation <= 135)) {
                    rotation = 90;
                } else if ((rotation > 135) && (rotation <= 225)) {
                    rotation = 180;
                } else if ((rotation > 225) && (rotation <= 315)) {
                    rotation = 270;
                } else {
                    rotation = 0;
                }
                if (rotation == mOrientation)
                    return;
                mOrientation = rotation;

            }
        };
        mOrEventListener.enable();
    }


    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            return mRememberedNormalOrientation;
        }
    }

    @Override
    public void onStart() {
        mOrientationListener.enable();
    }

    @Override
    public void onStop() {
        mOrientationListener.disable();
    }

    public interface OnCameraPrepareListener {
        void onPrepare(CameraManager.CameraDirection cameraDirection);
    }

    public interface SwitchCameraCallBack {
        public void switchCamera(boolean isSwitchFromFront);
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        mIsRecording = true;
        try {
            //       CameraInstance.getInstance().openOrCloseFlash(true);
            mCamera.unlock();
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOrientationHint(mCameraManager.getCameraDirection() == CameraManager.CameraDirection.CAMERA_BACK ? 90 : 270);//视频旋转90度
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioChannels(2);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            Camera.Size previewSize = parameters.getPreviewSize();
            if (previewSize.height > 720 || previewSize.width > 1280) {
                mMediaRecorder.setVideoSize(1280, 720);
            } else {
                mMediaRecorder.setVideoSize(previewSize.width, previewSize.height);
            }
            mMediaRecorder.setPreviewDisplay(getHolder().getSurface());
            mMediaRecorder.setVideoEncodingBitRate(2 * previewSize.width * previewSize.height);
            mMediaRecorder.setMaxDuration(MAX_DURATION - 1000);
            mStrVideoPath = Utils.createVideoPath(mContext);
            mMediaRecorder.setOutputFile(mStrVideoPath);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.e("CameraView", "rcord error: " + e.toString());
            releaseResources();
        } catch (IOException e) {
            Log.e("CameraView", "rcord error: " + e.toString());
            releaseResources();
        }
    }

    /**
     * 开始录制
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    /**
     * 停止录制
     */
    public synchronized void stopRecording(OnCameraListener onCameraListener) {
        if (mMediaRecorder == null) {
            return;
        }
        boolean isSuccess = false;
        try {
            mMediaRecorder.stop();
            isSuccess = true;
        } catch (Exception e) {
            Utils.deleteFile(mStrVideoPath);
        }
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();
        releaseResources();
        if (onCameraListener != null) {
            onCameraListener.onAfterUseCamera(isSuccess, mCameraManager.getCameraDirection() == CameraManager.CameraDirection.CAMERA_BACK ?
                    Constants.VIDEO_DEGREE_90 : Constants.VIDEO_DEGREE_270, mStrVideoPath);
        }
        if (isSuccess) {
            if (GBSPreferences.getInstacne().getSaveOriginalStateNum() == 1) {
                final String dstPath = Utils.createAlbumVideoPath();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.copyFile(mContext.getApplicationContext(), mStrVideoPath, dstPath);
                    }
                }).start();
            }
        }
    }

    /**
     * 释放资源
     */
    public void releaseResources() {
        mIsRecording = false;
        // CameraInstance.getInstance().openOrCloseFlash(false);
    }

}
