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
class PositionComparatorTest {

    private Comparator<String> comparator;

    @BeforeEach
    void setup() {
        this.comparator = new PositionComparator();
    }

    @Test
    void compare_ReferenceNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> comparator.compare(null, "a"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void compare_CompareNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> comparator.compare("a", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void compare_sameLengthOneDigitSameValue_ZeroReturned() {
        // arrange

        // act
        int actual = comparator.compare("a", "a");

        // assert
        assertThat(actual).isZero();


    }

    @Test
    void compare_sameLengthOneDigitReferenceBeforeCompare_MinusOneReturned() {
        // arrange

        // act
        int actual = comparator.compare("a", "b");

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(-1);

    }

    @Test
    void compare_sameLengthOneDigitReferenceAfterCompare_OneReturned() {
        // arrange

        // act
        int actual = comparator.compare("b", "a");

        // assert
        log.debug(actual);
        assertThat(actual).isOne();

    }

    @Test
    void compare_sameLengthTwoDigitSameValue_ZeroReturned() {
        // arrange

        // act
        int actual = comparator.compare("aa", "aa");

        // assert
        log.debug(actual);
        assertThat(actual).isZero();

    }

    @Test
    void compare_sameLengthTwoDigitReferenceAfterCompare_OneReturned() {
        // arrange

        // act
        int actual = comparator.compare("ab", "aa");

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(1);

    }

    @Test
    void compare_sameLengthTwoDigitReferenceBeforeCompare_MinusOneReturned() {
        // arrange

        // act
        int actual = comparator.compare("aa", "ab");

        // assert
        log.debug(actual);
        assertThat(actual).isEqualTo(-1);

    }

    @Test
    void sort_UnorderedListElementDifferentLengthNonNulls_ListCorrectSorted() {
        // arrange
        List<String> positions = new ArrayList<>();
        positions.add("a");
        positions.add("b");
        positions.add("aa");
        positions.add("e");

        // act
        positions.sort(comparator);

        // assert
        for (String str : positions) System.out.print(" " + str);
        assertThat(positions).containsExactlyElementsOf(of("a", "b", "e", "aa"));
    }

    @Test
    void sort_UnorderedListElementDifferentLengthWithNulls_ListCorrectSorted() {
        // arrange
        List<String> positions = new ArrayList<>();
        positions.add("a");
        positions.add(null);
        positions.add("aa");
        positions.add("e");

        // act
        Throwable actual = catchThrowable(() -> positions.sort(comparator));

        // assert
        for (String str : positions) System.out.print(" " + str);
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }
}
