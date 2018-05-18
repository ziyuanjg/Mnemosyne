package common.httpClient;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

/**
 * Created by Mr.Luo on 2018/5/4
 */
public class OkHTTPClient extends BaseClient {

    @Override
    String get(String url, Headers headers) {

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = buildRequest(url, headers, null);

        return excute(okHttpClient, request);
    }

    @Override
    String post(String url, Headers headers, Map<String, String> body) {

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = buildRequest(url, headers, body);

        return excute(okHttpClient, request);
    }

    @Override
    void getWithCallBack(String url, Headers headers, Callback callback) {

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = buildRequest(url, headers, null);

        okHttpClient.newCall(request).enqueue(callback);
    }

    @Override
    void postWithCallBack(String url, Headers headers, Map<String, String> body, Callback callback) {

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = buildRequest(url, headers, body);

        okHttpClient.newCall(request).enqueue(callback);
    }

    private String excute(OkHttpClient okHttpClient, Request request) {
        try {
            Response response = okHttpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Request buildRequest(String url, Headers headers, Map<String, String> body) {

        Request.Builder builder = new Builder();

        if (url == null) {
            throw new HTTPException(HTTPExceptionEnum.PARAM_ERROR_URL);
        }

        builder.url(url);

        if (headers != null) {
            builder.headers(headers);
        }

        if (body != null && !body.isEmpty()) {
            FormBody.Builder fromBody = new FormBody.Builder();
            for (Entry<String, String> entry : body.entrySet()) {
                fromBody.add(entry.getKey(), entry.getValue());
            }
            builder.post(fromBody.build());
        }

        return builder.build();
    }


}
