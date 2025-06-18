package io.blackbird.aemconnector.core.dto.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ContentReference implements Serializable {

    private String path;

    private List<ContentReference> references;

    public ContentReference(String path) {
        this.path = path;
        this.references = new ArrayList<>();
    }
}
