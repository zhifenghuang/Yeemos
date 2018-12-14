package com.yeemos.app.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.CommentBean;
import com.gbsocial.BeansBase.CommentBean.MenuCommentState;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.constants.GBSConstants.MenuObjectType;
import com.gbsocial.constants.GBSConstants.MenuOperateType;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.ConnectedUtil;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.fragment.MyInfoFragment;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.FiveBtnPopupWindow;
import com.yeemos.app.view.MorePopupWindow;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.view.TextViewDoubleClick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CommentsAdapter extends BaseAdapter {
    private PostBean postBean;
    private CommentsFragmentsetMentionText listener;
    private View commontsView;
    private Dialog dialog;
    private ArrayList<CommentBean> commentList;
    private boolean mIsAnonymity;

    public void setAnonymity(boolean isAnonymity) {
        mIsAnonymity = isAnonymity;
//        notifyDataSetChanged();
    }


    public interface CommentsFragmentsetMentionText {
        void setMentionTextViewText(String str, Integer replyUserID);

        void showOrHideListView(boolean show);

        void resetCommentNum(long commentNum);
    }

    public CommentsAdapter(PostBean postBean, View commontsView, CommentsFragmentsetMentionText listener) {
        super();
        this.postBean = postBean;
        this.commontsView = commontsView;
        this.listener = listener;
    }


    private void sortDatas() {
        Collections.sort(commentList, new Comparator<CommentBean>() {
            @Override
            public int compare(CommentBean lhs, CommentBean rhs) {
                if (lhs.getCreateTime() > rhs.getCreateTime()) {
                    return 1;
                } else if (lhs.getCreateTime() == rhs.getCreateTime()) {
                    return 0;
                } else
                    return -1;
            }
        });
    }

    public void setCommentList(ArrayList<CommentBean> commentList) {
        this.commentList = commentList;
        sortDatas();
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (listener != null) {
            listener.showOrHideListView((commentList != null && commentList.size() > 0));
        }
    }

    @Override
    public int getCount() {
        return commentList == null ? 0 : commentList.size();
    }

    @Override
    public Object getItem(int position) {
        return commentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//		View menuView=null;
        CommentViewHolder hold;
        boolean isLastData = (position == commentList.size() - 1 ? true : false);
        if (convertView == null) {
            convertView = LayoutInflater.from(BaseApplication.getAppContext())
                    .inflate(R.layout.listview_comments, parent, false);
//            hold = new CommentViewHolder(convertView);
//			convertView = View.inflate(BaseApplication.getAppContext(),
//					R.layout.listview_comments, null);
//			menuView = View.inflate(BaseApplication.getAppContext(),
//					R.layout.comment_list_menu, null);
//			//合成内容与菜单
//			convertView = new DragDelItem(convertView,menuView);
            hold = new CommentViewHolder(convertView);
            convertView.setTag(hold);
        } else {
            Object obj = convertView.getTag();
            if (obj != null) {
                hold = (CommentViewHolder) obj;
            } else {
                convertView = LayoutInflater.from(
                        BaseApplication.getAppContext()).inflate(
                        R.layout.listview_comments, parent, false);
//				convertView = View.inflate(BaseApplication.getAppContext(),
//						R.layout.listview_comments, null);
//				menuView = View.inflate(BaseApplication.getAppContext(),
//						R.layout.comment_list_menu, null);
//				//合成内容与菜单
//				convertView = new DragDelItem(convertView,menuView);
                hold = new CommentViewHolder(convertView);
                convertView.setTag(hold);

            }
        }
        hold.fillComment(commentList.get(position), isLastData);
        return convertView;
    }

    public void addNewComment(CommentBean comment) {
        if(commentList == null){
            commentList = new ArrayList<>();
        }
        commentList.add(comment);
    }

    public class CommentViewHolder implements OnClickListener, OnLongClickListener {
        private RoundedImageView imgIcon;
        private RelativeLayout commentLy;
        private TextView tvTime, tvTitle;
        private TextViewDoubleClick tvContent;
        private PopupWindow popupWindow;
        private View lineBottom;
        private String dispalyName;
        private CommentBean cBean;
        private ImageButton errorBtn;
        private MenuOperateType operateType;

        public CommentViewHolder(View convertView) {
            tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            tvContent = (TextViewDoubleClick) convertView.findViewById(R.id.tvContent);
            tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            imgIcon = (RoundedImageView) convertView.findViewById(R.id.imgIcon);
            lineBottom = convertView.findViewById(R.id.lineBottom);
            commentLy = (RelativeLayout) convertView.findViewById(R.id.commentLy);
            errorBtn = (ImageButton) convertView.findViewById(R.id.btnError);
            tvTitle.setOnClickListener(this);
            imgIcon.setOnClickListener(this);
            // 长按
            commentLy.setOnLongClickListener(this);
            tvContent.setOnLongClickListener(this);
            errorBtn.setOnClickListener(this);
        }

        public void fillComment(CommentBean cBean, boolean isLastData) {
            this.cBean = cBean;
            dispalyName = cBean.getOwner().getRemarkName();
            if (mIsAnonymity) {
                String userId = cBean.getOwner().getUserId();
                String md5Str = Utils.MD5(cBean.getOwner().getUserId() + postBean.getId());
     //           tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                tvTitle.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.color_221_221_221));
                if (userId.equals(MemberShipManager.getInstance().getUserID())) {
                    tvTitle.setText(Utils.getEmoText(md5Str)+ServerDataManager.getTextFromKey("pblc_txt_me"));
                }else {
                    tvTitle.setText(Utils.getEmoText(md5Str));
                }
                imgIcon.setNeedDrawVipBmp(false);
                if (userId.equals(postBean.getOwner().getUserId())) {
                    imgIcon.setImageResource(R.drawable.hide_on_bush_own);
                } else {
                    imgIcon.setImageResource(R.drawable.hide_on_bush_other);
                }
            } else {
  //              tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                tvTitle.setTextColor(BaseApplication.getAppContext().getResources().getColor(R.color.color_45_223_227));
                tvTitle.setText(dispalyName);
                imgIcon.setNeedDrawVipBmp(cBean.getOwner().isAuthenticate());
                Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater, Preferences.getAvatarUrl(cBean.getOwner().getUserAvatar()),imgIcon);
               // imgIcon.setImageUrl(Preferences.getAvatarUrl(cBean.getOwner().getUserAvatar()));
            }
            tvContent.setMovementMethod(TextViewDoubleClick.LocalLinkMovementMethod.getInstance());
            tvContent.setText(Utils.getKeywordClickable(cBean.getText(), null, null, R.color.color_142_153_168));
//            tvContent.setText(cBean.getText());
            tvTime.setText(Utils.getTime(cBean.getCreateTime()));
            if (isLastData) {
                lineBottom.setVisibility(View.VISIBLE);
            } else {
                lineBottom.setVisibility(View.INVISIBLE);
            }
            if (cBean.getCommentStatus() == MenuCommentState.CommentState_Fail) {
                errorBtn.setVisibility(View.VISIBLE);
                tvTime.setVisibility(View.INVISIBLE);
            } else {
                errorBtn.setVisibility(View.INVISIBLE);
                tvTime.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            // longClickLy.setVisibility(View.GONE);
            switch (v.getId()) {
                case R.id.btnError:
                    MorePopupWindow popUpWindow = new MorePopupWindow(
                            (Activity) commontsView.getContext(), new MorePopupWindow.MorePopupWindowClickListener() {
                        @Override
                        public void onFirstBtnClicked() {
                            DataManager.getInstance().removeFailCommentBean(cBean, DataManager.getInstance().getCurPostBean().getId());
                            CommentsAdapter.this.commentList.remove(cBean);
                            CommentsAdapter.this.notifyDataSetChanged();
                        }

                        @Override
                        public void onSecondBtnClicked() {

                        }

                        @Override
                        public void onThirdBtnClicked() {
                            GBExecutionPool.getExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    ServerResultBean<PostBean> result = DataManager.getInstance().comment(cBean);
                                    if (result.isSuccess()) {
                                        cBean.setCommentStatus(MenuCommentState.CommentState_Success);
                                        DataManager.getInstance().removeFailCommentBean(cBean, DataManager.getInstance().getCurPostBean().getId());
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            public void run() {
                                                CommentsAdapter.this.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFourthBtnClicked() {

                        }

                        @Override
                        public void onCancelBtnClicked() {
                            // TODO Auto-generated method stub
                        }
                    }, Constants.MORE_POPUPWINDOW_COMMENT);
                    popUpWindow.initView(null);
                    popUpWindow.showAtLocation(commontsView, Gravity.CENTER_HORIZONTAL, 0, 0);
                    break;
                case R.id.tvTitle:
                case R.id.imgIcon:
                    if (!mIsAnonymity) {
                        DataManager.getInstance().setCurOtherUser(cBean.getOwner());
                        if (cBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                            BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
                        } else {
                            BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.commentLy:
                case R.id.tvContent:
                    showPopupWindow();
                    break;
                default:
                    break;
            }
            return false;
        }

        private void showPopupWindow() {
            MorePopupWindow morePopupWindow = new MorePopupWindow(BaseApplication.getCurFragment().getActivity(),
                    new MorePopupWindow.MorePopupWindowClickListener(){

                @Override
                public void onFirstBtnClicked() {
                    if (mIsAnonymity) {
                        if (cBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                            listener.setMentionTextViewText("@" + cBean.getOwner().getUserName() + " ",
                                    Integer.valueOf(cBean.getOwner().getUserId()));
                        } else {
                            String md5Str = Utils.MD5(cBean.getOwner().getUserId() + postBean.getId());
                            listener.setMentionTextViewText("@" + Utils.getEmoText(md5Str) + " ",
                                    Integer.valueOf(cBean.getOwner().getUserId()));
                        }
                    } else {
                        listener.setMentionTextViewText("@" + cBean.getOwner().getUserName() + " ",
                                Integer.valueOf(cBean.getOwner().getUserId()));
                    }

                }

                @Override
                public void onSecondBtnClicked() {
                    reportTypeWindow();
                }

                @Override
                public void onThirdBtnClicked() {
                    DataManager.getInstance().setCurComment(cBean);
                    commentList.remove(cBean);
                    notifyDataSetChanged();
                    DataManager.getInstance().getCurPostBean()
                            .setCommentNums(DataManager.getInstance().getCurPostBean().getCommentNums() - 1);
                    DataManager.getInstance().delete(MenuObjectType.Menu_Object_Comment);
                    ArrayList<CommentBean> arrList = new ArrayList<CommentBean>();
                    int listSize = commentList.size();
                    if (listSize - 3 >= 0) {
                        arrList.add(commentList.get(listSize - 3));
                    }
                    if (listSize - 2 >= 0) {
                        arrList.add(commentList.get(listSize - 2));
                    }
                    if (listSize - 1 >= 0) {
                        arrList.add(commentList.get(listSize - 1));
                    }
                    DataManager.getInstance().getCurPostBean().setCommentObjects(arrList);
                    listener.resetCommentNum(arrList.size());
                }

                @Override
                public void onFourthBtnClicked() {

                }

                @Override
                public void onCancelBtnClicked() {

                }
            }, Constants.MORE_POPUPWINDOW_COMMENT_OPERATE);
            morePopupWindow.setcBean(cBean);
            morePopupWindow.initView(null);
            morePopupWindow.showAtLocation(commontsView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        }


        private void reportTypeWindow() {
            FiveBtnPopupWindow popUpWindow = new FiveBtnPopupWindow((Activity) commontsView.getContext(), new FiveBtnPopupWindow.FiveBtnPopupWindowClickListener() {
                @Override
                public void onFirstBtnClicked() {
                    // TODO Auto-generated method stub
                    reportSubmit(MenuOperateType.Menu_Operate_Report_Porn);
                }

                @Override
                public void onSecondBtnClicked() {
                    // TODO Auto-generated method stub
                    reportSubmit(MenuOperateType.Menu_Operate_Report_Scam);
                }

                @Override
                public void onThirdBtnClicked() {
                    // TODO Auto-generated method stub
                    reportSubmit(MenuOperateType.Menu_Operate_Report_Abuse);
                }

                @Override
                public void onFourthBtnClicked() {
                    // TODO Auto-generated method stub
                    reportSubmit(MenuOperateType.Menu_Operate_Report_CommercialSpam);
                }

                @Override
                public void onFifthBtnClicked() {
                    // TODO Auto-generated method stub
                    reportSubmit(MenuOperateType.Menu_Operate_Report_Offensive);
                }

                @Override
                public void onCancelBtnClicked() {
                    // TODO Auto-generated method stub

                }
            });
   //         popUpWindow.setTitleText("pblc_txt_whyreport");
            popUpWindow.initView();
            popUpWindow.showAtLocation(commontsView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        }

        private void reportSubmit(MenuOperateType opType) {
            if (!ConnectedUtil.isConnected(commontsView.getContext())) {
                return;
            }

            operateType = opType;
            commentList.remove(cBean);
            notifyDataSetChanged();
            DataManager.getInstance().setCurComment(cBean);
            DataManager.getInstance().report(MenuObjectType.Menu_Object_Comment, operateType);
            String content = ServerDataManager.getTextFromKey("pblc_txt_reportsuccess");
            String OK = ServerDataManager.getTextFromKey("pub_btn_ok");//"OK";
            BaseApplication.getCurFragment().showPublicDialog(null, content, OK, null, oneBtnDialoghandler);
        }

        Handler oneBtnDialoghandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.DIALOG_LEFT_BTN:
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }
}

