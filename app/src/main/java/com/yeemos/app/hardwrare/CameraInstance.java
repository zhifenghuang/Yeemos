package com.yeemos.app.hardwrare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.gbsocial.preferences.GBSPreferences;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.fragment.CameraFragment;
import com.yeemos.app.fragment.PhotoPreviewFragment;
import com.yeemos.app.interfaces.OnCameraListener;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.utils.BrightnessUtils;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.GLShaderJNIView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gigabud on 15-11-9.
 * 相机实例
 */
public class CameraInstance {

    private Camera mCamera;
    private Camera.Parameters mCameraParams;

    private int mDefaultCameraId = -1;
    private int mCameraSelection = Camera.CameraInfo.CAMERA_FACING_BACK;  //默认设为后置相机

    private boolean mIsPreviewing = false;
    private volatile static CameraInstance mCameraInstance;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mDefaultScreenResolution = -1;
    private String mFlashType = Camera.Parameters.FLASH_MODE_OFF;// 闪光灯是否开启
    private int mScreenWidth, mScreenHeight;
    private float mRate;
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static final Object mLock = new Object();
    private DisplayMetrics mDisplaymetrics;

    public void setScreenWH(int screenWidth, int screenHeight) {
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mRate = mScreenHeight * 1.0f / screenWidth;
    }


    private CameraInstance() {

    }

    public static CameraInstance getInstance() {
        if (mCameraInstance == null) {
            synchronized (CameraInstance.class) {
                if (mCameraInstance == null) {
                    mCameraInstance = new CameraInstance();
                }
            }
        }
        return mCameraInstance;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public boolean isBackCamera() {
        return mCameraSelection == Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    public String getFlashType() {
        return mFlashType;
    }

    public int getCameraSelection() {
        return mCameraSelection;
    }

    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    public int getPreviewHeight() {
        return mPreviewHeight;
    }

    private boolean mIsCameraInit = false;

//    public boolean isCameraInit() {
//        if (mCamera == null) {
//            mIsCameraInit = false;
//        }
//        return mIsCameraInit;
//    }


    public boolean tryOpenCamera() {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                int numberOfCameras = Camera.getNumberOfCameras();

                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == mCameraSelection) {
                        mDefaultCameraId = i;
                        break;
                    }
                }
            }
            stopPreview();
            if (mCamera != null)
                mCamera.release();

            if (mDefaultCameraId >= 0)
                mCamera = Camera.open(mDefaultCameraId);
            else
                mCamera = Camera.open();
            if (mCamera != null) {
                mCameraParams = mCamera.getParameters();
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public void startPreview() {
        synchronized (mLock) {
            if (mIsPreviewing) {
                stopPreview();
            }
            if (mCamera != null) {
                mCamera.startPreview();
                mIsPreviewing = true;
            }
        }
    }

    public void stopCamera() {
        synchronized (mLock) {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                if (mIsPreviewing) {
                    mCamera.stopPreview();
                    mIsPreviewing = false;
                }
                mCamera.release();
                mCamera = null;
            }
            //        mFlashType = Camera.Parameters.FLASH_MODE_OFF;
            mIsCameraInit = false;
            mCameraSelection = Camera.CameraInfo.CAMERA_FACING_BACK;
            mCameraInstance = null;
        }
    }

    public void stopPreview() {
        if (mIsPreviewing && mCamera != null) {
            mIsPreviewing = false;
            mCamera.stopPreview();
        }
    }

