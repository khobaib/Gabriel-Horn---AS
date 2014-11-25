package org.JoeMommas.app.viewHelpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import org.JoeMommas.app.utility.MemoryCacheHelper;
import com.parse.ParseObject;


/**
 * Created by Usama on 8/21/14.
 */
public class ParseImageCacheAdapter<T extends ParseObject> extends BaseParseArrayAdapter<T>
{
    private LruCache<String, Bitmap> bitmapCache;

    public ParseImageCacheAdapter(Context context, QueryFactory query, int itemViewResource)
    {
        super(context, query, itemViewResource);
        this.bitmapCache = MemoryCacheHelper.getDefaultBitmapCache();
    }

    public LruCache<String, Bitmap> getBitmapCache()
    {
        return bitmapCache;
    }

    public void setBitmapCache(LruCache<String, Bitmap> bitmapCache)
    {
        this.bitmapCache = bitmapCache;
    }
}
