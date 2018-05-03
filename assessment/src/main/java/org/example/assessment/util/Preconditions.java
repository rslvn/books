package org.example.assessment.util;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.util.CollectionUtils;
import org.example.assessment.exception.BookException;

/**
 * Created by resulav on 02.05.2018.
 */
public class Preconditions {

	private Preconditions() {
		// for sonarqube
	}

	/**
	 * Ensures that an object reference passed as a parameter to the calling
	 * method is not null.
	 *
	 * @param reference
	 * @param errorMessage
	 * @return
	 */
	public static <T> T checkNotNull(T reference, String errorMessage) {
		if (reference == null) {
			throw BookException.newInstance(errorMessage);
		}
		return reference;
	}

	/**
	 * Ensures that an object reference passed as a parameter to the calling
	 * method is not null or empty.
	 *
	 * @param reference
	 * @param errorMessage
	 */
	public static <T> void checkNotEmpty(Collection<T> reference, String errorMessage) {
		if (CollectionUtils.isEmpty(reference)) {
			throw BookException.newInstance(errorMessage);
		}
	}

	/**
	 * Ensures that an object reference passed as a parameter to the calling
	 * method is not null.
	 * 
	 * @param reference
	 * @param errorMessage
	 * @return
	 */
	public static String checkNotEmpty(String reference, String errorMessage) {
		String value = StringUtils.trimToNull(reference);

		if (value == null) {
			throw BookException.newInstance(errorMessage);
		}

		return value;
	}

}
