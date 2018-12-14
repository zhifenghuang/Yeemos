package com.yeemos.app.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.gifencoder.AnimatedGifEncoder;
import com.common.mentiontextview.MentionTextView;
import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.FriendGroup;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.TagBean;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.BitmapUtil;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.ChooseFriendView;
import com.yeemos.app.view.DrawCommentView;
import com.yeemos.app.view.GLShaderJNIView;
import com.yeemos.app.view.HandleRelativeLayout;
import com.yeemos.app.view.PostPopupView;
import com.yeemos.app.view.ScaleImageButton;
import com.yeemos.yeemos.jni.ExtractDecodeEditEncodeMuxTest;
import com.yeemos.yeemos.jni.ExtractMpegFramesTest;
import com.yeemos.yeemos.jni.ShaderJNILib;
import com.yeemos.yeemos.jni.TextureRender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by gigabud on 15-12-11.
 */
public class EditPostFragment extends BaseFragment implements View.OnClickListener {

    public static final String LAST_ANONYMOUS_TIME = "lastAnonymousTime";//上一次匿名时间
    public static final long ANONYMOUS_TIME = 24 * 3600 * 1000;  //发送匿名间隔时间为24小时

    public static final String SELECT_EMO_ID = "select_emo_id";
    public static final String SOURCE_PATH = "source_path";
    public static final String FILTER_TYPE = "filter_type";
    public static final String IS_FROM_ALBUM = "isFromAlbum";

