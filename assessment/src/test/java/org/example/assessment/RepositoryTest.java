package org.example.assessment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.example.assessment.common.Constants;
import org.example.assessment.common.ResultCode;
import org.example.assessment.exception.BookException;
import org.example.assessment.util.BookUtil;
import org.junit.After;
import org.junit.Test;
import org.onehippo.repository.testutils.RepositoryTestCase;

public class RepositoryTest extends RepositoryTestCase {

	private final static String HIPPO_QUERY_PATH = "/hippo:configuration/hippo:queries";
	private final static String PROPRTY_VALUE_FORMATTER = "%s= %s";

	@Test
	public void test_minimal() throws RepositoryException {
		Node rootNode = session.getRootNode();
		rootNode.addNode("test");
		rootNode.addNode("books");
		session.save();
		assertTrue(session.nodeExists("/test"));
		assertTrue(session.nodeExists("/books"));
	}

	@After
	@Override
	public void tearDown() throws Exception {
		removeNode("/books");
		super.tearDown(); // removes /test node and checks repository clean
							// state
	}

	@Test
	public void testPrintHippoQueries() throws RepositoryException, IOException {
		log.info(toString(session.getNode(HIPPO_QUERY_PATH)));
	}

	/**
	 * Traverses and prints nodes
	 *
	 * @param node
	 * @throws RepositoryException
	 */
	private String toString(Node node) throws RepositoryException, IOException {

		assertNotNull(node);

		StringBuilder sb = new StringBuilder();
		if (node.getParent() != null) {
			sb.append("\n");
		}
		sb.append(printableNodeText(node));

		// toString subNodes
		NodeIterator nodeIterator = node.getNodes();
		while (nodeIterator.hasNext()) {
			sb.append(toString(nodeIterator.nextNode()));
		}

		return sb.toString();
	}

	/**
	 * Prints path, type and values of a node
	 * 
	 * @param node
	 * @throws RepositoryException
	 */
	public String printableNodeText(Node node) throws RepositoryException, IOException {

		StringBuilder sb = new StringBuilder();

		String pathAsString = String.format(PROPRTY_VALUE_FORMATTER, "NODE", node.getName());
		sb.append(StringUtils.leftPad(pathAsString, pathAsString.length() + node.getDepth())).append("\n");

		pathAsString = String.format(PROPRTY_VALUE_FORMATTER, "IDENTIFIER", node.getIdentifier());
		sb.append(StringUtils.leftPad(pathAsString, pathAsString.length() + node.getDepth())).append("\n");

		pathAsString = String.format(PROPRTY_VALUE_FORMATTER, "PATH", node.getPath());
		sb.append(StringUtils.leftPad(pathAsString, pathAsString.length() + node.getDepth())).append("\n");

		sb.append(StringUtils.leftPad("Properties= ", "Properties= ".length() + node.getDepth())).append("\n");

		PropertyIterator propertyIterator = node.getProperties();
		while (propertyIterator.hasNext()) {
			sb.append(toString(propertyIterator.nextProperty(), node.getDepth() + 1)).append("\n");
		}

		return sb.toString();
	}

	/**
	 * @param property
	 * @param deep
	 * @return
	 * @throws RepositoryException
	 */
	private String toString(Property property, int deep)
			throws RepositoryException, IllegalStateException, IOException {
		String propertyValue;
		if (property.isMultiple()) {
			propertyValue = Arrays.toString(BookUtil.toStringArray(property, property.getName()));
		} else if (property.getType() == PropertyType.BINARY) {
			propertyValue = convert(property.getValue());
		} else {
			propertyValue = property.getValue().getString();

		}

		String text = String.format(PROPRTY_VALUE_FORMATTER, property.getName(), propertyValue);

		return StringUtils.leftPad(text, text.length() + deep);
	}

	/**
	 * @param value
	 * @return
	 * @throws IOException
	 * @throws RepositoryException
	 */
	private String convert(Value value) throws IOException, RepositoryException {
		Binary bv = value.getBinary();
		InputStream is = bv.getStream();
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
			int b = 0;
			while (b != -1) {
				b = is.read();
				if (b != -1) {
					bout.write(b);
				}
			}
			return Base64.getEncoder().encodeToString(bout.toByteArray());
		} catch (Exception e) {
			throw BookException.newInstance(ResultCode.FAILED, Constants.ERROR_INTERNAL, e);
		}
	}
}
