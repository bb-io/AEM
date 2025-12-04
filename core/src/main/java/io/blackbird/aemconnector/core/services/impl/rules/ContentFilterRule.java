package io.blackbird.aemconnector.core.services.impl.rules;

import lombok.Value;

@Value
public class ContentFilterRule {
    String key;
    String value;
    boolean createLanguageCopy;
}
