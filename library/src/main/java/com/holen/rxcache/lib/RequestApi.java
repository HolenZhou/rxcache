package com.holen.rxcache.lib;

import com.holen.rxcache.lib.callback.CacheCallback;
import com.holen.rxcache.lib.callback.CacheType;
import com.holen.rxcache.lib.converter.IDiskConverter;
import com.holen.rxcache.lib.core.DynamicKey;
import com.holen.rxcache.lib.fun.CacheResultFunc;
import com.holen.rxcache.lib.model.CacheMode;
import com.holen.rxcache.lib.model.CacheResult;
import com.holen.rxcache.lib.utils.RxUtil;
import com.holen.rxcache.lib.utils.Utils;

import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

/**
 * Created by holenzhou on 2018/5/9.
 */
public class RequestApi {

    private CacheMode cacheMode = CacheMode.DEFAULT;
    private long cacheTime = -1;
    private IDiskConverter diskConverter;
    private String cacheKey;
    private Observable api;

    public <T> RequestApi(Observable<T> api) {
        this.api = api;
        cacheMode = RxCacheProvider.getCacheMode();
        cacheTime = RxCacheProvider.getCacheTime();
    }

    public <T> Observable<T> buildCache(Class<T> clazz) {
        return doBuildCache(clazz);
    }

    public <T> Observable<T> buildCache(Type type) {
        return doBuildCache(type);
    }

    public <T> Observable<T> buildCache(CacheType<T> cacheType) {
        return doBuildCache(cacheType.getType());
    }

    public <T> Observable<CacheResult<T>> buildCache(CacheCallback<T> cacheCallback) {
        return doBuildCacheWithCallback(cacheCallback.getType());
    }

    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private <T> Observable<T> doBuildCache(Type type) {
        RxCache.Builder rxCacheBuilder = generateRxCache();
        RxCache rxCache = rxCacheBuilder.build();
        return api.compose(RxUtil.<T>io_main())
                .compose(rxCache.<T>transformer(cacheMode, type))
                .compose(new ObservableTransformer<CacheResult<T>, T>() {
                    @Override
                    public ObservableSource<T> apply(Observable<CacheResult<T>> upstream) {
                        return upstream.map(new CacheResultFunc<T>());
                    }
                });
    }

    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private <T> Observable<CacheResult<T>> doBuildCacheWithCallback(Type type) {
        RxCache.Builder rxCacheBuilder = generateRxCache();
        RxCache rxCache = rxCacheBuilder.build();
        return api.compose(RxUtil.io_main())
                .compose(rxCache.<T>transformer(cacheMode, type, true));
    }

    public RequestApi cacheMode(CacheMode cacheMode) {
        this.cacheMode = cacheMode;
        return this;
    }

    public RequestApi cacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
        return this;
    }

    public RequestApi dynamicKey(DynamicKey dynamicKey) {
        if (dynamicKey != null) {
            this.cacheKey = dynamicKey.getDynamicKey();
        }
        return this;
    }

    public RequestApi cacheTime(long cacheTime) {
        if (cacheTime <= -1) cacheTime = RxCacheProvider.DEFAULT_CACHE_NEVER_EXPIRE;
        this.cacheTime = cacheTime;
        return this;
    }

    /**
     * 设置缓存的转换器
     */
    public RequestApi cacheDiskConverter(IDiskConverter converter) {
        this.diskConverter = Utils.checkNotNull(converter, "converter == null");
        return this;
    }

    /**
     * 根据当前的请求参数，生成对应的RxCache和Cache
     */
    private RxCache.Builder generateRxCache() {
        final RxCache.Builder rxCacheBuilder = RxCacheProvider.getRxCacheBuilder();
        switch (cacheMode) {
            case DEFAULT://使用Okhttp的缓存
                break;
            case FIRSTREMOTE:
            case FIRSTCACHE:
            case ONLYREMOTE:
            case ONLYCACHE:
            case CACHEANDREMOTE:
            case CACHEANDREMOTEDISTINCT:
                if (diskConverter == null) {
                    rxCacheBuilder.cacheKey(Utils.checkNotNull(cacheKey, "cacheKey == null"))
                            .cacheTime(cacheTime);
                    return rxCacheBuilder;
                } else {
                    final RxCache.Builder cacheBuilder = RxCacheProvider.getRxCache().newBuilder();
                    cacheBuilder.diskConverter(diskConverter)
                            .cacheKey(Utils.checkNotNull(cacheKey, "cacheKey == null"))
                            .cacheTime(cacheTime);
                    return cacheBuilder;
                }
        }
        return rxCacheBuilder;
    }
}
