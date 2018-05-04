package org.example.assessment.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.example.assessment.exception.BookException;
import org.example.assessment.model.Book;
import org.example.assessment.store.BookCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookStoreResources {

	public static final String SERVICE_PATH = "/bookstore";
	public static final String METHOD_GET_BOOK = "/";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public BookStoreResources() {
	}

	/**
	 * lists books in repository
	 *
	 * @return list of {@link Book}
	 */
	@Path(METHOD_GET_BOOK)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<Book> getStoredBooks() {
		try {
			log.debug("getStoredBooks service is called");

			List<Book> bookList = BookCache.getInstance().getBookList();

			log.debug("{} stored book(s) found", bookList.size());

			return bookList;
		} catch (BookException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", BookException.newInstance("Error while getStoredBooks", e));
		} finally {
			log.debug("getStoredBooks executed");
		}

		return Lists.newArrayList();
	}
}
