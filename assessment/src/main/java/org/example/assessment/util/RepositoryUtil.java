/**
 * 
 */
package org.example.assessment.util;

import java.util.List;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.example.assessment.common.Constants;
import org.example.assessment.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * @return node of books
	 */
	public static Node getBooksNode(Session session) throws RepositoryException {
		if (session.getRootNode().hasNode(Constants.REPOSITORY)) {
			return session.getRootNode().getNode(Constants.REPOSITORY);
		}

		log.info("new repository adding");
		// Test case path not found exception fix
		addContentRepos(session);
        Node booksNode = session.getRootNode().addNode(Constants.REPOSITORY);
		booksNode.setPrimaryType(NodeType.NT_UNSTRUCTURED);

		return booksNode;
	}

	/**
	 * ATTENTION: This method for test cases
	 * 
	 * @param session
	 *            as system session
	 * @throws RepositoryException
	 *             generic exception
	 */
	private static void addContentRepos(Session session) throws RepositoryException {
		addRepository(session, Constants.REPOSITORY_CONTENT);
		addRepository(session, Constants.REPOSITORY_DOCUMENTS);
		addRepository(session, Constants.REPOSITORY_DOCUMENTS_PROJECT);
	}

	/**
	 * checks repository, adds a new repository if path not exists
	 * 
	 * @param session
	 *            system session
	 * @param realPath
	 *            path of repository
	 * @return node of realPath
	 * @throws RepositoryException
	 *             as generic exception
	 */
	private static Node addRepository(Session session, String realPath) throws RepositoryException {
		if (!session.getRootNode().hasNode(realPath)) {
			return session.getRootNode().addNode(realPath);
		} else {
			return session.getRootNode().getNode(realPath);
		}
	}

	/**
	 * Gets book by real path
	 * 
	 * @param session
	 *            session system session
	 * @param bookRealPath
	 *            path of book
	 * @return an instance of {@link Book} If path exists
	 * @throws RepositoryException
	 *             as generic exception
	 */
	public static Optional<Book> getBookByPath(Session session, String bookRealPath) throws RepositoryException {
		// path can not start with "/" under root node. Remove "/"If
		// bookRealPath is starting with "/"
		String bookPath = bookRealPath.startsWith(Constants.PATH_SEPARATOR)
				? bookRealPath.replaceFirst(Constants.PATH_SEPARATOR, "") : bookRealPath;

		Node rootNode = session.getRootNode();
		if (!rootNode.hasNode(bookPath)) {
			return Optional.empty();
		}

		// receive book from repository
		Node bookNode = session.getRootNode().getNode(bookPath);

		// convert book node to Book
		return Optional.of(BookUtil.toBook(bookNode));

	}

	/**
	 * Update books in book store object
	 * 
	 * @throws RepositoryException
	 *             as generic exception
	 */
	public static List<Book> getBooks(Session session) throws RepositoryException {
		Node booksNode = getBooksNode(session);
		return BookUtil.toBookList(booksNode.getNodes());
	}
}
