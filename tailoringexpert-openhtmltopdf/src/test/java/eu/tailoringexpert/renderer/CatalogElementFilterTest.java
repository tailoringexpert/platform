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
package eu.tailoringexpert.renderer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.tailoringexpert.domain.BaseCatalogElement;
import eu.tailoringexpert.domain.CatalogElement;
import lombok.extern.log4j.Log4j2;

@Log4j2
class CatalogElementFilterTest {

    CatalogElementFilter filter;

    @BeforeEach
    void beforeEach() {
        this.filter = new CatalogElementFilter();
    }

    @Test
    void apply_CollectionNonNullChapterNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> filter.apply(List.of(), null));

        // assert
        assertThat(actual)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void apply_NUllCollectionChapterNonNull_EmptyListReturned() {
        // arrange

        // act
        Collection<CatalogElement> actual = filter.apply(null, "1");

        // assert
        assertThat(actual)
                .isEmpty();
    }

    @Test
    void apply_Chapter1_ElementsOfChapterAndAllSupchaptersReturned() {
        // arrange
        List<CatalogElement> rows = List.of(
                BaseCatalogElement.builder()
                        .chapter("1")
                        .build(),
                BaseCatalogElement.builder()
                        .position("a")
                        .text("Requirement 1.a")
                        .build(),
                BaseCatalogElement.builder()
                        .chapter("1.1")
                        .build(),
                BaseCatalogElement.builder()
                        .position("a")
                        .text("Requirement 1.1.a")
                        .build(),
                BaseCatalogElement.builder()
                        .position("b")
                        .text("Requirement 1.1.b")
                        .build(),
                BaseCatalogElement.builder()
                        .chapter("1.1.1")
                        .build(),
                BaseCatalogElement.builder()
                        .chapter("1.1.1.2")
                        .build(),
                BaseCatalogElement.builder()
                        .position("a")
                        .text("Requirement 1.1.1.2.a")
                        .build()

        );

        // act
        Collection<CatalogElement> actual = filter.apply(rows, "1");

        // assert
        assertThat(actual)
                .hasSize(8);
    }

    @Test
    void apply_Chapter1_1_ElementsOfChapter1_1AndAllSubchaptersReturned() {
        // arrange
        List<CatalogElement> rows = List.of(
                BaseCatalogElement.builder()
                        .chapter("1")
                        .build(),
                BaseCatalogElement.builder()
                        .position("a")
                        .text("Requirement 1.a")
                        .build(),
                BaseCatalogElement.builder()
                        .chapter("1.1")
                        .build(),
                BaseCatalogElement.builder()
                        .position("a")
                        .text("Requirement 1.1.a")
                        .build(),
                BaseCatalogElement.builder()
                        .position("b")
                        .text("Requirement 1.1.b")
                        .build(),
                BaseCatalogElement.builder()
                        .chapter("1.1.1")
                        .build(),
                BaseCatalogElement.builder()
                        .chapter("1.1.1.2")
                        .build(),
                BaseCatalogElement.builder()
                        .position("a")
                        .text("Requirement 1.1.1.2.a")
                        .build()

        );

        // act
        Collection<CatalogElement> actual = filter.apply(rows, "1.1");

        // assert
        assertThat(actual)
                .hasSize(6);
    }

    @Test
    void apply_Chapter1_1_1_2_ElementsOfChapter1_1_1_2Returned() {
        // arrange
        List<CatalogElement> rows = List.of(
                BaseCatalogElement.builder()
                        .chapter("1")
                        .build(),
                BaseCatalogElement.builder()
                        .position("a")
                        .text("Requirement 1.a")
                        .build(),
                BaseCatalogElement.builder()
                        .chapter("1.1")
                        .build(),
                BaseCatalogElement.builder()
                        .position("a")
                        .text("Requirement 1.1.a")
                        .build(),
                BaseCatalogElement.builder()
                        .position("b")
                        .text("Requirement 1.1.b")
                        .build(),
                BaseCatalogElement.builder()
                        .chapter("1.1.1")
                        .build(),
                BaseCatalogElement.builder()
                        .chapter("1.1.1.2")
                        .build(),
                BaseCatalogElement.builder()
                        .position("a")
                        .text("Requirement 1.1.1.2.a")
                        .build()

        );

        // act
        Collection<CatalogElement> actual = filter.apply(rows, "1.1.1.2");

        // assert
        assertThat(actual)
                .hasSize(2);

    }

}
