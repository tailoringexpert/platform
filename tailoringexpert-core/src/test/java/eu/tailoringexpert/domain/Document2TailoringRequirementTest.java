/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2025 Michael Bädorf and others
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class Document2TailoringRequirementTest {

    private Document2TailoringRequirement converter;

    @BeforeEach
    void beforeEach() {
        this.converter = new Document2TailoringRequirement();
    }

    @Test
    void apply_OnlyNumberAndTitle() {
        // arrange
        Document document = Document.builder()
            .number("z")
            .title("ECSS-Q-ST-80")
            .build();

        // act
        TailoringRequirement actual = converter.apply(document);

        // assert
        assertThat(actual.getPosition()).isEqualTo("z");
        assertThat(actual.getText()).isEqualTo("ECSS-Q-ST-80");
    }

    @Test
    void apply_NumberAndTitleAndIssue() {
        // arrange
        Document document = Document.builder()
            .number("z")
            .title("ECSS-Q-ST-80")
            .issue("C")
            .build();

        // act
        TailoringRequirement actual = converter.apply(document);

        // assert
        assertThat(actual.getPosition()).isEqualTo("z");
        assertThat(actual.getText()).isEqualTo("ECSS-Q-ST-80C");
    }

    @Test
    void apply_NumberAndTitleAndIssueAndRevision() {
        // arrange
        Document document = Document.builder()
            .number("z")
            .title("ECSS-Q-ST-80")
            .issue("C")
            .revision("Rev 1.0")
            .build();

        // act
        TailoringRequirement actual = converter.apply(document);

        // assert
        assertThat(actual.getPosition()).isEqualTo("z");
        assertThat(actual.getText()).isEqualTo("ECSS-Q-ST-80CRev 1.0");
    }

}
