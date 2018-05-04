/**
 * 
 */
package org.example.assessment.common;

/**
 * Created by resulav on 02.05.2018.
 */
public enum BookField {
	
	BOOK_ID("book-id"), 
	NAME("name"), 
	AUTHOR("author"), 
	ISBN("isbn"), 
	INTRODUCTION("introduction"), 
	PARAGRAPHS("paragraphs");
	
	private final String fieldName;

	BookField(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

}
