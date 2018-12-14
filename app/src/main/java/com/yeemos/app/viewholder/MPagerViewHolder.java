package com.yeemos.app.viewholder;

import android.support.v4.app.Fragment;
import android.view.View;

public interface MPagerViewHolder<B> {
	abstract void viewHolder(View convertView, Fragment frg);

	abstract void fill(B bean, int position);
}
