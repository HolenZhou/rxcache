package com.holen.rxcache;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by holenzhou on 2018/5/7.
 */

public interface GithubService {

    @GET("/users/{user}")
    Observable<User> getUser(@Path("user") String user);

}