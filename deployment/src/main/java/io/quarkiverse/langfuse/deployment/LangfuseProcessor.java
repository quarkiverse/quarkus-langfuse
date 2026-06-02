package io.quarkiverse.langfuse.deployment;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.jandex.ClassType;

import com.langfuse.api.spi.LangfuseApiBuilderFactory;

import io.quarkiverse.langfuse.client.QuarkusLangfuseApiBuilderFactory;
import io.quarkiverse.langfuse.deployment.config.LangfuseBuildTimeConfig;
import io.quarkiverse.langfuse.runtime.LangfuseRecorder;
import io.quarkiverse.langfuse.runtime.langchain4j.LangfuseLangchain4jConfigBuilder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigBuilderBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;

class LangfuseProcessor {
    private static final String FEATURE = "langfuse";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void generateLangfuseBeans(LangfuseRecorder recorder, BuildProducer<SyntheticBeanBuildItem> beanProducer) {
        beanProducer.produce(
                SyntheticBeanBuildItem
                        .configure(DotNames.QUARKUS_LANGFUSE_CLIENT)
                        .setRuntimeInit()
                        .defaultBean()
                        .scope(ApplicationScoped.class)
                        .supplier(recorder.quarkusLangfuseClient())
                        .done());

        beanProducer.produce(
                SyntheticBeanBuildItem
                        .configure(DotNames.QUARKUS_LANGFUSE_ASYNC_CLIENT)
                        .setRuntimeInit()
                        .defaultBean()
                        .scope(ApplicationScoped.class)
                        .supplier(recorder.quarkusLangfuseAsyncClient())
                        .done());

        beanProducer.produce(
                SyntheticBeanBuildItem
                        .configure(DotNames.LANGFUSE_API)
                        .setRuntimeInit()
                        .defaultBean()
                        .scope(ApplicationScoped.class)
                        .createWith(recorder.langfuseApi())
                        .addInjectionPoint(ClassType.create(DotNames.QUARKUS_LANGFUSE_CLIENT))
                        .addInjectionPoint(ClassType.create(DotNames.QUARKUS_LANGFUSE_ASYNC_CLIENT))
                        .done());
    }

    @BuildStep
    ServiceProviderBuildItem nativeImageServiceProviderRegistration() {
        return new ServiceProviderBuildItem(LangfuseApiBuilderFactory.class.getName(),
                QuarkusLangfuseApiBuilderFactory.class.getName());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerOtelCapabilities(Capabilities capabilities, LangfuseBuildTimeConfig buildTimeConfig,
            LangfuseRecorder recorder,
            BuildProducer<SyntheticBeanBuildItem> beanProducer) {

        if (isOtelAvailableAndEnabled(capabilities, buildTimeConfig)) {
            beanProducer.produce(
                    SyntheticBeanBuildItem
                            .configure(DotNames.LANGFUSE_SPAN_PROCESSOR)
                            .addType(DotNames.SPAN_PROCESSOR)
                            .setRuntimeInit()
                            .scope(ApplicationScoped.class)
                            .supplier(recorder.langfuseSpanProcessor())
                            .unremovable()
                            .done());
        }
    }

    @BuildStep
    void registerLangChain4jCapabilities(Capabilities capabilities,
            CombinedIndexBuildItem indexBuildItem,
            LangfuseBuildTimeConfig buildConfig,
            BuildProducer<RunTimeConfigBuilderBuildItem> runTimeConfigBuilder) {

        if (isOtelAvailableAndEnabled(capabilities, buildConfig) && isQuarkusLangchain4jAvailable(indexBuildItem)) {
            runTimeConfigBuilder.produce(new RunTimeConfigBuilderBuildItem(LangfuseLangchain4jConfigBuilder.class));
        }
    }

    @BuildStep
    void indexLangfuseClasses(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(List.of(
                new IndexDependencyBuildItem("com.langfuse", "langfuse-java-api")));
    }

    private static boolean isQuarkusLangchain4jAvailable(CombinedIndexBuildItem indexBuildItem) {
        return indexBuildItem.getIndex().getClassByName(DotNames.REGISTER_AI_SERVICE) != null;
    }

    private static boolean isOtelAvailableAndEnabled(Capabilities capabilities, LangfuseBuildTimeConfig buildTimeConfig) {
        return capabilities.isPresent(Capability.OPENTELEMETRY_TRACER) && buildTimeConfig.otel().enabled();
    }
}
