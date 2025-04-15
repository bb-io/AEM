package io.blackbird.aemconnector.core.services.impl;

import io.blackbird.aemconnector.core.dto.BlackbirdJcrNode;
import io.blackbird.aemconnector.core.models.BlackbirdContentStructureModel;
import io.blackbird.aemconnector.core.services.BlackbirdContentStructureService;
import io.blackbird.aemconnector.core.services.BlackbirdServiceUserResolverProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;

@Slf4j
@Component(service = BlackbirdContentStructureService.class)
@Designate(ocd = BlackbirdContentStructureServiceImpl.Config.class)
public class BlackbirdContentStructureServiceImpl implements BlackbirdContentStructureService {

    @Reference
    private BlackbirdServiceUserResolverProvider resolverProvider;

    private Set<String> primaryTypeWhitelist;

    @ObjectClassDefinition(name = "Blackbird Content Structure Service Impl Config", description = "Configuration for filtering jcr:primaryTypes on content structure")
    public @interface Config {
        @AttributeDefinition(name = "WhitePropertyList", description = "List of allowed jcr:primaryType values")
        String[] whiteListPrimaryType() default {"cq:Page", "cq:PageContent", "nt:unstructured"};
    }

    @PostConstruct
    protected void activate(Config config) {
        primaryTypeWhitelist = new HashSet<>(Arrays.asList(config.whiteListPrimaryType()));
    }

    @Override
    public BlackbirdContentStructureModel getContentStructure(String path) {
        try (ResourceResolver resolver = resolverProvider.getContentStructureReaderResolver()) {
            Resource resource = resolver.getResource(path);
            log.info("Getting content for path {}", path);
            if (resource != null) {
                BlackbirdContentStructureModel contentStructureModel = resource.adaptTo(BlackbirdContentStructureModel.class);
                if (contentStructureModel != null) {
                    List<BlackbirdJcrNode> filtered = contentStructureModel.getContents().stream()
                            .filter(node -> node.getProperties().stream()
                                    .anyMatch(prop -> JCR_PRIMARYTYPE.equals(prop.getName()) &&
                                            prop.getValue() != null &&
                                            primaryTypeWhitelist.contains(prop.getValue()))
                            )
                            .collect(Collectors.toList());
                    contentStructureModel.setFilteredContents(filtered);
                    return contentStructureModel;
                }
                return null;
            }
        } catch (LoginException e) {
            log.error("Cannot access content structure reader", e);
        }
        return null;
    }
}
