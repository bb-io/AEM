package io.blackbird.aemconnector.core.services.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.fasterxml.jackson.databind.JsonNode;
import io.blackbird.aemconnector.core.exceptions.BlackbirdPageCopyMergeException;
import io.blackbird.aemconnector.core.services.BlackbirdPageCopyMergeService;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import io.blackbird.aemconnector.core.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component(service = BlackbirdPageCopyMergeService.class)
public class BlackbirdPageCopyMergeServiceImpl implements BlackbirdPageCopyMergeService {

    @Reference
    private BlackbirdServiceUserResolverProvider serviceUserResolverProvider;

    @Override
    public Page copyAndMerge(String sourcePath, String targetPath, JsonNode targetContent) throws BlackbirdPageCopyMergeException, LoginException {

        try (ResourceResolver resolver = serviceUserResolverProvider.getTranslationWriterResolver()) {
            PageManager pageManager = requireNonNull(resolver.adaptTo(PageManager.class), "Cannot adapt to PageManager");

            Page sourcePage = requireNonNull(pageManager.getPage(sourcePath),
                    String.format("Source Page does not exist, %s", sourcePath));

            Page targetPage = pageManager.getPage(targetPath);

            if (targetPage == null) {
                createCopyForTargetPage(targetPath, sourcePage, pageManager);
            } else {
                replaceExistingPageWithNewCopy(sourcePage, targetPage, resolver);
            }

            mergeJsonIntoPage(resolver.getResource(targetPath), targetContent);
            resolver.commit();
            return pageManager.getPage(targetPath);

        } catch (Exception e) {
            log.error("Failure in copyAndMerge: {}", e.getMessage(), e);
            throw new BlackbirdPageCopyMergeException(e.getMessage(), e);
        }
    }

    private void replaceExistingPageWithNewCopy(Page sourcePage, Page targetPage, ResourceResolver resolver) throws PersistenceException {
        resolver.delete(targetPage.getContentResource());
        resolver.copy(sourcePage.getContentResource().getPath(), targetPage.getPath());
        resolver.commit();
    }

    private void createCopyForTargetPage(String targetPath, Page sourcePage, PageManager pageManager) throws WCMException, BlackbirdPageCopyMergeException {
        createPagesHierarchicallyIfNotExist(PathUtils.getParent(targetPath), pageManager);
        PageManager.CopyOptions options = new PageManager.CopyOptions();

        options.page = sourcePage;
        options.destination = targetPath;
        options.shallow = true;
        options.resolveConflict = true;
        options.overridePage = true;
        options.autoSave = true;

        pageManager.copy(options);
    }

    private void mergeJsonIntoPage(Resource resource, JsonNode jsonNode) {
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
                mergeJsonIntoPage(child, value);
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

    private void createPagesHierarchicallyIfNotExist(String pagePath, PageManager pageManager) throws WCMException, BlackbirdPageCopyMergeException {
        String path = pagePath;
        Page page = pageManager.getPage(path);
        Deque<String> nonExistentPages = new ArrayDeque<>();

        while (page == null && PathUtils.getName(path) != null) {
            nonExistentPages.push(PathUtils.getName(path));
            path = PathUtils.getParent(path);
            page = pageManager.getPage(path);
        }
        if (page == null) {
            throw new BlackbirdPageCopyMergeException(String.format("root page doesn't exist for path: %s", pagePath));
        }
        for (String pageName : nonExistentPages) {
            page = pageManager.create(page.getPath(), pageName, StringUtils.EMPTY, pageName, true);
        }
    }
}
