package timeWheel.exception;

import common.BaseException;
import common.ExceptionInterface;

/**
 * Created by 希罗 on 2018/4/28
 */
public class FileLockException extends BaseException{

    public FileLockException(ExceptionInterface exception) {
        super(exception);
    }

    public FileLockException(ExceptionInterface exception, Throwable cause) {
        super(exception, cause);
    }
}
