package com.yeemos.app.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.mentiontextview.MentionAdapter;
import com.common.mentiontextview.MentionTextView;
import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.ImageCommentBean;
import com.gbsocial.BeansBase.ImgCommentResponseBean;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.BaseUtils;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.adapter.EmoAdapter;
import com.yeemos.app.fragment.BaseFragment;
import com.yeemos.app.fragment.EditPostFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.yeemos.jni.ShaderJNILib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gigabud on 17-1-17.
 */

public class DrawCommentView extends RelativeLayout implements View.OnClickListener {

    private Bitmap mCommentsBmp;
    private int mLastDrawColor = -1, mLastTextColor = Color.WHITE;
    private BaseFragment mBaseFragment;
    private ShowPostView mShowPostView;
    private WarterMarkView mWarterMarkView;
    private boolean mIsWartmarkShowCenter;
    protected boolean mIsWarterMarkText = true;
    private String mPostText, mWaterMarkText;
    private static final int CREATE_IMAGE_COMMENT = 1;//1创建绘画评论 2 回复某人的评论
    private static final int CREATE_REPLY_IMAGE_COMMENT = 2;
    private ArrayList<View> mEmoViewList;
    private boolean isAnimation = false;
    private String replyID;
    private InputMethodManager mInputMethodManager;

    private ArrayList<ImageComment> mImageCommentList;

    public static final int TYPE_COMMON_PEN = 0;
    public static final int TYPE_GLOW_PEN = 1;
    private int mCurrentPenType = TYPE_COMMON_PEN;

    public static final int TYPE_PEN_WIDTH_4 = 0;
    public static final int TYPE_PEN_WIDTH_10 = 1;
    public static final int TYPE_PEN_WIDTH_16 = 2;
    public static final int TYPE_PEN_WIDTH_22 = 3;
    //    private int mCurrentCommonPenWidthType = TYPE_PEN_WIDTH_12;
//    private int mCurrentGlowPenWidthType = TYPE_PEN_WIDTH_12;
    private int mCurrentPenWidthType = TYPE_PEN_WIDTH_4;

    public static final int USE_IN_EDIT_POST = 0;
    public static final int USE_IN_SHOW_POST = 1;

    private int mCurrentUseIn = USE_IN_EDIT_POST;


    public static class ImageComment {
        public long userId;
        public long imgCommentCreateTime;
        public boolean isHide = false;
        public ArrayList<DrawView.DrawLineInfo> commentDrawPath;
        public ArrayList<StickerView.StickerBean> strickers;
    }

