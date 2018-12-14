package com.yeemos.app.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gigabud.core.util.Country;
import com.gigabud.core.util.LanguagePreferences;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 15-12-29.
 */
public class SelectCountryFragment extends BaseFragment implements View.OnClickListener {

    public static String LAST_FRAGMENT_NAME="lastFragmentName";


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        final ListView listView = (ListView) view.findViewById(
                R.id.listview);
        listView.setAdapter(new SelectCountryAdapter(LanguagePreferences
                .getInstanse(getActivity()).getAllCountries(getActivity())));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SelectCountryAdapter adapter = (SelectCountryAdapter) listView
                        .getAdapter();
                String className = getArguments().getString(LAST_FRAGMENT_NAME);
                BaseFragment fragment = (BaseFragment) getActivity().getSupportFragmentManager().findFragmentByTag(className);
                if ( fragment != null ) {
                    fragment.refreshFromNextFragment(adapter.getItem(position));
                }
                goBack();
            }
        });
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {

        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.tvTitle,"slctcntry_ttl_selectcountry");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_select_country;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
            default:
                break;
        }

    }

    @SuppressLint("InflateParams")
    class SelectCountryAdapter extends BaseAdapter {
        private ArrayList<Country> mCountryList;

        public SelectCountryAdapter(ArrayList<Country> countryList) {
            mCountryList = countryList;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mCountryList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mCountryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Country country = mCountryList.get(position);
            HoldView holdView;
            if ( convertView == null ) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.select_country_item, null);
                holdView = new HoldView();
                holdView.tvCountryName = (TextView) convertView
                        .findViewById(R.id.tvCountryName);
                holdView.tvPhoneZip = (TextView) convertView
                        .findViewById(R.id.tvPhoneZip);
                convertView.setTag(holdView);
            } else {
                holdView = (HoldView) convertView.getTag();
            }
            holdView.tvCountryName.setText(country.countryName);
            holdView.tvPhoneZip.setText("+" + country.phoneZip);
            return convertView;
        }
    }

    class HoldView {
        TextView tvCountryName;
        TextView tvPhoneZip;
    }
}
