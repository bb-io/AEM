package io.blackbird.aemconnector.core.models;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_LASTMODIFIED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;

@Model(adaptables = Resource.class, adapters = BlackbirdEventViewerContent.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class BlackbirdEventViewerContent implements Serializable {

    private static final String CQ_LAST_MODIFIED = "cq:lastModified";
    private static final String FMDITA_TITLE = "fmditaTitle";
    private static final String CQ_TAGS = "cq:tags";
    private static final String[] TAG_LOCATIONS = {
            "jcr:content",
            "jcr:content/metadata"
    };

    private String title;
    private String path;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant created;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant modified;
    private Set<String> tags;

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
        tags = getTags(resource);
    }

    private Calendar getLastModified(Resource resource) {
        return Optional.ofNullable(resource.getChild(JCR_CONTENT))
                .map(Resource::getValueMap)
                .map(props -> {
                    Calendar modified = props.get(CQ_LAST_MODIFIED, Calendar.class);
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
            return StringUtils.firstNonBlank(properties.get(JCR_TITLE, String.class), properties.get(FMDITA_TITLE, String.class));
        }
        return null;
    }

    private Set<String> getTags(Resource resource) {
        Set<String> tags = new HashSet<>();
        TagManager tagManager = resource.getResourceResolver().adaptTo(TagManager.class);
        if (tagManager == null) {
            return tags;
        }

        for (String location : TAG_LOCATIONS) {
            Resource child = resource.getChild(location);
            if (child != null) {
                ValueMap valueMap = child.adaptTo(ValueMap.class);
                if (valueMap != null && valueMap.containsKey(CQ_TAGS)) {
                    String[] tagIds = valueMap.get(CQ_TAGS, new String[]{});
                    for (String tagId : tagIds) {
                        Tag tag = tagManager.resolve(tagId);
                        if (tag != null) {
                            tags.add(tag.getTagID());
                        }
                    }
                }
            }
        }
        return tags;
    }
}
