package org.example.assessment.util;

import javax.jcr.Node;

import org.example.assessment.common.BookField;
import org.example.assessment.exception.BookException;
import org.example.assessment.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookUtil {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private BookUtil() {
		// for sonarqube
	}

	/**
	 * Converts node to Book. It throws a BookException If something is wrong
	 *
	 * @param node
	 * @return a {@link Book} instance
	 */
	public static Book toBook(Node node) {
		try {
			Book book = new Book();
			book.setBookId(node.getName());
			book.setName(node.getProperty(BookField.NAME.getFieldName()).getString());
			book.setAuthor(node.getProperty(BookField.AUTHOR.getFieldName()).getString());
			book.setIsbn(node.getProperty(BookField.ISBN.getFieldName()).getString());
			book.setIntroduction(node.getProperty(BookField.INTRODUCTION.getFieldName()).getString());
			book.setParagraphs(node.getProperty(BookField.PARAGRAPHS.getFieldName()).getString());

			return book;

		} catch (Exception e) {
			throw BookException.newInstance("Error while toBook process", e);
		}
	}

	/**
	 * Converts Book to node. It throws a BookException If something is wrong
	 * 
	 * @param book
	 * @param bookNode
	 */
	public static void toNode(Book book, Node bookNode) {
		try {
			bookNode.setProperty(BookField.NAME.getFieldName(), book.getName());
			bookNode.setProperty(BookField.AUTHOR.getFieldName(), book.getAuthor());
			bookNode.setProperty(BookField.ISBN.getFieldName(), book.getIsbn());
			bookNode.setProperty(BookField.INTRODUCTION.getFieldName(), book.getIntroduction());
			bookNode.setProperty(BookField.PARAGRAPHS.getFieldName(), book.getParagraphs());

		} catch (Exception e) {
			throw BookException.newInstance("Error while toNode process", e);
		}
	}

}
