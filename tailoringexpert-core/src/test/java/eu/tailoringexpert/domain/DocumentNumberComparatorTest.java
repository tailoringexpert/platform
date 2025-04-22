package eu.tailoringexpert.domain;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Log4j2
class DocumentNumberComparatorTest {

    private Comparator<Document> comparator;

    @BeforeEach
    void setup() {
        this.comparator = new DocumentNumberComparator();
    }

    @Test
    void compare_ReferenceNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> comparator.compare(null, Document.builder().number("a").build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void compare_CompareNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> comparator.compare(Document.builder().number("a").build(), null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void compare_sameLengthOneDigitSameValue_ZeroReturned() {
        // arrange

        // act
        int actual = comparator.compare(
            Document.builder().number("a").build(),
            Document.builder().number("a").build()
        );

        // assert
        assertThat(actual).isZero();


    }

    @Test
    void compare_sameLengthOneDigitReferenceBeforeCompare_MinusOneReturned() {
        // arrange

        // act
        int actual = comparator.compare(
            Document.builder().number("a").build(),
            Document.builder().number("b").build()
        );

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(-1);

    }

    @Test
    void compare_sameLengthOneDigitReferenceAfterCompare_OneReturned() {
        // arrange

        // act
        int actual = comparator.compare(
            Document.builder().number("b").build(),
            Document.builder().number("a").build()
        );

        // assert
        log.debug(actual);
        assertThat(actual).isOne();

    }

    @Test
    void compare_sameLengthTwoDigitSameValue_ZeroReturned() {
        // arrange

        // act
        int actual = comparator.compare(
            Document.builder().number("aa").build(),
            Document.builder().number("aa").build()
        );

        // assert
        log.debug(actual);
        assertThat(actual).isZero();

    }

    @Test
    void compare_sameLengthTwoDigitReferenceAfterCompare_OneReturned() {
        // arrange

        // act
        int actual = comparator.compare(
            Document.builder().number("ab").build(),
            Document.builder().number("aa").build()
        );

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(1);

    }

    @Test
    void compare_sameLengthTwoDigitReferenceBeforeCompare_MinusOneReturned() {
        // arrange

        // act
        int actual = comparator.compare(
            Document.builder().number("aa").build(),
            Document.builder().number("ab").build()
        );

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(-1);

    }

    @Test
    void sort_UnorderedListElementDifferentLengthNonNulls_ListCorrectSorted() {
        // arrange
        List<Document> positions = new ArrayList<>();
        positions.add(Document.builder().number("a").build());
        positions.add(Document.builder().number("b").build());
        positions.add(Document.builder().number("aa").build());
        positions.add(Document.builder().number("e").build());

        // act
        positions.sort(comparator);

        // assert
        assertThat(positions).containsExactlyElementsOf(of(
            Document.builder().number("a").build(),
            Document.builder().number("b").build(),
            Document.builder().number("e").build(),
            Document.builder().number("aa").build()
        ));
    }

    @Test
    void sort_UnorderedListElementDifferentLengthWithNulls_ListCorrectSorted() {
        // arrange
        List<Document> positions = new ArrayList<>();
        positions.add(Document.builder().number("a").build());
        positions.add(null);
        positions.add(Document.builder().number("aa").build());
        positions.add(Document.builder().number("e").build());

        // act
        Throwable actual = catchThrowable(() -> positions.sort(comparator));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }
}
