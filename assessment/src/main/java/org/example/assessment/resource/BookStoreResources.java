package org.example.assessment.resource;

import java.util.List;

import javax.jcr.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.example.assessment.exception.BookException;
import org.example.assessment.model.Book;
import org.example.assessment.store.BookStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookStoreResources {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final BookStore bookStore;

	public BookStoreResources(Session session) {
		bookStore = BookStore.newBuilder().withSession(session).build();
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
		List<Book> bookList;
		try {
			log.info("listBooks service is called");
			bookList = bookStore.getBookList();
		} catch (BookException e) {
			bookList = Lists.newArrayList();
			log.error("", e);
		} catch (Exception e) {
			bookList = Lists.newArrayList();
			log.error("", BookException.newInstance("Error while listBooks", e));
		} finally {
			log.debug("listBooks executed");
		}
		return bookList;
	}
}
