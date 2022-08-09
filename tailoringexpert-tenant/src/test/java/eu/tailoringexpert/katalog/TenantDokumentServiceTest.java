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
package eu.tailoringexpert.katalog;

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.Datei;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

import static java.util.Map.ofEntries;
import static java.util.Optional.of;
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
        this.service = new TenantDokumentService(ofEntries(
            new SimpleEntry("TENANT", tenentDokumentServiceMock)
        ));
    }

    @Test
    void createKatalog_MandantNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        // act
        Optional<Datei> actual = service.createKatalog(katalog, erstellungsZeitpunt);

        // assert
        assertThat(actual).isEmpty();
        verify(tenentDokumentServiceMock, times(0)).createKatalog(katalog, erstellungsZeitpunt);
    }

    @Test
    void createKatalog_MandantVorhanden_ErgebnisDesMandantAufrufsZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder().build();
        LocalDateTime erstellungsZeitpunt = LocalDateTime.now();

        given(tenentDokumentServiceMock.createKatalog(katalog, erstellungsZeitpunt))
            .willReturn(of(Datei.builder().build()));

        // act
        Optional<Datei> actual = service.createKatalog(katalog, erstellungsZeitpunt);

        // assert
        verify(tenentDokumentServiceMock, times(1)).createKatalog(katalog, erstellungsZeitpunt);
        assertThat(actual).isPresent();
    }
}
