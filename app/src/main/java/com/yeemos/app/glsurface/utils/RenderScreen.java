/*
 * Copyright (C) 2013 MorihiroSoft
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yeemos.app.glsurface.utils;


import com.yeemos.yeemos.jni.ShaderJNILib;

class RenderScreen {

    private final int mFboTexId;


    private ShaderViewDraw mShaderViewDraw;

    //---------------------------------------------------------------------
    // PUBLIC METHODS
    //---------------------------------------------------------------------
    public RenderScreen(int id, boolean isBackCamera) {
        mFboTexId = id;

        initGL(isBackCamera);
    }


    public void draw() {
        GlUtil.checkGlError("draw_S");
    //    ShaderJNILib.step();
        mShaderViewDraw.draw();

        GlUtil.checkGlError("draw_E");
    }

    //---------------------------------------------------------------------
    // PRIVATE...
    //---------------------------------------------------------------------
    public void initGL(boolean isBackCamera) {
        GlUtil.checkGlError("initGL_S");
//        if (mShaderViewDraw == null) {
//            mShaderViewDraw = new ShaderViewDraw(mFboTexId, isBackCamera);
//        } else {
//            mShaderViewDraw.resetTextureVertices(isBackCamera);
//        }
        GlUtil.checkGlError("initGL_E");
    }
}
