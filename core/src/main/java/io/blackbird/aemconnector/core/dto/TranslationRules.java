package io.blackbird.aemconnector.core.dto;

import io.blackbird.aemconnector.core.services.impl.rules.AssetReferenceRule;
import io.blackbird.aemconnector.core.services.impl.rules.ContentFilterRule;
import io.blackbird.aemconnector.core.services.impl.rules.ContextRule;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class TranslationRules implements Serializable {

    public static final TranslationRules EMPTY = new TranslationRules(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

    private final List<ContextRule> contextRules;
    private final List<AssetReferenceRule> assetReferenceRules;
    private final List<ContentFilterRule> contentFilterRules;

}
