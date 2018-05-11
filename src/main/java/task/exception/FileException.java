package task.exception;

import common.BaseException;
import common.EnumInterface;

/**
 * 文件操作异常
 * Created by Mr.Luo on 2018/4/28
 */
public class FileException extends BaseException{

    public FileException(EnumInterface exception) {
        super(exception);
    }

    public FileException(EnumInterface exception, Throwable cause) {
        super(exception, cause);
    }
}
