package com.yeemos.app.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.FriendGroup;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.adapter.ChooseFriendAdapter;
import com.yeemos.app.manager.DataManager;

import java.util.ArrayList;

/**
 * Created by gigabud on 17-3-1.
 */

public class ChooseFriendView extends Dialog implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private View view;
    private AddGroupAndUserListener addGroupAndUserListener;

    private PostBean postBean;

    public void setAddGroupAndUserListener(AddGroupAndUserListener addGroupAndUserListener) {
        this.addGroupAndUserListener = addGroupAndUserListener;
    }

    public void setPostBean(PostBean postBean) {
        this.postBean = postBean;
    }

    public interface AddGroupAndUserListener {
        void AddGroupAndUserToPoast(ArrayList<FriendGroup> groupArrayList, ArrayList<Integer> userArrayList);
    }

    public ChooseFriendView(Context context, int themeResId) {
        super(context, themeResId);
        view = LayoutInflater.from(context).inflate(getLayoutID(), null);
        setContentView(view);
        getSwipeRefreshLayout().setOnRefreshListener(this);
        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);
        setCanceledOnTouchOutside(true);
        initView();
    }

    protected int getLayoutID() {
        return R.layout.layout_choose_friend_view;
    }

    public void setDialogSize(int width, int height) {
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = width; //设置宽度
        lp.height = height;
        // 将属性设置给窗体
        dialogWindow.setAttributes(lp);
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void getData(final boolean isFromCache) {

        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(isFromCache);
                final ArrayList<FriendGroup> allFriendGroup = DataManager.getInstance().getAllGroup(isFromCache);
                if (allFriendGroup.size() <= 0) {
                    allFriendGroup.add(new FriendGroup().setTrueGroup(false));//FriendGroup 确保第一行显示
                }
                BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSwipeRefreshLayout().setRefreshing(false);

                        getChooseFriendAdapter().setObjectArrayList(allFriendGroup, allFriends);

                        getChooseFriendAdapter().setGroupUserAndRefresh(postBean.getFriendGroups(), postBean.getFriendUsers());
                    }
                });
            }
        });
    }

    private ChooseFriendAdapter getChooseFriendAdapter() {
        ChooseFriendAdapter mChooseFriendAdapter = (ChooseFriendAdapter) getListView().getAdapter();
        if (mChooseFriendAdapter == null) {
            mChooseFriendAdapter = new ChooseFriendAdapter(ChooseFriendView.this);
            mChooseFriendAdapter.setChooseItemListener(new ChooseFriendAdapter.ChooseItemListener() {
                @Override
                public void onChooseItem(boolean noSelectItem) {
                    if (noSelectItem) {
                        getbtnOK().setClickable(false);
                        getbtnOK().setAlpha(0.5f);
                    } else {
                        getbtnOK().setClickable(true);
                        getbtnOK().setAlpha(1f);
                    }
                }
            });
            getListView().setAdapter(mChooseFriendAdapter);
        }
        return mChooseFriendAdapter;
    }

    protected void initView() {
        getChooseFriendTitle().setText(ServerDataManager.getTextFromKey("chsfrnd_ttl_choosefriends"));
        getCancel().setText(ServerDataManager.getTextFromKey("pblc_btn_cancel"));
        getbtnOK().setText(ServerDataManager.getTextFromKey("pblc_btn_ok"));
        getCancel().setOnClickListener(this);
        getbtnOK().setOnClickListener(this);
        getbtnOK().setClickable(false);
    }

    public View getContentView() {
        return view;
    }

    private TextView getChooseFriendTitle() {
        return (TextView) getContentView().findViewById(R.id.chooseFriendTitle);
    }

    private TextView getCancel() {
        return (TextView) getContentView().findViewById(R.id.cancel);
    }

    private TextView getbtnOK() {
        return (TextView) getContentView().findViewById(R.id.btnOK);
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getContentView().findViewById(R.id.swipeRefreshLayout);
    }

    private ListView getListView() {
        return (ListView) getContentView().findViewById(R.id.listView);
    }

    @Override
    public void onRefresh() {
        getData(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.btnOK:
                filterArrayList();
                dismiss();
                break;
        }
    }

    private void filterArrayList() {
        ArrayList<Object> arrayList = getChooseFriendAdapter().getObjectArrayList();
        ArrayList<FriendGroup> groupArrayList = new ArrayList<>();
        ArrayList<Integer> userArrayList = new ArrayList<>();

        for (Object obj : arrayList) {
            if (obj.getClass().isAssignableFrom(FriendGroup.class)) {
                if (((FriendGroup) obj).isSelect()) {
                    groupArrayList.add((FriendGroup) obj);
                }
            } else {
                if (((BasicUser) obj).isSelect()) {
                    userArrayList.add(Integer.valueOf(((BasicUser) obj).getUserId()));
                }
            }
        }
        addGroupAndUserListener.AddGroupAndUserToPoast(groupArrayList, userArrayList);

    }

}
