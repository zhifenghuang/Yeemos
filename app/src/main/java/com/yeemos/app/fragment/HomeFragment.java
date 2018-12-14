package com.yeemos.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.TagBean;
import com.gbsocial.BeansBase.TopicBean;
import com.gbsocial.server.YeemosTask;
import com.gigabud.core.JobDaddy.DaddyLook;
import com.gigabud.core.JobDaddy.JobDaddy;
import com.gigabud.core.JobDaddy.KittyJob;
import com.gigabud.core.task.ITask;
import com.gigabud.core.task.ITaskListener;
import com.google.gson.Gson;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.adapter.RecentPostAdapter;
import com.yeemos.app.interfaces.OnItemClickListener;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.MorePopupWindow;
import com.yeemos.app.view.SelectItemHorizontalScrollView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gigabud on 15-12-23.
 */
public class HomeFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, DaddyLook, ITaskListener {

    private SelectItemHorizontalScrollView mSelectEmoHorizontalScrollView, mSelectTopicHorizontalScrollView;
    //    private RecyclerView mUserRecentPostView;
    private ListView mUserRecentPostView;
    private RecentPostAdapter mFriendRecentPostAdapter;
    private ArrayList<PostBean> mUploadingPosts;
    private HashMap<String, ITask> mUploadFailedTasks;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BitmapCacheManager.getInstance().evictAll();
        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_home_header_view, null);
        mSelectEmoHorizontalScrollView = (SelectItemHorizontalScrollView) headView.findViewById(R.id.selectEmoView);
        mUserRecentPostView = (ListView) view.findViewById(R.id.userRecentPostView);
        mUserRecentPostView.addHeaderView(headView);
        addTags();
        addTopic(view);
        view.findViewById(R.id.btnCamera).setOnClickListener(this);
        view.findViewById(R.id.rlSearch).setOnClickListener(this);
        mFriendRecentPostAdapter = getFriendRecentPostAdapter();
        mUserRecentPostView.setAdapter(mFriendRecentPostAdapter);
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setSize(SwipeRefreshLayout.DEFAULT);
        resetRecentPostView(((HomeActivity) getActivity()).getRecentPostList());
        JobDaddy.getInstance().initDaddy(getContext());
        JobDaddy.getInstance().addObserver(this);
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (mUploadingPosts != null) {
            mUploadingPosts.clear();
        }
        mUploadingPosts = null;
        if (mUploadFailedTasks != null) {
            mUploadFailedTasks.clear();
        }
        mUploadFailedTasks = null;
        JobDaddy.getInstance().removeObserver(this);
    }

    public void onResume() {
        super.onResume();

        showOrHideRedPoint();
    }

    public void showOrHideRedPoint() {
        if (getView() == null) {
            return;
        }
        if (Preferences.getInstacne().getValues(HomeActivity.HAD_NEW_NOTIFICATION_MESSAGE, false)
                || Preferences.getInstacne().getValues(HomeActivity.HAD_REQUEST_FOLLOW_YOU_MESSAGE, false)) {
            getView().findViewById(R.id.redPoint).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.redPoint).setVisibility(View.GONE);
        }
    }

    private ArrayList<PostBean> getUploadingPosts() {
        if (mUploadingPosts == null) {
            mUploadingPosts = new ArrayList<>();
        }
        return mUploadingPosts;
    }

    private HashMap<String, ITask> getUploadFailedTasks() {
        if (mUploadFailedTasks == null) {
            mUploadFailedTasks = new HashMap<>();
        }
        return mUploadFailedTasks;
    }

    private void addUpLoadingPost(PostBean postBean) {
        for (PostBean pb : getUploadingPosts()) {
            if (pb.getId().equals(postBean.getId())) {
                getFriendRecentPostAdapter().notifyDataSetChanged();
                return;
            }
        }
        getUploadingPosts().add(0, postBean);
        postBean.setUploadState(1);//0:表示上传成功1:表示正在上传2:表示上传失败
        postBean.setUploadProgress(0);
        getFriendRecentPostAdapter().setUploadingPostBeanList(getUploadingPosts());
    }

    public void showTourialView() {
        final View rlTourialView = getActivity().findViewById(R.id.rlTourial);
        rlTourialView.postDelayed(new Runnable() {
            @Override
            public void run() {
                rlTourialView.setVisibility(View.VISIBLE);
                View tourialView4 = rlTourialView.findViewById(R.id.tourialView4);
                tourialView4.setVisibility(View.VISIBLE);
                View view1 = getView().findViewById(R.id.selectEmoView);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView4.getLayoutParams();
                lp.leftMargin = (((BaseActivity) getActivity()).getDisplaymetrics().widthPixels - Utils.dip2px(getActivity(), 330)) / 2;
                lp.topMargin = getView().findViewById(R.id.line2).getBottom() + view1.getTop() - Utils.dip2px(getActivity(), 55);
                rlTourialView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View tourialView4 = v.findViewById(R.id.tourialView4);
                        View tourialView5 = v.findViewById(R.id.tourialView5);
                        View tourialView6 = v.findViewById(R.id.tourialView6);
                        //          View tourialView7 = v.findViewById(R.id.tourialView7);
                        if (tourialView4.getVisibility() == View.VISIBLE) {
                            tourialView4.setVisibility(View.GONE);
                            tourialView5.setVisibility(View.VISIBLE);
                            View view2 = getView().findViewById(R.id.selectTopicView);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView5.getLayoutParams();
                            lp.leftMargin = (((BaseActivity) getActivity()).getDisplaymetrics().widthPixels - Utils.dip2px(getActivity(), 115)) / 2;
                            lp.topMargin = getView().findViewById(R.id.line2).getBottom() + view2.getTop() + (view2.getHeight() - Utils.dip2px(getActivity(), 37)) / 2;
                        } else if (tourialView5.getVisibility() == View.VISIBLE) {
                            tourialView5.setVisibility(View.GONE);
                            tourialView6.setVisibility(View.VISIBLE);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView6.getLayoutParams();
                            lp.leftMargin = (((BaseActivity) getActivity()).getDisplaymetrics().widthPixels - Utils.dip2px(getActivity(), 217)) / 2;
                            lp.topMargin = ((getView().findViewById(R.id.tvRecent).getBottom() + ((BaseActivity) getActivity()).getDisplaymetrics().heightPixels)) / 2 -
                                    Utils.dip2px(getActivity(), 37) / 2;
                        } else {
                            tourialView6.setVisibility(View.GONE);
                            v.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }, 100);

    }


    private RecentPostAdapter getFriendRecentPostAdapter() {
        if (mFriendRecentPostAdapter == null) {
            mFriendRecentPostAdapter = new RecentPostAdapter(getActivity());
            mFriendRecentPostAdapter.setIsRecentPostList(true);
            mFriendRecentPostAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    DataManager.getInstance().setShowPostList((ArrayList<PostBean>) ((HomeActivity) getActivity()).getRecentPostList().clone());
                    Bundle b = new Bundle();
                    b.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_FROM_RECENT);
                    b.putInt(ShowPostViewPagerFragment.SHOW_INDEX, position);
                    gotoPager(ShowPostViewPagerFragment.class, b);
                }

                @Override
                public void onItemTouch(MotionEvent event) {

                }
            });
            mFriendRecentPostAdapter.setOnUploadPostListener(new RecentPostAdapter.OnUploadPostListener() {
                @Override
                public void onFailedPostClick(PostBean postBean) {
                    showFailedPostDetail(postBean);
                }
            });
            mUserRecentPostView.setAdapter(mFriendRecentPostAdapter);
        }
        return mFriendRecentPostAdapter;
    }

    private void addTags() {
        ArrayList<TagBean> tagBeanList = ((HomeActivity) getActivity()).getTagBeanList();
        if (tagBeanList == null || tagBeanList.isEmpty()) {
            return;
        }
        mSelectEmoHorizontalScrollView.addEmosInHomeFragment(tagBeanList);
    }

    private void addTopic(View view) {
        mSelectTopicHorizontalScrollView = (SelectItemHorizontalScrollView) view.findViewById(R.id.selectTopicView);
        ArrayList<TopicBean> topicList = ((HomeActivity) getActivity()).getTopicList();
        if (topicList == null || topicList.isEmpty()) {
            return;
        }
        mSelectTopicHorizontalScrollView.addTopics(topicList);
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.tvSearch, "hm_btn_search");
        setOnlineText(R.id.tvExplore, "hm_txt_explore");
        setOnlineText(R.id.tvFeaturing, "hm_txt_featuring");
        setOnlineText(R.id.tvRecent, "hm_txt_recent");
    }

    public void resetRecentPostView(ArrayList<PostBean> recentPostList) {
        if (getView() != null) {
            getFriendRecentPostAdapter().setPostBeanList(recentPostList);
        }
    }

    public void resetTopics() {
        if (getView() != null) {
            addTopic(getView());
        }
    }


    public void resetTags() {
        if (getView() != null) {
            addTags();
        }
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCamera:
                ((HomeActivity) getActivity()).toCameraFragment();
                break;
            case R.id.rlSearch:
                gotoPager(SearchFragment.class, null);
                break;

        }
    }

    @Override
    public void onRefresh() {
        ((HomeActivity) getActivity()).getHomeFragmentData(mHandler);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (getView() == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
                    swipeLayout.setRefreshing(false);
                    break;

            }
        }
    };


    @Override
    public void onJobDone(KittyJob job) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            resetRecentPostView(((HomeActivity) getActivity()).getRecentPostList());
        }
    }

    @Override
    public void onNewJobIn(KittyJob job) {
        if (getView() == null) {
            return;
        }
        if (job != null) {
            YeemosTask yeemosTask;
            if (job instanceof YeemosTask) {
                yeemosTask = (YeemosTask) job;
                yeemosTask.addListener(this);
                if (yeemosTask.getYeemosTask() == YeemosTask.YeemosTaskType.CREATE_POST_BEAR_TASK) {
                    try {
                        JSONObject jb = new JSONObject(yeemosTask.getRequestBean().getData());
                        PostBean postBean = new Gson().fromJson(jb.get("subObject").toString(), PostBean.class);
                        ArrayList<PostBean> list = ((HomeActivity) getActivity()).getRecentPostList();
                        for (PostBean pb : list) {
                            if (postBean.getId().equals(pb.getId())) {
                                JobDaddy.getInstance().cancelJob(job);
                                return;
                            }
                        }
                        addUpLoadingPost(postBean);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onOldJobRestore(List<KittyJob> job) {
        if (getView() == null) {
            return;
        }
        if (job != null) {
            YeemosTask yeemosTask;
            for (KittyJob task : job) {
                if (task instanceof YeemosTask) {
                    yeemosTask = (YeemosTask) task;
                    yeemosTask.addListener(this);
                    if (yeemosTask.getYeemosTask() == YeemosTask.YeemosTaskType.CREATE_POST_BEAR_TASK) {
                        try {
                            JSONObject jb = new JSONObject(yeemosTask.getRequestBean().getData());
                            PostBean postBean = new Gson().fromJson(jb.get("subObject").toString(), PostBean.class);
                            ArrayList<PostBean> list = ((HomeActivity) getActivity()).getRecentPostList();
                            for (PostBean pb : list) {
                                if (postBean.getId().equals(pb.getId())) {
                                    JobDaddy.getInstance().cancelJob(task);
                                    return;
                                }
                            }
                            addUpLoadingPost(postBean);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onJobCancel(KittyJob job) {

    }

    @Override
    public void onDataChange(int dataType, Object data, int oprateType) {
        if (dataType == 1) {
            if (oprateType == 0) {
                ArrayList<PostBean> list = ((HomeActivity) getActivity()).getRecentPostList();
                for (PostBean postBean : list) {
                    if (postBean.getId().equals(((PostBean) data).getId())) {
                        list.remove(postBean);
                        break;
                    }
                }
            } else {
                if (oprateType == 1 || oprateType == 2) {
                    ArrayList<PostBean> list = ((HomeActivity) getActivity()).getRecentPostList();
                    int size = list.size();
                    for (int i = 0; i < size; i++) {
                        PostBean postBean = list.get(i);
                        if (postBean.getId().equals(((PostBean) data).getId())) {
                            list.remove(postBean);
                            break;
                        }
                    }
                    getFriendRecentPostAdapter().notifyDataSetChanged();
                } else if (oprateType == 6) {   //编辑post
                    PostBean postData = (PostBean) data;
                    String postId = postData.getId();
                    ArrayList<PostBean> list = ((HomeActivity) getActivity()).getRecentPostList();
                    for (int i = 0; i < list.size(); i++) {
                        PostBean postBean = list.get(i);
                        if (postBean.getId().equals(postId)) {
                            postBean.setCreateTime(postData.getCreateTime());
                            postBean.setIsPrivate(postData.getIsPrivate());
                            postBean.setIsAnonymity(postData.getIsAnonymity());
                            postBean.setText(postData.getText());
                            postBean.setTags(postData.getTags());
                            postBean.setExpiredType(postData.getExpiredType());
                            getFriendRecentPostAdapter().notifyDataSetChanged();
                            return;
                        }
                    }

                } else {
                    BasicUser dataOwer = ((PostBean) data).getOwner();
                    ArrayList<PostBean> list = ((HomeActivity) getActivity()).getRecentPostList();
                    int size = list.size();
                    for (int i = 0; i < size; i++) {
                        PostBean postBean = list.get(i);
                        if (postBean.getOwner().getUserId().equals(dataOwer.getUserId())) {
                            postBean.getOwner().resetBasicUser(dataOwer);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void start(final ITask task) {
        if (getActivity() != null && getView() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    YeemosTask yeemosTask;
                    if (task != null && task instanceof YeemosTask) {
                        yeemosTask = (YeemosTask) task;
                        if (yeemosTask.getYeemosTask() == YeemosTask.YeemosTaskType.CREATE_POST_BEAR_TASK) {
                            try {
                                JSONObject jb = new JSONObject(yeemosTask.getRequestBean().getData());
                                PostBean postBean = new Gson().fromJson(jb.get("subObject").toString(), PostBean.class);
                                ArrayList<PostBean> list = ((HomeActivity) getActivity()).getRecentPostList();
                                for (PostBean pb : list) {
                                    if (postBean.getId().equals(pb.getId())) {
                                        JobDaddy.getInstance().cancelJob(yeemosTask);
                                        return;
                                    }
                                }
                                addUpLoadingPost(postBean);
                                int size = getUploadingPosts().size();
                                for (int i = 0; i < size; ++i) {
                                    if (getUploadingPosts().get(i).getId().equals(postBean.getId())) {
                                        getUploadingPosts().get(i).setUploadState(1);//0:表示上传成功1:表示正在上传2:表示上传失败
                                        getUploadingPosts().get(i).setUploadProgress(0);
                                        getFriendRecentPostAdapter().notifyDataSetChanged();
                                        return;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void error(final ITask task) {
        if (getActivity() != null && getView() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (task != null && task instanceof YeemosTask) {
                        YeemosTask yeemosTask = (YeemosTask) task;
                        if (yeemosTask.getYeemosTask() == YeemosTask.YeemosTaskType.CREATE_POST_BEAR_TASK) {
                            try {
                                JSONObject jb = new JSONObject(yeemosTask.getRequestBean().getData());
                                PostBean postBean = new Gson().fromJson(jb.get("subObject").toString(), PostBean.class);
                                ArrayList<PostBean> list = ((HomeActivity) getActivity()).getRecentPostList();
                                for (PostBean pb : list) {
                                    if (postBean.getId().equals(pb.getId())) {
                                        JobDaddy.getInstance().cancelJob(yeemosTask);
                                        return;
                                    }
                                }
                                addUpLoadingPost(postBean);
                                getUploadFailedTasks().put(postBean.getId(), task);
                                int size = getUploadingPosts().size();
                                for (int i = 0; i < size; ++i) {
                                    if (getUploadingPosts().get(i).getId().equals(postBean.getId())) {
                                        getUploadingPosts().get(i).setUploadState(2);//0:表示上传成功1:表示正在上传2:表示上传失败
                                        getUploadingPosts().get(i).setUploadProgress(0);
                                        getFriendRecentPostAdapter().notifyDataSetChanged();
                                        return;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void success(final ITask task) {
        if (getActivity() != null) {
            YeemosTask yeemosTask;
            if (task != null && task instanceof YeemosTask) {
                yeemosTask = (YeemosTask) task;
                if (yeemosTask.getYeemosTask() == YeemosTask.YeemosTaskType.CREATE_POST_BEAR_TASK) {
                    try {
                        JSONObject jb = new JSONObject(yeemosTask.getRequestBean().getData());
                        final PostBean postBean = new Gson().fromJson(jb.get("subObject").toString(), PostBean.class);
                        postBean.setUploadState(0);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getView() != null) {
                                    int size = getUploadingPosts().size();
                                    for (int i = 0; i < size; ++i) {
                                        if (getUploadingPosts().get(i).getId().equals(postBean.getId())) {
                                            getUploadingPosts().remove(i);
                                            break;
                                        }
                                    }
                                }
                                ((HomeActivity) getActivity()).addRecentPost(postBean);
                                ((HomeActivity) getActivity()).addUserPost(postBean);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void progress(ITask task, final int progress) {
//        if (getView() != null) {
//            YeemosTask yeemosTask;
//            if (task != null && task instanceof YeemosTask) {
//                yeemosTask = (YeemosTask) task;
//                if (yeemosTask.getYeemosTask() == YeemosTask.YeemosTaskType.CREATE_POST_BEAR_TASK) {
//                    try {
//                        JSONObject jb = new JSONObject(yeemosTask.getRequestBean().getData());
//                        final PostBean postBean = new Gson().fromJson(jb.get("subObject").toString(), PostBean.class);
//                        postBean.setUploadState(0);
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (getView() != null) {
//                                    int size = getUploadingPosts().size();
//                                    for (int i = 0; i < size; ++i) {
//                                        if (getUploadingPosts().get(i).getId().equals(postBean.getId())) {
//                                            getFriendRecentPostAdapter().notifyDataSetChanged();
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                        });
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }


    //点击上传失败的post
    private void showFailedPostDetail(final PostBean postBean) {
        MorePopupWindow popUpWindow = new MorePopupWindow((Activity) getContext(), new MorePopupWindow.MorePopupWindowClickListener() {
            @Override
            public void onThirdBtnClicked() {
            }

            @Override
            public void onSecondBtnClicked() {
                ITask task = getUploadFailedTasks().get(postBean.getId());
                if (task != null) {
                    JobDaddy.getInstance().cancelJob((YeemosTask) task);
                    getUploadFailedTasks().remove(postBean.getId());
                }
                getUploadingPosts().remove(postBean);
                getFriendRecentPostAdapter().notifyDataSetChanged();
            }

            @Override
            public void onFirstBtnClicked() {
                ITask task = getUploadFailedTasks().get(postBean.getId());
                if (task != null) {
                    JobDaddy.getInstance().reStartJob((YeemosTask) task);
                    getUploadFailedTasks().remove(postBean.getId());
                }
            }

            @Override
            public void onFourthBtnClicked() {

            }

            @Override
            public void onCancelBtnClicked() {
                // TODO Auto-generated method stub
            }
        }, Constants.MORE_POPUPWINDOW_HANDLE_FAILED_POST);
        popUpWindow.initView(postBean);
        popUpWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }
}
