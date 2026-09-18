package io.quarkiverse.langfuse.deployment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CommentObjectType;
import com.langfuse.api.model.CreateCommentRequest;

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

class CommentOperationsTests extends CommentOperationsTestSupport {

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

    @BeforeEach
    void beforeEach() {
        resetAndGetWiremock();
    }

    // --- lookup ----------------------------------------------------------------------------

    @Test
    void findByIdResolvesInASingleRequestWithoutScanning() {
        stubComments(7, 3);
        stubCommentFound("comment-2");

        assertThat(langfuse.comments().findById("comment-2"))
                .isPresent()
                .get()
                .extracting(Comment::getId)
                .isEqualTo("comment-2");

        verifyCommentGetRequests(1, "comment-2");
        verifyListRequests(0);
    }

    @Test
    void findByIdTreatsNotFoundAsAbsence() {
        stubCommentFailure("nope", 404);

        assertThat(langfuse.comments().findById("nope")).isEmpty();
    }

    /**
     * The absence-versus-failure rule: only a 404 is recovered, so a rejected credential must escape
     * rather than read as "no comment with that id".
     */
    @Test
    void findByIdNeverMistakesARejectedCredentialForAbsence() {
        stubCommentFailure("comment-1", 401);

        assertThatThrownBy(() -> langfuse.comments().findById("comment-1"))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void findByIdNeverMistakesARefusedActionForAbsence() {
        stubCommentFailure("comment-1", 403);

        assertThatThrownBy(() -> langfuse.comments().findById("comment-1"))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    @Test
    void blankIdsAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.comments().findById("  "))
                .withMessageContaining("Comment id");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.comments().findById(null));

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubComments(7, 3);

        assertThat(langfuse.comments().findAll())
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

        assertThat(langfuse.comments().find(PageSelection.rangeClosed(2, 3, 3)))
                .extracting(Comment::getId)
                .containsExactly("comment-4", "comment-5", "comment-6", "comment-7", "comment-8", "comment-9");

        verifyListRequests(2);
        verifyPageRequested(1, 2);
        verifyPageRequested(1, 3);
        verifyPageRequested(0, 1);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubComments(7, 3);

        var stream = langfuse.comments().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubComments(7, 3);

        assertThat(langfuse.comments().findPage(Page.of(2, 3)))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> langfuse.comments().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- filtering -------------------------------------------------------------------------

    /**
     * A filtered view pages exactly like the unfiltered one, and carries its criteria on every request
     * of the walk rather than only the first.
     */
    @Test
    void filteredViewsSendTheirCriteriaOnEveryPageOfTheWalk() {
        stubComments(7, 3);

        assertThat(langfuse.comments().matching(traceFilter()).findAll())
                .hasSize(7)
                .extracting(Comment::getId)
                .startsWith("comment-1")
                .endsWith("comment-7");

        verifyListRequests(3);
        verifyFilteredListRequests(3, "TRACE", "trace-1", "user-1");
    }

    /**
     * {@code matching} replaces rather than composes, so only the second filter's criteria are sent.
     */
    @Test
    void matchingReplacesTheFilterRatherThanCombiningIt() {
        stubComments(2, 3);

        var filter = CommentFilter.builder()
                .objectType(CommentObjectType.SESSION)
                .build();

        assertThat(langfuse.comments().matching(traceFilter()).matching(filter).findAll()).hasSize(2);

        verifyListRequests(1);
        verifyObjectTypeRequested(1, "SESSION");
        verifyObjectTypeRequested(0, "TRACE");
    }

    @Test
    void matchingLeavesTheReceivingViewUnfiltered() {
        stubComments(2, 3);

        var comments = langfuse.comments();
        comments.matching(traceFilter());

        assertThat(comments.findAll()).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    @Test
    void matchingRejectsANullFilterBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.comments().matching(null))
                .withMessageContaining("Filter");

        verifyListRequests(0);
    }

    @Test
    void anEmptyFilterSendsNoCriteria() {
        stubComments(2, 3);

        assertThat(langfuse.comments().matching(CommentFilter.none()).findAll()).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    @Test
    void findByIdOnAFilteredViewIsNotScopedByTheFilter() {
        stubCommentFound("comment-2");

        assertThat(langfuse.comments().matching(traceFilter()).findById("comment-2"))
                .isPresent()
                .get()
                .extracting(Comment::getId)
                .isEqualTo("comment-2");

        verifyCommentGetRequests(1, "comment-2");
        verifyListRequests(0);
    }

    // --- writes ----------------------------------------------------------------------------

    /**
     * Langfuse answers a comment creation with the id alone, and this layer returns exactly that - no
     * hidden follow-up lookup to synthesise a {@code Comment}.
     */
    @Test
    void createReturnsTheNewIdWithoutASecondRequest() {
        stubCommentCreated("comment-new");

        assertThat(langfuse.comments().create(createRequest())).isEqualTo("comment-new");

        verifyCommentsCreated(1);
        verifyCommentGetRequests(0, "comment-new");
    }

    @Test
    void createRejectsANullRequestBeforeAnyCall() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.comments().create(null));

        verifyCommentsCreated(0);
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
}
