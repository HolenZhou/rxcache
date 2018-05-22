package com.holen.rxcache.lib;

import android.util.Log;

import com.holen.rxcache.lib.callback.CacheType;
import com.holen.rxcache.lib.converter.GsonDiskConverter;
import com.holen.rxcache.lib.converter.IDiskConverter;
import com.holen.rxcache.lib.model.CacheMode;
import com.holen.rxcache.lib.utils.RxUtil;
import com.holen.rxcache.lib.utils.Utils;

import java.io.File;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by holenzhou on 2018/5/9.
 */

public class RxCacheProvider {

    private static final String TAG = "RxCacheProvider";
    private static volatile RxCacheProvider singleton = null;
    private RxCache.Builder rxCacheBuilder;
    private RxCache rxCache;
    public static final int DEFAULT_CACHE_NEVER_EXPIRE = -1;          //缓存过期时间，默认永久缓存//Okhttp缓存对象
    private CacheMode mCacheMode = CacheMode.DEFAULT;                 //缓存类型
    private long mCacheTime = -1;                                     //缓存时间
    private File mCacheDirectory;                                     //缓存目录
    private long mCacheMaxSize;                                       //缓存大小                       //RxCache请求的Builder

    private RxCacheProvider() {
        rxCacheBuilder = new RxCache.Builder()
                .diskConverter(new GsonDiskConverter());      //目前只支持Serializable和Gson缓存其它可以自己扩展
    }

    public static RxCacheProvider getInstance() {
        if (singleton == null) {
            synchronized (RxCacheProvider.class) {
                if (singleton == null) {
                    singleton = new RxCacheProvider();
                }
            }
        }
        return singleton;
    }

    public static RxCache getRxCache() {
        return getInstance().rxCacheBuilder.build();
    }

    /**
     * 对外暴露 RxCacheBuilder,方便自定义
     */
    public static RxCache.Builder getRxCacheBuilder() {
        return getInstance().rxCacheBuilder;
    }

    /**
     * 全局的缓存模式
     */
    public RxCacheProvider setCacheMode(CacheMode cacheMode) {
        mCacheMode = cacheMode;
        return this;
    }

    /**
     * 获取全局的缓存模式
     */
    public static CacheMode getCacheMode() {
        return getInstance().mCacheMode;
    }

    /**
     * 全局的缓存过期时间
     */
    public RxCacheProvider setCacheTime(long cacheTime) {
        if (cacheTime <= -1) cacheTime = DEFAULT_CACHE_NEVER_EXPIRE;
        mCacheTime = cacheTime;
        return this;
    }

    /**
     * 获取全局的缓存过期时间
     */
    public static long getCacheTime() {
        return getInstance().mCacheTime;
    }

    /**
     * 全局的缓存大小,默认50M
     */
    public RxCacheProvider setCacheMaxSize(long maxSize) {
        mCacheMaxSize = maxSize;
        return this;
    }

    /**
     * 获取全局的缓存大小
     */
    public static long getCacheMaxSize() {
        return getInstance().mCacheMaxSize;
    }

    /**
     * 全局设置缓存的版本，默认为1，缓存的版本号
     */
    public RxCacheProvider setCacheVersion(int cacheersion) {
        if (cacheersion < 0)
            throw new IllegalArgumentException("cacheersion must > 0");
        rxCacheBuilder.appVersion(cacheersion);
        return this;
    }

    /**
     * 全局设置缓存的路径，默认是应用包下面的缓存
     */
    public RxCacheProvider setCacheDirectory(File directory) {
        mCacheDirectory = Utils.checkNotNull(directory, "directory == null");
        rxCacheBuilder.diskDir(directory);
        return this;
    }

    /**
     * 获取缓存的路劲
     */
    public static File getCacheDirectory() {
        return getInstance().mCacheDirectory;
    }

    /**
     * 全局设置缓存的转换器
     */
    public RxCacheProvider setCacheDiskConverter(IDiskConverter converter) {
        rxCacheBuilder.diskConverter(Utils.checkNotNull(converter, "converter == null"));
        return this;
    }

    public void build() {
        rxCache = rxCacheBuilder.build();
    }

    /**
     * 异步清空缓存
     */
    public static void clearCache() {
        rxClear().compose(RxUtil.<Boolean>io_main())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        Log.i(TAG, "clearCache success!!!");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.i(TAG, "clearCache err!!!");
                    }
                });
    }

    /**
     * 异步移除缓存（key）
     */
    public static void removeCache(String key) {
        rxRemove(key).compose(RxUtil.<Boolean>io_main()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean result) throws Exception {
                Log.i(TAG, "removeCache result: " + result);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                Log.i(TAG, "removeCache err!!!");
            }
        });
    }

    public static <T> RequestApi api(Observable<T> api) {
        return new RequestApi(api);
    }


    /**
     * 同步获取缓存
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T get(String key, Class<T> clazz) {
        return getInstance().rxCache.get(key, (Type) clazz);
    }

    /**
     * 同步获取缓存
     *
     * @param key
     * @param cacheType
     * @param <T>
     * @return
     */
    public static <T> T get(String key, CacheType<T> cacheType) {
        return getInstance().rxCache.get(key, cacheType.getType());
    }

    /**
     * 同步获取缓存
     *
     * @param key
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T get(String key, Type type) {
        return getInstance().rxCache.get(key, type);
    }

    /**
     * 通过Rx的方式获取缓存，返回一个Observable
     *
     * @param clazz 保存的类型
     * @param key  缓存key
     */
    public static <T> Observable<T> load(String key, Class<T> clazz) {
        return load(key, (Type) clazz);
    }

    /**
     * 通过Rx的方式获取缓存，返回一个Observable
     *
     * @param cacheType 保存类型的包装类
     * @param key  缓存key
     */
    public static <T> Observable<T> load(String key, CacheType<T> cacheType) {
        return load(key, cacheType.getType());
    }

    /**
     * 通过Rx的方式获取缓存，返回一个Observable
     *
     * @param type 保存的类型
     * @param key  缓存key
     */
    public static <T> Observable<T> load(String key, Type type) {
        return getInstance().rxCache.load(key, type);
    }

    /**
     * 同步保存
     *
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public static <T> boolean put(String key, T value) {
        return put(key, value, -1);
    }

    /**
     * 同步保存
     *
     * @param <T>
     * @param key
     * @param value
     * @param cacheTime 毫秒ms
     */
    public static <T> boolean put(String key, T value, long cacheTime) {
        return getInstance().rxCache.put(key, value, cacheTime);
    }

    /**
     * 通过Rx的方式保存，返回一个Observable
     *
     * @param key   缓存key
     * @param value 缓存Value
     */
    public static <T> Observable<Boolean> save(String key, T value) {
        return save(key, value, -1);
    }

    public static <T> Observable<Boolean> save(String key, T value, long cacheTime) {
        return getInstance().rxCache.save(key, value, cacheTime);
    }

    /**
     * 是否包含
     */
    public static boolean containsKey(String key) {
        return getRxCache().containsKey(key);
    }

    /**
     * 获取删除缓存的Observable
     */
    public static Observable<Boolean> rxRemove(String key) {
        return getRxCache().remove(key);
    }

    /**
     * 获取清空缓存的Observable
     */
    public static Observable<Boolean> rxClear() {
        return getRxCache().clear();
    }

}
