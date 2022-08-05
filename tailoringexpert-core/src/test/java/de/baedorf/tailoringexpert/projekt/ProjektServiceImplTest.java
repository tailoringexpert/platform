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

import de.baedorf.tailoringexpert.domain.Identifikator;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;
import de.baedorf.tailoringexpert.domain.Phase;
import de.baedorf.tailoringexpert.domain.Projekt;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.ScreeningSheetParameter;
import de.baedorf.tailoringexpert.domain.SelektionsVektor;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringStatus;
import de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier;
import de.baedorf.tailoringexpert.screeningsheet.ScreeningSheetService;
import de.baedorf.tailoringexpert.tailoring.TailoringService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

import static de.baedorf.tailoringexpert.domain.Phase.E;
import static de.baedorf.tailoringexpert.domain.Phase.F;
import static de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier.Anwendungscharakter;
import static de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier.Einsatzort;
import static de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier.Einsatzzweck;
import static de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier.Kostenorientierug;
import static de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier.Kuerzel;
import static de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier.Kurzname;
import static de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier.Lebensdauer;
import static de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier.Produkttyp;
import static de.baedorf.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
class ProjektServiceImplTest {

    private ProjektServiceImpl service;

    private TailoringService tailoringServiceMock;
    private ScreeningSheetService screeningSheetServiceMock;
    private ProjektServiceRepository repositoryMock;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(ProjektServiceRepository.class);
        this.tailoringServiceMock = mock(TailoringService.class);
        this.screeningSheetServiceMock = mock(ScreeningSheetService.class);
        this.service = new ProjektServiceImpl(
            repositoryMock,
            screeningSheetServiceMock,
            tailoringServiceMock
        );
    }

    @Test
    void ProjektServiceImpl_ProjektServiceRepositoryNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> new ProjektServiceImpl(
            null,
            mock(ScreeningSheetService.class),
            mock(TailoringService.class)));

        //assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ProjektServiceImpl_ScreeningServiceNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> new ProjektServiceImpl(
            mock(ProjektServiceRepository.class),
            null,
            mock(TailoringService.class)));

        //assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ProjektServiceImpl_ProjektPhaseServiceNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> new ProjektServiceImpl(
            mock(ProjektServiceRepository.class),
            mock(ScreeningSheetService.class),
            null));

        //assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createProject() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .toc(Kapitel.<KatalogAnforderung>builder()
                .kapitel(asList(Kapitel.<KatalogAnforderung>builder()
                    .name("General")
                    .anforderungen(asList(
                        KatalogAnforderung.builder()
                            .text("Die erste Anforderung")
                            .position("a")
                            .identifikatoren(asList(
                                Identifikator.builder()
                                    .typ("Q")
                                    .level(4)
                                    .limitierungen(asList("SAT", "LEO"))
                                    .build()
                            ))
                            .build()))
                    .build()))
                .build())
            .build();
        given(repositoryMock.getKatalog(anyString())).willReturn(katalog);

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

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .data(data)
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(Kurzname.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .selektionsVektor(selektionsVektor)
//            .selektionsVektor(SelektionsVektor.builder()
//                .cybersecurity(1)
//                .eee(2)
//                .planetaryProtection(3)
//                .pmmp(4)
//                .produktsicherung(5)
//                .qualitaetssicherung(6)
//                .safety(7)
//                .software(8)
//                .spaceDebris(9)
//                .zuverlaessigkeit(10)
//                .build())
            .build();
        given(screeningSheetServiceMock.createScreeningSheet(any())).willReturn(screeningSheet);

        given(tailoringServiceMock.createTailoring(any(), any(), eq(screeningSheet), any(), eq(katalog))).willReturn(Tailoring.builder()
            .screeningSheet(screeningSheet)
            .phasen(asList())
            .status(TailoringStatus.ANGELEGT)
            .build());

        given(repositoryMock.createProjekt(eq("1"), any(Projekt.class))).willReturn(Projekt.builder()
            .kuerzel("SAMPLE")
            .tailoring(Tailoring.builder()
                .screeningSheet(screeningSheet)
                .build())
            .build());


        // act
        CreateProjectTO actual = service.createProjekt("1", data, selektionsVektor);

        // assert
        assertThat(actual.getProjekt()).isNotBlank();

        log.info(actual.toString());
    }

    @Test
    void deleteProjekt_ProjektVorhanden_ProjektGeloescht() {
        // arrange
        given(repositoryMock.getProjekt("SAMPLE"))
            .willAnswer(invocation -> of(
                Projekt.builder()
                    .kuerzel(invocation.getArgument(0))
                    .build())
            );
        given(repositoryMock.deleteProjekt("SAMPLE")).willReturn(true);

        // act
        boolean actual = service.deleteProjekt("SAMPLE");

        // assert
        verify(repositoryMock, times(1))
            .deleteProjekt("SAMPLE");
        assertThat(actual).isTrue();
    }

    @Test
    void deleteProjekt_ProjektNichtVorhanden_ProjektNichtGeloescht() {
        // arrange
        given(repositoryMock.getProjekt("SAMPLE")).willReturn(empty());

        // act
        boolean actual = service.deleteProjekt("SAMPLE");

        // assert
        verify(repositoryMock, times(0))
            .deleteProjekt("SAMPLE");
        assertThat(actual).isFalse();
    }

    @Test
    void addProjektPhase_ProjektNichtVorhanden_PhaseWirdNichtHinzugefuegt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProjekt("DUMMY")).willReturn(empty());

        // act
        Optional<Tailoring> actual = service.addTailoring("DUMMY", "8.2.1", data, SelektionsVektor.builder().build());

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0)).getProjekt("8.2.1");
        verify(repositoryMock, times(0)).addTailoring(anyString(), any());
    }

    @Test
    void addProjektPhase_ScreeningSheetFalschesProjekt_PhaseWirdNichtHinzugefuegt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProjekt("DUMMY")).willReturn(of(Projekt.builder().build()));

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung("kuerzel")
                    .wert("ANDERES PROJEKT")
                    .build()
            ))
            .build();

        given(screeningSheetServiceMock.createScreeningSheet(data)).willReturn(screeningSheet);

        // act
        Optional<Tailoring> actual = service.addTailoring("DUMMY", "8.2.1", data, SelektionsVektor.builder().build());

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getKatalog("8.2.1");
        verify(tailoringServiceMock, times(0)).createTailoring(any(), any(), any(), any(), any());
        verify(repositoryMock, times(0)).addTailoring(anyString(), any());
    }

    @Test
    void addProjektPhase_KatalogNichtVorhanden_PhaseWirdNichtHinzugefuegt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProjekt("DUMMY")).willReturn(of(Projekt.builder().build()));
        given(repositoryMock.getKatalog("8.2.1")).willReturn(null);

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung("kuerzel")
                    .wert("ANDERES PROJEKT")
                    .build()
            ))
            .build();

        given(screeningSheetServiceMock.createScreeningSheet(data)).willReturn(screeningSheet);

        // act
        Optional<Tailoring> actual = service.addTailoring("DUMMY", "8.2.1", data, SelektionsVektor.builder().build());

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getKatalog("8.2.1");
        verify(tailoringServiceMock, times(0)).createTailoring(any(), any(), any(), any(), any());
        verify(repositoryMock, times(0)).addTailoring(anyString(), any());
    }

    @Test
    void addProjektPhase_ErstePhase_PhaseMasterErzeugt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProjekt("SAMPLE"))
            .willAnswer(invocation -> of(
                Projekt.builder()
                    .kuerzel(invocation.getArgument(0))
                    .build())
            );

        given(repositoryMock.getKatalog("8.2.1")).willReturn(Katalog.<KatalogAnforderung>builder().build());

        ArgumentCaptor<String> projektPhaseNameCaptor = ArgumentCaptor.forClass(String.class);
        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(ScreeningSheetParameter.builder().bezeichnung(Kuerzel.getName()).wert("SAMPLE").build()))
            .selektionsVektor(SelektionsVektor.builder().build())
            .build();
        given(screeningSheetServiceMock.createScreeningSheet(data)).willReturn(screeningSheet);

        given(tailoringServiceMock.createTailoring(projektPhaseNameCaptor.capture(), any(), eq(screeningSheet), any(), any())).willAnswer(invocation ->
            Tailoring.builder()
                .name(invocation.getArgument(0))
                .kennung(invocation.getArgument(1))
                .screeningSheet(invocation.getArgument(2))
                .build()
        );

        given(repositoryMock.addTailoring(eq("SAMPLE"), any(Tailoring.class)))
            .willReturn(of(Tailoring.builder().build()));


        // act
        Optional<Tailoring> actual = service.addTailoring("SAMPLE", "8.2.1", data, screeningSheet.getSelektionsVektor());

        // assert
        verify(repositoryMock, times(1)).getKatalog("8.2.1");
        assertThat(projektPhaseNameCaptor.getValue()).isEqualTo("master");
        assertThat(actual).isPresent();
    }

    @Test
    void addProjektPhase_EinePhaseBereitsVorhanden_PhaseMaster1Erzeugt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProjekt("SAMPLE"))
            .willAnswer(invocation -> of(
                Projekt.builder()
                    .kuerzel(invocation.getArgument(0))
                    .tailorings(asList(
                        Tailoring.builder()
                            .name("master")
                            .kennung("1000")
                            .build()
                    ))
                    .build())
            );
        given(repositoryMock.getKatalog("8.2.1")).willReturn(Katalog.<KatalogAnforderung>builder().build());

        SelektionsVektor selektionsVektor = SelektionsVektor.builder().build();
        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(ScreeningSheetParameter.builder().bezeichnung(Kuerzel.getName()).wert("SAMPLE").build()))
            .selektionsVektor(selektionsVektor)
            .build();
        given(screeningSheetServiceMock.createScreeningSheet(data)).willReturn(screeningSheet);

        ArgumentCaptor<String> projektPhaseNameCaptor = ArgumentCaptor.forClass(String.class);
        given(tailoringServiceMock.createTailoring(projektPhaseNameCaptor.capture(), any(), any(), any(), any())).willAnswer(invocation ->
            Tailoring.builder()
                .name(invocation.getArgument(0))
                .screeningSheet(screeningSheet)
                .zeichnungen(Collections.emptyList())
                .build()
        );

        given(repositoryMock.addTailoring(eq("SAMPLE"), any(Tailoring.class)))
            .willReturn(of(Tailoring.builder().build()));

        // act
        Optional<Tailoring> actual = service.addTailoring("SAMPLE", "8.2.1", data, selektionsVektor);

        // assert
        assertThat(projektPhaseNameCaptor.getValue()).isEqualTo("master1");
        assertThat(actual).isPresent();

    }

    @Test
    void copyProjekt_ProjektNichtVorhanden_ProjektWurdeNichtKopiert() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProjekt("DUMMY")).willReturn(empty());

        // act
        Optional<Projekt> actual = service.copyProjekt("DUMMY", data);

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0)).createProjekt(any());
    }

    @Test
    void copyProjekt_ProjektVorhanden_ProjektWurdeKopiert() throws IOException {
        // arrange
        byte[] data = newInputStream(get("src/test/resources/screeningsheet.pdf")).readAllBytes();

        given(repositoryMock.getProjekt("SAMPLE"))
            .willAnswer(invocation -> of(
                Projekt.builder()
                    .kuerzel(invocation.getArgument(0))
                    .tailorings(asList(
                        Tailoring.builder()
                            .name("master")
                            .phasen(asList(
                                Phase.ZERO,
                                Phase.A
                            ))
                            .build(),
                        Tailoring.builder()
                            .name("master1")
                            .phasen(asList(
                                Phase.B,
                                Phase.C,
                                Phase.D
                            ))
                            .build()
                    ))
                    .build())
            );

        given(screeningSheetServiceMock.createScreeningSheet(data))
            .willReturn(ScreeningSheet.builder()
                .parameters(asList(ScreeningSheetParameter.builder().bezeichnung(Kuerzel.getName()).wert("H3SAT").build()))
                .build());

        ArgumentCaptor<Projekt> projektKopieCaptor = ArgumentCaptor.forClass(Projekt.class);
        given(repositoryMock.createProjekt(projektKopieCaptor.capture()))
            .willAnswer(invocation -> invocation.getArgument(0));

        // act
        Optional<Projekt> actual = service.copyProjekt("SAMPLE", data);

        // assert
        assertThat(actual).isPresent();
        assertThat(projektKopieCaptor.getValue().getTailorings()).hasSize(2);
    }
}
