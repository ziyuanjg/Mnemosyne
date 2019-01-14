package com.mnemosyne.common.httpClient;

import okhttp3.Callback;
import okhttp3.Headers;

/**
 * Created by Mr.Luo on 2018/5/8
 */
public abstract class BaseClient implements HTTPClient {

    @Override
    public String send(String url, Headers headers, Object body, RequestTypeEnum requestTypeEnum) {

        url = buildUrl(url);

        String result = null;
        switch (requestTypeEnum) {
            case GET:
                result = get(url, headers);
                break;
            case POST:
                result = post(url, headers, body);
                break;
        }
        return result;
    }

    @Override
    public void sendWithCallBack(String url, Headers headers, Object body, RequestTypeEnum requestTypeEnum,
            Callback callback) {

        url = buildUrl(url);
        switch (requestTypeEnum) {
            case GET:
                getWithCallBack(url, headers, callback);
                break;
            case POST:
                postWithCallBack(url, headers, body, callback);
                break;
        }

    }

    abstract String get(String url, Headers headers);

    abstract String post(String url, Headers headers, Object body);

    abstract void getWithCallBack(String url, Headers headers, Callback callback);

    abstract void postWithCallBack(String url, Headers headers, Object body, Callback callback);

    private String buildUrl(String url) {

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "http://" + url;
        } else {
            return url;
        }
    }
}
