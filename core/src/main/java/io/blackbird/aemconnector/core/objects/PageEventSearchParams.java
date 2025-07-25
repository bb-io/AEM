package io.blackbird.aemconnector.core.objects;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Builder
@Value
public class PageEventSearchParams {
    String rootPath;
    String type;
    String startDate;
    String endDate;
    Set<String> events;
    Set<String> tags;
    String keyword;
    long offset;
    long limit;
}
