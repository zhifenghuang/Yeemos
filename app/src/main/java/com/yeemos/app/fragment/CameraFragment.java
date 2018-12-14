package com.yeemos.app.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.preferences.GBSPreferences;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.BaseUtils;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.glsurface.utils.OnCameraUseListener;
import com.yeemos.app.hardwrare.CameraManager;
import com.yeemos.app.hardwrare.SensorControler;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.BadgeUtil;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CameraGLSurfaceView;
import com.yeemos.app.view.CircleButton;
import com.yeemos.app.view.DirectionalViewPager;
import com.yeemos.app.view.PagerDotView;
import com.yeemos.app.view.SelectItemHorizontalScrollView;
import com.yeemos.app.R;
import com.yeemos.app.view.SquareCameraContainer;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by gigabud on 15-12-23.
 */
public class CameraFragment extends BaseFragment implements View.OnClickListener, View.OnTouchListener {

    public static final int TYPE_CAMERA_FOR_POST = 0;
    public static final int TYPE_CAMERA_FOR_CHAT = 1;
    public static final int TYPE_CAMERA_FOR_AVATER = 2;

    private final static String CLASS_LABEL = "CameraFragment";
    //  private PowerManager.WakeLock mWakeLock;
    protected CircleButton mTakePhotoOrRecord;

    private CameraManager mCameraManager;
    private SquareCameraContainer mCameraContainer;

//    private SelectItemHorizontalScrollView mSelectItemHorizontalScrollView;

    private boolean mIsToFilterFragment;

    protected int mCameraUseType = TYPE_CAMERA_FOR_POST;

    private DirectionalViewPager mParentDirectionalViewPager;
    private DirectionViewPagerFragment mParentFragment;

    private boolean mUsingCamera;

    private Bitmap mTakeBmp;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_camera;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    public void setParentDirectionalViewPager(DirectionalViewPager parentDirectionalViewPager) {
        mParentDirectionalViewPager = parentDirectionalViewPager;
    }

