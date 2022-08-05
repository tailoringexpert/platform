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
package de.baedorf.tailoringexpert.projekt;

import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;
import de.baedorf.tailoringexpert.domain.KatalogEntity;
import de.baedorf.tailoringexpert.domain.Projekt;
import de.baedorf.tailoringexpert.domain.ProjektEntity;
import de.baedorf.tailoringexpert.domain.ProjektInformation;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.ScreeningSheetEntity;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.domain.TailoringEntity;
import de.baedorf.tailoringexpert.repository.KatalogRepository;
import de.baedorf.tailoringexpert.repository.ProjektRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPAProjektServiceRepositoryTest {

    ProjektRepository projektRepositoryMock;
    KatalogRepository katalogDefinitionRepositoryMock;
    JPAProjektServiceRepositoryMapper mapperMock;
    JPAProjektServiceRepository repository;

    @BeforeEach
    void setup() {
        this.projektRepositoryMock = mock(ProjektRepository.class);
        this.katalogDefinitionRepositoryMock = mock(KatalogRepository.class);
        this.mapperMock = mock(JPAProjektServiceRepositoryMapper.class);
        this.repository = new JPAProjektServiceRepository(
            this.mapperMock,
            this.projektRepositoryMock,
            this.katalogDefinitionRepositoryMock
        );
    }

    @Test
    void getKatalog_KatalogVersionNichtVorhanden_NullErgebnis() {
        // arrange
        given(katalogDefinitionRepositoryMock.findByVersion("8.2.1"))
            .willReturn(null);

        given(mapperMock.toDomain((KatalogEntity) null))
            .willReturn(null);

        // act
        Katalog<KatalogAnforderung> actual = repository.getKatalog("8.2.1");

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getKatalog_KatalogVersionVorhanden_KatalogErgebnis() {
        // arrange
        KatalogEntity katalog = KatalogEntity.builder().build();
        given(katalogDefinitionRepositoryMock.findByVersion("8.2.1"))
            .willReturn(katalog);

        given(mapperMock.toDomain(katalog))
            .willReturn(Katalog.<KatalogAnforderung>builder().build());

        // act
        Katalog<KatalogAnforderung> actual = repository.getKatalog("8.2.1");

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void createProjekt_ProjektNichtNull_ProjektAngelegt() {
        // arrange
        Projekt projekt = Projekt.builder().build();
        ProjektEntity projektToSave = ProjektEntity.builder().build();
        given(mapperMock.createProjekt(projekt))
            .willReturn(projektToSave);

        given(mapperMock.toDomain(projektToSave))
            .willReturn(Projekt.builder().build());

        given(projektRepositoryMock.save(projektToSave))
            .willReturn(projektToSave);

        // act
        Projekt actual = repository.createProjekt(projekt);

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void deleteProjekt_ProjektVorhanden_ProjektGeloescht() {
        // arrange
        given(projektRepositoryMock.deleteByKuerzel("SAMPLE"))
            .willReturn(1l);

        // act
        boolean actual = repository.deleteProjekt("SAMPLE");

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void deleteProjekt_ProjektNichtVorhanden_KeinProjektGeloescht() {
        // arrange
        given(projektRepositoryMock.deleteByKuerzel("SAMPLE"))
            .willReturn(0l);

        // act
        boolean actual = repository.deleteProjekt("SAMPLE");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void getProjekt_ProjektNichtVorhanden_EmptyErgebnis() {
        // arrange
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(null);

        given(mapperMock.toDomain((ProjektEntity) null))
            .willReturn(null);

        // act
        Optional<Projekt> actual = repository.getProjekt("SAMPLE");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getProjekt_ProjektVorhanden_ProjektErgebnis() {
        // arrange
        ProjektEntity projektEntity = ProjektEntity.builder().build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projektEntity);

        given(mapperMock.toDomain(projektEntity))
            .willReturn(Projekt.builder().build());

        // act
        Optional<Projekt> actual = repository.getProjekt("SAMPLE");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void addProjektPhase_ProjektVorhanden_PhaseHinzugefuegt() {
        // arrange
        ProjektEntity projektEntity = ProjektEntity.builder().build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projektEntity);

        Tailoring tailoring = Tailoring.builder()
            .katalog(Katalog.<TailoringAnforderung>builder().version("8.2.1").build())
            .build();
        TailoringEntity projektPhaseToAdd = TailoringEntity.builder().build();
        given(mapperMock.toEntity(tailoring))
            .willReturn(projektPhaseToAdd);

        given(mapperMock.toDomain(projektPhaseToAdd))
            .willReturn(Tailoring.builder().build());

        // act
        Optional<Tailoring> actual = repository.addTailoring("SAMPLE", tailoring);

        // assert
        assertThat(actual).isPresent();
        assertThat(projektEntity.getTailorings()).contains(projektPhaseToAdd);
    }

    @Test
    void getProjektInformationen() {
        // arrange
        given(projektRepositoryMock.findAll())
            .willReturn(Arrays.asList(
                    ProjektEntity.builder()
                        .kuerzel("SAMPLE")
                        .build(),
                    ProjektEntity.builder()
                        .kuerzel("H3SAT")
                        .build(),
                    ProjektEntity.builder()
                        .kuerzel("H4SAT")
                        .build()
                )
            );

        given(mapperMock.geTailoringInformationen(any(ProjektEntity.class)))
            .willReturn(ProjektInformation.builder().build());

        // act
        Collection<ProjektInformation> actual = repository.getProjektInformationen();

        // assert
        assertThat(actual).hasSize(3);
    }

    @Test
    void getScreeningSheet_ProjektNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findByKuerzel(anyString()))
            .willReturn(null);

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("DUMMY");

        // assert
        assertThat(actual).isEmpty();
        verify(projektRepositoryMock, times(1)).findByKuerzel("DUMMY");
        verify(mapperMock, times(0)).getScreeningSheet(any());
    }

    @Test
    void getScreeningSheet_ProjektVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        ScreeningSheetEntity screeningSheet = ScreeningSheetEntity.builder().build().builder().build();
        ProjektEntity projekt = ProjektEntity.builder()
            .screeningSheet(screeningSheet)
            .build();
        given(projektRepositoryMock.findByKuerzel(anyString()))
            .willReturn(projekt);

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("DUMMY");

        // assert
        assertThat(actual).isEmpty();
        verify(projektRepositoryMock, times(1)).findByKuerzel("DUMMY");
        verify(mapperMock, times(1)).getScreeningSheet(screeningSheet);
    }

    @Test
    void getProjektInformation_KuerzelNull_OptionalEmptyWirdZurueckGegeben() {
        // arrange
        String projekt = null;

        ProjektEntity entity = null;
        given(projektRepositoryMock.findByKuerzel(projekt)).willReturn(entity);
        given(mapperMock.geTailoringInformationen(entity)).willReturn(null);

        // act
        Optional<ProjektInformation> actual = repository.getProjektInformation(projekt);

        // assert
        assertThat(actual).isEmpty();
        verify(projektRepositoryMock, times(1)).findByKuerzel(projekt);
        verify(mapperMock, times(1)).geTailoringInformationen(entity);

    }

    @Test
    void getProjektInformation_KuerzelVorhanden_OptionalWirdZurueckGegeben() {
        // arrange
        String projekt = "DUMMY";

        ProjektEntity entity = ProjektEntity.builder().kuerzel("DUMMY").build();
        given(projektRepositoryMock.findByKuerzel(projekt)).willReturn(entity);

        ProjektInformation projektInformation = ProjektInformation.builder().kuerzel("DUMMY").build();
        given(mapperMock.geTailoringInformationen(entity)).willReturn(projektInformation);

        // act
        Optional<ProjektInformation> actual = repository.getProjektInformation(projekt);

        // assert
        assertThat(actual).isNotEmpty();
        verify(projektRepositoryMock, times(1)).findByKuerzel(projekt);
        verify(mapperMock, times(1)).geTailoringInformationen(entity);

    }

    @Test
    void getScreeningSheetDatei_KuerzelNull_OptionalEmptyWirdZurueckGegeben() {
        // arrange
        String projekt = null;

        ProjektEntity entity = null;
        given(projektRepositoryMock.findByKuerzel(projekt)).willReturn(entity);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetDatei(projekt);

        // assert
        assertThat(actual).isEmpty();
        verify(projektRepositoryMock, times(1)).findByKuerzel(projekt);

    }

    @Test
    void getScreeningSheetDatei_KuerzelVorhanden_OptionalWirdZurueckGegeben() {
        // arrange
        String projekt = "DUMMY";

        ProjektEntity entity = ProjektEntity.builder()
            .kuerzel("DUMMY")
            .screeningSheet(ScreeningSheetEntity.builder()
                .id(4711L)
                .data("Hallo Du".getBytes(UTF_8))
                .build())
            .build();
        given(projektRepositoryMock.findByKuerzel(projekt)).willReturn(entity);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetDatei(projekt);

        // assert
        assertThat(actual).isNotEmpty();
        verify(projektRepositoryMock, times(1)).findByKuerzel(projekt);

    }

}
