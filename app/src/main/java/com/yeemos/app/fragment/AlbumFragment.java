package com.yeemos.app.fragment;

import android.Manifest;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.BaseUtils;
import com.gigabud.core.util.BitmapUtil;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.adapter.MediaFileAdapter;
import com.yeemos.app.adapter.ShowMediasAdapter;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;
import com.yeemos.yeemos.jni.ExtractDecodeEditEncodeMuxTest;
import com.yeemos.yeemos.jni.ShaderJNILib;
import com.yeemos.yeemos.jni.TextureRender;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gigabud on 16-6-21.
 */
public class AlbumFragment extends BaseFragment implements View.OnClickListener {

    public static final int MEDIA_TYPE_IMAGE = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;

    private MediaMetadataRetriever mMediaMetadataRetriever;

    public class MediaFileInfo {
        public String mediaLastFileAbsoluteName;
        public ArrayList<MediaInfo> mediaInfoList;
        public String mediaLastFileName;
    }

    private static final String CAMERA_PATH = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM;

    public class MediaInfo {
        public File mediaFile;
        public int mediaType;
        public String mediaTime;
        public int mediaRotateDegree;
        public long mediaAddTime;
    }

    private ArrayList<MediaFileInfo> mMediaFileInfos = new ArrayList<>();
    private Cursor mCursor;
    private ShowMediasAdapter mMediasAdapter;
    private MediaFileAdapter mMediaFileAdapter;

