package io.blackbird.aemconnector.core.models.configs;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public interface PageContentFilterConfig {

    PageContentFilterConfig EMPTY = new PageContentFilterConfig() {
        @Override
        public List<Property> getBlacklistedPropertyNames() {
            return Collections.emptyList();
        }

        @Override
        public List<Property> getBlacklistedNodeNames() {
            return Collections.emptyList();
        }
    };

    @Inject
    @Named("blacklistedPropertyNames")
    List<Property> getBlacklistedPropertyNames();

    @Inject
    @Named("blacklistedNodeNames")
    List<Property> getBlacklistedNodeNames();

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    interface Property {
        @ValueMapValue
        @Named("property")
        String getPropertyName();
    }

}
