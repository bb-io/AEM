package io.blackbird.aemconnector.core.models;

import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Optional;
import java.util.function.Predicate;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED;

@Model(adaptables = Resource.class, adapters = BlackbirdEventViewerPage.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class BlackbirdEventViewerPage implements Serializable {

    @Getter(AccessLevel.NONE)
    @Self
    private transient Page page;

    private String title;
    private String path;
    private String created;
    private String modified;

    @PostConstruct
    public void init() {
       if (page == null) {
           return;
       }

        title = ObjectUtils.firstNonNull(page.getPageTitle(), page.getTitle(), page.getName());
        path = page.getPath();

        ValueMap properties = page.getProperties();
        Instant createDate = Optional.ofNullable(properties.get(JCR_CREATED, Calendar.class))
                .map(Calendar::toInstant).orElse(null);

        created = createDate == null ? null : createDate.toString();

        modified = Optional.ofNullable(page.getLastModified())
                .map(Calendar::toInstant)
                .filter(Predicate.not(modifiedDate -> isCreatedAndModifiedDatesEqual(createDate, modifiedDate)))
                .map(Instant::toString).orElse(null);
    }

    private boolean isCreatedAndModifiedDatesEqual(Instant created, Instant modified) {
        return ObjectUtils.allNotNull(created, modified)
                && created.truncatedTo(ChronoUnit.SECONDS)
                .equals(modified.truncatedTo(ChronoUnit.SECONDS));
    }
}
