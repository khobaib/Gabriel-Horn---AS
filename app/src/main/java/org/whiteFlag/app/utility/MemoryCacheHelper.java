package org.whiteFlag.app.utility;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by Usama on 9/2/14.
 */
public class MemoryCacheHelper
{
    public static LruCache<String, Bitmap> getDefaultBitmapCache()
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4; // Use 1/4th of the available memory for this memory cache.

        LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, Bitmap bitmap)
            {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };

        return bitmapCache;
    }
}
