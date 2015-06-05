package snails.common.exception;


/**
 * 业务异常抛出
 */
public class ActionMessageException extends RuntimeException {

    public ActionMessageException(String message) {
        super(message);
    }

    public ActionMessageException(String format, Object... args) {
        super(String.format(format, args));
    }
}
