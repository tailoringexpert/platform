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
package eu.tailoringexpert.domain;

import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.TailoringState.ACTIVE;
import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class ResourceMapperTest {

    private ResourceMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new ResourceMapperImpl();

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest())
        );
    }

    @Test
    void toResoure_KatalogVersionNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();

        BaseCatalogVersion baseCatalogVersion = null;

        // act
        BaseCatalogVersionResource actual = mapper.toResource(pathContext, baseCatalogVersion);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResoure_KatalogVersion_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();

        BaseCatalogVersion baseCatalogVersion = new BaseCatalogVersion() {
            @Override
            public String getVersion() {
                return "8.2.1";
            }

            @Override
            public ZonedDateTime getValidFrom() {
                return ZonedDateTime.now();
            }

            @Override
            public ZonedDateTime getValidUntil() {
                return null;
            }
        };

        // act
        BaseCatalogVersionResource actual = mapper.toResource(pathContext, baseCatalogVersion);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
        assertThat(actual.getValidFrom()).isNotNull();
        assertThat(actual.getValidUntil()).isNull();
        assertThat(actual.getStandard()).isTrue();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/catalog/8.2.1", "self"),
            Link.of("http://localhost/catalog/8.2.1/project", "project"),
            Link.of("http://localhost/catalog/8.2.1/pdf", "pdf"),
            Link.of("http://localhost/catalog/8.2.1/json", "json")
        );

    }

    @Test
    void toResource_ProjektInformationNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        ProjectInformation projekt = null;

        // act
        ProjectResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_ProjektInformation_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .catalog("8.2.1");

        ProjectInformation projekt = ProjectInformation.builder()
            .identifier("SAMPLE")
            .catalogVersion("8.2.1")
            .tailorings(asList(
                TailoringInformation.builder().name("master").build(),
                TailoringInformation.builder().name("master1").build()
            ))
            .build();

        // act
        ProjectResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(projekt.getIdentifier());
        assertThat(actual.getCatalogVersion()).isEqualTo(projekt.getCatalogVersion());
        assertThat(actual.getTailorings()).hasSize(2);
        assertThat(actual.getTailorings()).extracting("name").containsExactlyInAnyOrder("master", "master1");

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE", "self"),
            Link.of("http://localhost/project/SAMPLE/selectionvector", "selectionvector"),
            Link.of("http://localhost/project/SAMPLE/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/project/SAMPLE/tailoring", "tailoring")
        );
    }

    @Test
    void toResource_ProjektInformationOhneProjektPhaseInformation_DatenMitNullPhasenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .catalog("8.2.1");

        ProjectInformation projekt = ProjectInformation.builder()
            .identifier("SAMPLE")
            .catalogVersion("8.2.1")
            .tailorings(null)
            .build();

        // act
        ProjectResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(projekt.getIdentifier());
        assertThat(actual.getCatalogVersion()).isEqualTo(projekt.getCatalogVersion());
        assertThat(actual.getTailorings()).isNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE", "self"),
            Link.of("http://localhost/project/SAMPLE/selectionvector", "selectionvector"),
            Link.of("http://localhost/project/SAMPLE/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/project/SAMPLE/tailoring", "tailoring")
        );
    }

    @Test
    void toResource_ProjectInformationCreationTimestampAvailable_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .catalog("8.2.1");

        ZonedDateTime now = ZonedDateTime.now();
        ProjectInformation projekt = ProjectInformation.builder()
            .identifier("SAMPLE")
            .catalogVersion("8.2.1")
            .creationTimestamp(now)
            .tailorings(asList(
                TailoringInformation.builder().name("master").build(),
                TailoringInformation.builder().name("master1").build()
            ))
            .build();

        // act
        ProjectResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(projekt.getIdentifier());
        assertThat(actual.getCatalogVersion()).isEqualTo(projekt.getCatalogVersion());
        assertThat(actual.getCreationTimestamp()).isEqualTo(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(now));
        assertThat(actual.getTailorings()).hasSize(2);
        assertThat(actual.getTailorings()).extracting("name").containsExactlyInAnyOrder("master", "master1");

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE", "self"),
            Link.of("http://localhost/project/SAMPLE/selectionvector", "selectionvector"),
            Link.of("http://localhost/project/SAMPLE/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/project/SAMPLE/tailoring", "tailoring")
        );
    }

    @Test
    void toResource_ProjektPhaseInformationNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        TailoringInformation projektPhase = null;

        // act
        TailoringResource actual = mapper.toResource(pathContext, projektPhase);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_TailoringInformation_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master");

        TailoringInformation projektPhase = TailoringInformation.builder()
            .name("master")
            .catalogVersion("8.2.1")
            .phases(asList(A, C))
            .build();

        // act
        TailoringResource actual = mapper.toResource(pathContext, projektPhase);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo("master");
        assertThat(actual.getPhases()).containsExactlyInAnyOrderElementsOf(asList(A, C));

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master", "self"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/selectionvector", "selectionvector"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/signature", "signature"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/document", "document"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/document/catalog", "tailoringcatalog"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/compare", "compare"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/catalog", "catalog"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/name", "name"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/requirement/import", "import"),
            Link.of("http://localhost/catalog/8.2.1/pdf", "basecatalog"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/attachment", "attachment"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/note", "note")
        );
    }

    @Test
    void toResource_ScreeningSheet_NoLinksReturned() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .build();

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getLinks()).isEmpty();
    }

    @Test
    void toResource_ProjektScreeningSheetNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        ScreeningSheet screeningSheet = null;

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_ProjektScreeningSheet_ProjektScreeningDatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE");

        ScreeningSheetParameter parameter = ScreeningSheetParameter.builder()
            .category("Identifier")
            .value("SAMPLE")
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .selectionVector(SelectionVector.builder().build())
            .parameters(asList(parameter))
            .build();

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getData()).isNull();
        assertThat(actual.getSelectionVector()).isNotNull();
        assertThat(actual.getParameters()).isNotNull();
        assertThat(actual.getParameters()).hasSize(1);
        assertThat(actual.getParameters()).containsOnly(ScreeningSheetParameterResource.builder()
            .label(parameter.getCategory())
            .value(parameter.getValue())
            .build());

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/screeningsheet", "self"),
            Link.of("http://localhost/project/SAMPLE/screeningsheet/pdf", "datei")

        );
    }

    @Test
    void toResource_ProjektScreeningSheetScreeningSheetParameterNull_ProjektScreeningParameterNullDatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE");

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .selectionVector(SelectionVector.builder().build())
            .parameters(null)
            .build();

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getData()).isNull();
        assertThat(actual.getSelectionVector()).isNotNull();
        assertThat(actual.getParameters()).isNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/screeningsheet", "self"),
            Link.of("http://localhost/project/SAMPLE/screeningsheet/pdf", "datei")

        );
    }

    @Test
    void toResource_ProjektScreeningSheetPDFDatenVorhanden_ProjektScreeningDatenUndLinksOk() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE");


        ScreeningSheetParameter parameter = ScreeningSheetParameter.builder()
            .category("Identifier")
            .value("SAMPLE")
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .selectionVector(SelectionVector.builder().build())
            .parameters(asList(parameter))
            .data(data)
            .build();

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getSelectionVector()).isNotNull();
        assertThat(actual.getParameters()).isNotNull();
        assertThat(actual.getParameters()).hasSize(1);
        assertThat(actual.getParameters()).containsOnly(ScreeningSheetParameterResource.builder()
            .label(parameter.getCategory())
            .value(parameter.getValue())
            .build());
        assertThat(actual.getData()).isEqualTo(data);

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/screeningsheet", "self"),
            Link.of("http://localhost/project/SAMPLE/screeningsheet/pdf", "datei")

        );
    }

    @Test
    void toResource_ProjektPhaseScreeningSheet_ProjektPhaseScreeningDatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master");

        ScreeningSheetParameter parameter = ScreeningSheetParameter.builder()
            .category("Identifier")
            .value("SAMPLE")
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .selectionVector(SelectionVector.builder().build())
            .parameters(asList(parameter))
            .build();

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getData()).isNull();
        assertThat(actual.getSelectionVector()).isNotNull();
        assertThat(actual.getParameters()).isNotNull();
        assertThat(actual.getParameters()).hasSize(1);
        assertThat(actual.getParameters()).containsOnly(ScreeningSheetParameterResource.builder()
            .label(parameter.getCategory())
            .value(parameter.getValue())
            .build());

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master/screeningsheet", "self"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/screeningsheet/pdf", "datei")

        );
    }


    @Test
    void toResource_ProjektNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        Project project = null;

        // act
        ProjectResource actual = mapper.toResource(pathContext, project);

        // assert
        assertThat(actual).isNull();
    }


    @Test
    void toResource_Projekt_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE");

        Project project = Project.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                Tailoring.builder()
                    .name("master")
                    .catalog(Catalog.<TailoringRequirement>builder()
                        .toc(Chapter.<TailoringRequirement>builder()
                            .number("1")
                            .build())
                        .build())
                    .build()
            ))
            .build();


        // act
        ProjectResource actual = mapper.toResource(pathContext, project);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(project.getIdentifier());
        assertThat(actual.getTailorings()).hasSize(1);

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE", "self"),
            Link.of("http://localhost/project/SAMPLE/selectionvector", "selectionvector"),
            Link.of("http://localhost/project/SAMPLE/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/project/SAMPLE/tailoring", "tailoring")
        );
    }

    @Test
    void toResource_ProjektPhaseNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        Tailoring tailoring = null;

        // act
        TailoringResource actual = mapper.toResource(pathContext, tailoring);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_Tailoring_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master");

        Tailoring tailoring = Tailoring.builder()
            .name("master")
            .state(ACTIVE)
            .phases(asList(A, C))
            .catalog(Catalog.<TailoringRequirement>builder()
                .version("8.2.1")
                .toc(Chapter.<TailoringRequirement>builder().build())
                .build())
            .build();


        // act
        TailoringResource actual = mapper.toResource(pathContext, tailoring);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo("master");
        assertThat(actual.getPhases()).containsExactlyInAnyOrderElementsOf(asList(A, C));
        assertThat(actual.getCatalogVersion()).isNotNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master", "self"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/selectionvector", "selectionvector"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/signature", "signature"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/document", "document"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/catalog", "catalog"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/name", "name"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/attachment", "attachment"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/document/catalog", "tailoringcatalog"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/compare", "compare"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/requirement/import", "import"),
            Link.of("http://localhost/catalog/8.2.1/pdf", "basecatalog"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/note", "note")
        );
    }

    @Test
    void toResource_ProjektPhaseAnforderungNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        TailoringRequirement anforderung = null;

        // act
        TailoringRequirementResource actual = mapper.toResource(pathContext, anforderung);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_TailoringRequirement_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master")
            .chapter("1.4");

        TailoringRequirement anforderung = TailoringRequirement.builder()
            .position("c")
            .text("Dies ist eine Testanforderung")
            .selected(TRUE)
            .reference(Reference.builder().text("Eine Reference").build())
            .build();

        // act
        TailoringRequirementResource actual = mapper.toResource(pathContext, anforderung);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPosition()).isEqualTo(anforderung.getPosition());
        assertThat(actual.getText()).isEqualTo(anforderung.getText());
        assertThat(actual.getSelected()).isTrue();
        assertThat(actual.getChanged()).isFalse();
        assertThat(actual.getReference()).isEqualTo("Eine Reference");

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master/catalog/1.4/c", "self"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/catalog/1.4/c/selected/false", "selected"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/catalog/1.4/c/text", "text")
        );
    }

    @Test
    void toResource_DokumentZeichnungNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        DocumentSignature documentSignature = null;

        // act
        DocumentSignatureResource actual = mapper.toResource(pathContext, documentSignature);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_DocumentSignature_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master");

        DocumentSignature documentSignature = DocumentSignature.builder()
            .faculty("Software")
            .signee("Hans Dampf")
            .state(DocumentSignatureState.AGREED)
            .applicable(TRUE)
            .build();

        // act
        DocumentSignatureResource actual = mapper.toResource(pathContext, documentSignature);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getFaculty()).isEqualTo(documentSignature.getFaculty());
        assertThat(actual.getSignee()).isEqualTo(documentSignature.getSignee());
        assertThat(actual.getState()).isEqualTo(documentSignature.getState());
        assertThat(actual.getApplicable()).isEqualTo(documentSignature.getApplicable());

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master/signature/Software", "self")
        );
    }

    @Test
    void toResource_AnforderungGruppeNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        Chapter<TailoringRequirement> gruppe = null;

        // act
        TailoringCatalogChapterResource actual = mapper.toResource(pathContext, gruppe);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_AnforderungGruppe_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master")
            .chapter("1.1");

        Chapter<TailoringRequirement> gruppe = Chapter.<TailoringRequirement>builder()
            .number("1.1")
            .build();

        // act
        TailoringCatalogChapterResource actual = mapper.toResource(pathContext, gruppe);

        // assert
        assertThat(actual).isNotNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master/catalog/1.1", "self"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/catalog/1.1/requirement", "requirement"),
            Link.of("http://localhost/project/SAMPLE/tailoring/master/catalog/1.1/selected/{selected}", "selection")
        );
    }

    @Test
    void toResource_SelektionsVektorNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        SelectionVector selectionVector = null;

        // act
        SelectionVectorResource actual = mapper.toResource(pathContext, selectionVector);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_ProjectSelectionVector_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE");

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        // act
        SelectionVectorResource actual = mapper.toResource(pathContext, selectionVector);

        // assert
        assertThat(actual.getLevels()).containsEntry("G", 1);
        assertThat(actual.getLevels()).containsEntry("E", 2);
        assertThat(actual.getLevels()).containsEntry("M", 3);
        assertThat(actual.getLevels()).containsEntry("P", 4);
        assertThat(actual.getLevels()).containsEntry("A", 5);
        assertThat(actual.getLevels()).containsEntry("Q", 6);
        assertThat(actual.getLevels()).containsEntry("S", 7);
        assertThat(actual.getLevels()).containsEntry("W", 8);
        assertThat(actual.getLevels()).containsEntry("O", 9);
        assertThat(actual.getLevels()).containsEntry("R", 10);

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/selectionvector", "self")
        );
    }

    @Test
    void toResource_TailoringSelectionVector_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master");

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        // act
        SelectionVectorResource actual = mapper.toResource(pathContext, selectionVector);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getLevels()).containsEntry("G", 1);
        assertThat(actual.getLevels()).containsEntry("E", 2);
        assertThat(actual.getLevels()).containsEntry("M", 3);
        assertThat(actual.getLevels()).containsEntry("P", 4);
        assertThat(actual.getLevels()).containsEntry("A", 5);
        assertThat(actual.getLevels()).containsEntry("Q", 6);
        assertThat(actual.getLevels()).containsEntry("S", 7);
        assertThat(actual.getLevels()).containsEntry("W", 8);
        assertThat(actual.getLevels()).containsEntry("O", 9);
        assertThat(actual.getLevels()).containsEntry("R", 10);

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master/selectionvector", "self")
        );
    }

    @Test
    void toResource_KatalogNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        Catalog<TailoringRequirement> domain = null;

        // act
        TailoringCatalogResource actual = mapper.toResource(pathContext, domain);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_TailoringCatalog_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master");

        Catalog<TailoringRequirement> domain = Catalog.<TailoringRequirement>builder()
            .toc(Chapter.<TailoringRequirement>builder().build())
            .build();

        // act
        TailoringCatalogResource actual = mapper.toResource(pathContext, domain);

        // assert
        assertThat(actual).isNotNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master/catalog", "self")
        );
    }

    @Test
    void toResource_Note_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master")
            .note("1");

        Note domain = Note.builder()
            .number(1)
            .text("Demotext")
            .creationTimestamp(ZonedDateTime.now())
            .build();

        // act
        NoteResource actual = mapper.toResource(pathContext, domain);

        // assert
        assertThat(actual).isNotNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master/note/1", "self")
        );
    }

    @Test
    void toResource_File_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master")
            .note("1");

        File domain = File.builder()
            .name("demo.pdf")
            .data("demo.pdf".getBytes(StandardCharsets.UTF_8))
            .build();

        // act
        FileResource actual = mapper.toResource(pathContext, domain);

        // assert
        assertThat(actual).isNotNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/project/SAMPLE/tailoring/master/attachment/demo.pdf", "self")
        );
    }
}
