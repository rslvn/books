package org.example.assessment.service;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.example.assessment.common.BookField;
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
		Preconditions.checkArgument(booksNode.hasNode(book.getBookId()),
				String.format("No boot to update by bookId: %s", book.getBookId()));

		// get book node and set properties
		Node bookNode = booksNode.getNode(book.getBookId());
		BookUtil.toNode(book, bookNode);

		session.save();
	}

	/**
	 * @param bookId
	 * @throws RepositoryException
	 */
	public void deleteBook(String bookId) throws RepositoryException {
		Node booksNode = RepositoryUtil.getBooksNode(session);

		NodeIterator nodeIterator = booksNode.getNodes(bookId);
		Preconditions.checkArgument(nodeIterator.getSize() > 0, String.format("no book found by bookId: %s", bookId));

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
	@SuppressWarnings("deprecation")
	public List<Book> queryBooks(String text) throws RepositoryException {
		log.info("Searching books contains {}", text);
		// toLowerCase to search case in-sensitive
		final String lowerQueryText = text.toLowerCase();
		// Query repository for the books containing text
		Query query = session.getWorkspace().getQueryManager().createQuery(
				"//*[jcr:like(fn:lower-case(@" + BookField.PARAGRAPHS.getFieldName() + "),'%" + lowerQueryText + "%')]",
				Query.XPATH);
		// execute query
		QueryResult queryResult = query.execute();
		log.info("Search result size: {}", queryResult.getNodes().getSize());

		return BookUtil.toBookList(queryResult.getNodes());
	}

}
