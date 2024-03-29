/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 Michael Bädorf and others
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

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class BaseCatalogVersionResourceTest {

    @Test
    void constructor_ValidUntilDate_ResourceWithUntilCreated() {
        // arrange

        // act
        BaseCatalogVersionResource actual = new BaseCatalogVersionResource(
            "8.2.1",
            "31.12.2022",
            "31.12.2022",
            Boolean.FALSE,
            Collections.emptyList()
        );

        // assert
        assertThat(actual.getValidUntil()).isNotNull();
    }

    @Test
    void constructor_NoValidUntilDate_ResourceWithNoUntilCreated() {
        // arrange

        // act
        BaseCatalogVersionResource actual = new BaseCatalogVersionResource(
            "8.2.1",
            "31.12.2022",
            null,
            Boolean.TRUE,
            Collections.emptyList()
        );

        // assert
        assertThat(actual.getValidUntil()).isNull();
    }
}
