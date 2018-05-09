package org.example.assessment;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.assessment.common.Constants;
import org.example.assessment.common.ResultCode;
import org.example.assessment.model.Book;
import org.example.assessment.model.BookResponse;
import org.example.assessment.resource.BookResources;
import org.example.assessment.resource.BookStoreResources;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.repository.RepositoryService;
import org.onehippo.repository.jaxrs.RepositoryJaxrsServlet;
import org.onehippo.repository.testutils.PortUtil;
import org.onehippo.repository.testutils.RepositoryTestCase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JaxrsTest extends RepositoryTestCase {

	private static final String NAME_BOOK1 = "nameBook1'";
	private static final String NAME_BOOK2 = "nameBook2";
	private static final String NAME_BOOK3 = "nameBook3";
	private static final String QUERY_TEXT = "introduction";
	private static final String QUERY_ESCAPE_CHARS = "'%";

	private static final String NAME_UPDATED = "nameUpdated";

	private static final Set<String> BOOK_NAME_SET = Sets.newHashSet(NAME_BOOK1, NAME_BOOK2, NAME_BOOK3);

	private static Tomcat tomcat;
	private static int portNumber;

	private static List<Book> bookList;
	private static Book book1;
	private static Book book2;
	private static Book book3;

	private static String baseUrl;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@ClassRule
	public static TemporaryFolder tmpTomcatFolder = new TemporaryFolder();

	private static String getTmpTomcatFolderName() {
		return tmpTomcatFolder.getRoot().getAbsolutePath();
	}

	@BeforeClass
	public static void setupTomcat() throws LifecycleException {
		tomcat = new Tomcat();
		tomcat.setBaseDir(getTmpTomcatFolderName());
		portNumber = PortUtil.getPortNumber(JaxrsTest.class);
		tomcat.setPort(portNumber);
		Context context = tomcat.addContext("/cms", getTmpTomcatFolderName());
		Tomcat.addServlet(context, "RepositoryJaxrsServlet", new RepositoryJaxrsServlet());
		context.addServletMappingDecoded("/ws/*", "RepositoryJaxrsServlet");
		tomcat.start();

		book1 = createBook(NAME_BOOK1, "authorBook1", "95-8929-675-1",
				ArrayUtils.toArray("introduction paragraph11", "introduction paragraph12"),
				ArrayUtils.toArray("book paragraph11", "book paragraph12", "book paragraph13"));
		book2 = createBook(NAME_BOOK2, "authorBook2 "+QUERY_ESCAPE_CHARS, "95-8929-675-2",
				ArrayUtils.toArray("introduction paragraph21", "introduction paragraph22"),
				ArrayUtils.toArray("book paragraph21", "book paragraph22", "book paragraph23"));
		book3 = createBook(NAME_BOOK3, "authorBook3", "95-8929-675-3",
				ArrayUtils.toArray("introduction paragraph31", "introduction paragraph32"),
				ArrayUtils.toArray("book paragraph31 " + QUERY_TEXT, "book paragraph32", "book paragraph33"));

		bookList = Lists.newArrayList(book1, book2, book3);

		baseUrl = "http://localhost:" + portNumber + "/cms/ws";
	}

	@AfterClass
	public static void tearDownTomcat() throws LifecycleException {
		tomcat.stop();
		tomcat.destroy();
	}

	@Before
	public void before() {
		if (HippoServiceRegistry.getService(RepositoryService.class) == null) {
			HippoServiceRegistry.registerService(server.getRepository(), RepositoryService.class);
		}
	}

	/**
	 *
	 */
	@Test
	public void test1_Hello() {
		expectOK("/hello", "Hello system!");
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test2_NoBookInRepositorySuccess() throws IOException {
		validateBooksInStoreByRepository();
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test31_AddBooks_Success() throws Exception {
		addBooksInternal(bookList);
		validateAddedBooks();
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test32_AddBooks_BookException() throws Exception {
		BookResponse addBookResponse = addBooks(null);
		Assert.assertEquals("addBooks result failed", ResultCode.VALIDATION_FAILED.getCode(),
				addBookResponse.getResultCode());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test40_DeleteBook_Success() throws Exception {
		Book[] books = getBooksNotEmpty();
		Book deletingBook = books[0];

		BookResponse deleteBookResponse = deleteBook(deletingBook.getBookId());
		Assert.assertEquals("deleteBook result failed", ResultCode.SUCCESS.getCode(),
				deleteBookResponse.getResultCode());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test41_DeleteBook_Fail() throws Exception {
		BookResponse deleteBookResponse = deleteBook("someBookId");
		Assert.assertEquals("deleteBook result failed", ResultCode.NOT_FOUND.getCode(),
				deleteBookResponse.getResultCode());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test50_UpdateBook_Success() throws Exception {
		Book[] books = getBooksNotEmpty();

		Book updatedingBook = books[0];
		updatedingBook.setName(NAME_UPDATED);

		BookResponse updateBookResponse = updateBook(updatedingBook);
		Assert.assertEquals("updateBook result failed", ResultCode.SUCCESS.getCode(),
				updateBookResponse.getResultCode());

		books = getBooksInternal();
		Assert.assertTrue("No book found to check updated name", ArrayUtils.isNotEmpty(books));

		Optional<Book> bookOptional = Arrays.stream(books).filter(b -> b.getBookId().equals(updatedingBook.getBookId()))
				.findFirst();
		Assert.assertTrue(String.format("No book found in repository by bookId: %s", updatedingBook.getBookId()),
				bookOptional.isPresent());

		Book updatedBook = bookOptional.get();
		log.info("name of updatedBook: {}", updatedBook.getName());
		Assert.assertEquals(String.format("Book updated name not matched by bookId: %s", updatedingBook.getBookId()),
				NAME_UPDATED, updatedBook.getName());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test51_UpdateBook_BookException() throws Exception {
		BookResponse updateBookResponse = updateBook(createBook("someBookId"));
		Assert.assertEquals("updateBook result failed", ResultCode.VALIDATION_FAILED.getCode(),
				updateBookResponse.getResultCode());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test51_UpdateBook_Exception() throws Exception {
		Book book = createBook("name", "author", "95-8929-675-8", ArrayUtils.toArray("introduction"),
				ArrayUtils.toArray("pragraphs"));
		book.setBookId(UUID.randomUUID().toString());

		BookResponse updateBookResponse = updateBook(book);
		Assert.assertEquals("updateBook result failed", ResultCode.NOT_FOUND.getCode(),
				updateBookResponse.getResultCode());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test6_SearchBook_Success() throws Exception {
		searchBookInternal(QUERY_TEXT);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void test6_SearchBook_EscapeChars() throws Exception {
		searchBookInternal(QUERY_ESCAPE_CHARS);
	}
	
	private void searchBookInternal(String queryText) throws Exception {
		Book[] books = getBooksNotEmpty();
		Book[] booksSearchResult = searchBooks(queryText);

		List<Book> booksSearchResultList = Lists.newArrayList(booksSearchResult);

		List<Book> booksHaveQueryTextList = Arrays.stream(books).filter(b -> StringUtils.containsIgnoreCase(b.getName(),
				queryText) || StringUtils.containsIgnoreCase(b.getIsbn(), queryText)
				|| StringUtils.containsIgnoreCase(b.getAuthor(), queryText)
				|| Arrays.stream(b.getParagraphs()).anyMatch(p -> StringUtils.containsIgnoreCase(p, queryText))
				|| Arrays.stream(b.getIntroduction()).anyMatch(p -> StringUtils.containsIgnoreCase(p, queryText)))

				.collect(Collectors.toList());

		log.info("booksSearchResultList size: {}, booksHaveQueryTextList size: {}", booksSearchResultList.size(),
				booksHaveQueryTextList.size());

		Assert.assertTrue("booksHaveQueryTextList and booksSearchResultList size not matched",
				booksHaveQueryTextList.containsAll(Lists.newArrayList(booksSearchResultList)));
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private Book[] validateAddedBooks() throws IOException {
		Book[] books = getBooksInternal();
		Arrays.stream(books).forEach(b -> {
			Assert.assertTrue(String.format("book whose name is %s, does not exist in repository", b.getName()),
					BOOK_NAME_SET.contains(b.getName()));
		});
		return books;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private Book[] getBooksNotEmpty() throws IOException {
		Book[] books = getBooksInternal();
		if (ArrayUtils.isEmpty(books)) {
			addBooksInternal(bookList);
			books = validateAddedBooks();
		}
		return books;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private Book[] getBooksInternal() throws IOException {
		String response = expectGetOK(BookResources.SERVICE_PATH);
		log.debug("getBooks response: \n {}", response);

		Assert.assertNotNull("getBooks response can not be null", response);

		return objectMapper.readValue(response, Book[].class);
	}

	/**
	 * @param bookList
	 * @return
	 * @throws IOException
	 */
	private BookResponse addBooksInternal(List<Book> bookList) throws IOException {
		BookResponse addBookResponse = addBooks(bookList);
		Assert.assertEquals("addBooks result failed", ResultCode.SUCCESS.getCode(), addBookResponse.getResultCode());

		return addBookResponse;
	}

	/**
	 * @param bookList
	 * @return
	 * @throws IOException
	 */
	private BookResponse addBooks(List<Book> bookList) throws IOException {
		String response = expectPostOK(BookResources.SERVICE_PATH, objectMapper.writeValueAsString(bookList));
		log.debug("addBooks response: {}", response);

		Assert.assertNotNull("addBooks response can not be null", response);

		return objectMapper.readValue(response, BookResponse.class);
	}

	/**
	 * @param bookId
	 * @return
	 * @throws IOException
	 */
	private BookResponse deleteBook(String bookId) throws IOException {
		String response = expectDelete(
				BookResources.SERVICE_PATH + BookResources.METHOD_DELETE_BOOK + Constants.PATH_SEPARATOR + bookId);
		log.debug("deleteBook response: {}", response);

		Assert.assertNotNull("deleteBook response can not be null", response);

		return objectMapper.readValue(response, BookResponse.class);
	}

	/**
	 * @param book
	 * @return
	 * @throws IOException
	 */
	private BookResponse updateBook(Book book) throws IOException {
		String response = expectPostOK(BookResources.SERVICE_PATH + BookResources.METHOD_UPDATE_BOOK,
				objectMapper.writeValueAsString(book));
		log.debug("updateBook response: {}", response);

		Assert.assertNotNull("updateBook response can not be null", response);

		return objectMapper.readValue(response, BookResponse.class);
	}

	/**
	 * @param queryText
	 * @return
	 * @throws IOException
	 */
	private Book[] searchBooks(String queryText) throws IOException {
		String response = expectGetOK(
				BookResources.SERVICE_PATH + BookResources.METHOD_SEARCH_BOOK + Constants.PATH_SEPARATOR + queryText);
		log.debug("searchBooks response: {}", response);

		Assert.assertNotNull("searchBooks response can not be null", response);

		return objectMapper.readValue(response, Book[].class);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private Book[] getStoredBooks() throws IOException {
		String response = expectGetOK(BookStoreResources.SERVICE_PATH);
		log.debug("getStoredBooks response: \n {}", response);

		Assert.assertNotNull("getStoredBooks response can not be null", response);

		return objectMapper.readValue(response, Book[].class);
	}

	/**
	 * @throws IOException
	 */
	private void validateBooksInStoreByRepository() throws IOException {
		Book[] booksInRepository = getBooksInternal();
		Assert.assertNotNull("booksInRepository can not be null", booksInRepository);

		Book[] booksInStore = getStoredBooks();
		Assert.assertNotNull("booksInStore can not be null", booksInStore);

		Assert.assertEquals("book size not matched booksInRepository and booksInStore", booksInRepository.length,
				booksInStore.length);

		// key = bookId, value - Book
		Map<String, Book> booksInRepositoryMap = Arrays.stream(booksInRepository)
				.collect(Collectors.toMap(Book::getBookId, Function.identity()));

		// validate books
		Arrays.stream(booksInStore).forEach(b -> {
			Book book = booksInRepositoryMap.get(b.getBookId());
			Assert.assertEquals("booksInRepository not matched to booksInStore", b, book);
			Assert.assertEquals("book name not matched booksInRepository and booksInStore", b.getName(),
					book.getName());
			Assert.assertEquals("book author not matched booksInRepository and booksInStore", b.getAuthor(),
					book.getAuthor());
			Assert.assertEquals("book ISBN not matched booksInRepository and booksInStore", b.getIsbn(),
					book.getIsbn());
			Assert.assertTrue("book introduction not matched booksInRepository and booksInStore",
					Arrays.equals(b.getIntroduction(), book.getIntroduction()));
			Assert.assertTrue("book paragraphs not matched booksInRepository and booksInStore",
					Arrays.equals(b.getParagraphs(), book.getParagraphs()));
		});

	}

	/**
	 * @param name
	 * @param author
	 * @param introduction
	 * @param isbn
	 * @param paragraphs
	 * @return
	 */
	private static Book createBook(String name, String author, String isbn, String[] introduction,
			String[] paragraphs) {
		Book book = new Book();
		book.setName(name);
		book.setAuthor(author);
		book.setIntroduction(introduction);
		book.setIsbn(isbn);
		book.setParagraphs(paragraphs);

		return book;
	}

	/**
	 * @param bookId
	 * @return
	 */
	private static Book createBook(String bookId) {
		Book book = new Book();
		book.setBookId(bookId);

		return book;
	}

	/**
	 * @param path
	 * @param message
	 */
	private void expectOK(String path, String message) {
		RequestSpecification client = getClient();
		client.get(getServiceUrl(path)).then().statusCode(200).content(equalTo(message));
	}

	/**
	 * @param path
	 * @param body
	 * @return
	 */
	private String expectPostOK(String path, String body) {
		RequestSpecification client = getClient();
		Response response = client.contentType(MediaType.APPLICATION_JSON).body(body).post(getServiceUrl(path));

		response.then().statusCode(200);

		return response.body().asString();
	}

	/**
	 * @param path
	 * @return
	 */
	private String expectGetOK(String path) {
		RequestSpecification client = getClient();
		Response response = client.get(getServiceUrl(path));
		response.then().statusCode(200);

		return response.body().asString();
	}

	/**
	 * @param path
	 * @return
	 */
	private String expectDelete(String path) {
		RequestSpecification client = getClient();
		Response response = client.delete(getServiceUrl(path));
		response.then().statusCode(200);

		return response.body().asString();
	}

	/**
	 * @param path
	 * @return
	 */
	private String getServiceUrl(String path) {
		return baseUrl + path;
	}

	/**
	 * return a REST client with basic authentication
	 *
	 * @return a rest client
	 */
	private RequestSpecification getClient() {
		return given().auth().preemptive().basic("admin", String.valueOf("admin"));
	}
}
