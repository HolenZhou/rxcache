package com.holen.rxcache.lib.core;

public class DynamicKey {
    private String key = "";
    private int page = -1;
    private String user = "";

    public DynamicKey(String key, int page) {
        this(key, page, "");
    }

    public DynamicKey(String key, String user) {
        this(key, -1, user);
    }

    public DynamicKey(String key, int page, String user) {
        this.key = key;
        this.page = page;
        this.user = user;
    }

    public String getDynamicKey() {
        return key + "_" + user + (page >= 0 ? "_" + page : "");
    }

}
