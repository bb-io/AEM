package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdResourceCopyMergeException;
import io.blackbird.aemconnector.core.services.BlackbirdPageCopyMergeService;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.utils.CopyMergeUtils;
import io.blackbird.aemconnector.core.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static java.util.Objects.requireNonNull;

@Slf4j
@Component(service = BlackbirdPageCopyMergeService.class)
public class BlackbirdPageCopyMergeServiceImpl implements BlackbirdPageCopyMergeService {

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Override
    public Page copyAndMerge(String sourcePath, String targetPath, JsonNode targetContent, JsonNode references) throws BlackbirdResourceCopyMergeException {

        try (ResourceResolver resolver = serviceUserResolverProvider.getTranslationWriterResolver()) {
            PageManager pageManager = requireNonNull(resolver.adaptTo(PageManager.class), "Cannot adapt to PageManager");

            Page sourcePage = requireNonNull(pageManager.getPage(sourcePath),
                    String.format("Source resource does not exist, %s", sourcePath));

            Page targetPage = pageManager.getPage(targetPath);

            if (targetPage == null) {
                createCopyForTargetPage(targetPath, sourcePage, pageManager, resolver);
            } else {
                replaceExistingPageWithNewCopy(sourcePage, targetPage, resolver);
            }

            Resource targetResource = resolver.getResource(targetPath);
            if (targetResource != null) {
                CopyMergeUtils.mergeJsonIntoResource(targetResource, targetContent);
            }

            if (references != null && references.isArray() && targetResource != null) {
                CopyMergeUtils.updateResourceReferences(targetResource, references);
            }
            resolver.commit();
            return pageManager.getPage(targetPath);

        } catch (Exception e) {
            log.error("Failure in copyAndMerge: {}", e.getMessage(), e);
            throw new BlackbirdResourceCopyMergeException(e.getMessage(), e);
        }
    }

    private void replaceExistingPageWithNewCopy(Page sourcePage, Page targetPage, ResourceResolver resolver) throws PersistenceException, RepositoryException {
        Resource targetJcrContent = requireNonNull(targetPage.getContentResource(),
                String.format("Target jcr:content resource does not exist, %s", targetPage.getPath()));
        Resource sourceJcrContent = requireNonNull(sourcePage.getContentResource(),
                String.format("Source jcr:content resource does not exist, %s", sourcePage.getPath()));
        resolver.delete(targetJcrContent);
        Node sourceJcrContentNode = requireNonNull(sourceJcrContent.adaptTo(Node.class), String.format("Can not adapt resource %s to node.", sourceJcrContent.getPath()));
        Node targetNode = requireNonNull(targetPage.adaptTo(Node.class), String.format("Can not adapt resource %s to node.", targetPage.getPath()));
        JcrUtil.copy(sourceJcrContentNode, targetNode, JCR_CONTENT);
    }

    private void createCopyForTargetPage(String targetPath, Page sourcePage, PageManager pageManager, ResourceResolver resolver) throws BlackbirdResourceCopyMergeException, PersistenceException, WCMException {
        CopyMergeUtils.createResourcesHierarchicallyIfNotExist(PathUtils.getParent(targetPath), PathUtils.getParent(sourcePage.getPath()), resolver);
        PageManager.CopyOptions options = new PageManager.CopyOptions();

        options.page = sourcePage;
        options.destination = targetPath;
        options.shallow = true;
        options.resolveConflict = true;
        options.autoSave = true;

        pageManager.copy(options);
    }
}
