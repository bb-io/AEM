package io.blackbird.aemconnector.core.services.impl.rules;

import lombok.Value;

import java.io.Serializable;

@Value
public class ContentFilterRule implements Serializable {
    String key;
    String value;
    boolean createLanguageCopy;
}
