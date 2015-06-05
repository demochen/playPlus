package snails.common.exception;




/**
 * 系统业务异常,可以返回具体业务原因等信息
 */
public class YBBizException extends Exception {
    /**
     * 短信不足
     */
    public static final int NOT_ENOUGH_SMS = 1;
    private Long userId;
    private Integer errorCode;
    public YBBizException(Long userId, int errorCode,String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
