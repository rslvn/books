package org.example.assessment.store;

import java.util.List;
import java.util.Map;

import org.example.assessment.model.Book;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by resulav on 04.05.2018.
 */
public class BookCache {

	private List<Book> bookList = Lists.newCopyOnWriteArrayList();
	private Map<String, Book> bookMap = Maps.newConcurrentMap();

	private BookCache() {
		// for sonarqube
	}

	public static BookCache getInstance() {
		return HOLDER.INSTANCE;
	}

	/**
	 * Retrieve cached book list
	 * 
	 * @return books in a new list
	 */
	public List<Book> getBookList() {
		return Lists.newArrayList(bookList);
	}

	/**
	 * Adds the book to cache If bookId is new or updates the book in cache If
	 * the bookId exists
	 * 
	 * @param book
	 *            book to add or to update
	 * @return cached book instance
	 */
	protected Book addOrUpdateBook(Book book) {
		Book cachedBook = bookMap.get(book.getBookId());
		if (cachedBook == null) {
			bookList.add(book);
			return bookMap.put(book.getBookId(), book);
		}

		if (bookList.contains(book)) {
			bookList.set(bookList.indexOf(cachedBook), book);
		} else {
			bookList.add(book);
		}

		return bookMap.put(book.getBookId(), book);
	}

	/**
	 * Removes the book from cache by bookId
	 * 
	 * @param bookId
	 *            the bookId of the removing book
	 * @return the removed book instance
	 */
	protected Book removeBook(String bookId) {
		Book removedBook = bookMap.remove(bookId);
		bookList.remove(removedBook);

		return removedBook;
	}

	/**
	 * Cache holder
	 * 
	 * @author resulav
	 *
	 */
	private static final class HOLDER {
		private static final BookCache INSTANCE = new BookCache();
	}

}
