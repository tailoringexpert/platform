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

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringCatalogEntity;
import eu.tailoringexpert.domain.TailoringState;
import eu.tailoringexpert.repository.LogoRepository;
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
        ProjectEntity projekt = null;

        // act
        Project actual = mapper.toDomain(projekt);

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
        TailoringCatalogEntity katalog = TailoringCatalogEntity.builder().version("NaN").build();
        TailoringEntity entity = new TailoringEntity();
        entity.setCatalog(katalog);

        // act
        mapper.addCatalog(null, entity);

        // assert
        assertThat(entity.getCatalog()).isEqualTo(katalog);
    }

    @Test
    void addKatalog_ProjektPhaseKatalogNichtVorhanden_EntityKatalogWordNullGesetzt() {
        // arrange
        Tailoring domain = Tailoring.builder().catalog(null).build();

        TailoringCatalogEntity katalog = TailoringCatalogEntity.builder().version("NaN").build();
        TailoringEntity entity = new TailoringEntity();
        entity.setCatalog(katalog);

        // act
        mapper.addCatalog(domain, entity);

        // assert
        assertThat(entity.getCatalog()).isNull();
    }

    @Test
    void addKatalog_ProjektPhaseEntityNichtVorhanden_NullWirdZurueckGegeben() {
        // arrange
        Tailoring domain = Tailoring.builder()
            .catalog(Catalog.<TailoringRequirement>builder()
                .version("8.2.1")
                .toc(Chapter.<TailoringRequirement>builder()
                    .requirements(Arrays.asList(
                        TailoringRequirement.builder()
                            .position("a")
                            .text("Text")
                            .build()
                    ))
                    .chapters(Arrays.asList(
                        Chapter.<TailoringRequirement>builder()
                            .number("1.1")
                            .build()
                    ))
                    .build())
                .build())
            .screeningSheet(ScreeningSheet.builder().build())
            .state(TailoringState.ACTIVE)
            .name("master")
            .selectionVector(SelectionVector.builder()
                .build())
            .build();

        TailoringEntity entity = new TailoringEntity();

        // act
        mapper.addCatalog(domain, entity);

        //assert
        assertThat(entity.getName()).isNull();
        assertThat(entity.getCatalog().getVersion()).isEqualTo(domain.getCatalog().getVersion());
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
