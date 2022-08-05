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

import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.Logo;
import de.baedorf.tailoringexpert.domain.LogoEntity;
import de.baedorf.tailoringexpert.domain.Projekt;
import de.baedorf.tailoringexpert.domain.ProjektEntity;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.SelektionsVektor;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.domain.TailoringEntity;
import de.baedorf.tailoringexpert.domain.TailoringKatalogEntity;
import de.baedorf.tailoringexpert.domain.TailoringStatus;
import de.baedorf.tailoringexpert.repository.LogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPATailoringServiceRepositoryMapperTest {

    private LogoRepository logoRepositoryMock;
    private JPATailoringServiceRepositoryMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new JPATailoringServiceRepositoryMapperImpl();

        this.logoRepositoryMock = mock(LogoRepository.class);
        this.mapper.setLogoRepository(logoRepositoryMock);
    }


    @Test
    void toDomain_ProjektEntityNichtVorhanden_NullWirdZurueckGegeben() {
        // arrange
        ProjektEntity projekt = null;

        // act
        Projekt actual = mapper.toDomain(projekt);

        //assert
        assertThat(actual).isNull();
    }

    @Test
    void toDomain_ProjektPhaseEntityNichtVorhanden_NullWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = null;

        // act
        Tailoring actual = mapper.toDomain(projektPhase);

        //assert
        assertThat(actual).isNull();
    }


    @Test
    void addKatalog_ProjektPhaseNichtVorhanden_EntityWieVorher() {
        // arrange
        TailoringKatalogEntity katalog = TailoringKatalogEntity.builder().version("NaN").build();
        TailoringEntity entity = new TailoringEntity();
        entity.setKatalog(katalog);

        // act
        mapper.addKatalog(null, entity);

        // assert
        assertThat(entity.getKatalog()).isEqualTo(katalog);
    }

    @Test
    void addKatalog_ProjektPhaseKatalogNichtVorhanden_EntityKatalogWordNullGesetzt() {
        // arrange
        Tailoring domain = Tailoring.builder().katalog(null).build();

        TailoringKatalogEntity katalog = TailoringKatalogEntity.builder().version("NaN").build();
        TailoringEntity entity = new TailoringEntity();
        entity.setKatalog(katalog);

        // act
        mapper.addKatalog(domain, entity);

        // assert
        assertThat(entity.getKatalog()).isNull();
    }

    @Test
    void addKatalog_ProjektPhaseEntityNichtVorhanden_NullWirdZurueckGegeben() {
        // arrange
        Tailoring domain = Tailoring.builder()
            .katalog(Katalog.<TailoringAnforderung>builder()
                .version("8.2.1")
                .toc(Kapitel.<TailoringAnforderung>builder()
                    .anforderungen(Arrays.asList(
                        TailoringAnforderung.builder()
                            .position("a")
                            .text("Text")
                            .build()
                    ))
                    .kapitel(Arrays.asList(
                        Kapitel.<TailoringAnforderung>builder()
                            .nummer("1.1")
                            .build()
                    ))
                    .build())
                .build())
            .screeningSheet(ScreeningSheet.builder().build())
            .status(TailoringStatus.AKTIV)
            .name("master")
            .selektionsVektor(SelektionsVektor.builder()
                .build())
            .build();

        TailoringEntity entity = new TailoringEntity();

        // act
        mapper.addKatalog(domain, entity);

        //assert
        assertThat(entity.getName()).isNull();
        assertThat(entity.getKatalog().getVersion()).isEqualTo(domain.getKatalog().getVersion());
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
}
