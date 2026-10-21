package io.quarkiverse.langfuse.it;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test field that receives the WireMock server started by {@link LangfuseStubResource}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface InjectLangfuseStub {
}