    public void setParentFragment(DirectionViewPagerFragment parentFragment) {
        mParentFragment = parentFragment;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLayout();
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
        setOnlineText(R.id.tvSaveTime, "cht_txt_disappea");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    public void resetCameraUI() {
        if (getView() == null) {
            return;
        }
        if (TextUtils.isEmpty(MemberShipManager.getInstance().getUserID())) {
            return;
        }
        ((ImageButton) getView().findViewById(R.id.btnProfile))
                .setImageResource(Preferences.getInstacne().getValues(HomeActivity.HAD_NEW_NOTIFICATION_MESSAGE, false)
                        || Preferences.getInstacne().getValues(HomeActivity.HAD_REQUEST_FOLLOW_YOU_MESSAGE, false) ?
                        R.drawable.camera_news : R.drawable.camera_no_news);
        resetUnReadPostNum(-1);
        resetUnReadMessageNum();
    }


    private void resetUnReadMessageNum() {
        TextView tvUnReadMsgNum = (TextView) getView().findViewById(R.id.tvUnReadMsgNum);
        ImageButton btnMsg = (ImageButton) getView().findViewById(R.id.btnMsg);
        int unreadMsgNum = IMsg.getUnReadMsgFriendNum(Long.parseLong(MemberShipManager.getInstance().getUserID()));
        BadgeUtil.setBadgeCount(getActivity(), (int) unreadMsgNum);
        if (unreadMsgNum > 0) {
            btnMsg.setImageResource(R.drawable.camera_to_chat_news);
            tvUnReadMsgNum.setText("");
        } else {
            btnMsg.setImageResource(R.drawable.camera_to_chat);
            tvUnReadMsgNum.setText("");
        }
    }

    public void resetUnReadPostNum(int unreadPostNum) {
        if (getView() == null) {
            return;
        }
        unreadPostNum = unreadPostNum < 0 ? ((HomeActivity) getActivity()).getUnReadPostNum() : unreadPostNum;
        ImageButton btnHome = (ImageButton) getView().findViewById(R.id.btnHome);
        if (unreadPostNum > 0) {
            btnHome.setImageResource(R.drawable.camera_to_home_news);
        } else {
            btnHome.setImageResource(R.drawable.camera_to_home);
        }
    }


    /**
     * 显示左右滑动引导页
     */
    public void showFlipTourial() {
        getActivity().findViewById(R.id.rlTourial).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.rlTourial).setOnClickListener(this);
        getActivity().findViewById(R.id.tourialFlipView).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTourialView();
            }
        });
        ViewPager tourialViewPager = (ViewPager) getActivity().findViewById(R.id.tourialViewPager);
        final ArrayList<View> viewList = new ArrayList<>();
        View itemView;
        Locale systemLocale = Locale.getDefault();
        ImageView ivFlip;
        for (int i = 0; i < 3; ++i) {
            itemView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_flip_tourial, null);
            ivFlip = itemView.findViewById(R.id.ivFlip);

            if (systemLocale.equals(Locale.SIMPLIFIED_CHINESE) || systemLocale.equals(Locale.CHINA) || systemLocale.equals(Locale.CHINESE)
                    || systemLocale.getCountry().contains("CN")) {
                if (i == 0) {
                    ivFlip.setImageResource(R.drawable.to_right_sc);
                } else if (i == 1) {
                    ivFlip.setImageResource(R.drawable.to_left_sc);
                } else {
                    ivFlip.setImageResource(R.drawable.to_bottom_sc);
                }
            } else if (systemLocale.equals(Locale.TRADITIONAL_CHINESE) || systemLocale.equals(Locale.TAIWAN)
                    || systemLocale.getCountry().contains("HK") || systemLocale.getCountry().contains("hk")) {
                if (i == 0) {
                    ivFlip.setImageResource(R.drawable.to_right_tc);
                } else if (i == 1) {
                    ivFlip.setImageResource(R.drawable.to_left_tc);
                } else {
                    ivFlip.setImageResource(R.drawable.to_bottom_tc);
                }
            } else {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ivFlip.getLayoutParams();
                lp.topMargin = Utils.dip2px(getActivity(), 70);
                if (i == 0) {
                    ivFlip.setImageResource(R.drawable.to_right_eng);
                } else if (i == 1) {
                    ivFlip.setImageResource(R.drawable.to_left_eng);
                } else {
                    ivFlip.setImageResource(R.drawable.to_bottom_eng);
                }
            }
            viewList.add(itemView);
        }
        TextView btnGotIt = (TextView) viewList.get(2).findViewById(R.id.btnGotIt);
        btnGotIt.setVisibility(View.VISIBLE);
        btnGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTourialView();
            }
        });
        final PagerDotView pagerDotView = (PagerDotView) getActivity().findViewById(R.id.pagerDotView);
        pagerDotView.setTotalPage(viewList.size());
        pagerDotView.setCurrentPageIndex(0);
        tourialViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(viewList.get(position));
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "";
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }
        });
        tourialViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pagerDotView.setCurrentPageIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tourialViewPager.setCurrentItem(0);
    }


    private void showTourialView() {
        Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_CAMERA_FRAGMENT, true);
        View rlTourialView = getActivity().findViewById(R.id.rlTourial);
        rlTourialView.findViewById(R.id.tourialFlipView).setVisibility(View.GONE);
        rlTourialView.setVisibility(View.VISIBLE);
        View tourialView1 = rlTourialView.findViewById(R.id.tourialView1);
        tourialView1.setVisibility(View.VISIBLE);
        View view1 = getView().findViewById(R.id.takePhotoOrRecord);
        int[] location1 = getInScreen(view1);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView1.getLayoutParams();
        lp.leftMargin = (((BaseActivity) getActivity()).getDisplaymetrics().widthPixels - Utils.dip2px(getActivity(), 235)) / 2;
        lp.topMargin = location1[1] - Utils.dip2px(getActivity(), 90);
