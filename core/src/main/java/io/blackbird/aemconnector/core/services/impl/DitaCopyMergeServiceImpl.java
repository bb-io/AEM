package io.blackbird.aemconnector.core.services.impl;

import com.adobe.granite.asset.api.AssetManager;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.WCMException;
import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.objects.VersionSyncResult;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.services.DitaCopyMergeService;
import io.blackbird.aemconnector.core.services.VersioningService;
import io.blackbird.aemconnector.core.utils.CopyMergeUtils;
import io.blackbird.aemconnector.core.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_DATA;
import static java.util.Objects.requireNonNull;

@Slf4j
@Component(service = DitaCopyMergeService.class)
public class DitaCopyMergeServiceImpl implements DitaCopyMergeService {

    private static final Pattern XML_PATTERN = Pattern.compile("<\\?xml[^>]+\\?>");
    private static final Pattern DOCTYPE_PATTERN = Pattern.compile("<!DOCTYPE[^>]+>");
    private static final String DITA_GUID_PREFIX = "GUID-";
    private static final String DITA_BINARY_PATH = "/jcr:content/renditions/original/jcr:content";
    private static final String FM_UUID = "fmUuid";
    private static final String SLASH = "/";
    private static final String TEXT_KEY = "__text";
    private static final String UNDERSCORE = "_";
    private static final String LEFT_BRACKET = "<";
    private static final String RIGHT_BRACKET = ">";
    private static final String TAG_INDEX_REGEX = "_(\\d+)$";

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;
    @Reference
    private transient VersioningService versioningService;

