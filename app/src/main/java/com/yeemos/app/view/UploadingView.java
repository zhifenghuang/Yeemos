package com.yeemos.app.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.YeemosTask;
import com.gigabud.core.task.ITask;
import com.gigabud.core.task.ITaskListener;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

/**
 * 上传进度显示视图
 *
 * @author Damon
 */
public class UploadingView extends LinearLayout implements OnClickListener, ITaskListener {
    private UploadingViewListener listener;

    private PostBean postBean;

    private YeemosTask task;

    private int progress;

    private boolean flag;

    @Override
    public void start(ITask task) {
        startProgressThread();
    }

    @Override
    public void error(final ITask task) {
        if (this.task == task) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((YeemosTask) task).setResult(-1);
                    setProgress(-1);
                }
            });
        }
    }

    @Override
    public void success(ITask task) {
        this.progress = 100;
        flag = false;
        if (this.task == task && listener != null) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setProgress(100);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDone(postBean);
                        }
                    }, 1000);
                }
            });
        }
    }

    private void startProgressThread() {
        if (flag) {
            return;
        }
        flag = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag && progress < 90) {
                    ++progress;
                    if (progress <= 90) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setProgress(progress);
                            }
                        });
                    }
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                flag = false;
            }
        }).start();
    }

    @Override
    public void progress(ITask task, final int progress) {
        if (this.task == task) {
            this.progress = progress;
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setProgress(progress);
                }
            });
        }
    }

    public interface UploadingViewListener {
        void onReloadBtnPressed(ITask task);

        void onDeleteBtnPressed(ITask task);

        void onDone(PostBean postBean);
    }

    public UploadingView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.listview_upload, this);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        progress = 0;
        flag = false;
        startProgressThread();
    }

    public UploadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public UploadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(YeemosTask task, PostBean postBean) {
        this.postBean = postBean;
        this.task = task;
        task.addListener(this);
        getProgressBar().setVisibility(View.GONE);
        refreshUI();
    }

    public void refreshUI() {
        if (task.getResult() == -1) {
            setProgress(-1);
        } else if (task.getResult() == -2) {
            setProgress(100);
            listener.onDone(postBean);
        } else {
            setProgress(task.getResult());
        }
        getReloadImgView().setOnClickListener(this);
        getDeleteImgView().setOnClickListener(this);
        getReloadImgView().setClickable(true);
        getDeleteImgView().setClickable(true);

        if (postBean != null) {
            getUploadImageView().setViewWH(Utils.dip2px(getContext(), 50), Utils.dip2px(getContext(), 50));
            getUploadImageView().setPostBean(postBean);
        }
    }

    public void setListener(UploadingViewListener listener) {
        this.listener = listener;
    }

    /**
     * 设置进度条进度
     *
     * @param progress -1,失败,其他,正常
     */
    public void setProgress(final int progress) {
        if (progress == -1) {
            getReloadImgView().setVisibility(View.VISIBLE);
            getDeleteImgView().setVisibility(View.VISIBLE);
            getTextView().setText(ServerDataManager.getTextFromKey("pub_txt_uploadingfailed"));
            getProgressBar().setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_menu_fail));
            getTextView().setTextColor(getResources().getColor(R.color.color_187_187_187));
        } else {
            getProgressBar().setProgress(progress);
            getReloadImgView().setVisibility(View.INVISIBLE);
            getDeleteImgView().setVisibility(View.INVISIBLE);
            getProgressBar().setVisibility(View.VISIBLE);
            getProgressBar().setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_menu));
            if (progress >= 100) {
                getUploadImageView().setImageResource(R.drawable.up_tick);
                getUploadImageView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                getTextView().setText(ServerDataManager.getTextFromKey("pblc_txt_finishingup"));
                getTextView().setTextColor(getResources().getColor(R.color.color_45_223_227));
            } else {
                getTextView().setText(ServerDataManager.getTextFromKey("pub_txt_uploading"));
                getTextView().setTextColor(getResources().getColor(R.color.color_88_89_91));
            }
        }
    }


    private ProgressBar getProgressBar() {
        return (ProgressBar) findViewById(R.id.progressBar);
    }

    private TextView getTextView() {
        return (TextView) findViewById(R.id.tvUploading);
    }

    private ImageView getReloadImgView() {
        return (ImageView) findViewById(R.id.imgReload);
    }

    private ImageView getDeleteImgView() {
        return (ImageView) findViewById(R.id.imgDelete);
    }

    private CustomUrlImageView getUploadImageView() {
        return (CustomUrlImageView) findViewById(R.id.imgUploading);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgReload:
                if (listener != null) {
                    listener.onReloadBtnPressed(task);
                    task.setResult(0);
                    setProgress(0);
                    if (postBean != null) {
                        getUploadImageView().setViewWH(Utils.dip2px(getContext(), 50), Utils.dip2px(getContext(), 50));
                        getUploadImageView().setPostBean(postBean);
                    }
                }
                break;
            case R.id.imgDelete:
                if (listener != null) {
                    listener.onDeleteBtnPressed(task);
                }
                break;
            default:
                break;
        }
    }


}
