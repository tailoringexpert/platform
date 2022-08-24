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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    void doImport_CatalogProvided_ImportSuccessful() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();
        given(repositoryMock.createCatalog(eq(catalog), any()))
            .willReturn(of(catalog));

        ZonedDateTime now = ZonedDateTime.of(
            LocalDateTime.of(2020, 12, 1, 8, 0, 0),
            ZoneId.systemDefault()
        );

        // act
        Boolean actual;
        try (MockedStatic<ZonedDateTime> dateTimeMock = mockStatic(ZonedDateTime.class)) {
            dateTimeMock.when(ZonedDateTime::now).thenReturn(now);
            actual = service.doImport(catalog);
            verify(repositoryMock, times(1))
                .createCatalog(catalog, now);
        }

        // assert
        assertThat(actual).isTrue();
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
    void getCatalog_VersionInSystem_CatalogWillBeLoaded() {
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
        given(repositoryMock.getCatalog(any())).willReturn(Optional.empty());

        // act
        Optional<File> actual = service.createCatalog("8.2.1");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getCatalog("8.2.1");
    }

    @Test
    void createCatalog_CatalogExisting_FileReturned() {
        // arrange
        given(repositoryMock.getCatalog(any()))
            .willReturn(of(Catalog.<BaseRequirement>builder().build()));

        given(documentServiceMock.createCatalog(any(), any()))
            .willReturn(of(File.builder().build()));

        // act
        Optional<File> actual = service.createCatalog("8.2.1");

        // assert
        assertThat(actual).isNotEmpty();
        verify(repositoryMock, times(1)).getCatalog("8.2.1");
        verify(documentServiceMock, times(1)).createCatalog(any(), any());
    }
}
