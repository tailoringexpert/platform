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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.TailoringStatus.AKTIV;
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

        KatalogVersion katalogVersion = null;

        // act
        KatalogVersionResource actual = mapper.toResource(pathContext, katalogVersion);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResoure_KatalogVersion_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();

        KatalogVersion katalogVersion = new KatalogVersion() {
            @Override
            public String getVersion() {
                return "8.2.1";
            }

            @Override
            public ZonedDateTime getGueltigAb() {
                return ZonedDateTime.now();
            }

            @Override
            public ZonedDateTime getGueltigBis() {
                return null;
            }
        };

        // act
        KatalogVersionResource actual = mapper.toResource(pathContext, katalogVersion);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
        assertThat(actual.getGueltigAb()).isNotNull();
        assertThat(actual.getGueltigBis()).isNull();
        assertThat(actual.getStandard()).isTrue();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/katalog/8.2.1", "self"),
            Link.of("http://localhost/katalog/8.2.1/projekt", "projekt"),
            Link.of("http://localhost/katalog/8.2.1/pdf", "pdf"),
            Link.of("http://localhost/katalog/8.2.1/json", "json")
        );

    }

    @Test
    void toResource_ProjektInformationNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        ProjektInformation projekt = null;

        // act
        ProjektInformationResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_ProjektInformation_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .katalog("8.2.1");

        ProjektInformation projekt = ProjektInformation.builder()
            .kuerzel("SAMPLE")
            .katalogVersion("8.2.1")
            .tailorings(asList(
                TailoringInformation.builder().name("master").build(),
                TailoringInformation.builder().name("master1").build()
            ))
            .build();

        // act
        ProjektInformationResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(projekt.getKuerzel());
        assertThat(actual.getKatalogVersion()).isEqualTo(projekt.getKatalogVersion());
        assertThat(actual.getTailorings()).hasSize(2);
        assertThat(actual.getTailorings()).extracting("name").containsExactlyInAnyOrder("master", "master1");

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE", "self"),
            Link.of("http://localhost/projekt/SAMPLE/selektionsvektor", "selektionsvektor"),
            Link.of("http://localhost/projekt/SAMPLE/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring", "tailoring")
        );
    }

    @Test
    void toResource_ProjektInformationOhneProjektPhaseInformation_DatenMitNullPhasenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .katalog("8.2.1");

        ProjektInformation projekt = ProjektInformation.builder()
            .kuerzel("SAMPLE")
            .katalogVersion("8.2.1")
            .tailorings(null)
            .build();

        // act
        ProjektInformationResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(projekt.getKuerzel());
        assertThat(actual.getKatalogVersion()).isEqualTo(projekt.getKatalogVersion());
        assertThat(actual.getTailorings()).isNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE", "self"),
            Link.of("http://localhost/projekt/SAMPLE/selektionsvektor", "selektionsvektor"),
            Link.of("http://localhost/projekt/SAMPLE/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring", "tailoring")
        );
    }

    @Test
    void toResource_ProjektInformationErstellungsZeitpunktVorhanden_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .katalog("8.2.1");

        ZonedDateTime now = ZonedDateTime.now();
        ProjektInformation projekt = ProjektInformation.builder()
            .kuerzel("SAMPLE")
            .katalogVersion("8.2.1")
            .erstellungsZeitpunkt(now)
            .tailorings(asList(
                TailoringInformation.builder().name("master").build(),
                TailoringInformation.builder().name("master1").build()
            ))
            .build();

        // act
        ProjektInformationResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(projekt.getKuerzel());
        assertThat(actual.getKatalogVersion()).isEqualTo(projekt.getKatalogVersion());
        assertThat(actual.getErstellungsZeitpunkt()).isEqualTo(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(now));
        assertThat(actual.getTailorings()).hasSize(2);
        assertThat(actual.getTailorings()).extracting("name").containsExactlyInAnyOrder("master", "master1");

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE", "self"),
            Link.of("http://localhost/projekt/SAMPLE/selektionsvektor", "selektionsvektor"),
            Link.of("http://localhost/projekt/SAMPLE/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring", "tailoring")
        );
    }

    @Test
    void toResource_ProjektPhaseInformationNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        TailoringInformation projektPhase = null;

        // act
        TailoringInformationResource actual = mapper.toResource(pathContext, projektPhase);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_ProjektPhaseInformation_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master");

        TailoringInformation projektPhase = TailoringInformation.builder()
            .name("master")
            .katalogVersion("8.2.1")
            .phasen(asList(A, C))
            .build();

        // act
        TailoringInformationResource actual = mapper.toResource(pathContext, projektPhase);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo("master");
        assertThat(actual.getPhasen()).containsExactlyInAnyOrderElementsOf(asList(A, C));

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master", "self"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/selektionsvektor", "selektionsvektor"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/zeichnung", "zeichnung"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/dokument", "dokument"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/dokument/katalog", "katalogdokument"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/dokument/vergleich", "vergleich"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/katalog", "katalog"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/name", "name"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/anforderungen/import", "import"),
            Link.of("http://localhost/katalog/8.2.1/pdf", "katalogdefinitiondokument")
        );
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
            .projekt("SAMPLE");

        ScreeningSheetParameter parameter = ScreeningSheetParameter.builder()
            .bezeichnung("Kuerzel")
            .wert("SAMPLE")
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .selektionsVektor(SelektionsVektor.builder().build())
            .parameters(asList(parameter))
            .build();

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getData()).isNull();
        assertThat(actual.getSelektionsVektor()).isNotNull();
        assertThat(actual.getParameters()).isNotNull();
        assertThat(actual.getParameters()).hasSize(1);
        assertThat(actual.getParameters()).containsOnly(ScreeningSheetParameterResource.builder()
            .bezeichnung(parameter.getBezeichnung())
            .wert(parameter.getWert())
            .build());

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/screeningsheet", "self"),
            Link.of("http://localhost/projekt/SAMPLE/screeningsheet/pdf", "datei")

        );
    }

    @Test
    void toResource_ProjektScreeningSheetScreeningSheetParameterNull_ProjektScreeningParameterNullDatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE");

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .selektionsVektor(SelektionsVektor.builder().build())
            .parameters(null)
            .build();

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getData()).isNull();
        assertThat(actual.getSelektionsVektor()).isNotNull();
        assertThat(actual.getParameters()).isNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/screeningsheet", "self"),
            Link.of("http://localhost/projekt/SAMPLE/screeningsheet/pdf", "datei")

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
            .projekt("SAMPLE");


        ScreeningSheetParameter parameter = ScreeningSheetParameter.builder()
            .bezeichnung("Kuerzel")
            .wert("SAMPLE")
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .selektionsVektor(SelektionsVektor.builder().build())
            .parameters(asList(parameter))
            .data(data)
            .build();

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getSelektionsVektor()).isNotNull();
        assertThat(actual.getParameters()).isNotNull();
        assertThat(actual.getParameters()).hasSize(1);
        assertThat(actual.getParameters()).containsOnly(ScreeningSheetParameterResource.builder()
            .bezeichnung(parameter.getBezeichnung())
            .wert(parameter.getWert())
            .build());
        assertThat(actual.getData()).isEqualTo(data);

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/screeningsheet", "self"),
            Link.of("http://localhost/projekt/SAMPLE/screeningsheet/pdf", "datei")

        );
    }

    @Test
    void toResource_ProjektPhaseScreeningSheet_ProjektPhaseScreeningDatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master");

        ScreeningSheetParameter parameter = ScreeningSheetParameter.builder()
            .bezeichnung("Kuerzel")
            .wert("SAMPLE")
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .selektionsVektor(SelektionsVektor.builder().build())
            .parameters(asList(parameter))
            .build();

        // act
        ScreeningSheetResource actual = mapper.toResource(pathContext, screeningSheet);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getData()).isNull();
        assertThat(actual.getSelektionsVektor()).isNotNull();
        assertThat(actual.getParameters()).isNotNull();
        assertThat(actual.getParameters()).hasSize(1);
        assertThat(actual.getParameters()).containsOnly(ScreeningSheetParameterResource.builder()
            .bezeichnung(parameter.getBezeichnung())
            .wert(parameter.getWert())
            .build());

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/screeningsheet", "self"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/screeningsheet/pdf", "datei")

        );
    }


    @Test
    void toResource_ProjektNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        Projekt projekt = null;

        // act
        ProjektResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNull();
    }


    @Test
    void toResource_Projekt_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE");

        Projekt projekt = Projekt.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                Tailoring.builder()
                    .name("master")
                    .katalog(Katalog.<TailoringAnforderung>builder()
                        .toc(Kapitel.<TailoringAnforderung>builder()
                            .nummer("1")
                            .build())
                        .build())
                    .build()
            ))
            .build();


        // act
        ProjektResource actual = mapper.toResource(pathContext, projekt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(projekt.getKuerzel());
        assertThat(actual.getTailorings()).hasSize(1);

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE", "self"),
            Link.of("http://localhost/projekt/SAMPLE", "copy")
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
    void toResource_ProjektPhase_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master");

        Tailoring tailoring = Tailoring.builder()
            .name("master")
            .status(AKTIV)
            .phasen(asList(A, C))
            .katalog(Katalog.<TailoringAnforderung>builder()
                .toc(Kapitel.<TailoringAnforderung>builder().build())
                .build())
            .build();


        // act
        TailoringResource actual = mapper.toResource(pathContext, tailoring);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo("master");
        assertThat(actual.getPhasen()).containsExactlyInAnyOrderElementsOf(asList(A, C));
        assertThat(actual.getStatus()).isEqualTo(AKTIV);
        assertThat(actual.getKatalog()).isNotNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master", "self"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/screeningsheet", "screeningsheet"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/selektionsvektor", "selektionsvektor"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/zeichnung", "zeichnung"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/dokument", "dokument"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/katalog", "katalog"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/name", "name")
        );
    }

    @Test
    void toResource_ProjektPhaseAnforderungNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        TailoringAnforderung anforderung = null;

        // act
        TailoringAnforderungResource actual = mapper.toResource(pathContext, anforderung);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_ProjektPhaseAnforderung_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master")
            .kapitel("1.4");

        TailoringAnforderung anforderung = TailoringAnforderung.builder()
            .position("c")
            .text("Dies ist eine Testanforderung")
            .ausgewaehlt(TRUE)
            .referenz(Referenz.builder().text("Eine Referenz").build())
            .build();

        // act
        TailoringAnforderungResource actual = mapper.toResource(pathContext, anforderung);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getPosition()).isEqualTo(anforderung.getPosition());
        assertThat(actual.getText()).isEqualTo(anforderung.getText());
        assertThat(actual.getAusgewaehlt()).isTrue();
        assertThat(actual.getGeaendert()).isFalse();
        assertThat(actual.getReferenz()).isEqualTo("Eine Referenz");

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/katalog/1.4/c", "self"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/katalog/1.4/c/ausgewaehlt/false", "ausgewaehlt"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/katalog/1.4/c/text", "text")
        );
    }

    @Test
    void toResource_DokumentZeichnungNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        DokumentZeichnung dokumentZeichnung = null;

        // act
        DokumentZeichnungResource actual = mapper.toResource(pathContext, dokumentZeichnung);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_DokumentZeichnung_DatenUndLinksOk() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master");

        DokumentZeichnung dokumentZeichnung = DokumentZeichnung.builder()
            .bereich("Software")
            .unterzeichner("Hans Dampf")
            .status(DokumentZeichnungStatus.AGREED)
            .anwendbar(TRUE)
            .build();

        // act
        DokumentZeichnungResource actual = mapper.toResource(pathContext, dokumentZeichnung);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getBereich()).isEqualTo(dokumentZeichnung.getBereich());
        assertThat(actual.getUnterzeichner()).isEqualTo(dokumentZeichnung.getUnterzeichner());
        assertThat(actual.getStatus()).isEqualTo(dokumentZeichnung.getStatus());
        assertThat(actual.getAnwendbar()).isEqualTo(dokumentZeichnung.getAnwendbar());

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/zeichnung/Software", "self")
        );
    }

    @Test
    void toResource_AnforderungGruppeNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        Kapitel<TailoringAnforderung> gruppe = null;

        // act
        TailoringKatalogKapitelResource actual = mapper.toResource(pathContext, gruppe);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_AnforderungGruppe_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master")
            .kapitel("1.1");

        Kapitel<TailoringAnforderung> gruppe = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1")
            .build();

        // act
        TailoringKatalogKapitelResource actual = mapper.toResource(pathContext, gruppe);

        // assert
        assertThat(actual).isNotNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/katalog/1.1", "self"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/katalog/1.1/anforderung", "anforderungen"),
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/katalog/1.1/ausgewaehlt/{ausgewaehlt}", "selektion")
        );
    }

    @Test
    void toResource_SelektionsVektorNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        SelektionsVektor selektionsVektor = null;

        // act
        SelektionsVektorResource actual = mapper.toResource(pathContext, selektionsVektor);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_ProjektSelektionsVektor_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE");

        SelektionsVektor selektionsVektor = SelektionsVektor.builder()
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
        SelektionsVektorResource actual = mapper.toResource(pathContext, selektionsVektor);

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
            Link.of("http://localhost/projekt/SAMPLE/selektionsvektor", "self")
        );
    }

    @Test
    void toResource_ProjektPhaseSelektionsVektor_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master");

        SelektionsVektor selektionsVektor = SelektionsVektor.builder()
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
        SelektionsVektorResource actual = mapper.toResource(pathContext, selektionsVektor);

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
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/selektionsvektor", "self")
        );
    }

    @Test
    void toResource_KatalogNull_NullWirdZurueckGegeben() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();
        Katalog<TailoringAnforderung> domain = null;

        // act
        KatalogResource actual = mapper.toResource(pathContext, domain);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toResource_Katalog_DatenUndLinksOK() {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master");

        Katalog<TailoringAnforderung> domain = Katalog.<TailoringAnforderung>builder()
            .toc(Kapitel.<TailoringAnforderung>builder().build())
            .build();

        // act
        KatalogResource actual = mapper.toResource(pathContext, domain);

        // assert
        assertThat(actual).isNotNull();

        assertThat(actual.getLinks()).containsExactlyInAnyOrder(
            Link.of("http://localhost/projekt/SAMPLE/tailoring/master/katalog", "self")
        );
    }

}
