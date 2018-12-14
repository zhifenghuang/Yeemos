package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.gbsocial.utils.GPSUtils;
import com.gigabud.core.http.DownloadFileManager;
import com.gigabud.core.util.BaseUtils;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.WeatherInfo;
import com.yeemos.app.utils.YahooWeather;
import com.yeemos.app.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by gigabud on 15-12-2.
 * 手动处理滑动的RelativeLayout
 */
public class HandleRelativeLayout extends RelativeLayout {

    private ArrayList<CustomFilterView> mViewList;
    private OnHandleRelativeLayoutEvent mOnHandleRelativeLayoutEvent;
    private boolean mIsViewPagerMove;

    private static final String LAST_GET_WEATHER_TIME = "lastGetWeatherTime";

    public static final String CURRENT_TEMPERATURE = "currentTemperature";

    private DisplayMetrics mDisplaymetrics;

    private boolean mIsHadEnableGPSPage;

    private Typeface mPFTypeFace, mOpenSansBoldTypeface, mOpenSansTypeface;

    private int mTotalFilters;

    private static final int VIEW_PAGER_COUNT = 4;

    private static final int FIX_FILTER_COUNT = 8;

    private ArrayList<String> mPhotoFilters;


    public DisplayMetrics getDisplaymetrics() {
        if (mDisplaymetrics == null) {
            mDisplaymetrics = new DisplayMetrics();
            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplaymetrics);
        }
        return mDisplaymetrics;
    }

    public interface OnHandleRelativeLayoutEvent {
        void onScroll(int pageNumber, float xOffset);

        void onClick();

        void isInGPSFilterPage(boolean isIn);
    }

    private Typeface getPFTypeFace() {
        if (mPFTypeFace == null) {
            mPFTypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/PFDinTextCompPro-Regular.ttf");
        }
        return mPFTypeFace;
    }

    private Typeface getOpenBoldTypeFace() {
        if (mOpenSansBoldTypeface == null) {
            mOpenSansBoldTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Bold.ttf");
        }
        return mOpenSansBoldTypeface;
    }

    private Typeface getOpenTypeFace() {
        if (mOpenSansTypeface == null) {
            mOpenSansTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf");
        }
        return mOpenSansTypeface;
    }

    public HandleRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initViewPager(Context context) {
        if (mViewList == null) {
            mViewList = new ArrayList<>();
        }
        mViewList.clear();
        for (int i = 0; i < VIEW_PAGER_COUNT; ++i) {
            CustomFilterView customFilterView = new CustomFilterView(getContext());
            customFilterView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
            mViewList.add(customFilterView);
        }
        mTotalFilters = FIX_FILTER_COUNT;
        resetViewByGPSEnable();

        final ViewPager viewPager = getViewPager();
        viewPager.setAdapter(mPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()

                                          {
                                              @Override
                                              public void onPageSelected(int arg0) {
                                                  int pageNo = arg0 % mTotalFilters;
                                                  CustomFilterView beforeView = mViewList.get((arg0 - 1) % VIEW_PAGER_COUNT);
                                                  CustomFilterView afterView = mViewList.get((arg0 + 1) % VIEW_PAGER_COUNT);
                                                  //                                                 if (mTotalFilters >= FIX_FILTER_COUNT + 1) {
                                                  if (pageNo == 0) {
                                                      if (mIsHadEnableGPSPage) {
                                                          beforeView.setFilterType(CustomFilterView.FilterType.TYPE_OPEN_GPS);
                                                      } else {
                                                          if (mPhotoFilters != null && !mPhotoFilters.isEmpty()) {
                                                              beforeView.setFilterType(CustomFilterView.FilterType.TYPE_IMAGE);
                                                              beforeView.setImage(mPhotoFilters.get(mPhotoFilters.size() - 1),
                                                                      getDisplaymetrics().widthPixels, getDisplaymetrics().heightPixels);
                                                          } else {
                                                              beforeView.setFilterType(CustomFilterView.FilterType.TYPE_TEMP);
                                                              beforeView.setText(getPFTypeFace());
                                                          }
                                                      }
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                  } else if (pageNo == FIX_FILTER_COUNT - 3) {
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_TIME);
                                                      afterView.setText(getPFTypeFace());
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                  } else if (pageNo == FIX_FILTER_COUNT - 2) {
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_DATE);
                                                      afterView.setText(getPFTypeFace(), getOpenBoldTypeFace(), getOpenTypeFace());
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                  } else if (pageNo == FIX_FILTER_COUNT - 1) {
                                                      if (mIsHadEnableGPSPage) {
                                                          afterView.setFilterType(CustomFilterView.FilterType.TYPE_OPEN_GPS);
                                                      } else {
                                                          afterView.setFilterType(CustomFilterView.FilterType.TYPE_TEMP);
                                                          afterView.setText(getPFTypeFace());
                                                      }
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_TIME);
                                                      beforeView.setText(getPFTypeFace());
                                                  } else if (pageNo == FIX_FILTER_COUNT) {
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_DATE);
                                                      beforeView.setText(getPFTypeFace(), getOpenBoldTypeFace(), getOpenTypeFace());
                                                      if (mPhotoFilters != null && !mPhotoFilters.isEmpty()) {
                                                          afterView.setFilterType(CustomFilterView.FilterType.TYPE_IMAGE);
                                                          afterView.setImage(mPhotoFilters.get(0),
                                                                  getDisplaymetrics().widthPixels, getDisplaymetrics().heightPixels);
                                                      }
                                                  } else if (pageNo > FIX_FILTER_COUNT) {
                                                      if (mPhotoFilters != null && !mPhotoFilters.isEmpty()) {
                                                          int totalPicSize = mPhotoFilters.size();
                                                          int currentIndex = pageNo - FIX_FILTER_COUNT - 1;
                                                          if (currentIndex == totalPicSize - 1) {
                                                              afterView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                          } else {
                                                              afterView.setFilterType(CustomFilterView.FilterType.TYPE_IMAGE);
                                                              afterView.setImage(mPhotoFilters.get(currentIndex + 1),
                                                                      getDisplaymetrics().widthPixels, getDisplaymetrics().heightPixels);
                                                          }
                                                          if (currentIndex == 0) {
                                                              beforeView.setFilterType(CustomFilterView.FilterType.TYPE_TEMP);
                                                              beforeView.setText(getPFTypeFace());
                                                          } else {
                                                              beforeView.setFilterType(CustomFilterView.FilterType.TYPE_IMAGE);
                                                              beforeView.setImage(mPhotoFilters.get(currentIndex - 1),
                                                                      getDisplaymetrics().widthPixels, getDisplaymetrics().heightPixels);
                                                          }
                                                      } else {
                                                          afterView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                          beforeView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                      }

                                                  } else {
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                  }

                                                  if (mIsHadEnableGPSPage) {
                                                      if (pageNo == FIX_FILTER_COUNT) {
                                                          if (mOnHandleRelativeLayoutEvent != null) {
                                                              mOnHandleRelativeLayoutEvent.isInGPSFilterPage(true);
                                                          }
                                                      } else if (pageNo == FIX_FILTER_COUNT - 1 || pageNo == 0) {
                                                          if (mOnHandleRelativeLayoutEvent != null) {
                                                              mOnHandleRelativeLayoutEvent.isInGPSFilterPage(false);
                                                          }
                                                      }
                                                  }
                                              }

                                              @Override
                                              public void onPageScrolled(int arg0, float arg1, int arg2) {
                                                  mIsViewPagerMove = true;
                                                  if (mOnHandleRelativeLayoutEvent != null) {
                                                      int pageNo = arg0 % mTotalFilters;
                                                      mOnHandleRelativeLayoutEvent.onScroll(pageNo, arg1);
                                                  }
                                              }

                                              @Override
                                              public void onPageScrollStateChanged(int arg0) {

                                              }
                                          }

        );
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2) % mTotalFilters);
    }

    private void resetViewByGPSEnable() {
        if (!BaseUtils.isGrantPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            return;
        }
        if (System.currentTimeMillis() - Preferences.getInstacne().getLongByKey(LAST_GET_WEATHER_TIME) > 3600 * 1000) {  //一小时取一次温度
            Location location = GPSUtils.getLocation(getContext());
            if (location != null) {
                final String locationStr = "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
                GBExecutionPool.getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        WeatherInfo weather = new YahooWeather().getWeatherString(getContext(), locationStr);
                        if (weather != null) {
                            Preferences.getInstacne().setValues(LAST_GET_WEATHER_TIME, System.currentTimeMillis());
                            Preferences.getInstacne().setValues(CURRENT_TEMPERATURE, weather.getCurrentTemp());
                        }
                    }
                });
            }
        }
        if (GPSUtils.isGpsEnable(getContext())) {
            mIsHadEnableGPSPage = false;
            ++mTotalFilters;
            String picFilters = Preferences.getInstacne().getValues(HomeActivity.FILTER_STICKERS, "");
            if (!TextUtils.isEmpty(picFilters)) {
                String[] names = picFilters.split(",");
                String photoPath;
                for (String name : names) {
                    photoPath = Preferences.getInstacne().getDownloadFilePathByName(name);
                    if (new File(photoPath).exists()) {
                        if (mPhotoFilters == null) {
                            mPhotoFilters = new ArrayList<>();
                        }
                        mPhotoFilters.add(photoPath);
                        ++mTotalFilters;
                    } else {
                        try {
                            String imageURL = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(name, "utf-8"), URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                            DownloadFileManager.getInstance().addDownloadFile(BaseApplication.getAppContext(), name, imageURL, name, 1);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            mIsHadEnableGPSPage = true;
            ++mTotalFilters;
        }
    }

    public void removeEnableGPSPage() {
        if (mIsHadEnableGPSPage && GPSUtils.isGpsEnable(getContext())) {
            mIsHadEnableGPSPage = false;
            int currentItem = getViewPager().getCurrentItem();
            int pageNo = currentItem % mTotalFilters;
            CustomFilterView beforeView = mViewList.get((currentItem - 1) % VIEW_PAGER_COUNT);
            CustomFilterView currentView = mViewList.get(getViewPager().getCurrentItem() % VIEW_PAGER_COUNT);
            CustomFilterView afterView = mViewList.get((currentItem + 1) % VIEW_PAGER_COUNT);

            if (pageNo == FIX_FILTER_COUNT - 1) {
                afterView.setFilterType(CustomFilterView.FilterType.TYPE_TEMP);
                afterView.setText(getPFTypeFace());
            } else if (pageNo == FIX_FILTER_COUNT) {
                currentView.setFilterType(CustomFilterView.FilterType.TYPE_TEMP);
                currentView.setText(getPFTypeFace());
            } else if (pageNo == 0) {
                beforeView.setFilterType(CustomFilterView.FilterType.TYPE_TEMP);
                beforeView.setText(getPFTypeFace());
            }
        }
    }

    public boolean isInEnableGPSPage() {
        return mIsHadEnableGPSPage && getViewPager().getCurrentItem() % mTotalFilters == FIX_FILTER_COUNT;
    }

    public CustomViewPager getViewPager() {
        return (CustomViewPager) findViewById(R.id.viewPager);
    }

    public void setCanScroll(boolean isCanScroll) {
        getViewPager().setCanScroll(isCanScroll);
    }

    public boolean isCanScroll() {
        return getViewPager().isCanScroll();
    }


    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position % mViewList.size()));
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int index = position % mViewList.size();
            View view = mViewList.get(index);
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            container.addView(mViewList.get(index));
            return mViewList.get(index);
        }

    };

    private long mTapTime;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsViewPagerMove = false;
                mTapTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if (!mIsViewPagerMove && System.currentTimeMillis() - mTapTime < 200) {
                    if (mOnHandleRelativeLayoutEvent != null) {
                        mOnHandleRelativeLayoutEvent.onClick();
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    public void setOnHandleRelativeLayoutEvent(OnHandleRelativeLayoutEvent onHandleRelativeLayoutEvent) {
        mOnHandleRelativeLayoutEvent = onHandleRelativeLayoutEvent;
    }

    public void destroyView() {
        mViewList.clear();
        mViewList = null;
    }

}

