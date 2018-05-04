package org.example.assessment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.onehippo.repository.testutils.RepositoryTestCase;

import com.google.common.collect.Lists;

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
			propertyValue = Arrays.toString(Lists.newArrayList(property.getValues()).parallelStream().map(v -> {
				try {
					return v.getString();
				} catch (ValueFormatException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
				return "";
			}).collect(Collectors.toList()).toArray());
		} else if (property.getType() == PropertyType.BINARY) {
			propertyValue = property.getBinary().toString();
		} else {
			propertyValue = property.getValue().getString();

		}

		String text = String.format(PROPRTY_VALUE_FORMATTER, property.getName(), propertyValue);

		return StringUtils.leftPad(text, text.length() + deep);
	}
}
