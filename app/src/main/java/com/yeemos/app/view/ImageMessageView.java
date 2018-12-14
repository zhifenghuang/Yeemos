package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

import java.io.File;

/**
 * Created by gigabud on 16-4-19.
 */
public class ImageMessageView extends View {

    private Bitmap mResendBmp;
    private Bitmap mThumBmp;
    private IMsg mMsg;
    private Rect mSrcRect, mResendSrcRect;
    private RectF mDstRectF, mArcRectF, mResendDstRectF;
    private Paint mPaint;
    private long mMyUserID;


    public ImageMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImageMessage(final IMsg msg, final long myUserID) {
        mMsg = msg;
        mMyUserID = myUserID;
        mThumBmp = null;
        File photoPath = new File(Preferences.getInstacne().getDownloadFilePathByName(msg.getThumb()));
        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                //这里我们拿到回掉回来的bitmap，可以加载到我们想使用到的地方
                mThumBmp = bitmap;
                if (msg.getsUID() != myUserID && msg.getRecvStatus().GetValues() < IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ_CONFIRM.GetValues()) {
                    IChat.getInstance().readMsg(mMsg);
                }
                invalidate();
            }
        };
        if (photoPath.exists()) {
            Glide.with(BaseApplication.getAppContext())
                    .load(Uri.fromFile(photoPath))
                    .asBitmap()   //强制转换Bitmap
                    .into(target);
        } else {
            String imageURL = Preferences.getInstacne().getChatFileDownloadURLByName(msg.getThumb());
            Glide.with(BaseApplication.getAppContext())
                    .load(imageURL)
                    .asBitmap()   //强制转换Bitmap
                    .into(target);

        }
        invalidate();

    }

    /**
     *
     */
    private Paint getPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(Utils.dip2px(getContext(), 3));
        return paint;
    }


    private Bitmap getResendBmp() {
        if (mResendBmp == null) {
            mResendBmp = ((BitmapDrawable) getResources().getDrawable(R.drawable.resend_photo)).getBitmap();
        }
        return mResendBmp;
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (mPaint == null) {
            mPaint = getPaint();
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        if (mThumBmp != null) {
            if (mSrcRect == null) {
                mSrcRect = new Rect(0, 0, mThumBmp.getWidth(), mThumBmp.getHeight());
            }
            mDstRectF = getDstRect(mThumBmp);
            canvas.drawBitmap(mThumBmp, mSrcRect, mDstRectF, null);
        }

        if (mMsg == null || mMsg.getsUID() != mMyUserID) {
            return;
        }

        int statusValue = mMsg.getSendStatus().GetValues();
        if (statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_SUCCESS.GetValues() ||
                statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_RECEIVED.GetValues()) {
            return;
        } else if (statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ.GetValues()) {
            if (mPaint == null) {
                mPaint = getPaint();
            }
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(getResources().getColor(R.color.half_transparent));
            canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
            return;
        }
        if (statusValue != IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE.GetValues()) {
            if (mMsg.getProgress() >= 100) {
                return;
            }
        }
        if (mPaint == null) {
            mPaint = getPaint();
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getResources().getColor(R.color.half_transparent));
        float centerX = getWidth() * 1.0f / 2;
        float centerY = getHeight() * 1.0f / 2;
        canvas.drawCircle(centerX, centerY, getWidth() * 0.25f, mPaint);

        if (mArcRectF == null) {
            float radius = getWidth() * 0.2f;
            mArcRectF = new RectF(centerX - radius, centerY - radius, centerX
                    + radius, centerY + radius);
        }

        if (statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE.GetValues()) {
            if (mResendSrcRect == null) {
                mResendSrcRect = new Rect(0, 0, getResendBmp().getWidth(), getResendBmp().getHeight());
            }
            if (mResendDstRectF == null) {
                mResendDstRectF = new RectF(centerX - getResendBmp().getWidth() * 0.25f, centerY - getResendBmp().getHeight() * 0.25f,
                        centerX + getResendBmp().getWidth() * 0.25f, centerY + getResendBmp().getHeight() * 0.25f);
            }
            canvas.drawBitmap(getResendBmp(), mResendSrcRect, mResendDstRectF, null);
        }else{
            float progress = mMsg.getProgress() * 1f / 100;
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(progress > 0.9f ? getResources().getColor(R.color.color_45_223_227) : Color.WHITE);
            canvas.drawArc(mArcRectF, -180, progress * 360f, false, mPaint);
        }


    }

    public RectF getDstRect(Bitmap bmp) {
        float bmpRatio = bmp.getHeight() * 1.0f / bmp.getWidth();
        float dstRatio = getHeight() * 1.0f / getWidth();
        int width, height;
        if (bmpRatio < dstRatio) {
            width = getWidth();
            height = (int) (width * bmpRatio);
            return new RectF(0, (getHeight() - height) / 2, width, (getHeight() + height) / 2);
        } else {
            height = getHeight();
            width = (int) (height / bmpRatio);
            return new RectF((getWidth() - width) / 2, 0, (getWidth() + width) / 2, height);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Utils.dip2px(getContext(), 88), Utils.dip2px(getContext(), 155));
    }

}
