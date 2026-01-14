package io.blackbird.aemconnector.core.objects;

import lombok.Value;

import java.io.Serializable;
import java.util.Map;

@Value
public class TranslatableContent implements Serializable {
    String path;
    private Map<String, String> properties;
}
