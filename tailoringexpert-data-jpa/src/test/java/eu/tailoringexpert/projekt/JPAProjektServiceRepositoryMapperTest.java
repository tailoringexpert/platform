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
package eu.tailoringexpert.projekt;

import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.KatalogEntity;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Projekt;
import eu.tailoringexpert.domain.ProjektEntity;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetEntity;
import eu.tailoringexpert.domain.SelektionsVektorEntity;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringInformation;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.KatalogRepository;
import eu.tailoringexpert.repository.LogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPAProjektServiceRepositoryMapperTest {
    private LogoRepository logoRepositoryMock;
    private KatalogRepository katalogRepositoryMock;
    private DRDRepository drdRepositoryMock;
    private JPAProjektServiceRepositoryMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new JPAProjektServiceRepositoryMapperImpl();

        this.logoRepositoryMock = mock(LogoRepository.class);
        this.mapper.setLogoRepository(logoRepositoryMock);

        this.drdRepositoryMock = mock(DRDRepository.class);
        this.mapper.setDrdRepository(drdRepositoryMock);

        katalogRepositoryMock = mock(KatalogRepository.class);
        this.mapper.setKatalogRepository(katalogRepositoryMock);
    }


    @Test
    void toDomain_ProjektEntityNull_NullWirdZureuckGegeben() {
        // arrange
        ProjektEntity projekt = null;

        // act
        Projekt actual = mapper.toDomain(projekt);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toDomain_ProjektPhaseEntityNull_NullWirdZureuckGegeben() {
        // arrange
        TailoringEntity projektPhase = null;

        // act
        Tailoring actual = mapper.toDomain(projektPhase);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toDomain_ProjektPhaseEntityMitPhasenVorhanden_ProjektPhaseMitPhasenListeWirdZureuckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .phasen(asList(Phase.ZERO))
            .build();

        // act
        Tailoring actual = mapper.toDomain(projektPhase);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPhasen()).hasSize(1);
    }

    @Test
    void toEntity_ProjektPhaseMitPhasenVorhanden_ProjektPhaseEntityMitPhasenListeWirdZureuckGegeben() {
        // arrange
        Tailoring tailoring = Tailoring.builder()
            .phasen(asList(Phase.ZERO))
            .build();

        // act
        TailoringEntity actual = mapper.toEntity(tailoring);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPhasen()).hasSize(1);
    }

    @Test
    void toEntity_ProjektPhaseNull_NullWirdZureuckGegeben() {
        // arrange
        Tailoring tailoring = null;

        // act
        TailoringEntity actual = mapper.toEntity(tailoring);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toDomain_KatalogDefinitionEntityNull_NullWirdZureuckGegeben() {
        // arrange
        KatalogEntity katalog = null;

        // act
        Katalog<KatalogAnforderung> actual = mapper.toDomain(katalog);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void createProjekt_ProjektNull_NullWirdZurueckGegeben() {
        // arrange
        Projekt projekt = null;

        // act
        ProjektEntity actual = mapper.createProjekt(projekt);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getProjektInformationen_ProjektPhaseEntityNull_NullWirdZurueckGegebeb() {
        // arrange
        TailoringEntity projektPhase = null;

        // act
        TailoringInformation actual = mapper.geTailoringInformationen(projektPhase);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getProjektInformationen_ProjektPhaseEntityPhasenVorhanden_ProjektPhaseInformationMitPhasenListeWirdZurueckGegebeb() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .phasen(asList(Phase.ZERO))
            .build();

        // act
        TailoringInformation actual = mapper.geTailoringInformationen(projektPhase);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPhasen()).hasSize(1);
    }

    @Test
    void getScreeningSheet_ScreeningSheetEntityNull_NullWirdZurueckGegeben() {
        ScreeningSheetEntity screeningSheet = null;

        // act
        ScreeningSheet actual = mapper.getScreeningSheet(screeningSheet);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getScreeningSheet_ScreeningSheetEntitySelektionsVektorAlleWerteNull_ScreeningSheetMit0WertenImSelektionsVektorWirdZurueckGegeben() {
        ScreeningSheetEntity screeningSheet = ScreeningSheetEntity.builder()
            .selektionsVektor(SelektionsVektorEntity.builder().build())
            .build();

        // act
        ScreeningSheet actual = mapper.getScreeningSheet(screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getSelektionsVektor().getLevel("A")).isZero();
        assertThat(actual.getSelektionsVektor().getLevel("Q")).isZero();
        assertThat(actual.getSelektionsVektor().getLevel("E")).isZero();
        assertThat(actual.getSelektionsVektor().getLevel("P")).isZero();
        assertThat(actual.getSelektionsVektor().getLevel("R")).isZero();
        assertThat(actual.getSelektionsVektor().getLevel("S")).isZero();
        assertThat(actual.getSelektionsVektor().getLevel("W")).isZero();
        assertThat(actual.getSelektionsVektor().getLevel("O")).isZero();
        assertThat(actual.getSelektionsVektor().getLevel("M")).isZero();
        assertThat(actual.getSelektionsVektor().getLevel("G")).isZero();
    }

    @Test
    void resolve_NullLogo_NullWirdZurueckgegeben() {
        // arrange
        Logo logo = null;

        // act
        LogoEntity actual = mapper.resolve(logo);

        // assert
        assertThat(actual).isNull();
        verify(logoRepositoryMock, times(0)).findByName(any());
    }

    @Test
    void resolve_LogoUebergeben_LogoEntityWirdZurueckgegeben() {
        // arrange
        Logo logo = Logo.builder().name("ECSS").build();

        LogoEntity logoEntity = LogoEntity.builder().name("ECSS").build();
        given(logoRepositoryMock.findByName("ECSS")).willReturn(logoEntity);

        // act
        LogoEntity actual = mapper.resolve(logo);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo("ECSS");
        verify(logoRepositoryMock, times(1)).findByName("ECSS");
    }

    @Test
    void resolve_NulKatalog_NullWirdZurueckgegeben() {
        // arrange
        Katalog<KatalogAnforderung> katalog = null;

        // act
        KatalogEntity actual = mapper.resolve(katalog);

        // assert
        assertThat(actual).isNull();
        verify(katalogRepositoryMock, times(0)).findByVersion(any());
    }

    @Test
    void resolve_KatalogUebergeben_KatalogEntityWirdZurueckgegeben() {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder().version("8.2.1").build();

        KatalogEntity katalogEntity = KatalogEntity.builder().version("8.2.1").build();
        given(katalogRepositoryMock.findByVersion("8.2.1")).willReturn(katalogEntity);

        // act
        KatalogEntity actual = mapper.resolve(katalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
        verify(katalogRepositoryMock, times(1)).findByVersion("8.2.1");
    }

    @Test
    void resolve_NullDRD_NullWirdZurueckgegeben() {
        // arrange
        DRD drd = null;

        // act
        DRDEntity actual = mapper.resolve(drd);

        // assert
        assertThat(actual).isNull();
        verify(drdRepositoryMock, times(0)).findByNummer(any());
    }

    @Test
    void resolve_DRDUebergeben_DRDEntityWirdZurueckgegeben() {
        // arrange
        DRD drd = DRD.builder().nummer("4711").build();

        DRDEntity drdEntity = DRDEntity.builder().nummer("4711").build();
        given(drdRepositoryMock.findByNummer("4711")).willReturn(drdEntity);

        // act
        DRDEntity actual = mapper.resolve(drd);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getNummer()).isEqualTo("4711");
        verify(drdRepositoryMock, times(1)).findByNummer("4711");
    }
}
