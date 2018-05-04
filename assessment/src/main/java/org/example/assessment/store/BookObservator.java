/**
 * 
 */
package org.example.assessment.store;

import java.util.List;
import java.util.Optional;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.cxf.common.util.CollectionUtils;
import org.example.assessment.common.Constants;
import org.example.assessment.exception.BookException;
import org.example.assessment.model.Book;
import org.example.assessment.util.Preconditions;
import org.example.assessment.util.RepositoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by resulav on 04.05.2018.
 */
public class BookObservator {
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final Session session;

	private final EventListener listener = (EventIterator events) -> {
		while (events.hasNext()) {
			try {
				Event e = events.nextEvent();
				if (log.isDebugEnabled()) {
					log.debug("Path: {}, Type: {}, TypeName: {}", e.getPath(), e.getType(), getEventName(e));
				}

				switch (e.getType()) {
				case Event.NODE_ADDED:
					loadBookByBookPath(e.getPath());
					break;

				case Event.NODE_REMOVED:
					removeBook(e.getPath());
					break;

				case Event.PROPERTY_CHANGED:
					loadBookByPropertyPath(e.getPath());
					break;

				default:
					break;
				}
			} catch (Exception e) {
				log.error("", BookException.newInstance("onEvent error", e));
			}
		}
	};

	/**
	 * 
	 * @param builder
	 */
	private BookObservator(Builder builder) {
		this.session = builder.session;
		try {
			loadBooks();
			this.session.getWorkspace().getObservationManager().addEventListener(listener,
					Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_CHANGED,
					Constants.PATH_SEPARATOR + Constants.REPOSITORY, true, null, null, false);
		} catch (Exception e) {
			throw BookException.newInstance("Error while setting up node EventListener", e);
		}
	}

	/**
	 * remove book from store
	 * 
	 * @param bookpath
	 *            book real path
	 */
	private void removeBook(String bookpath) {
		// get bookId from book path. The last part of path is bookId
		// /content/documents/myhippoproject/books/1090da58-0b99-422d-add9-f8d676fd3948
		String bookId = bookpath.substring(bookpath.lastIndexOf(Constants.PATH_SEPARATOR) + 1);
		BookCache.getInstance().removeBook(bookId);
		log.debug("Book removed from store by bookId {}", bookId);
	}

	/**
	 * gets the book from repository and puts it in store
	 * 
	 * @param bookPath
	 */
	private void loadBookByBookPath(String bookPath) {
		// get book from repository by real path
		Optional<Book> bookOptional = RepositoryUtil.getBookByPath(session, bookPath);
		if (bookOptional.isPresent()) {
			// put book to cache
			BookCache.getInstance().addOrUpdateBook(bookOptional.get());
			log.debug("{} is stored", bookPath);
		} else {
			log.debug("No book in repository by {}", bookPath);
		}
		log.debug("{} book(s) stored", BookCache.getInstance().getBookList().size());
	}

	/**
	 * gets the book from repository and puts it in store
	 * 
	 * @param propertyPath
	 */
	private void loadBookByPropertyPath(String propertyPath) {
		loadBookByBookPath(propertyPath.substring(0, propertyPath.lastIndexOf('/')));
	}

	/**
	 * gets books from repository and puts them in store
	 * 
	 * @throws RepositoryException
	 */
	private void loadBooks() throws RepositoryException {
		List<Book> bookList = RepositoryUtil.getBooks(session);
		if (CollectionUtils.isEmpty(bookList)) {
			log.info("No book to store");
			return;
		}
		bookList.forEach(b -> BookCache.getInstance().addOrUpdateBook(b));
		log.debug("{} book(s) stored", BookCache.getInstance().getBookList().size());
	}

	/** returns event name by event type
	 * @param e
	 * @return
	 */
	private String getEventName(Event e) {
		switch (e.getType()) {
		case Event.NODE_ADDED:
			return "NODE_ADDED";

		case Event.NODE_REMOVED:
			return "NODE_REMOVED";

		case Event.PROPERTY_CHANGED:
			return "PROPERTY_CHANGED";

		default:
			return "UNKNOWN";
		}
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * {@code BookStore} builder static inner class.
	 */
	public static final class Builder {
		private Session session;

		private Builder() {
		}

		/**
		 * Sets the {@code session} and returns a reference to this Builder so
		 * that the methods can be chained together.
		 *
		 * @param session
		 *            the {@code session} to set
		 * @return a reference to this Builder
		 */
		public Builder withSession(Session session) {
			this.session = session;
			return this;
		}

		/**
		 * Returns a {@code BookStore} built from the parameters previously set.
		 *
		 * @return a {@code BookStore} built with parameters of this
		 *         {@code BookStore.Builder}
		 */
		public BookObservator build() {
			Preconditions.checkNotNull(session, "session canot be null");
			return new BookObservator(this);
		}
	}
}
