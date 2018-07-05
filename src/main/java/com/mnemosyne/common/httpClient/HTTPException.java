package com.mnemosyne.common.httpClient;

import com.mnemosyne.common.BaseException;
import com.mnemosyne.common.EnumInterface;

/**
 * Created by Mr.Luo on 2018/5/8
 */
public class HTTPException extends BaseException {

    public HTTPException(EnumInterface exception) {
        super(exception);
    }

    public HTTPException(EnumInterface exception, Throwable cause) {
        super(exception, cause);
    }
}
