package io.blackbird.aemconnector.core.services;

import io.blackbird.aemconnector.core.objects.TranslatableContent;
import org.jetbrains.annotations.NotNull;

import javax.jcr.Node;
import java.util.Map;

public interface TranslatableDataExtractor {

    Map<String, TranslatableContent> extractFor(@NotNull Node node);
}
