package com.holen.rxcache;

import android.app.Application;

import com.holen.rxcache.lib.RxCacheProvider;
import com.holen.rxcache.lib.converter.GsonDiskConverter;

import java.io.File;


/**
 * Created by holenzhou on 2018/5/21.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RxCacheProvider.getInstance()
                .setCacheDirectory(new File(getExternalCacheDir(), "rxcache")) // 缓存目录，必须
                .setCacheDiskConverter(new GsonDiskConverter()) // 设置数据转换器默认使用GsonDiskConverter
                .setCacheMaxSize(50 * 1024 * 1024) // 最大缓存空间，默认50M
                .setCacheVersion(1)// 缓存版本为1
                .setCacheTime(-1) // 缓存时间，默认-1表示永久缓存
                .build();

    }
}
