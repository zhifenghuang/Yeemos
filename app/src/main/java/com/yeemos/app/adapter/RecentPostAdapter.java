package com.yeemos.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.TagBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.interfaces.OnItemClickListener;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CountDownView;
import com.yeemos.app.view.CustomUrlImageView;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-11-30.
 */

public class RecentPostAdapter extends BaseAdapter {
    protected Context context;
    protected ArrayList<PostBean> mPostBeanList, mUploadingPostBeanList;
    private OnItemClickListener mOnItemClickListener;
    private boolean mHasData = true;
    private boolean mIsGetingData;

    private boolean mIsRecentPostList;

    private OnUploadPostListener mOnUploadPostListener;

    public RecentPostAdapter(Context context) {
        this.context = context;
        mIsGetingData = false;
    }

    public void setPostBeanList(ArrayList<PostBean> postBeanList) {
        mHasData = (postBeanList.size() != 0 && postBeanList.size() % GBSConstants.PAGE_NUMBER_COMMENTS_POP == 0);
        mPostBeanList = postBeanList;
        notifyDataSetChanged();
    }

    public ArrayList<PostBean> getmPostBeanList() {
        return mPostBeanList;
    }

    public void setUploadingPostBeanList(ArrayList<PostBean> uploadingPostBeanList) {
        mUploadingPostBeanList = uploadingPostBeanList;
        notifyDataSetChanged();
    }

    public void setOnUploadPostListener(OnUploadPostListener onUploadPostListener) {
        mOnUploadPostListener = onUploadPostListener;
    }