    public DrawCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_draw_comment, this);
        findViewById(R.id.btnClose).setOnClickListener(this);
        findViewById(R.id.btnRemove).setOnClickListener(this);
        findViewById(R.id.btnRemove).setVisibility(View.GONE);
        findViewById(R.id.tvPaint).setOnClickListener(this);
        findViewById(R.id.topView).bringToFront();
        findViewById(R.id.btnPen).setOnClickListener(this);
        findViewById(R.id.btnGlowPen).setOnClickListener(this);
        findViewById(R.id.btnWartMark).setOnClickListener(this);
        findViewById(R.id.btnSticker).setOnClickListener(this);
        findViewById(R.id.warterMarkParentView).setOnClickListener(this);
        findViewById(R.id.btnGlowPen).setVisibility(View.GONE);
        findViewById(R.id.llSelectPenWidth).setVisibility(View.GONE);
        findViewById(R.id.btnGlowPen).setVisibility(View.GONE);
        findViewById(R.id.llSelectPenWidth).setVisibility(View.GONE);
        findViewById(R.id.tvSubmit).setVisibility(View.GONE);

        final DrawView drawView = (DrawView) findViewById(R.id.drawView);
        drawView.setEnable(false);
        drawView.setDrawType(DrawView.TYPE_DRAW_COMMENT);
        findViewById(R.id.ivColor).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        Bitmap bmp = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                        int x = bmp.getWidth() / 2;
                        int y = (int) (event.getY() * bmp.getHeight() / v.getHeight() + 0.5f);
                        if (x >= bmp.getWidth()) {
                            x = bmp.getWidth() - 1;
                        } else if (x < 0) {
                            x = 0;
                        }
                        if (y >= bmp.getHeight()) {
                            y = bmp.getHeight() - 1;
                        } else if (y < 0) {
                            y = 0;
                        }
                        int color = bmp.getPixel(x, y);
                        if (color != Color.TRANSPARENT) {
                            drawView.resetPaintColor(color);
                            mLastDrawColor = color;
                            if (mCurrentPenType == TYPE_GLOW_PEN) {
                                GradientDrawable bgShape = (GradientDrawable) findViewById(R.id.btnGlowPenBg).getBackground();
                                bgShape.setColor(color);
                            } else {
                                GradientDrawable bgShape = (GradientDrawable) findViewById(R.id.btnPenBg).getBackground();
                                bgShape.setColor(color);
                            }
                        }
                        break;
                }
                return true;
            }
        });
        findViewById(R.id.ivTextColor).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        Bitmap bmp = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                        int x = bmp.getWidth() / 2;
                        int y = (int) (event.getY() * bmp.getHeight() / v.getHeight() + 0.5f);
                        if (x >= bmp.getWidth()) {
                            x = bmp.getWidth() - 1;
                        } else if (x < 0) {
                            x = 0;
                        }
                        if (y >= bmp.getHeight()) {
                            y = bmp.getHeight() - 1;
                        } else if (y < 0) {
                            y = 0;
                        }
                        int color = bmp.getPixel(x, y);
                        if (color != Color.TRANSPARENT) {
                            mLastTextColor = color;
                            getEtPost().setTextColor(color);
                        }
                        break;
                }
                return true;
            }
        });
        drawView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (drawView.isEnable()) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            showOrHideWithAnim(findViewById(R.id.topView), false);
                            showOrHideWithAnim(getBottomView(), false);
                            if (mCurrentUseIn == USE_IN_EDIT_POST) {
                                showOrHideWithAnim(((EditPostFragment) mBaseFragment).getPostText(), false);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            showOrHideWithAnim(findViewById(R.id.topView), true);
                            showOrHideWithAnim(getBottomView(), true);
                            if (mCurrentUseIn == USE_IN_SHOW_POST) {
                                findViewById(R.id.tvSubmit).setVisibility((drawView.isDrawed() || isCanSubmit()) ? View.VISIBLE : View.GONE);
                            } else {
                                showOrHideWithAnim(((EditPostFragment) mBaseFragment).getPostText(), true);
                            }
                            findViewById(R.id.btnRemove).setVisibility(drawView.isDrawed() ? View.VISIBLE : View.GONE);
                            break;
                    }
                }
                return false;
            }
        });

        initEditTextPost();
        onEditKeyListener(getEtPost());
    }

    private void onEditKeyListener(EditText et) {
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            return true;
                    }
                }
                return false;
            }
        });
    }

    public DrawCommentView(Context context) {
        this(context, null);
    }

    public void setCurrentUseIn(int currentUseIn, BaseFragment fragment, ShowPostView showPostView) {
        mCurrentUseIn = currentUseIn;
        mBaseFragment = fragment;
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        initSelectPenWidthView((SelectPenWidthView) findViewById(R.id.penWidth_8), TYPE_PEN_WIDTH_4, drawView.getStrokeWidth(TYPE_PEN_WIDTH_4) + Utils.dip2px(getContext(), 2));
        initSelectPenWidthView((SelectPenWidthView) findViewById(R.id.penWidth_12), TYPE_PEN_WIDTH_10, drawView.getStrokeWidth(TYPE_PEN_WIDTH_10));
        initSelectPenWidthView((SelectPenWidthView) findViewById(R.id.penWidth_16), TYPE_PEN_WIDTH_16, drawView.getStrokeWidth(TYPE_PEN_WIDTH_16));
        initSelectPenWidthView((SelectPenWidthView) findViewById(R.id.penWidth_20), TYPE_PEN_WIDTH_22, drawView.getStrokeWidth(TYPE_PEN_WIDTH_22));
        if (mCurrentUseIn == USE_IN_EDIT_POST) {
            findViewById(R.id.btnText).setVisibility(View.VISIBLE);
            findViewById(R.id.btnText).setOnClickListener(this);
            findViewById(R.id.topView).bringToFront();
            findViewById(R.id.bottomView).setVisibility(View.GONE);
            mShowPostView = null;
        } else {
            final TextView tvSubmit = ((TextView) findViewById(R.id.tvSubmit));
            setDrawCommentNum();
            findViewById(R.id.btnText).setVisibility(View.GONE);
            findViewById(R.id.bottomView).setVisibility(View.VISIBLE);
            setOnClickListener(this);
            mShowPostView = showPostView;
            View view = findViewById(R.id.btnPen);
            BasicUser postOwer = mShowPostView.getPostBean().getOwner();
            if (postOwer.getUserId().equals(MemberShipManager.getInstance().getUserID())
                    || (postOwer.getFollowedStatus() == 1 && postOwer.getFollowStatus() == 1)) {
                tvSubmit.setOnClickListener(this);
                tvSubmit.setText(ServerDataManager.getTextFromKey("drwngcmmnt_btn_submit"));
                view.setOnClickListener(this);
                if (!Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_IN_DRAW_COMMENT_VIEW)) {
                    showTourialView();
                    Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_DRAW_COMMENT_VIEW, true);
                }
            } else {
                tvSubmit.setText(ServerDataManager.getTextFromKey("drwngcmmnt_txt_doodleforfriend"));
                view.setVisibility(View.GONE);
                findViewById(R.id.btnWartMark).setVisibility(View.GONE);
                findViewById(R.id.btnSticker).setVisibility(View.GONE);
            }
            getImageCommentData();
        }
    }

    private int[] getInScreen(View v) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        return location;
    }

    private void showTourialView() {
        final View rlTourialView = ((Activity) getContext()).findViewById(R.id.rlTourial);
        rlTourialView.postDelayed(new Runnable() {
            @Override
            public void run() {
                rlTourialView.setVisibility(View.VISIBLE);
                View tourialView6 = rlTourialView.findViewById(R.id.tourialView6);
                tourialView6.setVisibility(View.VISIBLE);
                View view1 = findViewById(R.id.btnPen);
                int[] location1 = getInScreen(view1);

                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView6.findViewById(R.id.triangleView6).getLayoutParams();
                final int screenWidth = ((BaseActivity) getContext()).getDisplaymetrics().widthPixels;
                lp.topMargin = location1[1] + view1.getHeight();
                lp.rightMargin = screenWidth - location1[0] - (view1.getWidth() + Utils.dip2px(getContext(), 15)) / 2;
                rlTourialView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View tourialView6 = v.findViewById(R.id.tourialView6);
                        View tourialView7 = v.findViewById(R.id.tourialView7);
                        if (tourialView6.getVisibility() == View.VISIBLE) {
                            tourialView6.setVisibility(View.GONE);
                            tourialView7.setVisibility(View.VISIBLE);
                            View view2 = findViewById(R.id.tvPaint);
                            int[] location1 = getInScreen(view2);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView7.getLayoutParams();
                            lp.topMargin = location1[1] - Utils.dip2px(getContext(), 75);
                            lp = (RelativeLayout.LayoutParams) tourialView7.findViewById(R.id.triangleView7).getLayoutParams();
                            lp.leftMargin = location1[0] + (view2.getWidth() - Utils.dip2px(getContext(), 15)) / 2;
                        } else {
                            tourialView7.setVisibility(View.GONE);
                            v.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }, 500);

    }

    private void initSelectPenWidthView(SelectPenWidthView selectPenWidthView, int penWidthType, int penWidth) {
        selectPenWidthView.setOnClickListener(this);
        selectPenWidthView.setTag(penWidthType);
        selectPenWidthView.setRadius(penWidth);

    }

    private void setDrawCommentNum() {
        if (mShowPostView != null) {
            ((TextView) findViewById(R.id.tvPaint)).setText(String.format(ServerDataManager.getTextFromKey("drwngcmmnt_btn_doodle"),
                    String.valueOf(mShowPostView.getPostBean().getImgCommentNum())));
        }
    }

    private void getImageCommentData() {
        final BaseActivity baseActivity = (BaseActivity) getContext();
        baseActivity.showLoadingDialog("", null, true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                JSONObject jb = new JSONObject();
                try {
                    jb.put("token", MemberShipManager.getInstance().getToken());
                    jb.put("id", mShowPostView.getPostBean().getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final ServerResultBean<ImgCommentResponseBean> result = DataManager.getInstance().getImageComment(jb.toString());
                baseActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        baseActivity.hideLoadingDialog();
                    }
                });
                if (result.isSuccess()) {
                    mImageCommentList = new ArrayList<>();
                    ImageComment imageComment;
                    ImgCommentResponseBean bean = result.getData();
                    if (bean != null && bean.getImgComment() != null) {
                        ArrayList<ImageCommentBean> list = bean.getImgComment();
                        for (ImageCommentBean imgComment : list) {
                            imageComment = new ImageComment();
                            imageComment.userId = imgComment.getUserId();
                            imageComment.commentDrawPath = new ArrayList<>();
                            imageComment.strickers = new ArrayList<>();
                            imageComment.isHide = (imgComment.getIsHide() == 1);
                            mImageCommentList.add(imageComment);
                            initImageComments(baseActivity, imgComment.getContent(), imageComment);
                        }
                    }
                    Collections.sort(mImageCommentList, new SortByCreateTime());
                    baseActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addComments();
                            mShowPostView.resetDrawCommentNum(getDrawUserNum());
                            setDrawCommentNum();
                        }
                    });

                }
            }
        });
    }

    private void addComments() {
        if (mImageCommentList == null || mImageCommentList.isEmpty()) {
            return;
        }
        for (ImageComment imageComment : mImageCommentList) {
            addCommentToDrawCommentViews(imageComment);
        }
        drawCommentsToBmp();
    }

    private void drawCommentsToBmp() {
        final RelativeLayout drawCommentViews = (RelativeLayout) findViewById(R.id.drawCommentViews);
        drawCommentViews.setVisibility(View.VISIBLE);
        final ImageView ivCommentsView = (ImageView) findViewById(R.id.ivCommentsView);
        ivCommentsView.setVisibility(View.GONE);
        if (drawCommentViews.getChildCount() > 0) {
            drawCommentViews.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivCommentsView.setImageBitmap(null);
                    ivCommentsView.setVisibility(View.VISIBLE);
                    if (mCommentsBmp != null && !mCommentsBmp.isRecycled()) {
                        mCommentsBmp.recycle();
                    }
                    mCommentsBmp = Bitmap.createBitmap(drawCommentViews.getWidth(), drawCommentViews.getHeight(), Bitmap.Config.ARGB_8888);
                    drawCommentViews.draw(new Canvas(mCommentsBmp));
                    ivCommentsView.setImageBitmap(mCommentsBmp);
                    drawCommentViews.setVisibility(View.GONE);
                }
            }, 300);
        } else {
            ivCommentsView.setImageBitmap(null);
            drawCommentViews.setVisibility(View.GONE);
            if (mCommentsBmp != null && !mCommentsBmp.isRecycled()) {
                mCommentsBmp.recycle();
            }
            mCommentsBmp = null;
        }
    }

    public void addCommentToDrawCommentViews(ImageComment imageComment) {
        RelativeLayout drawCommentViews = (RelativeLayout) findViewById(R.id.drawCommentViews);
        drawCommentViews.setVisibility(View.VISIBLE);
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_draw_comment_view, null);
        drawCommentViews.addView(itemView);
        itemView.setTag(imageComment.userId);
        itemView.setVisibility(imageComment.isHide ? View.GONE : View.VISIBLE);
        DrawView drawView = (DrawView) itemView.findViewById(R.id.itemDrawView);
        drawView.setCommentDrawPath(imageComment.commentDrawPath);

        ArrayList<StickerView.StickerBean> stickers;
        stickers = imageComment.strickers;
        if (stickers == null || stickers.isEmpty()) {
            return;
        }

        for (StickerView.StickerBean stickerBean : stickers) {
            if (stickerBean.type == StickerView.EMO_STICKER) {
                StickerView stickerView = new StickerView(getContext());
                if (TextUtils.isEmpty(stickerBean.stickerName)) {
                    continue;
                }
                Bitmap emoBmp;
                if (stickerBean.stickerName.startsWith("svg")) {
                    emoBmp = getBitmapFromVectorDrawable(Utils.getDrawableIdByName(stickerBean.stickerName));
                } else {
                    emoBmp = BitmapFactory.decodeResource(getResources(), Utils.getDrawableIdByName(stickerBean.stickerName));
                }
                if (stickerBean.initWidth != 0) {
                    stickerBean.scale = stickerBean.initWidth * stickerBean.scale / emoBmp.getWidth();
                }
                stickerView.setFixWarterMark(emoBmp, stickerBean.centerX, stickerBean.centerY, stickerBean.rotationDegree, stickerBean.scale);
                ((RelativeLayout) itemView.findViewById(R.id.itemStrickerView)).addView(stickerView);
                stickerView.setTag(StickerView.EMO_STICKER);
                stickerView.setTag(R.id.tag, String.valueOf(imageComment.userId));
            } else if (stickerBean.type == StickerView.TEXT_WARTERMARK) {
                WarterMarkView warterMarkView = new WarterMarkView(getContext());
                warterMarkView.setFixPostText(stickerBean);
                ((RelativeLayout) itemView.findViewById(R.id.itemWarterMarkView)).addView(warterMarkView);
                warterMarkView.setTag(StickerView.TEXT_WARTERMARK);
                warterMarkView.setTag(R.id.tag, String.valueOf(imageComment.userId));
            }
        }
    }

    class SortByCreateTime implements Comparator {
        public int compare(Object o1, Object o2) {
            long createTime1 = ((DrawCommentView.ImageComment) o1).imgCommentCreateTime;
            long createTime2 = ((DrawCommentView.ImageComment) o2).imgCommentCreateTime;
            if (createTime1 > createTime2) {
                return 1;
            } else if (createTime1 == createTime2) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    /**
     * 获取绘画评论的人数
     *
     * @return
     */

    public int getDrawUserNum() {
        int num = 0;
        if (mImageCommentList != null) {
            ArrayList<Long> userIds = new ArrayList<>();
            for (DrawCommentView.ImageComment bean : mImageCommentList) {
                if (!userIds.contains(bean.userId)) {
                    userIds.add(bean.userId);
                }
            }
            num = userIds.size();
        }
        return num;
    }

    private void initImageComments(BaseActivity baseActivity, String data, ImageComment imgComment) {
        if (!TextUtils.isEmpty(data)) {
            try {
                JSONObject jb = new JSONObject(data);
                imgComment.imgCommentCreateTime = jb.optLong("createTime");
                int screenWidth = jb.optInt("width");
                int screenHeight = jb.optInt("height");
                int imageWidth = jb.optInt("imageWidth");
                int imageHeight = jb.optInt("imageHeight");
                int contentMode = jb.optInt("contentMode");   //填充方式,目前只用0

                int orginWidth;
                int orginHeight;

                if (imageWidth != 0 && imageHeight != 0) {
                    float ratio1 = screenWidth * 1.0f / screenHeight;
                    float ratio2 = imageWidth * 1.0f / imageHeight;
                    if (ratio1 > ratio2) {
                        orginWidth = (int) (screenHeight * ratio2 + 0.5f);
                        orginHeight = screenHeight;
                    } else {
                        orginWidth = screenWidth;
                        orginHeight = (int) (screenWidth / ratio2 + 0.5f);
                    }
                } else {
                    orginWidth = screenWidth;
                    orginHeight = screenHeight;
                }

                int currentWidth = this.getWidth();
                int currentHeight = this.getHeight();
                ShaderJNILib.setOrginCurrentScreen(orginWidth, orginHeight, currentWidth, currentHeight);
                float widthRatio = (float) currentWidth / orginWidth;
                float heightRatio = (float) currentHeight / orginHeight;
                JSONArray ja = jb.optJSONArray("paths");
                if (ja != null && ja.length() > 0) {
                    DrawView.DrawLineInfo drawLineInfo;
                    int length = ja.length();
                    PointF previous = null, start = null, end = null;
                    JSONObject jb2;
                    JSONArray ja2, ja3;
                    for (int i = 0; i < length; ++i) {
                        drawLineInfo = new DrawView.DrawLineInfo();
                        drawLineInfo.drawPaths = new ArrayList<>();
                        imgComment.commentDrawPath.add(drawLineInfo);
                        jb2 = ja.getJSONObject(i);
                        drawLineInfo.color = jb2.getInt("color");
                        drawLineInfo.penType = jb2.optInt("penType");
                        drawLineInfo.penWidthType = jb2.optInt("penWidthType");
                        ja2 = jb2.getJSONArray("points");
                        int length2 = ja2.length();
                        for (int j = 0; j < length2; ++j) {
                            if (j == 0) {
                                previous = null;
                            }
                            ja3 = ja2.getJSONArray(j);
                            int a = ja3.getInt(0);
                            int b = ja3.getInt(1);
                            end = new PointF(ShaderJNILib.convertPointX(ja3.getInt(0)), ShaderJNILib.convertPointY(ja3.getInt(1)));
                            drawLineInfo.drawPaths.add(pointToDrawPath(previous, start, end));
                            start = previous;
                            previous = end;
                            if (start == null) {
                                start = previous;
                            }
                        }
                    }
                }
                ja = jb.optJSONArray("stickers");
                if (ja != null && ja.length() > 0) {
                    int length = ja.length();
                    StickerView.StickerBean bean;
                    JSONObject jb2;
                    for (int i = 0; i < length; ++i) {
                        bean = new StickerView.StickerBean();
                        jb2 = ja.getJSONObject(i);
                        bean.stickerName = jb2.optString("s_name");
                        if (TextUtils.isEmpty(bean.stickerName)) {
                            continue;
                        }
                        bean.centerX = ShaderJNILib.convertPointX(jb2.optInt("x"));
                        bean.centerY = ShaderJNILib.convertPointY(jb2.optInt("y"));
                        bean.initWidth = jb2.optInt("w");
                        bean.initHeight = jb2.optInt("h");
                        bean.rotationDegree = (float) jb2.optDouble("rotation");
                        bean.scale = (float) jb2.optDouble("scale") * widthRatio;
                        bean.stickerIndex = jb2.optInt("index");

                        bean.type = StickerView.EMO_STICKER;
                        imgComment.strickers.add(bean);
                    }
                }

                ja = jb.optJSONArray("text");
                if (ja != null && ja.length() > 0) {
                    int length = ja.length();
                    StickerView.StickerBean bean;
                    JSONObject jb2;
                    for (int i = 0; i < length; ++i) {
                        bean = new StickerView.StickerBean();
                        jb2 = ja.getJSONObject(i);
                        bean.text = jb2.optString("text");
                        if (TextUtils.isEmpty(bean.text)) {
                            continue;
                        }
                        bean.centerX = ShaderJNILib.convertPointX(jb2.optInt("x"));
                        bean.centerY = ShaderJNILib.convertPointY(jb2.optInt("y"));
                        bean.initWidth = ShaderJNILib.convertPointX(jb2.optInt("w"));
                        bean.initHeight = ShaderJNILib.convertPointY(jb2.optInt("h"));
                        bean.rotationDegree = (float) jb2.optDouble("rotation");
                        bean.scale = ((float) jb2.optDouble("scale"));
                        bean.color = jb2.optInt("color");
                        bean.widthRatio = widthRatio;
                        bean.heightRatio = heightRatio;
                        bean.isTextCenter = jb2.optInt("isCenter") == 1;
                        bean.type = StickerView.TEXT_WARTERMARK;
                        imgComment.strickers.add(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private DrawView.DrawPath pointToDrawPath(PointF previous, PointF start, PointF end) {
        DrawView.DrawPath dp = new DrawView.DrawPath();
        dp.point = new Point((int) (end.x + 0.5), (int) (end.y + 0.5));
        if (previous == null) {
            dp.isDrawPoint = true;
        } else {
            PointF mid1 = midPoint(previous, start);
            PointF mid2 = midPoint(end, previous);
            Path path = new Path();
            path.reset();
            path.moveTo(mid1.x, mid1.y);
            path.quadTo(previous.x, previous.y, mid2.x, mid2.y);
            dp.path = path;
            dp.isDrawPoint = false;
        }
        return dp;
    }

    private PointF midPoint(PointF p1, PointF p2) {
        return new PointF((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f);
    }

    /**
     * 获取绘画评论
     *
     * @return
     */
    private String getImageCommentContent() {

        //      BaseActivity baseActivity = (BaseActivity) getContext();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("width", ((BaseActivity) getContext()).getDisplaymetrics().widthPixels);
            jsonObject.put("height", ((BaseActivity) getContext()).getDisplaymetrics().heightPixels);
            jsonObject.put("imageWidth", this.getWidth());
            jsonObject.put("imageHeight", this.getHeight());
            jsonObject.put("createTime", Utils.getCurrentServerTime());
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("paths", jsonArray);
            DrawView drawView = (DrawView) findViewById(R.id.drawView);
            ArrayList<DrawView.DrawLineInfo> drawPathList = drawView.getDrawPathsList();
            if (drawPathList != null && !drawPathList.isEmpty()) {
                JSONObject jb1;
                JSONArray ja1, ja2;
                for (DrawView.DrawLineInfo drawLineInfo : drawPathList) {
                    if (drawView.isDrawLineInfoInvalid(drawLineInfo)) {
                        continue;
                    }
                    jb1 = new JSONObject();
                    ja1 = new JSONArray();
                    jb1.put("points", ja1);
                    jb1.put("color", drawLineInfo.color);
                    jb1.put("penType", drawLineInfo.penType);
                    jb1.put("penWidthType", drawLineInfo.penWidthType);
                    jsonArray.put(jb1);
                    for (DrawView.DrawPath dp : drawLineInfo.drawPaths) {
                        ja2 = new JSONArray();
                        ja2.put(dp.point.x);
                        ja2.put(dp.point.y);
                        ja1.put(ja2);
                    }
                }
            }
            WarterMarkParentView strickerParentView = (WarterMarkParentView) findViewById(R.id.strickerParentView);
            int childCount = strickerParentView.getChildCount();
            jsonArray = new JSONArray();
            jsonObject.put("stickers", jsonArray);
            StickerView stickerView;
            int tag;
            if (childCount > 0) {
                for (int i = 0; i < childCount; ++i) {
                    stickerView = (StickerView) strickerParentView.getChildAt(i);
                    tag = (int) stickerView.getTag();
                    if (!stickerView.isFix() && tag == StickerView.EMO_STICKER) {
                        jsonArray.put(stickerView.toJSONObject());
                    }
                }
            }
            WarterMarkParentView warterMarkParentView = (WarterMarkParentView) findViewById(R.id.warterMarkParentView);
            childCount = warterMarkParentView.getChildCount();
            jsonArray = new JSONArray();
            jsonObject.put("text", jsonArray);
            if (childCount > 0) {
                for (int i = 0; i < childCount; ++i) {
                    stickerView = (StickerView) warterMarkParentView.getChildAt(i);
                    tag = (int) stickerView.getTag();
                    if (!stickerView.isFix() && tag == StickerView.TEXT_WARTERMARK) {
                        jsonArray.put(stickerView.toJSONObject());
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 创建绘画评论
     *
     * @param type
     * @param replyUserId
     */
    public void createImageCommentData(final int type, final long replyUserId) {
        final BaseActivity baseActivity = (BaseActivity) getContext();
        baseActivity.showLoadingDialog("", null, true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("token", MemberShipManager.getInstance().getToken());
                    jsonObject.put("id", BaseUtils.getUUID() + System.currentTimeMillis() + "_id");
                    jsonObject.put("parentID", mShowPostView.getPostBean().getId());
                    jsonObject.put("type", type);
                    if (type == CREATE_REPLY_IMAGE_COMMENT) {  //1创建绘画评论 2 回复某人的评论
                        jsonObject.put("replyUserId", replyUserId);
                    }
                    jsonObject.put("content", getImageCommentContent());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final ServerResultBean<String> result = DataManager.getInstance().createImageComment(jsonObject.toString());
                baseActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        baseActivity.hideLoadingDialog();
                        if (result.isSuccess()) {
                            Toast.makeText(getContext(), ServerDataManager.getTextFromKey("drwngcmmnt_txt_submitsuccess"), Toast.LENGTH_LONG).show();
                            findViewById(R.id.tvSubmit).setVisibility(View.GONE);
                            findViewById(R.id.btnRemove).setVisibility(View.GONE);
                            getEtPost().setText("");
                            mWaterMarkText = "";
                            addCurrentComment();
//                            if (findViewById(R.id.rlDrawColor).getVisibility() == View.VISIBLE) {
//                                findViewById(R.id.btnPen).performClick();
//                            }
                        } else {
                            Toast.makeText(getContext(), ServerDataManager.getTextFromKey("drwngcmmnt_txt_submitfail"), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * 提交成功后将当前评论转为固定不可修改的评论
     */
    private void addCurrentComment() {
        ImageComment bean = new ImageComment();
        bean.userId = Long.parseLong(MemberShipManager.getInstance().getUserID());
        bean.imgCommentCreateTime = Utils.getCurrentServerTime();
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        bean.commentDrawPath = drawView.addDrawPathToComment();
        bean.strickers = new ArrayList<>();
        if (mImageCommentList == null) {
            mImageCommentList = new ArrayList<>();
        }
        mImageCommentList.add(bean);


        WarterMarkParentView strickerParentView = (WarterMarkParentView) findViewById(R.id.strickerParentView);
        int childCount = strickerParentView.getChildCount();
        StickerView stickerView;
        for (int i = 0; i < childCount; ++i) {
            stickerView = (StickerView) strickerParentView.getChildAt(i);
            stickerView.setFocusable(false);
            if (!stickerView.isFix()) {
                bean.strickers.add(stickerView.toStickerBean());
                stickerView.setFix(true);
            }
        }
        strickerParentView.removeAllViews();

        WarterMarkParentView warterMarkParentView = (WarterMarkParentView) findViewById(R.id.warterMarkParentView);
        childCount = warterMarkParentView.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            stickerView = (StickerView) warterMarkParentView.getChildAt(i);
            stickerView.setFocusable(false);
            if (!stickerView.isFix()) {
                bean.strickers.add(stickerView.toStickerBean());
                stickerView.setFix(true);
            }
        }
        warterMarkParentView.removeAllViews();
        addCommentToDrawCommentViews(bean);

        drawCommentsToBmp();

        mShowPostView.resetDrawCommentNum(getDrawUserNum());
        setDrawCommentNum();
        replyID = null;
    }

    /**
     * 根据userId，显示隐藏相关的评论
     *
     * @param userId
     * @param isHide
     */
    public void showOrHideImageCommentByUserId(long userId, boolean isHide) {
        boolean isHadUser = false;
        if (mImageCommentList != null) {
            for (DrawCommentView.ImageComment bean : mImageCommentList) {
                if (bean.userId == userId) {
                    bean.isHide = isHide;
                    isHadUser = true;
                }
            }
        }
        if (isHadUser) {
            findViewById(R.id.ivCommentsView).setVisibility(View.GONE);
            RelativeLayout drawCommentViews = (RelativeLayout) findViewById(R.id.drawCommentViews);
            drawCommentViews.setVisibility(View.VISIBLE);
            int childCount = drawCommentViews.getChildCount();
            long tagUserId;
            View childView;
            for (int i = 0; i < childCount; ++i) {
                childView = drawCommentViews.getChildAt(i);
                tagUserId = (long) childView.getTag();
                if (tagUserId == userId) {
                    childView.setVisibility(isHide ? View.GONE : View.VISIBLE);
                }
            }
            drawCommentsToBmp();
        }
    }

    /**
     * 显示隐藏所有的评论
     *
     * @param isHide
     */
    public void showOrHideAllImageComment(boolean isHide) {
        if (mImageCommentList != null && !mImageCommentList.isEmpty()) {
            for (DrawCommentView.ImageComment bean : mImageCommentList) {
                bean.isHide = isHide;
            }
            findViewById(R.id.ivCommentsView).setVisibility(View.GONE);
            RelativeLayout drawCommentViews = (RelativeLayout) findViewById(R.id.drawCommentViews);
            drawCommentViews.setVisibility(View.VISIBLE);
            int childCount = drawCommentViews.getChildCount();
            View childView;
            for (int i = 0; i < childCount; ++i) {
                childView = drawCommentViews.getChildAt(i);
                childView.setVisibility(isHide ? View.GONE : View.VISIBLE);
            }
            drawCommentsToBmp();
        }
    }

    /**
     * 根据userId，删除相关的评论
     *
     * @param userId
     */
    public void deleteImageCommentByUserId(long userId) {

        boolean isHadUser = false;
        if (mImageCommentList != null) {
            for (int i = 0; i < mImageCommentList.size(); ) {
                if (mImageCommentList.get(i).userId == userId) {
                    isHadUser = true;
                    mImageCommentList.remove(i);
                    continue;
                }
                ++i;
            }
        }
        if (isHadUser) {
            findViewById(R.id.ivCommentsView).setVisibility(View.GONE);
            RelativeLayout drawCommentViews = (RelativeLayout) findViewById(R.id.drawCommentViews);
            drawCommentViews.setVisibility(View.VISIBLE);
            long tagUserId;
            View childView;
            for (int i = 0; i < drawCommentViews.getChildCount(); ) {
                childView = drawCommentViews.getChildAt(i);
                tagUserId = (long) childView.getTag();
                if (tagUserId == userId) {
                    drawCommentViews.removeView(childView);
                    continue;
                }
                ++i;
            }
            mShowPostView.resetDrawCommentNum(getDrawUserNum());
            setDrawCommentNum();
        }
    }

    private void resetSelectPenWidthView() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.llSelectPenWidth);
        int childCount = ll.getChildCount();
        SelectPenWidthView selectPenWidthView;
//        int currentPenWidthType = (mCurrentPenType == TYPE_COMMON_PEN) ? mCurrentCommonPenWidthType : mCurrentGlowPenWidthType;
        for (int i = 0; i < childCount; ++i) {
            selectPenWidthView = (SelectPenWidthView) ll.getChildAt(i);
            selectPenWidthView.setSelected((int) selectPenWidthView.getTag() == mCurrentPenWidthType);
        }
    }


    @Override
    public void onClick(View v) {
        if (mCurrentUseIn == USE_IN_EDIT_POST) {
            if (((EditPostFragment) mBaseFragment).getHandleRelativeLayout().isInEnableGPSPage()) {
                return;
            }
        }
        switch (v.getId()) {
            case R.id.btnClose:
                removeDrawCommentView(true);
                break;
            case R.id.btnSticker:
                showStickersDialog();
                break;
            case R.id.btnText:
                mIsWarterMarkText = false;
                if (!isPostEditViewVisibility()) {
                    EditPostFragment editPostFragment = (EditPostFragment) mBaseFragment;
                    editPostFragment.getPostText().setVisibility(View.GONE);
                    ((EditPostFragment) mBaseFragment).getPostText().setVisibility(View.GONE);
                    editPostFragment.getHandleRelativeLayout().setCanScroll(false);
                    if (isEmoViewVisibility()) {
                        hideEmoView();
                    }
                    if (getBottomView().getVisibility() == View.VISIBLE) {
                        getBottomView().setVisibility(View.GONE);
                    }
                    findViewById(R.id.ivTextColor).setVisibility(View.GONE);
                    findViewById(R.id.ivTextColorBg).setVisibility(View.GONE);
                    getTopCoverView().setVisibility(View.VISIBLE);
                    findViewById(R.id.btnPen).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btnSticker).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btnWartMark).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btnPenCoverView).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnStickerCoverView).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnWartMarkCoverView).setVisibility(View.VISIBLE);

                    getEtPost().setFocusable(true);
                    getEtPost().setFocusableInTouchMode(true);
                    getEtPost().requestFocus();
                    showOrHideSoftKey(true);
                    RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) getEtPost().getLayoutParams();
                    rl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    getEtPost().setMaxLines(3);
                    getEtPost().setHint(ServerDataManager.getTextFromKey("edtpst_txt_titletagmentionfriend"));
                    getEtPost().setGravity(Gravity.LEFT | Gravity.TOP);
                    rl.topMargin = Utils.dip2px(getContext(), 60);
                    getPostEditView().setVisibility(View.VISIBLE);
                    ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_back));
                    if (mPostText == null) {
                        mPostText = "";
                    }
                    getEtPost().setText(mPostText);
                    getEtPost().setTextSize(16);
                    getEtPost().setSelection(mPostText.length());
                    getEtPost().setTextColor(Color.WHITE);
                    getEtPost().setTypeface(Typeface.DEFAULT);
                } else {
                    showView();
                }
                break;
            case R.id.penWidth_8:
            case R.id.penWidth_12:
            case R.id.penWidth_16:
            case R.id.penWidth_20:
                int penWidthType = (int) v.getTag();
                mCurrentPenWidthType = penWidthType;
                ((DrawView) findViewById(R.id.drawView)).setCurrentPenWidthType(penWidthType);
                resetSelectPenWidthView();
                break;
            case R.id.btnGlowPen:
                if (findViewById(R.id.reGlowPen).getVisibility() == View.VISIBLE && mCurrentPenType == TYPE_GLOW_PEN) {
                    showOrHidePenWithViewAndColorViewWithAnimation();
                    return;
                }
                mCurrentPenType = TYPE_GLOW_PEN;
                if (mLastDrawColor == -1) {
                    mLastDrawColor = Color.rgb(30, 161, 66);
                }
                showViewByCurrentPen();
                break;
            case R.id.btnPen:
                if (findViewById(R.id.reGlowPen).getVisibility() == View.VISIBLE && mCurrentPenType == TYPE_COMMON_PEN) {
                    showOrHidePenWithViewAndColorViewWithAnimation();
                    return;
                }
                mCurrentPenType = TYPE_COMMON_PEN;
                if (mLastDrawColor == -1) {
                    mLastDrawColor = Color.rgb(30, 161, 66);
                }
                showViewByCurrentPen();
                break;
            case R.id.warterMarkParentView:
                if (((DrawView) findViewById(R.id.drawView)).isEnable()) {
                    return;
                }
                if (!isPostEditViewVisibility()) {
                    findViewById(R.id.btnWartMark).performClick();
                }
                break;
            case R.id.btnWartMark:
                if (((DrawView) findViewById(R.id.drawView)).isEnable()) {
                    return;
                }
                mIsWarterMarkText = true;
                if (!isPostEditViewVisibility()) {
                    if (isEmoViewVisibility()) {
                        hideEmoView();
                    }
                    if (getBottomView().getVisibility() == View.VISIBLE) {
                        getBottomView().setVisibility(View.GONE);
                    }
                    removeMarkImageView();
                    getPostEditView().setVisibility(View.VISIBLE);
                    ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_back));
                    if (mWaterMarkText == null) {
                        mWaterMarkText = "";
                    }
                    if (mCurrentUseIn == USE_IN_EDIT_POST) {
                        ((EditPostFragment) mBaseFragment).getPostText().setVisibility(View.GONE);
                    }
                    getEtPost().setText(mWaterMarkText);
                    getEtPost().setTextSize(WarterMarkView.TEXT_SIZE);
                    getEtPost().setSelection(mWaterMarkText.length());
                    getEtPost().setTextColor(mLastTextColor);
                    getEtPost().setFocusable(true);
                    getEtPost().setFocusableInTouchMode(true);
                    getEtPost().requestFocus();
                    showOrHideSoftKey(true);
                    getEtPost().setMaxLines(100);
                    getEtPost().setHint("");
                    getEtPost().setTypeface(Typeface.DEFAULT_BOLD);
                    RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) getEtPost().getLayoutParams();
                    rl.height = getHeight() / 2;
                    getEtPost().requestLayout();
                    rl.topMargin = Utils.dip2px(getContext(), 30);
                    if (mIsWartmarkShowCenter) {
                        getEtPost().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                    } else {
                        getEtPost().setGravity(Gravity.LEFT | Gravity.BOTTOM);
                    }

                    findViewById(R.id.rlDrawColor).setVisibility(View.GONE);
                    findViewById(R.id.btnPen).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btnSticker).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btnText).setVisibility(mCurrentUseIn == USE_IN_EDIT_POST ? View.INVISIBLE : View.GONE);
                    findViewById(R.id.ivTextColor).setVisibility(View.VISIBLE);
                    findViewById(R.id.ivTextColorBg).setVisibility(View.VISIBLE);
                    getTopCoverView().setVisibility(View.VISIBLE);
                    findViewById(R.id.btnPenCoverView).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnStickerCoverView).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnTextCoverView).setVisibility(mCurrentUseIn == USE_IN_EDIT_POST ? View.VISIBLE : View.GONE);
                    findViewById(R.id.btnWartMarkCoverView).setVisibility(View.INVISIBLE);
                    if (mCurrentUseIn == USE_IN_EDIT_POST) {
                        ((EditPostFragment) mBaseFragment).getPostText().setVisibility(View.GONE);
                    }
                } else {
                    mIsWartmarkShowCenter = !mIsWartmarkShowCenter;
                    if (mIsWartmarkShowCenter) {
                        getEtPost().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                    } else {
                        getEtPost().setGravity(Gravity.LEFT | Gravity.BOTTOM);
                    }
                }
                break;
            case R.id.tvSubmit:
                if (!isCanSubmit()) {
                    return;
                }
                if (TextUtils.isEmpty(replyID)) {
                    createImageCommentData(CREATE_IMAGE_COMMENT, 0);
                } else {
                    try {
                        long replyUserId = Long.parseLong(replyID);
                        createImageCommentData(CREATE_REPLY_IMAGE_COMMENT, replyUserId);
                    } catch (Exception e) {
                        replyID = null;
                    }

                }
                break;
            case R.id.btnRemove:
                DrawView drawView = (DrawView) findViewById(R.id.drawView);
                drawView.unDoDraw();
                findViewById(R.id.tvSubmit).setVisibility(isCanSubmit() ? View.VISIBLE : View.GONE);
                v.setVisibility(drawView.isDrawPathsListEmpty() ? View.GONE : View.VISIBLE);
                break;
            case R.id.tvPaint:
                DrawComentPopupWindow mDrawComentPopupWindow = new DrawComentPopupWindow(BaseApplication.getCurFragment().getActivity());
                mDrawComentPopupWindow.setPostBean(mShowPostView.getPostBean(), this);
                mDrawComentPopupWindow.showAtLocation(this, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            default:
                break;
        }
    }

    private void showOrHidePenWithViewAndColorViewWithAnimation() {
        final View llSelectPenWidth = findViewById(R.id.llSelectPenWidth);
        final View rlDrawColor = findViewById(R.id.rlDrawColor);
        Animation animation;
        if (llSelectPenWidth.getVisibility() == View.VISIBLE) {
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_right_out);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    llSelectPenWidth.setVisibility(View.GONE);
                    rlDrawColor.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            llSelectPenWidth.startAnimation(animation);
            rlDrawColor.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_top_out));
        } else {
            llSelectPenWidth.setVisibility(View.VISIBLE);
            rlDrawColor.setVisibility(View.VISIBLE);
            rlDrawColor.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_top_in));
        }
    }

    private void showViewByCurrentPen() {
        ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_back));
        ((WarterMarkParentView) findViewById(R.id.warterMarkParentView)).setTouchEnable(false);
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        drawView.setCurrentPenType(mCurrentPenType);
        findViewById(R.id.btnRemove).setVisibility(drawView.isDrawPathsListEmpty() ? View.GONE : View.VISIBLE);
        drawView.setEnable(true);
        findViewById(R.id.reGlowPen).setVisibility(View.VISIBLE);
        findViewById(R.id.btnGlowPen).setVisibility(View.VISIBLE);
        resetSelectPenWidthView();
        drawView.setCurrentPenWidthType(mCurrentPenWidthType);
        drawView.resetPaintColor(mLastDrawColor);
        if (mCurrentPenType == TYPE_GLOW_PEN) {
//            drawView.resetPaintColor(mLastDrawColor);
            //          drawView.setCurrentPenWidthType(mCurrentGlowPenWidthType);
            findViewById(R.id.btnPenBg).setVisibility(View.GONE);
            findViewById(R.id.btnGlowPenBg).setVisibility(View.VISIBLE);
            GradientDrawable bgShape = (GradientDrawable) findViewById(R.id.btnGlowPenBg).getBackground();
            bgShape.setColor(mLastDrawColor);
        } else {
            findViewById(R.id.btnSticker).setVisibility(View.GONE);
            findViewById(R.id.reText).setVisibility(View.GONE);
            findViewById(R.id.btnText).setVisibility(View.GONE);
            getTopCoverView().setVisibility(View.GONE);
            findViewById(R.id.btnPenBg).setVisibility(View.VISIBLE);
            findViewById(R.id.btnGlowPenBg).setVisibility(View.GONE);
            GradientDrawable bgShape = (GradientDrawable) findViewById(R.id.btnPenBg).getBackground();
            bgShape.setColor(mLastDrawColor);
        }
        if (findViewById(R.id.rlDrawColor).getVisibility() == View.GONE) {
            showOrHidePenWithViewAndColorViewWithAnimation();
        }
        if (mCurrentUseIn == USE_IN_EDIT_POST) {
            ((EditPostFragment) mBaseFragment).getPostText().setVisibility(View.VISIBLE);
        }
    }

    Handler mAbandonDialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    break;
                case Constants.DIALOG_RIGHY_BTN:
                    removeDrawCommentView(false);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public boolean isPostEditViewVisibility() {
        return getPostEditView().getVisibility() == View.VISIBLE;
    }

    public View getPostEditView() {
        return findViewById(R.id.postEditView);
    }

    protected MentionTextView getEtPost() {
        return (MentionTextView) findViewById(R.id.etPost);
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
                    int maxLength = 140;
                    if (s.toString().contains(String.valueOf((char) 10))) {
                        int index = getEtPost().getSelectionStart();
                        s.delete(index - 1, index);//将输入的回车键移除
                        showView();
                    } else if (text.length() > maxLength) {
                        int index = getEtPost().getSelectionStart();
                        s.delete(index - 1, index);//将输入的回车键移除
                    }
                }
            }
        });
        getEtPost().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            return true;
                    }
                }
                return false;
            }
        });
    }

    protected void showView() {
        postEditView();
    }

    public void postEditView() {
        if (isPostEditViewVisibility()) {
            getPostEditView().setVisibility(View.GONE);
            getBottomView().setVisibility(View.VISIBLE);
            findViewById(R.id.btnPen).setVisibility(View.VISIBLE);
            findViewById(R.id.btnWartMark).setVisibility(View.VISIBLE);
            findViewById(R.id.btnSticker).setVisibility(View.VISIBLE);
            findViewById(R.id.btnText).setVisibility(mCurrentUseIn == USE_IN_EDIT_POST ? View.VISIBLE : View.GONE);
            findViewById(R.id.ivTextColor).setVisibility(View.GONE);
            findViewById(R.id.ivTextColorBg).setVisibility(View.GONE);
            getTopCoverView().setVisibility(View.GONE);

            if (!mIsWarterMarkText) {
                mPostText = getEtPost().getText().toString();
            } else {
                mWaterMarkText = getEtPost().getText().toString();
                addWatermarkImageView(mWaterMarkText, mLastTextColor,
                        findViewById(R.id.btnWartMark), getPostEditView(), mIsWartmarkShowCenter);
            }
            if (mCurrentUseIn == USE_IN_EDIT_POST) {
                EditPostFragment editPostFragment = (EditPostFragment) mBaseFragment;
                editPostFragment.getHandleRelativeLayout().setCanScroll(true);
                editPostFragment.getPostText().setVisibility(View.VISIBLE);
                editPostFragment.showPostText(mPostText);
            }
            ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_close));
        }
        showOrHideSoftKey(false);
    }

    private void showOrHideSoftKey(boolean isShow) {
        if (isShow) {
            getEtPost().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getInputMethodManager().toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }, 50);
        } else {
            if (getInputMethodManager().isActive()) {
                getInputMethodManager().hideSoftInputFromWindow(getEtPost().getWindowToken(), 0);
            }
        }
    }

    private InputMethodManager getInputMethodManager() {
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return mInputMethodManager;
    }


    private void showOrHideWithAnim(View view, boolean isShow) {
        AlphaAnimation animation;
        if (isShow) {
            view.setVisibility(View.VISIBLE);
            animation = new AlphaAnimation(0, 1);
        } else {
            view.setVisibility(View.GONE);
            animation = new AlphaAnimation(1, 0);
        }
        animation.setDuration(500);//设置动画持续时间
        view.setAnimation(animation);
        animation.start();
    }

    /**
     * 判断当前是否有未提交的绘画评论
     *
     * @return
     */
    private boolean isCanSubmit() {
        boolean isHadDrawPath = !((DrawView) findViewById(R.id.drawView)).isDrawPathsListEmpty();
        if (isHadDrawPath) {
            return true;
        }
        WarterMarkParentView strickerParentView = (WarterMarkParentView) findViewById(R.id.strickerParentView);
        int childCount = strickerParentView.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            if (!((StickerView) strickerParentView.getChildAt(i)).isFix()) {
                return true;
            }
        }

        WarterMarkParentView warterMarkParentView = (WarterMarkParentView) findViewById(R.id.warterMarkParentView);
        childCount = warterMarkParentView.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            if (!((StickerView) warterMarkParentView.getChildAt(i)).isFix()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPenViewShow() {
        return findViewById(R.id.reGlowPen).getVisibility() == View.VISIBLE;
    }

    private void hidePenView() {
        ((WarterMarkParentView) findViewById(R.id.warterMarkParentView)).setTouchEnable(true);
        ((DrawView) findViewById(R.id.drawView)).setEnable(false);
        findViewById(R.id.llSelectPenWidth).setVisibility(View.GONE);
        findViewById(R.id.rlDrawColor).setVisibility(View.GONE);
        findViewById(R.id.btnPenBg).setVisibility(View.GONE);
        findViewById(R.id.reGlowPen).setVisibility(View.GONE);
        findViewById(R.id.btnRemove).setVisibility(View.GONE);
        findViewById(R.id.btnSticker).setVisibility(View.VISIBLE);
        findViewById(R.id.btnText).setVisibility(mCurrentUseIn == USE_IN_EDIT_POST ? View.VISIBLE : View.GONE);
        findViewById(R.id.reText).setVisibility(View.VISIBLE);
        ((ImageView) findViewById(R.id.btnPen)).setImageDrawable(getResources().getDrawable(R.drawable.edit_pen));
        ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_close));
    }


    /**
     * 判断是否能退出
     *
     * @param isNeedSure
     */
    public void removeDrawCommentView(boolean isNeedSure) {
        if (isPenViewShow()) {
            hidePenView();
            return;
        }
        if (isPostEditViewVisibility()) {
            showView();
            return;
        }
        if (isEmoViewVisibility()) {
            hideEmoView();
            return;
        }
        if (mCurrentUseIn == USE_IN_SHOW_POST) {
            if (isNeedSure && isCanSubmit()) {
                ((BaseActivity) getContext()).showPublicDialog(null, ServerDataManager.getTextFromKey("drwngcmmnt_txt_abandon"),
                        ServerDataManager.getTextFromKey("pblc_btn_no"), ServerDataManager.getTextFromKey("pblc_btn_yes"), false, true, mAbandonDialog);
                return;
            }
            ViewGroup rl = (ViewGroup) getParent();
            rl.removeView(this);
            mShowPostView.showOrHideViewWhenShowCommentsView(true);
            recycleSource();
        } else {
            recycleSource();
            mBaseFragment.goBack();
        }
    }

    public String getReplyID() {
        return replyID;
    }

    public void setReplyID(String replyID) {
        this.replyID = replyID;
        if (findViewById(R.id.rlDrawColor).getVisibility() != View.VISIBLE) {
            findViewById(R.id.btnPen).performClick();
        }
    }


    /**
     * 显示或隐藏表贴图页
     */
    private void showStickersDialog() {

        if (isEmoViewVisibility()) {
            hideEmoView();
            return;
        }
        showEmoView();
        initViewPager();
    }

    public boolean isEmoViewVisibility() {
        return getEmoView().getVisibility() == View.VISIBLE;
    }

    /**
     * 隐藏贴图页
     */
    public void hideEmoView() {
        if (isAnimation) {
            return;
        }
        isAnimation = true;
        ObjectAnimator animator = ObjectAnimator.ofFloat(getEmoView(), "translationY", 0.0F, getMeasuredHeight());
        animator.setDuration(500).start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getEmoView().setVisibility(View.GONE);
                getBottomView().setVisibility(View.VISIBLE);
                if (mCurrentUseIn == USE_IN_EDIT_POST) {
                    ((EditPostFragment) mBaseFragment).getPostText().setVisibility(View.VISIBLE);
                }
                ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_close));
                getEmoView().clearAnimation();
                isAnimation = false;
            }
        });
        getTopCoverView().setVisibility(View.GONE);
        findViewById(R.id.btnPenCoverView).setVisibility(View.GONE);
        findViewById(R.id.ivTextColor).setVisibility(View.GONE);
        findViewById(R.id.ivTextColorBg).setVisibility(View.GONE);
        findViewById(R.id.btnWartMarkCoverView).setVisibility(View.GONE);
        findViewById(R.id.btnPen).setVisibility(View.VISIBLE);
        findViewById(R.id.btnWartMark).setVisibility(View.VISIBLE);
        findViewById(R.id.btnText).setVisibility(mCurrentUseIn == USE_IN_EDIT_POST ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnPen).setEnabled(true);
        findViewById(R.id.btnWartMark).setEnabled(true);
        if (mCurrentUseIn == USE_IN_EDIT_POST) {
            findViewById(R.id.btnText).setVisibility(View.VISIBLE);
            findViewById(R.id.btnText).setEnabled(true);
            ((EditPostFragment) mBaseFragment).getPostText().setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btnText).setVisibility(View.GONE);
            findViewById(R.id.btnTextCoverView).setVisibility(View.GONE);
        }

    }


    /**
     * 显示贴图页
     */
    private void showEmoView() {
        if (isAnimation) {
            return;
        }
        isAnimation = true;
        if (mCurrentUseIn == USE_IN_EDIT_POST) {
            ((EditPostFragment) mBaseFragment).getPostText().setVisibility(View.GONE);
        }
        findViewById(R.id.btnPen).setEnabled(false);
        findViewById(R.id.btnWartMark).setEnabled(false);
        findViewById(R.id.btnText).setEnabled(false);
        findViewById(R.id.rlDrawColor).setVisibility(View.GONE);
        getEmoView().setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(getEmoView(), "translationY", getMeasuredHeight(), 0.0F);
        animator.setDuration(500).start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getEmoView().clearAnimation();
                isAnimation = false;
                getTopCoverView().setVisibility(View.VISIBLE);
                findViewById(R.id.btnPenCoverView).setVisibility(View.VISIBLE);
                findViewById(R.id.btnWartMarkCoverView).setVisibility(View.VISIBLE);
                findViewById(R.id.btnPen).setVisibility(View.INVISIBLE);
                findViewById(R.id.btnWartMark).setVisibility(View.INVISIBLE);
                if (mCurrentUseIn == USE_IN_EDIT_POST) {
                    findViewById(R.id.btnText).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btnTextCoverView).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.btnText).setVisibility(View.GONE);
                    findViewById(R.id.btnTextCoverView).setVisibility(View.GONE);
                }

            }
        });
        getBottomView().setVisibility(View.GONE);
        ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_back));
    }

    /**
     * 添加贴图页
     */
    private void initViewPager() {
        if (mEmoViewList == null) {
            mEmoViewList = new ArrayList<>();
        }
        if (mEmoViewList.isEmpty()) {
            for (int i = 0; i < Constants.STRICK_NAMES.length; ++i) {
                mEmoViewList.add(LayoutInflater.from(getContext()).inflate(R.layout.layout_stickers_gridview, null));
            }
            getPagerDotView().setTotalPage(Constants.STRICK_NAMES.length);
            getStickerView().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    getPagerDotView().setCurrentPageIndex(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            getStickerView().setAdapter(new PagerAdapter() {
                @Override
                public boolean isViewFromObject(View arg0, Object arg1) {
                    return arg0 == arg1;
                }

                @Override
                public int getCount() {
                    return mEmoViewList.size();
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView(mEmoViewList.get(position));
                }

                @Override
                public CharSequence getPageTitle(int position) {
                    return "";
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    View view = mEmoViewList.get(position);
                    container.addView(view);
                    final String[] resArrayList = Constants.STRICK_NAMES[position];
                    GridView gridView = view.findViewById(R.id.gridView);
                    if (position == 0 || position == 1) {
                        gridView.setNumColumns(2);
                    }
                    final EmoAdapter emoAdapter = new EmoAdapter(getContext(), resArrayList, (position == 0 || position == 1) ? 2 : 5);
                    gridView.setAdapter(emoAdapter);
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position >= resArrayList.length) {
                                return;
                            }
                            hideEmoView();
                            int width = view.getWidth();
                            int height = view.getHeight();
                            int centerX = view.getLeft() + width / 2;
                            int centerY = view.getTop() + Utils.dip2px(getContext(), 73) + height / 2;
                            addEmo(resArrayList[position], Utils.getDrawableIdByName(resArrayList[position]),
                                    position + 1, centerX, centerY, width, height, findViewById(R.id.btnSticker));
                        }
                    });
                    return mEmoViewList.get(position);
                }
            });

        }
        getPagerDotView().setCurrentPageIndex(0);
        getStickerView().setCurrentItem(0);
    }


    /**
     * 添加表情
     *
     * @param name
     * @param selectedEmoId
     * @param index
     * @param centerX
     * @param centerY
     * @param width
     * @param height
     * @param btnStricker
     */
    private void addEmo(String name, int selectedEmoId, int index, int centerX, int centerY, int width, int height, View btnStricker) {
        StickerView stickerView = new StickerView(getContext());
        Bitmap emoBmp;
        if (name.startsWith("svg_")) {
            emoBmp = getBitmapFromVectorDrawable(selectedEmoId);
        } else {
            emoBmp = BitmapFactory.decodeResource(getResources(), selectedEmoId);
        }
        stickerView.setWaterMark(emoBmp, name, index, centerX, centerY, width * 1.0f / emoBmp.getWidth(), (ImageButton) btnStricker);
        ((RelativeLayout) findViewById(R.id.strickerParentView)).addView(stickerView);
        stickerView.setTag(StickerView.EMO_STICKER);
        findViewById(R.id.tvSubmit).setVisibility(View.VISIBLE);
        stickerView.setDeleteStickerListener(new StickerView.DeleteStickerListener() {
            @Override
            public void deleteSticker(StickerView stickerView) {
                if (stickerView.getParent() != null) {
                    ((RelativeLayout) findViewById(R.id.strickerParentView)).removeView(stickerView);
                }
                findViewById(R.id.tvSubmit).setVisibility(isCanSubmit() ? View.VISIBLE : View.GONE);
            }
        });
        stickerView.setTag(R.id.tag, MemberShipManager.getInstance().getUserID());
    }


    private Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * 添加水印
     *
     * @param postText
     * @param textColor
     * @param btnWartMark
     * @param editPostView
     * @param isTextCenter
     */
    public void addWatermarkImageView(String postText, int textColor, final View btnWartMark, final View editPostView, boolean isTextCenter) {
        if (TextUtils.isEmpty(postText)) {
            return;
        }
        removeMarkImageView();
        mWarterMarkView = new WarterMarkView(getContext());
        mWarterMarkView.setPostText(postText, textColor, isTextCenter);
        ((RelativeLayout) findViewById(R.id.warterMarkParentView)).addView(mWarterMarkView);
        mWarterMarkView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editPostView.getVisibility() != View.VISIBLE) {
                    btnWartMark.performClick();
                }
            }
        });
        mWarterMarkView.setTag(StickerView.TEXT_WARTERMARK);
        if (mCurrentUseIn == USE_IN_SHOW_POST) {
            findViewById(R.id.tvSubmit).setVisibility(View.VISIBLE);
        }
    }

    /**
     * 移除水印
     */
    public void removeMarkImageView() {
        if (mWarterMarkView != null && mWarterMarkView.getParent() != null) {
            ((RelativeLayout) findViewById(R.id.warterMarkParentView)).removeView(mWarterMarkView);
        }
        if (mWarterMarkView != null) {
            mWarterMarkView.recycleBmp();
        }
        mWarterMarkView = null;
        if (mCurrentUseIn == USE_IN_SHOW_POST) {
            findViewById(R.id.tvSubmit).setVisibility(isCanSubmit() ? View.VISIBLE : View.GONE);
        }
    }

    private ViewPager getStickerView() {
        return (ViewPager) findViewById(R.id.stickerView);
    }

    private PagerDotView getPagerDotView() {
        return (PagerDotView) findViewById(R.id.pagerDotView);
    }


    private View getTopCoverView() {
        return findViewById(R.id.rlCoverView);
    }

    private View getBottomView() {
        if (mCurrentUseIn == USE_IN_EDIT_POST) {
            return ((EditPostFragment) mBaseFragment).getBottomView();
        }
        return findViewById(R.id.bottomView);
    }

    public ShowPicTextView getShowPicTextView() {
        return (ShowPicTextView) findViewById(R.id.showPicTv);
    }

    private View getEmoView() {
        return findViewById(R.id.emoView);
    }

    public String getPostTextText() {
        if (mPostText == null)
            mPostText = "";
        return mPostText;
    }

    /**
     * 将这个View上绘制的东西保存为Bmp
     *
     * @param glBmp
     */
    public void savePhotoToAlbum(Bitmap glBmp, int currentEmo, View handleView) {
        DrawView drawView = findViewById(R.id.drawView);
        getShowPicTextView().setBitmap(glBmp);
        Bitmap bitmap = Bitmap.createBitmap(drawView.getWidth(), drawView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        findViewById(R.id.rlDrawWaterMarkView).draw(canvas);
        handleView.draw(canvas);
        int padding = Utils.dip2px(getContext(), 7);
        Bitmap wartMark = BitmapFactory.decodeResource(getResources(), R.drawable.white_water_mark);
        canvas.drawBitmap(wartMark, bitmap.getWidth() - wartMark.getWidth() - padding, bitmap.getHeight() - wartMark.getHeight() - padding, null);
        Bitmap emoBmp = ((BitmapDrawable) getResources().getDrawable(Constants.EMO_ID_COLOR[currentEmo][0])).getBitmap();
        canvas.drawBitmap(emoBmp, new Rect(0, 0, emoBmp.getWidth(), emoBmp.getHeight()),
                new RectF(padding, bitmap.getHeight() - emoBmp.getHeight() / 2 - padding, padding + emoBmp.getWidth() / 2, bitmap.getHeight() - padding), null);
        Utils.savePhotoToAlbum(bitmap, getContext());
        wartMark.recycle();
        glBmp.recycle();
        glBmp = null;
        bitmap.recycle();
        bitmap = null;
        getShowPicTextView().setBitmap(null);
    }

    /**
     * 获取绘制好的图片
     *
     * @param glBmp
     * @return
     */
    public Bitmap getPostBmp(Bitmap glBmp, View handleView) {
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        getShowPicTextView().setBitmap(glBmp);
        Bitmap bitmap = Bitmap.createBitmap(drawView.getWidth(), drawView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        findViewById(R.id.rlDrawWaterMarkView).draw(canvas);
        handleView.draw(canvas);
        glBmp.recycle();
        glBmp = null;
        getShowPicTextView().setBitmap(null);
        return bitmap;
    }

    public Bitmap getBmpInVideo(boolean isSave, int currentEmo, View handleView) {
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        getShowPicTextView().setBitmap(null);
        Bitmap bitmap = Bitmap.createBitmap(drawView.getWidth(), drawView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        findViewById(R.id.rlDrawWaterMarkView).draw(canvas);
        handleView.draw(canvas);
        if (isSave) {
            int padding = Utils.dip2px(getContext(), 7);
            Bitmap wartMark = BitmapFactory.decodeResource(getResources(), R.drawable.white_water_mark);
            canvas.drawBitmap(wartMark, bitmap.getWidth() - wartMark.getWidth() - padding, bitmap.getHeight() - wartMark.getHeight() - padding, null);
            wartMark.recycle();
            Bitmap emoBmp = ((BitmapDrawable) getResources().getDrawable(Constants.EMO_ID_COLOR[currentEmo][0])).getBitmap();
            canvas.drawBitmap(emoBmp, new Rect(0, 0, emoBmp.getWidth(), emoBmp.getHeight()),
                    new RectF(padding, bitmap.getHeight() - emoBmp.getHeight() / 2 - padding, padding + emoBmp.getWidth() / 2, bitmap.getHeight() - padding), null);
        }
        return bitmap;
    }

    private void recycleSource() {
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        drawView.recycleBmp();
        ((ImageView) findViewById(R.id.ivCommentsView)).setImageBitmap(null);
        if (mCommentsBmp != null && !mCommentsBmp.isRecycled()) {
            mCommentsBmp.recycle();
        }
        mCommentsBmp = null;
    }

}
