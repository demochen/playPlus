package snails.common.base.models;

public class BasicResult<T> {

	private T model;

	private boolean success;

	private String message;

	private Exception exception;

	/**
	 * @return the model
	 */
	public T getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(T model) {
		this.model = model;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the exception
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

}
