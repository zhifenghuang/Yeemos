package com.yeemos.app.glsurface.utils;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

public class RenderSrfTex {

    private int mFboTexId;
    //   private final CameraRecorder mRecorder;


    private EGLDisplay mSavedEglDisplay = null;
    private EGLSurface mSavedEglDrawSurface = null;
    private EGLSurface mSavedEglReadSurface = null;
    private EGLContext mSavedEglContext = null;

    private ShaderViewDraw mShaderViewDraw;

    private boolean mIsBackCamera;
    private int mViewWidth;
    private int mViewHeight;

    //---------------------------------------------------------------------
    // PUBLIC METHODS
    //---------------------------------------------------------------------
    public RenderSrfTex() {

    }

    public void init(int id, boolean isBackCamera, int viewWidth, int viewHeight) {
        mFboTexId = id;
        mIsBackCamera = isBackCamera;
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
        initGL();
    }

    public void draw() {
        saveRenderState();
        {
            GlUtil.checkGlError("draw_S");


            mShaderViewDraw.draw();


            GlUtil.checkGlError("draw_E");
        }
        restoreRenderState();
    }

    private void initGL() {
        GlUtil.checkGlError("initGL_S");
        if (mShaderViewDraw == null) {
            mShaderViewDraw = new ShaderViewDraw();
        }
        mShaderViewDraw.resetTextureID(mFboTexId, mIsBackCamera, mViewWidth, mViewHeight);
        GlUtil.checkGlError("initGL_E");
    }


    private void saveRenderState() {
        mSavedEglDisplay = EGL14.eglGetCurrentDisplay();
        mSavedEglDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
        mSavedEglReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
        mSavedEglContext = EGL14.eglGetCurrentContext();
    }

    private void restoreRenderState() {
        if (!EGL14.eglMakeCurrent(
                mSavedEglDisplay,
                mSavedEglDrawSurface,
                mSavedEglReadSurface,
                mSavedEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }
}
