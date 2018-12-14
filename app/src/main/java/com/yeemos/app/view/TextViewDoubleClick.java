package com.yeemos.app.view;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by gigabud on 16-8-15.
 */
public class TextViewDoubleClick extends TextView {

    boolean dontConsumeNonUrlClicks = true;

    boolean linkHit;

    public TextViewDoubleClick(Context context) {
        super(context);
    }

    public TextViewDoubleClick(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewDoubleClick(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean res = super.onTouchEvent(event );

        return res;
    }

    public void setTextViewHTML(String html) {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        setText(strBuilder);
    }


    public static class LocalLinkMovementMethod extends LinkMovementMethod {
        static LocalLinkMovementMethod sInstance;

        private long start  =  0;
        private long end  =  0;
        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null)
                sInstance = new LocalLinkMovementMethod();

            return sInstance;
        }



        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer,
                                    MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    start  =  System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    end  =  System.currentTimeMillis();
                    Long time  = end-start;
                    if(time>500){
                        return Touch.onTouchEvent(widget, buffer, event);
                    }else{
                        break;
                    }

            }
            return super.onTouchEvent(widget, buffer, event);
        }
    }
}
