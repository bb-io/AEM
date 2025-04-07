package io.blackbird.aemconnector.core.stubs;

import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.ResourceResolverWrapper;
import org.jetbrains.annotations.NotNull;

public class ResourceResolverStub extends ResourceResolverWrapper {

    private final PageManager pageManager;

    public ResourceResolverStub(ResourceResolver resolver, PageManager pageManager) {
        super(resolver);
        this.pageManager = pageManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <AdapterType> AdapterType adaptTo(@NotNull Class<AdapterType> type) {
        if (type == PageManager.class) {
            return (AdapterType) pageManager;
        }
        return super.adaptTo(type);
    }
}
