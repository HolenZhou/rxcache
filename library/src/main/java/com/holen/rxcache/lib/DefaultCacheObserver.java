package com.holen.rxcache.lib;

import android.util.Log;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by holenzhou on 2018/5/21.
 */
@SuppressWarnings(value = "unchecked")
public class DefaultCacheObserver<T> implements Observer<T> {

    private static final String TAG = "RxCache";

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T result) {
        if (result instanceof Boolean) {
            String msg = ((Boolean) result) ? "handle cache success" : "handle cache failed";
            Log.d(TAG, msg);
        } else {
            Log.d(TAG, "load cache result: " + result.toString());
        }
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "save or load error", e);
    }

    @Override
    public void onComplete() {

    }
}