    @Override
    public Resource copyAndMerge(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdResourceCopyMergeException {
        try (ResourceResolver resolver = serviceUserResolverProvider.getTranslationWriterResolver()) {
            Resource sourceResource = handleSourceResource(resolver, sourcePath);
            String targetContentString = getTargetContent(resolver, sourcePath, targetContent);
            return handleCopyAndMerge(resolver, sourceResource, targetPath, targetContentString, references);
        } catch (Exception ex) {
            log.error("Failure in copyAndMerge: {}", ex.getMessage(), ex);
            throw new BlackbirdResourceCopyMergeException(ex.getMessage(), ex);
        }
    }

    @Override
    public Resource copyAndMerge(String sourcePath, String targetPath, String targetContent) throws BlackbirdResourceCopyMergeException {
        try (ResourceResolver resolver = serviceUserResolverProvider.getTranslationWriterResolver()) {
            Resource sourceResource = handleSourceResource(resolver, sourcePath);
            return handleCopyAndMerge(resolver, sourceResource, targetPath, targetContent, null);
        } catch (Exception ex) {
            log.error("Failure in copyAndMerge: {}", ex.getMessage(), ex);
            throw new BlackbirdResourceCopyMergeException(ex.getMessage(), ex);
        }
    }

    private Resource handleCopyAndMerge(ResourceResolver resolver, Resource sourceResource, String targetPath, String targetContent, JsonNode references)
            throws BlackbirdResourceCopyMergeException, PersistenceException, RepositoryException, WCMException {
        handleTargetResource(sourceResource, targetPath, resolver);

        Resource updatedTargetResource = resolver.getResource(targetPath);

        if (updatedTargetResource != null) {
            updateJcrDataBinaryProperty(targetPath, resolver, targetContent);
        }

        if (references != null && references.isArray() && updatedTargetResource != null) {
            CopyMergeUtils.updateResourceReferences(updatedTargetResource, references);
        }

        resolver.commit();

        Resource nonNullableTargetResource = requireNonNull(updatedTargetResource, String.format("Target resource does not exist, %s", targetPath));

        synchronizeVersion(sourceResource.getPath(), nonNullableTargetResource.getPath());

        return nonNullableTargetResource;
    }

    private void synchronizeVersion(String sourcePath, String targetPath) {
        VersionSyncResult versionSyncResult = versioningService.synchronizeVersion(sourcePath, targetPath);
        log.info("Result of version synchronization for source {}, target {}, {} ", sourcePath, targetPath, versionSyncResult);
    }

    private void handleTargetResource(Resource sourceResource, String targetPath, ResourceResolver resolver) throws BlackbirdResourceCopyMergeException, PersistenceException, WCMException, RepositoryException {
        /*Resource sourceResource = requireNonNull(
                resolver.getResource(sourcePath),
                String.format("Source resource does not exist, %s", sourcePath)
        );*/

        Resource targetResource = resolver.getResource(targetPath);

        if (targetResource == null) {
            createCopyForTargetResource(targetPath, sourceResource, resolver, generateNewUuid());
            return;
        }
        int versionCountDifference = versioningService.getVersionCountDifference(
                sourceResource.getPath(), targetResource.getPath()
        );

        if (versionCountDifference < 0) {
            recreateTargetResource(sourceResource, targetResource, resolver);
            return;
        }

        Resource targetJcrContent = requireNonNull(
                targetResource.getChild(JCR_CONTENT),
                String.format("Target jcr:content resource does not exist, %s", targetPath)
        );
        String currentUuid = targetJcrContent.getValueMap().get(FM_UUID, String.class);
        replaceExistingResourceWithNewCopy(sourceResource, targetResource, resolver, currentUuid);
    }

    private Resource handleSourceResource(ResourceResolver resolver, String sourcePath) {
        return requireNonNull(resolver.getResource(sourcePath),
                String.format("Source resource does not exist, %s", sourcePath)
        );
    }

    private void recreateTargetResource(Resource source, Resource target, ResourceResolver resolver) throws PersistenceException, BlackbirdResourceCopyMergeException, WCMException {
        String targetPath = target.getPath();
        resolver.delete(target);
        createCopyForTargetResource(targetPath, source, resolver, generateNewUuid());
    }

    private String generateNewUuid() {
        return DITA_GUID_PREFIX.concat(UUID.randomUUID().toString());
    }

    private void updateJcrDataBinaryProperty(String targetPath, ResourceResolver resolver, String targetContentString) {
        Resource targetJcrData = requireNonNull(resolver.getResource(targetPath.concat(DITA_BINARY_PATH)),
                String.format("Target resource does not exist, %s", targetPath));
        Session session = requireNonNull(resolver.adaptTo(Session.class), "Can not adapt resourceResolver to session");
        try (InputStream targetStream = new ByteArrayInputStream(targetContentString.getBytes())) {
            Binary contentValue = session.getValueFactory().createBinary(targetStream);
            Node targetJcrDataNode = requireNonNull(targetJcrData.adaptTo(Node.class),
                    String.format("Can not adapt target resource to node, %s", targetJcrData.getPath()));
            targetJcrDataNode.setProperty(JCR_DATA, contentValue);
            session.save();
            contentValue.dispose();
        } catch (Exception ex) {
            log.error("Error while setting binary property: {}", ex.getMessage(), ex);
        }
    }

    private String getTargetContent(ResourceResolver resolver, String sourcePath, JsonNode targetContent) throws RepositoryException, IOException {
        Binary binary = requireNonNull(getSourceBinary(resolver, sourcePath),
                String.format("Can not get binary data from sourcePath %s", sourcePath));

        String binaryDataString;
        try (InputStream inputStream = binary.getStream()) {
            binaryDataString = requireNonNull(convertSourceBinaryToString(inputStream),
                    String.format("Can not convert binary data to string from sourcePath %s", sourcePath));
        }

        StringBuilder targetXmlContent = new StringBuilder();
        Matcher xmlMatcher = XML_PATTERN.matcher(binaryDataString);
        if (xmlMatcher.find()) {
            String xmlDeclaration = xmlMatcher.group();
            targetXmlContent.append(xmlDeclaration).append(System.lineSeparator());
        }

        Matcher doctypeMatcher = DOCTYPE_PATTERN.matcher(binaryDataString);
        if (doctypeMatcher.find()) {
            String doctypeDeclaration = doctypeMatcher.group();
            targetXmlContent.append(doctypeDeclaration).append(System.lineSeparator());
        }

        targetContent.fields().forEachRemaining(entry -> {
            targetXmlContent.append(buildXmlFromJson(entry.getKey().replaceAll(TAG_INDEX_REGEX, StringUtils.EMPTY), entry.getValue()));
        });
        return targetXmlContent.toString();
    }

    private String buildXmlFromJson(String tag, JsonNode node) {
        StringBuilder xmlContent = new StringBuilder();
        if (node.isArray()) {
            for (JsonNode item : node) {
                xmlContent.append(buildXmlFromJson(tag, item));
            }
            return xmlContent.toString();
        }
        xmlContent.append(LEFT_BRACKET).append(tag);
        proceedAttributes(node, xmlContent);
        xmlContent.append(RIGHT_BRACKET);
        proceedTagsContent(node, xmlContent);
        xmlContent.append(LEFT_BRACKET).append(SLASH).append(tag).append(RIGHT_BRACKET);
        return xmlContent.toString();
    }

    private void proceedTagsContent(JsonNode node, StringBuilder xmlContent) {
        if (node.has(TEXT_KEY)) {
            String tagContent = StringEscapeUtils.escapeXml11(node.get(TEXT_KEY).asText());
            Set<String> substitutedKeys = new HashSet<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String keyWithIndex = entry.getKey();
                String key = keyWithIndex.replaceAll(TAG_INDEX_REGEX, StringUtils.EMPTY);
                if (!TEXT_KEY.equals(keyWithIndex) && !keyWithIndex.startsWith(UNDERSCORE)) {
                    String placeholder = String.format("%%%s%%", keyWithIndex);
                    if (tagContent.contains(placeholder)) {
                        tagContent = tagContent.replace(placeholder, buildXmlFromJson(key, entry.getValue()));
                        substitutedKeys.add(keyWithIndex);
                    }
                }
            }
            xmlContent.append(tagContent);
            node.fields().forEachRemaining(entry -> {
                String keyWithIndex = entry.getKey();
                String key = keyWithIndex.replaceAll(TAG_INDEX_REGEX, StringUtils.EMPTY);
                if (!TEXT_KEY.equals(keyWithIndex) && !keyWithIndex.startsWith(UNDERSCORE) && !substitutedKeys.contains(keyWithIndex)) {
                    xmlContent.append(buildXmlFromJson(key, entry.getValue()));
                }
            });
        } else {
            node.fields().forEachRemaining(entry -> {
                String keyWithIndex = entry.getKey();
                String key = keyWithIndex.replaceAll(TAG_INDEX_REGEX, StringUtils.EMPTY);
                if (!TEXT_KEY.equals(keyWithIndex) && !keyWithIndex.startsWith(UNDERSCORE)) {
                    xmlContent.append(buildXmlFromJson(key, entry.getValue()));
                }
            });
        }
    }

