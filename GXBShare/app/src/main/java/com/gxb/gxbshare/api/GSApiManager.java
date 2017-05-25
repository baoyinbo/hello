package com.gxb.gxbshare.api;

import com.gxb.gxbshare.GSApplication;
import com.gxb.gxbshare.R;
import com.gxb.gxbshare.net.WJRequestParam;
import com.gxb.gxbshare.net.WJResponseListener;
import com.gxb.gxbshare.net.WrapResponseListener;
import com.gxb.gxbshare.net.http.cache.WJCacheResponseListener;
import com.gxb.gxbshare.net.http.cache.WrapCacheResponseListener;
import com.gxb.gxbshare.ui.common.DefaultLoadingView;
import com.gxb.gxbshare.ui.common.GSDefaultLoadingView;
import com.gxb.gxbshare.ui.common.GSLoadingView;
import com.robin.lazy.net.http.HttpRequestManager;
import com.robin.lazy.net.http.LoadingViewInterface;
import com.robin.lazy.net.http.RequestLifecycleContext;
import com.robin.lazy.net.http.cache.HttpCacheLoadType;

import java.util.HashMap;
import java.util.Map;

import model.GSTestModel;

/**
 * api请求管理
 * Created by baoyb on 2017/5/25.
 */

public class GSApiManager {
    //缓存大小
    private static final long maxCacheAge = 60 * 24 * 7L;
    private static GSApiManager apiManager = new GSApiManager();
    /***
     * 请求管理者
     */
    private HttpRequestManager requestManager;
    /***
     * 请求头,公共部分,所有接口都需要传的字段(其实也是放在请求体中)
     */
    private Map<String, String> headers;

    public static GSApiManager instance() {
        return apiManager;
    }

    private GSApiManager() {
        requestManager = GSApplication.getApplication().getHttpDataManager();
        headers = new HashMap<String, String>();
        headers.putAll(initHeader());
    }

    /***
     * 初始化请求头
     *
     * @return
     */
    private Map initHeader() {
        Map<String, String> map = new HashMap<>();
//        map.put("appVersion", WJDeviceHelper.getAppVersion());//应用程序版本号
//        map.put("osVersion", WJDeviceHelper.getOSVersion());//系统版本号
//        map.put("termTyp", "ANDROID");//客户端类型(IOS，ANDROID)
//        map.put("channelId", WJDeviceHelper.getAppChannelName());//客户端渠道(不同的渠道传不同的ID)
//        map.put("termId", WJDeviceHelper.getMobileUUID());//客户终端号
//        map.put("deviceId", WJDeviceHelper.getDeviceId());//推送终端号(登录后必传)
        return map;
    }

    /**
     * 得到公共参数
     *
     * @return
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 无缓存例子
     * @param lifecycleContext
     * @param mblNo            手机号
     * @param clientId         客户端授权编号
     * @param listener
     */
    public void test(RequestLifecycleContext lifecycleContext, String mblNo,
                            String clientId, WJResponseListener listener) {
        WJRequestParam param = new WJRequestParam(R.string.url_lizi, headers);
        if (requestManager.isExistTask(param.getMessageId())) return;
        param.addSendData("mblNo", mblNo);
        param.addSendData("clientId", clientId);
        requestManager.sendHttpPostRequest(lifecycleContext,
                param,
                new DefaultLoadingView(lifecycleContext.getCurrContext()),
                new WrapResponseListener<GSTestModel>(listener) {
                });
    }

    /**
     * 有缓存例子
     *
     * @param lifecycleContext
     * @param loadingView
     * @param listener
     */
    public void getIndexMsg(RequestLifecycleContext lifecycleContext, GSDefaultLoadingView loadingView,
                            WJCacheResponseListener listener) {
        WJRequestParam param = new WJRequestParam(R.string.url_lizi, headers);
        if (requestManager.isExistTask(param.getMessageId())) return;
        HttpCacheLoadType loadType = HttpCacheLoadType.NOT_USE_CACHE_UPDATE_CACHE;
        LoadingViewInterface loadingViewInterface = null;
        if (loadingView != null) {
            loadType = HttpCacheLoadType.USE_CACHE_AND_NET_UPDATE_CHCHE;
            loadingViewInterface = new GSLoadingView(loadingView);
        }
        requestManager.sendCacheHttpPostRequest(lifecycleContext,
                param,
                loadingViewInterface,
                new WrapCacheResponseListener<GSTestModel>(listener) {
                }, loadType, maxCacheAge);
    }
}
