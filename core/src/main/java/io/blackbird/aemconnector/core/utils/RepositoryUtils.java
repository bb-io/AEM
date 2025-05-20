package io.blackbird.aemconnector.core.utils;

import io.blackbird.aemconnector.core.exceptions.BlackbirdInternalErrorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static io.blackbird.aemconnector.core.utils.PathUtils.PATH_SEPARATOR;

@Slf4j
public class RepositoryUtils {

    public static final String ROOT_PATH = "/";

    private RepositoryUtils() {
    }

    public static String getPath(Item item) throws BlackbirdInternalErrorException {
        try {
            return item.getPath();
        } catch (RepositoryException e) {
            String message = String.format("Can't get path of %s, error: %s", item, e);
            log.trace(message);
            throw new BlackbirdInternalErrorException(message);
        }
    }

    public static Node getParent(Item item) throws BlackbirdInternalErrorException {
        try {
            return item.getParent();
        } catch (AccessDeniedException e) {
            log.trace("Access to {} denied, error: {}", getPath(item), e.toString());
            return null;
        } catch (RepositoryException e) {
            log.trace("Can't get parent of {}, error: {}", getPath(item), e.toString());
            throw new BlackbirdInternalErrorException(e.getMessage());
        }
    }

    public static Node getParentWithProperty(Property property, String propertyName) throws BlackbirdInternalErrorException {
        try {
            Session session = property.getSession();
            String currentPath = property.getPath();
            while (StringUtils.countMatches(currentPath, PATH_SEPARATOR) > 1) {
                Node node = JcrUtils.getNodeIfExists(currentPath, session);
                if (null != node) {
                    Node result = getParentWithProperty(node, propertyName);
                    if (null != result) {
                        return result;
                    }
                }
                currentPath = StringUtils.substringBeforeLast(currentPath, PATH_SEPARATOR);
            }
        } catch (RepositoryException e) {
            throw new BlackbirdInternalErrorException(e.getMessage());
        }
        return null;
    }

    public static String getName(Item item) throws BlackbirdInternalErrorException {
        try {
            return item.getName();
        } catch (RepositoryException e) {
            String message = String.format("Can't get name of %s, error: %s", item, e);
            log.trace(message);
            throw new BlackbirdInternalErrorException(message);
        }
    }

    public static String getPropertyAsString(Node node, String propertyName) throws BlackbirdInternalErrorException {
        try {
            return node.getProperty(propertyName).getString();
        } catch (RepositoryException e) {
            String message = String.format("Can't get property %s of %s, error: %s", propertyName, node, e);
            log.trace(message);
            throw new BlackbirdInternalErrorException(message);
        }
    }

    public static boolean hasProperty(Node node, String propertyName) throws BlackbirdInternalErrorException {
        try {
            return node.hasProperty(propertyName);
        } catch (RepositoryException e) {
            String message = String.format("Can't check if property %s of %s exists, error: %s", propertyName, node, e);
            log.trace(message);
            throw new BlackbirdInternalErrorException(message);
        }
    }

    private static Node getParentWithProperty(Node node, String propertyName) throws BlackbirdInternalErrorException {
        Node currentNode = node;
        do {
            if (hasProperty(currentNode, propertyName)) {
                return currentNode;
            }
            currentNode = getParent(currentNode);
        } while (null != currentNode && !ROOT_PATH.equals(getPath(currentNode)));

        log.debug("Can't find parent of {} with propertyName={}", getPath(node), propertyName);
        return null;
    }

}