//        Utils.setSubText((TextView) tourialView1.findViewById(R.id.tvLine1),
//                ServerDataManager.getTextFromKey("cmr_txt_selectemotion"), ServerDataManager.getTextFromKey("cmr_txt_scroll"),
//                Color.WHITE, getResources().getColor(R.color.color_255_143_51));
        tourialView1.findViewById(R.id.tvLine1).setVisibility(View.GONE);
        Utils.setSubText((TextView) tourialView1.findViewById(R.id.tvLine2),
                ServerDataManager.getTextFromKey("cmr_txt_taptotakephoto"), ServerDataManager.getTextFromKey("cmr_txt_tap"),
                Color.WHITE, getResources().getColor(R.color.color_65_116_232));
        Utils.setSubText((TextView) tourialView1.findViewById(R.id.tvLine3),
                ServerDataManager.getTextFromKey("cmr_txt_recordvideo"), ServerDataManager.getTextFromKey("cmr_txt_longpress"),
                Color.WHITE, getResources().getColor(R.color.color_34_166_166));


        rlTourialView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View tourialView1 = v.findViewById(R.id.tourialView1);
                View tourialView2 = v.findViewById(R.id.tourialView2);
                View tourialView3 = v.findViewById(R.id.tourialView3);
                if (tourialView1.getVisibility() == View.VISIBLE) {
                    tourialView1.setVisibility(View.GONE);
                    tourialView2.setVisibility(View.VISIBLE);
                    View view2 = getView().findViewById(R.id.btnCameraUpload);
                    int[] location1 = getInScreen(view2);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView2.getLayoutParams();
                    lp.topMargin = location1[1] - view2.getHeight() - Utils.dip2px(getActivity(), 3);
                } else if (tourialView2.getVisibility() == View.VISIBLE) {
                    tourialView2.setVisibility(View.GONE);
                    v.setVisibility(View.GONE);
//                    tourialView3.setVisibility(View.VISIBLE);
//                    View view3 = getView().findViewById(R.id.btnMsg);
//                    int[] location3 = getInScreen(view3);
//                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView3.getLayoutParams();
//                    lp.leftMargin = location3[0] - Utils.dip2px(getActivity(), 195);
//                    lp.topMargin = location3[1] - (Utils.dip2px(getActivity(), 37) - view3.getHeight()) / 2;
                } else if (tourialView3.getVisibility() == View.VISIBLE) {
                    tourialView3.setVisibility(View.GONE);
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.GONE);
                }
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
//        if (mWakeLock == null) {
//            PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
//            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL);
//            mWakeLock.acquire();
//        }
        if (mParentDirectionalViewPager != null) {
            mParentDirectionalViewPager.setCanScroll(true);
        }

        mCameraManager = CameraManager.getInstance(getActivity());
        if (mCameraUseType == TYPE_CAMERA_FOR_AVATER) {
            mCameraManager.setCameraDirection(CameraManager.CameraDirection.CAMERA_FRONT);
        } else {
            mCameraManager.setCameraDirection(CameraManager.CameraDirection.CAMERA_BACK);
        }

        initCameraLayout();

        showOrHideAllBtn(true);
        mTakePhotoOrRecord.resetCircleButton();
        mIsToFilterFragment = false;
        mUsingCamera = false;
        if (mCameraUseType != TYPE_CAMERA_FOR_POST) {
            //           mSelectItemHorizontalScrollView.setVisibility(View.GONE);
            getView().findViewById(R.id.bottomBar).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.recorder_flashlight_parent1).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.recorder_flashlight_parent1_1).setVisibility(View.VISIBLE);
            //           getView().findViewById(R.id.ivFrontEmo).setVisibility(View.GONE);
            getView().findViewById(R.id.btnToAlbum).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.btnCameraUpload).setVisibility(View.GONE);
            if (mCameraUseType == TYPE_CAMERA_FOR_AVATER) {
                getView().findViewById(R.id.cutAvaterView).setVisibility(View.VISIBLE);
            }
        } else {
            //           mSelectItemHorizontalScrollView.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.bottomBar).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.recorder_flashlight_parent1).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.recorder_flashlight_parent1_1).setVisibility(View.INVISIBLE);
            //           getView().findViewById(R.id.ivFrontEmo).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.btnToAlbum).setVisibility(View.GONE);
            getView().findViewById(R.id.btnCameraUpload).setVisibility(View.VISIBLE);
            resetCameraUI();
        }
        if (!Preferences.getInstacne().getBoolByKey(Constants.FLIP_TUTORIAL_IN_CAMERA_FRAGMENT)) {
            showFlipTourial();
            Preferences.getInstacne().setValues(Constants.FLIP_TUTORIAL_IN_CAMERA_FRAGMENT, true);
        } else if (!Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_IN_CAMERA_FRAGMENT)) {
            final View rlTourialView = getActivity().findViewById(R.id.rlTourial);
            rlTourialView.setVisibility(View.VISIBLE);
            rlTourialView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showTourialView();
                }
            }, 500);
        }
        if (mCameraUseType == TYPE_CAMERA_FOR_AVATER) {
            getView().findViewById(R.id.cutAvaterView).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsStartTimer = false;
        if (mCameraContainer != null) {
            if (mCameraContainer.getParent() != null) {
                ((ViewGroup) mCameraContainer.getParent()).removeAllViews();
            }
            mCameraContainer.onStop();
            mCameraContainer.releaseResources();
        } else {
            if (mCameraManager != null) {
                mCameraManager.releaseActivityCamera();
            }
        }

        if (mTakePhotoOrRecord != null) {
            mTakePhotoOrRecord.destroyView();
        }
//        if (mWakeLock != null) {
//            mWakeLock.release();
//            mWakeLock = null;
//        }

        if (mCameraUseType == TYPE_CAMERA_FOR_AVATER) {
            getView().findViewById(R.id.cutAvaterView).setVisibility(View.GONE);
        }
        mUsingCamera = false;
        mCameraContainer = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        ImageView showPic = (ImageView) getView().findViewById(R.id.showPic);
        showPic.setImageBitmap(null);
        if (mTakeBmp != null && !mTakeBmp.isRecycled()) {
            mTakeBmp.recycle();
        }
        mTakeBmp = null;
    }


    private void initLayout() {
        if (getView() == null) {
            return;
        }
        getView().findViewById(R.id.btnFlashlight).setOnClickListener(this);
        getView().findViewById(R.id.btnSwitchCamera).setOnClickListener(this);
        getView().findViewById(R.id.btnFlashlight_1).setOnClickListener(this);
        getView().findViewById(R.id.btnSwitchCamera_1).setOnClickListener(this);
        getView().findViewById(R.id.btnBack).setOnClickListener(this);
        getView().findViewById(R.id.btnToAlbum).setOnClickListener(this);
        mTakePhotoOrRecord = (CircleButton) getView().findViewById(R.id.takePhotoOrRecord);
        getView().findViewById(R.id.btnProfile).setOnClickListener(this);
        getView().findViewById(R.id.btnCameraUpload).setOnClickListener(this);
        getView().findViewById(R.id.btnMsg).setOnClickListener(this);
        getView().findViewById(R.id.btnHome).setOnClickListener(this);
//        mSelectItemHorizontalScrollView = (SelectItemHorizontalScrollView) getView().findViewById(R.id.selectEmoView);
//        mSelectItemHorizontalScrollView.addEmosInCameraFragment((ImageView) getView().findViewById(R.id.ivFrontEmo));
    }


    private void initCameraLayout() {
        getView().findViewById(R.id.showPic).setVisibility(View.GONE);
        RelativeLayout topLayout;
        if (mParentFragment == null) {
            topLayout = (RelativeLayout) getView().findViewById(R.id.recorder_surface_parent);
            topLayout.setVisibility(View.VISIBLE);
        } else {
            topLayout = (RelativeLayout) mParentFragment.getView().findViewById(R.id.recorder_surface_parent);
        }
        getView().findViewById(R.id.focusView).setOnTouchListener(this);

        if (topLayout.getChildCount() > 0)
            topLayout.removeAllViews();

        if (mCameraContainer == null) {
            if (topLayout.getChildCount() > 0)
                topLayout.removeAllViews();
            mCameraContainer = new SquareCameraContainer(getActivity());
        }
        mCameraContainer.onStart();
        mCameraContainer.bindActivity(getActivity());
        if (mCameraContainer.getParent() == null) {
            RelativeLayout.LayoutParams layoutParam1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParam1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            topLayout.addView(mCameraContainer, layoutParam1);
        }

        mTakePhotoOrRecord.setOnTouchListener(this);

        showSwitchCameraIcon();
    }

    private void showOrHideAllBtn(final boolean isShow) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isShow) {
                    getView().findViewById(R.id.recorder_flashlight_parent1).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.bottomBar).setVisibility(View.VISIBLE);
                } else {
                    getView().findViewById(R.id.recorder_flashlight_parent1).setVisibility(View.GONE);
                    getView().findViewById(R.id.bottomBar).setVisibility(View.GONE);
                }
            }
        });

    }

    private long mTapTime, mStartRecordingTime, mTouchDownTime;
    private boolean mIsStartTimer, mIsFingerUp;
    private Object mLockObject = new Object();
    private int mTouchType; //0表示什么都么做，1表示拍照或录制视频，2表示横向移动ScrollView表情

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mUsingCamera || mCameraContainer == null) {
            return true;
        }
        if (v.getId() == R.id.focusView) {
            mCameraContainer.onTouchEvent(event);
            return true;
        }
        if (v.getId() != R.id.takePhotoOrRecord) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mParentDirectionalViewPager != null) {
                    mParentDirectionalViewPager.setCanScroll(false);
                }
