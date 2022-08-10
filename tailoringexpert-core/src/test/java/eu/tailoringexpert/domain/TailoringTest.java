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

import eu.tailoringexpert.domain.Tailoring.TailoringBuilder;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

@Log
class TailoringTest {

    @Test
    void build() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        TailoringBuilder builder = Tailoring.builder()
            .screeningSheet(ScreeningSheet.builder().data(data).build())
            .phasen(asList(ZERO, A, B));

        // act
        Tailoring actual = builder.build();

        // assert
        assertThat(actual).isNotNull();
    }

}


