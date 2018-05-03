/**
 * 
 */
package org.example.assessment.util;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.example.assessment.common.Constants;
import org.example.assessment.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author resulav
 *
 */
public class RepositoryUtil {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private RepositoryUtil() {
		// for sonarqube
	}

	/**
	 * gets root node from repository, checks if books content exist, if not
	 * adds books node to the root
	 * 
	 * @return
	 */
	public static Node getBooksNode(Session session) throws RepositoryException {
		Node booksNode;
		if (session.getRootNode().hasNode(Constants.REPOSITORY)) {
			booksNode = session.getRootNode().getNode(Constants.REPOSITORY);
		} else {
			booksNode = session.getRootNode().addNode(Constants.REPOSITORY);
			booksNode.setPrimaryType(NodeType.NT_UNSTRUCTURED);
		}

		return booksNode;
	}

	public static Book getBook(Session session, String bookName) throws RepositoryException {

		Node booksNode = getBooksNode(session);

		Node bookNode = booksNode.getNode(bookName);
		if (bookNode == null) {
			return null;
		}
		
		return BookUtil.toBook(bookNode);
	}

	/**
	 * Update books in book store object
	 * 
	 * @throws RepositoryException
	 */
	public static List<Book> getBooks(Session session) throws RepositoryException {

		List<Book> books = Lists.newArrayList();

		Node booksNode = getBooksNode(session);
		NodeIterator nodeIterator = booksNode.getNodes();

		while (nodeIterator.hasNext()) {
			Node node = nodeIterator.nextNode();
			books.add(BookUtil.toBook(node));
		}

		return books;
	}
}
