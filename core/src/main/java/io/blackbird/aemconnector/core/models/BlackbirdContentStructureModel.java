package io.blackbird.aemconnector.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.blackbird.aemconnector.core.dto.BlackbirdJcrNode;
import io.blackbird.aemconnector.core.dto.BlackbirdJcrProperty;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Model(adaptables = Resource.class, adapters = BlackbirdContentStructureModel.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"values"})
public class BlackbirdContentStructureModel implements Serializable {

    @Self
    private transient Resource resource;

    @JsonIgnore
    private transient List<BlackbirdJcrNode> contents;

    @JsonProperty("values")
    private List<BlackbirdJcrNode> filteredContents;

    @PostConstruct
    protected void init() {
        contents = new ArrayList<>();
        if (resource != null) {
            for (Resource child : resource.getChildren()) {
                BlackbirdJcrNode node = new BlackbirdJcrNode();
                node.setName(child.getName());
                node.setPath(child.getPath());

                node.setProperties(child.getValueMap().entrySet().stream()
                        .map(e -> toJcrProperty(e.getKey(), e.getValue()))
                        .collect(Collectors.toList()));

                contents.add(node);
            }
        }
    }

    public List<BlackbirdJcrNode> getContents() {
        return contents == null ? Collections.emptyList() : new ArrayList<>(contents);
    }

    public void setFilteredContents(List<BlackbirdJcrNode> filteredContents) {
        this.filteredContents = filteredContents == null ? Collections.emptyList() : new ArrayList<>(filteredContents);
    }

    /**
     * Maps value map value to a serializable DTO, supports single field, multified and several data types
     * Might also use instanceof Calendar for Date objects
     *
     * @return BlackbirdJcrProperty
     */
    private BlackbirdJcrProperty toJcrProperty(String key, Object value) {
        if (value instanceof String) {
            return new BlackbirdJcrProperty(key, (String) value);
        } else if (value instanceof Calendar) {
            return Optional.of((Calendar) value)
                    .map(Calendar::toInstant)
                    .map(Instant::toString)
                    .map(date -> new BlackbirdJcrProperty(key, date)).orElse(null);
        } else if (value instanceof String[]) {
            return new BlackbirdJcrProperty(key, Arrays.asList((String[]) value));
        } else if (value instanceof Object[]) {
            Object[] arr = (Object[]) value;
            List<String> stringify = Arrays.stream(arr)
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            return new BlackbirdJcrProperty(key, stringify);
        } else {
            return new BlackbirdJcrProperty(key, String.valueOf(value));
        }
    }
}