    private void proceedAttributes(JsonNode node, StringBuilder xmlContent) {
        node.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            if (key.startsWith(UNDERSCORE) && !key.equals(TEXT_KEY)) {
                xmlContent.append(StringUtils.SPACE).append(key.substring(1))
                        .append("=\"").append(StringEscapeUtils.escapeXml11(entry.getValue().asText())).append("\"");
            }
        });
    }

    private Binary getSourceBinary(ResourceResolver resolver, String sourcePath) {
        return Optional.ofNullable(resolver.getResource(sourcePath.concat(DITA_BINARY_PATH)))
                .map(resource -> resource.adaptTo(Node.class))
                .map(node -> {
                    try {
                        return node.getProperty(JCR_DATA).getBinary();
                    } catch (RepositoryException ex) {
                        return null;
                    }
                })
                .orElse(null);
    }

    private String convertSourceBinaryToString(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException ex) {
            log.error("IOException while reading binary to string: {}", ex.getMessage(), ex);
            return null;
        }
    }

    private void createCopyForTargetResource(String targetPath, Resource sourceResource, ResourceResolver resolver, String uuid) throws BlackbirdResourceCopyMergeException, PersistenceException, WCMException {
        CopyMergeUtils.createResourcesHierarchicallyIfNotExist(PathUtils.getParent(targetPath), PathUtils.getParent(sourceResource.getPath()), resolver);
        AssetManager assetManager = requireNonNull(resolver.adaptTo(AssetManager.class), "Cannot adapt resourceResolver to assetManager");
        assetManager.copyAsset(sourceResource.getPath(), targetPath);
        updateUuid(resolver, targetPath, uuid);
    }

    private void replaceExistingResourceWithNewCopy(Resource sourceResource, Resource targetResource, ResourceResolver resolver, String uuid) throws PersistenceException, RepositoryException {
        String targetResourcePath = targetResource.getPath();
        Resource targetJcrContent = requireNonNull(targetResource.getChild(JCR_CONTENT),
                String.format("Target jcr:content resource does not exist, %s", targetResourcePath));
        Resource sourceJcrContent = requireNonNull(sourceResource.getChild(JCR_CONTENT),
                String.format("Source jcr:content resource does not exist, %s", sourceResource.getPath()));
        resolver.delete(targetJcrContent);
        Node sourceJcrContentNode = requireNonNull(sourceJcrContent.adaptTo(Node.class), String.format("Can not adapt resource %s to node", sourceJcrContent.getPath()));
        Node targetNode = requireNonNull(targetResource.adaptTo(Node.class), String.format("Can not adapt resource %s to node", targetResource.getPath()));
        JcrUtil.copy(sourceJcrContentNode, targetNode, JCR_CONTENT);
        updateUuid(resolver, targetResourcePath, uuid);
    }

    private void updateUuid(ResourceResolver resolver, String targetPath, String uuid) {
        Resource targetJcrContent = requireNonNull(resolver.getResource(String.join(SLASH, targetPath, JCR_CONTENT)),
                String.format("Target jcr:content resource does not exist, %s", targetPath));
        ModifiableValueMap properties = requireNonNull(targetJcrContent.adaptTo(ModifiableValueMap.class),
                String.format("Can not adapt resource %s to modifiableValueMap", targetJcrContent));
        properties.put(FM_UUID, uuid);
    }
}