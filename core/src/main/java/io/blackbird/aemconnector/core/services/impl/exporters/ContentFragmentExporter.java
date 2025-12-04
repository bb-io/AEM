package io.blackbird.aemconnector.core.services.impl.exporters;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentVariation;
import com.adobe.cq.dam.cfm.ElementTemplate;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.adobe.cq.dam.cfm.VariationDef;
import com.day.cq.commons.jcr.JcrConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blackbird.aemconnector.core.constants.ServletConstants;
import io.blackbird.aemconnector.core.dto.v2.ContentReference;
import io.blackbird.aemconnector.core.exceptions.BlackbirdServiceException;
import io.blackbird.aemconnector.core.services.ContentExporter;
import io.blackbird.aemconnector.core.services.ContentType;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.utils.Node2JsonUtil;
import io.blackbird.aemconnector.core.utils.ObjectUtils;
import io.blackbird.aemconnector.core.utils.StreamUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component(service = ContentExporter.class)
public class ContentFragmentExporter implements ContentExporter {

    public static final String TRANSLATABLE = "translatable";
    private static final List<String> REFERENCE_TYPES = Arrays.asList("reference", "content-fragment");
    public static final String REFERENCES = "references";
    public static final String DEFAULT_VARIATION = "master";
    public static final String VARIATION_KEY = "variation";

    private static final ObjectMapper MAPPER = Node2JsonUtil.getMapper();
    public static final String DATA = "data";

    @Reference
    private TranslationRulesService translationRulesService;

    @Override
    public boolean canExport(ContentType contentType) {
        return ContentType.CONTENT_FRAGMENT == contentType;
    }


    @Override
    public Serializable export(Resource resource, Map<String, Object> options) {

        ContentFragment contentFragment = ObjectUtils.ensureNotNull(resource.adaptTo(ContentFragment.class),
                () -> new BlackbirdServiceException(String.format("Resource %s could not be adapted to ContentFragment", resource.getPath())));

        String selectedVariation = resolveVariation(resource, contentFragment, options);

        List<String> translatablePropertiesFromTranslationRules = getTranslatablePropertiesFromVariation(selectedVariation, resource);

        boolean ignoreTranslationRules = options.containsKey(ServletConstants.IGNORE_TRANSLATION_RULES);
        ObjectNode result = MAPPER.createObjectNode();
        ObjectNode variationNode = initializeResultTree(result, selectedVariation);
        ArrayNode references = result.putArray(REFERENCES);



        processFragment(
                contentFragment,
                variationNode,
                references,
                selectedVariation,
                translatablePropertiesFromTranslationRules,
                ignoreTranslationRules
        );

        return result;
    }

    private List<String> getTranslatablePropertiesFromVariation(String variation, Resource resource) {
        String pathToVariation = resource.getPath() + "/" + JcrConstants.JCR_CONTENT + "/data/" + variation;
        Node variationNode = Optional.ofNullable(resource)
                .map(Resource::getResourceResolver)
                .map(resolver -> resolver.getResource(pathToVariation))
                .map(res -> res.adaptTo(Node.class))
                .orElse(null);
        return translationRulesService.collectTranslatableProperties(variationNode);
    }

    static String resolveVariation(Resource resource, ContentFragment contentFragment, Map<String, Object> options) {
        String variationOption = (String) options.get(VARIATION_KEY);

        return Optional.ofNullable(variationOption)
                .map(v -> {
                    boolean hasVariation = StreamUtils.stream(contentFragment.listAllVariations())
                            .map(VariationDef::getName)
                            .anyMatch(v::equals);
                    if (!hasVariation) {
                        throw new BlackbirdServiceException(
                                String.format("Requested variation '%s' not found for content fragment at '%s'", v, resource.getPath())
                        );
                    }
                    return v;
                })
                .orElse(DEFAULT_VARIATION);
    }

    private void processFragment(ContentFragment contentFragment, ObjectNode variationNode, ArrayNode references, String variationName, List<String> translatablePropertiesFromTranslationRules, boolean ignoreTranslationRules) {
        FragmentTemplate template = contentFragment.getTemplate();
        Map<String, Map<String, Object>> propertyConfig = StreamUtils.stream(template.getElements())
                .collect(Collectors.toMap(
                        ElementTemplate::getName,
                        ElementTemplate::getMetaData));
        contentFragment.getElements().forEachRemaining(element -> handleElement(element, propertyConfig, variationNode, references, variationName, translatablePropertiesFromTranslationRules, ignoreTranslationRules));
    }

    private void handleElement(ContentElement element, Map<String, Map<String, Object>> propertyConfig, ObjectNode variationNode, ArrayNode references, String variationName, List<String> translatablePropertiesFromTranslationRules, boolean ignoreTranslationRules) {
        String name = element.getName();
        Map<String, Object> metadata = propertyConfig.get(name);

        if (metadata == null) {
            return;
        }

        FragmentData fragmentData = getFragmentData(element, variationName);
        if (fragmentData == null) {
            return;
        }

        Object value = fragmentData.getValue();
        boolean isTranslatable = ignoreTranslationRules
                || Boolean.TRUE.equals(metadata.get(TRANSLATABLE))
                || translatablePropertiesFromTranslationRules.contains(name);

        if (isTranslatable && value != null) {
            variationNode.set(name, MAPPER.valueToTree(value));
        }

        String semanticType = fragmentData.getDataType().getSemanticType();
        if (REFERENCE_TYPES.contains(semanticType) && value instanceof String) {
            String propertyPath = JcrConstants.JCR_CONTENT + "/data/" + variationName;
            String referencePath = (String) value;
            ContentReference ref = new ContentReference(name, propertyPath, referencePath);
            references.add(MAPPER.valueToTree(ref));
        }
    }

    private FragmentData getFragmentData(ContentElement element, String variationName) {
        if (DEFAULT_VARIATION.equals(variationName)) {
            return element.getValue();
        }
        ContentVariation variation = element.getVariation(variationName);
        if (variation == null) {
            return null;
        }
        return variation.getValue();
    }

    private static ObjectNode initializeResultTree(ObjectNode result, String variationNodeName) {
        ObjectNode jcrContent = result.putObject(JcrConstants.JCR_CONTENT);
        ObjectNode data = jcrContent.putObject(DATA);
        return data.putObject(variationNodeName);
    }
}
