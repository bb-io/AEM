package io.blackbird.aemconnector.core.dto.v2;

import com.day.cq.wcm.api.NameConstants;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.Serializable;

@Value
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ContentReference implements Serializable {
    String propertyName;
    String propertyPath;
    String referencePath;

    @JsonCreator
    public ContentReference(@JsonProperty(NameConstants.PN_DT_NAME) String propertyName,
                            @JsonProperty("propertyPath") String propertyPath,
                            @JsonProperty("referencePath") String referencePath) {
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
