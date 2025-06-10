package io.blackbird.aemconnector.core.services.impl;

import com.adobe.granite.asset.api.AssetManager;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.api.constants.NameConstants;
import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.services.BlackbirdAssetCopyMergeService;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.utils.CopyMergeUtils;
import io.blackbird.aemconnector.core.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component(service = BlackbirdAssetCopyMergeService.class)
public class BlackbirdAssetCopyMergeServiceImpl implements BlackbirdAssetCopyMergeService {

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Override
    public Resource copyAndMerge(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdResourceCopyMergeException {
        try (ResourceResolver resolver = serviceUserResolverProvider.getTranslationWriterResolver()) {
            Resource sourceResource = requireNonNull(resolver.getResource(sourcePath),
                    String.format("Source resource does not exist, %s", sourcePath));
            Resource targetResource = resolver.getResource(targetPath);
            if (targetResource == null) {
                createCopyForTargetResource(targetPath, sourceResource, resolver);
            } else {
                replaceExistingResourceWithNewCopy(sourceResource, targetResource, resolver);
            }
            Resource updatedResource = resolver.getResource(targetPath);
            if (updatedResource != null) {
                CopyMergeUtils.mergeJsonIntoResource(updatedResource, targetContent);
            }
            if (references != null && references.isArray() && updatedResource != null) {
                CopyMergeUtils.updateResourceReferences(updatedResource, references);
            }
            resolver.commit();
            return requireNonNull(updatedResource, String.format("Target resource does not exist, %s", targetPath));
        } catch (Exception ex) {
            log.error("Failure in copyAndMerge: {}", ex.getMessage(), ex);
            throw new BlackbirdResourceCopyMergeException(ex.getMessage(), ex);
        }
    }

    private void createCopyForTargetResource(String targetPath, Resource sourceResource, ResourceResolver resolver) throws BlackbirdResourceCopyMergeException, PersistenceException, WCMException {
        CopyMergeUtils.createResourcesHierarchicallyIfNotExist(PathUtils.getParent(targetPath), PathUtils.getParent(sourceResource.getPath()), resolver);
        AssetManager assetManager = requireNonNull(resolver.adaptTo(AssetManager.class), "Cannot adapt ResourceResolver to AssetManager");
        assetManager.copyAsset(sourceResource.getPath(), targetPath);
        resolver.commit();
    }

    private void replaceExistingResourceWithNewCopy(Resource sourceResource, Resource targetResource, ResourceResolver resolver) throws PersistenceException {
        String targetResourcePath = targetResource.getPath();
        Resource targetJcrContent = requireNonNull(targetResource.getChild(NameConstants.NN_CONTENT),
                String.format("Target jcr:content resource does not exist, %s", targetResourcePath));
        Resource sourceJcrContent = requireNonNull(sourceResource.getChild(NameConstants.NN_CONTENT),
                String.format("Source jcr:content resource does not exist, %s", sourceResource.getPath()));
        resolver.delete(targetJcrContent);
        resolver.copy(sourceJcrContent.getPath(), targetResourcePath);
        resolver.commit();
    }
}
