package snails.common.jdbc.exception;


/**
 * 运行期异常
 */
public class JDBCException extends RuntimeException {
    public JDBCException(String message) {
        super(message);
    }

    public JDBCException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
