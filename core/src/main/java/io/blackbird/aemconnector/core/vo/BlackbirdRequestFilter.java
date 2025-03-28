package io.blackbird.aemconnector.core.vo;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Value Object that represents request params
 */
public class BlackbirdRequestFilter {

    boolean validFilters = true;

    public BlackbirdRequestFilter(SlingHttpServletRequest request) {
        //some validations
    }

    public boolean isValidFilters() {
        return validFilters;
    }
}
