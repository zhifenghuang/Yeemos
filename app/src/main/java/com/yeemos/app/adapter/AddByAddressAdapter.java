package com.yeemos.app.adapter;

import android.content.ContentUris;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.fragment.MyInfoFragment;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.FollowButton;
import com.yeemos.app.view.FollowImageView;
import com.yeemos.app.view.MorePopupWindow;
import com.yeemos.app.view.RoundedImageView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigabud on 16-8-3.
 */
public class AddByAddressAdapter extends BaseAdapter {

    private ArrayList<BasicUser> arrayList;

    public void setArrayList(ArrayList<BasicUser> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (arrayList == null || arrayList.isEmpty()) ? 0 : arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder hold;
        if (convertView == null) {
            convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.add_by_address_item, parent, false);
            hold = new ViewHolder(convertView);
            convertView.setTag(hold);
        } else {
            Object obj = convertView.getTag();
            if (obj != null) {
                hold = (ViewHolder) obj;
            } else {
                convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.add_by_address_item, parent, false);
                hold = new ViewHolder(convertView);
                convertView.setTag(hold);
            }
        }
        hold.fill(arrayList.get(position), position);
        return convertView;
    }

    public int getPositionForSection() {
        for (int i = 0; i < getCount(); i++) {
            boolean nbool = arrayList.get(i).isServerBasicUser();
            if(!nbool){
                return i;
            }
        }
        return -1;
    }

    class ViewHolder implements View.OnClickListener{

        TextView itemTitle, tvTitle, tvContent, invite;
        FollowImageView btnFollow;
        RoundedImageView imgIcon;
        BasicUser bBean;

        public ViewHolder(View convertView){
            itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
            tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            tvContent = (TextView) convertView.findViewById(R.id.tvContent);
            invite = (TextView) convertView.findViewById(R.id.invite);

            btnFollow = (FollowImageView) convertView.findViewById(R.id.btnFollow);
            imgIcon = (RoundedImageView) convertView.findViewById(R.id.imgIcon);
        }

        public void fill(final BasicUser bBean, int position) {
            this.bBean = bBean;
            tvTitle.setText(bBean.getRemarkName());
   //         imgIcon.setDefaultImageResId(R.drawable.default_avater);
            if(bBean.isServerBasicUser()) {
                invite.setVisibility(View.GONE);
                imgIcon.setNeedDrawVipBmp(bBean.isAuthenticate());
                Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater, Preferences.getAvatarUrl(bBean.getUserAvatar()),imgIcon);
                //imgIcon.setImageUrl(Preferences.getAvatarUrl(bBean.getUserAvatar()));
                tvContent.setText("@" + bBean.getUserName());
                if(position == 0) {
                    itemTitle.setText(ServerDataManager.getTextFromKey("addbyaddrss_ttl_addfromaddressbook"));
                    itemTitle.setVisibility(View.VISIBLE);
                }else {
                    itemTitle.setVisibility(View.GONE);
                }
                tvTitle.setOnClickListener(this);
                imgIcon.setOnClickListener(this);
                tvContent.setOnClickListener(this);
                if (bBean.getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                    btnFollow.setVisibility(View.INVISIBLE);
                    return;
                }
                btnFollow.setVisibility(View.VISIBLE);
                btnFollow.setStatus(FollowButton.FollowButtonStatus.GetObject(bBean.getFollowStatus()));
                btnFollow.setFollowImageClickListener(new FollowImageView.OnFollowImgClickListener() {
                    public void onClick() {
                        if (btnFollow.getStatus() == FollowButton.FollowButtonStatus.FollowButtonStatus_Follow) {
                            followOperate(bBean);
                        } else {
                            MorePopupWindow popUpWindow = new MorePopupWindow(BaseApplication.getCurFragment().getActivity(), new MorePopupWindow.MorePopupWindowClickListener() {
                                @Override
                                public void onThirdBtnClicked() {
                                }

                                @Override
                                public void onSecondBtnClicked() {
                                }

                                @Override
                                public void onFirstBtnClicked() {
                                    followOperate(bBean);
                                }

                                @Override
                                public void onFourthBtnClicked() {

                                }

                                @Override
                                public void onCancelBtnClicked() {
                                    // TODO Auto-generated method stub
                                }
                            }, Constants.MORE_POPUPWINDOW_UNFOLLOWREQUEST);
                            popUpWindow.initView(null);
                            popUpWindow.showAtLocation(BaseApplication.getCurFragment().getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                        }
                    }
                });

            }else {
                btnFollow.setVisibility(View.GONE);
                invite.setVisibility(View.VISIBLE);
                invite.setSelected(bBean.isInvited());
                if(!bBean.isInvited()) {
                    invite.setText(ServerDataManager.getTextFromKey("addbyaddrss_btn_invite"));
                    invite.setTextColor(BaseApplication.getCurFragment().getResources().getColor(R.color.color_45_223_227));
                }else {
                    invite.setText(ServerDataManager.getTextFromKey("addbyaddrss_btn_invited"));
                    invite.setTextColor(BaseApplication.getCurFragment().getResources().getColor(R.color.color_255_255_255));
                }
                tvTitle.setOnClickListener(null);
                imgIcon.setOnClickListener(null);
                tvContent.setOnClickListener(null);
                invite.setOnClickListener(this);
                tvContent.setText(bBean.getMobile());
                if( bBean.getContactid()!= -1) {
                    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, bBean.getContactid());
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                            BaseApplication.getAppContext().getContentResolver(), uri);
                    imgIcon.setImageBitmap(BitmapFactory.decodeStream(input));

//                    Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater,uri,imgIcon);
                }else{
                    Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater,"",imgIcon);
                }
                if(position == getPositionForSection()) {
                    itemTitle.setText(ServerDataManager.getTextFromKey("addbyaddrss_ttl_invitetoyeemos"));
                    itemTitle.setVisibility(View.VISIBLE);
                }else {
                    itemTitle.setVisibility(View.GONE);
                }
            }

        }
        private void followOperate(BasicUser bBean) {
            if (bBean.getBlockStatus() == 1) {
                //如果这个用户已经将当前用户Block
                btnFollow.setStatus(FollowButton.FollowButtonStatus.FollowButtonStatus_Following);
                if (bBean.getBlockStatus() == 1) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnFollow.setStatus(FollowButton.FollowButtonStatus.FollowButtonStatus_Follow);
                        }
                    }, 1000);
                }
            } else {
                DataManager.getInstance().follow(bBean);
                btnFollow.setStatus(FollowButton.FollowButtonStatus.GetObject(bBean.getFollowStatus()));
            }
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.invite:
                    if(!bBean.isInvited()) {
                        invite.setSelected(true);
                        bBean.setInvited(true);
                        invite.setText(ServerDataManager.getTextFromKey("addbyaddrss_btn_invited"));
                        invite.setTextColor(BaseApplication.getCurFragment().getResources().getColor(R.color.color_255_255_255));
                        doSendSMSTo();
                    }
                    break;
                case R.id.tvTitle:
                case R.id.imgIcon:
                case R.id.tvContent:
                    DataManager.getInstance().setCurOtherUser(bBean);
                    String userId = bBean.getUserId();
                    if (userId.equals(MemberShipManager.getInstance().getUserInfo().getUserId())) {
                        BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
                    } else {
                        DataManager.getInstance().setCurOtherUser(bBean);
                        BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
                    }
                    break;
            }
        }
        public void sendSMS(){
            //获取短信管理器
            android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
            //拆分短信内容（手机短信长度限制）
            String message = String.format(ServerDataManager.getTextFromKey("addbyaddrss_txt_invitemessage"), MemberShipManager.getInstance().getUserName());
            List<String> divideContents = smsManager.divideMessage(message);
            for (String text : divideContents) {
                smsManager.sendTextMessage(bBean.getMobile(), null, text, null, null);
            }
        }
        public void doSendSMSTo(){
            String message = String.format(ServerDataManager.getTextFromKey("addbyaddrss_txt_invitemessage"), MemberShipManager.getInstance().getUserName());
            if(PhoneNumberUtils.isGlobalPhoneNumber(bBean.getMobile().replaceAll(" ",""))){
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + bBean.getMobile()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_TEXT, message);
                intent.putExtra("sms_body", message);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){ //At least KitKat
                    //Need to change the build to API 19
                    String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(BaseApplication.getAppContext());
                    if (defaultSmsPackageName != null){
                        //Can be null in case that there is no default, then the user would be able to choose any app that support this intent.
                        intent.setPackage(defaultSmsPackageName);
                    }
                }
                try {
                    BaseApplication.getAppContext().startActivity(intent);
                }catch(Exception e){

                }
            }
        }
    }
}
