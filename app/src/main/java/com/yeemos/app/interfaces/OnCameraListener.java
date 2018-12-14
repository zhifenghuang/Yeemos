package com.yeemos.app.interfaces;

import android.graphics.Bitmap;

/**
 * Created by gigabud on 15-12-4.
 */
public interface OnCameraListener {
    void showPicture(Bitmap bmp);
    void onAfterUseCamera(boolean isSuccess, int shaderType, String srcPath);
}
