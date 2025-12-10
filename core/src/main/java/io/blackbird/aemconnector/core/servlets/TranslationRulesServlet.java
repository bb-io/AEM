package io.blackbird.aemconnector.core.servlets;

import io.blackbird.aemconnector.core.dto.TranslationRules;
import io.blackbird.aemconnector.core.exceptions.BlackbirdHttpErrorException;
import io.blackbird.aemconnector.core.services.TranslationRulesService;
import io.blackbird.aemconnector.core.servlets.internal.BlackbirdAbstractBaseServlet;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Optional;

import static io.blackbird.aemconnector.core.constants.ServletConstants.TRANSLATION_RULES_FILE_NOT_FOUND;

@Slf4j
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = TranslationRulesServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET
)
public class TranslationRulesServlet extends BlackbirdAbstractBaseServlet {

    public static final String RESOURCE_TYPE = "bb-aem-connector/services/translation-rules";

    @Reference
    private TranslationRulesService translationRulesService;

    @Override
    public Serializable buildResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        try {
            TranslationRules translationsRules = translationRulesService.getTranslationsRules();

            log.info(translationsRules.toString());

            return translationsRules;

        } catch (Exception e) {
            throw BlackbirdHttpErrorException.internalServerError(e.getMessage());
        }
    }

    @Override
    public InputStream buildXmlResponsePayload(SlingHttpServletRequest request, SlingHttpServletResponse response) throws BlackbirdHttpErrorException {
        Optional<InputStream> translationRulesFileInputStream = translationRulesService.getTranslationRulesFileInputStream();

        return translationRulesFileInputStream.orElseThrow(
                () -> BlackbirdHttpErrorException.notFound(TRANSLATION_RULES_FILE_NOT_FOUND)
        );
    }
}
