package io.quarkiverse.langfuse.deployment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CommentObjectType;
import com.langfuse.api.model.CreateCommentRequest;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.CommentFilter;
import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

class AsyncCommentOperationsTests extends CommentOperationsTestSupport {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.api.default-page-size", "3")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig());

    @Inject
    LangfuseOperations langfuse;

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    @BeforeEach
    void beforeEach() {
        resetAndGetWiremock();
    }

    // --- lookup ----------------------------------------------------------------------------

    @Test
    void findByIdResolvesInASingleRequestWithoutScanning() {
        stubComments(7, 3);
        stubCommentFound("comment-2");

        assertThat(await(asyncLangfuse.comments().findById("comment-2")))
                .isNotNull()
                .extracting(Comment::getId)
                .isEqualTo("comment-2");

        verifyCommentGetRequests(1, "comment-2");
        verifyListRequests(0);
    }

    @Test
    void findByIdEmitsNullWhenAbsent() {
        stubCommentFailure("nope", 404);

        assertThat(await(asyncLangfuse.comments().findById("nope"))).isNull();
    }

    /**
     * The asynchronous mirror of the absence-versus-failure rule: only a 404 is recovered, so a 401
     * fails the {@link Uni} rather than emitting {@code null}.
     */
    @Test
    void findByIdNeverMistakesARejectedCredentialForAbsence() {
        stubCommentFailure("comment-1", 401);

        assertThatThrownBy(() -> await(asyncLangfuse.comments().findById("comment-1")))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void findByIdNeverMistakesARefusedActionForAbsence() {
        stubCommentFailure("comment-1", 403);

        assertThatThrownBy(() -> await(asyncLangfuse.comments().findById("comment-1")))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    /**
     * Validation runs outside the deferred supplier, so blank input is thrown from the call rather than
     * emitted as a failure at subscription time.
     */
    @Test
    void blankIdsAreRejectedBeforeTheUniIsEvenBuilt() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.comments().findById("  "))
                .withMessageContaining("Comment id");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.comments().findById(null));

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubComments(7, 3);

        assertThat(await(asyncLangfuse.comments().findAll()))
                .hasSize(7)
                .extracting(Comment::getId)
                .startsWith("comment-1")
                .endsWith("comment-7");

        verifyListRequests(3);
        verifyUnfilteredListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubComments(20, 3);

        assertThat(await(asyncLangfuse.comments().find(PageSelection.rangeClosed(2, 3, 3))))
                .extracting(Comment::getId)
                .containsExactly("comment-4", "comment-5", "comment-6", "comment-7", "comment-8", "comment-9");

        verifyListRequests(2);
        verifyPageRequested(1, 2);
        verifyPageRequested(1, 3);
        verifyPageRequested(0, 1);
    }

    @Test
    void multisAreLazyUntilSubscribed() {
        stubComments(7, 3);

        var multi = asyncLangfuse.comments().streamAll();

        verifyListRequests(0);

        assertThat(collect(multi.select().first(4))).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubComments(7, 3);

        assertThat(await(asyncLangfuse.comments().findPage(Page.of(2, 3))))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> await(asyncLangfuse.comments().findAll()))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- filtering -------------------------------------------------------------------------

    @Test
    void filteredViewsSendTheirCriteriaOnEveryPageOfTheWalk() {
        stubComments(7, 3);

        assertThat(await(asyncLangfuse.comments().matching(traceFilter()).findAll()))
                .hasSize(7)
                .extracting(Comment::getId)
                .startsWith("comment-1")
                .endsWith("comment-7");

        verifyListRequests(3);
        verifyFilteredListRequests(3, "TRACE", "trace-1", "user-1");
    }

    @Test
    void matchingReplacesTheFilterRatherThanCombiningIt() {
        stubComments(2, 3);

        var filter = CommentFilter.builder()
                .objectType(CommentObjectType.SESSION)
                .build();

        assertThat(await(asyncLangfuse.comments().matching(traceFilter()).matching(filter).findAll())).hasSize(2);

        verifyListRequests(1);
        verifyObjectTypeRequested(1, "SESSION");
        verifyObjectTypeRequested(0, "TRACE");
    }

    @Test
    void matchingLeavesTheReceivingViewUnfiltered() {
        stubComments(2, 3);

        var comments = asyncLangfuse.comments();
        comments.matching(traceFilter());

        assertThat(await(comments.findAll())).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    @Test
    void matchingRejectsANullFilterBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.comments().matching(null))
                .withMessageContaining("Filter");

        verifyListRequests(0);
    }

    @Test
    void anEmptyFilterSendsNoCriteria() {
        stubComments(2, 3);

        assertThat(await(asyncLangfuse.comments().matching(CommentFilter.none()).findAll())).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createEmitsTheNewIdWithoutASecondRequest() {
        stubCommentCreated("comment-new");

        assertThat(await(asyncLangfuse.comments().create(createRequest()))).isEqualTo("comment-new");

        verifyCommentsCreated(1);
        verifyCommentGetRequests(0, "comment-new");
    }

    @Test
    void createRejectsANullRequestBeforeTheUniIsEvenBuilt() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.comments().create(null));

        verifyCommentsCreated(0);
    }

    // --- parity ----------------------------------------------------------------------------

    /**
     * The two trees are independent implementations, so they are checked against each other.
     */
    @Test
    void bothTreesReturnTheSameCommentsForTheSameRequests() {
        stubComments(7, 3);

        var sync = langfuse.comments().findAll();

        resetRequests();

        assertThat(await(asyncLangfuse.comments().findAll()))
                .extracting(Comment::getId)
                .isEqualTo(sync.stream().map(Comment::getId).toList());

        verifyListRequests(3);
    }

    /**
     * The filtered view is proved across both trees too, since each builds its own request.
     */
    @Test
    void bothTreesSendTheSameFilterForTheSameCriteria() {
        stubComments(2, 3);

        langfuse.comments().matching(traceFilter()).findAll();

        resetRequests();

        assertThat(await(asyncLangfuse.comments().matching(traceFilter()).findAll())).hasSize(2);

        verifyFilteredListRequests(1, "TRACE", "trace-1", "user-1");
    }

    private static CommentFilter traceFilter() {
        return CommentFilter.builder()
                .objectType(CommentObjectType.TRACE)
                .objectId("trace-1")
                .authorUserId("user-1")
                .build();
    }

    private static CreateCommentRequest createRequest() {
        return CreateCommentRequest.builder()
                .projectId("project-1")
                .objectType("TRACE")
                .objectId("trace-1")
                .content("looks good")
                .build();
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }

    private static <T> List<T> collect(Multi<T> multi) {
        return multi.collect().asList().await().atMost(TIMEOUT);
    }
}
