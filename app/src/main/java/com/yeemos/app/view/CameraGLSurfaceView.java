package com.yeemos.app.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.gbsocial.preferences.GBSPreferences;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.glsurface.utils.CameraRenderer;
import com.yeemos.app.glsurface.utils.MediaAudioEncoder;
import com.yeemos.app.glsurface.utils.MediaEncoder;
import com.yeemos.app.glsurface.utils.MediaMuxerWrapper;
import com.yeemos.app.glsurface.utils.MediaVideoEncoder;
import com.yeemos.app.glsurface.utils.OnCameraUseListener;
import com.yeemos.app.hardwrare.CameraManager;
import com.yeemos.app.hardwrare.SensorControler;
import com.yeemos.app.interfaces.IActivityLifiCycle;
import com.yeemos.app.interfaces.ICameraOperation;
import com.yeemos.app.interfaces.OnCameraListener;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;
import com.yeemos.yeemos.jni.ShaderJNILib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;


/**
 * Created by gigabud on 17-3-3.
 */

public class CameraGLSurfaceView extends GLSurfaceView implements ICameraOperation, IActivityLifiCycle {

    public static final String TAG = "CameraGLSurfaceView";

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
    //   private MediaRecorder mMediaRecorder;
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

    private float mRate;
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();


    private CameraRenderer mRenderer = null;
    //   private CameraRecorder mRecorder = null;

    /**
     * muxer for audio/video recording
     */
    private MediaMuxerWrapper mMuxer;


    public CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(false, 0, 0);

        mContext = context;
        mCameraManager = CameraManager.getInstance(context);
        mCameraId = mCameraManager.getCameraDirection();

        setFocusable(true);

        mSensorControler = SensorControler.getInstance();
        mOrientationListener = new CameraOrientationListener(mContext);
        mOrientationListener.enable();

        setEGLContextClientVersion(2);
        mRenderer = new CameraRenderer(this);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        ShaderJNILib.setPlatform(Constants.PLATFORM_ANDROID);
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

    private void init(boolean translucent, int depth, int stencil) {

        /*
         * By default, GLSurfaceView() creates a RGB_565 opaque surface. If we
         * want a translucent one, we should change the surface's format here,
         * using PixelFormat.TRANSLUCENT for GL Surfaces is interpreted as any
         * 32-bit surface with alpha by SurfaceFlinger.
         */
        if (translucent) {
            this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }

        /*
         * Setup the context factory for 2.0 rendering. See ContextFactory class
         * definition below
         */
        setEGLContextFactory(new ContextFactory());

        /*
         * We need to choose an EGLConfig that matches the format of our surface
         * exactly. This is going to be done in our custom config chooser. See
         * ConfigChooser class definition below.
         */
        setEGLConfigChooser(translucent ? new ConfigChooser(8, 8, 8, 8, depth,
                stencil) : new ConfigChooser(5, 6, 5, 0, depth, stencil));

        /* Set the renderer responsible for frame rendering */
    }

    public static class ContextFactory implements
            GLSurfaceView.EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public EGLContext createContext(EGL10 egl, EGLDisplay display,
                                        EGLConfig eglConfig) {
            Log.w(TAG, "creating OpenGL ES 2.0 context");
            checkEglError("Before eglCreateContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
            EGLContext context = egl.eglCreateContext(display, eglConfig,
                    EGL10.EGL_NO_CONTEXT, attrib_list);
            checkEglError("After eglCreateContext", egl);
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display,
                                   EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    private static void checkEglError(String prompt, EGL10 egl) {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }


    private static class ConfigChooser implements
            GLSurfaceView.EGLConfigChooser {

        public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
            mRedSize = r;
            mGreenSize = g;
            mBlueSize = b;
            mAlphaSize = a;
            mDepthSize = depth;
            mStencilSize = stencil;
        }

        /*
         * This EGL config specification is used to specify 2.0 rendering. We
         * use a minimum size of 4 bits for red/green/blue, but will perform
         * actual matching in chooseConfig() below.
         */
        private static int EGL_OPENGL_ES2_BIT = 4;
        private static int[] s_configAttribs2 = {EGL10.EGL_RED_SIZE, 4,
                EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

            /*
             * Get the number of minimally matching EGL configurations
             */
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException(
                        "No configs match configSpec");
            }

            /*
             * Allocate then read the array of minimally matching EGL configs
             */
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs,
                    num_config);
            /*
             * Now return the "best" one
             */
            return chooseConfig(egl, display, configs);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                      EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);

