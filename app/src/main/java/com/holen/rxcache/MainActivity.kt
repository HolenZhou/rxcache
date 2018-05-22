package com.holen.rxcache

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun nativeCache(view: View) {
        startActivity(Intent(this, NativeCacheActivity::class.java))
    }

    fun networkCache(view: View) {
        startActivity(Intent(this, NetCacheActivity::class.java))
    }
}
