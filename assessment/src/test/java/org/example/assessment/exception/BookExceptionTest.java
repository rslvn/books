package org.example.assessment.exception;

import org.example.assessment.common.ResultCode;
import org.junit.Test;

/**
 * Created by resulav on 07.05.2018.
 */
public class BookExceptionTest {

	@Test(expected = BookException.class)
	public void testBookException() {
		throw BookException.newInstance(ResultCode.FAILED, "test BookException");
	}

	@Test(expected = BookException.class)
	public void testBookExceptionByCause() {
		throw BookException.newInstance(ResultCode.FAILED, "test BookException",
				new Exception("dummy exception message"));
	}
}
