package io.blackbird.aemconnector.core.filters.componentwrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blackbird.aemconnector.core.objects.TranslatableContent;
import io.blackbird.aemconnector.core.utils.Node2JsonUtil;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ComponentWrapper {
    private static final String WRAPPER_DIV_CLASS = "blackbird-component-wrapper";
    private static final String DATA_ATTRIBUTE_NAME = "data-bb-translatable";

    @NotNull
    private TranslatableContent translatableContent;
    @NotNull
    private String componentContent;

    public String wrapComponent() {
        String dataAttributes = buildDataAttributes(translatableContent);
        StringBuilder html = new StringBuilder()
                .append("<div ")
                .append("class=\"")
                .append(WRAPPER_DIV_CLASS)
                .append("\"")
                .append(dataAttributes)
                .append(">")
                .append(componentContent)
                .append("</div>");
        return html.toString();
    }

    private String buildDataAttributes(TranslatableContent translatableProperty) {

        ObjectMapper mapper = Node2JsonUtil.getMapper();
        String translatablePropertyJson;
        try {
            translatablePropertyJson = mapper.writeValueAsString(translatableProperty);
        } catch (JsonProcessingException e) {
            translatablePropertyJson = "{}";
        }
        StringBuilder sb = new StringBuilder()
                .append(" ")
                .append(DATA_ATTRIBUTE_NAME)
                .append("=")
                .append("'")
                .append(translatablePropertyJson)
                .append("'");
        return sb.toString();
    }
}
