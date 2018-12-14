package com.yeemos.app.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.BitmapUtil;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

import java.io.File;

/**
 * Created by gigabud on 16-8-10.
 */
public class CustomFilterView extends RelativeLayout {

    public enum FilterType {
        TYPE_NONE,
        TYPE_TIME,
        TYPE_DATE,
        TYPE_TEMP,
        TYPE_IMAGE,
        TYPE_OPEN_GPS
    }

    private FilterType mFilterType;

    public CustomFilterView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.custom_filter_view, this);
    }

    public void setFilterType(FilterType filterType) {
        mFilterType = filterType;
        RelativeLayout rlTime = (RelativeLayout) findViewById(R.id.rlTime);
        TextView tvTemp = (TextView) findViewById(R.id.tvTemp);
        ImageView iv = (ImageView) findViewById(R.id.iv);
        RelativeLayout rlEnableGPs = (RelativeLayout) findViewById(R.id.rlEnableGPS);
        setBackgroundColor(Color.TRANSPARENT);
        rlEnableGPs.setVisibility(View.GONE);
        rlTime.setVisibility(View.GONE);
        tvTemp.setVisibility(View.GONE);
        iv.setVisibility(View.GONE);
        if (mFilterType == FilterType.TYPE_TIME) {
            rlTime.setVisibility(View.VISIBLE);
        } else if (mFilterType == FilterType.TYPE_DATE) {
            rlTime.setVisibility(View.VISIBLE);
        } else if (mFilterType == FilterType.TYPE_TEMP) {
            tvTemp.setVisibility(View.VISIBLE);
        } else if (mFilterType == FilterType.TYPE_IMAGE) {
            iv.setVisibility(View.VISIBLE);
        } else if (mFilterType == FilterType.TYPE_OPEN_GPS) {
            setBackgroundColor(getResources().getColor(R.color.half_transparent));
            rlEnableGPs.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvEnableGPS)).setText(ServerDataManager.getTextFromKey("edtpst_btn_enablelocation"));
            ((TextView) findViewById(R.id.tvUnlockFilters)).setText(ServerDataManager.getTextFromKey("edtpst_txt_unlockfilter"));
            ((TextView) findViewById(R.id.tvEnableToSee)).setText(ServerDataManager.getTextFromKey("edtpst_txt_unlockfilterdes"));
            findViewById(R.id.tvEnableGPS).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getContext().startActivity(intent);
                }
            });
        }
    }

    public void setText(Typeface... typeface) {
        if (mFilterType == FilterType.TYPE_TIME) {
            findViewById(R.id.rlDate).setVisibility(View.GONE);
            TextView tvTime = (TextView) findViewById(R.id.tvTime);
            tvTime.setTypeface(typeface[0]);
            boolean is24Format = android.text.format.DateFormat.is24HourFormat(getContext());
            tvTime.setText(Utils.getTimeStrOnlyHourBySystem(getContext(), is24Format));
            TextView tvAMOrPM = (TextView) findViewById(R.id.tvAMOrPM);
            tvAMOrPM.setTypeface(typeface[0]);
            if (!is24Format) {
                tvAMOrPM.setVisibility(View.VISIBLE);
                tvAMOrPM.setText(Utils.geAMOrPM());
            } else {
                tvAMOrPM.setVisibility(View.GONE);
            }
        } else if (mFilterType == FilterType.TYPE_DATE) {
            findViewById(R.id.rlDate).setVisibility(View.VISIBLE);
            TextView tvTime = (TextView) findViewById(R.id.tvTime);
            tvTime.setTypeface(typeface[0]);
            boolean is24Format = android.text.format.DateFormat.is24HourFormat(getContext());
            tvTime.setText(Utils.getTimeStrOnlyHourBySystem(getContext(), is24Format));
            TextView tvAMOrPM = (TextView) findViewById(R.id.tvAMOrPM);
            tvAMOrPM.setTypeface(typeface[0]);
            if (!is24Format) {
                tvAMOrPM.setVisibility(View.VISIBLE);
                tvAMOrPM.setText(Utils.geAMOrPM());
            } else {
                tvAMOrPM.setVisibility(View.GONE);
            }
            TextView tvWeekDay = (TextView) findViewById(R.id.tvWeekday);
            tvWeekDay.setTypeface(typeface[1]);
            tvWeekDay.setText(Utils.getWeek(getContext()));
            TextView tvDate = (TextView) findViewById(R.id.tvDate);
            tvDate.setTypeface(typeface[2]);
            tvDate.setText(Utils.getMonth(getContext()));
        } else if (mFilterType == FilterType.TYPE_TEMP) {
            TextView tvTemp = (TextView) findViewById(R.id.tvTemp);
            tvTemp.setTypeface(typeface[0]);
            tvTemp.setText(getCurrentTemperature());
        }
    }

    private String getCurrentTemperature() {
        return Preferences.getInstacne().getValues(HandleRelativeLayout.CURRENT_TEMPERATURE, 25) + "°C";
    }

    public void setImage(String photoPath, int viewWidth, int viewHeight) {
//        Bitmap image = BitmapCacheManager.getInstance().get(photoPath);
//        if (image != null) {
//            ((ImageView) findViewById(R.id.iv)).setImageBitmap(image);
//        } else {
//            image = BitmapUtil.getBitmapFromFile(photoPath, viewWidth, viewHeight);
//            if (image != null) {
//                BitmapCacheManager.getInstance().put(photoPath, image);
//                ((ImageView) findViewById(R.id.iv)).setImageBitmap(image);
//            }
//        }
        Utils.loadImage(getContext(), new File(photoPath), 0, "", (ImageView) findViewById(R.id.iv));
    }
}
