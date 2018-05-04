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
import org.example.assessment.exception.BookException;
import org.example.assessment.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author resulav
 *
 */
public class RepositoryUtil {

	protected static final Logger log = LoggerFactory.getLogger(RepositoryUtil.class);

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
			log.info("new repository adding");
			// Test case path not found exception fix
			addContentRepos(session);
			booksNode = session.getRootNode().addNode(Constants.REPOSITORY);
			booksNode.setPrimaryType(NodeType.NT_UNSTRUCTURED);
		}

		return booksNode;
	}

	/**
	 * ATTENTION: This method for test cases
	 * 
	 * @param session
	 * @throws RepositoryException
	 */
	public static void addContentRepos(Session session) throws RepositoryException {
		addRepository(session, Constants.REPOSITORY_CONTENT);
		addRepository(session, Constants.REPOSITORY_DOCUMENTS);
		addRepository(session, Constants.REPOSITORY_DOCUMENTS_PROJECT);
	}

	public static Node addRepository(Session session, String realPath) throws RepositoryException {
		if (!session.getRootNode().hasNode(realPath)) {
			return session.getRootNode().addNode(realPath);
		} else {
			return session.getRootNode().getNode(realPath);
		}
	}

	public static Book getBookByPath(Session session, String bookRealPath) {
		try {
			// path can not start with "/" under root node. Remove "/"If
			// bookRealPath is starting with "/"
			String bookPath = bookRealPath.startsWith(Constants.PATH_SEPARATOR)
					? bookRealPath.replaceFirst(Constants.PATH_SEPARATOR, "") : bookRealPath;

			// receive book from repository
			Node bookNode = session.getRootNode().getNode(bookPath);
			if (bookNode == null) {
				return null;
			}

			// convert book node to Book
			return BookUtil.toBook(bookNode);

		} catch (Exception e) {
			log.error("", BookException.newInstance("error while getBookByPath", e));
		}

		return null;
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
