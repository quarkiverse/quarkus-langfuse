package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.Optional;

import com.langfuse.api.model.CursorMeta;

/**
 * Maps a generated cursor-paginated response onto a {@link CursorResult}.
 *
 * <p>
 * The cursor-addressed counterpart of {@link PagedResults}. Missing metadata is treated as
 * "no further data" rather than being guessed at.
 */
final class CursorResults {

    private CursorResults() {
    }

    // CursorMeta.getCursor() is documented as "the cursor to use for the next page; absent when there
    // is no next page" - i.e. it already IS the next cursor, not the one just used. Folded straight to
    // null (rather than Optional) since that's what the DefaultCursorResult constructor expects.
    static <T> CursorResult<T> from(Cursor cursor, List<T> data, CursorMeta meta) {
        var nextCursor = Optional.ofNullable(meta)
                .map(CursorMeta::getCursor)
                .orElse(null);

        return new DefaultCursorResult<>(data, cursor, nextCursor);
    }
}
