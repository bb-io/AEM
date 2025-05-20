package io.blackbird.aemconnector.core.utils;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryUtilsTest {

    @Test
    void shouldGetPath() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Item item = mock(Item.class);
        String expectedPath = "/content/path";
        when(item.getPath()).thenReturn(expectedPath);

        // WHEN
        String actualPath = RepositoryUtils.getPath(item);

        // THEN
        assertEquals(expectedPath, actualPath);
        verify(item).getPath();
    }

    @Test
    void shouldThrowExceptionWhenGetPathFails() throws RepositoryException {
        // GIVEN
        Item item = mock(Item.class);
        when(item.getPath()).thenThrow(new RepositoryException("Test exception"));

        // WHEN & THEN
        assertThrows(BlackbirdInternalErrorException.class, () -> RepositoryUtils.getPath(item));
        verify(item).getPath();
    }

    @Test
    void shouldGetParent() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Item item = mock(Item.class);
        Node parent = mock(Node.class);
        when(item.getParent()).thenReturn(parent);

        // WHEN
        Node actualParent = RepositoryUtils.getParent(item);

        // THEN
        assertEquals(parent, actualParent);
        verify(item).getParent();
    }

    @Test
    void shouldReturnNullWhenAccessDenied() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Item item = mock(Item.class);
        when(item.getParent()).thenThrow(new AccessDeniedException("Access denied"));
        when(item.getPath()).thenReturn("/content/path");

        // WHEN
        Node actualParent = RepositoryUtils.getParent(item);

        // THEN
        assertNull(actualParent);
        verify(item).getParent();
    }

    @Test
    void shouldThrowExceptionWhenGetParentFails() throws RepositoryException {
        // GIVEN
        Item item = mock(Item.class);
        when(item.getParent()).thenThrow(new RepositoryException("Test exception"));
        when(item.getPath()).thenReturn("/content/path");

        // WHEN & THEN
        assertThrows(BlackbirdInternalErrorException.class, () -> RepositoryUtils.getParent(item));
        verify(item).getParent();
    }

    @Test
    void shouldHandleNullPropertyPath() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        Session session = mock(Session.class);
        String propertyName = "testProperty";

        when(property.getSession()).thenReturn(session);
        when(property.getPath()).thenReturn(null);

        // WHEN
        Node result = RepositoryUtils.getParentWithProperty(property, propertyName);

        // THEN
        assertNull(result);
    }

    @Test
    void shouldHandleRootPath() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        Session session = mock(Session.class);
        String propertyName = "testProperty";

        when(property.getSession()).thenReturn(session);
        when(property.getPath()).thenReturn("/");

        // WHEN
        Node result = RepositoryUtils.getParentWithProperty(property, propertyName);

        // THEN
        assertNull(result);
    }

    @Test
    void shouldThrowExceptionWhenGetParentWithPropertyFails() throws RepositoryException {
        // GIVEN
        Property property = mock(Property.class);
        when(property.getSession()).thenThrow(new RepositoryException("Test exception"));

        // WHEN & THEN
        assertThrows(BlackbirdInternalErrorException.class,
                () -> RepositoryUtils.getParentWithProperty(property, "testProperty"));
    }

    @Test
    void shouldGetName() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Item item = mock(Item.class);
        String expectedName = "testName";
        when(item.getName()).thenReturn(expectedName);

        // WHEN
        String actualName = RepositoryUtils.getName(item);

        // THEN
        assertEquals(expectedName, actualName);
        verify(item).getName();
    }

    @Test
    void shouldThrowExceptionWhenGetNameFails() throws RepositoryException {
        // GIVEN
        Item item = mock(Item.class);
        when(item.getName()).thenThrow(new RepositoryException("Test exception"));

        // WHEN & THEN
        assertThrows(BlackbirdInternalErrorException.class, () -> RepositoryUtils.getName(item));
        verify(item).getName();
    }

    @Test
    void shouldGetPropertyAsString() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);
        Property property = mock(Property.class);
        String propertyName = "testProperty";
        String expectedValue = "testValue";

        when(node.getProperty(propertyName)).thenReturn(property);
        when(property.getString()).thenReturn(expectedValue);

        // WHEN
        String actualValue = RepositoryUtils.getPropertyAsString(node, propertyName);

        // THEN
        assertEquals(expectedValue, actualValue);
        verify(node).getProperty(propertyName);
        verify(property).getString();
    }

    @Test
    void shouldThrowExceptionWhenGetPropertyAsStringFails() throws RepositoryException {
        // GIVEN
        Node node = mock(Node.class);
        when(node.getProperty(anyString())).thenThrow(new RepositoryException("Test exception"));

        // WHEN & THEN
        assertThrows(BlackbirdInternalErrorException.class,
                () -> RepositoryUtils.getPropertyAsString(node, "testProperty"));
        verify(node).getProperty("testProperty");
    }

    @Test
    void shouldCheckIfHasProperty() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Node node = mock(Node.class);
        String propertyName = "testProperty";
        when(node.hasProperty(propertyName)).thenReturn(true);

        // WHEN
        boolean hasProperty = RepositoryUtils.hasProperty(node, propertyName);

        // THEN
        assertTrue(hasProperty);
        verify(node).hasProperty(propertyName);
    }

    @Test
    void shouldThrowExceptionWhenHasPropertyFails() throws RepositoryException {
        // GIVEN
        Node node = mock(Node.class);
        when(node.hasProperty(anyString())).thenThrow(new RepositoryException("Test exception"));

        // WHEN & THEN
        assertThrows(BlackbirdInternalErrorException.class,
                () -> RepositoryUtils.hasProperty(node, "testProperty"));
        verify(node).hasProperty("testProperty");
    }

    @Test
    void shouldHandleRootNodeInGetParent() throws RepositoryException {
        // GIVEN
        Node rootNode = mock(Node.class);
        when(rootNode.getPath()).thenReturn("/");
        when(rootNode.getParent()).thenThrow(new RepositoryException("Root node has no parent"));

        // WHEN & THEN
        assertThrows(BlackbirdInternalErrorException.class, () -> RepositoryUtils.getParent(rootNode));
        verify(rootNode).getParent();
    }

    @Test
    void shouldHandlePathWithOneSegment() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        Session session = mock(Session.class);
        String propertyName = "testProperty";

        when(property.getSession()).thenReturn(session);
        when(property.getPath()).thenReturn("/content");

        // WHEN
        Node result = RepositoryUtils.getParentWithProperty(property, propertyName);

        // THEN
        assertNull(result);
        verify(property).getSession();
        verify(property).getPath();
    }

    @Test
    void shouldHandleExceptionsInGetParentWithProperty() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        String propertyName = "testProperty";

        // Setup property to throw exception
        when(property.getSession()).thenThrow(new RepositoryException("Test exception"));

        // WHEN & THEN
        assertThrows(BlackbirdInternalErrorException.class,
                () -> RepositoryUtils.getParentWithProperty(property, propertyName));
    }

    @Test
    void shouldHandlePathWithTwoSegments() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        Session session = mock(Session.class);
        String propertyName = "testProperty";

        when(property.getSession()).thenReturn(session);
        when(property.getPath()).thenReturn("/content/node");

        // WHEN
        Node result = RepositoryUtils.getParentWithProperty(property, propertyName);

        // THEN
        assertNull(result);
        verify(property).getSession();
        verify(property).getPath();
    }

    @Test
    void shouldHandleEmptyPropertyPath() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        Session session = mock(Session.class);
        String propertyName = "testProperty";

        when(property.getSession()).thenReturn(session);
        when(property.getPath()).thenReturn("");

        // WHEN
        Node result = RepositoryUtils.getParentWithProperty(property, propertyName);

        // THEN
        assertNull(result);
        verify(property).getSession();
        verify(property).getPath();
    }

    @Test
    void shouldFindNodeWithProperty() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        Session session = mock(Session.class);
        Node node = mock(Node.class);
        String propertyName = "testProperty";

        when(property.getSession()).thenReturn(session);
        when(property.getPath()).thenReturn("/test/path/to/property");
        when(node.hasProperty(propertyName)).thenReturn(true);

        try (MockedStatic<JcrUtils> jcrUtil = Mockito.mockStatic(JcrUtils.class)) {
            jcrUtil.when(() -> JcrUtils.getNodeIfExists("/test/path/to/property", session)).thenReturn(node);

            // WHEN
            Node result = RepositoryUtils.getParentWithProperty(property, propertyName);

            // THEN
            assertEquals(node, result);
            verify(property).getSession();
            verify(property).getPath();
            verify(node).hasProperty(propertyName);
        }
    }

    @Test
    void shouldFindAncestorNodeWithProperty() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        Session session = mock(Session.class);
        Node childNode = mock(Node.class);
        Node parentNode = mock(Node.class);
        String propertyName = "testProperty";
        String childPath = "/test/path/to/property";
        String parentPath = "/test/path";

        when(property.getSession()).thenReturn(session);
        when(property.getPath()).thenReturn(childPath);
        when(childNode.hasProperty(propertyName)).thenReturn(false);
        when(parentNode.hasProperty(propertyName)).thenReturn(true);

        try (MockedStatic<JcrUtils> jcrUtil = Mockito.mockStatic(JcrUtils.class)) {
            jcrUtil.when(() -> JcrUtils.getNodeIfExists(childPath, session)).thenReturn(childNode);
            jcrUtil.when(() -> JcrUtils.getNodeIfExists(parentPath, session)).thenReturn(parentNode);

            // Mock getParent to return parentNode when called on childNode
            when(childNode.getParent()).thenReturn(parentNode);

            // WHEN
            Node result = RepositoryUtils.getParentWithProperty(property, propertyName);

            // THEN
            assertEquals(parentNode, result);
            verify(property).getSession();
            verify(property).getPath();
            verify(childNode).hasProperty(propertyName);
            verify(parentNode).hasProperty(propertyName);
            verify(childNode).getParent();
        }
    }

    @Test
    void shouldSkipNonExistentNodesAndFindNodeWithProperty() throws RepositoryException, BlackbirdInternalErrorException {
        // GIVEN
        Property property = mock(Property.class);
        Session session = mock(Session.class);
        Node node = mock(Node.class);
        String propertyName = "testProperty";
        String propertyPath = "/test/path/to/deep/property";
        String nonExistentPath = "/test/path/to/deep";
        String existingPath = "/test/path/to";

        when(property.getSession()).thenReturn(session);
        when(property.getPath()).thenReturn(propertyPath);
        when(node.hasProperty(propertyName)).thenReturn(true);

        try (MockedStatic<JcrUtils> jcrUtil = Mockito.mockStatic(JcrUtils.class)) {
            // The property path node doesn't exist
            jcrUtil.when(() -> JcrUtils.getNodeIfExists(propertyPath, session)).thenReturn(null);
            // The intermediate path node doesn't exist
            jcrUtil.when(() -> JcrUtils.getNodeIfExists(nonExistentPath, session)).thenReturn(null);
            // But this node exists and has the property
            jcrUtil.when(() -> JcrUtils.getNodeIfExists(existingPath, session)).thenReturn(node);

            // WHEN
            Node result = RepositoryUtils.getParentWithProperty(property, propertyName);

            // THEN
            assertEquals(node, result);
            verify(property).getSession();
            verify(property).getPath();
            verify(node).hasProperty(propertyName);
        }
    }

}
