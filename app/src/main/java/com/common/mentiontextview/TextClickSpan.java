package com.common.mentiontextview;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class TextClickSpan extends ClickableSpan{
	
	private OnTextClickListener listener;
	
	public TextClickSpan(OnTextClickListener listener){
		this.listener = listener;
	}
	@Override
	public void onClick(View widget) {
		// TODO Auto-generated method stub
		listener.clickTextView();
	}
	@Override
	public void updateDrawState(TextPaint ds) {
		// TODO Auto-generated method stub
		super.updateDrawState(ds);
		listener.setStyle(ds);
	}
}
