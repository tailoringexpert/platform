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
package eu.tailoringexpert.renderer;

import eu.tailoringexpert.domain.File;
import org.apache.pdfbox.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class PDFEngineTest {

    PDFEngine engine;

    @BeforeEach
    void beforeEach() {
        this.engine =
            new PDFEngine(
                () -> RendererRequestConfiguration.builder()
                    .id("plattform")
                    .name("TailoringExpert")
                    .templateHome("baseuri")
                    .build()
            );
    }

    @Test
    void process_NoDocId_NullPointerExceptionThrown() {
        // arrange

        // act
        Exception actual = catchException(() -> engine.process(null, "html", "suffix"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void process_NoHTML_NullPointerExceptionThrown() {
        // arrange

        // act
        Exception actual = catchException(() -> engine.process("docid", null, "suffix"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void process_Nouffix_NullPointerExceptionThrown() {
        // arrange

        // act
        Exception actual = catchException(() -> engine.process("docid", "html", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void process_toByteArrayWithError_NullReturned() {
        // arrange

        // act
        File actual;
        try (MockedStatic<IOUtils> io = mockStatic(IOUtils.class)) {
            io.when(() -> IOUtils.toByteArray(any())).thenThrow(new IOException());
            actual = engine.process("4711", "tailoring", "parameter");
        }

        // assert
        assertThat(actual).isNull();
    }
}
