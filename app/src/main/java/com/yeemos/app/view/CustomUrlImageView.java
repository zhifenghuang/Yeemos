package com.yeemos.app.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.gbsocial.BeansBase.PostBean;
import com.gigabud.core.http.DownloadFileManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by gigabud on 16-5-12.
 */
@SuppressLint("AppCompatCustomView")
public class CustomUrlImageView extends ImageView {
    private int mViewWidth, mViewHeight;
    private boolean mIsNeedRouctRect;
    private int mRoundRadius;

    /**
     * 3x3 矩阵，主要用于缩小放大
     */
    private Matrix mMatrix;
    /**
     * 渲染图像，使用图像为绘制图形着色
     */
    private BitmapShader mBitmapShader;

    /**
     * 绘图的Paint
     */
    private Paint mBitmapPaint;
    private RectF mRoundRect;


    public CustomUrlImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIsNeedRouctRect = false;
    }

    public CustomUrlImageView(Context context) {
        super(context);
        mIsNeedRouctRect = false;
    }

    public void setPostBean(PostBean postBean) {
        setPostBean(postBean, true);
    }

    public void setPostBean(PostBean postBean, boolean isUseThumb) {
        if (postBean.getAttachDataType() == Constants.POST_ATTACH_DATA_TYPE.ONLY_TEXT.GetValue()) {
            setImageResource(R.drawable.pure_text);
            return;
        }
        if (postBean.getAttachDataType() == Constants.POST_ATTACH_DATA_TYPE.VIDEO_TEXT.GetValue() && postBean.getVideo() != null && postBean.getVideo().length() > 0) {
            if (postBean.getImage() == null) {
                Log.e("CustomUrlImageView", "image　null");
                return;
            }
            try {
                String picURL = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(postBean.getImage(), "utf-8"),
                        URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                File file = new File(DownloadFileManager.getInstance().getFilePath(getContext(), postBean.getImage()));
                Utils.loadImage(getContext(), file, R.drawable.default_post, picURL, this, postBean.getImage());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (postBean.getAttachDataType() == Constants.POST_ATTACH_DATA_TYPE.IMAGE_TEXT.GetValue() && postBean.getImage() != null && postBean.getImage().length() > 0) {
            try {
                String imageName;
                if (isUseThumb) {
                    imageName = postBean.getImageSmall();
                    if (TextUtils.isEmpty(imageName)) {
                        imageName = postBean.getImage();
                    }
                } else {
                    imageName = postBean.getImage();
                }
                String imageURL = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(imageName, "utf-8"),
                        URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                File file = new File(DownloadFileManager.getInstance().getFilePath(getContext(), imageName));
                Utils.loadImage(getContext(), file, R.drawable.default_post, imageURL, this, imageName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }


    public void setViewWH(int viewWidth, int viewHeight) {
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
    }

    public void setNeedRouctRect(boolean isNeedRouctRect, int radius) {
        mIsNeedRouctRect = isNeedRouctRect;
        mRoundRadius = radius;
        invalidate();
    }


    public void setNeedRouctRect(boolean isNeedRouctRect) {
        mIsNeedRouctRect = isNeedRouctRect;
        mRoundRadius = Utils.dip2px(getContext(), 5);
        invalidate();
    }

    public void init() {
        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
    }

    public void onDraw(Canvas canvas) {
        if (!mIsNeedRouctRect) {
            super.onDraw(canvas);
        } else {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }

            Bitmap bmp = drawableToBitamp(drawable);
            if (bmp == null) {
                return;
            }
            if (mBitmapPaint == null || mMatrix == null) {
                init();
            }

            // 将bmp作为着色器，就是在指定区域内绘制bmp
            mBitmapShader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            float scale = Math.max(getWidth() * 1.0f / bmp.getWidth(),
                    getHeight() * 1.0f / bmp.getHeight());
            mMatrix.setScale(scale, scale);
            // 设置变换矩阵
            mBitmapShader.setLocalMatrix(mMatrix);
            // 设置shader
            mBitmapPaint.setShader(mBitmapShader);
            if (mRoundRect == null) {
                mRoundRect = new RectF(0, 0, getWidth(), getHeight());
            } else {
                mRoundRect.set(0, 0, getWidth(), getHeight());
            }
            canvas.drawRoundRect(mRoundRect, mRoundRadius, mRoundRadius,
                    mBitmapPaint);
        }
    }

    /**
     * drawable转bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap drawableToBitamp(Drawable drawable) {
        if (drawable instanceof BitmapDrawable || drawable instanceof GlideBitmapDrawable) {
            try {
                Bitmap bmp;
                if (drawable instanceof BitmapDrawable) {
                    bmp = ((BitmapDrawable) drawable).getBitmap();
                } else {
                    bmp = ((GlideBitmapDrawable) drawable).getBitmap();
                }
                if (bmp.getWidth() > 0 && bmp.getHeight() > 0) {
                    float ratio1 = bmp.getHeight() * 1.0f / bmp.getWidth();
                    float ratio2 = getHeight() * 1.0f / getWidth();
                    if (Math.abs(ratio1 - ratio2) > 0.1f) {
                        if (ratio1 < ratio2) {
                            int newWidth = bmp.getHeight() * getWidth() / getHeight();
                            if (newWidth <= 0) {
                                return bmp;
                            }
                            int startX = (int) ((bmp.getWidth() - newWidth) * 0.5);
                            bmp = Bitmap.createBitmap(bmp, startX, 0, newWidth, bmp.getHeight(), null, false);
                        } else {
                            int newHeight = bmp.getWidth() * getHeight() / getWidth();
                            int startY = (int) ((bmp.getHeight() - newHeight) * 0.5);
                            if (newHeight <= 0) {
                                return bmp;
                            }
                            bmp = Bitmap.createBitmap(bmp, 0, startY, bmp.getWidth(), newHeight, null, false);
                        }
                    }
                }
                return bmp;
            }catch (OutOfMemoryError e){
                return null;
            }
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mViewWidth == 0 || mViewHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mViewWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mViewHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
