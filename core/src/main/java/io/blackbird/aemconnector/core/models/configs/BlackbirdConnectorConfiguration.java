package io.blackbird.aemconnector.core.models.configs;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class BlackbirdConnectorConfiguration {

    @Self
    private Resource resource;

    private static final String PAGE_CONTENT_FILTER = "pageContentFilter";

    public PageContentFilterConfig getPageContentFilterConfig() {
        Resource pageContentFilterResource = resource.getChild(PAGE_CONTENT_FILTER);
        return pageContentFilterResource != null ? pageContentFilterResource.adaptTo(PageContentFilterConfig.class) : PageContentFilterConfig.EMPTY;
    }

}
