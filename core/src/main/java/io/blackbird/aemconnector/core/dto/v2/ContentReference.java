package io.blackbird.aemconnector.core.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

import java.io.Serializable;

@Value
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ContentReference implements Serializable {
    String propertyName;
    String propertyPath;
    String referencePath;

    public ContentReference(String propertyName, String propertyPath, String referencePath) {
        this.propertyName = propertyName;
        this.propertyPath = propertyPath;
        this.referencePath = referencePath;
    }

    public ContentReference(String referencePath) {
        this.referencePath = referencePath;
        this.propertyName = null;
        this.propertyPath = null;
    }
}
