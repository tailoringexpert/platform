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
package eu.tailoringexpert.tailoring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import eu.tailoringexpert.domain.MediaTypeProvider;

class MediaTypeProviderTest {

    MediaTypeProvider provider;

    @BeforeEach
    void beforeEach() {
        this.provider = new MediaTypeProvider();
    }

    @Test
    void apply_UnbekannterTyp_NullWirdZurueckGegeben() {
        // arrange

        // act
        MediaType actual = provider.apply("BLAH");

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void apply_PdF_MediatypeKorrektErmittelt() {
        // arrange

        // act
        MediaType actual = provider.apply("PdF");

        // assert
        assertThat(actual).isEqualTo(MediaType.valueOf("application/pdf"));
    }

    @Test
    void apply_XSLM_MediatypeKorrektErmittelt() {
        // arrange

        // act
        MediaType actual = provider.apply("XLSM");

        // assert
        assertThat(actual).isEqualTo(MediaType.valueOf("application/vnd.ms-excel.sheet.macroEnabled.12"));
    }
}
