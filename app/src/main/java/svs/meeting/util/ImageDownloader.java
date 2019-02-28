package svs.meeting.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class ImageDownloader {

    public static ImageDownloader imageDownloader;
    public static ImageDownloader getInstance(){
        if(imageDownloader==null){
            synchronized (ImageDownloader.class){
                if(imageDownloader==null){
                    imageDownloader=new ImageDownloader();
                }
            }
        }
        return imageDownloader;
    }


    private static final String TAG = "ImageDownloader";
    private LruCache<String, Bitmap> lruCache;

    public ImageDownloader() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        Log.e("maxMemory","maxMemory="+maxMemory);
        int cacheSize = (int) (maxMemory / 8);
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    // 把Bitmap对象加入到缓存中
    public synchronized void  addBitmapToMemory(String key, Bitmap bitmap) {
        Log.i(TAG, "addBitmapToMemory: " + lruCache.size());
        if (getBitmapFromMemCache(key) == null) {
            lruCache.put(key, bitmap);
        }
    }

    // 从缓存中得到Bitmap对象
    public synchronized Bitmap getBitmapFromMemCache(String key) {
        Log.i(TAG, "lrucache size: " + lruCache.size());
        return lruCache.get(key);
    }

    // 从缓存中删除指定的Bitmap
    public void removeBitmapFromMemory(String key) {
        lruCache.remove(key);
    }


}