    public void initCamera(int frameRate, Context context) {
        synchronized (mLock) {
            if (mCamera == null) {
                return;
            }
            mCameraParams = mCamera.getParameters();
            Camera.Size previewSize = getPreviewSize(mCameraParams.getSupportedPreviewSizes(), 480);
            if (previewSize == null) {
                List<Camera.Size> resolutionList = mCameraParams.getSupportedPreviewSizes();
                if (resolutionList != null && resolutionList.size() > 0) {
                    Collections.sort(resolutionList, new Utils.ResolutionComparator());
                    if (mDefaultScreenResolution == -1) {
                        boolean hasSize = false;
                        for (int i = 0; i < resolutionList.size(); i++) {
                            Camera.Size size = resolutionList.get(i);
                            if (size != null && size.width == 640 && size.height == 320) {
                                previewSize = size;
                                hasSize = true;
                                break;
                            }
                        }
                        if (!hasSize) {
                            int mediumResolution = resolutionList.size() / 2;
                            if (mediumResolution >= resolutionList.size())
                                mediumResolution = resolutionList.size() - 1;
                            previewSize = resolutionList.get(mediumResolution);
                        }
                    } else {
                        if (mDefaultScreenResolution >= resolutionList.size())
                            mDefaultScreenResolution = resolutionList.size() - 1;
                        previewSize = resolutionList.get(mDefaultScreenResolution);
                    }

                }
            }
            if (previewSize != null) {
                mPreviewWidth = previewSize.width;
                mPreviewHeight = previewSize.height;
                mCameraParams.setPreviewSize(mPreviewWidth, mPreviewHeight);
            }

            mCameraParams.setPreviewFrameRate(frameRate);

            mCameraParams.setPictureFormat(PixelFormat.JPEG);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                //mCamera.setDisplayOrientation(Utils.determineDisplayOrientation(context, mDefaultCameraId));
                mCamera.setDisplayOrientation(90);
                List<String> focusModes = mCameraParams.getSupportedFocusModes();
                if (focusModes != null) {
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    } else {
                        mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                    }
                }
            } else {
                mCamera.setDisplayOrientation(90);
            }
            //        mCameraParams.setMeteringAreas();
            mCamera.setParameters(mCameraParams);
            //        mCamera.cancelAutoFocus();
            mIsCameraInit = true;
            mCamera.autoFocus(mAutoFocusTakePictureCallback);
        }
    }

    /**
     * 闪光灯开闭
     */

    public void switchFlash() {
        if (mCamera == null) {
            Log.e("CameraInstance", "Camera is not opened!");
            return;
        }

        if (mFlashType.equals(Camera.Parameters.FLASH_MODE_OFF)) {
            mFlashType = Camera.Parameters.FLASH_MODE_ON;
        }
//        else if (mFlashType.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
//            mFlashType = Camera.Parameters.FLASH_MODE_ON;
//        }
        else {
            mFlashType = Camera.Parameters.FLASH_MODE_OFF;
        }
    }

    /**
     * 切换前后摄像头
     */
    public void switchCameraSelection() {
        mCameraSelection = (mCameraSelection == Camera.CameraInfo.CAMERA_FACING_BACK ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK);
        mIsCameraInit = false;
    }

    public Camera.Size getPreviewSize(List<Camera.Size> list, int th) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        if (getDisplaymetrics().widthPixels == 480 && getDisplaymetrics().heightPixels == 800) {
            for (Camera.Size s : list) {
                if (s.width == 640 && s.height == 480) {
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
        if (getDisplaymetrics().widthPixels == 480 && getDisplaymetrics().heightPixels == 800) {
            for (Camera.Size s : list) {
                if (s.width == 640 && s.height == 480) {
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


    private static final int FOCUS_AREA_SIZE = 300;

    public void foucusCamera(MotionEvent event) {
        if (mCamera != null) {
//            List<Camera.Area> focusAreas = new ArrayList<>();
//            List<Camera.Area> meteringAreas = new ArrayList<>();
//            Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
//            Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f);
//            focusAreas.add(new Camera.Area(focusRect, 1));
//            meteringAreas.add(new Camera.Area(meteringRect, 1));
//            mCameraParams.setFocusAreas(focusAreas);
//            mCameraParams.setMeteringAreas(meteringAreas);
//            mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
//            try {
//                mCamera.setParameters(mCameraParams);
//
//            } catch (Exception e) {
//                Log.e("Focus problem", e.toString());
//            }
//            mCamera.autoFocus(mAutoFocusTakePictureCallback);

            try {
                mCameraParams = mCamera.getParameters();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (Build.VERSION.SDK_INT >= 14) {
                if (mCameraParams.getMaxNumFocusAreas() <= 0) {

                    mCamera.autoFocus(mAutoFocusTakePictureCallback);
                    return;
                }

                List<Camera.Area> focusAreas = new ArrayList<>();
                List<Camera.Area> meteringAreas = new ArrayList<>();
                Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
                Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f);
                focusAreas.add(new Camera.Area(focusRect, 1));
                meteringAreas.add(new Camera.Area(meteringRect, 1));
                mCameraParams.setFocusAreas(focusAreas);
                mCameraParams.setMeteringAreas(meteringAreas);
                mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                try {
                    mCamera.setParameters(mCameraParams);
                } catch (Exception e) {
                    Log.e("Focus problem", e.toString());
                }
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {

            } else {
                //          mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    };

    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(FOCUS_AREA_SIZE * coefficient).intValue();
        int left = clamp((int) x - areaSize / 2, 0, mPreviewWidth / 2 - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, mPreviewHeight / 2 - areaSize);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        Matrix matrix = new Matrix();
        matrix.mapRect(rectF);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public void openOrCloseFlash(boolean isOpen) {
        if (mFlashType.equals(Camera.Parameters.FLASH_MODE_ON)) {
            mCameraParams = mCamera.getParameters();
            mCameraParams.setFlashMode(isOpen ? mFlashType : Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mCameraParams);
        }
    }


    public void takePicture(final OnCameraListener onCameraListener, final Context context, final int cameraForType) {
        mCameraParams = mCamera.getParameters();
        Camera.Size pictureSize = getPictureSize(mCameraParams.getSupportedPictureSizes(), mScreenWidth / 2);
        if (pictureSize != null) {
            mCameraParams.setPictureSize(pictureSize.width, pictureSize.height);
            mCameraParams.setFlashMode(mFlashType);
            mCamera.setParameters(mCameraParams);
        }
        mCamera.takePicture(null, null,
                new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {
                        stopPreview();
                        if (mFlashType.equals(Camera.Parameters.FLASH_MODE_ON)) {
                            mCameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            mCamera.setParameters(mCameraParams);
                        }
                        if (data != null) {
                            BitmapCacheManager.getInstance().evictAll();  //释放Cache的所有图片，防止之后溢出
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                            if (mCameraSelection == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                bmp = Utils.rotateBmp(bmp, 90f);
                            } else {
                                bmp = Utils.rotateBmp(bmp, -90f);
                            }
//                            int screenWidth = getDisplaymetrics().widthPixels;
//                            int screenHeight = getDisplaymetrics().heightPixels;
//                            float ratio1 = bmp.getHeight() * 1.0f / bmp.getWidth();
//                            float ratio2 = screenHeight * 1.0f / screenWidth;
//                            Log.e("aaaa", screenWidth + ", " + screenHeight+", "+bmp.getWidth()+", "+bmp.getHeight());
//                            Log.e("aaaa", ratio1 + ", " + ratio2);
//                            if (Math.abs(ratio1 - ratio2) > 0.1f) {
//                                Log.e("aaaa", "need cut");
//                                if (ratio1 < ratio2) {
//                                    int newWidth = bmp.getHeight() * screenWidth / screenHeight;
//                                    int startX = (int) ((bmp.getWidth() - newWidth) * 0.5);
//                                    bmp = Bitmap.createBitmap(bmp, startX, 0, newWidth, bmp.getHeight(), null, false);
//                                } else {
//                                    int newHeight = bmp.getWidth() * screenHeight / screenWidth;
//                                    int startY = (int) ((bmp.getHeight() - newHeight) * 0.5);
//                                    bmp = Bitmap.createBitmap(bmp, 0, startY, bmp.getWidth(), newHeight, null, false);
//                                }
//                            }
                            BitmapCacheManager.getInstance().put(cameraForType == CameraFragment.TYPE_CAMERA_FOR_POST ? GLShaderJNIView.KEY_SHADER_PICTURE : PhotoPreviewFragment.PREVIEW_PICTURE, bmp);
                            if (GBSPreferences.getInstacne().getSaveOriginalStateNum() == 1 && cameraForType == CameraFragment.TYPE_CAMERA_FOR_POST) {
                                Utils.savePhotoToAlbum(bmp, context);
                            }
                            if (onCameraListener != null) {
                                onCameraListener.showPicture(bmp);
                                //onCameraListener.onAfterUseCamera(true, Constants.PIC_SHADER_FILTER, isToChat ? Utils.saveJpegForChat(bmp, context) : Utils.saveJpeg(bmp, context));
                                onCameraListener.onAfterUseCamera(true, Constants.PIC_SHADER_FILTER, cameraForType == CameraFragment.TYPE_CAMERA_FOR_POST ? Utils.saveJpeg(bmp, context) : null);
                            }
                        }

                    }
                });
    }

    public DisplayMetrics getDisplaymetrics() {
        if (mDisplaymetrics == null) {
            mDisplaymetrics = new DisplayMetrics();
            ((WindowManager) BaseApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplaymetrics);
        }
        return mDisplaymetrics;
    }
}

