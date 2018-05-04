package org.example.assessment.model;

import org.junit.Test;

import pl.pojo.tester.api.assertion.Assertions;
import pl.pojo.tester.api.assertion.Method;

/**
 * Created by resulav on 05.05.2018.
 */
public class ModelTest {

	@Test
	public void testBook() {
		Assertions.assertPojoMethodsFor(Book.class).testing(Method.CONSTRUCTOR, Method.GETTER,Method.SETTER,
				Method.TO_STRING).areWellImplemented();
	}

}
