package common;

/**
 * Created by 希罗 on 2018/4/28
 */
public class BaseException extends RuntimeException{

    ExceptionInterface exceptionInterface;


    public BaseException(ExceptionInterface exception) {
        super(exception.getCode()+":"+exception.getMessage());
    }
    public BaseException(ExceptionInterface exception, Throwable cause) {
        super(exception.getCode()+":"+exception.getMessage(), cause);
    }
}
