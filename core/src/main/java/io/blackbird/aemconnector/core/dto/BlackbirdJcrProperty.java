package io.blackbird.aemconnector.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple DTO for jcr property, optionally can have multifield values or single value
 * use @JsonInclude(JsonInclude.Include.NON_EMPTY) to prevent empty values[] shown
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"name", "value", "values"})
public class BlackbirdJcrProperty implements Serializable {
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String value;
    private List<String> values;

    public BlackbirdJcrProperty() {
    }

    public BlackbirdJcrProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public BlackbirdJcrProperty(String name, List<String> values) {
        this.name = name;
        this.values = new ArrayList<>(values);
    }

    public boolean isMultiValue() {
        return values != null;
    }

    public List<String> getValues() {
        return values == null ? Collections.emptyList() : new ArrayList<>(values);
    }

    public void setValues(List<String> values) {
        this.values = values == null ? Collections.emptyList() : new ArrayList<>(values);
    }
}
