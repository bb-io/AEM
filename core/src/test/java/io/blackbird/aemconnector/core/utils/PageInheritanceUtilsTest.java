package io.blackbird.aemconnector.core.utils;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class PageInheritanceUtilsTest {

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
    private Node node;

    @BeforeEach
    void setUp() {
        Resource resource = context.create().resource("/content/test");
        node = resource.adaptTo(Node.class);
    }

    @Test
    void shouldReturnTrueWhenNodeIsLocal() throws RepositoryException {
        assertTrue(PageInheritanceUtils.isLocalNode(node));
    }

    @Test
    void shouldReturnFalseWhenNodeHasLiveRelationshipMixin() throws RepositoryException {
        node.addMixin("cq:LiveRelationship");
        assertFalse(PageInheritanceUtils.isLocalNode(node));
    }

    @Test
    void shouldReturnFalseWhenNodeHasMasterProperty() throws RepositoryException {
        node.setProperty("cq:master", "/content/test");
        assertFalse(PageInheritanceUtils.isLocalNode(node));
    }

    @Test
    void shouldReturnTrueWhenNodeHasLiveSyncCancelledMixin() throws RepositoryException {
        node.addMixin("cq:LiveSyncCancelled");
        assertTrue(PageInheritanceUtils.isNodeInheritanceCancelled(node));
    }

    @Test
    void shouldReturnTrueWhenNodeHasCancelledForChildrenProperty() throws RepositoryException {
        node.setProperty("cq:isCancelledForChildren", true);
        assertTrue(PageInheritanceUtils.isNodeInheritanceCancelled(node));
    }

    @Test
    void shouldReturnTrueWhenNodeHasPropertyLiveSyncCancelledMixinAndPropertyInCancelledList() throws RepositoryException {
        node.addMixin("cq:PropertyLiveSyncCancelled");

        Value value = context.resourceResolver().adaptTo(javax.jcr.Session.class)
                .getValueFactory().createValue("jcr:title");

        node.setProperty("cq:propertyInheritanceCancelled", new Value[]{value});
        Property property = node.setProperty("jcr:title", "Test");

        assertTrue(PageInheritanceUtils.isPropertyInheritanceCancelled(node, property));
    }

    @Test
    void shouldReturnFalseWhenNodeHasPropertyLiveSyncCancelledMixinAndPropertyNotInCancelledList() throws RepositoryException {
        node.addMixin("cq:PropertyLiveSyncCancelled");

        Value value = context.resourceResolver().adaptTo(javax.jcr.Session.class)
                .getValueFactory().createValue("cq:description");

        node.setProperty("cq:propertyInheritanceCancelled", new Value[]{value});
        Property property = node.setProperty("jcr:title", "Test");

        assertFalse(PageInheritanceUtils.isPropertyInheritanceCancelled(node, property));
    }

    @Test
    void shouldExportPropertyWhenNodeIsLocal() throws RepositoryException {
        Property property = node.setProperty("jcr:title", "Test");
        assertTrue(PageInheritanceUtils.shouldExportProperty(node, property));
    }

    @Test
    void shouldExportPropertyWhenNodeInheritanceCancelled() throws RepositoryException {
        node.setProperty("cq:isCancelledForChildren", true);
        Property property = node.setProperty("jcr:title", "Test");
        assertTrue(PageInheritanceUtils.shouldExportProperty(node, property));
    }

    @Test
    void shouldExportPropertyWhenPropertyInheritanceCancelled() throws RepositoryException {
        node.addMixin("cq:PropertyLiveSyncCancelled");

        Value value = context.resourceResolver().adaptTo(javax.jcr.Session.class)
                .getValueFactory().createValue("jcr:title");

        node.setProperty("cq:propertyInheritanceCancelled", new Value[]{value});
        Property property = node.setProperty("jcr:title", "Test");

        assertTrue(PageInheritanceUtils.shouldExportProperty(node, property));
    }

    @Test
    void shouldNotExportPropertyWhenNodeNotLocalAndInheritanceNotCancelled() throws RepositoryException {
        node.addMixin("cq:LiveRelationship");
        node.setProperty("cq:master", "/content/test");
        Property property = node.setProperty("jcr:title", "Test");

        assertFalse(PageInheritanceUtils.shouldExportProperty(node, property));
    }
}
