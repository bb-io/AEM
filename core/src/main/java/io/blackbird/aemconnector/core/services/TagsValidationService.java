package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;

import java.util.Set;

public interface TagsValidationService {

    void validateTags(Set<String> tags) throws BlackbirdServiceException;

}
