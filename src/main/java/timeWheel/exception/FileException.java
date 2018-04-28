package timeWheel.exception;

import common.BaseException;
import common.ExceptionInterface;

/**
 * 文件操作异常
 * Created by 希罗 on 2018/4/28
 */
public class FileException extends BaseException{

    public FileException(ExceptionInterface exception) {
        super(exception);
    }

    public FileException(ExceptionInterface exception, Throwable cause) {
        super(exception, cause);
    }
}
