package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.Optional;

import com.langfuse.api.model.UtilsMetaResponse;

/**
 * Maps a generated paginated response onto a {@link PagedResult}.
 *
 * <p>
 * Every page-addressed Langfuse list endpoint reports its pagination metadata as a
 * {@link UtilsMetaResponse}, so this mapping is shared by all of them. Missing metadata is treated
 * as "no further pages" rather than being guessed at.
 */
final class PagedResults {

    private PagedResults() {
    }

    // meta can be absent on some error/edge responses; treating that as "0 of 0" rather than throwing
    // or guessing means callers get an empty, well-formed PagedResult instead of a NullPointerException.
    static <T> PagedResult<T> from(Page page, List<T> data, UtilsMetaResponse meta) {
        var metadata = Optional.ofNullable(meta);

        return new DefaultPagedResult<>(
                data,
                page,
                metadata.map(UtilsMetaResponse::getTotalItems).orElse(0),
                metadata.map(UtilsMetaResponse::getTotalPages).orElse(0));
    }
}