//                mSelectItemHorizontalScrollView.setItemMoved(false);
//                mSelectItemHorizontalScrollView.onTouchEvent(event);
                mTouchDownTime = System.currentTimeMillis();
                mTouchType = 0;
                break;
            case MotionEvent.ACTION_MOVE:
//                if (mTouchType != 1) {
//                    mSelectItemHorizontalScrollView.onTouchEvent(event);
//                }
                if (mTouchType == 0 && System.currentTimeMillis() - mTouchDownTime > 200) {
//                    if (mSelectItemHorizontalScrollView.getItemMoved()) {
//                        mTouchType = 2;
//                    } else {
                    mTouchType = 1;
                    if (!initTakeOrRecord(true)) {
                        mTouchType = 3;
                    }
                    //                   }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mParentDirectionalViewPager != null) {
                    mParentDirectionalViewPager.setCanScroll(true);
                }
                if (mTouchType == 0) {
                    //                 mSelectItemHorizontalScrollView.onTouchEvent(event);
                    mTouchType = 1;
                    if (!initTakeOrRecord(false)) {
                        mTouchType = 3;
                    }
                }
                if (mTouchType == 1) {
                    mIsFingerUp = true;
                    synchronized (mLockObject) {
                        mIsStartTimer = false;
                        if (mStartRecordingTime > 0) {
                            if (!mCameraContainer.isRecording() && mCameraUseType == TYPE_CAMERA_FOR_POST) {
                                return true;   //在此之前就已经结束
                            }
                            stopRecording();
                        } else {
                            int x = (int) event.getRawX();
                            int y = (int) event.getRawY();
                            int[] location = new int[2];
                            v.getLocationOnScreen(location);
                            if ((x > location[0] && x < location[0] + v.getWidth()) && (y > location[1] && y < location[1] + v.getHeight())) {
                                mTakePhotoOrRecord.setVisibility(View.INVISIBLE);
                                mUsingCamera = true;
                                if (mParentDirectionalViewPager != null) {
                                    mParentDirectionalViewPager.setCanScroll(false);
                                }

                                boolean isSuccessful = mCameraContainer.takePicture(new OnCameraUseListener() {
                                    @Override
                                    public void takePicture(final Bitmap bmp) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTakeBmp = bmp;
                                                if (mCameraUseType == TYPE_CAMERA_FOR_POST) {
                                                    ((HomeActivity) getActivity()).setViewPagerCanScroll(true);
                                                }
                                                if (bmp != null) {
                                                    mCameraContainer.stopPreview();
                                                    if (GBSPreferences.getInstacne().getSaveOriginalStateNum() == 1 && mCameraUseType == CameraFragment.TYPE_CAMERA_FOR_POST) {
                                                        Utils.savePhotoToAlbum(bmp, getContext());
                                                    }
                                                    ImageView showPic = (ImageView) getView().findViewById(R.id.showPic);
                                                    showPic.setVisibility(View.VISIBLE);
                                                    showPic.setImageBitmap(bmp);
                                                    if (mCameraUseType != TYPE_CAMERA_FOR_POST) {
                                                        if (mCameraUseType == TYPE_CAMERA_FOR_AVATER) {
                                                            int x = 0;
                                                            int y = bmp.getHeight() * 5 / 12 - bmp.getWidth() / 2;
                                                            Bitmap newBmp = Bitmap.createBitmap(bmp, x, y, bmp.getWidth(), bmp.getWidth());
                                                            BitmapCacheManager.getInstance().remove(PhotoPreviewFragment.PREVIEW_PICTURE);
                                                            BitmapCacheManager.getInstance().put(PhotoPreviewFragment.PREVIEW_PICTURE, newBmp);
                                                            getView().findViewById(R.id.cutAvaterView).setVisibility(View.GONE);
                                                            goBack();
                                                            return;
                                                        }
                                                        BitmapCacheManager.getInstance().put(PhotoPreviewFragment.PREVIEW_PICTURE, bmp);
                                                        Bundle bundle = new Bundle();
                                                        bundle.putInt(CameraForChatOrAvaterFragment.USE_CAMERA_TYPE, mCameraUseType);
                                                        gotoPager(PhotoPreviewFragment.class, bundle);
                                                    } else {
                                                        String srcPath = Utils.saveJpeg(bmp, getContext());
                                                        returnToCaller(Constants.PIC_SHADER_FILTER, srcPath);
                                                    }
                                                } else {
                                                    SensorControler.getInstance().unlockFocus();
                                                    mUsingCamera = false;
                                                    mCameraContainer.startPreview();
                                                    mTakePhotoOrRecord.resetCircleButton();
                                                    showOrHideAllBtn(true);
//                                                    if (mCameraUseType == TYPE_CAMERA_FOR_POST) {
//                                                        getView().findViewById(R.id.ivFrontEmo).setVisibility(View.VISIBLE);
//                                                        //                                                      mSelectItemHorizontalScrollView.setVisibility(View.VISIBLE);
//                                                    }
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void recordingEnd(String videoPath) {

                                    }
                                });
                                if (!isSuccessful) {
                                    mUsingCamera = false;
                                    mCameraContainer.startPreview();
                                    mTakePhotoOrRecord.resetCircleButton();
                                    showOrHideAllBtn(true);
//                                    if (mCameraUseType == TYPE_CAMERA_FOR_POST) {
//                                        getView().findViewById(R.id.ivFrontEmo).setVisibility(View.VISIBLE);
////                                        mSelectItemHorizontalScrollView.setVisibility(View.VISIBLE);
//                                    }
                                }
                            } else {
                                mUsingCamera = false;
                                mCameraContainer.startPreview();
                                mTakePhotoOrRecord.resetCircleButton();
                                showOrHideAllBtn(true);
//                                if (mCameraUseType == TYPE_CAMERA_FOR_POST) {
//                                    getView().findViewById(R.id.ivFrontEmo).setVisibility(View.VISIBLE);
////                                    mSelectItemHorizontalScrollView.setVisibility(View.VISIBLE);
//                                }
                            }
                        }
                    }
                } else if (mTouchType == 2) {
//                    if (mCameraUseType == TYPE_CAMERA_FOR_POST) {
// //                       mSelectItemHorizontalScrollView.onTouchEvent(event);
//                    }
                } else if (mTouchType == 3) {
                    mUsingCamera = false;
                    mCameraContainer.startPreview();
                    mTakePhotoOrRecord.resetCircleButton();
                    showOrHideAllBtn(true);
//                    if (mCameraUseType == TYPE_CAMERA_FOR_POST) {
//                        getView().findViewById(R.id.ivFrontEmo).setVisibility(View.VISIBLE);
//                        mSelectItemHorizontalScrollView.setVisibility(View.VISIBLE);
//                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean initTakeOrRecord(boolean isRecord) {
        if (isRecord) {
            if (!BaseUtils.isGrantPermission(getActivity(), Manifest.permission.CAMERA)
                    || !BaseUtils.isGrantPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                    || !BaseUtils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ((BaseActivity) (getActivity())).requestPermission(0, Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return false;
            }
        } else {
            if (!BaseUtils.isGrantPermission(getActivity(), Manifest.permission.CAMERA)
                    || !BaseUtils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ((BaseActivity) (getActivity())).requestPermission(0, Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return false;
            }
        }
        BitmapCacheManager.getInstance().evictAll();  //释放Cache的所有图片，防止之后溢出
        mTapTime = System.currentTimeMillis();
        mStartRecordingTime = 0;
        mIsFingerUp = false;
        if (mCameraUseType == TYPE_CAMERA_FOR_POST) {
//            mTakePhotoOrRecord.setEmo(mSelectItemHorizontalScrollView.getLastSelectItem() % Constants.EMO_ID_COLOR.length);
//            getView().findViewById(R.id.ivFrontEmo).setVisibility(View.INVISIBLE);
//            mSelectItemHorizontalScrollView.setVisibility(View.INVISIBLE);

            mTakePhotoOrRecord.setEmo(0);
            if (isRecord) {
                startTimer();
            }
            ((HomeActivity) getActivity()).setViewPagerCanScroll(false);
        } else {
            mTakePhotoOrRecord.setEmo(-1);
        }
        return true;
    }

    private void startTimer() {
        if (mIsStartTimer) {
            return;
        }
        mIsStartTimer = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsStartTimer) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mStartRecordingTime == 0 && System.currentTimeMillis() - mTapTime > 500 && !mIsFingerUp) {   //当按下时间超过0.5s时,默认为开始录制视频
                        synchronized (mLockObject) {
                            showOrHideAllBtn(false);
                            mCameraContainer.startRecording();
                            mStartRecordingTime = System.currentTimeMillis();
                        }
                    }
                    if (mIsFingerUp) {
                        mIsStartTimer = false;
                        break;
                    }
                    if (mCameraContainer.isRecording()) {
                        long recordTime = System.currentTimeMillis() - mStartRecordingTime;
                        mTakePhotoOrRecord.resetArcAngle(recordTime, CameraGLSurfaceView.MAX_DURATION);
                        if (recordTime >= CameraGLSurfaceView.MAX_DURATION) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopRecording();
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    private void stopRecording() {
        mIsStartTimer = false;
        mUsingCamera = true;
        mParentDirectionalViewPager.setCanScroll(false);
        mTakePhotoOrRecord.setVisibility(View.INVISIBLE);
        mCameraContainer.stopRecording(new OnCameraUseListener() {
            @Override
            public void takePicture(Bitmap bmp) {

            }

            @Override
            public void recordingEnd(String videoPath) {
                if (System.currentTimeMillis() - mStartRecordingTime > 1100) {
                    mCameraContainer.stopPreview();
                    ((HomeActivity) getActivity()).setViewPagerCanScroll(true);
                    returnToCaller(Constants.VIDEO_DEGREE_0, videoPath);
                    mTakePhotoOrRecord.setOnTouchListener(null);
                } else {
                    mUsingCamera = false;
                    mCameraContainer.startPreview();
                    mTakePhotoOrRecord.resetCircleButton();
                    showOrHideAllBtn(true);
//                    getView().findViewById(R.id.ivFrontEmo).setVisibility(View.VISIBLE);
//                    mSelectItemHorizontalScrollView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
            case R.id.btnFlashlight:
            case R.id.btnFlashlight_1:
                if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    return;
                }
                mCameraManager.setLightStatus(mCameraManager.getLightStatus().next());
                showFlashIcon();
                break;
            case R.id.btnSwitchCamera:
            case R.id.btnSwitchCamera_1:
                mCameraManager.setCameraDirection(mCameraManager.getCameraDirection().next());
                v.setClickable(false);
                mCameraContainer.switchCamera();

                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setClickable(true);
                    }
                }, 500);
                showSwitchCameraIcon();
                break;
            case R.id.btnProfile:
                mParentDirectionalViewPager.setCurrentItem(0);
                break;
            case R.id.btnMsg:
                ((HomeActivity) getActivity()).toChatListFragment();
                break;
            case R.id.btnHome:
                ((HomeActivity) getActivity()).toHomeFragment();
                break;
            case R.id.btnCameraUpload:
            case R.id.btnToAlbum:
                Bundle b = new Bundle();
                b.putInt(CameraForChatOrAvaterFragment.USE_CAMERA_TYPE, mCameraUseType);
                gotoPager(AlbumFragment.class, b);
                break;
            default:
                break;
        }
    }

    private void showSwitchCameraIcon() {
        if (mCameraManager.getCameraDirection() == CameraManager.CameraDirection.CAMERA_FRONT) {
            if (mCameraUseType != TYPE_CAMERA_FOR_POST) {
                getView().findViewById(R.id.btnFlashlight_1).setVisibility(View.INVISIBLE);
            } else {
                getView().findViewById(R.id.btnFlashlight).setVisibility(View.INVISIBLE);
            }

        } else {
            if (mCameraUseType != TYPE_CAMERA_FOR_POST) {
                getView().findViewById(R.id.btnFlashlight_1).setVisibility(View.VISIBLE);
            } else {
                getView().findViewById(R.id.btnFlashlight).setVisibility(View.VISIBLE);
            }
            showFlashIcon();
        }
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            if (mCameraUseType != TYPE_CAMERA_FOR_POST) {
                getView().findViewById(R.id.btnSwitchCamera_1).setVisibility(View.VISIBLE);
            } else {
                getView().findViewById(R.id.btnSwitchCamera).setVisibility(View.VISIBLE);
            }
        }
    }


    private void showFlashIcon() {
        if (mCameraManager.getLightStatus() == CameraManager.FlashLigthStatus.LIGHT_ON) {
            if (mCameraUseType != TYPE_CAMERA_FOR_POST) {
                ((ImageButton) getView().findViewById(R.id.btnFlashlight_1)).setImageResource(R.drawable.camera_flashlight_on);
            } else {
                ((ImageButton) getView().findViewById(R.id.btnFlashlight)).setImageResource(R.drawable.camera_flashlight_on);
            }
        } else {
            if (mCameraUseType != TYPE_CAMERA_FOR_POST) {
                ((ImageButton) getView().findViewById(R.id.btnFlashlight_1)).setImageResource(R.drawable.camera_flashlight_off);
            } else {
                ((ImageButton) getView().findViewById(R.id.btnFlashlight)).setImageResource(R.drawable.camera_flashlight_off);
            }
        }
    }

    private synchronized void returnToCaller(int shaderFilter, String srcPath) {
        if (mIsToFilterFragment) {
            return;
        }
        ((HomeActivity) getActivity()).setViewPagerCanScroll(false);
        mIsToFilterFragment = true;
        Bundle bundle = new Bundle();
        bundle.putString(EditPostFragment.SOURCE_PATH, srcPath);
        bundle.putInt(EditPostFragment.FILTER_TYPE, shaderFilter);
        bundle.putInt(EditPostFragment.SELECT_EMO_ID, 0);//mSelectItemHorizontalScrollView.getLastSelectItem() % Constants.EMO_ID_COLOR.length);
        bundle.putBoolean(EditPostFragment.IS_FROM_ALBUM, false);
        gotoPager(EditPostFragment.class, bundle);
    }
}
