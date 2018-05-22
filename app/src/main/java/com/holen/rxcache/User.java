package com.holen.rxcache;

import java.io.Serializable;

/**
 * Created by holenzhou on 2018/5/7.
 */

public class User implements Serializable {

    public String login;
    public long id;
    public String avatar_url;
    public String url;
    public String type;

    public User(long id, String login) {
        this.id = id;
        this.login = login;
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", id=" + id +
                ", avatar_url='" + avatar_url + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
