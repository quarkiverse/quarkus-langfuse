package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

class CursorSelectionTests {

    @Test
    void allUsesTheConfiguredDefaultBatchSize() {
        assertThat(PageSelection.all())
                .extracting(PageSelection::pageSize)
                .isEqualTo(OptionalInt.empty());

        assertThat(CursorSelection.all())
                .satisfies(selection -> assertThat(selection.startCursor()).isEmpty())
                .extracting(CursorSelection::limit, CursorSelection::batchCount)
                .containsExactly(OptionalInt.empty(), OptionalInt.empty());
    }

    @Test
    void allWithLimitStartsAtTheBeginningAndIsUnbounded() {
        assertThat(CursorSelection.all(75))
                .satisfies(selection -> assertThat(selection.startCursor()).isEmpty())
                .extracting(CursorSelection::limit, CursorSelection::batchCount)
                .containsExactly(OptionalInt.of(75), OptionalInt.empty());
    }

    @Test
    void fromIsUnbounded() {
        var cursor = Cursor.at("abc", 75);

        assertThat(CursorSelection.from(cursor))
                .satisfies(selection -> assertThat(selection.startCursor()).contains(cursor))
                .extracting(CursorSelection::limit, CursorSelection::batchCount)
                .containsExactly(OptionalInt.of(75), OptionalInt.empty());
    }

    @Test
    void onlySelectsASingleBatch() {
        var cursor = Cursor.at("abc", 75);

        assertThat(CursorSelection.only(cursor))
                .satisfies(selection -> assertThat(selection.startCursor()).contains(cursor))
                .extracting(CursorSelection::limit, CursorSelection::batchCount)
                .containsExactly(OptionalInt.of(75), OptionalInt.of(1));
    }

    @Test
    void firstSelectsTheLeadingBatches() {
        assertThat(CursorSelection.first(3, 75))
                .satisfies(selection -> assertThat(selection.startCursor())
                        .hasValueSatisfying(cursor -> assertThat(cursor.value()).isEmpty()))
                .extracting(CursorSelection::limit, CursorSelection::batchCount)
                .containsExactly(OptionalInt.of(75), OptionalInt.of(3));
    }

    @Test
    void fromWithCountIsBounded() {
        assertThat(CursorSelection.from(Cursor.at("abc", 75), 3))
                .extracting(CursorSelection::limit, CursorSelection::batchCount)
                .containsExactly(OptionalInt.of(75), OptionalInt.of(3));
    }

    @Test
    void zeroBatchesSelectsNothing() {
        assertThat(CursorSelection.first(0, 75).batchCount()).hasValue(0);
        assertThat(CursorSelection.from(Cursor.at("abc", 75), 0).batchCount()).hasValue(0);
    }

    @Test
    void firstBatchResolvesTheConfiguredDefaultLimit() {
        assertThat(CursorSelection.all().firstBatch(50))
                .satisfies(cursor -> assertThat(cursor.value()).isEmpty())
                .extracting(Cursor::limit)
                .isEqualTo(50);
    }

    @Test
    void firstBatchKeepsAnExplicitCursor() {
        var cursor = Cursor.at("abc", 75);

        assertThat(CursorSelection.from(cursor).firstBatch(50)).isEqualTo(cursor);
    }

    @Test
    void limitMustBePositive() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CursorSelection.all(0))
                .withMessageContaining("Cursor limit");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> CursorSelection.first(3, 0));
    }

    @Test
    void batchCountMustNotBeNegative() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CursorSelection.first(-1, 75))
                .withMessageContaining("Batch count");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> CursorSelection.from(Cursor.at("abc", 75), -1));
    }
}
