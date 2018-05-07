/**
 * 
 */
package org.example.assessment.common;

/**
 * @author resulav
 *
 */
public enum ResultCode {
	
	SUCCESS(0),
	FAILED(1),
	NOT_FOUND(2),
	ALREADY_EXIST(3),
	VALIDATION_FAILED(4),
	;
	
	private final int code;

	/**
	 * @param code
	 */
	private ResultCode(int code) {
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

}
