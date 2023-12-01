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
import eu.tailoringexpert.domain.ScreeningSheetEntity;
import eu.tailoringexpert.domain.ScreeningSheetParameterEntity;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringCatalogEntity;
import eu.tailoringexpert.domain.TailoringState;
import eu.tailoringexpert.repository.LogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.util.List.of;
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
        this.logoRepositoryMock = mock(LogoRepository.class);
        this.mapper = new JPATailoringServiceRepositoryMapperGenerated();
        this.mapper.setLogoRepository(logoRepositoryMock);
    }


    @Test
    void toDomain_ProjectEntityNull_NullReturned() {
        // arrange
        ProjectEntity entity = null;

        // act
        Project actual = mapper.toDomain(entity);

        //assert
        assertThat(actual).isNull();
    }

    @Test
    void toDomain_TailoringEntityNull_NullReturned() {
        // arrange
        TailoringEntity entity = null;

        // act
        Tailoring actual = mapper.toDomain(entity);

        //assert
        assertThat(actual).isNull();
    }


    @Test
    void updateTailoring_TailoringNull_EntityNotUpdated() {
        // arrange
        TailoringCatalogEntity catalog = TailoringCatalogEntity.builder().version("NaN").build();
        TailoringEntity entity = new TailoringEntity();
        entity.setCatalog(catalog);

        // act
        mapper.updateTailoring(null, entity);

        // assert
        assertThat(entity.getCatalog()).isEqualTo(catalog);
    }

    @Test
    void updateTailoring_TailoringCatalogNull_EntityCatalogNullUpdated() {
        // arrange
        Tailoring domain = Tailoring.builder().catalog(null).build();

        TailoringCatalogEntity catalog = TailoringCatalogEntity.builder().version("NaN").build();
        TailoringEntity entity = new TailoringEntity();
        entity.setCatalog(catalog);

        // act
        mapper.updateTailoring(domain, entity);

        // assert
        assertThat(entity.getCatalog()).isNull();
    }

    @Test
    void updateTailoring_TailoringNotNull_OnlyCatalogSelectionVectorStateUpdated() {
        // arrange
        Chapter<TailoringRequirement> toc = Chapter.<TailoringRequirement>builder()
            .requirements(of(
                TailoringRequirement.builder()
                    .position("a")
                    .text("Text")
                    .build()
            ))
            .chapters(of(
                Chapter.<TailoringRequirement>builder()
                    .number("1.1")
                    .build()
            ))
            .build();

        Tailoring domain = Tailoring.builder()
            .catalog(Catalog.<TailoringRequirement>builder()
                .version("8.2.1")
                .toc(toc)
                .build())
            .screeningSheet(ScreeningSheet.builder().build())
            .state(TailoringState.AGREED)
            .name("master")
            .selectionVector(SelectionVector.builder()
                .build())
            .build();

        TailoringEntity entity = new TailoringEntity();

        // act
        mapper.updateTailoring(domain, entity);

        //assert
        assertThat(entity.getName()).isNull();
        assertThat(entity.getSelectionVector()).isNotNull();
        assertThat(entity.getState()).isEqualTo(TailoringState.AGREED);
        assertThat(entity.getCatalog().getVersion()).isEqualTo(domain.getCatalog().getVersion());
    }

    @Test
    void resolve_LogoNull_NullReturned() {
        // arrange
        Logo logo = null;

        // act
        LogoEntity actual = mapper.resolve(logo);

        // assert
        assertThat(actual).isNull();
        verify(logoRepositoryMock, times(0)).findByName(any());
    }

    @Test
    void resolve_LogoNotNull_LogoEntityReturned() {
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
    void toScreeningSheetParameters_ProjectNotNull_IdentifierNotNull() {
        // arrange
        ScreeningSheetEntity entity = ScreeningSheetEntity.builder()
            .parameters(of(
                ScreeningSheetParameterEntity.builder()
                    .category(ScreeningSheet.PARAMETER_PROJECT)
                    .value("Sample")
                    .build()
            ))
            .build();

        // act
        ScreeningSheet actual = mapper.toScreeningSheetParameters(entity);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getProject()).isEqualTo("Sample");
    }

    @Test
    void toScreeningSheetParameters_ProjectNull_IdentifierNull() {
        // arrange
        ScreeningSheetEntity entity = ScreeningSheetEntity.builder()
            .parameters(of(
                ScreeningSheetParameterEntity.builder()
                    .category("Project lead")
                    .value("Someone")
                    .build()
            ))
            .build();

        // act
        ScreeningSheet actual = mapper.toScreeningSheetParameters(entity);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getProject()).isNull();
    }

    @Test
    void toScreeningSheetParameters_PhasesNull_PhasesEmptyList() {
        // arrange
        ScreeningSheetEntity entity = ScreeningSheetEntity.builder()
            .parameters(of(
                ScreeningSheetParameterEntity.builder()
                    .category("Project lead")
                    .value("Someone")
                    .build()
            ))
            .build();

        // act
        ScreeningSheet actual = mapper.toScreeningSheetParameters(entity);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPhases()).isEmpty();
    }

    @Test
    void toScreeningSheetParameters_PhasesNotNull_PhasesList() {
        // arrange
        ScreeningSheetEntity entity = ScreeningSheetEntity.builder()
            .parameters(of(
                ScreeningSheetParameterEntity.builder()
                    .category(ScreeningSheet.PARAMETER_PHASE)
                    .value("0")
                    .build(),
                ScreeningSheetParameterEntity.builder()
                    .category(ScreeningSheet.PARAMETER_PHASE)
                    .value("F")
                    .build()
            ))
            .build();

        // act
        ScreeningSheet actual = mapper.toScreeningSheetParameters(entity);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPhases()).hasSize(2);
        assertThat(actual.getPhases()).containsExactly(ZERO, F);
    }

}
