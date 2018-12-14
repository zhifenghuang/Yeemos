package com.yeemos.app.fragment;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.gigabud.core.util.BaseUtils;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CutAvaterView;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-6-21.
 */
public class PhotoPreviewFragment extends BaseFragment implements View.OnClickListener, View.OnTouchListener {

    public static final String PREVIEW_PICTURE = "preview_picture";
    private int mCameraUseType;

    private int current_Top, current_Right, current_Bottom, current_Left;// 当前图片上下左右坐标
    private int start_x, start_y, current_x, current_y;// 触摸位置
    private float beforeLenght, afterLenght;// 两触点距离
    private float scale_temp = 1.0f;// 缩放比例
    private int mCutLeft, mCutTop, mCutRight, mCutBottom;
    private Bitmap mScreenBmp;

    /**
     * 模式 NONE：无 DRAG：拖拽. ZOOM:缩放
     *
     * @author zhangjia
     */
    private enum MODE {
        NONE, DRAG, ZOOM

    }

    private MODE mode = MODE.NONE;// 默认模式

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_photo_preview;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION_TWO;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            goBack();
            return;
        }
        mCameraUseType = bundle.getInt(CameraForChatOrAvaterFragment.USE_CAMERA_TYPE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnClose).setOnClickListener(this);
        view.findViewById(R.id.tvSend).setOnClickListener(this);
        if (mCameraUseType == CameraFragment.TYPE_CAMERA_FOR_AVATER) {
            view.findViewById(R.id.cutAvaterView).setVisibility(View.VISIBLE);
            view.findViewById(R.id.ivPreviewPicture).setOnTouchListener(this);
        }
    }

    public void onStart() {
        super.onStart();
        final Bitmap bmp = BitmapCacheManager.getInstance().get(PREVIEW_PICTURE);
        if (bmp == null) {
            goBack();
            return;
        }

        final ImageView ivPreviewPicture = (ImageView) getView().findViewById(R.id.ivPreviewPicture);
        ivPreviewPicture.setImageBitmap(bmp);
//        if (mCameraUseType == CameraFragment.TYPE_CAMERA_FOR_AVATER) {
//            ivPreviewPicture.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (getView() == null) {
//                        return;
//                    }
//
//                    CutAvaterView cutAvaterView = (CutAvaterView) getView().findViewById(R.id.cutAvaterView);
//                    int cutWidth = cutAvaterView.getWidth();
//
//                    if (bmp.getWidth() < cutWidth || bmp.getHeight() < cutWidth) {
//                        mCutLeft = cutAvaterView.getCutLeft();
//                        mCutTop = cutAvaterView.getCutTop();
//                        mCutRight = cutAvaterView.getCutRight();
//                        mCutBottom = cutAvaterView.getCutBottom();
//
//                        //setScale(ivPreviewPicture, Math.max(cutWidth * 1.0f / bmp.getWidth(), cutWidth * 1.0f / bmp.getHeight()));
//                        setPosition(ivPreviewPicture, mCutLeft, mCutTop, mCutRight, mCutBottom);
//                    }
//                }
//            }, 100);
//        }
        ((BaseActivity) getActivity()).setScreenFull(true);
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        if (mCameraUseType == CameraFragment.TYPE_CAMERA_FOR_CHAT) {
            setOnlineText(R.id.tvSend, "pblc_btn_send");
        } else {
            setOnlineText(R.id.tvSend, "pblc_btn_finish");
        }
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                BitmapCacheManager.getInstance().remove(PREVIEW_PICTURE);
                goBack();
                break;
            case R.id.tvSend:
                if (!BaseUtils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ((BaseActivity) getActivity()).requestPermission(BaseActivity.PERMISSION_WRITE_EXTERNAL_STORAGE_CODE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    return;
                }
                Bitmap bmp = BitmapCacheManager.getInstance().get(PREVIEW_PICTURE);
                if (bmp == null) {
                    goBack();
                    return;
                }
                if (mCameraUseType == CameraFragment.TYPE_CAMERA_FOR_CHAT) {
                    DataManager.getInstance().setFilePath(Utils.saveJpegForChat(bmp, getActivity()));
                } else {
                    View rl = getView().findViewById(R.id.rl);
                    mScreenBmp = Bitmap.createBitmap(rl.getWidth(), rl.getHeight(), Bitmap.Config.ARGB_8888);
                    rl.draw(new Canvas(mScreenBmp));
                    ImageView iv = (ImageView) getView().findViewById(R.id.ivPreviewPicture);
                    iv.setImageBitmap(mScreenBmp);
                    iv.layout(0, 0, mScreenBmp.getWidth(), mScreenBmp.getHeight());
                    bmp.recycle();
                    BitmapCacheManager.getInstance().remove(PhotoPreviewFragment.PREVIEW_PICTURE);
                    int x = 0;
                    int y = mScreenBmp.getHeight() * 5 / 12 - mScreenBmp.getWidth() / 2;
                    Bitmap newBmp = Bitmap.createBitmap(mScreenBmp, x, y, mScreenBmp.getWidth(), mScreenBmp.getWidth());
                    BitmapCacheManager.getInstance().put(PhotoPreviewFragment.PREVIEW_PICTURE, newBmp);
                }
                getActivity().finish();
                break;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        BitmapCacheManager.getInstance().evictAll();
        if (mScreenBmp != null && !mScreenBmp.isRecycled()) {
            mScreenBmp.recycle();
        }
        mScreenBmp = null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.ivPreviewPicture) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    CutAvaterView cutAvaterView = (CutAvaterView) getView().findViewById(R.id.cutAvaterView);
                    mCutLeft = cutAvaterView.getCutLeft();
                    mCutTop = cutAvaterView.getCutTop();
                    mCutRight = cutAvaterView.getCutRight();
                    mCutBottom = cutAvaterView.getCutBottom();
                    onTouchDown(event);
                    break;
                // 多点触摸
                case MotionEvent.ACTION_POINTER_DOWN:
                    onPointerDown(event);
                    break;

                case MotionEvent.ACTION_MOVE:
                    onTouchMove(v, event);
                    break;
                case MotionEvent.ACTION_UP:
                    mode = MODE.NONE;
                    break;

                // 多点松开
                case MotionEvent.ACTION_POINTER_UP:
                    mode = MODE.NONE;
                    break;
            }
        }
        return true;
    }

    /**
     * 按下
     **/
    private void onTouchDown(MotionEvent event) {
        mode = MODE.DRAG;
        start_x = (int) event.getX();
        start_y = (int) event.getY();

    }

    /**
     * 两个手指 只能放大缩小旋转
     **/
    private void onPointerDown(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            mode = MODE.ZOOM;
            beforeLenght = getDistance(event);// 获取两点的距离
        }
    }

    /**
     * 移动的处理
     **/
    private void onTouchMove(View view, MotionEvent event) {
        int left = 0, top = 0, right = 0, bottom = 0;
        /** 处理拖动 **/
        if (mode == MODE.DRAG) {
            current_x = (int) event.getX();
            current_y = (int) event.getY();
            left = current_x - start_x + view.getLeft();
            right = left + view.getWidth();
            top = current_y - start_y + view.getTop();
            bottom = top + view.getHeight();
            setPosition(view, left, top, right, bottom);
        } else if (mode == MODE.ZOOM) {
            afterLenght = getDistance(event);// 获取两点的距离
            scale_temp = afterLenght / beforeLenght;// 求的缩放的比例
            setScale(view, scale_temp);
            beforeLenght = afterLenght;
        }
        view.invalidate();

    }

    /**
     * 实现处理拖动
     **/
    private void setPosition(View v, int left, int top, int right, int bottom) {
        if (left > mCutLeft) {
            right = mCutLeft + right - left;
            left = mCutLeft;
        }
        if (top > mCutTop) {
            bottom = mCutTop + bottom - top;
            top = mCutTop;
        }
        if (right < mCutRight) {
            left = mCutRight - right + left;
            right = mCutRight;
        }
        if (bottom < mCutBottom) {
            top = mCutBottom - bottom + top;
            bottom = mCutBottom;
        }
        v.layout(left, top, right, bottom);
    }

    /**
     * 获取两点的距离
     **/
    private float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 处理缩放
     **/
    private void setScale(View view, float scale) {
        if (scale < 1) {
            float minScaleX = (mCutRight - mCutLeft) * 1.0f / view.getWidth();
            float minScaleY = (mCutBottom - mCutTop) * 1.0f / view.getHeight();
            if (scale < minScaleX || scale < minScaleY) {
                scale = Math.max(minScaleX, minScaleY);
            }
        }
        int disX = (int) (view.getWidth() * Math.abs(1 - scale)) / 4;// 获取缩放水平距离
        int disY = (int) (view.getHeight() * Math.abs(1 - scale)) / 4;// 获取缩放垂直距离
        if (scale > 1) {
            current_Left = view.getLeft() - disX;
            current_Top = view.getTop() - disY;
            current_Right = view.getRight() + disX;
            current_Bottom = view.getBottom() + disY;
            setPosition(view, current_Left, current_Top, current_Right, current_Bottom);
        } else if (scale < 1) {
            current_Left = view.getLeft() + disX;
            current_Top = view.getTop() + disY;
            current_Right = view.getRight() - disX;
            current_Bottom = view.getBottom() - disY;
            setPosition(view, current_Left, current_Top, current_Right, current_Bottom);
        }
    }
}
