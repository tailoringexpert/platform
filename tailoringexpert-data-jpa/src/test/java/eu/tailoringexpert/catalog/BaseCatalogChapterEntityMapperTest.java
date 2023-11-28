/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2023 Michael Bädorf and others
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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseCatalogChapterEntity;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Chapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class BaseCatalogChapterEntityMapperTest {

    private BaseCatalogChapterEntityMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new BaseCatalogChapterEntityMapperGenerated();
    }

    @Test
    void createCatalog_ChapterWithRequirements_EntityRequirementsContainsValidNumber() {
        // arrange
        Chapter<BaseRequirement> toc = Chapter.<BaseRequirement>builder()
            .number("1.2.1")
            .requirements(asList(
                BaseRequirement.builder()
                    .text("Requirement 1")
                    .position("a")
                    .build(),
                BaseRequirement.builder()
                    .text("Requirement 2")
                    .position("b")
                    .build())
            )
            .build();

        // act
        BaseCatalogChapterEntity actual = mapper.toEntity(toc);

        // assert
        assertThat(actual.getRequirements())
            .hasSize(2);
    }

    @Test
    void createCatalog_ChapterNullRequirements_EntityNullRequirementsReturned() {
        // arrange
        Chapter<BaseRequirement> toc = Chapter.<BaseRequirement>builder()
            .number("1.2.1")
            .requirements(null)
            .build();

        // act
        BaseCatalogChapterEntity actual = mapper.toEntity(toc);

        // assert
        assertThat(actual.getRequirements())
            .isNull();
    }
}
