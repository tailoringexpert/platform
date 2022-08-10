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

import eu.tailoringexpert.domain.Datei;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
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

class KatalogServiceImplTest {

    private KatalogServiceRepository repositoryMock;
    private DokumentService dokumentServiceMock;
    private KatalogServiceImpl service;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(KatalogServiceRepository.class);
        this.dokumentServiceMock = mock(DokumentService.class);
        this.service = new KatalogServiceImpl(repositoryMock, dokumentServiceMock);
    }

    @Test
    void doImport_KatalogWirwUebergeben_ImportWirdDurchgefuehrt() {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder().build();
        given(repositoryMock.createKatalog(eq(katalog), any()))
            .willReturn(of(katalog));

        ZonedDateTime now = ZonedDateTime.of(
            LocalDateTime.of(2020, 12, 1, 8, 0, 0),
            ZoneId.systemDefault()
        );

        // act
        Boolean actual;
        try (MockedStatic<ZonedDateTime> dateTimeMock = mockStatic(ZonedDateTime.class)) {
            dateTimeMock.when(ZonedDateTime::now).thenReturn(now);
            actual = service.doImport(katalog);
            verify(repositoryMock, times(1))
                .createKatalog(katalog, now);
        }

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void doImport_KatalogWirwNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.doImport(null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKatalog_VersionNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getKatalog(null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKatalog_VersionVorhanden_KatalogLadenWirdAufgerufen() {
        // arrange
        given(repositoryMock.getKatalog("8.2.1")).willReturn(of(Katalog.<KatalogAnforderung>builder().build()));

        // act
        service.getKatalog("8.2.1");

        // assert
        verify(repositoryMock, times(1)).getKatalog("8.2.1");
    }

    @Test
    void createKatalog_KatalogNichtVorhanden_EmptyWirdZurueckGegebe() {
        // arrange
        given(repositoryMock.getKatalog(any())).willReturn(Optional.empty());

        // act
        Optional<Datei> actual = service.createKatalog("8.2.1");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getKatalog("8.2.1");
    }

    @Test
    void createKatalog_KatalogVorhanden_DateiWirdZurueckGegebe() {
        // arrange
        given(repositoryMock.getKatalog(any()))
            .willReturn(of(Katalog.<KatalogAnforderung>builder().build()));

        given(dokumentServiceMock.createKatalog(any(), any()))
            .willReturn(of(Datei.builder().build()));

        // act
        Optional<Datei> actual = service.createKatalog("8.2.1");

        // assert
        assertThat(actual).isNotEmpty();
        verify(repositoryMock, times(1)).getKatalog("8.2.1");
        verify(dokumentServiceMock, times(1)).createKatalog(any(), any());
    }
}
