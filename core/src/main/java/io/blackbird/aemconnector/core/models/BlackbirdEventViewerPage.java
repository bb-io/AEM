package io.blackbird.aemconnector.core.models;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.blackbird.aemconnector.core.dto.ContentReference;
import io.blackbird.aemconnector.core.services.ReferenceCollectorService;
import io.blackbird.aemconnector.core.utils.jackson.InstantSerializer;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED;

@Model(adaptables = Resource.class, adapters = BlackbirdEventViewerPage.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class BlackbirdEventViewerPage implements Serializable {

    private String title;
    private String path;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant created;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant modified;
    private List<ContentReference> references;
    @JsonIgnore
    @OSGiService
    private ReferenceCollectorService referenceCollectorService;

    @Inject
    public BlackbirdEventViewerPage(@Self Page page,
                                    @ValueMapValue(name = JCR_CREATED) Calendar pageCreatedDate) {
        if (page == null) {
            return;
        }

        title = getPageTitle(page);
        path = page.getPath();
        created = Optional.ofNullable(pageCreatedDate).map(Calendar::toInstant).orElse(null);
        modified = Optional.ofNullable(page.getLastModified())
                .map(Calendar::toInstant)
                .filter(modifiedDate -> !isSameDates(created, modifiedDate))
                .orElse(null);
    }

    @PostConstruct
    private void init() {
        references = referenceCollectorService.getReferences(path);
    }

    private boolean isSameDates(Instant created, Instant modified) {
        return ObjectUtils.allNotNull(created, modified)
                && created.truncatedTo(ChronoUnit.SECONDS)
                .equals(modified.truncatedTo(ChronoUnit.SECONDS));
    }

    private String getPageTitle(Page page) {
        return Stream.of(page.getPageTitle(), page.getTitle(), page.getName())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
