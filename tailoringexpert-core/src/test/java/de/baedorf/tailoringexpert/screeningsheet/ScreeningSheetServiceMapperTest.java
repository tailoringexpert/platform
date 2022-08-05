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
package de.baedorf.tailoringexpert.screeningsheet;

import de.baedorf.tailoringexpert.domain.Parameter;
import de.baedorf.tailoringexpert.domain.ScreeningSheetParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.baedorf.tailoringexpert.domain.DatenTyp.SKALAR;
import static org.assertj.core.api.Assertions.assertThat;

class ScreeningSheetServiceMapperTest {

    private ScreeningSheetServiceMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new ScreeningSheetServiceMapperImpl();
    }

    @Test
    void createScreeningSheet_ParameterNull_NullWirdZurueckGegeben() {
        // arrange

        // act
        ScreeningSheetParameter actual = mapper.createScreeningSheet(null);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void createScreeningSheet_ParameterVorhanden_ScreeningSheetParameterWirdZurueckGegeben() {
        // arrange
        Parameter parameter = Parameter.builder()
            .datenTyp(SKALAR)
            .kategorie("Lebensdauer")
            .bezeichnung("15 Jahre < t")
            .name("Dauer4")
            .reihenfolge(1)
            .build();

        // act
        ScreeningSheetParameter actual = mapper.createScreeningSheet(parameter);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getBezeichnung()).isEqualTo(parameter.getKategorie());
        assertThat(actual.getWert()).isEqualTo(parameter.getBezeichnung());
    }
}
