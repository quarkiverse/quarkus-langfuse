package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;

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

    // The generated metadata classes (CursorMeta, ObservationsV2Meta, ExperimentsResponseMeta,
    // GetScoresV3Meta) are structurally identical for our purposes but share no supertype - the
    // generator emits each one independently - so the caller, which is the only code that knows its
    // own meta type, supplies the accessor rather than this class naming one of them.
    //
    // getCursor() is documented as "the cursor to use for the next page; absent when there is no next
    // page" - i.e. it already IS the next cursor, not the one just used. Folded straight to null
    // (rather than Optional) since that's what the DefaultCursorResult constructor expects.
    static <T, M> CursorResult<T> from(Cursor cursor, List<T> data, M meta, Function<M, String> nextCursor) {
        var nextCursorValue = Optional.ofNullable(meta)
                .map(nextCursor)
                .orElse(null);

        return CursorResult.of(data, cursor, nextCursorValue);
    }
}
