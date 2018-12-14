package com.common.mentiontextview;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.HashTagBean;
import com.gigabud.core.util.BaseUtils;
import com.gigabud.core.util.ReflectUtil;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * 支持@ #动态弹出下拉列表
 *
 * @author xiangwei.ma
 */
public class MentionTextView extends AutoCompleteTextView {

    public static final int POPUP_HEIGHTH = 250;
    private static final int MESSAGE_TEXT_CHANGED = 100;
    private static final int DEFAULT_AUTOCOMPLETE_DELAY = 150;
    private int mBeginMentionIndex = -1;
    private int mEndMentionIndex = -1;
    private boolean bIsPupupDown = false; // tag popup up/down

    private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;

    private MentionTextViewWatcher mMentionTextViewWatcher = null;
    private String mCurWillSearchMentionKey; // will searching key
    private ArrayList<String> mArrTrigerKey = null;

    public ListView mListView;

    public MentionTextView(Context context) {
        super(context);
        initMentionTextView();
    }

    public MentionTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMentionTextView();
    }

    public MentionTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMentionTextView();
    }

    public void setIsPupupDown(boolean bFlag) {
        bIsPupupDown = bFlag;
    }

    /**
     * add triger key
     *
     * @param trigerKey
     */
    public void addMentionTrigerkey(char trigerKey) {
        if (mArrTrigerKey == null) {
            mArrTrigerKey = new ArrayList<String>();
        }

        mArrTrigerKey.add(Character.toString(trigerKey));
    }

    public boolean checkTriger(char trigerKey) {
        if (mArrTrigerKey != null) {
            for (int i = 0; i < mArrTrigerKey.size(); ++i) {
                String strChara = mArrTrigerKey.get(i);
                if (strChara.charAt(0) == trigerKey)
                    return true;

            }

        }
        return false;
    }

    int[] screenLocation = new int[2];
    Point p = new Point();

    protected void initMentionTextView() {
        addTextChangedListener(getMentionTextViewWatcher());

        this.setDropDownBackgroundResource(R.drawable.mentiontextview_popup);

        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                int width = getWidth();
                int height = getHeight();

                // Log.i("MentionTextViewWatcher", "width:" + width + " height:"
                // + height );
                setDropDownHeight((int) BaseUtils.convertDpToPixel(
                        POPUP_HEIGHTH, BaseApplication.getAppContext()));
                setDropDownWidth(width);

                if (bIsPupupDown) {
                    int nPopupHeighth = (int) BaseUtils.convertDpToPixel(
                            POPUP_HEIGHTH, BaseApplication.getAppContext());
                    setDropDownVerticalOffset((int) -0.2 * (height + nPopupHeighth));
                } else {
                    int nPopupHeighth = (int) BaseUtils.convertDpToPixel(
                            POPUP_HEIGHTH, BaseApplication.getAppContext());
                    if (Build.VERSION.SDK_INT >= 24 && isPopupShowing()) {
                        getDisplay().getSize(p);
                        getLocationOnScreen(screenLocation);
                        ListPopupWindow lpw = (ListPopupWindow) ReflectUtil.getFieldNoException(AutoCompleteTextView.class, MentionTextView.this, "mPopup");
                        PopupWindow pw = (PopupWindow) ReflectUtil.getFieldNoException(ListPopupWindow.class, lpw, "mPopup");
                        View popupDectorView = (View) ReflectUtil.getFieldNoException(PopupWindow.class, pw, "mDecorView");
                        int targetY = screenLocation[1] - nPopupHeighth;
                        if (popupDectorView != null && popupDectorView.getLayoutParams() != null && ((WindowManager.LayoutParams) popupDectorView.getLayoutParams()).y != targetY) {
                            ((WindowManager.LayoutParams) popupDectorView.getLayoutParams()).y = targetY;
                            ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).updateViewLayout(popupDectorView, popupDectorView.getLayoutParams());
                        }
                    } else {
                        setDropDownVerticalOffset(-1 * (height + nPopupHeighth));
                    }
                }
                return true;
            }
        });
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = parent.getAdapter().getItem(position);
                if (object.getClass().isAssignableFrom(HashTagBean.class)) {
                    setNewText(((HashTagBean) object).getHasTag());
                } else {
                    setNewText(((BasicUser) object).getUserName());
                }
            }
        });
    }

    private MentionTextViewWatcher getMentionTextViewWatcher() {
        if (mMentionTextViewWatcher == null)
            mMentionTextViewWatcher = new MentionTextViewWatcher();
        return mMentionTextViewWatcher;

    }

    // bottomLLId
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MentionTextView.super.performFiltering((CharSequence) msg.obj,
                    msg.arg1);
        }
    };

    public void showDropDown() {
        MentionAdapter adapter = (MentionAdapter) getAdapter();
        if (adapter != null) {
            String curSearchingMentionKey = adapter.getCurMentionContent();
            if (curSearchingMentionKey != null
                    && mCurWillSearchMentionKey != null) {
                if (curSearchingMentionKey.equals(mCurWillSearchMentionKey)) {
                    super.showDropDown();
                }
            }
        }
    }


    private class MentionTextViewWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (mListView != null) {
                ListAdapter adapter = mListView.getAdapter();
                if (adapter != null && adapter.getCount() > 0) {
                    mListView.setSelection(adapter.getCount() - 1);
                }
            }

            int nCursorIndex = 0;
            Editable spannable = getText();
            nCursorIndex = Selection.getSelectionStart(spannable) - 1;
            if (nCursorIndex < 0) {
                mBeginMentionIndex = -1;
                mEndMentionIndex = -1;
                nCursorIndex = 0;
            }
            if (nCursorIndex < s.length()) {
                if (checkTriger(s.charAt(nCursorIndex))) {
                    mBeginMentionIndex = nCursorIndex;
                }
                if (mBeginMentionIndex >= s.length()) { // 表明'@'字符不在字符串中
                    mBeginMentionIndex = -1;
                    mCurWillSearchMentionKey = null;
                    MentionAdapter adapter = (MentionAdapter) getAdapter();
                    if (adapter != null) {
                        adapter.resetCurMentionContent();
                    }
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, null),
                            mAutoCompleteDelay); // dealy call
                }
                if (mBeginMentionIndex != -1) // Need get data from network
                {
                    // check '@' is deleted
                    if (checkTriger(s.charAt(mBeginMentionIndex))) {
                        int nTemoBegin = mBeginMentionIndex;
                        int nTempEnd = Selection.getSelectionStart(spannable);
                        if (nTemoBegin < nTempEnd) {

                            CharSequence temp = s.subSequence(nTemoBegin,
                                    nTempEnd);
                            mCurWillSearchMentionKey = temp.toString();
                            mHandler.removeMessages(MESSAGE_TEXT_CHANGED); // remove
                            // old
                            // message
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(
                                    MESSAGE_TEXT_CHANGED, temp),
                                    mAutoCompleteDelay); // dealy call

                        } else {

                        }
                    }
                }
                mEndMentionIndex = start + count;
            }

        }
    }


    /**
     * 选择下拉ListView，会默认replace old text
     **/

    public void setNewText(String text) {
        clearComposingText();

        removeTextChangedListener(getMentionTextViewWatcher());

        String contentString = getText().toString();
        String startString = contentString.subSequence(0, mBeginMentionIndex + 1).toString();
        String endString = contentString.substring(mEndMentionIndex).toString();

        int nCursorIndex = 0;
        contentString = startString + text.toString() + " " + endString;
        nCursorIndex = startString.length() + text.toString().length() + 1;

        setText(contentString);

        // make sure we keep the caret at the end of the text view
        Editable spannable = getText();
        Selection.setSelection(spannable, nCursorIndex);

        mBeginMentionIndex = -1;
        mEndMentionIndex = -1;
        addTextChangedListener(getMentionTextViewWatcher());
    }

    protected void replaceText(CharSequence text) {

    }

    public void setAutoCompleteDelay(int autoCompleteDelay) {
        mAutoCompleteDelay = autoCompleteDelay;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {

    }

    @Override
    public void onFilterComplete(int count) {
        super.onFilterComplete(count);
    }
}
