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

import eu.tailoringexpert.domain.Identifikator;
import eu.tailoringexpert.domain.Kapitel;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelektionsVektor;
import eu.tailoringexpert.domain.TailoringAnforderung;
import eu.tailoringexpert.domain.TailoringAnforderung.TailoringAnforderungBuilder;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Log4j2
class TailoringServiceMapperTest {

    TailoringServiceMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new TailoringServiceMapperImpl();
    }

    @Test
    void convert_nullPhasenEinschraenkung_AnforderungAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtung")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();

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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isTrue();
    }


    @Test
    void convert_leeePhasenEinschraenkung_AnforderungAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(emptyList())
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtung")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();

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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isTrue();
    }

    @Test
    void convert_erfuelltePhasenEinschraenkung_AnforderungAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(asList(B, F))
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();

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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isTrue();
    }

    @Test
    void convert_erfuelltePhasenEinschraenkungFalscherIdenitifaktor_AnforderungNichtSAsugewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(asList(B, F))
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(asList("SAT"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT1")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(B))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();

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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isFalse();
    }


    @Test
    void convert_erfuelltePhasenEinschraenkungRichtigerIdenitifaktor_AnforderungAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(asList(B, F))
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(asList("SAT"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();


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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isTrue();
    }

    @Test
    void convert_erfuelltePhasenEinschraenkungIdenitifaktoren_AnforderungAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(asList(B, F))
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(4)
                    .limitierungen(asList("SAT", "LEO"))
                    .build(),
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(asList("SAT"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();


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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isTrue();
    }


    @Test
    void convert_nichtErfuelltePhasenEinschraenkung_AnforderungNichtAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(asList(B, F))
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(ZERO))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();


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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isFalse();
    }

    @Test
    void convert_keineLimitierungLevelErfuellt_AnforderungAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(emptyList())
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();


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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isTrue();
    }

    @Test
    void convert_LimitierungNullLevelErfuellt_AnforderungNichtAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(emptyList())
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(null)
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();


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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isTrue();
    }

    @Test
    void convert_LimitierungLeerLevelErfuellt_AnforderungNichtAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(emptyList())
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(emptyList())
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();


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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isTrue();
    }

    @Test
    void convert_LimitierungLeerLevelKleiner_AnforderungAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(emptyList())
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(emptyList())
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();


        SelektionsVektor selektionsVektor = SelektionsVektor.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 5)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isFalse();
    }

    @Test
    void toProjektKatalog() {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .version("8.2.1")
            .toc(Kapitel.<KatalogAnforderung>builder()
                .anforderungen(asList(
                    KatalogAnforderung.builder()
                        .phasen(asList(ZERO, A))
                        .position("a")
                        .text("Anforderung toc a")
                        .identifikatoren(emptyList())
                        .build()
                ))
                .kapitel(asList(
                    Kapitel.<KatalogAnforderung>builder()
                        .name("Gruppe 1")
                        .kapitel(asList(
                            Kapitel.<KatalogAnforderung>builder()
                                .name("Gruppe 1.1")
                                .build()
                        )).build(),
                    Kapitel.<KatalogAnforderung>builder()
                        .name("Gruppe 2")
                        .build()
                ))
                .build())
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();

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
        Katalog<TailoringAnforderung> actual = mapper.toTailoringKatalog(katalog, screeningSheet, selektionsVektor);

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void toProjektPhaseAnforderung_ScreeningSheetOhnePhase_RuntimeExceptionWirdGeworfen() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .limitierungen(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(emptyList())
            .build();

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

        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        Throwable actual = catchThrowable(() -> mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder));

        // assert
        assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    void toProjektKatalog_AnforderungOhneLimitierung_AnforderungWirdAusgewaehlt() {
        // arrange
        KatalogAnforderung requirement = KatalogAnforderung.builder()
            .text("Die erste Anforderung")
            .position("a")
            .phasen(asList(ZERO))
            .identifikatoren(asList(
                Identifikator.builder()
                    .typ("Q")
                    .level(6)
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName())
                    .wert("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .wert("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .wert("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .wert(asList(ZERO))
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .wert("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .wert("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .wert("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .wert("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .bezeichnung(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .wert("erforderlich")
                    .build()
            ))
            .build();


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


        TailoringAnforderungBuilder tailoringAnforderungBuilder = TailoringAnforderung.builder();

        // act
        mapper.toTailoringAnforderung(requirement, screeningSheet, selektionsVektor, tailoringAnforderungBuilder);
        TailoringAnforderung actual = tailoringAnforderungBuilder.build();

        // assert
        assertThat(actual.getAusgewaehlt()).isTrue();
    }

    @Test
    void toProjektKatalog_KatalogNull_NullWirdZurueckGegeben() {
        // arrange

        // act
        Katalog<TailoringAnforderung> actual = mapper.toTailoringKatalog(
            null,
            ScreeningSheet.builder().build(),
            SelektionsVektor.builder().build());

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toProjektKatalog_KeineAnforderungGruppeVorhanden_KatalogMitNullGruppenZurueckGegeben() {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .toc(Kapitel.<KatalogAnforderung>builder().build())
            .build();

        // act
        Katalog<TailoringAnforderung> actual = mapper.toTailoringKatalog(
            katalog,
            ScreeningSheet.builder().build(),
            SelektionsVektor.builder().build()
        );

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc().getKapitel()).isNull();
    }

    @Test
    void toProjektKatalog_KeineAnforderngVorhanden_KatalogMitNullAnforderungListeZurueckGegeben() {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .toc(Kapitel.<KatalogAnforderung>builder().build())
            .build();

        // act
        Katalog<TailoringAnforderung> actual = mapper.toTailoringKatalog(
            katalog,
            ScreeningSheet.builder().build(),
            SelektionsVektor.builder().build()
        );

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc().getAnforderungen()).isNull();
    }
}
