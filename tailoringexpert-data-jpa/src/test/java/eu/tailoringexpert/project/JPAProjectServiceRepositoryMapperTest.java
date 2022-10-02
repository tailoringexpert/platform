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
package eu.tailoringexpert.project;

import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetEntity;
import eu.tailoringexpert.domain.SelectionVectorEntity;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringInformation;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.DRDRepository;
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

class JPAProjectServiceRepositoryMapperTest {
    private LogoRepository logoRepositoryMock;
    private BaseCatalogRepository baseCatalogRepositoryMock;
    private DRDRepository drdRepositoryMock;
    private JPAProjectServiceRepositoryMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new JPAProjectServiceRepositoryMapperImpl();

        this.logoRepositoryMock = mock(LogoRepository.class);
        this.mapper.setLogoRepository(logoRepositoryMock);

        this.drdRepositoryMock = mock(DRDRepository.class);
        this.mapper.setDrdRepository(drdRepositoryMock);

        baseCatalogRepositoryMock = mock(BaseCatalogRepository.class);
        this.mapper.setBaseCatalogRepository(baseCatalogRepositoryMock);
    }


    @Test
    void toDomain_ProjectEntityNull_NullReturned() {
        // arrange
        ProjectEntity project = null;

        // act
        Project actual = mapper.toDomain(project);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toDomain_TailoringEntityNull_NullReturned() {
        // arrange
        TailoringEntity tailoring = null;

        // act
        Tailoring actual = mapper.toDomain(tailoring);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toDomain_TailoringEntityWithPhase_TailoringWithPhaseListReturned() {
        // arrange
        TailoringEntity tailoring = TailoringEntity.builder()
            .phases(asList(Phase.ZERO))
            .build();

        // act
        Tailoring actual = mapper.toDomain(tailoring);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPhases()).hasSize(1);
    }

    @Test
    void toEntity_TailoringWithPhase_TailoringntityWithPhaseListReturned() {
        // arrange
        Tailoring tailoring = Tailoring.builder()
            .phases(asList(Phase.ZERO))
            .build();

        // act
        TailoringEntity actual = mapper.toEntity(tailoring);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPhases()).hasSize(1);
    }

    @Test
    void toEntity_TailoringNull_NullReturned() {
        // arrange
        Tailoring tailoring = null;

        // act
        TailoringEntity actual = mapper.toEntity(tailoring);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toDomain_BaseCatalogEntityNull_NullReturned() {
        // arrange
        BaseCatalogEntity katalog = null;

        // act
        Catalog<BaseRequirement> actual = mapper.toDomain(katalog);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void createProjekt_ProjectNull_NullReturned() {
        // arrange
        Project project = null;

        // act
        ProjectEntity actual = mapper.createProject(project);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getTailoringInformationen_TailoringEntityNull_NullReturned() {
        // arrange
        TailoringEntity tailoring = null;

        // act
        TailoringInformation actual = mapper.getProjectInformationen(tailoring);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getTailoringInformationen_TailoringEntityWithPhase_getTailoringInformationenWithPhaseListReturned() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .phases(asList(Phase.ZERO))
            .build();

        // act
        TailoringInformation actual = mapper.getProjectInformationen(projektPhase);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPhases()).hasSize(1);
    }

    @Test
    void getScreeningSheet_ScreeningSheetEntityNull_NullReturned() {
        ScreeningSheetEntity screeningSheet = null;

        // act
        ScreeningSheet actual = mapper.getScreeningSheet(screeningSheet);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getScreeningSheet_ScreeningSheetEntitySelectionVectorAllValuesNull_ScreeningSheetWith0AllSelectionVectorParameterReturned() {
        ScreeningSheetEntity screeningSheet = ScreeningSheetEntity.builder()
            .selectionVector(SelectionVectorEntity.builder().build())
            .build();

        // act
        ScreeningSheet actual = mapper.getScreeningSheet(screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getSelectionVector().getLevel("A")).isZero();
        assertThat(actual.getSelectionVector().getLevel("Q")).isZero();
        assertThat(actual.getSelectionVector().getLevel("E")).isZero();
        assertThat(actual.getSelectionVector().getLevel("P")).isZero();
        assertThat(actual.getSelectionVector().getLevel("R")).isZero();
        assertThat(actual.getSelectionVector().getLevel("S")).isZero();
        assertThat(actual.getSelectionVector().getLevel("W")).isZero();
        assertThat(actual.getSelectionVector().getLevel("O")).isZero();
        assertThat(actual.getSelectionVector().getLevel("M")).isZero();
        assertThat(actual.getSelectionVector().getLevel("G")).isZero();
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
    void resolve_LogoExist_LogoEntityReturned() {
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
    void resolve_BaseCatalogNull_NullReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = null;

        // act
        BaseCatalogEntity actual = mapper.resolve(catalog);

        // assert
        assertThat(actual).isNull();
        verify(baseCatalogRepositoryMock, times(0)).findByVersion(any());
    }

    @Test
    void resolve_BaseCatalogNotNull_BaseCatalogEntityReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().version("8.2.1").build();

        BaseCatalogEntity baseCatalogEntity = BaseCatalogEntity.builder().version("8.2.1").build();
        given(baseCatalogRepositoryMock.findByVersion("8.2.1")).willReturn(baseCatalogEntity);

        // act
        BaseCatalogEntity actual = mapper.resolve(catalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
        verify(baseCatalogRepositoryMock, times(1)).findByVersion("8.2.1");
    }

    @Test
    void resolve_DRDNull_NullReturned() {
        // arrange
        DRD drd = null;

        // act
        DRDEntity actual = mapper.resolve(drd);

        // assert
        assertThat(actual).isNull();
        verify(drdRepositoryMock, times(0)).findByNumber(any());
    }

    @Test
    void resolve_DRDNotNull_DRDEntityReturned() {
        // arrange
        DRD drd = DRD.builder().number("4711").build();

        DRDEntity drdEntity = DRDEntity.builder().number("4711").build();
        given(drdRepositoryMock.findByNumber("4711")).willReturn(drdEntity);

        // act
        DRDEntity actual = mapper.resolve(drd);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getNumber()).isEqualTo("4711");
        verify(drdRepositoryMock, times(1)).findByNumber("4711");
    }
}
