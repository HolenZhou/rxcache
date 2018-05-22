package com.holen.rxcache;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.holen.rxcache.lib.DefaultCacheObserver;
import com.holen.rxcache.lib.RxCache;
import com.holen.rxcache.lib.RxCacheProvider;
import com.holen.rxcache.lib.callback.CacheType;
import com.holen.rxcache.lib.converter.SerializableDiskConverter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by holenzhou on 2018/5/22.
 */

public class NativeCacheActivity extends AppCompatActivity {

    private Model model;
    private static final String TAG = "NativeCacheActivity";
    private static final String CACHE_KEY = "NativeCacheActivity";
    private static final String CACHE_WITH_TIME_KEY = "NativeCacheActivity_time";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_cache);
        model = new Model();
        model.id = 110;
        model.name = "holen";
        model.sex = "男";
        model.city = "Shenzhen";
    }

    public void put(View view) {
        RxCacheProvider.put(CACHE_KEY, model);
    }

    public void get(View view) {
        Model result = RxCacheProvider.get(CACHE_KEY, Model.class);
        Log.d(TAG, "get: " + result);
    }

    public void save(View view) {
        RxCacheProvider.save(CACHE_KEY, model)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultCacheObserver<Boolean>());
    }

    public void load(View view) {
        RxCacheProvider.load(CACHE_KEY, Model.class)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Model>() {
                    @Override
                    public void accept(Model result) throws Exception {
                        Log.d(TAG, "accept: " + result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "throwable: ", throwable);
                    }
                });
    }

    public void saveTime(View view) {
        List<Model> modelList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            modelList.add(new Model(i, "holen" + i));
        }
        RxCacheProvider.put(CACHE_WITH_TIME_KEY, modelList, 10 * 1000); // 缓存10s
        // or
//        RxCacheProvider.save(CACHE_WITH_TIME_KEY, modelList, 10 * 1000).subscribe(new DefaultCacheObserver<>());
    }

    public void cacheType(View view) {
        List<Model> modelList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            modelList.add(new Model(i, "holen" + i));
        }
        List<Model> result = RxCacheProvider.get(CACHE_WITH_TIME_KEY, new CacheType<List<Model>>() {
        });
        Log.d(TAG, "cacheType: " + result);
    }

    public void remove(View view) {
        RxCacheProvider.removeCache(CACHE_KEY);
    }

    public void rxRemove(View view) {
        RxCacheProvider.rxRemove(CACHE_KEY)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    public void contains(View view) {
        boolean contain = RxCacheProvider.containsKey(CACHE_KEY);
        Log.d(TAG, "contains: " + contain);
    }

    public void clear(View view) {
        RxCacheProvider.clearCache();
        // or
        RxCacheProvider.rxClear()
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    public void yourRxCache(View view) {
        RxCache rxCache = RxCacheProvider.getRxCacheBuilder()
                .diskConverter(new SerializableDiskConverter())
                .build();
        User user = new User(222, "Sherlock");
        rxCache.put("user-cache", user);
        User result = rxCache.get("user-cache", User.class);
        Log.d(TAG, "yourRxCache: " + result);
    }
}
