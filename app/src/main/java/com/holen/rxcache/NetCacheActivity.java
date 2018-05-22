/*
 * Copyright (C) 2017 zhouyou(478319399@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.holen.rxcache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.holen.rxcache.lib.RxCacheProvider;
import com.holen.rxcache.lib.callback.CacheCallback;
import com.holen.rxcache.lib.model.CacheMode;
import com.holen.rxcache.lib.model.CacheResult;

import io.reactivex.functions.Consumer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * <p>描述：缓存使用介绍</p>
 * 作者： zhouyou<br>
 * 日期： 2017/7/6 16:25 <br>
 * 版本： v1.0<br>
 */
public class NetCacheActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CacheActivity";
    TextView cache_content_txt;
    private GithubService githubService;
    private CacheMode mRxCacheMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_cache);
        cache_content_txt = (TextView) findViewById(R.id.cache_content);
        findViewById(R.id.default_cache).setOnClickListener(this);
        findViewById(R.id.first_remote).setOnClickListener(this);
        findViewById(R.id.first_cache).setOnClickListener(this);
        findViewById(R.id.only_remote).setOnClickListener(this);
        findViewById(R.id.only_cache).setOnClickListener(this);
        findViewById(R.id.cache_remote).setOnClickListener(this);
        findViewById(R.id.cache_remote_distinct).setOnClickListener(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        githubService = retrofit.create(GithubService.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.default_cache: /** 默认缓存，走的是okhttp cache*/
                mRxCacheMode = CacheMode.DEFAULT;
                break;
            case R.id.first_remote: /** 先请求网络，请求网络失败后再加载缓存 （自定义缓存Rxcache）*/
                mRxCacheMode = CacheMode.FIRSTREMOTE;
                break;
            case R.id.first_cache:/** 先加载缓存，缓存没有再去请求网络 （自定义缓存Rxcache）*/
                mRxCacheMode = CacheMode.FIRSTCACHE;
                break;
            case R.id.only_remote: /** 仅加载网络，但数据依然会被缓存 （自定义缓存Rxcache）*/
                mRxCacheMode = CacheMode.ONLYREMOTE;
                break;
            case R.id.only_cache: /** 只读取缓存 （自定义缓存Rxcache）*/
                mRxCacheMode = CacheMode.ONLYCACHE;
                break;
            case R.id.cache_remote:/** 先使用缓存，不管是否存在，仍然请求网络，会回调两次（自定义缓存Rxcache）*/
                mRxCacheMode = CacheMode.CACHEANDREMOTE;
                break;
            case R.id.cache_remote_distinct:
                /** 先使用缓存，不管是否存在，仍然请求网络，
                 有缓存先显示缓存，等网络请求数据回来后发现和缓存是一样的就不会再返回，否则数据不一样会继续返回。
                 （目的是为了防止数据是一致的也会刷新两次界面）（自定义缓存Rxcache）*/
                mRxCacheMode = CacheMode.CACHEANDREMOTEDISTINCT;
                break;
        }
        requestGitCache();
//        requestGitCache2();
    }

    private void requestGitCache() {
        RxCacheProvider.api(githubService.getUser("HolenZhou"))
                .cacheKey(NetCacheActivity.class.getSimpleName())
                .cacheMode(mRxCacheMode)
                .buildCache(User.class)
                .subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        Log.d(TAG, "accept: " + user);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "throwable: ", throwable);
                    }
                });
    }

    private void requestGitCache2() {
        RxCacheProvider.api(githubService.getUser("HolenZhou"))
                .cacheKey(this.getClass().getSimpleName())
                .cacheMode(mRxCacheMode)
                .cacheTime(5 * 60 * 1000) // 5min
                .buildCache(new CacheCallback<User>() {
                })
                .subscribe(new Consumer<CacheResult<User>>() {
                    @Override
                    public void accept(CacheResult<User> userCacheResult) throws Exception {
                        Log.d(TAG, "accept: " + userCacheResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "throwable: " + throwable.getMessage(), throwable);
                    }
                });
    }

    /**
     * 移除缓存
     */
    public void onRemoveCache(View view) {
        RxCacheProvider.removeCache(this.getClass().getSimpleName());
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
