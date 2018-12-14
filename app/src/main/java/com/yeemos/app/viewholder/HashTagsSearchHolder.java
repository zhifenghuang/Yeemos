package com.yeemos.app.viewholder;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.HashTagBean;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.fragment.HashTagsFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.R;

public class HashTagsSearchHolder implements MPagerViewHolder<HashTagBean> {
	private TextView tvHashTag;
	private TextView tvDes;
    private TextView tvPhotoUnit;
	RelativeLayout hashTagRe;
	@Override
	public void fill(final HashTagBean bean, int position) {
		tvHashTag.setText(" " + bean.getHasTag());
		tvHashTag.setCompoundDrawablesWithIntrinsicBounds(BaseApplication.getAppContext().getResources().getDrawable(R.drawable.hashtag_label),null,null,null);
		tvDes.setText("" + bean.getAssociatePostNums());
		tvPhotoUnit.setText(ServerDataManager.getTextFromKey("srchtag_txt_posts"));
		hashTagRe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Bundle bundle = new Bundle();
//				bundle.putString(Constants.KEY_SEND_STRING_TO_HASHTAGSFRAGMENT, bean.getHasTag());
				DataManager.getInstance().setCurKeyWord(bean.getHasTag());
				BaseApplication.getCurFragment().gotoPager(HashTagsFragment.class, null);
			}
		});
	}

	@Override
	public void viewHolder( View convertView, Fragment frg) {
		tvHashTag = (TextView) convertView.findViewById(R.id.tvHashTag);
		tvDes = (TextView) convertView.findViewById(R.id.tvDes);
        tvPhotoUnit = (TextView) convertView.findViewById(R.id.tvPhotoDes);
		hashTagRe = (RelativeLayout)convertView.findViewById(R.id.hashTagRe);
	}

}
