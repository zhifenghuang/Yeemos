package com.yeemos.app.view;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.ReplyTagBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.adapter.PillSreactAdapter;
import com.yeemos.app.interfaces.ChangeNumListener;
import com.yeemos.app.manager.DataManager;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by gigabud on 16-5-26.
 */
public class PillSreactView extends PostPopupWindow {

    private ArrayList<ReplyTagBean> mReplyTagList;
    private ChangeNumListener changeNumListener;
    private boolean mIsAnonymity;
    private PostBean mPostBean;

    public void setAnonymity(boolean isAnonymity) {
        mIsAnonymity = isAnonymity;
    }

    public void setChangeNumListener(ChangeNumListener changeNumListener) {
        this.changeNumListener = changeNumListener;
    }

//    public PillSreactView(Context context) {
//        this(context, null);
//    }

    public PillSreactView(Activity context, PostBean postBean) {
        super(context);
//        LayoutInflater.from(context).inflate(R.layout.pill_sreact_view, this);
        mPostBean = postBean;
        getTvPostView().setText(ServerDataManager.getTextFromKey("pllrct_ttl_feel"));
        getNoFeelText().setText(ServerDataManager.getTextFromKey("pllrct_txt_nofeel"));

//        getSwipeRefreshLayout().setOnRefreshListener(this);
//        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);
//        getData();
    }

    protected void getData() {
        super.getData();
        if (mPostBean == null) {
            return;
        }
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<PostBean> result = DataManager.getInstance().getReplyTagFromPost(mPostBean, 0);
                if (result == null || result.getData() == null) {
                    setmHasData(false);
                    setmIsGetingData(false);
                    return;
                }
                mReplyTagList = result.getData().getReplyTags();

                if (mReplyTagList == null || mReplyTagList.isEmpty() || mReplyTagList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                    setmHasData(false);
                }

                handler.sendEmptyMessage(0);
                setmIsGetingData(false);
            }
        });
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            if (getSwipeRefreshLayout().isRefreshing()) {
                getSwipeRefreshLayout().setRefreshing(false);
            }

            getPillAdapter().setReplyTagList(DataManager.getInstance().getCurPostBean(), mReplyTagList);
            if (mReplyTagList != null && mReplyTagList.size() > 0) {

                getNoFillLayout().setVisibility(GONE);
                int count = 0;
                for (ReplyTagBean tag : mReplyTagList) {
                    count += tag.getReplyNums();
                }
                changeNumListener.setDataNum(count);
//                if (getParent() != null) {
//                    ((TextView) ((View) getParent()).findViewById(R.id.tvEmoNum)).setText(String.valueOf(count));
//                }
            } else {
                changeNumListener.setDataNum(0);
                getNoFillLayout().setVisibility(VISIBLE);
            }
        }
    };

    private PillSreactAdapter getPillAdapter() {
        PillSreactAdapter adapter = (PillSreactAdapter) getListView().getAdapter();

        if (adapter == null) {
            adapter = new PillSreactAdapter(context, PillSreactView.this);
            adapter.setAnonymity(mIsAnonymity);
            getListView().setAdapter(adapter);
        }
        return adapter;
    }

    @Override
    public void loadMoreData() {
        super.loadMoreData();
        if (!ismHasData() || ismIsGetingData()) {
            return;
        }
        setmIsGetingData(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<PostBean> result = DataManager.getInstance()
                        .getReplyTagFromPost(DataManager.getInstance().getCurPostBean(), mReplyTagList.size());
                if (result == null || result.getData() == null) {
                    setmHasData(false);
                    setmIsGetingData(false);
                    return;
                }
                ArrayList<ReplyTagBean> mTagList = result.getData().getReplyTags();
                if (mTagList == null || mTagList.isEmpty() || mTagList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                    setmHasData(false);
                    if (!mTagList.isEmpty()) {
                        mReplyTagList.addAll(mTagList);
                        handler.sendEmptyMessage(0);
                    }
                }
                setmIsGetingData(false);
            }
        });
    }

    //    private ImageView getNoFeelLog() {
//        return (ImageView) findViewById(R.id.noFeelLog);
//    }
//
//    private TextView getNoFeelText() {
//        return (TextView) findViewById(R.id.noFeelText);
//    }
//
//    private SwipeRefreshLayout getSwipeRefreshLayout() {
//        return (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
//    }
//
//    private View getNoFillLayout() {
//        return findViewById(R.id.noFillLayout);
//    }
//
//    private ListView getListView() {
//        return (ListView) findViewById(R.id.listView);
//    }

}
