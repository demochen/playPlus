package snails.common.exception;



public abstract class ApiBizException extends Exception {
    private Long userId;
    private String message;
    public ApiBizException(Long userId ,String msg) {
        super(msg);
        this.userId = userId;
        this.message = msg;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
