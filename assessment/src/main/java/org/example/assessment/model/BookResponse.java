/**
 * 
 */
package org.example.assessment.model;

import org.example.assessment.common.ResultCode;

/**
 * @author resulav
 *
 */
public class BookResponse {
	private Integer resultCode;
	private String resultText;
	private String message;

	private BookResponse() {
		// For json conversion
	}

	private BookResponse(Builder builder) {
		resultCode = builder.resultCode.getCode();
		resultText = builder.resultCode.name();
		message = builder.message;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * @return the resultCode
	 */
	public int getResultCode() {
		return resultCode;
	}

	/**
	 * @return the resultText
	 */
	public String getResultText() {
		return resultText;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * {@code BookResponse} builder static inner class.
	 */
	public static final class Builder {
		private ResultCode resultCode;
		private String message;

		private Builder() {
		}

		/**
		 * Sets the {@code resultCode} and returns a reference to this Builder
		 * so that the methods can be chained together.
		 *
		 * @param resultCode
		 *            the {@code resultCode} to set
		 * @return a reference to this Builder
		 */
		public Builder withResultCode(ResultCode resultCode) {
			this.resultCode = resultCode;
			return this;
		}

		/**
		 * Sets the {@code message} and returns a reference to this Builder so
		 * that the methods can be chained together.
		 *
		 * @param message
		 *            the {@code message} to set
		 * @return a reference to this Builder
		 */
		public Builder withMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Returns a {@code BookResponse} built from the parameters previously
		 * set.
		 *
		 * @return a {@code BookResponse} built with parameters of this
		 *         {@code BookResponse.Builder}
		 */
		public BookResponse build() {
			if (resultCode == null) {
				resultCode = ResultCode.SUCCESS;
			}

			return new BookResponse(this);
		}
	}
}
