package org.example.assessment.store;

import java.util.List;
import java.util.stream.Collectors;

import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.example.assessment.common.Constants;
import org.example.assessment.exception.BookException;
import org.example.assessment.model.Book;
import org.example.assessment.util.BookUtil;
import org.example.assessment.util.Preconditions;
import org.example.assessment.util.RepositoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookStore {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final Session session;

	private List<Book> bookList = Lists.newCopyOnWriteArrayList();

	private final EventListener listener = (EventIterator events) -> {
		while (events.hasNext()) {
			try {
				Event e = events.nextEvent();
				if (log.isDebugEnabled()) {
					log.debug("Path: {}, Type: {}, TypeName: {}", e.getPath(), e.getType(), getEventName(e));
				}

				switch (e.getType()) {
				case Event.NODE_ADDED:
					String bookId = getBookIdFromNodePath(e.getPath());
					log.debug("{} is added", bookId);
					loadBook(bookId);
					break;

				case Event.NODE_REMOVED:
					bookId = getBookIdFromNodePath(e.getPath());
					log.debug("{} is removed", bookId);
					bookList.remove(BookUtil.toBook(bookId));
					break;

				case Event.PROPERTY_CHANGED:
					bookId = getBookIdFromPropertyPath(e.getPath());
					log.debug("{} is changed", bookId);
					updateBook(bookId);
					break;

				default:
					break;
				}
			} catch (Exception e) {
				log.error("", BookException.newInstance("onEvent error", e));
			}
		}
	};

	private BookStore(Builder builder) {
		this.session = builder.session;
		try {
			loadBooks();
			this.session.getWorkspace().getObservationManager().addEventListener(listener,
					Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_CHANGED, "/" + Constants.REPOSITORY, true,
					null, null, false);
		} catch (Exception e) {
			throw BookException.newInstance("Error while setting up node EventListener", e);
		}
	}

	/**
	 * @param path
	 * @return
	 */
	private String getBookIdFromNodePath(String path) {
		return path.substring(path.lastIndexOf(Constants.PATH_SEPARATOR) + 1);
	}

	/**
	 * @param path
	 * @return
	 */
	private String getBookIdFromPropertyPath(String path) {
		String[] parts = path.split("/");

		return parts[parts.length - 2];
	}

	/**
	 * Returns stored books
	 * 
	 * @return
	 */
	public List<Book> getBookList() {
		return bookList;
	}

	/**
	 * @param bookId
	 */
	private void updateBook(String bookId) {
		try {
			Book book = RepositoryUtil.getBook(session, bookId);
			if (book == null) {
				log.error("No book with nodeName {} in repository", bookId);
				loadBooks();
				return;
			}

			List<Book> filteredBook = bookList.stream().filter(o -> o.getBookId().equals(bookId))
					.collect(Collectors.toList());
			if (filteredBook.size() != 1) {
				log.error("book size mismatched with bookId: ", bookId);
				loadBooks();
			}

			Book storedBook = filteredBook.get(0);
			bookList.set(bookList.indexOf(storedBook), book);

		} catch (Exception e) {
			log.error("Error while loadBooks", e);
		} finally {
			log.debug("cached book size: {}", bookList.size());
		}
	}

	/**
	 * @param bookName
	 */
	private void loadBook(String bookName) {
		try {
			Book book = RepositoryUtil.getBook(session, bookName);
			log.debug("Book exist with {}: {}", bookName, bookList.contains(BookUtil.toBook(bookName)));
			if (book != null) {
				bookList.add(book);
			}
		} catch (Exception e) {
			log.error("Error while loadBooks", e);
		} finally {
			log.debug("{} book(s) loaded", bookList.size());
		}
	}

	/**
	 * Update books in book store object
	 */
	private void loadBooks() {
		try {
			bookList.clear();
			List<Book> books = RepositoryUtil.getBooks(session);
			bookList.addAll(books);
		} catch (Exception e) {
			log.error("Error while loadBooks", e);
		} finally {
			log.debug("{} book(s) loaded", bookList.size());
		}
	}

	/**
	 * @param list
	 * @param name
	 * @return
	 */
	public List<Book> containsBook(List<Book> list, String bookId) {
		return list.stream().filter(o -> o.getBookId().equals(bookId)).collect(Collectors.toList());
	}

	/**
	 * @param e
	 * @return
	 */
	private String getEventName(Event e) {
		switch (e.getType()) {
		case Event.NODE_ADDED:
			return "NODE_ADDED";

		case Event.NODE_MOVED:
			return "NODE_MOVED";

		case Event.NODE_REMOVED:
			return "NODE_REMOVED";

		case Event.PERSIST:
			return "PERSIST";

		case Event.PROPERTY_ADDED:
			return "PROPERTY_ADDED";

		case Event.PROPERTY_CHANGED:
			return "PROPERTY_CHANGED";

		case Event.PROPERTY_REMOVED:
			return "PROPERTY_REMOVED";

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
		public BookStore build() {
			Preconditions.checkNotNull(session, "");

			return new BookStore(this);
		}
	}
}
