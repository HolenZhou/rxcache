package com.holen.rxcache.lib.exception;

/**
 * Created by holenzhou on 2018/5/16.
 */

public class RxCacheNullException extends RuntimeException {

    public RxCacheNullException() {
        super("cache is null");
    }

}
