package com.mnemosyne.slave.exception;

import com.mnemosyne.common.BaseException;
import com.mnemosyne.common.EnumInterface;

/**
 * Created by Mr.Luo on 2018/5/14
 */
public class SlaveException extends BaseException {

    public SlaveException(EnumInterface exception) {
        super(exception);
    }

    public SlaveException(EnumInterface exception, Throwable cause) {
        super(exception, cause);
    }
}
