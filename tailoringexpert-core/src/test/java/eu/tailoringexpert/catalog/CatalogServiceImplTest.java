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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
class CatalogServiceImplTest {

    private CatalogServiceRepository repositoryMock;
    private DocumentService documentServiceMock;
    private CatalogServiceImpl service;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(CatalogServiceRepository.class);
        this.documentServiceMock = mock(DocumentService.class);
        this.service = new CatalogServiceImpl(repositoryMock, documentServiceMock);
    }

    @Test
    void doImport_CatalogAlreadyExists_FalseReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().version("8.2.1").build();
        given(repositoryMock.existsCatalog("8.2.1")).willReturn(true);

        ZonedDateTime now = ZonedDateTime.of(
            LocalDateTime.of(2020, 12, 1, 8, 0, 0),
            ZoneId.systemDefault()
        );

        // act
        Boolean actual;
        try (MockedStatic<ZonedDateTime> dateTimeMock = mockStatic(ZonedDateTime.class)) {
            dateTimeMock.when(ZonedDateTime::now).thenReturn(now);
            actual = service.doImport(catalog);
        }

        // assert
        assertThat(actual).isFalse();
        verify(repositoryMock, times(0)).createCatalog(catalog, now);
    }

    @Test
    void doImport_CatalogNotExists_TrueReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().version("8.2.1").build();
        given(repositoryMock.createCatalog(eq(catalog), any())).willReturn(of(catalog));

        given(repositoryMock.existsCatalog("8.2.1")).willReturn(false);

        ZonedDateTime now = ZonedDateTime.of(
            LocalDateTime.of(2020, 12, 1, 8, 0, 0),
            ZoneId.systemDefault()
        );

        // act
        Boolean actual;
        try (MockedStatic<ZonedDateTime> dateTimeMock = mockStatic(ZonedDateTime.class)) {
            dateTimeMock.when(ZonedDateTime::now).thenReturn(now);
            actual = service.doImport(catalog);
        }

        // assert
        assertThat(actual).isTrue();
        verify(repositoryMock, times(1)).createCatalog(catalog, now);
    }

    @Test
    void doImport_CatalogNotButNotImported_FalseReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().version("8.2.1").build();
        given(repositoryMock.createCatalog(eq(catalog), any())).willReturn(empty());

        given(repositoryMock.existsCatalog("8.2.1")).willReturn(false);

        ZonedDateTime now = ZonedDateTime.of(
            LocalDateTime.of(2020, 12, 1, 8, 0, 0),
            ZoneId.systemDefault()
        );

        // act
        Boolean actual;
        try (MockedStatic<ZonedDateTime> dateTimeMock = mockStatic(ZonedDateTime.class)) {
            dateTimeMock.when(ZonedDateTime::now).thenReturn(now);
            actual = service.doImport(catalog);
        }

        // assert
        assertThat(actual).isFalse();
        verify(repositoryMock, times(1)).createCatalog(catalog, now);
    }

    @Test
    void doImport_CatalogNotProvided_NullPointerExceptionIsThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.doImport(null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getCatalog_VersionNotProvided_NullPointerExceptionIsThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getCatalog(null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getCatalog_VersionExist_CatalogWillBeLoaded() {
        // arrange
        given(repositoryMock.getCatalog("8.2.1")).willReturn(of(Catalog.<BaseRequirement>builder().build()));

        // act
        service.getCatalog("8.2.1");

        // assert
        verify(repositoryMock, times(1)).getCatalog("8.2.1");
    }

    @Test
    void createCatalog_CatalogNotExisting_EmptyReturned() {
        // arrange
        given(repositoryMock.getCatalog(any())).willReturn(empty());

        // act
        Optional<File> actual = service.createCatalog("8.2.1");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getCatalog("8.2.1");
    }

    @Test
    void createCatalog_CatalogExists_FileReturned() {
        // arrange
        given(repositoryMock.getCatalog(any())).willReturn(of(Catalog.<BaseRequirement>builder().build()));

        given(documentServiceMock.createCatalog(any(), any())).willReturn(of(File.builder().build()));

        // act
        Optional<File> actual = service.createCatalog("8.2.1");

        // assert
        assertThat(actual).isNotEmpty();
        verify(repositoryMock, times(1)).getCatalog("8.2.1");
        verify(documentServiceMock, times(1)).createCatalog(any(), any());
    }

    @Test
    void createCatalog_DocumentServiceEmptyResult_EmptyReturned() {
        // arrange
        given(repositoryMock.getCatalog(any())).willReturn(of(Catalog.<BaseRequirement>builder().build()));

        given(documentServiceMock.createCatalog(any(), any())).willReturn(empty());

        // act
        Optional<File> actual = service.createCatalog("8.2.1");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getCatalog("8.2.1");
        verify(documentServiceMock, times(1)).createCatalog(any(), any());
    }

    @Test
    void createDocuments_CatalogNotExisting_EmptyReturned() {
        // arrange
        given(repositoryMock.getCatalog("8.2.1")).willReturn(empty());

        // act
        Optional<File> actual = service.createDocuments("8.2.1");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void createDocuments_CatalogExisting_ZipReturned() throws IOException {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().version("8.2.1").build();
        given(repositoryMock.getCatalog("8.2.1"))
            .willReturn(of(catalog));

        List<File> dokumente = asList(
            File.builder()
                .name("DUMMY-KATALOG.pdf")
                .data("Testdokument".getBytes(UTF_8))
                .build()
        );
        given(documentServiceMock.createAll(eq(catalog), any())).willReturn(dokumente);

        // act
        Optional<File> actual = service.createDocuments("8.2.1");

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getName()).isEqualTo("catalog_8.2.1.zip");
        assertThat(actual.get().getType()).isEqualTo("zip");

        Collection<String> zipDateien = fileNameInZip(actual.get().getData());
        assertThat(zipDateien)
            .hasSize(1)
            .containsExactly("DUMMY-KATALOG.pdf");
    }

    Collection<String> fileNameInZip(byte[] zip) throws IOException {
        Collection<String> result = new ArrayList<>();
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zip));
        ZipEntry entry;
        while (nonNull(entry = zin.getNextEntry())) {
            result.add(entry.getName());
            zin.closeEntry();
        }
        zin.close();
        return result;
    }

    @Test
    void createDocuments_CloseZipOutputStreamException_ExceptionThrown() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().version("8.2.1").build();
        given(repositoryMock.getCatalog("8.2.1"))
            .willReturn(of(catalog));

        List<File> dokumente = asList(
            File.builder()
                .name("DUMMY-KATALOG.pdf")
                .data("Testdokument".getBytes(UTF_8))
                .build()
        );
        given(documentServiceMock.createAll(eq(catalog), any())).willReturn(dokumente);

        CatalogServiceImpl serviceSpy = Mockito.spy(service);
        given(serviceSpy.createZip(dokumente)).willThrow(new RuntimeException());

        // act
        Throwable actual = catchThrowable(() -> serviceSpy.createDocuments("8.2.1"));

        // assert
        assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    void createZip_addToZipSimulatedException_ExceptionThrown() {
        // arrange
        List<File> files = List.of(
            File.builder()
                .name("DUMMY-KATALOG.pdf")
                .data("Testdokument".getBytes(UTF_8))
                .build()
        );

        CatalogServiceImpl serviceSpy = Mockito.spy(service);
        doThrow(new RuntimeException()).when(serviceSpy).addToZip(any(File.class), any(ZipOutputStream.class));

        // act
        Throwable actual = catchThrowable(() -> serviceSpy.createZip(files));

        // assert
        assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    void addToZip_ZipOutputStremException_ExceptionThrown() throws Exception {
        // arrange
        Throwable actual;
        try (ZipOutputStream zipMock = mock(ZipOutputStream.class)) {
            File file = File.builder().name("dummy.pdf").build();
            doThrow(new IOException()).when(zipMock).putNextEntry(any());

            // act
            actual = catchThrowable(() -> service.addToZip(file, zipMock));
        }

        // assert
        assertThat(actual).isInstanceOf(IOException.class);
    }
}
