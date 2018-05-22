package com.holen.rxcache;

/**
 * Created by holenzhou on 2018/5/22.
 */

public class Model {

    public long id;
    public String name;
    public String sex;
    public String city;

    public Model() {

    }

    public Model(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Model{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
