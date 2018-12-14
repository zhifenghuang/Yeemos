package com.yeemos.app.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.common.mentiontextview.MentionAdapter;
import com.common.mentiontextview.MentionTextView;
import com.gbsocial.BeansBase.FriendGroup;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.TagBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBPlatform;
import com.gigabud.common.platforms.errorkey.PlatformErrorKeys;
import com.gigabud.core.http.DownloadFileManager;
import com.gigabud.core.http.DownloadListener;
import com.gigabud.core.util.ConnectedUtil;
import com.gigabud.core.util.GBExecutionPool;
import com.gigabud.core.util.NetUtil;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.fragment.EditPostFragment;
import com.yeemos.app.fragment.MyInfoFragment;
import com.yeemos.app.fragment.ShowPostViewPagerFragment;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.interfaces.ChangeNumListener;
import com.yeemos.app.manager.DataChangeManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.yeemos.jni.ExtractDecodeEditEncodeMuxTest;
import com.yeemos.yeemos.jni.TextureRender;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by gigabud on 16-6-1.
 */
public class ShowPostView extends RelativeLayout implements View.OnClickListener, View.OnTouchListener, DownloadListener, MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {


    private SurfaceView mSurfaceView;
    private ImageView mShowPicImageView, mThumbImageView;
    private PostBean mPostBean;
    private int mShowViewType;
    private int mCurrentEmoID;
    private Bitmap mEmoBmp;
    private Dialog dialog;

    private DrawCommentView mDrawCommentView;

    private MediaPlayer mMediaPlayer;

    private ShowPostViewPagerFragment mShowPostViewPagerFragment;

    private boolean mIsPostObjectExist;

    private boolean mIsEditable;
    protected int mEditSelectEmoID;
    private boolean mIsEditAnonymousOn;
    private boolean mIsEditPostHrsOn;
    private String mEditPostText;
    private int mEditPrivateState;//选中图片的Index
    private boolean hasShowPostPopupWindow = false;
    private boolean firstPreDraw = true;

    private ArrayList<FriendGroup> groupList;
    private ArrayList<Integer> userIdList;
    private float pointX;


    private InputMethodManager mInputMethodManager;

    private boolean mIsVideoPlaying;

    private int mSourceWidth, mSourceHeight;

    public void setParentFragment(ShowPostViewPagerFragment showPostViewPagerFragment) {
        mShowPostViewPagerFragment = showPostViewPagerFragment;
    }

