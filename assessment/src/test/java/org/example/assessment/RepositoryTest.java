package org.example.assessment;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Test;
import org.onehippo.repository.testutils.RepositoryTestCase;

import org.hippoecm.repository.util.Utilities;

import static org.junit.Assert.assertTrue;

public class RepositoryTest extends RepositoryTestCase {

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
        super.tearDown(); // removes /test node and checks repository clean state
    }
}
