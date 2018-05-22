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

package com.holen.rxcache.lib.stategy;


import android.util.Log;

import com.holen.rxcache.lib.RxCache;
import com.holen.rxcache.lib.model.CacheResult;

import java.lang.reflect.Type;
import java.util.ConcurrentModificationException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * <p>描述：实现缓存策略的基类</p>
 * 作者： zhouyou<br>
 * 日期： 2016/12/24 10:35<br>
 * 版本： v2.0<br>
 */
public abstract class BaseStrategy implements IStrategy {
    private static final String TAG = "BaseStrategy";

    <T> Observable<CacheResult<T>> loadCache(final RxCache rxCache, Type type, final String key, final long time, final boolean needEmpty) {
        Observable<CacheResult<T>> observable = rxCache.<T>load(key, type, time).flatMap(new Function<T, ObservableSource<CacheResult<T>>>() {
            @Override
            public ObservableSource<CacheResult<T>> apply(@NonNull T t) throws Exception {
//                if (t == null) {
//                    return Observable.error(new NullPointerException("Not find the cache!"));
//                }
                return Observable.just(new CacheResult<>(true, t));
            }
        });
        if (needEmpty) {
            observable = observable
                    .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends CacheResult<T>>>() {
                        @Override
                        public ObservableSource<? extends CacheResult<T>> apply(@NonNull Throwable throwable) throws Exception {
                            return Observable.empty();
                        }
                    });
        }
        return observable;
    }

    //请求成功后：异步保存
    <T> Observable<CacheResult<T>> loadRemote2(final RxCache rxCache, final String key, Observable<T> source, final boolean needEmpty) {
        Observable<CacheResult<T>> observable = source
                .map(new Function<T, CacheResult<T>>() {
                    @Override
                    public CacheResult<T> apply(@NonNull T t) throws Exception {
                        Log.i(TAG,"loadRemote result=" + t);
                        rxCache.save(key, t).subscribeOn(Schedulers.io())
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(@NonNull Boolean status) throws Exception {
                                        Log.i(TAG, "save status => " + status);
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(@NonNull Throwable throwable) throws Exception {
                                        if (throwable instanceof ConcurrentModificationException) {
                                            Log.i(TAG, "Save failed, please use a synchronized cache strategy :", throwable);
                                        } else {
                                            Log.i(TAG, throwable.getMessage());
                                        }
                                    }
                                });
                        return new CacheResult<T>(false, t);
                    }
                });
        if (needEmpty) {
            observable = observable
                    .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends CacheResult<T>>>() {
                        @Override
                        public ObservableSource<? extends CacheResult<T>> apply(@NonNull Throwable throwable) throws Exception {
                            return Observable.empty();
                        }
                    });
        }
        return observable;
    }

    //请求成功后：同步保存
    <T> Observable<CacheResult<T>> loadRemote(final RxCache rxCache, final String key, Observable<T> source, final boolean needEmpty) {
        Observable<CacheResult<T>> observable = source
                .flatMap(new Function<T, ObservableSource<CacheResult<T>>>() {
                    @Override
                    public ObservableSource<CacheResult<T>> apply(final @NonNull T t) throws Exception {
                        return  rxCache.save(key, t).map(new Function<Boolean, CacheResult<T>>() {
                            @Override
                            public CacheResult<T> apply(@NonNull Boolean aBoolean) throws Exception {
                                Log.i(TAG, "save status => " + aBoolean);
                                return new CacheResult<T>(false, t);
                            }
                        }).onErrorReturn(new Function<Throwable, CacheResult<T>>() {
                            @Override
                            public CacheResult<T> apply(@NonNull Throwable throwable) throws Exception {
                                Log.i(TAG, "save status => " + throwable);
                                return new CacheResult<T>(false, t);
                            }
                        });
                    }
                });
        if (needEmpty) {
            observable = observable
                    .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends CacheResult<T>>>() {
                        @Override
                        public ObservableSource<? extends CacheResult<T>> apply(@NonNull Throwable throwable) throws Exception {
                            return Observable.empty();
                        }
                    });
        }
        return observable;
    }
}