    private GLShaderJNIView mSurfaceView;
    protected int mShaderFilter;
    private String mSrcPath;
    private boolean mIsFromAlbum;
    protected int mSelectEmoID;
    private boolean mIsAnonymousOn = true;
    private boolean mIsPostHrsOn = true;
    private int mPrivateState;
    private boolean isDeleteSrc = true;
    private Dialog dialog;
    private ArrayList<FriendGroup> groupList;
    private ArrayList<Integer> userIdList;
    private int mKeyBoardHeight;
//    private boolean mIsCanSendAnonymous;//是否可以匿名

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_edit_post;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION_TWO;
    }

    @Override
    protected void initFilterForBroadcast() {

    }


    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        init(view);

        getActivity().getContentResolver()
                .registerContentObserver(
                        Settings.Secure
                                .getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED),
                        false, mGpsMonitor);

        if (Preferences.getInstacne().isFirstAnonymous()) {
            String content = ServerDataManager.getTextFromKey("pblc_txt_firsttimetouseanonymous");
            String btn = ServerDataManager.getTextFromKey("pblc_btn_igotit");
            showPublicDialog(null, content, btn, null, firstAnonymousDialog);
        }
    }


    protected void init(final View view) {
        getEtPost().setIsPupupDown(true);
        mSurfaceView = (GLShaderJNIView) view.findViewById(R.id.glView);
        Bundle bundle = getArguments();
        if (bundle == null) {
            getActivity().finish();
            return;
        }
        mShaderFilter = bundle.getInt(FILTER_TYPE, Constants.PIC_SHADER_FILTER);
        mSrcPath = bundle.getString(SOURCE_PATH);
        mSelectEmoID = bundle.getInt(SELECT_EMO_ID);
        mIsFromAlbum = bundle.getBoolean(IS_FROM_ALBUM, false);
        if (mShaderFilter != Constants.ONLY_TEXT) {
            ShaderJNILib.setShaderType(mShaderFilter);
            ShaderJNILib.setPlatform(Constants.PLATFORM_ANDROID);
            mSurfaceView.setShaderFilterType(mShaderFilter, mSrcPath);
        }

        view.findViewById(R.id.btnShare).setOnClickListener(this);
        view.findViewById(R.id.btnPrivacy).setOnClickListener(this);
        view.findViewById(R.id.btnAnonymous).setOnClickListener(this);
        view.findViewById(R.id.btnSetTime).setOnClickListener(this);
        view.findViewById(R.id.btnSave).setOnClickListener(this);
        view.findViewById(R.id.rlPostText).setOnClickListener(this);
        view.findViewById(R.id.btnEmo).setOnClickListener(this);
        ((ImageButton) view.findViewById(R.id.btnSetTime)).
                setImageResource(mIsPostHrsOn ? R.drawable.edit_24hrs : R.drawable.edit_unlimited);

        getDrawCommentView().setCurrentUseIn(DrawCommentView.USE_IN_EDIT_POST, this, null);

        getHandleRelativeLayout().initViewPager(getContext());
        getHandleRelativeLayout().setOnHandleRelativeLayoutEvent(new HandleRelativeLayout.OnHandleRelativeLayoutEvent() {
            @Override
            public void onScroll(int pageNumber, float xOffset) {
                ShaderJNILib.resetXOffset(xOffset, pageNumber);
                mSurfaceView.requestRender();
            }

            @Override
            public void onClick() {
                if (!getDrawCommentView().isPostEditViewVisibility()) {
                    getDrawCommentView().findViewById(R.id.btnWartMark).performClick();
                }
            }

            @Override
            public void isInGPSFilterPage(boolean isIn) {
                if (isIn) {
                    getHandleRelativeLayout().bringToFront();
                } else {
                    getPostText().bringToFront();
                    getTopView().bringToFront();
                    getDrawCommentView().bringToFront();
                    getBottomView().bringToFront();
                }
            }
        });

        final RelativeLayout rootView = (RelativeLayout) view.findViewById(R.id.rootView);
        rootView.getViewTreeObserver().
                addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        if (getView() == null) {
                            return;
                        }
                        int screenHeight = rootView.getHeight();
                        if (mKeyBoardHeight > screenHeight / 3) {
                            return;
                        }
                        Rect r = new Rect();
                        rootView.getWindowVisibleDisplayFrame(r);
                        int heightDifference = screenHeight - (r.bottom - r.top);
                        if (heightDifference > screenHeight / 3) {
                            mKeyBoardHeight = heightDifference;
                        }
                    }
                });
    }

    protected void resetViewBySelectEmo() {
        ((ScaleImageButton) getView().findViewById(R.id.btnEmo)).setImageResource(Constants.EMO_ID_COLOR[mSelectEmoID][0]);
        View btnShare = getView().findViewById(R.id.btnShare);
        GradientDrawable gd = (GradientDrawable) btnShare.getBackground();
        if (gd != null) {
            gd.setColor(Constants.EMO_ID_COLOR[mSelectEmoID][1]);
        }
    }

    public void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(mSrcPath)) {
            if (!new File(mSrcPath).exists()) {
                getActivity().finish();
                return;
            }
        }

        ((BaseActivity) getActivity()).setScreenFull(true);
        resetViewBySelectEmo();
        ((ImageButton) getView().findViewById(R.id.btnAnonymous)).
                setImageResource(mIsAnonymousOn ? R.drawable.edit_anonymous : R.drawable.edit_not_anonymous);
        ((ImageButton) getView().findViewById(R.id.btnSetTime)).
                setImageResource(mIsPostHrsOn ? R.drawable.edit_24hrs : R.drawable.edit_unlimited);
    }

    private final ContentObserver mGpsMonitor = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getHandleRelativeLayout().removeEnableGPSPage();
                }
            });
        }
    };

    private void showTourialView() {

        final View rlTourialView = getActivity().findViewById(R.id.rlTourial);
        rlTourialView.postDelayed(new Runnable() {
            @Override
            public void run() {
                rlTourialView.setVisibility(View.VISIBLE);
                View tourialView0 = rlTourialView.findViewById(R.id.tourialView0);
                tourialView0.setVisibility(View.VISIBLE);
                View view1 = getView().findViewById(R.id.btnText);
                int[] location1 = getInScreen(view1);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView0.getLayoutParams();
                lp.leftMargin = location1[0] - (Utils.dip2px(getActivity(), 250) - view1.getWidth()) / 2;
                lp.topMargin = location1[1] + view1.getHeight();
                rlTourialView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getView() == null || getActivity() == null) {
                            rlTourialView.setVisibility(View.GONE);
//                            getActivity().finish();
                            return;
                        }
                        View tourialView0 = v.findViewById(R.id.tourialView0);
                        View tourialView1 = v.findViewById(R.id.tourialView1);
                        View tourialView2 = v.findViewById(R.id.tourialView2);
                        View tourialView3 = v.findViewById(R.id.tourialView2_1);
                        if (tourialView0.getVisibility() == View.VISIBLE) {
                            tourialView0.setVisibility(View.GONE);
                            tourialView1.setVisibility(View.VISIBLE);
                            View view2 = getView().findViewById(R.id.btnAnonymous);
                            int[] location1 = getInScreen(view2);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView1.getLayoutParams();
                            lp.leftMargin = location1[0] - (Utils.dip2px(getActivity(), 250) - view2.getWidth()) / 2;
                            lp.topMargin = location1[1] - Utils.dip2px(getActivity(), 52);
                        } else if (tourialView1.getVisibility() == View.VISIBLE) {
                            tourialView1.setVisibility(View.GONE);
                            tourialView2.setVisibility(View.VISIBLE);
                            View view2 = getView().findViewById(R.id.btnSetTime);
                            int[] location2 = getInScreen(view2);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView2.getLayoutParams();
                            lp.leftMargin = location2[0] - (Utils.dip2px(getActivity(), 250) - view2.getWidth()) / 2;
                            lp.topMargin = location2[1] - Utils.dip2px(getActivity(), 52);
                        } else if (tourialView2.getVisibility() == View.VISIBLE) {
                            tourialView2.setVisibility(View.GONE);
                            tourialView3.setVisibility(View.VISIBLE);
                            ((TextView) tourialView3.findViewById(R.id.tv2_1)).setText(ServerDataManager.getTextFromKey("edtpst_txt_presstoshare"));
                            View view3 = getView().findViewById(R.id.btnShare);
                            int[] location2 = getInScreen(view3);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView3.getLayoutParams();
                            lp.rightMargin = Utils.dip2px(getActivity(), 5);
                            lp.topMargin = location2[1] - Utils.dip2px(getActivity(), 60);
                            lp = (RelativeLayout.LayoutParams) tourialView3.findViewById(R.id.triangleView2_1).getLayoutParams();
                            lp.rightMargin = (view3.getWidth() - Utils.dip2px(getContext(), 15)) / 2;
                        } else {
                            tourialView3.setVisibility(View.GONE);
                            v.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }, 500);

    }

    public void onResume() {
        super.onResume();
        ShaderJNILib.resetXOffset(0f, getHandleRelativeLayout().getViewPager().getCurrentItem());
        if (mSurfaceView != null) {
            mSurfaceView.setVisibility(View.VISIBLE);
            mSurfaceView.onMediaResume();
        }
        getDrawCommentView().setVisibility(View.VISIBLE);
        if (!Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_IN_EDIT_POST_FRAGMENT)) {
            Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_EDIT_POST_FRAGMENT, true);
            showTourialView();
        }
    }

    public void onPause() {
        super.onPause();
        if (mSurfaceView != null) {
            mSurfaceView.onMediaPause();
            mSurfaceView.setVisibility(View.GONE);
        }
        getDrawCommentView().setVisibility(View.GONE);
        ShaderJNILib.destroySource();
    }

    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getContentResolver().unregisterContentObserver(mGpsMonitor);
        getHandleRelativeLayout().destroyView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (isDeleteSrc && !TextUtils.isEmpty(mSrcPath)) {
            new File(mSrcPath).delete();
        }
        if (mSurfaceView != null) {
            mSurfaceView.releaseMediaPlayer();
        }
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }


    @Override
    public void onClick(View v) {
        if (getHandleRelativeLayout().isInEnableGPSPage()) {
            return;
        }
        switch (v.getId()) {
            case R.id.btnEmo:
                getDrawCommentView().postEditView();
                showPostPopupWindow(PostPopupView.POST_PUPUP_EMOS);
                break;
            case R.id.rlPostText:
                getDrawCommentView().findViewById(R.id.btnText).performClick();
                break;
            case R.id.btnSave:
                if (mShaderFilter == Constants.ONLY_TEXT) {
                    return;
                }
                if (mShaderFilter == Constants.PIC_SHADER_FILTER) {
                    BitmapCacheManager.getInstance().evictAll();  //释放Cache的所有图片，防止之后溢出
                    showLoadingDialog(ServerDataManager.getTextFromKey("pblc_txt_saving"), null, true);
                    mSurfaceView.startGetGLBmp(new GLShaderJNIView.OnSaveBmp() {
                        @Override
                        public void getGLViewBmp(final Bitmap bmp) {
                            getDrawCommentView().savePhotoToAlbum(bmp, mSelectEmoID, getHandleRelativeLayout());
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideLoadingDialog();
                                    Toast.makeText(getActivity(), ServerDataManager.getTextFromKey("cht_txt_saved"), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    showLoadingDialog(ServerDataManager.getTextFromKey("pblc_txt_saving"), null, true);
                    GBExecutionPool.getExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            ExtractDecodeEditEncodeMuxTest test = new ExtractDecodeEditEncodeMuxTest();
                            test.setContext(getActivity());
                            boolean isSaved = false;
                            String savePath = Utils.createAlbumVideoPath();
                            try {
                                test.init(mSrcPath, savePath, mShaderFilter, TextureRender.USE_FOR_UPLOAD_SAVE_POST, null);
                                DataManager.getInstance().setSelectObject(getDrawCommentView().getBmpInVideo(true, mSelectEmoID, getHandleRelativeLayout()));
                                test.testExtractDecodeEditEncodeMuxAudioVideo();
                                Utils.registerVideo(getActivity(), savePath);
                                isSaved = true;
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            if (!isSaved) {
                                Utils.copyFile(getActivity(), mSrcPath, savePath);
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideLoadingDialog();
                                    Toast.makeText(getActivity(), ServerDataManager.getTextFromKey("cht_txt_saved"), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                break;
            case R.id.btnShare:
                String postText = getEtPost().getText().toString();
                if (mShaderFilter == Constants.ONLY_TEXT && postText.trim().length() == 0) {
                    return;
                }
                showLoadingDialog("", null, true);
                if (mShaderFilter == Constants.PIC_SHADER_FILTER) {
                    BitmapCacheManager.getInstance().evictAll();  //释放Cache的所有图片，防止之后溢出
                    mSurfaceView.startGetGLBmp(new GLShaderJNIView.OnSaveBmp() {
                        @Override
                        public void getGLViewBmp(final Bitmap bmp) {
                            final Bitmap postBmp = getDrawCommentView().getPostBmp(bmp, getHandleRelativeLayout());
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    upLoadPost(postBmp.getWidth(), postBmp.getHeight(), Utils.saveJpeg(postBmp, getActivity()), Utils.saveThumb(postBmp));
                                }
                            });
                        }
                    });
                } else {
                    DataManager.getInstance().setSelectObject(getDrawCommentView().getBmpInVideo(false, mSelectEmoID, getHandleRelativeLayout()));
                    upLoadPost(0, 0, mSrcPath, null);
                }
                break;
            case R.id.btnPrivacy:
                showPostPopupWindow(PostPopupView.POST_PUPUP_PRIVACY);
                break;
            case R.id.btnAnonymous:

//                if (!mIsAnonymousOn && !mIsCanSendAnonymous) {
//                    String content = ServerDataManager.getTextFromKey(DataManager.getInstance().getBasicCurUser().isAuthenticate() ? "edtpst_txt_tenanonymous" : "edtpst_txt_oneanonymous");
//                    String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
//                    showPublicDialog(null, content, Okey, null, oneBtnDialoghandler);
//                    return;
//                }
                mIsAnonymousOn = !mIsAnonymousOn;
                ((ImageButton) v).
                        setImageResource(mIsAnonymousOn ? R.drawable.edit_anonymous : R.drawable.edit_not_anonymous);
                break;
            case R.id.btnSetTime:
                mIsPostHrsOn = !mIsPostHrsOn;
                ((ImageButton) v).
                        setImageResource(mIsPostHrsOn ? R.drawable.edit_24hrs : R.drawable.edit_unlimited);
                break;
        }
    }

    public void showPostText(final String postText) {
        final TextView tv1 = (TextView) getView().findViewById(R.id.tvOne);
        final TextView tv2 = (TextView) getView().findViewById(R.id.tvTwo);
        tv1.setText("");
        tv2.setText("");
        tv1.setText(postText == null ? "" : postText);
        tv1.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (TextUtils.isEmpty(postText)) {
                    return;
                }
                if (tv1.getLineCount() > 2) {
                    int start = tv1.getLayout().getLineStart(2);
                    tv1.setMaxLines(2);
                    if (start > postText.length()) {
                        return;
                    }
                    String twoText = postText.substring(start);
                    tv2.setText(twoText);
                    tv2.setSingleLine();
                    tv2.setVisibility(View.VISIBLE);
                } else {
                    tv2.setVisibility(View.GONE);
                }
            }
        });
    }

    public View getTopView() {
        return getView().findViewById(R.id.topView);
    }

    public View getBottomView() {
        return getView().findViewById(R.id.bottomLayout);
    }


    public RelativeLayout getPostText() {
        return (RelativeLayout) getView().findViewById(R.id.rlPostText);
    }

    public HandleRelativeLayout getHandleRelativeLayout() {
        return (HandleRelativeLayout) getView().findViewById(R.id.handleView);
    }

    private DrawCommentView getDrawCommentView() {
        return (DrawCommentView) getView().findViewById(R.id.drawCommentView);
    }

    protected PostBean createPost() {
        PostBean postBean = new PostBean();
        ArrayList<TagBean> list = new ArrayList<>();
        TagBean bean = new TagBean();
        bean.setId(mSelectEmoID);
        list.add(bean);
        postBean.setTags(list);
        postBean.setIsFromAblum(mIsFromAlbum ? 1 : 0);
        postBean.setText(getDrawCommentView().getPostTextText());
        postBean.setOwner(BasicUser.from(MemberShipManager.getInstance().getUserInfo()));
        postBean.setIsAnonymity(mIsAnonymousOn ? 1 : 0);
        postBean.setIsPrivate(exChangeIndexAndPrivateState(mPrivateState));
        postBean.setExpiredType(mIsPostHrsOn ? 1 : 0);
        postBean.setUploadState(1);//0:表示上传成功1:表示正在上传2:表示上传失败
        if (postBean.getIsPrivate() == 3) {//自由选择好友分享
            postBean.setFriendGroups(groupList);
            postBean.setFriendUsers(userIdList);

            Preferences.getInstacne().setLastSharedID(groupList, userIdList);
        }
        return postBean;
    }

    protected MentionTextView getEtPost() {
        return (MentionTextView) getView().findViewById(R.id.etPost);
    }

    protected void upLoadPost(final int srcWidth, final int srcHeight, final String path, final String imageSmall) {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            public void run() {
                final PostBean postBean = createPost();
                if (mShaderFilter == Constants.PIC_SHADER_FILTER) {
                    String[] strs = path.split("/");
                    String fileName = strs[strs.length - 1];
                    postBean.setFirstAttachFilePath(path);
                    postBean.setImage(fileName);
                    strs = imageSmall.split("/");
                    fileName = strs[strs.length - 1];

                    JSONObject object = new JSONObject();
                    try {
                        object.put("width", srcWidth);
                        object.put("height", srcHeight);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    postBean.setAdditionalParameters(object.toString());

                    postBean.setImageSmall(fileName);
                    postBean.setSecondAttachFilePath(imageSmall);
                    postBean.setAttachDataType(Constants.POST_ATTACH_DATA_TYPE.IMAGE_TEXT.GetValue());
                    postBean.setVideoFileSize(new File(path).length());

                } else {
                    ExtractDecodeEditEncodeMuxTest test = new ExtractDecodeEditEncodeMuxTest();
                    test.setContext(getActivity());
                    String savePath = Utils.createVideoPath(getActivity());
                    boolean isSaved = false;
                    try {
                        test.init(mSrcPath, savePath, mShaderFilter, TextureRender.USE_FOR_UPLOAD_SAVE_POST, null);
                        test.testExtractDecodeEditEncodeMuxAudioVideo();
                        isSaved = true;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    if (!isSaved) {
                        savePath = path;
                        isDeleteSrc = false;
                    }

                    String[] strs = savePath.split("/");
                    String fileName = strs[strs.length - 1];
                    postBean.setVideo(fileName);
                    postBean.setAttachDataType(Constants.POST_ATTACH_DATA_TYPE.VIDEO_TEXT.GetValue());
                    postBean.setFirstAttachFilePath(savePath);
                    postBean.setVideoFileSize(new File(savePath).length());
                    android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
                    try {
                        mmr.setDataSource(savePath);
                        long length = Long.parseLong(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION));
                        int time = (int) (length * 1.0 / 1000 + 0.5);
                        postBean.setVideoPlayLength(time);

                        JSONObject object = new JSONObject();
                        object.put("width", Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
                        object.put("height", Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
                        postBean.setAdditionalParameters(object.toString());
                    } catch (Exception ex) {
                        Log.e("EditPostFragment", "MediaMetadataRetriever exception " + ex);
                    } finally {
                        mmr.release();
                        mmr = null;
                    }
                    String thumbPath = convertVideoTopFiveFragmeToGif(savePath);
                    strs = thumbPath.split("/");
                    fileName = strs[strs.length - 1];
                    postBean.setImage(fileName);
                    postBean.setSecondAttachFilePath(thumbPath);

                    String smallImagePath = Utils.saveThumb(BitmapUtil.getVideoThumbnail(savePath));
                    strs = smallImagePath.split("/");
                    fileName = strs[strs.length - 1];
                    postBean.setImageSmall(fileName);
                    postBean.setThreeAttachFilePath(smallImagePath);
                }
                DataManager.getInstance().createPost(postBean);
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsAnonymousOn) {
                            Preferences.getInstacne().setValues(LAST_ANONYMOUS_TIME, System.currentTimeMillis());
                        }
                        DataManager.getInstance().setSelectObject(postBean);
                        hideLoadingDialog();
                        getActivity().finish();
                    }
                });
            }
        });
        // 上传
    }


    private String convertVideoTopFiveFragmeToGif(String videoPath) {
        String saveFile = Utils.createCachePath(getActivity());
        File frameFile = new File(saveFile, "frame");
        String gifPath = null;
        if (!frameFile.exists() || !frameFile.isDirectory()) {
            frameFile.mkdirs();
        }
        try {
            ExtractMpegFramesTest test = new ExtractMpegFramesTest();
            test.setContext(getActivity());
            try {
                test.testExtractMpegFrames(videoPath, frameFile);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            OutputStream os = null;
            gifPath = saveFile + "/" + UUID.randomUUID().toString() + Constants.GIF_EXTENSION;
            os = new FileOutputStream(gifPath);
            Bitmap bitmap;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.setFrameRate(1000f);
            encoder.setRepeat(Integer.MAX_VALUE);
            encoder.start(bos);
            for (int i = 0; i < 9; ++i) {
                if (i <= 4) {
                    bitmap = BitmapFactory.decodeFile(frameFile + "/" + i + Constants.IMAGE_EXTENSION);
                } else {
                    bitmap = BitmapFactory.decodeFile(frameFile + "/" + (8 - i) + Constants.IMAGE_EXTENSION);
                }
                if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                    continue;
                }
                encoder.addFrame(bitmap);
                bitmap.recycle();
            }
            encoder.finish();
            os.write(bos.toByteArray());
            bos.close();
            os.close();
        } catch (IOException e) {
            Log.e(getClass().getName(), "IO", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < 5; ++i) {
                new File(frameFile + "/" + i + Constants.IMAGE_EXTENSION).delete();
            }
        }
        return gifPath;
    }


    protected Handler firstAnonymousDialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    Preferences.getInstacne().setFirstAnonymous(false);
                    break;
                default:
                    break;
            }
        }
    };

    protected Handler oneBtnDialoghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 显示选表情和隐私PopupWindow
     *
     * @param popupWindowType
     */
    private void showPostPopupWindow(int popupWindowType) {
        PostPopupView postPopupView = new PostPopupView(getActivity(), popupWindowType,
                popupWindowType == PostPopupView.POST_PUPUP_EMOS ? 0 : mPrivateState, R.drawable.bubble_cloud);
        int[] size = postPopupView.getViewSize();
        final PopupWindow popupWindow = new PopupWindow(postPopupView, size[0],
                size[1], true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();

        final int[] location = new int[2];
        getBottomView().getLocationOnScreen(location);
        popupWindow.showAtLocation(getBottomView(), Gravity.NO_GRAVITY, location[0],
                popupWindowType == PostPopupView.POST_PUPUP_EMOS ? ((int) (location[1] - 0.92 * size[1])) : ((int) (location[1] - 0.85 * size[1])));
        popupWindow.getContentView().setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                popupWindow.dismiss();
                return false;
            }
        });

        postPopupView.setOnPopupItemClick(new PostPopupView.OnPopupItemClick() {
            @Override
            public void popupItemClick(int popupType, final int clickNo) {
                if (popupType == PostPopupView.POST_PUPUP_EMOS) {
                    mSelectEmoID = clickNo;
                    resetViewBySelectEmo();
                } else {

                    if (clickNo == 2) {
                        ChooseFriendView chooseFriendView = new ChooseFriendView(EditPostFragment.this.getContext(), R.style.ActionSheetDialogStyle);
                        DisplayMetrics displayMetrics = getDisplaymetrics();
                        chooseFriendView.setDialogSize(displayMetrics.widthPixels - 50, displayMetrics.heightPixels - 150);
                        chooseFriendView.setAddGroupAndUserListener(new ChooseFriendView.AddGroupAndUserListener() {
                            @Override
                            public void AddGroupAndUserToPoast(ArrayList<FriendGroup> groupArrayList, ArrayList<Integer> userArrayList) {
                                ((ImageButton) getView().findViewById(R.id.btnPrivacy)).setImageResource(getResourceId(clickNo));
                                mPrivateState = clickNo;
                                groupList = groupArrayList;
                                userIdList = userArrayList;
                            }
                        });
                        chooseFriendView.setPostBean(new PostBean().setFriendGroups(groupList).setFriendUsers(userIdList));
                        chooseFriendView.getData(true);
                        chooseFriendView.show();//显示对话框
                    } else {
                        mPrivateState = clickNo;
                        ((ImageButton) getView().findViewById(R.id.btnPrivacy)).setImageResource(getResourceId(clickNo));
                    }
                }
                popupWindow.dismiss();
            }
        });
    }

    /**
     * 分享Post的类型与图片Index之间的对应关系
     *
     * @param resourceId
     * @return
     */
    private int exChangeIndexAndPrivateState(int resourceId) {
        switch (resourceId) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 2;
            default:
                return 0;
        }
    }

    private int getResourceId(int id) {
        switch (id) {
            case 0:
                return R.drawable.public_unselected;
            case 1:
                return R.drawable.friend_unselected;
            case 2:
                return R.drawable.custom_unselected;
            case 3:
                return R.drawable.stranger_unselected;
            default:
                return R.drawable.public_unselected;
        }

    }

    @Override
    public void goBack() {
        getActivity().finish();
    }
}
