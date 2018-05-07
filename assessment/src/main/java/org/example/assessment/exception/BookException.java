package org.example.assessment.exception;

import org.example.assessment.common.ResultCode;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ResultCode resultCode;
	private final String reason;

	public BookException(ResultCode resultCode, String reason) {
		super(getExceptionMessage(resultCode, reason));
		this.resultCode=resultCode;
		this.reason=reason;
	}

	public BookException(ResultCode resultCode, String reason, Exception e) {
		super(getExceptionMessage(resultCode, reason), e);
		this.resultCode=resultCode;
		this.reason=reason;
	}

	public static BookException newInstance(ResultCode resultCode, String reason) {
		return new BookException(resultCode, reason);
	}

	public static BookException newInstance(ResultCode resultCode, String reason, Exception e) {
		return new BookException(resultCode, reason, e);
	}

	private static String getExceptionMessage(ResultCode resultCode, String reason) {
		return new StringBuilder().append(resultCode.getCode()).append("-").append(resultCode.name()).append(": ")
				.append(reason).toString();
	}

	/**
	 * @return the resultCode
	 */
	public ResultCode getResultCode() {
		return resultCode;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

}
