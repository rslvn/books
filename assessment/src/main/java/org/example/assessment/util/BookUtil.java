package org.example.assessment.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ArrayUtils;
import org.example.assessment.common.BookField;
import org.example.assessment.exception.BookException;
import org.example.assessment.model.Book;

import com.google.common.collect.Lists;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookUtil {
    private BookUtil() {
        // for sonarqube
    }

    /**
     * Converts nodeIterator to Book list.
     *
     * @param nodeIterator nodes in iterator
     * @return a {@link Book} instance
     * @throws RepositoryException as generic exception
     */
    public static List<Book> toBookList(NodeIterator nodeIterator) throws RepositoryException {
        List<Book> books = Lists.newArrayList();
        while (nodeIterator.hasNext()) {
            books.add(BookUtil.toBook(nodeIterator.nextNode()));
        }
        return books;
    }

    /**
     * Converts node to Book.
     *
     * @param node book node
     * @return a {@link Book} instance
     * @throws RepositoryException as generic exception
     */
    public static Book toBook(Node node) throws RepositoryException {
        Book book = new Book();
        book.setBookId(node.getName());
        if (node.hasProperty(BookField.NAME.getFieldName())) {
            book.setName(node.getProperty(BookField.NAME.getFieldName()).getString());
        }
        if (node.hasProperty(BookField.AUTHOR.getFieldName())) {
            book.setAuthor(node.getProperty(BookField.AUTHOR.getFieldName()).getString());
        }
        if (node.hasProperty(BookField.ISBN.getFieldName())) {
            book.setIsbn(node.getProperty(BookField.ISBN.getFieldName()).getString());
        }
        if (node.hasProperty(BookField.INTRODUCTION.getFieldName())) {
            book.setIntroduction(toStringArray(node.getProperty(BookField.INTRODUCTION.getFieldName()), BookField.INTRODUCTION.getFieldName()));
        }
        if (node.hasProperty(BookField.PARAGRAPHS.getFieldName())) {
            book.setParagraphs(toStringArray(node.getProperty(BookField.PARAGRAPHS.getFieldName()), BookField.PARAGRAPHS.getFieldName()));
        }

        return book;
    }

    private static String[] toStringArray(Property property, String propertyName) throws RepositoryException {
        if (!property.isMultiple()) {
            return ArrayUtils.toArray(property.getString());
        }
        return Arrays.stream(property.getValues()).map(v -> {
            try {
                return v.getString();
            } catch (RepositoryException e) {
                throw BookException.newInstance(String.format("Invalid %s value", propertyName), e);

            }
        }).toArray(String[]::new);
    }

    /**
     * Converts Book to node.
     *
     * @param book     book instance
     * @param bookNode book node
     * @throws RepositoryException as generic exception
     */
    public static void toNode(Book book, Node bookNode) throws RepositoryException {
        bookNode.setProperty(BookField.NAME.getFieldName(), book.getName());
        bookNode.setProperty(BookField.AUTHOR.getFieldName(), book.getAuthor());
        bookNode.setProperty(BookField.ISBN.getFieldName(), book.getIsbn());
        bookNode.setProperty(BookField.INTRODUCTION.getFieldName(), book.getIntroduction());
        bookNode.setProperty(BookField.PARAGRAPHS.getFieldName(), book.getParagraphs());
    }

    /**
     * Converts Book to node.
     *
     * @param book     book instance
     * @param bookNode book node
     * @throws RepositoryException as generic exception
     */
    public static void toNodeForUpdate(Book book, Node bookNode) throws RepositoryException {
        setPropertyValueIfChanged(bookNode, BookField.NAME.getFieldName(), book.getName());
        setPropertyValueIfChanged(bookNode, BookField.AUTHOR.getFieldName(), book.getAuthor());
        setPropertyValueIfChanged(bookNode, BookField.ISBN.getFieldName(), book.getIsbn());
        setPropertyValueIfChanged(bookNode, BookField.INTRODUCTION.getFieldName(), book.getIntroduction());
        setPropertyValueIfChanged(bookNode, BookField.PARAGRAPHS.getFieldName(), book.getIntroduction());
    }

    private static void setPropertyValueIfChanged(Node bookNode, String fieldName, String[] newValue) throws RepositoryException {
        if (!bookNode.hasProperty(fieldName) || !Arrays.equals(newValue, toStringArray(bookNode.getProperty(fieldName), fieldName))) {
            bookNode.setProperty(fieldName, newValue);
        }
    }

    private static void setPropertyValueIfChanged(Node bookNode, String fieldName, String newValue) throws RepositoryException {
        if (!bookNode.hasProperty(fieldName) || !Objects.equals(newValue, bookNode.getProperty(fieldName).getString())) {
            bookNode.setProperty(fieldName, newValue);
        }
    }

}
