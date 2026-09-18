package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.langfuse.api.model.CursorMeta;
import com.langfuse.api.model.GetScoresV3Meta;

import io.quarkiverse.langfuse.api.cursor.Cursor;

class CursorResultsTests {

    @Test
    void extractsTheNextCursorFromCursorMeta() {
        var meta = CursorMeta.builder()
                .cursor("next-token")
                .build();

        assertThat(CursorResults.from(Cursor.first(50), List.of("a", "b"), meta, CursorMeta::getCursor))
                .satisfies(result -> assertThat(result.items()).containsExactly("a", "b"))
                .satisfies(result -> assertThat(result.hasNext()).isTrue())
                .extracting(result -> result.nextCursor().orElseThrow())
                .isEqualTo(Cursor.at("next-token", 50));
    }

    @Test
    void extractsTheNextCursorFromAStructurallyUnrelatedMetaType() {
        var meta = GetScoresV3Meta.builder()
                .limit(50)
                .cursor("scores-token")
                .build();

        assertThat(CursorResults.from(Cursor.first(50), List.of("a"), meta, GetScoresV3Meta::getCursor))
                .satisfies(result -> assertThat(result.hasNext()).isTrue())
                .extracting(result -> result.nextCursor().orElseThrow())
                .isEqualTo(Cursor.at("scores-token", 50));
    }

    @Test
    void anAbsentCursorInTheMetadataMeansNoFurtherData() {
        var meta = CursorMeta.builder()
                .build();

        assertThat(CursorResults.from(Cursor.first(50), List.of("a"), meta, CursorMeta::getCursor))
                .satisfies(result -> assertThat(result.items()).containsExactly("a"))
                .satisfies(result -> assertThat(result.hasNext()).isFalse())
                .extracting(result -> result.nextCursor())
                .satisfies(next -> assertThat(next).isEmpty());
    }

    @Test
    void absentMetadataMeansNoFurtherData() {
        assertThat(CursorResults.from(Cursor.first(50), List.of("a"), (CursorMeta) null, CursorMeta::getCursor))
                .satisfies(result -> assertThat(result.items()).containsExactly("a"))
                .satisfies(result -> assertThat(result.hasNext()).isFalse())
                .extracting(result -> result.nextCursor())
                .satisfies(next -> assertThat(next).isEmpty());
    }
}
