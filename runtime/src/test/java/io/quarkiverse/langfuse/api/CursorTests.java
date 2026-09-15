package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class CursorTests {

    @Test
    void firstStartsAtTheBeginningOfTheCollection() {
        assertThat(Cursor.first(75))
                .satisfies(cursor -> assertThat(cursor.value()).isEmpty())
                .extracting(Cursor::limit)
                .isEqualTo(75);
    }

    @Test
    void atCarriesTheServerSuppliedValue() {
        assertThat(Cursor.at("ey-opaque-token", 75))
                .satisfies(cursor -> assertThat(cursor.value()).contains("ey-opaque-token"))
                .extracting(Cursor::limit)
                .isEqualTo(75);
    }

    /**
     * The cursor value is whatever Langfuse hands back. Nothing here may interpret it.
     */
    @Test
    void cursorValuesAreOpaque() {
        assertThat(Cursor.at("{\"id\":\"abc\",\"ts\":1}", 10).value())
                .contains("{\"id\":\"abc\",\"ts\":1}");
    }

    @Test
    void cursorsAreValueObjects() {
        assertThat(Cursor.at("abc", 75))
                .isEqualTo(Cursor.at("abc", 75))
                .hasSameHashCodeAs(Cursor.at("abc", 75))
                .isNotEqualTo(Cursor.at("abc", 50))
                .isNotEqualTo(Cursor.at("def", 75))
                .isNotEqualTo(Cursor.first(75));
    }

    @Test
    void limitMustBePositive() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Cursor.first(0))
                .withMessageContaining("Cursor limit");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> Cursor.at("abc", -1));
    }

    @Test
    void valueMustNotBeNullOrBlank() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Cursor.at(null, 75))
                .withMessageContaining("must not be null or blank");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> Cursor.at("   ", 75));
    }
}
