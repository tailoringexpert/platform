/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package eu.tailoringexpert.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.extern.log4j.Log4j2;

@Log4j2
class CatalogueElementQueryTest {

    CatalogueElementQuery query;

    @BeforeEach
    void beforeEach() {
        this.query = new CatalogueElementQuery();
    }

    @Test
    void rowsByChapter_CollectionNonNullChapterNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> query.byChapter(List.of(), null));

        // assert
        assertThat(actual)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rowsByChapter_NUllCollectionChapterNonNull_EmptyListReturned() {
        // arrange

        // act
        Collection<CatalogueElement> actual = query.byChapter(null, "1");

        // assert
        assertThat(actual)
                .isEmpty();
    }

    @ParameterizedTest(name = "chapter {1} has {2} rows")
    @MethodSource("getRows")
    void requirementsByChapter_ChapterProvided_FilteredRowsReturned(Collection<CatalogueElement> rows, String chapter,
            int expectedRows) {
        // act
        Collection<CatalogueElement> actual = query.byChapter(rows, chapter);

        // assert
        assertThat(actual)
                .hasSize(expectedRows);
    }

    @SuppressWarnings({ "java:S1144" })
    private static Stream<Arguments> getRows() { // NOPMD - suppressed UnusedPrivateMethod - Used by
                                                 // parameterized test
                                                 // getRequirement_NullParameter_ExceptionThrown

        // arrange
        List<CatalogueElement> rows = List.of(
                BaseCatalogueElement.builder()
                        .chapter("1")
                        .build(),
                BaseCatalogueElement.builder()
                        .position("a")
                        .text("Requirement 1.a")
                        .build(),
                BaseCatalogueElement.builder()
                        .chapter("1.1")
                        .build(),
                BaseCatalogueElement.builder()
                        .position("a")
                        .text("Requirement 1.1.a")
                        .build(),
                BaseCatalogueElement.builder()
                        .position("b")
                        .text("Requirement 1.1.b")
                        .build(),
                BaseCatalogueElement.builder()
                        .chapter("1.1.1")
                        .build(),
                BaseCatalogueElement.builder()
                        .chapter("1.1.1.2")
                        .build(),
                BaseCatalogueElement.builder()
                        .position("a")
                        .text("Requirement 1.1.1.2.a")
                        .build()

        );

        return Stream.of(
                Arguments.of(rows, "1", 8),
                Arguments.of(rows, "1.1", 6),
                Arguments.of(rows, "1.1.1.2", 2));
    }

}
