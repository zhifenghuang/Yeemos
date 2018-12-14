package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;

import com.yeemos.app.utils.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by gigabud on 16-1-26.
 */


public class FlyEmoView extends View {

    private ArrayList<FlyEmo> mflyEmos;
    private boolean mFlag;
    private Matrix mMatrix;
    private float mSpeed;

    class FlyEmo {
        Bitmap emo;
        float currentX;
        float currentY;
        float currentScale;
    }

    public FlyEmoView(Context context, AttributeSet attr) {
        super(context, attr);
        mFlag = false;
        mSpeed = Utils.dip2px(context, 3);
    }

    public boolean addEmo(Bitmap bmp) {
        if (bmp == null) {
            return false;
        }
        if (getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
        }
        if (mflyEmos == null) {
            mflyEmos = new ArrayList<>();
        }
        Random random = new Random();
        FlyEmo flyEmo = new FlyEmo();
        flyEmo.emo = bmp;
        flyEmo.currentX = getWidth() / 10 + random.nextInt(getWidth() * 4 / 5);
        flyEmo.currentY = getHeight() / 5 + random.nextInt(getHeight() * 7 / 10);
        flyEmo.currentScale = 0.1f;
        mflyEmos.add(flyEmo);
        if (!mFlag) {
            startThread();
        }
        return true;
    }

    private synchronized void startThread() {
        if (mFlag) {
            return;
        }
        mFlag = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (mFlag) {

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        mFlag = false;
                    }
                    postInvalidate();
                }
            }
        }).start();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mflyEmos == null || mflyEmos.size() == 0) {
            mFlag = false;
            return;
        }
        if (!mFlag) {
            startThread();
        }
        FlyEmo flyEmo;
        for (int i = 0; i < mflyEmos.size(); ) {
            flyEmo = mflyEmos.get(i);
            if (flyEmo.emo == null || flyEmo.currentY <= 0) {
                mflyEmos.remove(i);
                continue;
            }
            ++i;
            if (mMatrix == null) {
                mMatrix = new Matrix();
            }
            mMatrix.reset();
            if (flyEmo.currentScale < 1.0f) {
                flyEmo.currentScale += 0.05f;
                mMatrix.postScale(flyEmo.currentScale, flyEmo.currentScale);
                mMatrix.postTranslate(
                        flyEmo.currentX + 0.5f * flyEmo.emo.getWidth()
                                * (1 - flyEmo.currentScale), flyEmo.currentY
                                + 0.5f * flyEmo.emo.getHeight()
                                * (1 - flyEmo.currentScale));
                canvas.drawBitmap(flyEmo.emo, mMatrix, null);
            } else {
                if (mSpeed <= 1.0f) {
                    mSpeed = Utils.dip2px(getContext(), 5);
                }
                flyEmo.currentY = flyEmo.currentY - mSpeed;
                mMatrix.postTranslate(flyEmo.currentX, flyEmo.currentY);
                mMatrix.postScale(flyEmo.currentScale, flyEmo.currentScale);
                canvas.drawBitmap(flyEmo.emo, mMatrix, null);
            }
        }
    }

    public void stopEmoFly() {
        if (mflyEmos != null) {
            mflyEmos.clear();
        }
        mFlag = false;
    }

}