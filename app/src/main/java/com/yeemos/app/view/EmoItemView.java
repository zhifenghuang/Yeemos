package com.yeemos.app.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.TagBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.fragment.ShowPostViewPagerFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.R;
import java.util.ArrayList;

/**
 * Created by gigabud on 16-9-27.
 */
public class EmoItemView extends RelativeLayout implements View.OnClickListener {

    private TagBean mTagBean;

    public EmoItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.emo_item_view, this);
        setOnClickListener(this);
    }

    public void setTagBean(TagBean tagBean) {
        mTagBean = tagBean;
        if(tagBean.getPostNums() <= 0){
            getEmoItemView().setAlpha(0.5f);
        }

    }

    private ProgressBar getProgressBar() {
        return (ProgressBar) findViewById(R.id.progressBar);
    }

    private ImageView getEmoItemView() {
        return (ImageView) findViewById(R.id.emoIcon);
    }

    @Override
    public void onClick(View v) {

        if(mTagBean.getPostNums() > 0){
            DataManager.getInstance().setCurTag(mTagBean);
            Bundle b = new Bundle();
            b.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_BY_TAG);
            ((BaseActivity) getContext()).gotoPager(ShowPostViewPagerFragment.class, b);
        }else {
            setClickable(false);
            getProgressBar().setVisibility(VISIBLE);
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    final ServerResultBean<ArrayList<PostBean>> serverResultBean = DataManager.getInstance()
                            .searchPostByTag(0, mTagBean, GBSConstants.SortType.SortType_Time);
                    ((BaseActivity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getProgressBar().setVisibility(GONE);
                            if(serverResultBean != null && serverResultBean.getData() != null && serverResultBean.getData().size() > 0){
                                getEmoItemView().setAlpha(1.0f);
                                mTagBean.setPostNums(serverResultBean.getData().size());
                            }
                            setClickable(true);
                        }
                    });
                }
            });
        }
    }
}
