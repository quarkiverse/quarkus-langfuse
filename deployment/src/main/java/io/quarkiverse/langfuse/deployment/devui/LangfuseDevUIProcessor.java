package io.quarkiverse.langfuse.deployment.devui;

import java.util.Map;

import org.jboss.logging.Logger;

import io.quarkiverse.langfuse.deployment.devservices.LangfuseDevServicesProcessor;
import io.quarkiverse.langfuse.deployment.devservices.QuarkusLangfuseContainer;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

public class LangfuseDevUIProcessor {
    private static final Logger LOG = Logger.getLogger(LangfuseDevUIProcessor.class);

    @BuildStep(onlyIf = IsLocalDevelopment.class)
    CardPageBuildItem devuiCard() {
        LOG.debug("Inside DoclingDevUIProcessor.devuiCard");

        var card = new CardPageBuildItem();

        card.addPage(
                Page.externalPageBuilder("Langfuse UI")
                        .dynamicUrlJsonRPCMethodName(
                                "devui-dev-services:devServicesConfig",
                                Map.of(
                                        "name", LangfuseDevServicesProcessor.FEATURE,
                                        "configKey", QuarkusLangfuseContainer.CONFIG_LANGFUSE_UI))
                        .isHtmlContent()
                        .doNotEmbed());

        return card;
    }
}
