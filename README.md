# RxCache
A library of LRU cache and support Rx.

该库代码大部分借鉴于开源库[RxEasyHttp](https://github.com/zhou-you/RxEasyHttp)的[缓存模块](https://github.com/zhou-you/RxEasyHttp/wiki/cache#%E7%BC%93%E5%AD%98%E4%BD%BF%E7%94%A8)，特此声明并感谢！

## 简介

**Library项目地址**： **[rxcache](https://github.com/HolenZhou/rxcache)**

RxCache是一个本地缓存功能库，采用Rxjava+DiskLruCache来实现，线程安全内部采用ReadWriteLock机制防止频繁读写缓存造成的异常，可以独立使用，单独用RxCache来存储数据。也可以采用transformer与retrofit网络请求结合，让你的网络库实现网络缓存功能，而且支持适用于不同业务场景的六种缓存模式。

对于本地缓存，可以进行缓存读写操作（异步或同步）、判断缓存是否存在、根据key删除缓存（异步或同步）、清空缓存（异步或同步）等。缓存Key会自动进行MD5加密、可以提供设置缓存磁盘大小、缓存key、缓存时间、缓存存储的转换器、缓存目录、缓存Version等功能。本库不作为重点介绍。

## 本地缓存

### 1.全局缓冲初始化

使用RxCacheProvider来初始化app的全局缓冲配置
```
RxCacheProvider.getInstance()
        .setCacheDirectory(new File(getExternalCacheDir(), "rxcache")) // 缓存目录，必须
        .setCacheDiskConverter(new GsonDiskConverter()) // 设置数据转换器默认使用GsonDiskConverter
        .setCacheMaxSize(50 * 1024 * 1024) // 最大缓存空间，默认50M
        .setCacheVersion(1)// 缓存版本为1
        .setCacheTime(-1) // 缓存时间，默认-1表示永久缓存
        .build();
```

### 2.同步写入和读取缓存
```
// 同步写入缓存
Model model = new Model(110, "holen"); // 要缓存的javaBean
RxCacheProvider.put(CACHE_KEY, model);

// 同步读取缓存
Model result = RxCacheProvider.get(CACHE_KEY, Model.class);
```

### 3.异步写入和读取缓存
```
// 异步写入缓存
Model model = new Model(110, "holen"); 
RxCacheProvider.save(CACHE_KEY, model)
        .subscribeOn(Schedulers.io())
        .subscribe(new DefaultCacheObserver<Boolean>());

// 异步读取缓存
RxCacheProvider.load(CACHE_KEY, Model.class)
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Model>() {
            @Override
            public void accept(Model result) throws Exception {
                Log.d(TAG, "accept: " + result);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e(TAG, "throwable: ", throwable);
            }
        });
```

### 4.设置保存时间

```
RxCacheProvider.put(CACHE_WITH_TIME_KEY, model, 10 * 1000); // 缓存10s
// or
RxCacheProvider.save(CACHE_WITH_TIME_KEY, model, 10 * 1000).subscribe(new DefaultCacheObserver<>());
```

### 5.使用CacheType类包装缓存类型
```
List<Model> modelList = new ArrayList<>();
for (int i = 0; i < 3; i++) {
    modelList.add(new Model(i, "holen" + i));
}
List<Model> result = RxCacheProvider.get(CACHE_WITH_TIME_KEY, new CacheType<List<Model>>(){});
```
### 6.同步或异步清楚缓存
```
// 同步清楚缓存
RxCacheProvider.removeCache(CACHE_KEY);
// 异步清楚缓存
RxCacheProvider.rxRemove(CACHE_KEY)
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
```

### 7.判断是否包含缓存
```
boolean contain = RxCacheProvider.containsKey(CACHE_KEY);
```
### 8.clear缓存
```
RxCacheProvider.clearCache();
// or
RxCacheProvider.rxClear()
        .subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
```

### 9.获取单独的RxCache

RxCacheProvider中操作缓存的方法在RxCache中都有，因为RxCacheProvider内部就是通过RxCache来操作缓存的。
```
RxCache rxCache = RxCacheProvider.getRxCacheBuilder()
        .diskConverter(new SerializableDiskConverter())
        .build();
User user = new User(222, "Sherlock");
rxCache.save("user-cache", user);
```

## 网络缓存

### 1.六种网络缓存策略

*   DEFAULT：不使用缓存，该模式下,cacheKey,cacheTime 等参数均无效
*   FIRSTREMOTE：先请求网络，请求网络失败后再加载缓存
*   FIRSTCACHE：先加载缓存，缓存没有再去请求网络
*   ONLYREMOTE：仅加载网络，但数据依然会被缓存
*   ONLYCACHE：只读取缓存
*   CACHEANDREMOTE:先使用缓存，不管是否存在，仍然请求网络，CallBack会回调两次.
*   CACHEANDREMOTEDISTINCT:先使用缓存，不管是否存在，仍然请求网络，CallBack回调不一定是两次，如果发现请求的网络数据和缓存数据是相同的，就不会再返回网络的回调，即只回调一次。若不相同仍然会回调两次。（目的是为了防止数据没有发生变化，也需要回调两次导致界面无用的重复刷新）

*注：无论对于哪种缓存模式，都可以指定一个cacheKey，建议针对不同需要缓存的页面设置不同的cacheKey，如果相同，会导致数据覆盖。*

### 2.使用方法
```
Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
GithubService  githubService = retrofit.create(GithubService.class);
// 1.将写好的retrofit的网络请求接口的Observable对象直接传入RxCacheProvider的api方法
// 2.配置缓存key、缓存模式、缓存时间等参数
// 3.调用buildCache，传入需要缓存的类型，即可获得一个具备缓存功能的Observable对象
RxCacheProvider.api(githubService.getUser("HolenZhou"))
        .cacheKey(NetCacheActivity.class.getSimpleName())
        .cacheMode(mRxCacheMode)
        .cacheTime(5 * 60 * 1000) // 5min
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
```
### 3.设置回调中包含结果是否来自缓存的信息
```
RxCacheProvider.api(githubService.getUser("HolenZhou"))
        .cacheKey(this.getClass().getSimpleName())
        .cacheMode(mRxCacheMode)
        .buildCache(new CacheCallback<User>(){})
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
```

### 4.分页缓存
```
RxCacheProvider.api(githubService.getUser("HolenZhou"))
		.dynamicKey(new DynamicKey(NetCacheActivity.class.getSimpleName(), PAGE_NUM))
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
```

###  5.分账户缓存
```
RxCacheProvider.api(githubService.getUser("HolenZhou"))
		.dynamicKey(new DynamicKey(NetCacheActivity.class.getSimpleName(), account))
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
```

