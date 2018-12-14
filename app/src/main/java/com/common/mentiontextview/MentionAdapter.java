package com.common.mentiontextview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.HashTagBean;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MentionAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List resultList = new ArrayList();
    private String mCurMentionContent = null;

    public MentionAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Object getItem(int index) { // 1213
        return index >= getCount() ? null : resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= getCount()) {
            return null;
        }
        Holder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.mention_list_item, parent, false);
            holder = new Holder(convertView);
            convertView.setTag(holder);
        } else {
            Object obj = convertView.getTag();
            if (obj != null) {
                holder = (Holder) obj;
            } else {
                convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.mention_list_item, parent, false);
                holder = new Holder(convertView);
                convertView.setTag(holder);
            }
        }
        holder.fillData(getItem(position));
//		TextView textView = (TextView) convertView.findViewById(R.id.geo_search_result_text);
//		textView.setText(getItem(position));

        return convertView;
    }

    class Holder {
        TextView textView1, textView2,tvPhotoDes;
        LinearLayout imgIconLy;
        RoundedImageView imgIcon;

        public Holder(View cView) {
            textView1 = (TextView) cView.findViewById(R.id.textView1);
            textView2 = (TextView) cView.findViewById(R.id.textView2);
            imgIconLy = (LinearLayout) cView.findViewById(R.id.imgIconLy);
            imgIcon = (RoundedImageView) cView.findViewById(R.id.imgIcon);
            tvPhotoDes = (TextView) cView.findViewById(R.id.tvPhotoDes);
        }

        public void fillData(Object object) {
            if (object.getClass().isAssignableFrom(HashTagBean.class)) {
                textView1.setVisibility(View.GONE);
                textView2.setCompoundDrawablesRelativeWithIntrinsicBounds(BaseApplication.getAppContext().
                        getResources().getDrawable(R.drawable.hashtag_label), null, null, null);
                textView2.setText(((HashTagBean) object).getHasTag());
                textView2.setTextColor(mContext.getResources().getColor(R.color.color_45_223_227));
                imgIconLy.setVisibility(View.GONE);
                tvPhotoDes.setVisibility(View.VISIBLE);
                tvPhotoDes.setText(((HashTagBean) object).getAssociatePostNums()+" "+ServerDataManager.getTextFromKey("srchtag_txt_posts"));
            } else {
                textView1.setVisibility(View.VISIBLE);
                imgIconLy.setVisibility(View.VISIBLE);
                tvPhotoDes.setVisibility(View.GONE);
                textView1.setText(((BasicUser) object).getRemarkName());
                textView2.setText("@" + ((BasicUser) object).getUserName());
                textView1.setTextColor(Color.BLACK);
                textView2.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,null,null);
                textView2.setTextColor(BaseApplication.getCurFragment().getResources().getColor(R.color.color_187_187_187));
                imgIcon.setNeedDrawVipBmp(((BasicUser) object).isAuthenticate());
                Utils.loadImage(BaseApplication.getAppContext(), R.drawable.default_avater, Preferences.getAvatarUrl(((BasicUser) object).getAvatar()), imgIcon);
            }
        }
    }

    public String getCurMentionContent() {
        return mCurMentionContent;
    }

    public void resetCurMentionContent() {
        mCurMentionContent = null;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    mCurMentionContent = constraint.toString();
                    String strTrigerKey = mCurMentionContent.substring(0, 1);
                    if (mCurMentionContent.length() > 1) {
                        String strMention = mCurMentionContent.substring(1);
                        List<Object> locations = findRementions(mContext,
                                strTrigerKey, strMention);

                        // Assign the data to the FilterResults
                        filterResults.values = locations;
                        filterResults.count = locations.size();

                    } else {
                        List<Object> locations = findRementions(mContext,
                                strTrigerKey, "");
                        // Assign the data to the FilterResults
                        filterResults.values = locations;
                        filterResults.count = locations.size();
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

        };
        return filter;
    }

    private List<Object> findRementions(Context context, String strTrigerKey,
                                        String query_text) {
        List<Object> geo_search_results = new ArrayList<>();
        if ("@".equals(strTrigerKey)) {
            ArrayList<BasicUser> arrList = DataManager.getInstance().getAllFriends(true);
            ArrayList<BasicUser> filterArrList = new ArrayList<>();
            for (BasicUser basicUser : arrList) {
                if (basicUser.getUserName().contains(query_text) || basicUser.getRemarkName().contains(query_text)) {
                    filterArrList.add(basicUser);
                }
            }
            if (filterArrList != null) {
                if (!filterArrList.isEmpty()) {
                    Collections.sort(filterArrList, new Comparator<BasicUser>() {
                        @Override
                        public int compare(BasicUser user1, BasicUser user2) {
                            return user1.getPinyinName().toUpperCase().compareTo(user2.getPinyinName().toUpperCase());
                        }
                    });
                }
                geo_search_results.addAll(filterArrList);
            }

        } else if ("#".equals(strTrigerKey)) {

            ArrayList<HashTagBean> arrList = DataManager.getInstance().searchHashTags(query_text, geo_search_results.size(), 20);
            if (arrList != null) {
                geo_search_results.addAll(arrList);
            }
        }

        return geo_search_results;
    }

}