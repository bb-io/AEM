package io.blackbird.aemconnector.core.objects;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class CqPageResource extends ResourceWrapper {

    /**
     * Creates a new wrapper instance delegating all method calls to the given
     * <code>resource</code>.
     *
     * @param resource The resource to wrap
     */
    public CqPageResource(Resource resource) {
        super(resource);
    }

    @Override
    public Iterator<Resource> listChildren() {
        return Optional.ofNullable(getChild(JcrConstants.JCR_CONTENT))
                .map(res -> List.of(res).iterator())
                .orElse(Collections.emptyIterator());
    }

    @Override
    public Iterable<Resource> getChildren() {
        return this::listChildren;
    }
}
