package org.example.assessment.service;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang3.StringUtils;
import org.example.assessment.common.Constants;
import org.example.assessment.common.ResultCode;
import org.example.assessment.model.Book;
import org.example.assessment.util.BookUtil;
import org.example.assessment.util.Preconditions;
import org.example.assessment.util.RepositoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookService {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final String QUERY_PREFIX = StringUtils.join("/jcr:root/", Constants.REPOSITORY, "//(*,nt:unstructured)");
	private final Session session;

	public BookService(Session session) {
		this.session = session;
	}

	/**
	 * Inserts books to the repository
	 * 
	 * @param books
	 * @return
	 * @throws RepositoryException
	 * 
	 */
	public void addBooks(List<Book> books) throws RepositoryException {
		Node booksNode = RepositoryUtil.getBooksNode(session);

		// add book node and set properties
		for (Book book : books) {
			Node bookNode = booksNode.addNode(book.getBookId());
			BookUtil.toNode(book, bookNode);
		}

		session.save();
	}

	/**
	 * @param book
	 * @throws RepositoryException
	 */
	public void updateBook(Book book) throws RepositoryException {
		Node booksNode = RepositoryUtil.getBooksNode(session);
		// validate the book exists or not
		Preconditions.checkArgument(booksNode.hasNode(book.getBookId()), ResultCode.NOT_FOUND,
				String.format("No book found by bookId: %s", book.getBookId()));

		// get book node and set properties
		Node bookNode = booksNode.getNode(book.getBookId());
		BookUtil.toNodeForUpdate(book, bookNode);

		session.save();
	}

	/**
	 * @param bookId
	 * @throws RepositoryException
	 */
	public void deleteBook(String bookId) throws RepositoryException {
		Node booksNode = RepositoryUtil.getBooksNode(session);

		NodeIterator nodeIterator = booksNode.getNodes(bookId);
		Preconditions.checkArgument(nodeIterator.getSize() > 0, ResultCode.NOT_FOUND,
				String.format("No book found by bookId: %s", bookId));

		while (nodeIterator.hasNext()) {
			((Node) nodeIterator.next()).remove();
		}

		session.save();
	}

	/**
	 * retrieves books from repository
	 *
	 * @return list of books
	 * @throws RepositoryException
	 */
	public List<Book> getBooks() throws RepositoryException {
		List<Book> bookList = RepositoryUtil.getBooks(session);
		log.debug("{} book(s) found", bookList.size());

		return bookList;
	}

	/**
	 * Searches books containing text as case in-sensitive
	 *
	 * @param text
	 * @return list of books
	 * @throws RepositoryException
	 */
	public List<Book> searchBooksByContains(String text) throws RepositoryException {
		log.info("Searching books contains text: {}", text);

		String queryText = StringUtils.join(QUERY_PREFIX, "[jcr:contains(.,'", text, "')]");

		return queryBooks(queryText);
	}

	/**
	 * Searches books containing text as case in-sensitive
	 *
	 * @param text
	 * @return list of books
	 * @throws RepositoryException
	 */
	public List<Book> searchBooksByLike(String text) throws RepositoryException {
		log.info("Searching books contains text: {}", text);

		String queryText = StringUtils.join(QUERY_PREFIX, "[jcr:like(@name,'%", text, "%') or jcr:like(@isbn,'%", text,
				"%')or jcr:like(@author,'%", text, "%') or jcr:like(@paragraphs,'%", text,
				"%') or jcr:like(@introduction,'%", text, "%')]");

		return queryBooks(queryText);
	}

	/**
	 * Searches books containing text as case in-sensitive
	 *
	 * @param isbn
	 * @return list of books
	 * @throws RepositoryException
	 */
	public List<Book> queryBooksByISBN(String isbn) throws RepositoryException {
		log.info("Searching books contains ISBN: {}", isbn);

		String queryText = StringUtils.join(QUERY_PREFIX, "[@isbn='", isbn, "']");

		return queryBooks(queryText);
	}

	/**
	 * Searches books containing text as case in-sensitive
	 *
	 * @param queryText
	 * @return list of books
	 * @throws RepositoryException
	 */
	@SuppressWarnings("deprecation")
	private List<Book> queryBooks(String queryText) throws RepositoryException {
		log.debug("QUERY: {}", queryText);
		// Query repository for the books containing text
		Query query = session.getWorkspace().getQueryManager().createQuery(queryText, Query.XPATH);
		QueryResult queryResult = query.execute();

		return BookUtil.toBookList(queryResult.getNodes());
	}

}
