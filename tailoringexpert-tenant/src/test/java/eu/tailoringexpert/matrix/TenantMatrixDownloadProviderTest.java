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
package eu.tailoringexpert.matrix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.File;
import lombok.extern.log4j.Log4j2;

@Log4j2
class TenantMatrixDownloadProviderTest {

    TenantMatrixDownloadProvider download;

    @BeforeEach
    void beforeEach() {
        this.download = new TenantMatrixDownloadProvider("target/PLATFORM/test/1000");
    }

    @Test
    void apply_NullPath_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> download.apply(null));

        // assert
        assertThat(actual)
                .isNotNull()
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void apply_NoInpustream_EmptyReturned() {
        // arrange

        // act
        Optional<File> actual = null;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class);
                MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("PLATFORM");
            filesMock.when(() -> Files.newInputStream(any())).thenReturn(null);

            actual = download.apply("test.pdf");
        }

        // assert
        assertThat(actual)
                .isEmpty();
    }

    @Test
    void apply_FileExists_OptionalWithFileReturned() {
        // arrange
        byte[] data = "This is demo content".getBytes();

        // act
        Optional<File> actual = null;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class);
                MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("PLATFORM");
            filesMock.when(() -> Files.newInputStream(any())).thenReturn(new ByteArrayInputStream(data));

            actual = download.apply("screeningsheet.pdf");
        }
        // assert
        assertThat(actual)
                .isNotNull()
                .isNotEmpty();
    }
}
