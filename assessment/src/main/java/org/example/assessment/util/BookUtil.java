package org.example.assessment.util;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.example.assessment.common.BookField;
import org.example.assessment.model.Book;

import com.google.common.collect.Lists;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookUtil {
	private BookUtil() {
		// for sonarqube
	}

	/**
	 * Converts nodeIterator to Book list.
	 *
	 * @param nodeIterator nodes in iterator
	 * @return a {@link Book} instance
	 * @throws RepositoryException as generic exception
	 */
	public static List<Book> toBookList(NodeIterator nodeIterator) throws RepositoryException {
		List<Book> books = Lists.newArrayList();
		while (nodeIterator.hasNext()) {
			books.add(BookUtil.toBook(nodeIterator.nextNode()));
		}
		return books;
	}

	/**
	 * Converts node to Book.
	 *
	 * @param node book node
	 * @return a {@link Book} instance
	 * @throws RepositoryException as generic exception
	 */
	public static Book toBook(Node node) throws RepositoryException {
		Book book = new Book();
		book.setBookId(node.getName());
		book.setName(node.getProperty(BookField.NAME.getFieldName()).getString());
		book.setAuthor(node.getProperty(BookField.AUTHOR.getFieldName()).getString());
		book.setIsbn(node.getProperty(BookField.ISBN.getFieldName()).getString());
		book.setIntroduction(node.getProperty(BookField.INTRODUCTION.getFieldName()).getString());
		book.setParagraphs(node.getProperty(BookField.PARAGRAPHS.getFieldName()).getString());

		return book;
	}

	/**
	 * Converts Book to node.
	 * 
	 * @param book book instance
	 * @param bookNode book node
	 * @throws RepositoryException as generic exception
	 */
	public static void toNode(Book book, Node bookNode) throws RepositoryException {
		bookNode.setProperty(BookField.NAME.getFieldName(), book.getName());
		bookNode.setProperty(BookField.AUTHOR.getFieldName(), book.getAuthor());
		bookNode.setProperty(BookField.ISBN.getFieldName(), book.getIsbn());
		bookNode.setProperty(BookField.INTRODUCTION.getFieldName(), book.getIntroduction());
		bookNode.setProperty(BookField.PARAGRAPHS.getFieldName(), book.getParagraphs());
	}

}
