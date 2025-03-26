package io.blackbird.aemconnector.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.sling.api.SlingConstants.PROPERTY_PATH;


/**
 * Simple DTO for jcr nodes, explicitly sets node name and path, along to its value map
 * use @JsonInclude(JsonInclude.Include.NON_EMPTY) to prevent empty values[] shown
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"name", PROPERTY_PATH, "properties"})
public class BlackbirdJcrNode implements Serializable {
    private String name;
    private String path;
    private List<BlackbirdJcrProperty> properties;

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public List<BlackbirdJcrProperty> getProperties() {
        return properties == null ? Collections.emptyList() : new ArrayList<>(properties);
    }

    public void setProperties(List<BlackbirdJcrProperty> properties) {
        this.properties = properties == null ? Collections.emptyList() : new ArrayList<>(properties);
    }
}
