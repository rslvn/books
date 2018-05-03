package org.example.assessment.resource;

import java.util.List;
import java.util.UUID;

import javax.jcr.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.example.assessment.exception.BookException;
import org.example.assessment.model.Book;
import org.example.assessment.service.BookService;
import org.example.assessment.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookResources {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final BookService bookService;

	public BookResources(Session systemSession) {
		bookService = new BookService(systemSession);

	}

	/**
	 * Adds books to repository
	 *
	 * @param books
	 *            as list of {@link Book}
	 * @return true/false
	 */
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public boolean addBooks(List<Book> books) {
		try {
			log.debug("addBooks service is called.");
			Preconditions.checkNotEmpty(books, "books can not be empty");
			books.forEach(b -> {
				Preconditions.checkNotEmpty(b.getName(), "book name can not be empty");
				Preconditions.checkNotEmpty(b.getAuthor(), "book author can not be empty");
				Preconditions.checkNotEmpty(b.getIsbn(), "book ISBN can not be empty");
				Preconditions.checkNotEmpty(b.getIntroduction(), "book introduction can not be empty");
				Preconditions.checkNotEmpty(b.getParagraphs(), "book parapraphs can not be empty");
				b.setBookId(UUID.randomUUID().toString());
			});

			log.debug("Book size: {}", books.size());

			bookService.saveBooks(books);

			return true;

		} catch (BookException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", BookException.newInstance("Error while addBooks", e));
		} finally {
			log.debug("addBooks executed");
		}

		return false;
	}

	/**
	 * lists books in repository
	 *
	 * @return list of {@link Book}
	 */
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<Book> getBooks() {
		try {
			log.info("listBooks service is called");
			return bookService.getBooks();
		} catch (BookException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", BookException.newInstance("Error while listBooks", e));
		} finally {
			log.debug("listBooks executed");
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
	@Path("/search/{query}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<Book> searchBooks(@PathParam("query") String query) {
		try {
			log.info("searchBooks service is called");
			Preconditions.checkNotEmpty(query, "query text not be empty");
			return bookService.queryBooks(query);
		} catch (BookException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", BookException.newInstance("Error while searchBooks", e));
		} finally {
			log.debug("searchBooks executed");
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
	@Path("/delete/{bookId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@DELETE
	public boolean deleteBook(@PathParam("bookId") String bookId) {
		try {
			log.info("deleteBook service is called");
			Preconditions.checkNotEmpty(bookId, "bookId not be empty");
			bookService.deleteBook(bookId);
			return true;
		} catch (BookException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", BookException.newInstance("Error while deleteBook", e));
		} finally {
			log.debug("deleteBook executed");
		}

		return false;
	}

	/**
	 * Adds books to repository
	 *
	 * @param books
	 *            as list of {@link Book}
	 * @return true/false
	 */
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public boolean updateBook(Book book) {
		try {
			log.debug("updateBook service is called.");
			Preconditions.checkNotNull(book, "books can not be null");

			Preconditions.checkNotEmpty(book.getBookId(), "book ID name can not be empty");
			Preconditions.checkNotNull(book.getName(), "book name can not be empty");
			Preconditions.checkNotNull(book.getAuthor(), "book author can not be empty");
			Preconditions.checkNotNull(book.getIsbn(), "book ISBN can not be empty");
			Preconditions.checkNotNull(book.getIntroduction(), "book introduction can not be empty");
			Preconditions.checkNotNull(book.getParagraphs(), "book parapraphs can not be empty");

			bookService.updateBook(book);
			return true;
		} catch (BookException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", BookException.newInstance("Error while updateBook", e));
		} finally {
			log.debug("updateBook executed");
		}

		return false;
	}
}