    public void setIsRecentPostList(boolean isRecentPostList) {
        mIsRecentPostList = isRecentPostList;
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getCount() {
        return (int) Math.ceil(getAllPostSize() / 6.0);
    }

    private int getUploadingPostSize() {
        return (mUploadingPostBeanList == null ? 0 : mUploadingPostBeanList.size());
    }

    private int getAllPostSize() {
        int count = 0;
        count += getUploadingPostSize();
        count += (mPostBeanList == null ? 0 : mPostBeanList.size());
        return count;
    }

    @Override
    public Object getItem(int position) {
        if (mUploadingPostBeanList != null && mUploadingPostBeanList.size() > position) {
            return mUploadingPostBeanList.get(position);
        }
        return mPostBeanList.get(position - getUploadingPostSize());
    }

    @Override
    public long getItemId(int position) {
        return (int) Math.ceil(getCount() / 6);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int width = parent.getWidth() / 3;
        int height = (int) (width * 4f / 3 + 0.5f);
        ViewHolder hold;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_recent_post_odd_item, parent, false);
            hold = new ViewHolder(convertView);
            convertView.setTag(hold);
        } else {
            Object obj = convertView.getTag();
            if (obj != null) {
                hold = (ViewHolder) obj;
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.layout_recent_post_odd_item, parent, false);
                hold = new ViewHolder(convertView);
                convertView.setTag(hold);
            }
        }
        hold.setViewWH(width, height);
        hold.fillData(position);
        getMoreData(position);
        return convertView;
    }


    protected void getMoreData(int position) {
        if (position == getCount() / 2 && mHasData && !mIsGetingData) {
            mIsGetingData = true;
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    getServerData();
                }
            });
        }

    }

    private void getServerData() {
        if (!mHasData) {
            return;
        }
        ServerResultBean<ArrayList<PostBean>> serverBean1 = DataManager.getInstance().getHomePostList(mPostBeanList.size(), 0, false);
        if (serverBean1 != null && serverBean1.getData() != null) {
            final ArrayList<PostBean> recentPostList = serverBean1.getData();
            if (recentPostList.size() < GBSConstants.PAGE_NUMBER_COMMENTS_POP) {
                mHasData = false;
            }
            mPostBeanList.addAll(recentPostList);
            mHasData = (mPostBeanList.size() != 0 && mPostBeanList.size() % GBSConstants.PAGE_NUMBER_COMMENTS_POP == 0);
            if (BaseApplication.getCurFragment() == null || BaseApplication.getCurFragment().getActivity() == null) {
                return;
            }
            BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIsGetingData = false;
                    notifyDataSetChanged();
                }
            });
        } else {
            mIsGetingData = false;
            mHasData = false;
        }
    }

    class ViewHolder implements View.OnClickListener {

        private LinearLayout topLayoutParent1, topLayoutParent2;
        private RelativeLayout topLayoutLeft, topLayoutRight1, topLayoutRight2,
                bootLayout1, bootLayout2, bootLayout3;
        private int position;
        private int width, height;

        public ViewHolder(View cView) {
            topLayoutParent1 = (LinearLayout) cView.findViewById(R.id.topLayoutParent1);
            topLayoutParent2 = (LinearLayout) cView.findViewById(R.id.topLayoutParent2);
            bootLayout1 = (RelativeLayout) cView.findViewById(R.id.bootLayout1);
            bootLayout2 = (RelativeLayout) cView.findViewById(R.id.bootLayout2);
            bootLayout3 = (RelativeLayout) cView.findViewById(R.id.bootLayout3);
        }

        public void setViewWH(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public void fillData(int position) {
            this.position = position;

            if (position % 2 == 0) {
                topLayoutParent1.setVisibility(View.VISIBLE);
                topLayoutParent2.setVisibility(View.GONE);
                topLayoutLeft = (RelativeLayout) topLayoutParent1.findViewById(R.id.topLayoutLeft);
                topLayoutRight1 = (RelativeLayout) topLayoutParent1.findViewById(R.id.topLayoutRight1);
                topLayoutRight2 = (RelativeLayout) topLayoutParent1.findViewById(R.id.topLayoutRight2);
            } else {
                topLayoutParent1.setVisibility(View.GONE);
                topLayoutParent2.setVisibility(View.VISIBLE);
                topLayoutLeft = (RelativeLayout) topLayoutParent2.findViewById(R.id.topLayoutLeft);
                topLayoutRight1 = (RelativeLayout) topLayoutParent2.findViewById(R.id.topLayoutRight1);
                topLayoutRight2 = (RelativeLayout) topLayoutParent2.findViewById(R.id.topLayoutRight2);
            }
            setViewWH(topLayoutLeft, gettopLayoutLeftUrlImageView(), width * 2, height * 2);
            setViewWH(topLayoutRight1, gettopLayoutRight1UrlImageView(), width, height);
            setViewWH(topLayoutRight2, gettopLayoutRight2UrlImageView(), width, height);
            setViewWH(bootLayout1, getbootLayout1UrlImageView(), width, height);
            setViewWH(bootLayout2, getbootLayout2UrlImageView(), width, height);
            setViewWH(bootLayout3, getbootLayout3UrlImageView(), width, height);
            int size = getAllPostSize();
            //,第一个和第二个的位置由position决定
            if (position % 2 == 0) {
                setViewVisible(topLayoutLeft);
                showView(topLayoutLeft, gettopLayoutLeftUrlImageView(), gettopLayoutLeftTextView(),
                        gettopLayoutLeftImageView(), position * 6, false);
                if (position * 6 + 2 < size) {
                    setViewVisible(topLayoutRight1);
                    setViewVisible(topLayoutRight2);
                    showView(topLayoutRight1, gettopLayoutRight1UrlImageView(), gettopLayoutRight1TextView(),
                            gettopLayoutRight1ImageView(), position * 6 + 1, true);
                    showView(topLayoutRight2, gettopLayoutRight2UrlImageView(), gettopLayoutRight2TextView(),
                            gettopLayoutRight2ImageView(), position * 6 + 2, true);
                } else if (position * 6 + 1 < size) {
                    setViewVisible(topLayoutRight1);
                    setViewInvisible(topLayoutRight2);
                    showView(topLayoutRight1, gettopLayoutRight1UrlImageView(), gettopLayoutRight1TextView(),
                            gettopLayoutRight1ImageView(), position * 6 + 1, true);
                } else if (position * 6 < size) {
                    setViewInvisible(topLayoutRight1);
                    setViewInvisible(topLayoutRight2);
                }
            } else {
                setViewVisible(topLayoutRight1);
                showView(topLayoutRight1, gettopLayoutRight1UrlImageView(), gettopLayoutRight1TextView(),
                        gettopLayoutRight1ImageView(), position * 6, true);
                if (position * 6 + 2 < size) {
                    setViewVisible(topLayoutLeft);
                    setViewVisible(topLayoutRight2);
                    showView(topLayoutLeft, gettopLayoutLeftUrlImageView(), gettopLayoutLeftTextView(),
                            gettopLayoutLeftImageView(), position * 6 + 1, false);
                    showView(topLayoutRight2, gettopLayoutRight2UrlImageView(), gettopLayoutRight2TextView(),
                            gettopLayoutRight2ImageView(), position * 6 + 2, true);
                } else if (position * 6 + 1 < size) {
                    setViewVisible(topLayoutLeft);
                    setViewInvisible(topLayoutRight2);
                    showView(topLayoutLeft, gettopLayoutLeftUrlImageView(), gettopLayoutLeftTextView(),
                            gettopLayoutLeftImageView(), position * 6 + 1, false);
                } else if (position * 6 < size) {
                    setViewGone(topLayoutLeft);
                    setViewGone(topLayoutRight2);
                }
            }

            if (position * 6 + 3 < size) {
                showView(bootLayout1, getbootLayout1UrlImageView(), getbootLayout1TextView(),
                        getbootLayout1ImageView(), position * 6 + 3, true);
                setViewVisible(bootLayout1);
                if (position * 6 + 4 < size) {
                    setViewVisible(bootLayout2);
                    showView(bootLayout2, getbootLayout2UrlImageView(), getbootLayout2TextView(),
                            getbootLayout2ImageView(), position * 6 + 4, true);
                } else {
                    setViewInvisible(bootLayout2);
                }
                if (position * 6 + 5 < size) {
                    setViewVisible(bootLayout3);
                    showView(bootLayout3, getbootLayout3UrlImageView(), getbootLayout3TextView(),
                            getbootLayout3ImageView(), position * 6 + 5, true);
                } else {
                    setViewInvisible(bootLayout3);
                }

            } else {
                setViewGone(bootLayout1);
                setViewGone(bootLayout2);
                setViewGone(bootLayout3);
            }

        }

        private void setViewInvisible(View view) {
            view.setVisibility(View.INVISIBLE);
            view.setOnClickListener(null);
        }

        private void setViewVisible(View view) {
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(this);
        }

        private void setViewGone(View view) {
            view.setVisibility(View.GONE);
            view.setOnClickListener(null);
        }

        private void setViewWH(RelativeLayout relativeLayout, CustomUrlImageView urlImageView, int width, int height) {
            int padding = Utils.dip2px(context, 1);
            relativeLayout.setPadding(padding, padding, padding, padding);
            urlImageView.setViewWH(width - 2 * padding, height - 2 * padding);
            urlImageView.setNeedRouctRect(true);
        }

        private void showView(RelativeLayout relativeLayout, CustomUrlImageView urlImageView, TextView textView,
                              ImageView imageview, int position, boolean isShowThumb) {

            PostBean postBean = (PostBean) getItem(position);
            urlImageView.setTag(R.id.post_bean, postBean);
            urlImageView.setPostBean(postBean, isShowThumb);
            relativeLayout.findViewById(R.id.ivUploadFailed).setTag(postBean);
            int uploadState = postBean.getUploadState();//0:表示上传成功1:表示正在上传2:表示上传失败
            if (uploadState == 0) {
                relativeLayout.findViewById(R.id.uploadBg).setVisibility(View.GONE);
                relativeLayout.findViewById(R.id.uploadProgressView).setVisibility(View.GONE);
                relativeLayout.findViewById(R.id.ivUploadFailed).setVisibility(View.GONE);
                relativeLayout.findViewById(R.id.ivUploadFailed).setOnClickListener(null);
            } else if (uploadState == 1) {
                relativeLayout.findViewById(R.id.uploadBg).setVisibility(View.VISIBLE);
                CountDownView countDownView = (CountDownView) relativeLayout.findViewById(R.id.uploadProgressView);
                countDownView.setVisibility(View.VISIBLE);
                countDownView.setIsAddProgress(true, postBean);
                countDownView.setPaintColor(context.getResources().getColor(R.color.color_45_223_227));
                countDownView.setProgress(100, postBean.getUploadProgress());
                relativeLayout.findViewById(R.id.ivUploadFailed).setVisibility(View.GONE);
                relativeLayout.findViewById(R.id.ivUploadFailed).setOnClickListener(null);
            } else {
                relativeLayout.findViewById(R.id.uploadBg).setVisibility(View.VISIBLE);
                relativeLayout.findViewById(R.id.uploadProgressView).setVisibility(View.GONE);
                relativeLayout.findViewById(R.id.ivUploadFailed).setVisibility(View.VISIBLE);
                relativeLayout.findViewById(R.id.ivUploadFailed).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PostBean pb = (PostBean) v.getTag();
                        if (pb != null && mOnUploadPostListener != null) {
                            mOnUploadPostListener.onFailedPostClick(pb);
                        }
                    }
                });
            }

            if (mIsRecentPostList) {
                String openIds = Preferences.getInstacne().getValues(HomeActivity.HAD_OPEN_POST_IDS, "");
                relativeLayout.findViewById(R.id.ivEmo).setAlpha(openIds.contains(postBean.getId()) ? 0.3f : 1f);
            } else {
                relativeLayout.findViewById(R.id.ivEmo).setAlpha(1f);
            }

            if (postBean.getIsAnonymity() == 1) {
                textView.setText(ServerDataManager.getTextFromKey("pblc_txt_anonymity"));
                textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                textView.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.color_221_221_221));
                if (postBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                    textView.setText(ServerDataManager.getTextFromKey("pblc_txt_anonymity") + ServerDataManager.getTextFromKey("pblc_txt_me"));
                }
            } else {
                textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                textView.setTextColor(Color.WHITE);
                textView.setText(postBean.getOwner().getRemarkName());
            }

            ArrayList<TagBean> tagList = postBean.getTags();
            if (tagList != null && !tagList.isEmpty()) {
                imageview.setVisibility(View.VISIBLE);
                int resId = Constants.EMO_ID_COLOR[tagList.get(0).getId()][0];
                imageview.setImageResource(resId);
                textView.setVisibility(View.VISIBLE);
            } else {
                imageview.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
            }

        }

        @Override
        public void onClick(View v) {
            int index = 0;
            PostBean postBean = null;
            switch (v.getId()) {
                case R.id.topLayoutLeft:
                    if (position % 2 == 0) {
                        index = position * 6;
                    } else {
                        index = position * 6 + 1;
                    }
                    if (index < getUploadingPostSize()) {
                        return;
                    }
                    postBean = (PostBean) gettopLayoutLeftUrlImageView().getTag(R.id.post_bean);
                    if (mIsRecentPostList) {
                        gettopLayoutLeftImageView().setAlpha(0.3f);
                    }
                    break;
                case R.id.topLayoutRight1:
                    if (position % 2 == 0) {
                        index = position * 6 + 1;
                    } else {
                        index = position * 6;
                    }
                    if (index < getUploadingPostSize()) {
                        return;
                    }
                    postBean = (PostBean) gettopLayoutRight1UrlImageView().getTag(R.id.post_bean);
                    if (mIsRecentPostList) {
                        gettopLayoutRight1ImageView().setAlpha(0.3f);
                    }
                    break;
                case R.id.topLayoutRight2:
                    index = position * 6 + 2;
                    if (index < getUploadingPostSize()) {
                        return;
                    }
                    postBean = (PostBean) gettopLayoutRight2UrlImageView().getTag(R.id.post_bean);
                    gettopLayoutRight2ImageView().setAlpha(0.3f);
                    break;
                case R.id.bootLayout1:
                    index = position * 6 + 3;
                    if (index < getUploadingPostSize()) {
                        return;
                    }
                    postBean = (PostBean) getbootLayout1UrlImageView().getTag(R.id.post_bean);
                    if (mIsRecentPostList) {
                        getbootLayout1ImageView().setAlpha(0.3f);
                    }
                    break;
                case R.id.bootLayout2:
                    index = position * 6 + 4;
                    if (index < getUploadingPostSize()) {
                        return;
                    }
                    postBean = (PostBean) getbootLayout2UrlImageView().getTag(R.id.post_bean);
                    if (mIsRecentPostList) {
                        getbootLayout2ImageView().setAlpha(0.3f);
                    }
                    break;
                case R.id.bootLayout3:
                    index = position * 6 + 5;
                    if (index < getUploadingPostSize()) {
                        return;
                    }
                    postBean = (PostBean) getbootLayout3UrlImageView().getTag(R.id.post_bean);
                    if (mIsRecentPostList) {
                        getbootLayout3ImageView().setAlpha(0.3f);
                    }
                    break;
            }
            if (postBean == null) {
                return;
            }
            if (mIsRecentPostList) {
                String openIds = Preferences.getInstacne().getValues(HomeActivity.HAD_OPEN_POST_IDS, "");
                if (openIds.length() == 0) {
                    openIds += postBean.getId();
                } else {
                    openIds += ("," + postBean.getId());
                }
                Preferences.getInstacne().setValues(HomeActivity.HAD_OPEN_POST_IDS, openIds);
            }
            mOnItemClickListener.onItemClick(index - getUploadingPostSize());

        }

        /**
         * 第一个post
         *
         * @return
         */
        private CustomUrlImageView gettopLayoutLeftUrlImageView() {
            return (CustomUrlImageView) topLayoutLeft.findViewById(R.id.customImageView);
        }

        private TextView gettopLayoutLeftTextView() {
            return (TextView) topLayoutLeft.findViewById(R.id.tvText);
        }

        private ImageView gettopLayoutLeftImageView() {
            return (ImageView) topLayoutLeft.findViewById(R.id.ivEmo);
        }

        /**
         * 第二个post
         *
         * @return
         */

        private CustomUrlImageView gettopLayoutRight1UrlImageView() {
            return (CustomUrlImageView) topLayoutRight1.findViewById(R.id.customImageView);
        }

        private TextView gettopLayoutRight1TextView() {
            return (TextView) topLayoutRight1.findViewById(R.id.tvText);
        }

        private ImageView gettopLayoutRight1ImageView() {
            return (ImageView) topLayoutRight1.findViewById(R.id.ivEmo);
        }


        /**
         * 第三个post
         *
         * @return
         */
        private CustomUrlImageView gettopLayoutRight2UrlImageView() {
            return (CustomUrlImageView) topLayoutRight2.findViewById(R.id.customImageView);
        }

        private TextView gettopLayoutRight2TextView() {
            return (TextView) topLayoutRight2.findViewById(R.id.tvText);
        }

        private ImageView gettopLayoutRight2ImageView() {
            return (ImageView) topLayoutRight2.findViewById(R.id.ivEmo);
        }

        /**
         * 第四个post
         *
         * @return
         */

        private CustomUrlImageView getbootLayout1UrlImageView() {
            return (CustomUrlImageView) bootLayout1.findViewById(R.id.customImageView);
        }

        private TextView getbootLayout1TextView() {
            return (TextView) bootLayout1.findViewById(R.id.tvText);
        }

        private ImageView getbootLayout1ImageView() {
            return (ImageView) bootLayout1.findViewById(R.id.ivEmo);
        }

        /**
         * 第五个post
         *
         * @return
         */
        private CustomUrlImageView getbootLayout2UrlImageView() {
            return (CustomUrlImageView) bootLayout2.findViewById(R.id.customImageView);
        }

        private TextView getbootLayout2TextView() {
            return (TextView) bootLayout2.findViewById(R.id.tvText);
        }

        private ImageView getbootLayout2ImageView() {
            return (ImageView) bootLayout2.findViewById(R.id.ivEmo);
        }


        /**
         * 第六个post
         *
         * @return
         */
        private CustomUrlImageView getbootLayout3UrlImageView() {
            return (CustomUrlImageView) bootLayout3.findViewById(R.id.customImageView);
        }

        private TextView getbootLayout3TextView() {
            return (TextView) bootLayout3.findViewById(R.id.tvText);
        }

        private ImageView getbootLayout3ImageView() {
            return (ImageView) bootLayout3.findViewById(R.id.ivEmo);
        }
    }

    public interface OnUploadPostListener {
        public void onFailedPostClick(PostBean postBean);
    }

}
