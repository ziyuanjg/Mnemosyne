package common.httpClient;

import java.util.Map;
import okhttp3.Callback;
import okhttp3.Headers;

/**
 * Created by Mr.Luo on 2018/5/8
 */
public interface HTTPClient {

    String send(String url, Headers headers, Object body, RequestTypeEnum requestTypeEnum);

    void sendWithCallBack(String url, Headers headers, Object body, RequestTypeEnum requestTypeEnum,
            Callback callback);
}