                // We need at least mDepthSize and mStencilSize bits
                if (d < mDepthSize || s < mStencilSize)
                    continue;

                // We want an *exact* match for red/green/blue/alpha
                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);

                if (r == mRedSize && g == mGreenSize && b == mBlueSize
                        && a == mAlphaSize)
                    return config;
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                     EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }

        private void printConfigs(EGL10 egl, EGLDisplay display,
                                  EGLConfig[] configs) {
            int numConfigs = configs.length;
            Log.w(TAG, String.format("%d configurations", numConfigs));
            for (int i = 0; i < numConfigs; i++) {
                Log.w(TAG, String.format("Configuration %d:\n", i));
                printConfig(egl, display, configs[i]);
            }
        }

        private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attributes = {EGL10.EGL_BUFFER_SIZE, EGL10.EGL_ALPHA_SIZE,
                    EGL10.EGL_BLUE_SIZE,
                    EGL10.EGL_GREEN_SIZE,
                    EGL10.EGL_RED_SIZE,
                    EGL10.EGL_DEPTH_SIZE,
                    EGL10.EGL_STENCIL_SIZE,
                    EGL10.EGL_CONFIG_CAVEAT,
                    EGL10.EGL_CONFIG_ID,
                    EGL10.EGL_LEVEL,
                    EGL10.EGL_MAX_PBUFFER_HEIGHT,
                    EGL10.EGL_MAX_PBUFFER_PIXELS,
                    EGL10.EGL_MAX_PBUFFER_WIDTH,
                    EGL10.EGL_NATIVE_RENDERABLE,
                    EGL10.EGL_NATIVE_VISUAL_ID,
                    EGL10.EGL_NATIVE_VISUAL_TYPE,
                    0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                    EGL10.EGL_SAMPLES,
                    EGL10.EGL_SAMPLE_BUFFERS,
                    EGL10.EGL_SURFACE_TYPE,
                    EGL10.EGL_TRANSPARENT_TYPE,
                    EGL10.EGL_TRANSPARENT_RED_VALUE,
                    EGL10.EGL_TRANSPARENT_GREEN_VALUE,
                    EGL10.EGL_TRANSPARENT_BLUE_VALUE,
                    0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                    0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                    0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                    0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                    EGL10.EGL_LUMINANCE_SIZE, EGL10.EGL_ALPHA_MASK_SIZE,
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RENDERABLE_TYPE,
                    0x3042 // EGL10.EGL_CONFORMANT
            };
            String[] names = {"EGL_BUFFER_SIZE", "EGL_ALPHA_SIZE",
                    "EGL_BLUE_SIZE", "EGL_GREEN_SIZE", "EGL_RED_SIZE",
                    "EGL_DEPTH_SIZE", "EGL_STENCIL_SIZE", "EGL_CONFIG_CAVEAT",
                    "EGL_CONFIG_ID", "EGL_LEVEL", "EGL_MAX_PBUFFER_HEIGHT",
                    "EGL_MAX_PBUFFER_PIXELS", "EGL_MAX_PBUFFER_WIDTH",
                    "EGL_NATIVE_RENDERABLE", "EGL_NATIVE_VISUAL_ID",
                    "EGL_NATIVE_VISUAL_TYPE", "EGL_PRESERVED_RESOURCES",
                    "EGL_SAMPLES", "EGL_SAMPLE_BUFFERS", "EGL_SURFACE_TYPE",
                    "EGL_TRANSPARENT_TYPE", "EGL_TRANSPARENT_RED_VALUE",
                    "EGL_TRANSPARENT_GREEN_VALUE",
                    "EGL_TRANSPARENT_BLUE_VALUE", "EGL_BIND_TO_TEXTURE_RGB",
                    "EGL_BIND_TO_TEXTURE_RGBA", "EGL_MIN_SWAP_INTERVAL",
                    "EGL_MAX_SWAP_INTERVAL", "EGL_LUMINANCE_SIZE",
                    "EGL_ALPHA_MASK_SIZE", "EGL_COLOR_BUFFER_TYPE",
                    "EGL_RENDERABLE_TYPE", "EGL_CONFORMANT"};
            int[] value = new int[1];
            for (int i = 0; i < attributes.length; i++) {
                int attribute = attributes[i];
                String name = names[i];
                if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
                    Log.w(TAG, String.format("  %s: %d\n", name, value[0]));
                } else {
                    // Log.w(TAG, String.format("  %s: failed\n", name));
                    while (egl.eglGetError() != EGL10.EGL_SUCCESS)
                        ;
                }
            }
        }

        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];
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
        turnLight(CameraManager.FlashLigthStatus.LIGHT_OFF);  //设置闪光灯
        mCameraManager.setActivityCamera(mCamera);
    }

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
        if (onCameraPrepareListener != null) {
            onCameraPrepareListener.onPrepare(mCameraId);
        }
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
        mRenderer.takePicture(true, onCameraUseListener);
        return true;
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
                initCamera();
                mRenderer.setCamera(mCamera, mCameraId == CameraManager.CameraDirection.CAMERA_BACK);
                mCameraManager.setCameraDirection(mCameraId);

                if (mCameraId == CameraManager.CameraDirection.CAMERA_FRONT) {
                    mSensorControler.lockFocus();
                } else {
                    mSensorControler.unlockFocus();
                }
            } catch (Exception e) {
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

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId.ordinal(), cameraInfo);

        int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
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
        postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isBackCamera = mCameraManager.getCameraDirection() == CameraManager.CameraDirection.CAMERA_BACK;
                ShaderJNILib.setOneShaderType(isBackCamera ? Constants.VIDEO_DEGREE_90 : Constants.VIDEO_DEGREE_270,
                        isBackCamera ? Constants.COMMON_SHADER_TYPE : Constants.BEAUTIFY_SKIN_SHADER_TYPE);
            }
        }, 500);
        mOrientationListener.enable();
    }

    @Override
    public void onStop() {
        try {
            releaseCamera();
        } catch (Exception e) {
            //相机已经关了
            e.printStackTrace();
        }
        ShaderJNILib.destroySource();
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
//        if (mRecorder == null) {
//            mStrVideoPath = Utils.createVideoPath(mContext);
//            mRecorder = new CameraRecorder();
//            mRecorder.prepareEncoder(mStrVideoPath);
//            mRenderer.setRecorder(mRecorder);
//        }
//        mRecorder.setPauseState(false);
//        mRenderer.setPauseState(false);

        try {
            mStrVideoPath = Utils.createVideoPath(mContext);
            mMuxer = new MediaMuxerWrapper(mStrVideoPath);

            int width = 720;
            DisplayMetrics dis = new DisplayMetrics();
            ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dis);
            int height = (int) (width * 1.0f * dis.heightPixels / dis.widthPixels + 0.5f);
            new MediaVideoEncoder(mMuxer, mMediaEncoderListener, width, height);
            new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
    }

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                mRenderer.setVideoEncoder((MediaVideoEncoder) encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                mRenderer.setVideoEncoder(null);
        }
    };

    /**
     * 开始录制
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    /**
     * 停止录制
     */
    public synchronized void stopRecording(OnCameraUseListener onCameraUseListener) {
        releaseResources();
        if (mMuxer == null) {
            return;
        }
//        mRecorder.setPauseState(false);
//        mRecorder.stop();
//        mRecorder = null;
//        mRenderer.setRecorder(null);
//        mRenderer.setPauseState(false);

        mMuxer.stopRecording();
        mMuxer = null;

        if (onCameraUseListener != null) {
            onCameraUseListener.recordingEnd(mStrVideoPath);
        }
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

    public void pauseRecording() {
//        mRecorder.setPauseState(true);
//        mRenderer.setPauseState(true);
    }

    /**
     * 释放资源
     */
    public void releaseResources() {
        mIsRecording = false;
    }

}