    private int mCameraUseType;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            goBack();
            return;
        }
        mCameraUseType = bundle.getInt(CameraForChatOrAvaterFragment.USE_CAMERA_TYPE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_album;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION_TWO;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        BitmapCacheManager.getInstance().evictAll();

        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.alphaView).setVisibility(View.GONE);
        view.findViewById(R.id.llAlbum).setOnClickListener(this);
        view.findViewById(R.id.alphaView).setOnClickListener(this);
    }

    public void onResume() {
        super.onResume();
        if (!BaseUtils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return;
        }
        if (mMediaFileAdapter == null || mMediaFileInfos.isEmpty()) {
            showLoadingDialog("", null, true);
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    getAlbumMedias(MEDIA_TYPE_IMAGE);
                    if (mCameraUseType == CameraFragment.TYPE_CAMERA_FOR_POST) {
                        getAlbumMedias(MEDIA_TYPE_VIDEO);
                    }
                    if (mMediaFileInfos != null && mMediaFileInfos.size() > 1) {
                        Collections.sort(mMediaFileInfos, new SortByFileName());
                    }
                    for (MediaFileInfo mediaFileInfo : mMediaFileInfos) {
                        if (mediaFileInfo.mediaInfoList != null && mediaFileInfo.mediaInfoList.size() > 1) {
                            Collections.sort(mediaFileInfo.mediaInfoList, new SortByMediaAddTime());
                        }
                    }
                    if (getView() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoadingDialog();
                                int initOpenIndex = -1;
                                MediaFileInfo mediaFileInfo;
                                for (int i = 0; i < mMediaFileInfos.size(); ++i) {
                                    mediaFileInfo = mMediaFileInfos.get(i);
                                    if (mediaFileInfo.mediaLastFileAbsoluteName.contains(CAMERA_PATH + "/Camera") && mediaFileInfo.mediaLastFileName.equalsIgnoreCase("Camera")) {
                                        initOpenIndex = i;
                                        break;
                                    }
                                }
                                if (initOpenIndex == -1) {
                                    for (int i = 0; i < mMediaFileInfos.size(); ++i) {
                                        mediaFileInfo = mMediaFileInfos.get(i);
                                        if (mediaFileInfo.mediaLastFileAbsoluteName.contains(CAMERA_PATH)) {
                                            initOpenIndex = i;
                                            break;
                                        }
                                    }
                                }
                                initOpenIndex = Math.max(initOpenIndex, 0);
                                initGridView(initOpenIndex);
                                initListView(initOpenIndex);
                            }
                        });
                    }
                }
            });
        }
    }

    private void getAlbumMedias(int mediaType) {  //mediaType==0表示image,mediaType==1表示video
        if (getActivity() == null) {
            return;
        }
        // 执行查询，返回一个cursor
        int filePathColumn;
        int bucketNameColumn;
        int fileContentType;
        int dateAdded;
        if (mediaType == MEDIA_TYPE_IMAGE) {
            mCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                            MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.DATE_ADDED}, null,
                    null, null);
            filePathColumn = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            bucketNameColumn = mCursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            fileContentType = mCursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
            dateAdded = mCursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
        } else {
            mCursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                            MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DATE_ADDED}, null,
                    null, null);
            filePathColumn = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            bucketNameColumn = mCursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            fileContentType = mCursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
            dateAdded = mCursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
        }
        mCursor.moveToLast();
        String mediaFilePath, bunketName, mediaLastPath;
        String fileType;
        long mediaAddTime;
        while (mCursor != null && mCursor.getCount() > 0) {
            fileType = mCursor.getString(fileContentType);
            mediaFilePath = mCursor.getString(filePathColumn);
            mediaLastPath = null;
            if (fileType != null) {
                if (fileType.equalsIgnoreCase("image/gif") ||
                        (mediaType == MEDIA_TYPE_VIDEO && (!fileType.equalsIgnoreCase("video/mp4") || mediaFilePath.endsWith(".3gp")))) {
                    if (mCursor.isFirst()) {
                        break;
                    }
                    mCursor.moveToPrevious();
                    continue;
                }
            } else {
                if (mCursor.isFirst()) {
                    break;
                }
                mCursor.moveToPrevious();
                continue;
            }
            try {
                bunketName = mCursor.getString(bucketNameColumn);
                mediaLastPath = mediaFilePath.split("/" + mCursor.getString(bucketNameColumn) + "/")[0] + ("/" + bunketName);
                mediaAddTime = mCursor.getLong(dateAdded);
            } catch (Exception e) {
                mediaAddTime = 0l;
            }
            if (TextUtils.isEmpty(mediaLastPath)) {
                continue;
            }
            addMediaFile(mediaLastPath, mediaFilePath, mediaType, mediaAddTime);
            try {
                if (mCursor.isFirst()) {
                    break;
                }
                mCursor.moveToPrevious();
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                        }
                    });
                }
                break;
            }
        }
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
            mCursor = null;
        }

        if (mMediaMetadataRetriever != null) {
            mMediaMetadataRetriever.release();
            mMediaMetadataRetriever = null;
        }
    }

    class SortByFileName implements Comparator {
        public int compare(Object o1, Object o2) {
            String name1 = ((MediaFileInfo) o1).mediaLastFileName.toLowerCase();
            String name2 = ((MediaFileInfo) o2).mediaLastFileName.toLowerCase();
            return name1.compareTo(name2);
        }
    }

    class SortByMediaAddTime implements Comparator {
        public int compare(Object o1, Object o2) {
            long mediaAddTime1 = ((MediaInfo) o1).mediaAddTime;
            long mediaAddTime2 = ((MediaInfo) o2).mediaAddTime;
            if (mediaAddTime1 > mediaAddTime2) {
                return -1;
            } else if (mediaAddTime1 == mediaAddTime2) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    private void initListView(int openIndex) {
        final ListView listView = (ListView) getView().findViewById(R.id.list);
        mMediaFileAdapter = new MediaFileAdapter(getActivity());
        listView.setAdapter(mMediaFileAdapter);
        mMediaFileAdapter.resetMediaFiles(mMediaFileInfos);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaFileInfo mediaFileInfo = mMediaFileInfos.get(position);
                ((TextView) getView().findViewById(R.id.tvAlbum)).setText(mediaFileInfo.mediaLastFileName);
                mMediasAdapter.resetMediaFileInfo(mediaFileInfo);
                listView.setVisibility(View.GONE);
                getView().findViewById(R.id.alphaView).setVisibility(View.GONE);

            }
        });
        ((TextView) getView().findViewById(R.id.tvAlbum)).setText(mMediaFileInfos.isEmpty() ? "" : mMediaFileInfos.get(openIndex).mediaLastFileName);
    }

    private void initGridView(int openIndex) {
        final GridView gridView = (GridView) getView().findViewById(R.id.gridView);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mMediasAdapter = new ShowMediasAdapter(this);
        gridView.setAdapter(mMediasAdapter);
        mMediasAdapter.resetMediaFileInfo(mMediaFileInfos.isEmpty() ? null : mMediaFileInfos.get(openIndex));
        mMediasAdapter.setItemWidth(dm.widthPixels / 3 - Utils.dip2px(getActivity(), 3));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ImageView iv = (ImageView) view.findViewById(R.id.iv);
                final MediaInfo info = (MediaInfo) iv.getTag(R.id.media_info);
                if (info != null && iv.getDrawable() != null) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                    if (mCameraUseType == CameraFragment.TYPE_CAMERA_FOR_CHAT) {
                        Bitmap bmp = BitmapUtil.getBitmapFromFile(info.mediaFile, dm.widthPixels, dm.heightPixels);
                        if (bmp == null) {
                            return;
                        }
                        DataManager.getInstance().setFilePath(Utils.saveJpegForChat(bmp, getActivity()));
                        bmp.recycle();
                        getActivity().finish();
                    } else if (mCameraUseType == CameraFragment.TYPE_CAMERA_FOR_POST) {
                        if (info.mediaType == MEDIA_TYPE_IMAGE) {
                            ShaderJNILib.destroySource();
                            String savePath = Utils.createImagePath(getActivity());
                            if (Utils.copyFile(getActivity(), info.mediaFile.getAbsolutePath(), savePath)) {
                                Bundle bundle = new Bundle();
                                bundle.putInt(EditPostFragment.SELECT_EMO_ID, 0);
                                bundle.putString(EditPostFragment.SOURCE_PATH, savePath);
                                bundle.putInt(EditPostFragment.FILTER_TYPE, Constants.PIC_SHADER_FILTER);
                                bundle.putBoolean(EditPostFragment.IS_FROM_ALBUM, true);
                                gotoPager(EditPostFragment.class, bundle);
                            }
                        } else {
                            showLoadingDialog("", null, true);
                            GBExecutionPool.getExecutor().execute(new Runnable() {
                                public void run() {
                                    int degree = info.mediaRotateDegree % 360;
                                    int filterType;
                                    if (degree > 45 && degree <= 135) {
                                        filterType = Constants.VIDEO_DEGREE_90;
                                    } else if (degree > 135 && degree <= 225) {
                                        filterType = Constants.VIDEO_DEGREE_180;
                                    } else if (degree > 225 && degree <= 315) {
                                        filterType = Constants.VIDEO_DEGREE_270;
                                    } else {
                                        filterType = Constants.VIDEO_DEGREE_0;
                                    }
                                    ExtractDecodeEditEncodeMuxTest test = new ExtractDecodeEditEncodeMuxTest();
                                    test.setContext(getActivity());
                                    final String savePath = Utils.createVideoPath(getActivity());
                                    boolean isSaved = false;
                                    try {
                                        test.init(info.mediaFile.getAbsolutePath(), savePath, filterType, TextureRender.USE_FOR_GET_NEW_VIDEO_FROM_ALBUM, null);
                                        test.testExtractDecodeEditEncodeMuxAudioVideo();
                                        isSaved = true;
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                    if (isSaved) {
                                        ShaderJNILib.destroySource();
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                hideLoadingDialog();
                                                Bundle bundle = new Bundle();
                                                bundle.putInt(EditPostFragment.SELECT_EMO_ID, 0);
                                                bundle.putString(EditPostFragment.SOURCE_PATH, savePath);
                                                bundle.putInt(EditPostFragment.FILTER_TYPE, Constants.VIDEO_DEGREE_0);
                                                bundle.putBoolean(EditPostFragment.IS_FROM_ALBUM, true);
                                                gotoPager(EditPostFragment.class, bundle);

                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                hideLoadingDialog();
                                                ((BaseActivity) getActivity()).showPublicDialog(null, ServerDataManager.getTextFromKey("pblc_txt_videofailed"),
                                                        ServerDataManager.getTextFromKey("pub_btn_ok"), null, false, true, null);
                                            }
                                        });
                                    }
                                }
                            });
                        }

                    } else {
                        Bitmap bmp = BitmapUtil.getBitmapFromFile(info.mediaFile, dm.widthPixels, dm.heightPixels);
                        if (bmp == null) {
                            return;
                        }
                        BitmapCacheManager.getInstance().put(PhotoPreviewFragment.PREVIEW_PICTURE, bmp);
                        Bundle b = new Bundle();
                        b.putInt(CameraForChatOrAvaterFragment.USE_CAMERA_TYPE, mCameraUseType);
                        gotoPager(PhotoPreviewFragment.class, b);
                    }
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        //    setIsReceiveBroadcast(!hidden);
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((BaseActivity) getActivity()).setScreenFull(false);
        } else {
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
                mCursor = null;
            }
            if (mMediaMetadataRetriever != null) {
                mMediaMetadataRetriever.release();
                mMediaMetadataRetriever = null;
            }
        }
    }

    private void addMediaFile(String mediaLastPath, String mediaPath, int mediaType, long mediaAddTime) {
        for (MediaFileInfo imageFileInfo : mMediaFileInfos) {
            if (imageFileInfo.mediaLastFileAbsoluteName.equals(mediaLastPath)) {
                File mediaFile = new File(mediaPath);
                if (mediaFile.exists()) {
                    MediaInfo mediaInfo = new MediaInfo();
                    mediaInfo.mediaFile = mediaFile;
                    mediaInfo.mediaType = mediaType;
                    mediaInfo.mediaAddTime = mediaAddTime;
                    if (mMediaMetadataRetriever == null) {
                        mMediaMetadataRetriever = new MediaMetadataRetriever();
                    }
                    try {
                        if (mediaType == MEDIA_TYPE_VIDEO) {
                            mMediaMetadataRetriever.setDataSource(mediaPath);
                            mediaInfo.mediaRotateDegree = Integer.parseInt(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                            mediaInfo.mediaTime = Utils.getHMSTime(Long.parseLong(mMediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)));
                        }
                    } catch (Exception e) {
                    }
                    imageFileInfo.mediaInfoList.add(mediaInfo);
                }
                return;
            }
        }
        File mediaFile = new File(mediaPath);
        if (mediaFile.exists()) {
            ArrayList<MediaInfo> list = new ArrayList<>();
            MediaInfo mediaInfo = new MediaInfo();
            mediaInfo.mediaFile = new File(mediaPath);
            mediaInfo.mediaType = mediaType;
            mediaInfo.mediaAddTime = mediaAddTime;
            if (mMediaMetadataRetriever == null) {
                mMediaMetadataRetriever = new MediaMetadataRetriever();
            }
            if (mediaType == MEDIA_TYPE_VIDEO) {
                try {
                    mMediaMetadataRetriever.setDataSource(mediaPath);
                    mediaInfo.mediaRotateDegree = Integer.parseInt(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                    mediaInfo.mediaTime = Utils.getHMSTime(Long.parseLong(mMediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)));
                } catch (Exception e) {
                    return;
                }
            }
            list.add(mediaInfo);
            MediaFileInfo mediaFileInfo = new MediaFileInfo();
            mediaFileInfo.mediaLastFileAbsoluteName = mediaLastPath;
            mediaFileInfo.mediaInfoList = list;
            String[] fileNames = mediaLastPath.split("/");
            if (fileNames.length > 0) {
                mediaFileInfo.mediaLastFileName = fileNames[fileNames.length - 1];
                mMediaFileInfos.add(mediaFileInfo);
            }
        }
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.tvChangeFile, "slctpht_btn_changfolder");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llAlbum:
                if (!BaseUtils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ((BaseActivity) getActivity()).requestPermission(BaseActivity.PERMISSION_WRITE_EXTERNAL_STORAGE_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    return;
                }
                if (mMediaFileAdapter == null) {
                    return;
                }
                ListView listView = (ListView) getView().findViewById(R.id.list);
                if (listView.getVisibility() == View.VISIBLE) {
                    listView.setVisibility(View.GONE);
                    getView().findViewById(R.id.alphaView).setVisibility(View.GONE);
                    break;
                }
                listView.setVisibility(View.VISIBLE);
                if (mMediaFileInfos.size() < 4) {
                    int height = Utils.dip2px(getActivity(), 36) * mMediaFileInfos.size();
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) listView.getLayoutParams();
                    lp.height = height;
                    listView.setLayoutParams(lp);
                }
                getView().findViewById(R.id.alphaView).setVisibility(View.VISIBLE);
                mMediaFileAdapter.notifyDataSetChanged();
                break;
            case R.id.alphaView:
                v.setVisibility(View.GONE);
                getView().findViewById(R.id.list).setVisibility(View.GONE);
                break;
            case R.id.btnBack:
                goBack();
                break;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        hideLoadingDialog();
        BitmapCacheManager.getInstance().evictAll();
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
            mCursor = null;
        }
        if (mMediaMetadataRetriever != null) {
            mMediaMetadataRetriever.release();
            mMediaMetadataRetriever = null;
        }
    }

}
