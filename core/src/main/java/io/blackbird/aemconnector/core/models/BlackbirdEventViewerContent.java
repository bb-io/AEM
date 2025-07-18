package io.blackbird.aemconnector.core.models;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.blackbird.aemconnector.core.utils.jackson.InstantSerializer;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LASTMODIFIED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.day.cq.wcm.api.constants.NameConstants.PN_PAGE_LAST_MOD;

@Model(adaptables = Resource.class, adapters = BlackbirdEventViewerContent.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class BlackbirdEventViewerContent implements Serializable {

    private String title;
    private String path;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant created;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant modified;

    @Inject
    public BlackbirdEventViewerContent(@Self Resource resource,
                                       @ValueMapValue(name = JCR_CREATED) Calendar pageCreatedDate) {
        if (resource == null) {
            return;
        }

        title = getTitle(resource);
        path = resource.getPath();
        created = Optional.ofNullable(pageCreatedDate).map(Calendar::toInstant).orElse(null);
        modified = Optional.ofNullable(getLastModified(resource))
                .map(Calendar::toInstant)
                .filter(modifiedDate -> !isSameDates(created, modifiedDate))
                .orElse(null);
    }

    private Calendar getLastModified(Resource resource) {
        return Optional.ofNullable(resource.getChild(JCR_CONTENT))
                .map(Resource::getValueMap)
                .map(props -> {
                    Calendar modified = props.get(PN_PAGE_LAST_MOD, Calendar.class);
                    return (modified != null) ? modified : props.get(JCR_LASTMODIFIED, Calendar.class);
                })
                .orElse(null);
    }

    private boolean isSameDates(Instant created, Instant modified) {
        return ObjectUtils.allNotNull(created, modified)
                && created.truncatedTo(ChronoUnit.SECONDS)
                .equals(modified.truncatedTo(ChronoUnit.SECONDS));
    }

    private String getTitle(Resource resource) {
        Page page = resource.adaptTo(Page.class);
        if (page != null) {
            return StringUtils.firstNonBlank(page.getPageTitle(), page.getTitle(), page.getName());
        }
        Resource jcrContent = resource.getChild(JCR_CONTENT);
        if (jcrContent != null) {
            ValueMap properties = jcrContent.getValueMap();
            return StringUtils.firstNonBlank(properties.get(JCR_TITLE, String.class), properties.get("fmditaTitle", String.class));
        }
        return null;
    }
}
