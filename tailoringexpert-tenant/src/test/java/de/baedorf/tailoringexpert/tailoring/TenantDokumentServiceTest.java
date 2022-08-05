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
package de.baedorf.tailoringexpert.tailoring;

import de.baedorf.tailoringexpert.TenantContext;
import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Tailoring;
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

class TenantDokumentServiceTest {

    TenantDokumentService service;
    DokumentService tenentDokumentServiceMock;

    @BeforeEach
    void beforeEach() {
        this.tenentDokumentServiceMock = mock(DokumentService.class);
        this.service = new TenantDokumentService(Map.ofEntries(
            new SimpleEntry("TENANT", tenentDokumentServiceMock)
        ));
    }

    @Test
    void createAnforderungDokument_MandantNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        // act
        Optional<Datei> actual = service.createAnforderungDokument(tailoring, erstellungsZeitpunt);

        // assert
        assertThat(actual).isEmpty();
        verify(tenentDokumentServiceMock, times(0)).createAnforderungDokument(tailoring, erstellungsZeitpunt);
    }

    @Test
    void createAnforderungDokument_MandantVorhanden_ErgebnisDesMandantAufrufsZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        given(tenentDokumentServiceMock.createAnforderungDokument(tailoring, erstellungsZeitpunt))
            .willReturn(Optional.of(Datei.builder().build()));

        // act
        Optional<Datei> actual = service.createAnforderungDokument(tailoring, erstellungsZeitpunt);

        // assert
        verify(tenentDokumentServiceMock, times(1)).createAnforderungDokument(tailoring, erstellungsZeitpunt);
        assertThat(actual).isPresent();
    }

    @Test
    void createVergleichsDokument_MandantNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        // act
        Optional<Datei> actual = service.createVergleichsDokument(tailoring, erstellungsZeitpunt);

        // assert
        assertThat(actual).isEmpty();
        verify(tenentDokumentServiceMock, times(0)).createVergleichsDokument(tailoring, erstellungsZeitpunt);
    }

    @Test
    void createVergleichsDokument_MandantVorhanden_ErgebnisDesMandantAufrufsZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        given(tenentDokumentServiceMock.createVergleichsDokument(tailoring, erstellungsZeitpunt))
            .willReturn(Optional.of(Datei.builder().build()));

        // act
        Optional<Datei> actual = service.createVergleichsDokument(tailoring, erstellungsZeitpunt);

        // assert
        verify(tenentDokumentServiceMock, times(1)).createVergleichsDokument(tailoring, erstellungsZeitpunt);
        assertThat(actual).isPresent();
    }

    @Test
    void createAll_MandantNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        // act
        Collection<Datei> actual = service.createAll(tailoring, erstellungsZeitpunt);

        // assert
        verify(tenentDokumentServiceMock, times(0)).createAll(tailoring, erstellungsZeitpunt);
        assertThat(actual).isEmpty();
    }

    @Test
    void createAll_MandantVorhanden_ErgebnisDesMandantAufrufsZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        Tailoring tailoring = Tailoring.builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        given(tenentDokumentServiceMock.createAll(tailoring, erstellungsZeitpunt))
            .willReturn(Arrays.asList(Datei.builder().build()));

        // act
        Collection<Datei> actual = service.createAll(tailoring, erstellungsZeitpunt);

        // assert
        verify(tenentDokumentServiceMock, times(1)).createAll(tailoring, erstellungsZeitpunt);
        assertThat(actual).hasSize(1);
    }
}
