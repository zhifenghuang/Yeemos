package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.gbsocial.BeansBase.TopicBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.fragment.ShowPostViewPagerFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;
import com.yeemos.app.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by gigabud on 16-5-18.
 */
public class TopicItemView extends RelativeLayout implements View.OnClickListener {
    private TopicBean topicBean;

    public TopicItemView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.topic_item_view, this);
        setOnClickListener(this);
    }

    public void setTopic(TopicBean topicBean) {
        this.topicBean = topicBean;
        if (topicBean.getSubObjectNums() <= 0) {
            getRoundRectImageView().setAlpha(0.5f);
        }
        getRoundRectImageView().setRoundRadius(3);
        try {
            String url = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(topicBean.getImage(), "utf-8"), URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
            Utils.loadImage(BaseApplication.getAppContext(),0,url,getRoundRectImageView());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        TextView tv = (TextView) findViewById(R.id.tvTopicName);
        tv.setText(topicBean.getText());

    }

    private RoundRectImageView getRoundRectImageView() {
        return (RoundRectImageView) findViewById(R.id.topicIcon);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {

        if (topicBean == null) {
            return;
        }
        if (topicBean.getSubObjectNums() > 0) {
            DataManager.getInstance().setCurTopic(topicBean);
            Bundle b = new Bundle();
            b.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_BY_TOPIC);
            ((BaseActivity) getContext()).gotoPager(ShowPostViewPagerFragment.class, b);
        } else {
            setClickable(false);
            getProgressBar().setVisibility(VISIBLE);
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    final ServerResultBean<TopicBean> resultBean = DataManager.getInstance()
                            .getPostFromTopic(0, topicBean, GBSConstants.SortType.SortType_Character);
                    ((BaseActivity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getProgressBar().setVisibility(GONE);
                            if (resultBean != null && resultBean.isSuccess() && resultBean.getData() != null
                                    && resultBean.getData().getSubObjects() != null && resultBean.getData().getSubObjects().size() > 0) {
                                getRoundRectImageView().setAlpha(1.0f);
                                topicBean.setSubObjectNums(resultBean.getData().getSubObjects().size());
//                                DataManager.getInstance().setCurTopic(topicBean);
//                                Bundle b = new Bundle();
//                                b.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_BY_TOPIC);
//                                ((BaseActivity) getContext()).gotoPager(ShowPostViewPagerFragment.class, b);
                            }
                            setClickable(true);
                        }
                    });

                }
            });
        }
    }
}
