package com.yeemos.app.utils;

/**
 * Created by gigabud on 15-12-4.
 */

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.cache.GBCommonCache;
import com.gigabud.core.http.DownloadFileManager;
import com.gigabud.core.util.BitmapUtil;
import com.gigabud.core.util.DeviceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.chat.manager.RabbitMQManager;
import com.yeemos.app.fragment.BaseFragment;
import com.yeemos.app.fragment.HashTagsFragment;
import com.yeemos.app.fragment.MyInfoFragment;
import com.yeemos.app.fragment.TermsOrPrivacyFragment;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utils {
    public static ContentValues mContentValues = null;


    public static int determineDisplayOrientation(Context context, int defaultCameraId) {
        int displayOrientation = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(defaultCameraId, cameraInfo);

            int degrees = getRotationAngle(context);


            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                displayOrientation = (cameraInfo.orientation + degrees) % 360;
                displayOrientation = (360 - displayOrientation) % 360;
            } else {
                displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
            }
        }
        return displayOrientation;
    }

    public static int getRotationAngle(Context context) {
        int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    public static int getRotationAngle(int rotation) {
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    public static String createImagePath(Context context) {
//        long dateTaken = System.currentTimeMillis();
//        String title = Constants.FILE_START_NAME + dateTaken;
        String filename = UUID.randomUUID().toString() + Constants.IMAGE_EXTENSION;

        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + filename;
        return filePath;
    }

    public static String createImagePathForChat(Context context) {
//        long dateTaken = System.currentTimeMillis();
//        String title = Constants.FILE_START_NAME + dateTaken;
        String filename = UUID.randomUUID().toString();

        String dirPath = DownloadFileManager.SD_PATH + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + filename;
        return filePath;
    }

    public static String createAlbumtImagePath(Context context) {
        String title = UUID.randomUUID().toString();
        String filename = title + Constants.IMAGE_EXTENSION;

        String dirPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + filename;
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Images.ImageColumns.TITLE, title);
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.ImageColumns.DATA, filePath);
        mContentValues = values;
        return filePath;
    }

    public static String createProjectVideoPath(Context context) {
        String title = UUID.randomUUID().toString();
        String filename = title + Constants.VIDEO_EXTENSION;
        String filePath = genrateProjectFilePath(title, true, null);

        ContentValues values = new ContentValues(7);
        values.put(Video.Media.TITLE, title);
        values.put(Video.Media.DISPLAY_NAME, filename);
        values.put(Video.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(Video.Media.MIME_TYPE, "video/3gpp");
        values.put(Video.Media.DATA, filePath);
        mContentValues = values;

        return filePath;
    }

    public static String createCachePath(Context context) {
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        return dirPath;
    }

    public static String createYeemosVideoFile(Context context) {
        String fileName = UUID.randomUUID().toString() + Constants.VIDEO_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/" + Constants.PROJECT_NAME;
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        return dirPath + "/" + fileName;
    }

    public static String createYeemosPhotoFile(Context context) {
        String fileName = UUID.randomUUID().toString() + Constants.IMAGE_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/" + Constants.PROJECT_NAME;
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        return dirPath + "/" + fileName;
    }

    public static String createVideoPath(Context context) {
        String fileName = UUID.randomUUID().toString() + Constants.VIDEO_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        return dirPath + "/" + fileName;
    }

    public static String createAlbumVideoPath() {
        String title = UUID.randomUUID().toString();
        String fileName = title + Constants.VIDEO_EXTENSION;
        String albumPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera";
        File file = new File(albumPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String path = albumPath + "/" + fileName;
        ContentValues values = new ContentValues(7);
        values.put(Video.Media.TITLE, title);
        values.put(Video.Media.DISPLAY_NAME, fileName);
        values.put(Video.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(Video.Media.MIME_TYPE, "video/mp4");
        values.put(Video.Media.DATA, path);
        mContentValues = values;
        return path;
    }


    private static String genrateProjectFilePath(String uniqueId, boolean isFinalPath, File tempFolderPath) {
        String fileName = uniqueId + Constants.VIDEO_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/" + Constants.PROJECT_NAME + "/download";
        if (isFinalPath) {
            File file = new File(dirPath);
            if (!file.exists() || !file.isDirectory())
                file.mkdirs();
        } else
            dirPath = tempFolderPath.getAbsolutePath();
        String filePath = dirPath + "/" + fileName;
        return filePath;
    }

    public static class ResolutionComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size size1, Camera.Size size2) {
            if (size1.height != size2.height)
                return size1.height - size2.height;
            else
                return size1.width - size2.width;
        }
    }

    public static Bitmap rotateBmp(Bitmap bmp, float rotateDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
        bmp.recycle();
        bmp = null;
        return newBmp;
    }

    /**
     * 保存JPG图片
     *
     * @param bmp
     */
    public static String saveJpeg(Bitmap bmp, Context context) {
        String folder = createImagePath(context);
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return folder;
    }

    /**
     * 保存JPG图片
     *
     * @param bmp
     */
    public static String saveJpegWithPath(Bitmap bmp, String folder) {
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return folder;
    }

    /**
     * 保存JPG聊天图片
     *
     * @param bmp
     */
    public static String saveJpegForChat(Bitmap bmp, Context context) {
        String folder = createImagePathForChat(context);
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder + Constants.IMAGE_EXTENSION);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);

            fout = new FileOutputStream(folder + "_s" + Constants.IMAGE_EXTENSION);
            bos = new BufferedOutputStream(fout);
            getThumPic(bmp).compress(Bitmap.CompressFormat.JPEG, 70, bos);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return folder + Constants.IMAGE_EXTENSION;
    }

    private static Bitmap getThumPic(Bitmap bmp) {
        Matrix matrix = new Matrix();
        float scaleX = 264.0f / bmp.getWidth();
        float scalY = 465.0f / bmp.getHeight();
        if (scaleX > scalY) {
            matrix.postScale(scaleX, scaleX);
        } else {
            matrix.postScale(scalY, scalY);
        }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    /**
     * 保存到相册
     *
     * @param bmp
     */
    public static String savePhotoToAlbum(Bitmap bmp, Context context) {
        String folder = createAlbumtImagePath(context);
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, bos);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mContentValues);
        mContentValues = null;
        return folder;
    }

    /**
     * @param path
     */
    public static void deleteFile(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }


    /**
     * 将录好的视频在系统内注册
     */
    public static void registerVideo(Context context, String path) {
        if (mContentValues != null) {
            Uri videoTable = Uri.parse(Constants.VIDEO_CONTENT_URI);
            mContentValues.put(Video.Media.SIZE, new File(path).length());
            try {
                context.getContentResolver().insert(videoTable, mContentValues);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
            }
            mContentValues = null;
        }
    }

    /**
     * 复制文件
     *
     * @param oldPath
     * @param newPath
     */
    public static boolean copyFile(Context context, String oldPath, String newPath) {
        boolean isSuccessful = false;
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                inStream = new FileInputStream(oldPath); //读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                isSuccessful = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isSuccessful = false;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
                if (fs != null) {
                    fs.close();
                    fs = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                isSuccessful = false;
            }
        }

        registerVideo(context, newPath);
        return isSuccessful;
    }


    /**
     * @param st
     * @return
     */
    public static boolean isTextHadChar(String st) {
        //      StringBuilder builder = new StringBuilder();
        try {
            for (int i = 0; i < st.length(); i++) {
                char c = st.charAt(i);
                if (!Character.isLetterOrDigit(c) && !Character.isSpaceChar(c) && !Character.isWhitespace(c)) {
//                    String unicode = String.valueOf(c);
                    int code = (int) c;
                    if (code >= 0 && code <= 255) {
                        //      unicode = "\\\\u" + Integer.toHexString(c);
                        return true;
                    }
//                    builder.append(unicode);
                } else {
//                    builder.append(c);
                    return true;
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 根据图片名字来获取资源Id
     *
     * @param picName
     * @return
     */
    public static int getDrawableIdByName(String picName) {
        int resID = BaseApplication.getAppContext().getResources().getIdentifier(picName,
                "drawable", BaseApplication.getAppContext().getApplicationInfo().packageName);
        return resID;
    }


    /* *
     *
     * @param videoFile
     * @param fileName
     * @return
     */
    public static String saveVideoFirstFrame(String videoFile, String fileName) {
        Bitmap thumb = BitmapUtil.getVideoThumbnail(videoFile);
        if (thumb != null) {
            String frameName = fileName.substring(0, fileName.length() - 4) + Constants.IMAGE_EXTENSION;
            String savePath = Preferences.getInstacne().getDownloadFilePathByName(frameName);
            saveJpegWithPath(thumb, savePath);
            thumb.recycle();
            return savePath;
        }
        return null;
    }

    /* *
     * 保存视频第一帧的缩略图
     * @param videoFile
     * @param fileName
     * @return
     */
    public static String saveThumb(Bitmap bmp) {
        String filePath = "";
        if (bmp != null) {
            String thumbName = "small_" + UUID.randomUUID().toString() + Constants.IMAGE_EXTENSION;
            filePath = Preferences.getInstacne().getDownloadFilePathByName(thumbName);
            float scaleX = 320f / bmp.getWidth();
            float scaleY = 568f / bmp.getHeight();
            if (scaleX > 1.0f || scaleY > 1.0f) {
                saveJpegWithPath(bmp, filePath);
                bmp.recycle();
                bmp = null;
            } else {
                Matrix matrix = new Matrix();
                if (scaleX > scaleY) {
                    matrix.postScale(scaleX, scaleX);
                } else {
                    matrix.postScale(scaleY, scaleY);
                }
                Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                bmp.recycle();
                bmp = null;
                saveJpegWithPath(newBmp, filePath);
                newBmp.recycle();
                newBmp = null;
            }
        }
        return filePath;
    }

    /**
     * 取创建到现在的时间差
     *
     * @param time
     * @return
     */
    public static String getTime(long time) {
        time = getCurrentServerTime() - time;
        if (time < 0) {
            time = 0;
        }
        /**
         * pblc_txt_secondsago
         * pblc_txt_minutesago
         * pblc_txt_hoursago
         * pblc_txt_daysago
         * pblc_txt_weeksago
         **/
        int second = (int) Math.floor(time / 1000);
        if (second < 60) {
            return getDateString(second, ServerDataManager.getTextFromKey("pblc_txt_secondsago"));
        }
        int min = (int) Math.floor(second / 60);
        if (min < 60) {
            return getDateString(min, ServerDataManager.getTextFromKey("pblc_txt_minutesago"));
        }
        int hour = (int) Math.floor(min / 60);
        if (hour < 24) {
            return getDateString(hour, ServerDataManager.getTextFromKey("pblc_txt_hoursago"));
        }
        int day = (int) Math.floor(hour / 24);
        if (day < 7) {
            return getDateString(day, ServerDataManager.getTextFromKey("pblc_txt_daysago"));
        }
        return getDateString((int) Math.floor(day / 7), ServerDataManager.getTextFromKey("pblc_txt_weeksago"));
    }


    /**
     * 将时间转化为时分秒
     *
     * @param time
     * @return
     */
    public static String getHMSTime(long time) {
        if (time < 0) {
            time = 0;
        }
        int second = (int) Math.floor(time / 1000);
        if (second < 60) {
            if (second == 0) {
                second = 1;
            }
            return String.format("00:%02d", second);
        }
        int min = (int) Math.floor(second / 60);
        if (min < 60) {
            return String.format("%02d:%02d", min, second % 60);
        }
        int hour = (int) Math.floor(min / 60);
        if (hour > 100) {
            return String.format("%d:%02d:%02d", hour, min % 60, second % 60);
        }
        return String.format("%02d:%02d:%02d", hour, min % 60, second % 60);
    }

    /**
     * 取创建到现在的时间差
     *
     * @param time
     * @return
     */
    public static String getLastMessageTime(long time) {
        long detalTime = getCurrentServerTime() - time;
        if (detalTime < 0) {
            detalTime = 0;
        }
        int second = (int) Math.floor(detalTime / 1000);
        if (second < 60) {
            return getDateString(second, ServerDataManager.getTextFromKey("pblc_txt_secondsago"));
        }
        int min = (int) Math.floor(second / 60);
        if (min < 60) {
            return getDateString(min, ServerDataManager.getTextFromKey("pblc_txt_minutesago"));
        }
        int hour = (int) Math.floor(min / 60);
        if (hour < 6) {
            return getDateString(hour, ServerDataManager.getTextFromKey("pblc_txt_hoursago"));
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getCurrentServerTime());
        int day1 = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTimeInMillis(time);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);
        return day1 == day2 ? getTimeStrOnlyHour(time) : getTimeStrOnlyDate(time);
    }

    @SuppressLint("SimpleDateFormat")
    private static String getTimeStrOnlyHour(long time) {
        SimpleDateFormat mSdf = new SimpleDateFormat("HH:mm");
        Date dt = new Date(time);
        return mSdf.format(dt);
    }

    public static String getDateString(int time, String keyString) {
        return String.format(keyString, time);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeStrOnlyHourBySystem(Context context, boolean is24Format) {
        SimpleDateFormat mSdf;
        if (is24Format) {
            mSdf = new SimpleDateFormat("HH:mm");
        } else {
            mSdf = new SimpleDateFormat("hh:mm");
        }
        Date dt = new Date(Utils.getCurrentServerTime());
        return mSdf.format(dt);
    }

    public static String geAMOrPM() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        return hour >= 12 ? "PM" : "AM";
    }

    private static String getTimeStrOnlyDate(long time) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat mSdf = new SimpleDateFormat("MM-dd");
        Date dt = new Date(time);
        return mSdf.format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
    }

    private static String getTimeStrDateAndHour(long time) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat mSdf = new SimpleDateFormat("MM-dd HH:mm");
        Date dt = new Date(time);
        return mSdf.format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
    }


    public static String getWeek(Context context) {
        Calendar c = Calendar.getInstance();
        int w = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        String weekDay = context.getResources().getString(Constants.WEEKDAYS[w]);
        return weekDay;
    }

    public static String getMonth(Context context) {
        Calendar c = Calendar.getInstance();
        String month = context.getResources().getString(Constants.MONTHS[c.get(Calendar.MONTH)]);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return " " + month + " " + (day < 10 ? "0" + day : day);
    }

    /**
     * 获取日期格式
     *
     * @return
     */
    public static String getDateString(long date) {
        Time time = new Time();
        time.set(date);
        String moth = time.month + 1 + "";
        if (moth.length() < 2) {
            moth = "0" + moth;
        }
        String monthStr = time.year + "." + moth + "." + time.monthDay;
        return monthStr;
    }

    /**
     * Text点击跳转
     *
     * @param str
     * @param users
     * @param date
     * @return
     */
    public static SpannableString getKeywordClickable(String str, final ArrayList<BasicUser> users, String date, int colorId) {
        if (TextUtils.isEmpty(str)) {
            return new SpannableString("");
        }
        SpannableString ss = new SpannableString(str);
        Matcher matcher = Pattern.compile("@[A-Za-z0-9._\\-]{4,20}\\s?").matcher(str);
        Matcher matcher2 = Pattern.compile("#(\\w+)\\s?").matcher(str);
        int end = 0;
        while (matcher.find()) {
            String key = matcher.group();
            int start = end + str.substring(end).indexOf(key);
            end = start + key.length();
            String topicSting = str.substring(start, end);
            ss.setSpan(getClickableSpan(topicSting, colorId, null), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        end = 0;
        while (matcher2.find()) {
            String key = matcher2.group();
            int start = end + str.substring(end).indexOf(key);
            end = start + key.length();
            String topicSting = str.substring(start, end);
            ss.setSpan(getClickableSpan(topicSting, colorId, null), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        end = 0;
        for (int i = 0; users != null && i < users.size(); i++) {
            int start = end + str.substring(end).indexOf(users.get(i).getRemarkName());
            if (start <= end - 1) {
                break;
            }
            end = start + users.get(i).getRemarkName().length();
            String commenter = str.substring(start, end);
            ss.setSpan(getClickableSpan("@" + commenter, colorId, users.get(i)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (!TextUtils.isEmpty(date)) {
            int start = str.length() - date.length();
            ForegroundColorSpan dateSpan = new ForegroundColorSpan(
                    BaseApplication.getAppContext().getResources().getColor(R.color.text_home_page_gray));
            ss.setSpan(dateSpan, start, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new RelativeSizeSpan(0.85f), start, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    public static ClickableSpan getClickableSpan(final String clickableStr, final int colorId, final BasicUser user) {
        ClickableSpan cs = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String searchStr = clickableStr;
                if (user != null) {
                    searchStr = "@" + user.getUserName();
                }
                gotoPager(searchStr);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                //R.color.color_142_153_168
                ds.setColor(BaseApplication.getAppContext().getResources().getColor(colorId));
                ds.setUnderlineText(false);
            }
        };
        return cs;
    }

    /**
     * 跳转
     *
     * @param str
     */
    private static void gotoPager(String str) {
        String newStr = str.substring(1).trim();
        if (str.contains("#")) {
//            Bundle bundle = new Bundle();
//            bundle.putString(Constants.KEY_SEND_STRING_TO_HASHTAGSFRAGMENT, newStr.trim());
            DataManager.getInstance().setCurKeyWord(newStr.trim());
            BaseApplication.getCurFragment().gotoPager(HashTagsFragment.class, null);
        } else {
            DataManager.getInstance().setCurOtherUser(new BasicUser().setUserName(newStr.trim()));
//            BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
            if (newStr.equals(MemberShipManager.getInstance().getUserInfo().getUserName())) {
                BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
//                if (BaseApplication.getCurFragment().getActivity().getClass().isAssignableFrom(HomeActivity.class)) {
//                    BaseApplication.getCurFragment().gotoPager(UserCenterFragment.class, null);
//                    ((BaseActivity) BaseApplication.getCurFragment().getActivity()).changeProfileCompoundButtonState();
//                } else {
//                    BaseApplication.getCurFragment().gotoPager(UserCenterFragmentEx.class, null);
//                }
            } else {
                BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
            }
        }
    }


    /**
     * @param value
     * @return
     */
    public static String transformNumber(long value) {
        if (value < 0) {
            value = 0;
        }
        if (value < 10000) {
            return String.valueOf(value);
        }
        String newValue = (value / 1000) + ".";
        newValue += ((value % 1000) / 100 + "k");
        return newValue;
    }

    /**
     * @param value
     * @return
     */
    public static String transformKiloNumber(long value) {
        if (value < 0) {
            value = 0;
        }
        if (value < 1000) {
            return String.valueOf(value);
        }
        String newValue = (value / 1000) + ".";
        newValue += ((value % 1000) / 100 + "k");
        return newValue;
    }

    /**
     * 如果不是全屏需要获取View位置时，y值减去状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static ArrayList<BasicUser> getLocalContactsInfos(Context context) {
        ContentResolver cr = context.getContentResolver();
//        String str[] = {Phone.CONTACT_ID, Phone.DISPLAY_NAME, Phone.NUMBER, Phone.PHOTO_ID};
//        Cursor cur = cr.query(Phone.CONTENT_URI, str, null, null, null);

        Cursor cur = cr.query(android.provider.ContactsContract.Contacts.CONTENT_URI,
                new String[]{android.provider.ContactsContract.Contacts._ID}, null, null, null);
        BasicUser basicInfo;
        String id;
        String mimetype;
        ArrayList<BasicUser> localList = new ArrayList<>();
        if (cur != null) {
            while (cur.moveToNext()) {
//                if (TextUtils.isEmpty(cur.getString(cur.getColumnIndex(Phone.DISPLAY_NAME)))
//                        || cur.getString(cur.getColumnIndex(Phone.DISPLAY_NAME)).equals("")) {
//                    continue;
//                }
//                basicInfo = new BasicUser();
//                basicInfo.setServerBasicUser(false);
//                basicInfo.setMobile(cur.getString(cur.getColumnIndex(Phone.NUMBER)));
//                basicInfo.setNick(cur.getString(cur.getColumnIndex(Phone.DISPLAY_NAME)));
//                long contactid = cur.getLong(cur.getColumnIndex(Phone.CONTACT_ID));
//                long photoid = cur.getLong(cur.getColumnIndex(Phone.PHOTO_ID));
//                // 如果photoid 大于0 表示联系人有头像 ，如果没有给此人设置头像则给他一个默认的
//                if (photoid > 0) {
//                    basicInfo.setContactid(contactid);
//                }

                basicInfo = new BasicUser();
                basicInfo.setServerBasicUser(false);

                id = cur.getString(cur.getColumnIndex(android.provider.ContactsContract.Contacts._ID));

                //从一个Cursor获取所有的信息
                Cursor contactInfoCursor = cr.query(
                        android.provider.ContactsContract.Data.CONTENT_URI,
                        new String[]{android.provider.ContactsContract.Data.CONTACT_ID,
                                android.provider.ContactsContract.Data.MIMETYPE,
                                android.provider.ContactsContract.Data.DATA1,
                        },
                        android.provider.ContactsContract.Data.CONTACT_ID + "=" + id, null, null);

                basicInfo.setContactid(Integer.valueOf(id));
                while (contactInfoCursor.moveToNext()) {
//                    Log.v("AAAAA","*******");
                    mimetype = contactInfoCursor.getString(
                            contactInfoCursor.getColumnIndex(android.provider.ContactsContract.Data.MIMETYPE));
                    String value = contactInfoCursor.getString(
                            contactInfoCursor.getColumnIndex(android.provider.ContactsContract.Data.DATA1));

                    if (mimetype.contains("/name")) {
//                         Log.v("AAAAA","姓名=" + value);
                        basicInfo.setNick(value);
                    } else if (mimetype.contains("/im")) {
//                        Log.v("AAAAA", "聊天(QQ)账号=" + value);
                    } else if (mimetype.contains("/email")) {
//                        Log.v("AAAAA", "邮箱=" + value);
                        basicInfo.setEmail(value);
                    } else if (mimetype.contains("/phone")) {
//                        Log.v("AAAAA", "电话=" + value);
                        basicInfo.setMobile(value.replace(" ", ""));
                    } else if (mimetype.contains("/postal")) {
//                        Log.v("AAAAA", "邮编=" + value);
                    } else if (mimetype.contains("/photo")) {
//                        Log.v("AAAAA", "照片=" + value);
                    } else if (mimetype.contains("/group")) {
//                        Log.v("AAAAA", "组=" + value);
                    }
//                    Log.v("AAAAA","*******");
                }
                contactInfoCursor.close();
                if (!TextUtils.isEmpty(basicInfo.getNick())
                        && !basicInfo.getNick().equals("")
                        && !TextUtils.isEmpty(basicInfo.getMobile())
                        && !basicInfo.getMobile().equals("")) {
                    localList.add(basicInfo);
                }
            }
        }
        cur.close();
        return localList;

    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersion() {
        try {
            PackageManager manager = BaseApplication.getAppContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(BaseApplication.getAppContext().getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String MD5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            //获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            //使用指定的字节更新摘要
            mdInst.update(btInput);
            //获得密文
            byte[] md = mdInst.digest();
            //把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getEmoText(String md5Str) {

        int s1 = Integer.valueOf(md5Str.substring(0, 2), 16);
        int s2 = Integer.valueOf(md5Str.substring(2, 4), 16);
        int s3 = Integer.valueOf(md5Str.substring(30, 32), 16);
        int totalEmos = Constants.SYSTEM_EMO_IDS.length;
        return new String(Character.toChars(Constants.SYSTEM_EMO_IDS[s1 % totalEmos]))
                + new String(Character.toChars(Constants.SYSTEM_EMO_IDS[s2 % totalEmos]))
                + new String(Character.toChars(Constants.SYSTEM_EMO_IDS[s3 % totalEmos]));
    }

    /**
     * @param context
     * @param defaultId
     * @param path
     * @param iv
     * @Param File file
     */
    public static void loadImage(Context context, File file, int defaultId, String path, ImageView iv) {
        if (file != null && file.exists()) {
            loadImage(context, defaultId, Uri.fromFile(file), iv);
        } else {
            loadImage(context, defaultId, path, iv);
        }
    }

    /**
     * @param context
     * @param defaultId
     * @param path
     * @param iv
     * @Param File file
     */
    public static void loadImage(Context context, File file, int defaultId, String path, ImageView iv, String fileName) {
        if (file != null && file.exists()) {
            if (fileName.endsWith(Constants.GIF_EXTENSION)) {
                Glide.with(context)
                        .load(Uri.fromFile(file))
                        .asGif()
                        .placeholder(defaultId)
                        .error(defaultId)//load失敗的Drawable
                        .centerCrop()//中心切圖, 會填滿
                        .fitCenter()//中心fit, 以原本圖片的長寬為主
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(iv);
            } else {
                Glide.with(context)
                        .load(Uri.fromFile(file))
                        .placeholder(defaultId)
                        .error(defaultId)//load失敗的Drawable
                        .centerCrop()//中心切圖, 會填滿
                        .fitCenter()//中心fit, 以原本圖片的長寬為主
                        .crossFade()
                        .into(iv);
            }
        } else {
            if (fileName.endsWith(Constants.GIF_EXTENSION)) {
                Glide.with(context)
                        .load(path)
                        .asGif()
                        .placeholder(defaultId)
                        .error(defaultId)//load失敗的Drawable
                        .centerCrop()//中心切圖, 會填滿
                        .fitCenter()//中心fit, 以原本圖片的長寬為主
                        .crossFade()
                        .into(iv);
            } else {
                Glide.with(context)
                        .load(path)
                        .placeholder(defaultId)
                        .error(defaultId)//load失敗的Drawable
                        .centerCrop()//中心切圖, 會填滿
                        .fitCenter()//中心fit, 以原本圖片的長寬為主
                        .crossFade()
                        .into(iv);
            }
        }
    }

    /**
     * @param context
     * @param defaultId
     * @param path
     * @param iv
     */
    public static void loadImage(Context context, int defaultId, String path, ImageView iv) {
        if (iv.getMeasuredWidth() > 0 && iv.getMeasuredHeight() > 0) {
            Glide.with(context)
                    .load(path)
                    .override(iv.getMeasuredWidth(), iv.getMeasuredHeight())
                    .error(defaultId)//load失敗的Drawable
                    .placeholder(defaultId)
                    .centerCrop()//中心切圖, 會填滿
                    .fitCenter()//中心fit, 以原本圖片的長寬為主
                    .crossFade()
                    .into(iv);
        } else {
            Glide.with(context)
                    .load(path)
                    .error(defaultId)//load失敗的Drawable
                    .placeholder(defaultId)
                    .centerCrop()//中心切圖, 會填滿
                    .fitCenter()//中心fit, 以原本圖片的長寬為主
                    .crossFade()
                    .into(iv);
        }

    }

    /**
     * @param context
     * @param defaultId
     * @param uri
     * @param iv
     */
    public static void loadImage(Context context, int defaultId, Uri uri, ImageView iv) {
        if (iv.getMeasuredWidth() > 0 && iv.getMeasuredHeight() > 0) {
            Glide.with(context)
                    .load(uri)
                    .placeholder(defaultId)
                    .override(iv.getMeasuredWidth(), iv.getMeasuredHeight())
                    .error(defaultId)//load失敗的Drawable
                    .centerCrop()//中心切圖, 會填滿
                    .fitCenter()//中心fit, 以原本圖片的長寬為主
                    .crossFade()
                    .into(iv);
        } else {
            Glide.with(context)
                    .load(uri)
                    .placeholder(defaultId)
                    .error(defaultId)//load失敗的Drawable
                    .centerCrop()//中心切圖, 會填滿
                    .fitCenter()//中心fit, 以原本圖片的長寬為主
                    .crossFade()
                    .into(iv);
        }
    }

    public static void setSubText(TextView tv, String text, String subText, int textColor, int subTextColor) {
        int index = text.indexOf(subText);
        if (index >= 0) {
            SpannableString ss = new SpannableString(text);
            ss.setSpan(new ForegroundColorSpan(subTextColor), index, index + subText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ss);
            tv.setTextColor(textColor);
        } else {
            tv.setText(text);
            tv.setTextColor(textColor);
        }
    }

    public static void setSubText(TextView tv, String text, String subText1, String subText2, int textColor, int subTextColor1, int subTextColor2) {
        int index1 = text.indexOf(subText1);
        int index2 = text.indexOf(subText2);
        SpannableString ss = new SpannableString(text);
        if (index1 >= 0) {
            ss.setSpan(new ForegroundColorSpan(subTextColor1), index1, index1 + subText1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ss);
            tv.setTextColor(textColor);
        }

        if (index2 >= 0) {
            ss.setSpan(new ForegroundColorSpan(subTextColor2), index2, index2 + subText2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ss);
            tv.setTextColor(textColor);
        }
        if (index1 < 0 && index2 < 0) {
            tv.setText(text);
            tv.setTextColor(textColor);
        }
    }

    public static void setTermsPrivacyText(final BaseFragment fragment, TextView tv, String text, String subText1, String subText2, String subText3, int textColor, int subTextColor1, int subTextColor2, int subTextColor3) {
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        int index1 = text.indexOf(subText1);
        int index2 = text.indexOf(subText2);
        int index3 = text.indexOf(subText3);
        SpannableString ss = new SpannableString(text);
        if (index1 >= 0) {
            ss.setSpan(new ForegroundColorSpan(subTextColor1), index1, index1 + subText1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Bundle b = new Bundle();
                    b.putInt(TermsOrPrivacyFragment.HTML_TYPE, TermsOrPrivacyFragment.HTML_TERMS_OF_SERVICE);
                    fragment.gotoPager(TermsOrPrivacyFragment.class, b);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setUnderlineText(false);
                }
            }, index1, index1 + subText1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ss);
            tv.setTextColor(textColor);
        }

        if (index2 >= 0) {
            ss.setSpan(new ForegroundColorSpan(subTextColor2), index2, index2 + subText2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Bundle b = new Bundle();
                    b.putInt(TermsOrPrivacyFragment.HTML_TYPE, TermsOrPrivacyFragment.HTML_PRIVACY_POLICY);
                    fragment.gotoPager(TermsOrPrivacyFragment.class, b);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setUnderlineText(false);
                }
            }, index2, index2 + subText2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ss);
            tv.setTextColor(textColor);
        }

        if (index3 >= 3) {
            ss.setSpan(new ForegroundColorSpan(subTextColor3), index3, index3 + subText3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Bundle b = new Bundle();
                    b.putInt(TermsOrPrivacyFragment.HTML_TYPE, TermsOrPrivacyFragment.HTML_COMMUNITY);
                    fragment.gotoPager(TermsOrPrivacyFragment.class, b);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setUnderlineText(false);
                }
            }, index3, index3 + subText3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ss);
            tv.setTextColor(textColor);
        }

    }


    /**
     * 判断设备上是否安装app
     *
     * @param context
     * @return
     */
    public static boolean isAppAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(packageName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取聊天服务器时间
     *
     * @return
     */
    public static long getCurrentServerTime() {
        long lastServerTime = Preferences.getInstacne().getValues(RabbitMQManager.LAST_SERVER_TIME, 0l);
        long lastDeviceTime = Preferences.getInstacne().getValues(RabbitMQManager.CURRENT_DEVICE_TIME, 0l);
        if (lastServerTime > 0l) {
            if (lastDeviceTime > 0l && System.currentTimeMillis() > lastDeviceTime) {
                return lastServerTime + (System.currentTimeMillis() - lastDeviceTime);
            }
            Preferences.getInstacne().setValues(RabbitMQManager.CURRENT_DEVICE_TIME, System.currentTimeMillis());
            return lastServerTime;
        }
        return DeviceUtil.getGMTMileSecs();
    }


    public static void saveArrayCache(String key, Object object) {
        if (BaseApplication.getAppContext() != null) {
            saveCache(key, object == null ? "[]" : new Gson().toJson(object));
        }
    }

    public static void saveCache(String key, String value) {
        if (BaseApplication.getAppContext() != null) {
            GBCommonCache.get(BaseApplication.getAppContext()).put(key, value);
        }
    }

    public static <T> ArrayList<T> getCache(Class<T> dataClass, String key) {
        ArrayList<T> dataList = new ArrayList<T>();
        if (BaseApplication.getAppContext() != null) {
            String cacheStr = GBCommonCache.get(BaseApplication.getAppContext()).getAsString(key);
            if (!TextUtils.isEmpty(cacheStr)) {
                Gson gson = new Gson();
                JsonArray array = new JsonParser().parse(cacheStr).getAsJsonArray();
                for (JsonElement elem : array) {
                    dataList.add(gson.fromJson(elem, dataClass));
                }
            }
        }
        return dataList;
    }

    /**
     * 刷新媒体库
     *
     * @param context
     */
    public static void scanDeviceMedia(Context context) {
        IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentfilter.addDataScheme("file");
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));
    }

    /**
     * 获取屏幕宽度的像素
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度度的像素
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 清除缓存
     */
    public static void deleteCache(Context context) {
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File directory = new File(dirPath);
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}