    public ShowPostView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.show_post_view, this);
        findViewById(R.id.tvCommentsNum).setOnClickListener(this);
        findViewById(R.id.tvDrawCommentNum).setOnClickListener(this);
        findViewById(R.id.btnView).setOnClickListener(this);
        findViewById(R.id.tvEmoNum).setOnClickListener(this);
        findViewById(R.id.btnPostDetail).setOnClickListener(this);
        findViewById(R.id.tvUserName).setOnClickListener(this);
        mIsPostObjectExist = false;
        mIsEditable = false;
        findViewById(R.id.rlBottom).setOnClickListener(this);

        findViewById(R.id.btnSelectEmo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPostBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                    mShowPostViewPagerFragment.addEmo();
                }
            }
        });
        findViewById(R.id.btnSelectEmo).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!mPostBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                    showPostPopupWindow(PostPopupView.POST_PUPUP_EMOS);
                    hasShowPostPopupWindow = true;
                }
                return true;
            }
        });

        getEtPost().setIsPupupDown(true);


        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsEditable) {
                    int screenWidth = mShowPostViewPagerFragment.getDisplaymetrics().widthPixels;
                    if (Math.abs(pointX) < screenWidth / 3) {
                        mShowPostViewPagerFragment.onPostItemClick(false);
                    } else {
                        mShowPostViewPagerFragment.onPostItemClick(true);
                    }
                }
            }
        });
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mIsEditable) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            pointX = event.getRawX();
                            break;
                    }
                }
                return false;
            }
        });
    }

    public void addReplyTag() {
        long replyTageNum = mPostBean.getReplyTagNums();
        mPostBean.setReplyTagNums(++replyTageNum);
        ((TextView) findViewById(R.id.tvEmoNum)).setText(String.valueOf(mPostBean.getReplyTagNums()));
    }

    public void resetRemarkName() {
        if (mPostBean == null) {
            return;
        }
        TextView tvUser = (TextView) findViewById(R.id.tvUserName);
        if (mPostBean.getIsAnonymity() == 1) {
            tvUser.setText(ServerDataManager.getTextFromKey("pblc_txt_anonymity"));
            tvUser.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            tvUser.setTextColor(getResources().getColor(R.color.color_221_221_221));
            if (mPostBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                tvUser.setText(ServerDataManager.getTextFromKey("pblc_txt_anonymity") + ServerDataManager.getTextFromKey("pblc_txt_me"));
            }
        } else {
            tvUser.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tvUser.setTextColor(Color.WHITE);
            if (mPostBean.getOwner().isAuthenticate()) {
                tvUser.setText(mPostBean.getOwner().getRemarkName() + " ");
                Drawable drawable = getResources().getDrawable(R.drawable.vip_tick);
                drawable.setBounds(0, 0, Utils.sp2px(getContext(), 18), Utils.sp2px(getContext(), 18));
                tvUser.setCompoundDrawables(null, null, drawable, null);
            } else {
                tvUser.setText(mPostBean.getOwner().getRemarkName());
                tvUser.setCompoundDrawables(null, null, null, null);
            }
        }
    }

    private long mStartViewTime;

    public void showViewByType() {
        initPostView();
        if (mShowPostViewPagerFragment == null || mShowPostViewPagerFragment.getActivity() == null) {
            return;
        }
        if (mPostBean == null) {
            return;
        }
//        if (!mShowPostViewPagerFragment.isShowPostViewCurrentItem(this)) {
//            return;
//        }
        if (mShowPostViewPagerFragment != null && !mPostBean.isAvailable()) {
            String content = ServerDataManager.getTextFromKey("pblc_txt_postnotavailable");
            String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
            ((BaseActivity) mShowPostViewPagerFragment.getActivity())
                    .showPublicDialog(null, content, Okey, null, false, true, onPostDialoghandler);
            return;
        }
        mShowPostViewPagerFragment.changeReplyTag(mCurrentEmoID);
        mShowViewType = mPostBean.getAttachDataType();
        mStartViewTime = System.currentTimeMillis();

        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                if (mThumbImageView != null) {
                    mThumbImageView.setVisibility(View.GONE);
                }
                mShowPicImageView.setVisibility(View.VISIBLE);
                mIsPostObjectExist = true;
                int width = ((BaseActivity) getContext()).getDisplaymetrics().widthPixels;
                int height = ((BaseActivity) getContext()).getDisplaymetrics().heightPixels;
                float ratio1 = bitmap.getWidth() * 1.0f / width;
                float ratio2 = bitmap.getHeight() * 1.0f / height;
                if (ratio2 > ratio1) {
                    mSourceWidth = (int) (bitmap.getWidth() / ratio2 + 0.5f);
                    mSourceHeight = height;
                } else {
                    mSourceWidth = width;
                    mSourceHeight = (int) (bitmap.getHeight() / ratio1 + 0.5f);
                }
                LayoutParams layoutParam = (RelativeLayout.LayoutParams) mShowPicImageView.getLayoutParams();
                layoutParam.addRule(RelativeLayout.CENTER_IN_PARENT,
                        RelativeLayout.TRUE);
                layoutParam.width = mSourceWidth;
                layoutParam.height = mSourceHeight;
                if (mShowPicImageView != null) {
                    mShowPicImageView.setImageBitmap(bitmap);
                    findViewById(R.id.fileLoadBar).setVisibility(View.GONE);
                }
            }
        };
        final RelativeLayout showViewLayout = (RelativeLayout) findViewById(R.id.show_view);
        if (mShowPicImageView == null) {
            mShowPicImageView = new ImageView(getContext());
            mShowPicImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            showViewLayout.addView(mShowPicImageView, 0);
        }
        if (mShowViewType == Constants.POST_ATTACH_DATA_TYPE.IMAGE_TEXT.GetValue()) {
            if (TextUtils.isEmpty(mPostBean.getImage())) {
                String content = ServerDataManager.getTextFromKey("pblc_txt_postnotavailable");
                String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                ((BaseActivity) mShowPostViewPagerFragment.getActivity())
                        .showPublicDialog(null, content, Okey, null, false, true, onPostDialoghandler);
                return;
            }
            destroyView();
            File picPath = new File(Preferences.getInstacne().getDownloadFilePathByName(mPostBean.getImage()));
            mIsPostObjectExist = false;
            if (picPath.exists()) {
                Glide.with(BaseApplication.getAppContext()) // could be an issue!
                        .load(Uri.fromFile(picPath))
                        .asBitmap()   //强制转换Bitmap
                        .into(target);
            } else {
                try {
                    if (!TextUtils.isEmpty(mPostBean.getImageSmall())) {
                        File thumbPath = new File(Preferences.getInstacne().getDownloadFilePathByName(mPostBean.getImageSmall()));
                        if (mThumbImageView == null) {
                            mThumbImageView = new ImageView(getContext());
                            mThumbImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            showViewLayout.addView(mThumbImageView, 0);
                        }
                        String thumbUrl = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(mPostBean.getImageSmall(), "utf-8"),
                                URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                        Utils.loadImage(BaseApplication.getAppContext(), thumbPath, 0, thumbUrl, mThumbImageView);

                        SimpleTarget<Bitmap> thumbTarget = new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                                int width = ((BaseActivity) getContext()).getDisplaymetrics().widthPixels;
                                int height = ((BaseActivity) getContext()).getDisplaymetrics().heightPixels;
                                float ratio1 = bitmap.getWidth() * 1.0f / width;
                                float ratio2 = bitmap.getHeight() * 1.0f / height;
                                if (ratio2 > ratio1) {
                                    mSourceWidth = (int) (bitmap.getWidth() / ratio2 + 0.5f);
                                    mSourceHeight = height;
                                } else {
                                    mSourceWidth = width;
                                    mSourceHeight = (int) (bitmap.getHeight() / ratio1 + 0.5f);
                                }

                                LayoutParams layoutParam = (RelativeLayout.LayoutParams) mThumbImageView.getLayoutParams();
                                layoutParam.addRule(RelativeLayout.CENTER_IN_PARENT,
                                        RelativeLayout.TRUE);
                                layoutParam.width = mSourceWidth;
                                layoutParam.height = mSourceHeight;
                                if (mThumbImageView != null) {
                                    mThumbImageView.setImageBitmap(bitmap);
                                }
                            }
                        };

                        if (thumbPath.exists()) {
                            Glide.with(BaseApplication.getAppContext()) // could be an issue!
                                    .load(Uri.fromFile(thumbPath))
                                    .asBitmap()   //强制转换Bitmap
                                    .into(thumbTarget);
                        } else {
                            Glide.with(BaseApplication.getAppContext()) // could be an issue!
                                    .load(thumbUrl)
                                    .asBitmap()   //强制转换Bitmap
                                    .into(thumbTarget);
                        }
                    }
                    String imageURL = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(mPostBean.getImage(), "utf-8"),
                            URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));

                    Glide.with(BaseApplication.getAppContext()) // could be an issue!
                            .load(imageURL)
                            .asBitmap()   //强制转换Bitmap
                            .into(target);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            showPostText();
        } else if (mShowViewType == Constants.POST_ATTACH_DATA_TYPE.VIDEO_TEXT.GetValue()) {
            if (TextUtils.isEmpty(mPostBean.getVideo())) {
                String content = ServerDataManager.getTextFromKey("pblc_txt_postnotavailable");
                String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                ((BaseActivity) mShowPostViewPagerFragment.getActivity())
                        .showPublicDialog(null, content, Okey, null, false, true, onPostDialoghandler);
                return;
            }
            if (isPlayingVideo()) {
                mIsPostObjectExist = true;
                findViewById(R.id.fileLoadBar).setVisibility(View.GONE);
                return;
            }
            destroyView();
            String videoPath = Preferences.getInstacne().getDownloadFilePathByName(mPostBean.getVideo());
            if (new File(videoPath).exists()) {
                if (mSourceWidth == 0 || mSourceHeight == 0) {
                    android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
                    try {
                        mmr.setDataSource(videoPath);
                        int videoWidth = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                        int videoHeight = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

                        int width = ((BaseActivity) getContext()).getDisplaymetrics().widthPixels;
                        int height = ((BaseActivity) getContext()).getDisplaymetrics().heightPixels;
                        float ratio1 = videoWidth * 1.0f / width;
                        float ratio2 = videoHeight * 1.0f / height;
                        if (ratio2 > ratio1) {
                            mSourceWidth = (int) (videoWidth / ratio2 + 0.5f);
                            mSourceHeight = height;
                        } else {
                            mSourceWidth = width;
                            mSourceHeight = (int) (videoHeight / ratio1 + 0.5f);
                        }
                    } catch (Exception ex) {
                        Log.e("EditPostFragment", "MediaMetadataRetriever exception " + ex);
                    } finally {
                        mmr.release();
                        mmr = null;
                    }
                }
                //if (mSurfaceView == null) {
                mSurfaceView = new SurfaceView(getContext());
                LayoutParams layoutParam = new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                layoutParam.addRule(RelativeLayout.CENTER_IN_PARENT,
                        RelativeLayout.TRUE);
                layoutParam.width = mSourceWidth;
                layoutParam.height = mSourceHeight;
                showViewLayout.addView(mSurfaceView, 0, layoutParam);
                // }
                SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
                surfaceHolder.addCallback(this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                surfaceHolder.setFormat(PixelFormat.RGBA_8888);
                showPostText();
                mIsPostObjectExist = true;
            } else {
                mShowPicImageView.setVisibility(View.VISIBLE);
                mShowPicImageView.setImageBitmap(null);
                if (!TextUtils.isEmpty(mPostBean.getImageSmall())) {
                    try {
                        String thumbUrl = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(mPostBean.getImageSmall(), "utf-8"),
                                URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                        Glide.with(BaseApplication.getAppContext()) // could be an issue!
                                .load(thumbUrl)
                                .asBitmap()   //强制转换Bitmap
                                .into(target);
                    } catch (Exception e) {

                    }
                }
                mIsPostObjectExist = false;
                if (!mPostBean.isDownloadFileExistInServer()) {
                    return;
                }
                try {
                    String videoURL = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(mPostBean.getVideo(), "utf-8"), URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                    DownloadFileManager.getInstance().addDownloadFile(BaseApplication.getAppContext(), mPostBean.getId(), videoURL, mPostBean.getVideo(), 1);
                    DownloadFileManager.getInstance().addDownloadListener(this);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        findViewById(R.id.fileLoadBar).setVisibility(mIsPostObjectExist ? View.GONE : View.VISIBLE);
    }


    public void showPostPic(PostBean postBean) {
        if (mShowPostViewPagerFragment == null || mShowPostViewPagerFragment.getActivity() == null) {
            return;
        }
        mPostBean = postBean;
        mShowViewType = mPostBean.getAttachDataType();
        mStartViewTime = System.currentTimeMillis();
        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                if (mThumbImageView != null) {
                    mThumbImageView.setVisibility(View.GONE);
                }
                int width = ((BaseActivity) getContext()).getDisplaymetrics().widthPixels;
                int height = ((BaseActivity) getContext()).getDisplaymetrics().heightPixels;
                float ratio1 = bitmap.getWidth() * 1.0f / width;
                float ratio2 = bitmap.getHeight() * 1.0f / height;
                if (ratio2 > ratio1) {
                    mSourceWidth = (int) (bitmap.getWidth() / ratio2 + 0.5f);
                    mSourceHeight = height;
                } else {
                    mSourceWidth = width;
                    mSourceHeight = (int) (bitmap.getHeight() / ratio1 + 0.5f);
                }
                LayoutParams layoutParam = (RelativeLayout.LayoutParams) mShowPicImageView.getLayoutParams();
                layoutParam.width = mSourceWidth;
                layoutParam.height = mSourceHeight;
                layoutParam.addRule(RelativeLayout.CENTER_IN_PARENT,
                        RelativeLayout.TRUE);
                if (mShowPicImageView != null) {
                    mShowPicImageView.setImageBitmap(bitmap);
                    findViewById(R.id.fileLoadBar).setVisibility(View.GONE);
                }
            }
        };
        final RelativeLayout showViewLayout = findViewById(R.id.show_view);
        if (mShowPicImageView == null) {
            mShowPicImageView = new ImageView(getContext());
            mShowPicImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            showViewLayout.addView(mShowPicImageView, 0);
        }
        findViewById(R.id.fileLoadBar).setVisibility(View.VISIBLE);
        if (mShowViewType == Constants.POST_ATTACH_DATA_TYPE.IMAGE_TEXT.GetValue()) {
            destroyView();
            mShowPicImageView.setVisibility(View.VISIBLE);
            mShowPicImageView.setImageBitmap(null);
            File picPath = new File(Preferences.getInstacne().getDownloadFilePathByName(mPostBean.getImage()));
            if (picPath.exists()) {
                Glide.with(BaseApplication.getAppContext()) // could be an issue!
                        .load(Uri.fromFile(picPath))
                        .asBitmap()   //强制转换Bitmap
                        .into(target);
            } else {
                try {
                    if (!TextUtils.isEmpty(mPostBean.getImageSmall())) {
                        File thumbPath = new File(Preferences.getInstacne().getDownloadFilePathByName(mPostBean.getImageSmall()));
                        if (mThumbImageView == null) {
                            mThumbImageView = new ImageView(getContext());
                            mThumbImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            showViewLayout.addView(mThumbImageView, 0);
                        }
                        mThumbImageView.setVisibility(View.VISIBLE);
                        mThumbImageView.setImageBitmap(null);
                        String thumbUrl = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(mPostBean.getImageSmall(), "utf-8"),
                                URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                        Utils.loadImage(BaseApplication.getAppContext(), thumbPath, 0, thumbUrl, mThumbImageView);

                        SimpleTarget<Bitmap> thumbTarget = new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                                int width = ((BaseActivity) getContext()).getDisplaymetrics().widthPixels;
                                int height = ((BaseActivity) getContext()).getDisplaymetrics().heightPixels;
                                float ratio1 = bitmap.getWidth() * 1.0f / width;
                                float ratio2 = bitmap.getHeight() * 1.0f / height;
                                if (ratio2 > ratio1) {
                                    mSourceWidth = (int) (bitmap.getWidth() / ratio2 + 0.5f);
                                    mSourceHeight = height;
                                } else {
                                    mSourceWidth = width;
                                    mSourceHeight = (int) (bitmap.getHeight() / ratio1 + 0.5f);
                                }

                                LayoutParams layoutParam = (RelativeLayout.LayoutParams) mThumbImageView.getLayoutParams();
                                layoutParam.addRule(RelativeLayout.CENTER_IN_PARENT,
                                        RelativeLayout.TRUE);
                                layoutParam.width = mSourceWidth;
                                layoutParam.height = mSourceHeight;
                                if (mThumbImageView != null) {
                                    mThumbImageView.setImageBitmap(bitmap);
                                }
                            }
                        };

                        if (thumbPath.exists()) {
                            Glide.with(BaseApplication.getAppContext()) // could be an issue!
                                    .load(Uri.fromFile(thumbPath))
                                    .asBitmap()   //强制转换Bitmap
                                    .into(thumbTarget);
                        } else {
                            Glide.with(BaseApplication.getAppContext()) // could be an issue!
                                    .load(thumbUrl)
                                    .asBitmap()   //强制转换Bitmap
                                    .into(thumbTarget);
                        }
                    }
                    String imageURL = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(mPostBean.getImage(), "utf-8"),
                            URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));

                    Glide.with(BaseApplication.getAppContext()) // could be an issue!
                            .load(imageURL)
                            .asBitmap()   //强制转换Bitmap
                            .into(target);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            showPostText();
        } else if (mShowViewType == Constants.POST_ATTACH_DATA_TYPE.VIDEO_TEXT.GetValue()) {
            destroyView();
            if (mShowPicImageView == null) {
                mShowPicImageView = new ImageView(getContext());
                mShowPicImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                showViewLayout.addView(mShowPicImageView, 0);
            }
            mShowPicImageView.setVisibility(View.VISIBLE);
            mShowPicImageView.setImageBitmap(null);
            if (!TextUtils.isEmpty(mPostBean.getImageSmall())) {
                try {
                    String thumbUrl = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(mPostBean.getImageSmall(), "utf-8"),
                            URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                    Glide.with(BaseApplication.getAppContext()) // could be an issue!
                            .load(thumbUrl)
                            .asBitmap()   //强制转换Bitmap
                            .into(target);
                } catch (Exception e) {

                }
            }
            if (!mPostBean.isDownloadFileExistInServer()) {
                return;
            }
            try {
                String videoURL = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(mPostBean.getVideo(), "utf-8"), URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                DownloadFileManager.getInstance().addDownloadFile(BaseApplication.getAppContext(), mPostBean.getId(), videoURL, mPostBean.getVideo(), 1);
                DownloadFileManager.getInstance().addDownloadListener(this);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        findViewById(R.id.fileLoadBar).setVisibility(View.GONE);
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (Exception e) {
                Log.e("releaseMediaPlayer", "Exception: " + e.toString());
            }
        }
    }

    public boolean isPlayingVideo() {
        return mMediaPlayer != null;
    }

    private void showPostText() {
        if (mDrawCommentView != null) {
            return;
        }
        findViewById(R.id.rlText).setVisibility(View.VISIBLE);
        findViewById(R.id.rlTextAll).setVisibility(View.GONE);
        findViewById(R.id.rlTextAll).setOnTouchListener(this);
        firstPreDraw = true;
        final TextViewDoubleClick tv1 = (TextViewDoubleClick) findViewById(R.id.tvOne);
        tv1.setText("");
        tv1.setMovementMethod(TextViewDoubleClick.LocalLinkMovementMethod.getInstance());
        tv1.setText(Utils.getKeywordClickable(mPostBean.getText(), null, null, R.color.color_45_223_227));
        final ViewTreeObserver vto = tv1.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (!firstPreDraw) {
                    return true;
                }
                firstPreDraw = false;
                int lineCount = tv1.getLineCount();
                if (lineCount > 3) {
                    tv1.setOnTouchListener(ShowPostView.this);
                    findViewById(R.id.rlText).setOnTouchListener(ShowPostView.this);
                    int lineEndIndex = tv1.getLayout().getLineEnd(2);//获取被截断的字符长度
                    if (lineEndIndex > 3) {
                        lineEndIndex -= 3;
                    }
                    String text = tv1.getText().subSequence(0, lineEndIndex) + "...";//为了显示省略号
                    tv1.setText(text);
                } else {
                    tv1.setOnTouchListener(null);
                    findViewById(R.id.rlText).setOnTouchListener(null);
                }
                return true;
            }
        });
    }

    private void showTextComplete() {
        if (mDrawCommentView != null) {
            return;
        }
        findViewById(R.id.rlTextAll).setVisibility(View.VISIBLE);
        findViewById(R.id.rlText).setVisibility(View.GONE);
        TextViewDoubleClick showAllText = (TextViewDoubleClick) findViewById(R.id.tvTextAll);
        showAllText.setOnTouchListener(this);
        showAllText.setMovementMethod(TextViewDoubleClick.LocalLinkMovementMethod.getInstance());
        showAllText.setText(Utils.getKeywordClickable(mPostBean.getText(), null, null, R.color.color_45_223_227));
    }

    public void resetDrawCommentNum(int num) {
        mPostBean.setImgCommentNum(num);
        ((TextView) findViewById(R.id.tvDrawCommentNum)).setText(String.valueOf(num));
    }

    public void setPostBean(final PostBean postBean, final boolean isFirstShow) {
        if (mPostBean != null && postBean.getId().equals(mPostBean.getId())) {
            initPostView();
            return;
        }
        mPostBean = postBean;
        if (!mShowPostViewPagerFragment.isOldData()) {
            showViewByType();
        } else {
            initPostView();
            if (!NetUtil.isConnected(getContext())) {
                mPostBean.setAvailable(true);
                showViewByType();
                return;
            }
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ServerResultBean<PostBean> serverResultBean = DataManager.getInstance().getOnePostInfo(postBean);
                    if (serverResultBean != null && serverResultBean.getData() != null) {
                        mPostBean = serverResultBean.getData();
                        if (mPostBean == null
                                || (!mPostBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())
                                && mPostBean.getOwner().isPrivate()
                                && mPostBean.getOwner().getFollowStatus() != 1)) {
                            mPostBean.setAvailable(false);
                        }
                        if (isFirstShow) {
                            ((BaseActivity) getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showViewByType();
                                }
                            });
                        }
                    } else if (serverResultBean != null && !serverResultBean.isSuccess()) {
                        if (serverResultBean.getErrorCode() != null
                                && (serverResultBean.getErrorCode().equals(PlatformErrorKeys.CONNECTTION_OFFLINE)
                                || serverResultBean.getErrorCode().equals(PlatformErrorKeys.CONNECTTION_EXCEPTION)
                                || serverResultBean.getErrorCode().equals(PlatformErrorKeys.CONNECTTION_ERROR)
                                || serverResultBean.getErrorCode().equals(PlatformErrorKeys.CONNECTTION_TIMEOUT))) {
                            mPostBean.setAvailable(true);
                        } else {
                            mPostBean.setAvailable(false);
                        }
                        ((BaseActivity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showViewByType();
                            }
                        });
                    }
                }
            });
        }
    }

    private void initPostView() {

        ArrayList<TagBean> tagList = mPostBean.getTags();
        if (tagList != null && !tagList.isEmpty()) {
            mCurrentEmoID = tagList.get(0).getId();
        } else {
            mCurrentEmoID = -1;
        }
        resetViewBySelectEmo();


        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.rlUserName).getLayoutParams();
        if (MemberShipManager.getInstance().getUserID().equals(mPostBean.getOwner().getUserId())) {
            findViewById(R.id.btnView).setVisibility(VISIBLE);
            ((TextView) findViewById(R.id.btnView)).setText(Utils.transformKiloNumber(mPostBean.getReadCount()));
            ImageButton btnPostPrivacy = (ImageButton) findViewById(R.id.btnPostPrivacy);
            btnPostPrivacy.setVisibility(View.VISIBLE);
            lp = (RelativeLayout.LayoutParams) btnPostPrivacy.getLayoutParams();
            int width = Utils.dip2px(getContext(), 27);
            if (mPostBean.getIsAnonymity() == 1) {
                lp.width = (int) (width * 0.9f);
                lp.height = (int) (width * 0.9f);
            } else {
                lp.width = width;
                lp.height = width;
            }
            btnPostPrivacy.setLayoutParams(lp);
            btnPostPrivacy.setImageResource(getResourceId(exChangeIndexAndPrivateState(mPostBean.getIsPrivate())));
            if (mPostBean.getIsPrivate() == 3) {
                btnPostPrivacy.setOnClickListener(this);
            } else {
                btnPostPrivacy.setOnClickListener(null);
            }
        } else {
            findViewById(R.id.btnPostPrivacy).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnView).setVisibility(INVISIBLE);
        }

        ((TextView) findViewById(R.id.tvDrawCommentNum)).setText(String.valueOf(mPostBean.getImgCommentNum()));

        ((TextView) findViewById(R.id.tvCommentsNum)).setText(String.valueOf(mPostBean.getCommentNums()));
        ((TextView) findViewById(R.id.tvEmoNum)).setText(String.valueOf(mPostBean.getReplyTagNums()));
        ((TextView) findViewById(R.id.tvPostBeforeTime)).setText(Utils.getTime(mPostBean.getCreateTime()));
        TextView tvUser = (TextView) findViewById(R.id.tvUserName);
        if (mPostBean.getIsAnonymity() == 1) {
            tvUser.setText(ServerDataManager.getTextFromKey("pblc_txt_anonymity"));
            tvUser.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            tvUser.setTextColor(getResources().getColor(R.color.color_221_221_221));
            if (mPostBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                tvUser.setText(ServerDataManager.getTextFromKey("pblc_txt_anonymity") + ServerDataManager.getTextFromKey("pblc_txt_me"));
            }
            tvUser.setCompoundDrawables(null, null, null, null);
        } else {
            tvUser.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tvUser.setTextColor(Color.WHITE);

            if (mPostBean.getOwner().isAuthenticate()) {
                tvUser.setText(mPostBean.getOwner().getRemarkName() + " ");
                Drawable drawable = getResources().getDrawable(R.drawable.vip_tick);
                drawable.setBounds(0, 0, Utils.sp2px(getContext(), 18), Utils.sp2px(getContext(), 18));
                tvUser.setCompoundDrawables(null, null, drawable, null);
            } else {
                tvUser.setText(mPostBean.getOwner().getRemarkName());
                tvUser.setCompoundDrawables(null, null, null, null);
            }
        }

        if (mPostBean.getExpiredType() == 1) {
            findViewById(R.id.roundProgressBar).setVisibility(VISIBLE);
            ((RoundProgressBar) findViewById(R.id.roundProgressBar))
                    .setProgress((int) Math.floor((Utils.getCurrentServerTime() - mPostBean.getCreateTime()) / (60 * 60 * 1000)));
        } else {
            findViewById(R.id.roundProgressBar).setVisibility(GONE);
        }

        findViewById(R.id.ivIsFromAlbum).setVisibility(mPostBean.isFromAblum() ? View.VISIBLE : View.GONE);
    }

    public boolean isDownloadedObjectTimeOut() {
        boolean isDownloadedObjectTimeOut = !mIsPostObjectExist && (System.currentTimeMillis() - mStartViewTime) >= 90 * 1000;
        if (isDownloadedObjectTimeOut) {
            mStartViewTime = System.currentTimeMillis();
        }
        return isDownloadedObjectTimeOut;
    }


    public PostBean getPostBean() {
        return mPostBean;
    }

    public Bitmap getEmoBmp() {
        if (mCurrentEmoID == -1) {
            return null;
        }
        if (mEmoBmp == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            mEmoBmp = BitmapFactory.decodeResource(getResources(), Constants.EMO_ID_COLOR[mCurrentEmoID][0], options);
        }
        return mEmoBmp;
    }

    public boolean isPostEditViewVisibility() {
        return getPostEditView().getVisibility() == View.VISIBLE;
    }

    protected MentionTextView getEtPost() {
        return (MentionTextView) findViewById(R.id.etPost);
    }

    private InputMethodManager getInputMethodManager() {
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return mInputMethodManager;
    }

    public void showOrHideSoftKey(boolean isShow) {
        if (isShow) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    getInputMethodManager().toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }, 50);
        } else {
            if (getInputMethodManager().isActive()) {
                getInputMethodManager().hideSoftInputFromWindow(this.getWindowToken(), 0);
            }
        }
    }


    protected void clickBtnText() {
        findViewById(R.id.bottomEditView).setVisibility(View.GONE);
        findViewById(R.id.rlPostText).setVisibility(View.GONE);
        getEtPost().setFocusable(true);
        getEtPost().setFocusableInTouchMode(true);
        getEtPost().requestFocus();
        showOrHideSoftKey(true);
        getEtPost().setMaxLines(3);
        getEtPost().setHint(ServerDataManager.getTextFromKey("edtpst_txt_titletagmentionfriend"));
        getEtPost().setGravity(Gravity.LEFT | Gravity.TOP);
        getEtPost().setPadding(0, Utils.dip2px(getContext(), 60), 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvUserName:
                if (mPostBean.getIsAnonymity() == 0) {
                    DataManager.getInstance().setCurOtherUser(mPostBean.getOwner());
                    if (mPostBean.getOwner().getUserName().equals(MemberShipManager.getInstance().getUserInfo().getUserName())) {
                        mShowPostViewPagerFragment.gotoPager(MyInfoFragment.class, null);
                    } else {
                        mShowPostViewPagerFragment.gotoPager(UserInfoFragment.class, null);
                    }
                }
                break;
            case R.id.tvEmoNum:
                showPillSreactView();
                break;
            case R.id.btnPostDetail:
                showPostDetail();
                break;
            case R.id.tvCommentsNum:
                showCommentsView();
                break;
            case R.id.tvDrawCommentNum:
                if (mIsPostObjectExist) {
                    showDrawCommentsView();
                }
                break;
            case R.id.rlText:
                if (!mIsEditable) {
                    findViewById(R.id.rlText).setVisibility(View.GONE);
                    findViewById(R.id.rlTextAll).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.tvTextAll)).setText(mPostBean.getText());
                }
                break;
            case R.id.rlPostText:
            case R.id.btnText:
                if (!isPostEditViewVisibility()) {
                    clickBtnText();
                    getPostEditView().setVisibility(View.VISIBLE);
                    //     ((ImageView) findViewById(R.id.btnBack)).setImageDrawable(getResources().getDrawable(R.drawable.edit_close));
                    mEditPostText = mEditPostText == null ? "" : mEditPostText;
                    getEtPost().setText(mEditPostText);
                    getEtPost().setTextSize(16);
                    getEtPost().setSelection(mEditPostText.length());
                    getEtPost().setTextColor(Color.WHITE);
                    getEtPost().setTypeface(Typeface.DEFAULT);
                } else {
                    showView();
                }
                break;
            case R.id.btnBack:
                if (!isPostEditViewVisibility()) {
                    mIsEditable = false;
                    showOrHideEditPostView();
                } else {
                    showView();
                }
                break;
            case R.id.btnEmo:
                showPostPopupWindow(PostPopupView.POST_PUPUP_EMOS);
                break;
            case R.id.btnPrivacy:
                showPostPopupWindow(PostPopupView.POST_PUPUP_PRIVACY);
                break;
            case R.id.btnAnonymous:
                mIsEditAnonymousOn = !mIsEditAnonymousOn;
                ((ImageButton) v).
                        setImageResource(mIsEditAnonymousOn ? R.drawable.edit_anonymous : R.drawable.edit_not_anonymous);
                break;
            case R.id.btnSetTime:
                mIsEditPostHrsOn = !mIsEditPostHrsOn;
                ((ImageButton) v).
                        setImageResource(mIsEditPostHrsOn ? R.drawable.edit_24hrs : R.drawable.edit_unlimited);
                break;
            case R.id.btnShare:
                if (mIsEditAnonymousOn && mPostBean.getIsAnonymity() != 1) {
                    long lastAnonymousTime = Preferences.getInstacne().getValues(EditPostFragment.LAST_ANONYMOUS_TIME, 0l);
                    if (System.currentTimeMillis() - lastAnonymousTime < EditPostFragment.ANONYMOUS_TIME) {
                        Toast.makeText(getContext(), ServerDataManager.getTextFromKey("edtpst_txt_oneanonymous"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                mShowPostViewPagerFragment.showLoadingDialog("", null, true);
                GBExecutionPool.getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentEmoID = mEditSelectEmoID;
                        ArrayList<TagBean> tagList = mPostBean.getTags();
                        tagList.get(0).setId(mEditSelectEmoID);

                        mPostBean.setIsPrivate(exChangeIndexAndPrivateState(mEditPrivateState));
                        if (mPostBean.getIsPrivate() == 3) {//自由选择好友分享
                            mPostBean.setFriendGroups(groupList);
                            mPostBean.setFriendUsers(userIdList);
                        }
                        if (mIsEditPostHrsOn && mPostBean.getExpiredType() != 1) {
                            mPostBean.setCreateTime(Utils.getCurrentServerTime());
                        }
                        mPostBean.setExpiredType(mIsEditPostHrsOn ? 1 : 0);
                        mPostBean.setIsAnonymity(mIsEditAnonymousOn ? 1 : 0);
                        mEditPostText = getEtPost().getText().toString();
                        mPostBean.setText(mEditPostText);
                        final ServerResultBean resultBean = DataManager.getInstance().updatePost(mPostBean);
                        ((BaseActivity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mShowPostViewPagerFragment.hideLoadingDialog();
                                if (resultBean.isSuccess()) {
                                    mIsEditable = false;
                                    showOrHideEditPostView();
                                    resetViewBySelectEmo();
                                    showPostText();
                                    mShowPostViewPagerFragment.changeReplyTag(mCurrentEmoID);
                                    DataChangeManager.getInstance().notifyDataChange(1, mPostBean, 6);
                                    Toast.makeText(getContext(), ServerDataManager.getTextFromKey("pblc_txt_editedsuccessful"), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                break;
            case R.id.btnView:
                ReadUserPopuWindow viewPopuWindow = new ReadUserPopuWindow(mShowPostViewPagerFragment.getActivity());
                viewPopuWindow.setPost(getPostBean());
                viewPopuWindow.showAtLocation(this, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                break;
            case R.id.btnPostPrivacy:
                CustomView mCustomView = new CustomView(mShowPostViewPagerFragment.getActivity());
                mCustomView.setPost(getPostBean());
                mCustomView.showAtLocation(this, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                break;
        }
    }

    private void showPostText(final String postText) {
        final TextView tv1 = (TextView) findViewById(R.id.tv1);
        final TextView tv2 = (TextView) findViewById(R.id.tv2);
        tv1.setText("");
        tv2.setText("");
        tv1.setText(postText == null ? "" : postText);
        tv1.addOnLayoutChangeListener(new OnLayoutChangeListener() {
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
                    tv2.setMaxLines(1);
                    tv2.setVisibility(View.VISIBLE);
                } else {
                    tv2.setVisibility(View.GONE);
                }
            }
        });
    }

    public View getPostEditView() {
        return findViewById(R.id.postEditView);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mDrawCommentView != null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (v.getId() == R.id.rlTextAll && v.getVisibility() == View.VISIBLE) {
                    findViewById(R.id.rlText).setVisibility(View.VISIBLE);
                    findViewById(R.id.rlTextAll).setVisibility(View.GONE);
                    return true;
                } else if ((v.getId() == R.id.tvOne || v.getId() == R.id.rlText) && v.getVisibility() == View.VISIBLE) {
                    showTextComplete();
                }
                break;

        }
        return false;
    }

    public void resetWH() {
        mSourceWidth = 0;
        mSourceHeight = 0;
    }

    public void destroyView() {
        DownloadFileManager.getInstance().removeDownloadListener(this);
        if (mShowViewType == Constants.POST_ATTACH_DATA_TYPE.VIDEO_TEXT.GetValue()) {
            releaseMediaPlayer();
            findViewById(R.id.rlPlayMovieCountdownView).setVisibility(View.GONE);
        }
//        if (mShowPicImageView != null) {
//            ((RelativeLayout) findViewById(R.id.show_view)).setVisibility(View.GONE);
//            //   mShowPicImageView = null;
//        }
        if (mSurfaceView != null) {
            ((RelativeLayout) findViewById(R.id.show_view)).removeView(mSurfaceView);
            mSurfaceView = null;
        }
//        if (mThumbImageView != null) {
//            ((RelativeLayout) findViewById(R.id.show_view)).setVisibility(View.GONE);
//            //       mThumbImageView = null;
//        }
        findViewById(R.id.rlText).setVisibility(View.GONE);
    }


    @Override
    public void notifyDownloadInfo(String tag, String url, long fileSize, long currentDownloadSize) {
        if (mPostBean == null) {
            return;
        }
        if (fileSize == currentDownloadSize) {
            if (tag.equals(mPostBean.getId()) && getContext() != null) {
                ((BaseActivity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostBean.setDownloadFileExistInServer(true);
                        showViewByType();
                    }
                });
            }
        } else if (fileSize == DownloadFileManager.NO_FOUND_FILE_SIZE) {
            if (tag.equals(mPostBean.getId())) {
                mPostBean.setDownloadFileExistInServer(false);
            }
        }
    }

    /**
     * 显示选表情和隐私PopupWindow
     *
     * @param popupWindowType
     */
    private synchronized void showPostPopupWindow(int popupWindowType) {
        if (hasShowPostPopupWindow) {
            return;
        }
        hasShowPostPopupWindow = true;
        PostPopupView postPopupView = new PostPopupView(getContext(), popupWindowType,
                popupWindowType == PostPopupView.POST_PUPUP_EMOS ? 0 : mEditPrivateState, R.drawable.bubble_cloud_show_post);
        int[] size = postPopupView.getViewSize();
        final PopupWindow popupWindow = new PopupWindow(postPopupView, size[0],
                size[1], true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();
        View v;
        if (mIsEditable) {
            v = findViewById(R.id.bottomEditView);
        } else {
            v = findViewById(R.id.btnSelectEmo);
        }
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0],
                popupWindowType == PostPopupView.POST_PUPUP_EMOS ? ((int) (location[1] - 0.92 * size[1])) : ((int) (location[1] - 0.85 * size[1])));
        popupWindow.getContentView().setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                hasShowPostPopupWindow = false;
                popupWindow.dismiss();
                return false;
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hasShowPostPopupWindow = false;
            }
        });

        postPopupView.setOnPopupItemClick(new PostPopupView.OnPopupItemClick() {
            @Override
            public void popupItemClick(int popupType, final int clickNo) {
                if (popupType == PostPopupView.POST_PUPUP_EMOS) {
                    if (!mIsEditable) {
                        mCurrentEmoID = clickNo;
                        resetViewBySelectEmo();
                        mShowPostViewPagerFragment.changeReplyTag(mCurrentEmoID);
                        mShowPostViewPagerFragment.addEmo();
                    } else {
                        mEditSelectEmoID = clickNo;
                        resetEditViewByPostEmo(clickNo);
                    }
                } else {

                    if (clickNo == 2) {
                        ChooseFriendView chooseFriendView = new ChooseFriendView(mShowPostViewPagerFragment.getContext(), R.style.ActionSheetDialogStyle);
                        DisplayMetrics displayMetrics = mShowPostViewPagerFragment.getDisplaymetrics();
                        chooseFriendView.setDialogSize(displayMetrics.widthPixels - 30, displayMetrics.heightPixels - 100);
                        chooseFriendView.setAddGroupAndUserListener(new ChooseFriendView.AddGroupAndUserListener() {
                            @Override
                            public void AddGroupAndUserToPoast(ArrayList<FriendGroup> groupArrayList, ArrayList<Integer> userArrayList) {
                                groupList = groupArrayList;
                                userIdList = userArrayList;
                                mEditPrivateState = clickNo;
                                ((ImageButton) findViewById(R.id.btnPrivacy)).setImageResource(getResourceId(clickNo));
                            }
                        });
                        chooseFriendView.setPostBean(mPostBean);
                        chooseFriendView.getData(true);
                        chooseFriendView.show();//显示对话框
                    } else {
                        mEditPrivateState = clickNo;
                        ((ImageButton) findViewById(R.id.btnPrivacy)).setImageResource(getResourceId(clickNo));
                    }
                }
                hasShowPostPopupWindow = false;
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

    /**
     * 显示绘画评论页面
     */
    public void showDrawCommentsView() {
        mDrawCommentView = new DrawCommentView(getContext());
        mDrawCommentView.setCurrentUseIn(DrawCommentView.USE_IN_SHOW_POST, mShowPostViewPagerFragment, this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mSourceWidth, mSourceHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT,
                RelativeLayout.TRUE);
        ((RelativeLayout) (mShowPostViewPagerFragment.getView())).addView(mDrawCommentView, lp);
        showOrHideViewWhenShowCommentsView(false);
    }

    public boolean isShowDrawCommentsView() {
        return mDrawCommentView != null;
    }

    public void showOrHideViewWhenShowCommentsView(boolean isShow) {
        if (isShow) {
            if (mShowPostViewPagerFragment.getShowPostType() == ShowPostViewPagerFragment.SHOW_POST_BY_DRAWING_COMMENT_GCM) {
                mShowPostViewPagerFragment.goBack();
                return;
            }
            findViewById(R.id.topView).setVisibility(View.VISIBLE);
            findViewById(R.id.rlBottom).setVisibility(View.VISIBLE);
            findViewById(R.id.rightView).setVisibility(View.VISIBLE);
            findViewById(R.id.rlText).setVisibility(View.VISIBLE);
            mDrawCommentView = null;
        } else {
            findViewById(R.id.topView).setVisibility(View.GONE);
            findViewById(R.id.rlBottom).setVisibility(View.GONE);
            findViewById(R.id.rightView).setVisibility(View.GONE);
            findViewById(R.id.rlText).setVisibility(View.GONE);
        }
    }


    /**
     * 显示评论页面
     */
    private void showCommentsView() {
        DataManager.getInstance().setCurPostBean(mPostBean);
        final DisplayMetrics displayMetrics = mShowPostViewPagerFragment.getDisplaymetrics();
        final CommontsView commontsView = new CommontsView(mShowPostViewPagerFragment.getActivity(), R.style.ActionSheetDialogStyle);
        commontsView.setChangeNumListener(new ChangeNumListener() {
            @Override
            public void setDataNum(int num) {
                ((TextView) findViewById(R.id.tvCommentsNum)).setText(String.valueOf(num));
            }
        });
        commontsView.setAnonymity(mPostBean.getIsAnonymity() == 1);
        Window dialogWindow = commontsView.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = displayMetrics.widthPixels; //设置宽度
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        commontsView.show();//显示对话框
    }

    private RelativeLayout getParentViewLy() {
        return (RelativeLayout) findViewById(R.id.show_view);
    }


    /**
     * 显示心情比例页面
     */
    private void showPillSreactView() {
        DataManager.getInstance().setCurPostBean(mPostBean);
        // DisplayMetrics displayMetrics = mShowPostViewPagerFragment.getDisplaymetrics();
        final PillSreactView pillSreactView = new PillSreactView(mShowPostViewPagerFragment.getActivity(), mPostBean);
        pillSreactView.setChangeNumListener(new ChangeNumListener() {
            @Override
            public void setDataNum(int num) {
                TextView tvEmoNum = findViewById(R.id.tvEmoNum);
                String text = tvEmoNum.getText().toString();
                try {
                    int current = Integer.parseInt(text);
                    if (current > num) {
                        return;
                    }
                } catch (Exception e) {

                }
                ((TextView) findViewById(R.id.tvEmoNum)).setText(String.valueOf(num));
            }
        });
        pillSreactView.setAnonymity(mPostBean.getIsAnonymity() == 1);
        pillSreactView.showAtLocation(this, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }

    private void resetViewBySelectEmo() {
        if (mCurrentEmoID == -1) {
            mEmoBmp = null;
            return;
        }
        ((ScaleImageButton) findViewById(R.id.btnSelectEmo)).setImageResource(Constants.EMO_ID_COLOR[mCurrentEmoID][0]);
        if (mPostBean.getOwner().getUserId().equals(DataManager.getInstance().getBasicCurUser().getUserId())) {
            return;  //不能自己为自己点赞
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        mEmoBmp = BitmapFactory.decodeResource(getResources(), Constants.EMO_ID_COLOR[mCurrentEmoID][0], options);
    }

    //点击post更多
    private void showPostDetail() {
        int flag;
        if (!mPostBean.getOwner().getUserName().equals(MemberShipManager.getInstance().getUserInfo().getUserName())) {
            flag = Constants.MORE_POPUPWINDOW_OTHERPOST;
        } else {
            flag = Constants.MORE_POPUPWINDOW_MYPOST;

        }
        MorePopupWindow popUpWindow = new MorePopupWindow((Activity) getContext(), new MorePopupWindow.MorePopupWindowClickListener() {
            @Override
            public void onThirdBtnClicked() {
                if (mPostBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserInfo().getUserId())) {
                    shareByPlatform(GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook);
                } else {
                    DataManager.getInstance().receivePostNotification(mPostBean.getOwner());
                    DataChangeManager.getInstance().notifyDataChange(0, mPostBean.getOwner(), 3);
                    DataChangeManager.getInstance().notifyDataChange(1, mPostBean, 3);
                }
            }

            @Override
            public void onSecondBtnClicked() {
                if (mPostBean.getOwner().getUserId()
                        .equals(MemberShipManager.getInstance().getUserInfo().getUserId())) {
                    mIsEditable = true;
                    showOrHideEditPostView();
                } else {
                    String content = ServerDataManager.getTextFromKey("mssg_block_confirmblock");
                    String cancel = ServerDataManager.getTextFromKey("pblc_btn_no");
                    String Okey = ServerDataManager.getTextFromKey("pblc_btn_yes");
                    mShowPostViewPagerFragment.showPublicDialog(null, content, cancel, Okey, blockDialog);
                }
            }

            @Override
            public void onFirstBtnClicked() {
                if (mPostBean.getOwner().getUserId()
                        .equals(MemberShipManager.getInstance().getUserInfo().getUserId())) {
                    String content = ServerDataManager.getTextFromKey("pub_txt_suretodelete");
                    String cancel = ServerDataManager.getTextFromKey("pblc_btn_no");
                    String Okey = ServerDataManager.getTextFromKey("pblc_btn_yes");
                    mShowPostViewPagerFragment.showPublicDialog(null, content, cancel, Okey, deleteDialog);
                } else {
                    DataManager.getInstance().blockPost(mPostBean);
                    DataChangeManager.getInstance().notifyDataChange(1, mPostBean, 0);
                }
            }

            @Override
            public void onFourthBtnClicked() {
                if (mPostBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserInfo().getUserId())) {
                    //    shareByPlatform(GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram);
                } else {
                    reportTypeWindow();
                }
            }

            @Override
            public void onCancelBtnClicked() {
                // TODO Auto-generated method stub
            }
        }, flag);
        popUpWindow.initView(mPostBean);
        popUpWindow.showAtLocation(this, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private String mShareVideoPath;

    private void shareByPlatform(final GBMemberShip_V2.MemberShipThirdPartyType platform) {
        if (!mIsPostObjectExist) {
            return;
        }
        if (platform == GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook) {
            if (!Utils.isAppAvilible(getContext(), "com.facebook.katana")) {
                Toast.makeText(getContext(), ServerDataManager.getTextFromKey("pblc_txt_nofb"), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mShareVideoPath = null;
        int objectType = mPostBean.getAttachDataType();
        if (objectType == Constants.POST_ATTACH_DATA_TYPE.VIDEO_TEXT.GetValue()) {
            final String videoPath = Preferences.getInstacne().getDownloadFilePathByName(mPostBean.getVideo());
            final File file = new File(videoPath);
            if (file.exists()) {
                final View exportingView = LayoutInflater.from(getContext()).inflate(R.layout.exporting_view, null);
                ((TextView) exportingView.findViewById(R.id.tvExporting)).setText(ServerDataManager.getTextFromKey("pblc_txt_exporting"));
                exportingView.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                final ExportingProgressView exportingProgressView = (ExportingProgressView) exportingView.findViewById(R.id.exportingProgressView);
                exportingProgressView.setProgress(0);
                addView(exportingView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        int progress = msg.what;
                        exportingProgressView.setProgress(progress);
                        if (progress == 100) {
                            removeView(exportingView);
                            File shareVideoFile = new File(mShareVideoPath);
                            if (!shareVideoFile.exists()) {
                                shareVideoFile = file;
                            }
                            if (platform == GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook) {
                                final Uri shareUri = Uri.fromFile(shareVideoFile);
                                GBExecutionPool.getExecutor().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        MemberShipManager.getInstance().getFacebook((Activity) getContext()).postVideoWithDialog(shareUri, new GBPlatform.GBUserActionHandler() {
                                            public void onSuccess(Object obj) {
                                                //                   Toast.makeText(getContext(), ServerDataManager.getTextFromKey("pblc_txt_sharedsuccessful"), Toast.LENGTH_SHORT).show();
                                            }

                                            public void onError(String strError) {

                                            }

                                            public void onTimeout() {

                                            }

                                            public void onCannel() {
                                            }
                                        });
                                    }
                                });

                            } else if (platform == GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram) {
                                MemberShipManager.getInstance().getInstagram(getContext()).postVideo(Uri.fromFile(shareVideoFile), "");
                            }
                        }
                    }
                };
                GBExecutionPool.getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        ExtractDecodeEditEncodeMuxTest test = new ExtractDecodeEditEncodeMuxTest();
                        test.setContext(getContext());
                        try {
                            mShareVideoPath = Utils.createYeemosVideoFile(getContext());
                            test.init(videoPath, mShareVideoPath, Constants.VIDEO_DEGREE_0, TextureRender.USE_FOR_ADD_WARTMARK, handler);
                            int[] size = test.getSize();
                            Bitmap bmp = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bmp);
                            Bitmap wartMark = BitmapFactory.decodeResource(getResources(), R.drawable.white_water_mark);
                            Bitmap emoBmp = ((BitmapDrawable) getResources().getDrawable(Constants.EMO_ID_COLOR[mCurrentEmoID][0])).getBitmap();
                            int padding = Utils.dip2px(getContext(), 7);
                            Rect src = new Rect(0, 0, emoBmp.getWidth(), emoBmp.getHeight());
                            if (platform == GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook) {
                                canvas.drawBitmap(wartMark, size[0] - wartMark.getWidth() - padding, size[1] - wartMark.getHeight() - padding, null);
                                canvas.drawBitmap(emoBmp, src, new RectF(padding, size[1] - emoBmp.getHeight() / 2, padding + emoBmp.getWidth() / 2, size[1]), null);
                            } else {
                                canvas.drawBitmap(wartMark, size[0] - wartMark.getWidth() - padding, (size[0] + size[1]) / 2 - wartMark.getHeight(), null);
                                canvas.drawBitmap(emoBmp, src, new RectF(padding, (size[0] + size[1]) / 2 - emoBmp.getHeight() / 2, padding + emoBmp.getWidth() / 2, (size[0] + size[1]) / 2), null);
                            }
                            DataManager.getInstance().setSelectObject(bmp);
                            wartMark.recycle();
                            test.testExtractDecodeEditEncodeMuxAudioVideo();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                            ((BaseActivity) getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    removeView(exportingView);
                                    mShareVideoPath = videoPath;
                                    Message msg = new Message();
                                    msg.what = 100;
                                    handler.sendMessage(msg);
                                }
                            });
                        }
                    }
                });
            }

        } else if (objectType == Constants.POST_ATTACH_DATA_TYPE.IMAGE_TEXT.GetValue()) {
            if (mShowPicImageView != null && mShowPicImageView.getDrawable() != null) {
                Bitmap bmpPic;
                if (mShowPicImageView.getDrawable() instanceof GlideBitmapDrawable) {
                    bmpPic = ((GlideBitmapDrawable) mShowPicImageView.getDrawable()).getBitmap();
                } else if (mShowPicImageView.getDrawable() instanceof BitmapDrawable) {
                    bmpPic = ((BitmapDrawable) mShowPicImageView.getDrawable()).getBitmap();
                } else {
                    return;
                }
                final Bitmap bmp = Bitmap.createBitmap(bmpPic.getWidth(), bmpPic.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);
                canvas.drawBitmap(bmpPic, 0, 0, null);
                int padding = Utils.dip2px(getContext(), 7);
                Bitmap wartMark = BitmapFactory.decodeResource(getResources(), R.drawable.white_water_mark);
                canvas.drawBitmap(wartMark, bmp.getWidth() - wartMark.getWidth() - padding, bmp.getHeight() - wartMark.getHeight() - padding, null);
                Bitmap emoBmp = ((BitmapDrawable) getResources().getDrawable(Constants.EMO_ID_COLOR[mCurrentEmoID][0])).getBitmap();
                canvas.drawBitmap(emoBmp, padding, bmp.getHeight() - emoBmp.getHeight() - padding, null);

                canvas.drawBitmap(emoBmp, new Rect(0, 0, emoBmp.getWidth(), emoBmp.getHeight()),
                        new RectF(padding, bmp.getHeight() - emoBmp.getHeight() / 2 - padding, padding + emoBmp.getWidth() / 2, bmp.getHeight() - padding), null);
                Utils.saveJpegWithPath(bmp, Utils.createYeemosPhotoFile(getContext()));
                wartMark.recycle();
                if (platform == GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook) {
                    GBExecutionPool.getExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            MemberShipManager.getInstance().getFacebook((Activity) getContext()).postPhotoWithDialog(bmp, "", new GBPlatform.GBUserActionHandler() {
                                public void onSuccess(Object obj) {
                                    bmp.recycle();
                                    //     Toast.makeText(getContext(), ServerDataManager.getTextFromKey("pblc_txt_sharedsuccessful"), Toast.LENGTH_SHORT).show();
                                }

                                public void onError(String strError) {
                                    bmp.recycle();
                                }

                                public void onTimeout() {
                                    bmp.recycle();
                                }

                                public void onCannel() {
                                    bmp.recycle();
                                }
                            });
                        }
                    });
                } else if (platform == GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram) {
                    MemberShipManager.getInstance().getInstagram(getContext()).postPhoto(bmp, "");
                }
            }
        }
    }

    Handler deleteDialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                case Constants.DIALOG_RIGHY_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    DataManager.getInstance().setDeletePost(mPostBean);
                    DataManager.getInstance().setCurPostBean(mPostBean);
                    DataManager.getInstance().delete(GBSConstants.MenuObjectType.Menu_Object_Post);
                    DataChangeManager.getInstance().notifyDataChange(1, mPostBean, 1);
                    //    NotifyCenter.sendBoardcastByDataUpdate(Constants.DELETE_POST);
                    mShowPostViewPagerFragment.removePost();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    Handler blockDialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                case Constants.DIALOG_RIGHY_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    DataManager.getInstance().setCurOtherUser(mPostBean.getOwner());
                    DataManager.getInstance().blockUser(mPostBean.getOwner());
                    mShowPostViewPagerFragment.removeAllPostByUser(mPostBean.getOwner().getUserId());
                    DataChangeManager.getInstance().notifyDataChange(0, mPostBean.getOwner(), 2);
                    Toast.makeText(getContext(), ServerDataManager.getTextFromKey("pblc_txt_blocksuccessful"), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void reportTypeWindow() {

        FiveBtnPopupWindow popUpWindow = new FiveBtnPopupWindow(
                (Activity) getContext(), new FiveBtnPopupWindow.FiveBtnPopupWindowClickListener() {
            @Override
            public void onFirstBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Porn);
            }

            @Override
            public void onSecondBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Scam);
            }

            @Override
            public void onThirdBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Abuse);
            }

            @Override
            public void onFourthBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_CommercialSpam);
            }

            @Override
            public void onFifthBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Offensive);
            }

            @Override
            public void onCancelBtnClicked() {
                // TODO Auto-generated method stub
            }
        });
        popUpWindow.initView();
        popUpWindow.showAtLocation(this, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void reportSubmit(GBSConstants.MenuOperateType opType) {
        DataManager.getInstance().setCurPostBean(mPostBean);
        DataManager.getInstance().setReportId(mPostBean.getId());
        DataChangeManager.getInstance().notifyDataChange(1, mPostBean, 0);
        if (!ConnectedUtil.isConnected(getContext())) {
            return;
        }
        DataManager.getInstance().report(GBSConstants.MenuObjectType.Menu_Object_Post, opType);
        String content = ServerDataManager.getTextFromKey("pblc_txt_reportsuccess");
        String okay = ServerDataManager.getTextFromKey("pub_btn_ok");
        mShowPostViewPagerFragment.showPublicDialog(null, content, okay, null, oneBtnDialoghandler);
    }

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

    protected Handler onPostDialoghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    DataChangeManager.getInstance().notifyDataChange(1, mPostBean, 0);
                    break;
                default:
                    break;
            }
        }
    };


    public boolean isGoBack() {
        if (mDrawCommentView != null) {
            if (mShowPostViewPagerFragment.getShowPostType() == ShowPostViewPagerFragment.SHOW_POST_BY_DRAWING_COMMENT_GCM) {
                return true;
            }
            mDrawCommentView.removeDrawCommentView(true);
            return false;
        }

        if (mIsEditable) {
            if (isPostEditViewVisibility()) {
                showView();
                return false;
            }
            mIsEditable = false;
            showOrHideEditPostView();
            return false;
        }
        return true;
    }

    /**
     * 根据是否处于编辑模式显示View
     */
    private void showOrHideEditPostView() {
        if (mIsEditable) {
            findViewById(R.id.bottomEditView).setVisibility(View.VISIBLE);
            findViewById(R.id.rlPostText).setVisibility(View.VISIBLE);
            findViewById(R.id.btnBack).setVisibility(View.VISIBLE);
            findViewById(R.id.btnText).setVisibility(View.VISIBLE);
            findViewById(R.id.rlBottom).setVisibility(View.GONE);
            findViewById(R.id.rightView).setVisibility(View.GONE);
            findViewById(R.id.topView).setVisibility(View.GONE);
            findViewById(R.id.rlText).setVisibility(View.GONE);
            findViewById(R.id.btnBack).setOnClickListener(this);
            findViewById(R.id.btnText).setOnClickListener(this);
            findViewById(R.id.btnEmo).setOnClickListener(this);
            findViewById(R.id.btnShare).setOnClickListener(this);
            findViewById(R.id.btnPrivacy).setOnClickListener(this);
            findViewById(R.id.btnAnonymous).setOnClickListener(this);
            findViewById(R.id.btnSetTime).setOnClickListener(this);
            findViewById(R.id.rlPostText).setOnClickListener(this);
            initEditTextPost();
            getEtPost().setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_LEFT:
                            case KeyEvent.KEYCODE_DPAD_RIGHT:
                                return true;
                        }
                    }
                    return false;
                }
            });
            ArrayList<TagBean> tagList = mPostBean.getTags();
            mEditSelectEmoID = 0;
            if (tagList != null && !tagList.isEmpty()) {
                mEditSelectEmoID = tagList.get(0).getId();
            } else {
                mEditSelectEmoID = 0;
            }
            resetEditViewByPostEmo(mEditSelectEmoID);
            LayoutParams lp = (LayoutParams) findViewById(R.id.rlText).getLayoutParams();
            lp.addRule(RelativeLayout.ABOVE, R.id.bottomEditView);
            mIsEditAnonymousOn = mPostBean.getIsAnonymity() == 1;
            mIsEditPostHrsOn = mPostBean.getExpiredType() == 1;
            mEditPrivateState = exChangeIndexAndPrivateState(mPostBean.getIsPrivate());
            mEditPostText = mPostBean.getText();
            showPostText(mEditPostText);
            getEtPost().setText(mEditPostText);
            ((ImageButton) findViewById(R.id.btnAnonymous)).
                    setImageResource(mIsEditAnonymousOn ? R.drawable.edit_anonymous : R.drawable.edit_not_anonymous);
            ((ImageButton) findViewById(R.id.btnSetTime)).
                    setImageResource(mIsEditPostHrsOn ? R.drawable.edit_24hrs : R.drawable.edit_unlimited);
            ((ImageButton) findViewById(R.id.btnPrivacy)).setImageResource(getResourceId(mEditPrivateState));
            mShowPostViewPagerFragment.setViewPagerCanScroll(false);
        } else {
            findViewById(R.id.bottomEditView).setVisibility(View.GONE);
            findViewById(R.id.rlPostText).setVisibility(View.GONE);
            findViewById(R.id.btnBack).setVisibility(View.GONE);
            findViewById(R.id.btnText).setVisibility(View.GONE);
            findViewById(R.id.rlBottom).setVisibility(View.VISIBLE);
            findViewById(R.id.topView).setVisibility(View.VISIBLE);
            findViewById(R.id.rlText).setVisibility(View.VISIBLE);
            findViewById(R.id.rightView).setVisibility(View.VISIBLE);
            LayoutParams lp = (LayoutParams) findViewById(R.id.rlText).getLayoutParams();
            lp.addRule(RelativeLayout.ABOVE, R.id.rlBottom);
            mShowPostViewPagerFragment.setViewPagerCanScroll(true);
            ImageButton btnPostPrivacy = (ImageButton) findViewById(R.id.btnPostPrivacy);
            btnPostPrivacy.setImageResource(getResourceId(exChangeIndexAndPrivateState(mPostBean.getIsPrivate())));
            if (mPostBean.getIsPrivate() == 3) {
                btnPostPrivacy.setOnClickListener(this);
            } else {
                btnPostPrivacy.setOnClickListener(null);
            }
            if (mPostBean.getExpiredType() == 1) {
                findViewById(R.id.roundProgressBar).setVisibility(VISIBLE);
                ((RoundProgressBar) findViewById(R.id.roundProgressBar))
                        .setProgress((int) Math.floor((Utils.getCurrentServerTime() - mPostBean.getCreateTime()) / (60 * 60 * 1000)));
            } else {
                findViewById(R.id.roundProgressBar).setVisibility(GONE);
            }
            ((TextView) findViewById(R.id.tvPostBeforeTime)).setText(Utils.getTime(mPostBean.getCreateTime()));
        }
    }

    public void initEditTextPost() {

        getEtPost().setAdapter(new MentionAdapter(BaseApplication.getAppContext()));
        getEtPost().setThreshold(1);
        getEtPost().addMentionTrigerkey('@');
        getEtPost().addMentionTrigerkey('#');

        getEtPost().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {//手机软键盘无法使用OnKeyListener监听Enter键
                    String text = s.toString();
                    if (s.toString().contains(String.valueOf((char) 10))) {
                        int index = getEtPost().getSelectionStart();
                        s.delete(index - 1, index);//将输入的回车键移除
                        showView();
                    } else if (text.length() > 500) {
                        int index = getEtPost().getSelectionStart();
                        s.delete(index - 1, index);//将输入的回车键移除
                    }
                }
            }
        });
    }

    private void showView() {
        mEditPostText = getEtPost().getText().toString();
        getPostEditView().setVisibility(View.GONE);
        findViewById(R.id.bottomEditView).setVisibility(View.VISIBLE);
        findViewById(R.id.rlPostText).setVisibility(View.VISIBLE);
        ((ImageView) findViewById(R.id.btnBack)).setImageDrawable(getResources().getDrawable(R.drawable.edit_back));
        showOrHideSoftKey(false);
        showPostText(mEditPostText);
    }

    protected void resetEditViewByPostEmo(int emoId) {
        ((ScaleImageButton) findViewById(R.id.btnEmo)).setImageResource(Constants.EMO_ID_COLOR[emoId][0]);
        View btnShare = findViewById(R.id.btnShare);
        GradientDrawable gd = (GradientDrawable) btnShare.getBackground();
        if (gd != null) {
            gd.setColor(Constants.EMO_ID_COLOR[emoId][1]);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mPostBean != null && mShowViewType == Constants.POST_ATTACH_DATA_TYPE.VIDEO_TEXT.GetValue()) {

            String videoPath = Preferences.getInstacne().getDownloadFilePathByName(mPostBean.getVideo());
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(videoPath);
                mMediaPlayer.setDisplay(holder);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setOnPreparedListener(this);
            try {
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setLooping(true);
            } catch (Exception e) {
                Log.e("ShowPostView", "media player prepare failed: "
                        + e.toString() + ", " + videoPath);
                if (e instanceof IllegalStateException) {
                    Utils.deleteFile(videoPath);
                }
                releaseMediaPlayer();
                return;
            }
            mShowPostViewPagerFragment.getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mIsVideoPlaying = false;
            if (mMediaPlayer != null) {
                resetCountDownView();
                new Thread() {
                    @Override
                    public void run() {
                        while (mMediaPlayer != null && mSurfaceView != null) {
                            mHandler.sendEmptyMessage(0);
                            try {
                                sleep(5);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        }
    }

    private void resetCountDownView() {
        findViewById(R.id.rlPlayMovieCountdownView).setVisibility(View.VISIBLE);
        CountDownView countDownView = (CountDownView) findViewById(R.id.playMovieCountdownView);
        countDownView.setProgress(100, 0);
        TextView tv = (TextView) findViewById(R.id.tvTotalTime);
        tv.setText(String.valueOf(mPostBean.getVideoPlayLength()));
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    //更新进度
                    if (mMediaPlayer != null && mIsVideoPlaying) {
                        TextView tv = (TextView) findViewById(R.id.tvTotalTime);
                        CountDownView countDownView = (CountDownView) findViewById(R.id.playMovieCountdownView);
                        long currentPosition = mMediaPlayer.getCurrentPosition();//(System.currentTimeMillis() - mUserHandleBeforeTime);
                        long duration = mMediaPlayer.getDuration();
                        int total = (int) ((mMediaPlayer.getDuration() + 500) * 0.001);
                        if (total < 0) {
                            total = (int) mPostBean.getVideoPlayLength();
                        }
                        int current = (int) Math.ceil((duration - currentPosition) * 0.001f);
                        if (current > total || current < 0) {
                            current = 0;
                        }
                        if (current == 0) {
                            tv.setText(String.valueOf(mPostBean.getVideoPlayLength()));
                            countDownView.setProgress(duration, 0);
                        } else {
                            tv.setText(String.valueOf(current));
                            countDownView.setProgress(duration, currentPosition);
                        }
                        if (currentPosition >= 150) {
                            if (mThumbImageView != null) {
                                mThumbImageView.setVisibility(View.GONE);
                            }
                            if (mShowPicImageView != null && mShowPicImageView.getVisibility() == View.VISIBLE) {
                                mShowPicImageView.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mMediaPlayer != null && mSurfaceView != null) {
            mSurfaceView.getHolder().setFixedSize(mSurfaceView.getWidth(), mSurfaceView.getHeight());
            mMediaPlayer.start();
            mIsVideoPlaying = true;
            findViewById(R.id.fileLoadBar).setVisibility(View.GONE);
        }
    }
}
