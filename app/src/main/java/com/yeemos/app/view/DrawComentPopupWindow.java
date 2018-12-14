package com.yeemos.app.view;

import android.app.Activity;
import android.view.View;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.adapter.DrawCommentUserListAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by gigabud on 17-1-19.
 */

public class DrawComentPopupWindow extends PostPopupWindow {

    private ArrayList<BasicUser> arrayList;
    private DrawCommentView mDrawCommentView;
    private PostBean postBean;

    public void setPostBean(PostBean postBean, DrawCommentView mDrawCommentView) {
        this.postBean = postBean;
        this.mDrawCommentView = mDrawCommentView;
    }

    public DrawComentPopupWindow(Activity context) {
        super(context);

//        ddl_ttl_Doodle
//        ddl_btn_hideall
//        ddl_btn_showall
        getTvPostView().setText(ServerDataManager.getTextFromKey("ddl_ttl_Doodle"));
        getTvPostView().setTextColor(context.getResources().getColor(R.color.color_45_223_227));


        getTopButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataManager.getInstance().hideAll(postBean);
                if (postBean.isAllHide()) {
                    getTopButton().setText(ServerDataManager.getTextFromKey("ddl_btn_showall"));
                } else {
                    getTopButton().setText(ServerDataManager.getTextFromKey("ddl_btn_hideall"));
                }
                mDrawCommentView.showOrHideAllImageComment(postBean.isAllHide());
                ((DrawCommentUserListAdapter) getListView().getAdapter()).hideOrShowAllUserDrawed(postBean.isAllHide() ? 1 : 0);
            }
        });
//        getNoFeelText().setText(ServerDataManager.getTextFromKey("pllrct_txt_nofeel"));
    }


    @Override
    protected void getData() {
        super.getData();
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //获取数据
                JSONObject jb = new JSONObject();
                try {
                    jb.put("token", MemberShipManager.getInstance().getToken());
                    jb.put("id", postBean.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final ServerResultBean<PostBean> resObj = DataManager.getInstance().getPainUserFromPost(0, jb.toString());
                BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getSwipeRefreshLayout().isRefreshing()) {
                            getSwipeRefreshLayout().setRefreshing(false);
                        }
                    }
                });
                if (resObj == null || resObj.getData() == null) {
                    return;
                }
                postBean.setAllHide(resObj.getData().isAllHide() ? 1 : 0);
                postBean.setPaintUsers(resObj.getData().getPaintUsers());
                arrayList = resObj.getData().getPaintUsers();

                BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (BaseApplication.getCurFragment().getActivity() == null) {
                            return;
                        }
                        if (arrayList == null || arrayList.size() <= 0) {
                            getTopButton().setVisibility(View.GONE);
                        } else {
                            if (postBean.isAllHide()) {
                                getTopButton().setText(ServerDataManager.getTextFromKey("ddl_btn_showall"));
                            } else {
                                getTopButton().setText(ServerDataManager.getTextFromKey("ddl_btn_hideall"));
                            }
                            getTopButton().setVisibility(View.VISIBLE);
                        }
                        DrawCommentUserListAdapter drawCommentUserListAdapter;
                        if (getListView().getAdapter() == null) {
                            drawCommentUserListAdapter = new DrawCommentUserListAdapter(BaseApplication.getAppContext());
                            getListView().setAdapter(drawCommentUserListAdapter);
                        } else {
                            drawCommentUserListAdapter = (DrawCommentUserListAdapter) getListView().getAdapter();
                        }
                        drawCommentUserListAdapter.setPostBean(postBean, mDrawCommentView, DrawComentPopupWindow.this);
                        drawCommentUserListAdapter.setArrayList(arrayList);
                        drawCommentUserListAdapter.notifyDataSetChanged();

                    }
                });
            }
        });
    }

}
