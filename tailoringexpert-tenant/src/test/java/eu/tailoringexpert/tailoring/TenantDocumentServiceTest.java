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

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        this.service = new TenantDocumentService(Map.ofEntries(
            new SimpleEntry("TENANT", tenentDocumentServiceMock)
        ));
    }

    @Test
    void createAnforderungDokument_MandantNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        // act
        Optional<File> actual = service.createRequirementDocument(tailoring, erstellungsZeitpunt);

        // assert
        assertThat(actual).isEmpty();
        verify(tenentDocumentServiceMock, times(0)).createRequirementDocument(tailoring, erstellungsZeitpunt);
    }

    @Test
    void createAnforderungDokument_MandantVorhanden_ErgebnisDesMandantAufrufsZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        given(tenentDocumentServiceMock.createRequirementDocument(tailoring, erstellungsZeitpunt))
            .willReturn(Optional.of(File.builder().build()));

        // act
        Optional<File> actual = service.createRequirementDocument(tailoring, erstellungsZeitpunt);

        // assert
        verify(tenentDocumentServiceMock, times(1)).createRequirementDocument(tailoring, erstellungsZeitpunt);
        assertThat(actual).isPresent();
    }

    @Test
    void createVergleichsDokument_MandantNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        // act
        Optional<File> actual = service.createComparisonDocument(tailoring, erstellungsZeitpunt);

        // assert
        assertThat(actual).isEmpty();
        verify(tenentDocumentServiceMock, times(0)).createComparisonDocument(tailoring, erstellungsZeitpunt);
    }

    @Test
    void createVergleichsDokument_MandantVorhanden_ErgebnisDesMandantAufrufsZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        given(tenentDocumentServiceMock.createComparisonDocument(tailoring, erstellungsZeitpunt))
            .willReturn(Optional.of(File.builder().build()));

        // act
        Optional<File> actual = service.createComparisonDocument(tailoring, erstellungsZeitpunt);

        // assert
        verify(tenentDocumentServiceMock, times(1)).createComparisonDocument(tailoring, erstellungsZeitpunt);
        assertThat(actual).isPresent();
    }

    @Test
    void createAll_MandantNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        // act
        Collection<File> actual = service.createAll(tailoring, erstellungsZeitpunt);

        // assert
        verify(tenentDocumentServiceMock, times(0)).createAll(tailoring, erstellungsZeitpunt);
        assertThat(actual).isEmpty();
    }

    @Test
    void createAll_MandantVorhanden_ErgebnisDesMandantAufrufsZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        given(tenentDocumentServiceMock.createAll(tailoring, erstellungsZeitpunt))
            .willReturn(Arrays.asList(File.builder().build()));

        // act
        Collection<File> actual = service.createAll(tailoring, erstellungsZeitpunt);

        // assert
        verify(tenentDocumentServiceMock, times(1)).createAll(tailoring, erstellungsZeitpunt);
        assertThat(actual).hasSize(1);
    }
}
