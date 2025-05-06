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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.ApplicableDocumentEntity;
import eu.tailoringexpert.domain.BaseCatalogChapterEntity;
import eu.tailoringexpert.domain.BaseCatalogChapterEntity.BaseCatalogChapterEntityBuilder;
import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.BaseRequirementEntity;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.Document;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.repository.ApplicableDocumentRepository;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
class JPACatalogServiceRepositoryMapperTest {

    private LogoRepository logoRepositoryMock;
    private DRDRepository drdRepositoryMock;
    private ApplicableDocumentRepository applicableDocumentRepositoryMock;
    private JPACatalogServiceRepositoryMapper mapper;

    @BeforeEach
    void setup() {
        this.logoRepositoryMock = mock(LogoRepository.class);
        this.drdRepositoryMock = mock(DRDRepository.class);
        this.applicableDocumentRepositoryMock = mock(ApplicableDocumentRepository.class);
        this.mapper = new JPACatalogServiceRepositoryMapperGenerated();
        this.mapper.setLogoRepository(logoRepositoryMock);
        this.mapper.setDrdRepository(drdRepositoryMock);
        this.mapper.setApplicableDocumentRepository(applicableDocumentRepositoryMock);
    }

    @Test
    void createCatalog_CatalogNull_NullReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = null;

        // act
        BaseCatalogEntity actual = mapper.createCatalog(catalog);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void createCatalog_CatalogTocNull_BaseCatalogWithoutTocCreated() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .version("8.2.1")
            .toc(null)
            .build();

        // act
        BaseCatalogEntity actual = mapper.createCatalog(catalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
        assertThat(actual.getValidFrom()).isNotNull();
        assertThat(actual.getToc()).isNull();
    }

    @Test
    void createCatalog_CatalogValid_BaseCatalogEntityCreated() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .version("8.2.1")
            .toc(Chapter.<BaseRequirement>builder()
                .requirements(asList(
                    BaseRequirement.builder()
                        .position("a")
                        .text("Requirement")
                        .phases(asList(Phase.ZERO))
                        .build()
                ))
                .build())
            .build();

        // act
        BaseCatalogEntity actual = mapper.createCatalog(catalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
        assertThat(actual.getValidFrom()).isNotNull();
        assertThat(actual.getToc()).isNotNull();
        assertThat(actual.getToc().getRequirements()).hasSize(1);

    }

    @Test
    void createCatalog_BaseCatalogEntityNull_NullReturned() {
        // arrange
        BaseCatalogEntity katalog = null;

        // act
        Catalog<BaseRequirement> actual = mapper.createCatalog(katalog);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void createCatalog_BaseCatalogNoToc_CatalogWithoutTocCreated() {
        // arrange
        BaseCatalogEntity katalog = BaseCatalogEntity.builder()
            .version("8.2.1")
            .build();

        // act
        Catalog<BaseRequirement> actual = mapper.createCatalog(katalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc()).isNull();
        assertThat(actual.getVersion()).isEqualTo(katalog.getVersion());
    }

    @Test
    void createCatalog_BaseCatalogEntityValid_CatalogCreated() {
        // arrange
        BaseCatalogEntity katalog = BaseCatalogEntity.builder()
            .version("8.2.1")
            .toc(BaseCatalogChapterEntity.builder().build())
            .build();

        // act
        Catalog<BaseRequirement> actual = mapper.createCatalog(katalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc()).isNotNull();
        assertThat(actual.getVersion()).isEqualTo(katalog.getVersion());
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
    void resolve_LogoProvided_LogoEntityReturned() {
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
    void resolve_DRDValid_ExistingDRDReturned() {
        // arrange
        DRD drd = DRD.builder().number("drd-47.11").build();

        DRDEntity drdEntity = DRDEntity.builder().id(12l).build();
        given(drdRepositoryMock.findByNumber("drd-47.11")).willReturn(drdEntity);

        // act
        DRDEntity actual = mapper.resolve(drd);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        verify(drdRepositoryMock, times(1)).findByNumber("drd-47.11");
    }

    @Test
    void resolve_DocumentNull_NullReturned() {
        // arrange
        Document document = null;

        // act
        ApplicableDocumentEntity actual = mapper.resolve(document);

        // assert
        assertThat(actual).isNull();
        verify(applicableDocumentRepositoryMock, times(0)).findByTitleAndIssueAndRevision(any(), any(), any());
    }

    @Test
    void resolve_DocumentValid_ExistingDcoumentReturned() {
        // arrange
        Document document  = Document.builder()
                .title("ECSS-Q-ST-80")
                .issue("C")
                .revision("Rev.1")
                .build();

        ApplicableDocumentEntity documentEntity = ApplicableDocumentEntity.builder().id(12l).build();
        given(applicableDocumentRepositoryMock.findByTitleAndIssueAndRevision("ECSS-Q-ST-80", "C", "Rev.1")).willReturn(documentEntity);

        // act
        ApplicableDocumentEntity actual = mapper.resolve(document);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        verify(applicableDocumentRepositoryMock, times(1)).findByTitleAndIssueAndRevision("ECSS-Q-ST-80", "C", "Rev.1");
    }


    @Test
    void addNumber_ChapterWithRequirements_EntityRequirementsContainsValidNumber() {
        // arrange
        BaseCatalogChapterEntityBuilder builder = BaseCatalogChapterEntity.builder()
            .number("1.2.1")
            .requirements(asList(
                BaseRequirementEntity.builder()
                    .text("Requirement 1")
                    .position("a")
                    .build(),
                BaseRequirementEntity.builder()
                    .text("Requirement 2")
                    .position("b")
                    .build())
            );

        // act
        mapper.addNumber(builder);
        BaseCatalogChapterEntity actual = builder.build();

        // assert
        assertThat(actual.getRequirements())
            .hasSize(2)
            .extracting(BaseRequirementEntity::getPosition, BaseRequirementEntity::getNumber)
            .containsOnly(
                tuple("a", "1.2.1.a"),
                tuple("b", "1.2.1.b")
            );
    }

    @Test
    void createCatalog_ChapterNullRequirements_EntityNullRequirementsReturned() {
        // arrange
        BaseCatalogChapterEntityBuilder builder = BaseCatalogChapterEntity.builder()
            .number("1.2.1")
            .requirements(null);

        // act
        mapper.addNumber(builder);
        BaseCatalogChapterEntity actual = builder.build();

        // assert
        assertThat(actual.getRequirements())
            .isNull();
    }
}
