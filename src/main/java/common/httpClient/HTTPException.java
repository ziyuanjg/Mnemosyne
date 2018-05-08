package common.httpClient;

import common.BaseException;
import common.EnumInterface;

/**
 * Created by 希罗 on 2018/5/8
 */
public class HTTPException extends BaseException{

    public HTTPException(EnumInterface exception) {
        super(exception);
    }

    public HTTPException(EnumInterface exception, Throwable cause) {
        super(exception, cause);
    }
}
