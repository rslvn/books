package org.example.assessment.resource;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.example.assessment.common.Constants;
import org.example.assessment.common.ResultCode;
import org.example.assessment.exception.BookException;
import org.example.assessment.model.Book;
import org.example.assessment.model.BookResponse;
import org.example.assessment.service.BookService;
import org.example.assessment.store.BookObservator;
import org.example.assessment.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookResources {

	public static final String SERVICE_PATH = "/books";
	public static final String METHOD_GET_ADD_BOOK = "/";
	public static final String METHOD_SEARCH_BOOK = "/search";
	public static final String METHOD_DELETE_BOOK = "/delete";
	public static final String METHOD_UPDATE_BOOK = "/update";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private BookService bookService;

	public BookResources(Session session) {
		bookService = new BookService(session);
		BookObservator.newBuilder().withSession(session).build();
	}

	/**
	 * Adds books to repository
	 *
	 * @param books
	 *            as list of {@link Book}
	 * @return true/false
	 */
	@Path(METHOD_GET_ADD_BOOK)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public BookResponse addBooks(List<Book> books) {

		BookResponse.Builder response = BookResponse.newBuilder();

		try {
			log.debug("addBooks service is called.");
			// constraint check
			Preconditions.checkNotEmpty(books, "books can not be empty");
			books.forEach(b -> {
				validateBook(b);
				validateBookByISBN(b.getIsbn());
				// generate and set a unique bookId
				b.setBookId(UUID.randomUUID().toString());
			});

			log.debug("Size of books to add: {}: ", books.size());
			// call service
			bookService.addBooks(books);

		} catch (BookException e) {
			response.withResultCode(e.getResultCode()).withMessage(e.getReason());
			log.error("", e);
		} catch (Exception e) {
			BookException be = BookException.newInstance(ResultCode.FAILED, Constants.ERROR_INTERNAL, e);
			response.withResultCode(be.getResultCode()).withMessage(Constants.ERROR_INTERNAL);
			log.error("Error while addBooks", be);

		} finally {
			log.debug("addBooks executed");
		}

		return response.build();
	}

	private void validateBookByISBN(String isbn) {
		try {
			Preconditions.checkEmpty(bookService.queryBooksByISBN(isbn), ResultCode.ALREADY_EXIST,
					String.format("A book already exist by ISBN: %s", isbn));
		} catch (RepositoryException e) {
			throw BookException.newInstance(ResultCode.FAILED, Constants.ERROR_INTERNAL, e);
		}
	}

	/**
	 * lists books in repository
	 *
	 * @return list of {@link Book}
	 */
	@Path(METHOD_GET_ADD_BOOK)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<Book> getBooks() {
		try {
			log.debug("getBooks service is called");
			return bookService.getBooks();
		} catch (BookException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("Error while getBooks",
					BookException.newInstance(ResultCode.FAILED, Constants.ERROR_INTERNAL, e));
		} finally {
			log.debug("getBooks executed");
		}

		return Lists.newArrayList();
	}

	/**
	 * Searches books in repository containing text
	 *
	 * @param query
	 *            as search text
	 * @return list of {@link Book}
	 */
	@Path(METHOD_SEARCH_BOOK + "/{query}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<Book> searchBooks(@PathParam("query") String query) {
		try {
			log.debug("searchBooks service is called");
			// constraint check
			Preconditions.checkNotEmpty(query, "query text not be empty");

			// call service
			return bookService.searchBooksByLike(query);

		} catch (BookException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("Error while searchBooks",
					BookException.newInstance(ResultCode.FAILED, Constants.ERROR_INTERNAL, e));
		} finally {
			log.debug("searchBooks executed");
		}

		return Lists.newArrayList();
	}

	/**
	 * Searches books in repository containing text
	 *
	 * @param bookId
	 *            as bookId
	 * @return list of {@link Book}
	 */
	@Path(METHOD_DELETE_BOOK + "/{bookId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@DELETE
	public BookResponse deleteBook(@PathParam("bookId") String bookId) {

		BookResponse.Builder response = BookResponse.newBuilder();

		try {
			log.debug("deleteBook service is called");
			// constraint check
			Preconditions.checkNotEmpty(bookId, "bookId not be empty");
			// call service
			bookService.deleteBook(bookId);

		} catch (BookException e) {
			response.withResultCode(e.getResultCode()).withMessage(e.getReason());
			log.error("", e);
		} catch (Exception e) {
			BookException be = BookException.newInstance(ResultCode.FAILED, Constants.ERROR_INTERNAL, e);
			response.withResultCode(be.getResultCode()).withMessage(Constants.ERROR_INTERNAL);
			log.error("Error while deleteBook", be);

		} finally {
			log.debug("deleteBook executed");
		}

		return response.build();
	}

	/**
	 * Adds books to repository
	 *
	 * @param book
	 *            as list of {@link Book}
	 * @return true/false
	 */
	@Path(METHOD_UPDATE_BOOK)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public BookResponse updateBook(Book book) {
		BookResponse.Builder response = BookResponse.newBuilder();
		try {
			log.debug("updateBook service is called.");
			// constraint check
			Preconditions.checkNotNull(book, "books can not be null");
			Preconditions.checkNotEmpty(book.getBookId(), "book ID name can not be empty");
			validateBook(book);

			// call service
			bookService.updateBook(book);

		} catch (BookException e) {
			response.withResultCode(e.getResultCode()).withMessage(e.getReason());
			log.error("", e);
		} catch (Exception e) {
			BookException be = BookException.newInstance(ResultCode.FAILED, Constants.ERROR_INTERNAL, e);
			response.withResultCode(be.getResultCode()).withMessage(Constants.ERROR_INTERNAL);
			log.error("Error while updateBook", be);

		} finally {
			log.debug("updateBook executed");
		}

		return response.build();
	}

	/**
	 * validate a book parameters
	 *
	 * @param book
	 */
	private void validateBook(Book book) {
		Preconditions.checkNotEmpty(book.getName(), "book name can not be empty");
		Preconditions.checkNotEmpty(book.getAuthor(), "book author can not be empty");
		Preconditions.checkNotEmpty(book.getIsbn(), "book ISBN can not be empty");
		Preconditions.checkArgument(book.getIsbn().length() == Constants.ISBN_SIZE,
				String.format("ISBN size should be %d", Constants.ISBN_SIZE));

		Preconditions.checkNotEmpty(book.getIntroduction(), "book introduction can not be empty");
		Arrays.stream(book.getIntroduction())
				.forEach(p -> Preconditions.checkNotEmpty(p, "introduction paragraph can not be empty"));

		Preconditions.checkNotEmpty(book.getParagraphs(), "book parapraphs can not be empty");
		Arrays.stream(book.getParagraphs()).forEach(p -> Preconditions.checkNotEmpty(p, "paragraph can not be empty"));
	}
}
