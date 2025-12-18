package io.blackbird.aemconnector.core.utils;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

public final class PageInheritanceUtils {

    private PageInheritanceUtils() {
    }

    private static final String MIXIN_LIVE_RELATIONSHIP = "cq:LiveRelationship";
    private static final String MIXIN_LIVE_SYNC_CANCELLED = "cq:LiveSyncCancelled";
    private static final String MIXIN_PROPERTY_LIVE_SYNC_CANCELLED = "cq:PropertyLiveSyncCancelled";

    private static final String PROP_CANCELLED_FOR_CHILDREN = "cq:isCancelledForChildren";
    private static final String PROP_PROPERTY_INHERITANCE_CANCELLED = "cq:propertyInheritanceCancelled";
    private static final String PROP_BLUEPRINT_MASTER = "cq:master";

    public static boolean shouldExportProperty(Node node, Property property) throws RepositoryException {
        if (isLocalNode(node)) {
            return true;
        }
        if (isNodeInheritanceCancelled(node)) {
            return true;
        }
        return isPropertyInheritanceCancelled(node, property);
    }

    public static boolean isLocalNode(Node node) throws RepositoryException {
        return !node.isNodeType(MIXIN_LIVE_RELATIONSHIP) && !node.hasProperty(PROP_BLUEPRINT_MASTER);
    }

    public static boolean isNodeInheritanceCancelled(Node node) throws RepositoryException {
        return node.isNodeType(MIXIN_LIVE_SYNC_CANCELLED) || (node.hasProperty(PROP_CANCELLED_FOR_CHILDREN)
                && node.getProperty(PROP_CANCELLED_FOR_CHILDREN).getBoolean());
    }

    public static boolean isPropertyInheritanceCancelled(Node node, Property property) throws RepositoryException {
        if (node.isNodeType(MIXIN_PROPERTY_LIVE_SYNC_CANCELLED) && node.hasProperty(PROP_PROPERTY_INHERITANCE_CANCELLED)) {
            for (Value value : node.getProperty(PROP_PROPERTY_INHERITANCE_CANCELLED).getValues()) {
                if (value.getString().equals(property.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
