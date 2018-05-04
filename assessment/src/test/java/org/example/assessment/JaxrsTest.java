package org.example.assessment;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.ArrayUtils;
import org.easymock.EasyMock;
import org.example.assessment.common.Constants;
import org.example.assessment.model.Book;
import org.example.assessment.resource.BookResources;
import org.example.assessment.resource.BookStoreResources;
import org.example.assessment.service.BookService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.repository.RepositoryService;
import org.onehippo.repository.jaxrs.RepositoryJaxrsServlet;
import org.onehippo.repository.testutils.PortUtil;
import org.onehippo.repository.testutils.RepositoryTestCase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

@RunWith(org.easymock.EasyMockRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JaxrsTest extends RepositoryTestCase {

	private static final String NAME_BOOK1 = "nameBook1";
	private static final String NAME_BOOK2 = "nameBook2";
	private static final String NAME_BOOK3 = "nameBook3";
	private static final String QUERY_TEXT = "Search Text";

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

		book1 = createBook(NAME_BOOK1, "authorBook1", "introductionBook1", "isbnBook1", "paragrafsBook1");
		book2 = createBook(NAME_BOOK2, "authorBook2", "introductionBook2", "isbnBook2", "paragrafsBook2");
		book3 = createBook(NAME_BOOK3, "authorBook3", "introductionBook3", "isbnBook3", "paragrafsBook3 " + QUERY_TEXT);

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

	@Test
	public void test1Hello() {
		expectOK("/hello", "Hello system!");
	}

	@Test
	public void test2NoBookInRepositorySuccess() throws IOException {
		validateBooksInStoreByRepository();
	}

	@Test
	public void test3AddBooksSuccess() throws Exception {
		addBooksInternal(bookList);
		validateAddedBooks();
	}

	@Test
	public void test4DeleteBookSuccess() throws Exception {
		Book[] books = getBooks();
		if (ArrayUtils.isEmpty(books)) {
			addBooksInternal(bookList);
			books = validateAddedBooks();
		}

		Book deletingBook = books[0];

		boolean deleteBookResult = deleteBook(deletingBook.getBookId());
		Assert.assertTrue("deleteBook result failed", deleteBookResult);
	}

	@Test
	public void test5UpdateBookSuccess() throws Exception {
		Book[] books = getBooks();
		if (ArrayUtils.isEmpty(books)) {
			addBooksInternal(bookList);
			books = validateAddedBooks();
		}

		Book updatedingBook = books[0];
		updatedingBook.setName(NAME_UPDATED);

		boolean updateBookResult = updateBook(updatedingBook);
		Assert.assertTrue("deleteBook result failed", updateBookResult);

		books = getBooks();
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

	@Test
	public void test6SearchBookSuccess() throws Exception {
		Book[] books = getBooks();
		if (ArrayUtils.isEmpty(books)) {
			addBooksInternal(bookList);
			books = validateAddedBooks();
		}

		Book[] booksSearchResult = searchBooks();

		List<Book> booksSearchResultList = Lists.newArrayList(booksSearchResult);

		List<Book> booksHaveQueryTextList = Arrays.stream(books).filter(b -> b.getParagraphs().contains(QUERY_TEXT))
				.collect(Collectors.toList());

		log.info("booksSearchResultList size: {}, booksHaveQueryTextList size: {}", booksSearchResultList.size(),
				booksHaveQueryTextList.size());

		Assert.assertTrue("booksHaveQueryTextList and booksSearchResultList size not matched",
				booksHaveQueryTextList.containsAll(Lists.newArrayList(booksSearchResultList)));

	}

	@Test
	public void test10AddBooksBookException() throws Exception {
		boolean addResult = addBooks(null);
		Assert.assertFalse("addBooks result failed", addResult);
	}

	@Test(expected = RuntimeException.class)
	public void test10AddBooksException() throws Exception {
		BookService bookService = EasyMock. createMock(BookService.class);
        bookService.saveBooks(bookList);
		EasyMock.expectLastCall().andThrow(new RuntimeException());
		EasyMock.replay(bookService);

        bookService.saveBooks(bookList);

		boolean addResult = addBooks(bookList);
		Assert.assertFalse("addBooks result failed", addResult);
	}

    @Test
    public void test11DeleteBookFail() throws Exception {
        boolean deleteBookResult = deleteBook("someBookId");
        Assert.assertFalse("deleteBook result failed", deleteBookResult);
    }

	private Book[] validateAddedBooks() throws IOException {
		Book[] books = getBooks();
		Arrays.stream(books).forEach(b -> {
			Assert.assertTrue(String.format("book whose name is %s, does not exist in repository", b.getName()),
					BOOK_NAME_SET.contains(b.getName()));
		});
		return books;
	}

	private Book[] getBooks() throws IOException {
		String response = expectGetOK(BookResources.SERVICE_PATH);
		log.debug("getBooks response: \n {}", response);

		Assert.assertNotNull("getBooks response can not be null", response);

		return objectMapper.readValue(response, Book[].class);
	}

	private boolean addBooksInternal(List<Book> bookList) throws JsonProcessingException {
		boolean addResult = addBooks(bookList);
		Assert.assertTrue("addBooks result failed", addResult);

		return addResult;
	}

	private boolean addBooks(List<Book> bookList) throws JsonProcessingException {
		String response = expectPostOK(BookResources.SERVICE_PATH, objectMapper.writeValueAsString(bookList));
		log.debug("addBooks response: {}", response);

		Assert.assertNotNull("addBooks response can not be null", response);

		return Boolean.valueOf(response);
	}

	private boolean deleteBook(String bookId) throws JsonProcessingException {
		String response = expectDelete(
				BookResources.SERVICE_PATH + BookResources.METHOD_DELETE_BOOK + Constants.PATH_SEPARATOR + bookId);
		log.debug("deleteBook response: {}", response);

		Assert.assertNotNull("deleteBook response can not be null", response);

		return Boolean.valueOf(response);
	}

	private boolean updateBook(Book book) throws JsonProcessingException {
		String response = expectPostOK(BookResources.SERVICE_PATH + BookResources.METHOD_UPDATE_BOOK,
				objectMapper.writeValueAsString(book));
		log.debug("updateBook response: {}", response);

		Assert.assertNotNull("updateBook response can not be null", response);

		return Boolean.valueOf(response);
	}

	private Book[] searchBooks() throws IOException {
		String response = expectGetOK(
				BookResources.SERVICE_PATH + BookResources.METHOD_SEARCH_BOOK + Constants.PATH_SEPARATOR + QUERY_TEXT);
		log.debug("searchBooks response: {}", response);

		Assert.assertNotNull("searchBooks response can not be null", response);

		return objectMapper.readValue(response, Book[].class);
	}

	private Book[] getStoredBooks() throws IOException {
		String response = expectGetOK(BookStoreResources.SERVICE_PATH);
		log.debug("getStoredBooks response: \n {}", response);

		Assert.assertNotNull("getStoredBooks response can not be null", response);

		return objectMapper.readValue(response, Book[].class);
	}

	private void validateBooksInStoreByRepository() throws IOException {
		Book[] booksInRepository = getBooks();
		Assert.assertNotNull("booksInRepository can not be null", booksInRepository);

		Book[] booksInStore = getStoredBooks();
		Assert.assertNotNull("booksInStore can not be null", booksInStore);

		Assert.assertEquals("book size not matched booksInRepository and booksInStore", booksInRepository.length,
				booksInStore.length);

		// key = id, value - websites
		Map<String, Book> booksInRepositoryMap = Arrays.stream(booksInRepository)
				.collect(Collectors.toMap(Book::getBookId, Function.identity()));

		Arrays.stream(booksInStore).forEach(b -> {
			Book book = booksInRepositoryMap.get(b.getBookId());
			Assert.assertEquals("booksInRepository not matched to booksInStore", b, book);
			Assert.assertEquals("book name not matched booksInRepository and booksInStore", b.getName(),
					book.getName());
			Assert.assertEquals("book author not matched booksInRepository and booksInStore", b.getAuthor(),
					book.getAuthor());
			Assert.assertEquals("book ISBN not matched booksInRepository and booksInStore", b.getIsbn(),
					book.getIsbn());
			Assert.assertEquals("book introduction not matched booksInRepository and booksInStore", b.getIntroduction(),
					book.getIntroduction());
			Assert.assertEquals("book paragraphs not matched booksInRepository and booksInStore", b.getParagraphs(),
					book.getParagraphs());
		});

	}

	private static Book createBook(String name, String author, String introduction, String isbn, String paragrafs) {
		Book book = new Book();
		book.setName(name);
		book.setAuthor(author);
		book.setIntroduction(introduction);
		book.setIsbn(isbn);
		book.setParagraphs(paragrafs);

		return book;
	}

	private void expectOK(String path, String message) {
		RequestSpecification client = getClient(path);
		client.get(getServiceUrl(path)).then().statusCode(200).content(equalTo(message));
	}

	private String expectPostOK(String path, String body) {
		RequestSpecification client = getClient(path);
		Response response = client.contentType(MediaType.APPLICATION_JSON).body(body).post(getServiceUrl(path));

		response.then().statusCode(200);

		return response.body().asString();
	}

	private String expectGetOK(String path) {
		RequestSpecification client = getClient(path);
		Response response = client.get(getServiceUrl(path));
		response.then().statusCode(200);

		return response.body().asString();
	}

	private String expectDelete(String path) {
		RequestSpecification client = getClient(path);
		Response response = client.delete(getServiceUrl(path));
		response.then().statusCode(200);

		return response.body().asString();
	}

	private String getServiceUrl(String path) {
		return baseUrl + path;
	}

	private RequestSpecification getClient(String path) {
		return given().auth().preemptive().basic("admin", String.valueOf("admin"));
	}
}
