package io.blackbird.aemconnector.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.blackbird.aemconnector.core.models.BlackbirdEventViewerContent;
import io.blackbird.aemconnector.core.models.BlackbirdEventViewerPage;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"rootPath", "startDate", "endDate", "events", "limit", "offset", "total", "more", "results", "pages"})
@Value
@Builder
@Jacksonized
public class BlackbirdPageEventViewerDto implements Serializable {
    String rootPath;
    String type;
    String startDate;
    String endDate;
    transient Set<String> events;
    Set<String> tags;
    String keywords;
    long offset;
    long limit;
    @JsonProperty("total")
    long totalMatches;
    @JsonProperty("more")
    boolean hasMore;
    int results;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    transient List<BlackbirdEventViewerContent> content;
}
