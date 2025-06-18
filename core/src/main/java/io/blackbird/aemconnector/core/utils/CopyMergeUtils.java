package io.blackbird.aemconnector.core.utils;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class CopyMergeUtils {

    private static final String PROPERTY_PATH = "propertyPath";
    private static final String PROPERTY_NAME = "propertyName";
    private static final String REFERENCE_PATH = "referencePath";
    private static final String SLASH = "/";

    public static void updateResourceReferences(Resource targetResource, JsonNode references) {
        for (JsonNode reference : references) {
            String propertyPath = reference.path(PROPERTY_PATH).asText(null);
            String propertyName = reference.path(PROPERTY_NAME).asText(null);
            String referencePath = reference.path(REFERENCE_PATH).asText(null);
            if (ObjectUtils.anyNotNull(propertyPath, propertyName, referencePath)) {
                Optional.ofNullable(targetResource.getChild(StringUtils.removeStart(propertyPath, SLASH)))
                        .map(resource -> resource.adaptTo(ModifiableValueMap.class))
                        .ifPresent(properties -> properties.put(propertyName, referencePath));
            }
        }
    }

    public static void createResourcesHierarchicallyIfNotExist(String targetParentPath, String sourceParentPath, ResourceResolver resolver) throws BlackbirdResourceCopyMergeException, PersistenceException, WCMException {
        String targetPath = targetParentPath;
        String sourcePath = sourceParentPath;
        Resource targetResource = resolver.getResource(targetPath);
        Resource sourceResource = resolver.getResource(sourcePath);
        Map<Resource, String> nonExistentResources = new LinkedHashMap<>();

        while (targetResource == null && PathUtils.getName(targetPath) != null && sourceResource != null) {
            nonExistentResources.put(sourceResource, PathUtils.getName(targetPath));
            targetPath = PathUtils.getParent(targetPath);
            targetResource = resolver.getResource(targetPath);
            sourcePath = PathUtils.getParent(sourcePath);
            sourceResource = resolver.getResource(sourcePath);
        }
        if (targetResource == null) {
            throw new BlackbirdResourceCopyMergeException(String.format("Root resource doesn't exist for path: %s", targetParentPath));
        }
        List<Map.Entry<Resource, String>> entries = new ArrayList<>(nonExistentResources.entrySet());
        Collections.reverse(entries);
        for (Map.Entry<Resource, String> entry : entries) {
            Resource source = entry.getKey();
            String resourceName = entry.getValue();
            String primaryType = source.getValueMap().get(JcrConstants.JCR_PRIMARYTYPE, String.class);
            if (NameConstants.NT_PAGE.equals(primaryType)) {
                PageManager pageManager = requireNonNull(resolver.adaptTo(PageManager.class), "Cannot adapt to PageManager");
                Page sourcePage = source.adaptTo(Page.class);
                PageManager.CopyOptions options = new PageManager.CopyOptions();
                options.page = sourcePage;
                options.destination = targetResource.getPath().concat(SLASH).concat(resourceName);
                options.shallow = true;
                options.resolveConflict = true;
                options.autoSave = true;
                targetResource = pageManager.copy(options);
            } else {
                Map<String, Object> properties = new HashMap<>();
                properties.put(JcrConstants.JCR_PRIMARYTYPE, primaryType);
                targetResource = resolver.create(targetResource, resourceName, properties);
            }
            resolver.commit();
        }
    }

    public static void mergeJsonIntoResource(Resource resource, JsonNode jsonNode) {
        if (!jsonNode.isObject()) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            if (value.isObject()) {
                Resource child = resource.getChild(key);
                mergeJsonIntoResource(child, value);
            } else {
                ModifiableValueMap modifiableValueMap = resource.adaptTo(ModifiableValueMap.class);
                if (modifiableValueMap != null) {
                    if (value.isArray()) {
                        String[] items = new String[value.size()];
                        for (int i = 0; i < value.size(); i++) {
                            items[i] = value.get(i).asText();
                        }
                        modifiableValueMap.put(key, items);
                    } else {
                        modifiableValueMap.put(key, value.asText());
                    }
                }
            }
        }
    }
}
