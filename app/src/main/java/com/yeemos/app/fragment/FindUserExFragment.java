package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.yeemos.app.R;

/**
 * Created by gigabud on 16-10-27.
 */

public class FindUserExFragment extends FindUserFragment {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.tvDone).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tvDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    @Override
    public void updateUIText() {
        super.updateUIText();
        setOnlineText(R.id.tvDone, "pblc_btn_done");
    }
}
