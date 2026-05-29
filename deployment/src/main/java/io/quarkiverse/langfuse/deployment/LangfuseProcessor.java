package io.quarkiverse.langfuse.deployment;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;

import io.quarkiverse.langfuse.LangfuseClient;
import io.quarkiverse.langfuse.client.QuarkusLangfuseClient;
import io.quarkiverse.langfuse.runtime.LangfuseRecorder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;

class LangfuseProcessor {
    private static final String FEATURE = "langfuse";
    private static final DotName QUARKUS_LANGFUSE_CLIENT = DotName.createSimple(QuarkusLangfuseClient.class);
    private static final DotName LANGFUSE_CLIENT = DotName.createSimple(LangfuseClient.class);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void generateLangfuseBeans(LangfuseRecorder recorder, BuildProducer<SyntheticBeanBuildItem> beanProducer) {
        beanProducer.produce(
                SyntheticBeanBuildItem
                        .configure(QUARKUS_LANGFUSE_CLIENT)
                        .setRuntimeInit()
                        .defaultBean()
                        .scope(ApplicationScoped.class)
                        .supplier(recorder.quarkusLangfuseClient())
                        .done());

        beanProducer.produce(
                SyntheticBeanBuildItem
                        .configure(LANGFUSE_CLIENT)
                        .setRuntimeInit()
                        .defaultBean()
                        .scope(ApplicationScoped.class)
                        .createWith(recorder.langfuseClient())
                        .addInjectionPoint(ClassType.create(QUARKUS_LANGFUSE_CLIENT))
                        .done());
    }

    @BuildStep
    void indexLangfuseClasses(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(List.of(
                new IndexDependencyBuildItem("com.langfuse", "langfuse-java-api")));
    }
}
