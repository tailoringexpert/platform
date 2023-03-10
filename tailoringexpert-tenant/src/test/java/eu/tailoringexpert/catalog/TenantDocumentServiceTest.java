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

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;

import static java.util.Map.ofEntries;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TenantDocumentServiceTest {

    TenantDocumentService service;
    DocumentService tenentDocumentServiceMock;

    @BeforeEach
    void beforeEach() {
        this.tenentDocumentServiceMock = mock(DocumentService.class);
        this.service = new TenantDocumentService(ofEntries(
            new SimpleEntry("TENANT", tenentDocumentServiceMock)
        ));
    }

    @Test
    void createCatalog_TenantNotExists_NoSuchMethodExceptionThrown() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        // act
        Exception actual = catchException(() -> service.createCatalog(catalog, erstellungsZeitpunt));

        // assert
        assertThat(actual).isInstanceOf(NoSuchMethodException.class);
        verify(tenentDocumentServiceMock, times(0)).createCatalog(catalog, erstellungsZeitpunt);
    }

    @Test
    void createCatalog_TenantExists_TenantImplementationReturned() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        given(tenentDocumentServiceMock.createCatalog(catalog, erstellungsZeitpunt))
            .willReturn(of(File.builder().build()));

        // act
        Optional<File> actual = service.createCatalog(catalog, erstellungsZeitpunt);

        // assert
        verify(tenentDocumentServiceMock, times(1)).createCatalog(catalog, erstellungsZeitpunt);
        assertThat(actual).isPresent();
    }

    @Test
    void createAll_TenantNotExists_NoSuchMethodExceptionThrown() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        // act
        Exception actual = catchException(() -> service.createAll(catalog, erstellungsZeitpunt));

        // assert
        assertThat(actual).isInstanceOf(NoSuchMethodException.class);
        verify(tenentDocumentServiceMock, times(0)).createAll(catalog, erstellungsZeitpunt);
    }

    @Test
    void createAll_TenantExists_TenantImplementationReturned() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        given(tenentDocumentServiceMock.createAll(catalog, erstellungsZeitpunt))
            .willReturn(List.of());

        // act
        service.createAll(catalog, erstellungsZeitpunt);

        // assert
        verify(tenentDocumentServiceMock, times(1)).createAll(catalog, erstellungsZeitpunt);
    }
}
