package com.yeemos.app.manager;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by gigabud on 16-4-26.
 */
public class BitmapCacheManager extends LruCache<String, Bitmap> {

    private static BitmapCacheManager mBitmapCacheManager;

    private static final int MAX_SIZE = 2 * 1024 * 1024;

    private static Object mObject = new Object();

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    private BitmapCacheManager(int maxSize) {
        super(maxSize);
    }

    public static BitmapCacheManager getInstance() {
        if (mBitmapCacheManager == null) {
            synchronized (mObject) {
                if (mBitmapCacheManager == null) {
                    mBitmapCacheManager = new BitmapCacheManager(MAX_SIZE);
                }
            }
        }
        return mBitmapCacheManager;
    }
}
