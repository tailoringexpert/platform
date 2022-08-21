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

import java.io.IOException;
import java.io.InputStream;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

class FileTest {

    @Test
    void getLength_DatenVorhanden_LaengeErmittelt() throws IOException {
        // arrange
        byte[] daten;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            daten = is.readAllBytes();
        }

        File file = File.builder().data(daten).build();

        // act
        long actual = file.getLength();

        // assert
        assertThat(actual).isPositive();
    }

    @Test
    void getLength_DatenNichtVorhanden_0Laenge() {
        // arrange
        File file = File.builder().build();

        // act
        long actual = file.getLength();

        // assert
        assertThat(actual).isZero();
    }

    @Test
    void getName_DocIdUndTypeVorhanden_ZusammengesetzterName() throws IOException {
        // arrange
        File file = File.builder()
            .name("hallo.du")
            .build();

        // act
        String actual = file.getName();

        // assert
        assertThat(actual).isEqualTo("hallo.du");
    }
}
