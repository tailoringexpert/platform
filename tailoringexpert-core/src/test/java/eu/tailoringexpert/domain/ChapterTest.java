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

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class ChapterTest {

    @Test
    void allRequirements_SubchaptersPartiallyWithRequirements_AllRequirementsInListReturned() {
        // arrange
        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("1")
            .requirements(asList(
                TailoringRequirement.builder()
                    .text("Requirement 1")
                    .build()))
            .chapters(asList(
                Chapter.<TailoringRequirement>builder()
                    .number("1.1")
                    .requirements(asList(
                        TailoringRequirement.builder()
                            .text("Requirement 1.1")
                            .build()))
                    .chapters(
                        asList(Chapter.<TailoringRequirement>builder()
                            .number("1.1.1")
                            .requirements(asList(
                                TailoringRequirement.builder()
                                    .text("Requirement 1.1.1")
                                    .build()))
                            .build())
                    )
                    .build(),
                Chapter.<TailoringRequirement>builder()
                    .number("1.2")
                    .requirements(asList(
                        TailoringRequirement.builder()
                            .text("Requirement 1.2")
                            .build()))
                    .build()))
            .build();

        // act
        List<TailoringRequirement> actual = chapter.allRequirements().collect(toList());

        // assert
        assertThat(actual).hasSize(4);
    }

    @Test
    void getChapter_Chapter1_1_2Exists_ChapterReturned() {
        // arrange
        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("1")
            .requirements(asList(
                TailoringRequirement.builder()
                    .text("Requirement 1")
                    .build()))
            .chapters(asList(
                Chapter.<TailoringRequirement>builder()
                    .number("1.1")
                    .requirements(asList(
                        TailoringRequirement.builder()
                            .text("Requirement 1.1")
                            .build()))
                    .chapters(asList(
                        Chapter.<TailoringRequirement>builder()
                            .number("1.1.1")
                            .requirements(asList(
                                TailoringRequirement.builder()
                                    .text("Requirement 1.1.1")
                                    .build()))
                            .build(),
                        Chapter.<TailoringRequirement>builder()
                            .number("1.1.2")
                            .requirements(asList(
                                TailoringRequirement.builder()
                                    .text("Requirement 1.1.2")
                                    .build()))
                            .build()))
                    .build(),
                Chapter.<TailoringRequirement>builder()
                    .number("1.2")
                    .requirements(asList(
                        TailoringRequirement.builder()
                            .text("Requirement 1.2")
                            .build()))
                    .build()))
            .build();

        // act
        Chapter<TailoringRequirement> actual = chapter.getChapter("1.1.2");

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getNumber()).isEqualTo("1.1.2");

    }

}
