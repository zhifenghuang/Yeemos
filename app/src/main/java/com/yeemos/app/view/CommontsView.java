package com.yeemos.app.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.mentiontextview.MentionAdapter;
import com.common.mentiontextview.MentionTextView;
import com.gbsocial.BeansBase.CommentBean;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.adapter.CommentsAdapter;
import com.yeemos.app.interfaces.ChangeNumListener;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by gigabud on 16-5-24.
 */
public class CommontsView extends Dialog implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private CommentsAdapter mCommentListAdapter = null;
    private boolean mIsAnonymity;


    private ShowPostView mShowPostView;
    private Integer curReplyUserID;
    private String replyUserName;
    private Activity context;
    private ArrayList<CommentBean> arrayList;
    private ChangeNumListener changeNumListener;
    private View view;
    protected View viewG;
    protected RelativeLayout popLayout;
    private int mLastCommentsViewHeight;

    public void setChangeNumListener(ChangeNumListener changeNumListener) {
        this.changeNumListener = changeNumListener;
    }

    public void setAnonymity(boolean isAnonymity) {
        mIsAnonymity = isAnonymity;
        getAdapter().setAnonymity(mIsAnonymity);
        getEditText().setHint(ServerDataManager.getTextFromKey(mIsAnonymity ? "cmmnt_txt_saysthanonymous" : "cmmnt_txt_saysomething"));

    }

    public CommontsView(Activity context, int styleID) {
        super(context, styleID);
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.commonts_view, null);
        setContentView(view);
        getData();

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        final ViewGroup.LayoutParams lp = getMoreMenuView().getLayoutParams();
        mLastCommentsViewHeight = displayMetrics.heightPixels * 4 / 5;
        lp.height = mLastCommentsViewHeight;
        lp.width = displayMetrics.widthPixels;

        getEditText().setAdapter(new MentionAdapter(BaseApplication.getAppContext()));
        getEditText().setThreshold(1);
        getEditText().addMentionTrigerkey('@');
        getEditText().addMentionTrigerkey('#');
        getEditText().mListView = getListView();

        getTvPostView().setText(ServerDataManager.getTextFromKey("cmmnt_ttl_comment"));
        getNoCommentText().setText(ServerDataManager.getTextFromKey("cmmnt_txt_nocomment"));
        getIvSenderAvater().setNeedDrawVipBmp(DataManager.getInstance().getBasicCurUser().isAuthenticate());
        Utils.loadImage(BaseApplication.getAppContext(), R.drawable.default_avater, Preferences.getAvatarUrl(DataManager.getInstance().getBasicCurUser().getUserAvatar()), getIvSenderAvater());


        getSwipeRefreshLayout().setOnRefreshListener(this);
        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);
        viewG = getContentView().findViewById(R.id.view_bg);

        getBtnClose().setOnClickListener(this);
        getBtnSend().setSelected(false);
        getBtnSend().setEnabled(getBtnSend().isSelected());

        getMoreMenuView().getViewTreeObserver().
                addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        if (getMoreMenuView().getParent() == null) {
                            getMoreMenuView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            return;
                        }
                        Rect r = new Rect();
                        getMoreMenuView().getWindowVisibleDisplayFrame(r);

                        int screenHeight = displayMetrics.heightPixels;
                        int heightDifference = screenHeight - (r.bottom - r.top);
                        if (heightDifference > screenHeight / 5) {
                            lp.height = screenHeight - heightDifference;
                            if (Build.VERSION.SDK_INT < 19) {
                                return;
                            }
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        } else {
                            lp.height = screenHeight * 4 / 5;
                        }
                        if (mLastCommentsViewHeight != lp.height) {
                            getMoreMenuView().requestLayout();
                            mLastCommentsViewHeight = lp.height;
                        }
                    }
                });

        getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                getBtnSend().setSelected(s.toString().length() != 0 ? true : false);
                getBtnSend().setEnabled(getBtnSend().isSelected());
            }
        });
        this.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    lp.height = displayMetrics.heightPixels * 4 / 5;
                }
                return false;
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = getMoreMenuView().getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        getBtnSend().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String commentText = getEditText().getText().toString();
                final CommentBean commentBean = (CommentBean) new CommentBean().setText(commentText).setOwner(DataManager.getInstance().getBasicCurUser());
                if (mIsAnonymity && !TextUtils.isEmpty(replyUserName) && commentText.startsWith(replyUserName)) {
                    commentBean.setAnonymityReplyUser(curReplyUserID);
                }
                commentBean.setCommentStatus(CommentBean.MenuCommentState.CommentState_Success);
                getAdapter().addNewComment(commentBean);
                DataManager.getInstance().getCurPostBean().setCommentNums(DataManager.getInstance().getCurPostBean().getCommentNums() + 1);
                getAdapter().notifyDataSetChanged();
                if (changeNumListener != null) {
                    changeNumListener.setDataNum(getAdapter().getCount());
                }
                GBExecutionPool.getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        ServerResultBean<PostBean> result = DataManager.getInstance().comment(commentBean);
                        if (!result.isSuccess()) {
                            commentBean.setCommentStatus(CommentBean.MenuCommentState.CommentState_Fail);
                            DataManager.getInstance().addFailCommentBean(commentBean, DataManager.getInstance().getCurPostBean().getId());
                            DataManager.getInstance().getCurPostBean().setCommentNums(DataManager.getInstance().getCurPostBean().getCommentNums() - 1);
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            public void run() {
                                mCommentListAdapter.notifyDataSetChanged();
                                if (changeNumListener != null) {
                                    changeNumListener.setDataNum(mCommentListAdapter.getCount());
                                }
                            }
                        });
                    }
                });
                getEditText().setText("");
                closeKeyBoard();
                ArrayList<CommentBean> arrList = DataManager.getInstance()
                        .getCurPostBean().getCommentObjects();
                if (arrList == null) {
                    arrList = new ArrayList<CommentBean>();
                }
                if (arrList.size() >= 3) {
                    arrList.remove(0);
                }
                arrList.add(commentBean);
                DataManager.getInstance().getCurPostBean().setCommentObjects(arrList);
            }
        });
    }

    private View getViewWholeBg() {
        return getContentView().findViewById(R.id.ViewWholeBg);
    }

    private RelativeLayout getMoreMenuView() {
        if (popLayout == null && getContentView() != null) {
            popLayout = (RelativeLayout) getContentView().findViewById(R.id.pop_layout);
        }
        return popLayout;
    }

    protected int getContentViewID() {
        return R.layout.commonts_view;
    }

    private void closeKeyBoard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(getEditText().getWindowToken(), 0);
        }
    }

    public CommentsAdapter getAdapter() {
        if (mCommentListAdapter == null) {
            mCommentListAdapter = new CommentsAdapter(DataManager.getInstance().getCurPostBean(), getContentView(),
                    new CommentsAdapter.CommentsFragmentsetMentionText() {

                        @Override
                        public void setMentionTextViewText(String str, Integer replyUserID) {
                            replyUserName = str;
                            curReplyUserID = replyUserID;
                            getEditText().setText(str);
                            getEditText().requestFocus();
                            getEditText().setSelection(str.length());
                            InputMethodManager imm = (InputMethodManager) context
                                    .getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                                    InputMethodManager.HIDE_IMPLICIT_ONLY);
                        }

                        @Override
                        public void showOrHideListView(boolean show) {
                            if (show) {
                                getNoCommentLayout().setVisibility(GONE);
                            } else {
                                getNoCommentLayout().setVisibility(VISIBLE);
                            }
                        }

                        @Override
                        public void resetCommentNum(long commentNum) {
                            if (changeNumListener != null) {
                                changeNumListener.setDataNum((int) commentNum);
                            }
                        }
                    });
        }
        return mCommentListAdapter;
    }

    protected void getData() {
        if (!getSwipeRefreshLayout().isRefreshing()) {
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    getSwipeRefreshLayout().setRefreshing(true);
                }
            });
        }
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final ServerResultBean<PostBean> resObj = DataManager.getInstance().getCommentsFromPost(0,
                        DataManager.getInstance().getCurPostBean());
                if (resObj == null || resObj.getData() == null) {
                    return;
                }
                arrayList = resObj.getData().getCommentObjects();
                handler.sendEmptyMessage(0);
            }
        });
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (getSwipeRefreshLayout().isRefreshing()) {
                getSwipeRefreshLayout().setRefreshing(false);
            }
            getAdapter().setCommentList(arrayList);
            getListView().setAdapter(getAdapter());
            changeNumListener.setDataNum(arrayList.size());
        }
    };

    private TextView getNoCommentText() {
        return (TextView) getContentView().findViewById(R.id.noCommentText);
    }

    private RelativeLayout getNoCommentLayout() {
        return (RelativeLayout) getContentView().findViewById(R.id.noCommentLayout);
    }

    private MentionTextView getEditText() {
        return (MentionTextView) getContentView().findViewById(R.id.etComment);
    }

    private ImageButton getBtnSend() {
        return (ImageButton) getContentView().findViewById(R.id.btnSend);
    }

    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getContentView().findViewById(R.id.swipeRefreshLayout);
    }

    protected ListView getListView() {
        return (ListView) getContentView().findViewById(R.id.listView);
    }

    protected TextView getTvPostView() {
        return (TextView) getContentView().findViewById(R.id.tvPostView);
    }

    private View getContentView() {
        return view;
    }

    @Override
    public void onRefresh() {
        getData();
    }

    private RoundedImageView getIvSenderAvater() {
        return (RoundedImageView) getContentView().findViewById(R.id.ivSenderAvater);
    }

    private ImageButton getBtnClose() {
        return (ImageButton) getContentView().findViewById(R.id.btnClose);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnClose:
                closeKeyBoard();
                dismiss();
                break;
        }
    }

}
