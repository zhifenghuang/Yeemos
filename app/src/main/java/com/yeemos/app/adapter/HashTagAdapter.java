package com.yeemos.app.adapter;

import android.content.Context;

import com.gbsocial.BeansBase.PostBean;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.fragment.BaseFragment;
import com.yeemos.app.fragment.HashTagsFragment;
import com.yeemos.app.fragment.SharesFragment;
import com.yeemos.app.manager.DataManager;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-12-6.
 */

public class HashTagAdapter extends RecentPostAdapter {

    public HashTagAdapter(Context context) {
        super(context);
    }

    @Override
    protected void getMoreData(int position) {
        if (position == getCount() / 2) {
            BaseFragment baseFragment = BaseApplication.getCurFragment();
            if (baseFragment.getClass().isAssignableFrom(HashTagsFragment.class)) {
                ((HashTagsFragment) baseFragment).loadMoreData();
            } else if (baseFragment.getClass().isAssignableFrom(SharesFragment.class)) {
                ((SharesFragment) baseFragment).loadMoreData();
            }
        }
    }

    @Override
    public void setPostBeanList(ArrayList<PostBean> postBeanList) {
        addPosts(postBeanList);
    }


    public void setPostBeanListDirect(ArrayList<PostBean> postBeanList) {
        mPostBeanList = postBeanList;
        DataManager.getInstance().setShowPostList(mPostBeanList);
        notifyDataSetChanged();
    }

    private void addPosts(ArrayList<PostBean> postList) {
        if (mPostBeanList == null || mPostBeanList.isEmpty()) {
            mPostBeanList = postList;
        } else {
            ArrayList<PostBean> addList = new ArrayList<>();
            boolean isAdd;
            for (PostBean pb : postList) {
                isAdd = true;
                for (PostBean postBean : mPostBeanList) {
                    if (pb.getId().equals(postBean.getId())) {
                        isAdd = false;
                        break;
                    }
                }
                if (isAdd) {
                    addList.add(pb);
                }
            }
            mPostBeanList.addAll(addList);
            addList.clear();
            addList = null;
        }
        DataManager.getInstance().setShowPostList(mPostBeanList);
        notifyDataSetChanged();
    }

    public void removePost(PostBean postBean) {
        for (PostBean mPostBean : mPostBeanList) {
            if (postBean.getId().equals(mPostBean.getId())) {
                mPostBeanList.remove(mPostBean);
                break;
            }
        }
        notifyDataSetChanged();
    }
}
