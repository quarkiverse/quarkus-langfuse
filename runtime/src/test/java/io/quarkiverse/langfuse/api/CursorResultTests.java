package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class CursorResultTests {

    @Test
    void exposesItemsAndCursor() {
        var cursor = Cursor.first(3);

        assertThat(new DefaultCursorResult<>(List.of("a", "b", "c"), cursor, "next-token"))
                .satisfies(result -> assertThat(result.items()).containsExactly("a", "b", "c"))
                .extracting(CursorResult::cursor)
                .isEqualTo(cursor);
    }

    @Test
    void nextCursorKeepsTheSameLimit() {
        var result = new DefaultCursorResult<>(List.of("a"), Cursor.at("first-token", 75), "next-token");

        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor())
                .isPresent()
                .contains(Cursor.at("next-token", 75));
    }

    @Test
    void theLastBatchHasNoNextCursor() {
        var result = new DefaultCursorResult<>(List.of("a"), Cursor.first(75), null);

        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isEmpty();
    }

    @Test
    void aBlankNextCursorIsTreatedAsExhausted() {
        assertThat(new DefaultCursorResult<>(List.of("a"), Cursor.first(75), "   ").hasNext())
                .isFalse();
    }

    @Test
    void itemsAreDefensivelyCopiedAndUnmodifiable() {
        var source = new ArrayList<>(List.of("a", "b"));
        var result = new DefaultCursorResult<>(source, Cursor.first(75), null);

        source.add("c");

        assertThat(result.items()).containsExactly("a", "b");
        assertThatThrownBy(() -> result.items().add("d"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nullItemsBecomeAnEmptyList() {
        assertThat(new DefaultCursorResult<String>(null, Cursor.first(75), null).items())
                .isNotNull()
                .isEmpty();
    }

    @Test
    void cursorIsRequired() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DefaultCursorResult<>(List.of(), null, null))
                .withMessageContaining("Cursor must not be null");
    }
}
