package common;

/**
 * Created by 希罗 on 2018/4/28
 */
public class BaseException extends RuntimeException{

    EnumInterface exceptionInterface;


    public BaseException(EnumInterface exception) {
        super(exception.getCode()+":"+exception.getMessage());
    }
    public BaseException(EnumInterface exception, Throwable cause) {
        super(exception.getCode()+":"+exception.getMessage(), cause);
    }
}
