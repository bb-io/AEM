package io.blackbird.aemconnector.core.objects;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Builder
@Value
public class PageEventSearchParams {
    String rootPath;
    String startDate;
    String endDate;
    Set<String> events;
    long offset;
    long limit;
}
