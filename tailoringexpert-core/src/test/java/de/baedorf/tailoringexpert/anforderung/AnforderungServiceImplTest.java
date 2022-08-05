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
package de.baedorf.tailoringexpert.anforderung;

import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Referenz;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
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
class AnforderungServiceImplTest {

    private AnforderungServiceRepository repositoryMock;

    private AnforderungService service;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(AnforderungServiceRepository.class);
        this.service = new AnforderungServiceImpl(repositoryMock);
    }

    @Test
    void AnforderungServiceImpl_AnforderungServiceRepositoryNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> new AnforderungServiceImpl(null));

        //assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void handleAusgewaehlt_AnforderungOhneBisherigeAuswahlAenderungNeuerAuswahl_AuswahlGeandertAenderungGesezt() {
        // arrange
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringAnforderung.builder()
                    .ausgewaehlt(FALSE)
                    .build()
            )
        );
        given(repositoryMock.updateAnforderung(anyString(), anyString(), anyString(), any(TailoringAnforderung.class)))
            .willAnswer(invocation -> of(invocation.getArgument(3)));


        // act
        Optional<TailoringAnforderung> actual = service.handleAusgewaehlt("SAMPLE", "master", "1.1", "a", TRUE);

        // assert
        verify(repositoryMock, times(1))
            .updateAnforderung(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringAnforderung.class));
        assertThat(actual).isPresent();
        assertThat(actual.get().getAusgewaehlt()).isTrue();
        assertThat(actual.get().getAusgewaehltGeaendert()).isNotNull();
    }

    @Test
    void handleAusgewaehlt_AnforderungNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(empty());

        // act
        Optional<TailoringAnforderung> actual = service.handleAusgewaehlt("SAMPLE", "master", "1.1", "a", TRUE);

        // assert
        verify(repositoryMock, times(0))
            .updateAnforderung(anyString(), anyString(), anyString(), any(TailoringAnforderung.class));
        assertThat(actual).isEmpty();
    }

    @Test
    void handleAusgewaehlt_AnforderungMitBisherigeAuswahlAenderungNeuerAuswahl_AuswahlGeandertAenderungNichtMehrGesetzt() {
        // arrange
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringAnforderung.builder()
                    .ausgewaehlt(FALSE)
                    .ausgewaehltGeaendert(ZonedDateTime.now())
                    .build()
            )
        );
        given(repositoryMock.updateAnforderung(anyString(), anyString(), anyString(), any()))
            .willAnswer(invocation -> of(invocation.getArgument(3)));

        // act
        Optional<TailoringAnforderung> actual = service.handleAusgewaehlt("SAMPLE", "master", "1.1", "a", TRUE);

        // assert
        verify(repositoryMock, times(1))
            .updateAnforderung(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringAnforderung.class));
        assertThat(actual).isPresent();
        assertThat(actual.get().getAusgewaehlt()).isTrue();
        assertThat(actual.get().getAusgewaehltGeaendert()).isNull();
    }

    @Test
    void handleAusgewaehlt_KeineAenderung_KeinUpdate() {
        // arrange
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringAnforderung.builder()
                    .ausgewaehlt(FALSE)
                    .ausgewaehltGeaendert(ZonedDateTime.now())
                    .build()
            )
        );

        // act
        Optional<TailoringAnforderung> actual = service.handleAusgewaehlt("SAMPLE", "master", "1.1", "a", FALSE);

        // assert
        verify(repositoryMock, times(0))
            .updateAnforderung(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringAnforderung.class));
        assertThat(actual).isPresent();
        assertThat(actual.get().getAusgewaehlt()).isFalse();
        assertThat(actual.get().getAusgewaehltGeaendert()).isNotNull();
    }

    @Test
    void handleAusgewaehlt_KapitelNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getKapitel("SAMPLE", "master", "1")).willReturn(empty());

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = service.handleAusgewaehlt("SAMPLE", "master", "1", TRUE);

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0))
            .updateAusgewaehlt(anyString(), anyString(), any(Kapitel.class));
    }

    @Test
    void handleAusgewaehlt_GemischteAusgewaehlt_AnforderungenInKapitelUndUnterkapitelnGeaendertGesetzt() {
        // arrange
        given(repositoryMock.getKapitel("SAMPLE", "master", "1")).willReturn(of(
                Kapitel.<TailoringAnforderung>builder()
                    .nummer("1")
                    .anforderungen(asList(
                        TailoringAnforderung.builder()
                            .text("Anforderung 1")
                            .ausgewaehlt(FALSE)
                            .build()))
                    .kapitel(asList(
                        Kapitel.<TailoringAnforderung>builder()
                            .nummer("1.1")
                            .anforderungen(asList(
                                TailoringAnforderung.builder()
                                    .text("Anforderung 1.1")
                                    .ausgewaehlt(TRUE)
                                    .build()))
                            .kapitel(asList(
                                Kapitel.<TailoringAnforderung>builder()
                                    .nummer("1.1.1")
                                    .anforderungen(asList(
                                        TailoringAnforderung.builder()
                                            .text("Anforderung 1.1.1")
                                            .ausgewaehlt(FALSE)
                                            .build()))
                                    .build()))
                            .build(),
                        Kapitel.<TailoringAnforderung>builder()
                            .nummer("1.2")
                            .anforderungen(asList(TailoringAnforderung.builder()
                                .text("Anforderung 1.2")
                                .ausgewaehlt(TRUE)
                                .build()))
                            .build()))
                    .build()
            )
        );
        given(repositoryMock.updateAusgewaehlt(anyString(), anyString(), any(Kapitel.class)))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = service.handleAusgewaehlt("SAMPLE", "master", "1", TRUE);

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().allAnforderungen()).allMatch(anforderung -> TRUE.equals(anforderung.getAusgewaehlt()));
        assertThat(actual.get().allAnforderungen())
            .extracting(TailoringAnforderung::getAusgewaehltGeaendert)
            .filteredOn(Objects::nonNull)
            .hasSize(2);
    }


    @Test
    void createAnforderung_Anforderung111b_Anforderung111b1WurdeErzeugt() {
        // arrange
        List<TailoringAnforderung> anforderungen = new ArrayList<>();
        anforderungen.add(TailoringAnforderung.builder()
            .position("a")
            .text("Anforderung 1.1.1")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("b")
            .text("Anforderung 1.1.2")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("c")
            .text("Anforderung 1.1.3")
            .ausgewaehlt(FALSE)
            .build());

        Kapitel<TailoringAnforderung> kapitel1_1_1 = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1.1")
            .anforderungen(anforderungen)
            .build();

        given(repositoryMock.getKapitel("SAMPLE", "master", "1.1.1"))
            .willReturn(of(kapitel1_1_1));
        given(repositoryMock.updateKapitel("SAMPLE", "master", kapitel1_1_1))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<TailoringAnforderung> actual = service.createAnforderung(
            "SAMPLE",
            "master",
            "1.1.1",
            "b",
            "Dies ist eine neue Anforderung"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getPosition()).isEqualTo("b1");
        assertThat(anforderungen).hasSize(4);
        assertThat(anforderungen.get(2).getPosition()).isEqualTo("b1");
    }

    @Test
    void createAnforderung_Anforderung111b1_Anforderung111b1WurdeErzeugtAnforderung111b1ZuAnforderung111b2() {
        // arrange
        List<TailoringAnforderung> anforderungen = new ArrayList<>();
        anforderungen.add(TailoringAnforderung.builder()
            .position("a")
            .text("Anforderung 1.1.1")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("b")
            .text("Anforderung 1.1.2")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("b1")
            .text("Anforderung 1.1.2b1")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("c")
            .text("Anforderung 1.1.3")
            .ausgewaehlt(FALSE)
            .build());

        Kapitel<TailoringAnforderung> kapitel1_1_1 = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1.1")
            .anforderungen(anforderungen)
            .build();

        given(repositoryMock.getKapitel("SAMPLE", "master", "1.1.1"))
            .willReturn(of(kapitel1_1_1));
        given(repositoryMock.updateKapitel("SAMPLE", "master", kapitel1_1_1))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<TailoringAnforderung> actual = service.createAnforderung(
            "SAMPLE",
            "master",
            "1.1.1",
            "b",
            "Dies ist eine neue zwischengeschobene neue Anforderung"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getPosition()).isEqualTo("b1");
        assertThat(anforderungen).hasSize(5);
        assertThat(anforderungen.get(3).getPosition()).isEqualTo("b2");
    }

    @Test
    void createAnforderung_Anforderung111b2_Anforderung111b2WurdeErzeugt() {
        // arrange
        List<TailoringAnforderung> anforderungen = new ArrayList<>();
        anforderungen.add(TailoringAnforderung.builder()
            .position("a")
            .text("Anforderung 1.1.1")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("b")
            .text("Anforderung 1.1.2")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("b1")
            .text("Anforderung 1.1.2b1")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("c")
            .text("Anforderung 1.1.3")
            .ausgewaehlt(FALSE)
            .build());

        Kapitel<TailoringAnforderung> kapitel1_1_1 = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1.1")
            .anforderungen(anforderungen)
            .build();

        given(repositoryMock.getKapitel("SAMPLE", "master", "1.1.1"))
            .willReturn(of(kapitel1_1_1));
        given(repositoryMock.updateKapitel("SAMPLE", "master", kapitel1_1_1))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<TailoringAnforderung> actual = service.createAnforderung(
            "SAMPLE",
            "master",
            "1.1.1",
            "b1",
            "Dies ist eine neue 2. neue Anforderung"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getPosition()).isEqualTo("b2");
        assertThat(anforderungen).hasSize(5);
        assertThat(anforderungen.get(2).getPosition()).isEqualTo("b1");
        assertThat(anforderungen.get(3).getPosition()).isEqualTo("b2");
        assertThat(anforderungen.get(3).getText()).isEqualTo("Dies ist eine neue 2. neue Anforderung");
    }

    @Test
    void createAnforderung_KapitelNichtVorhanden_AnforderungWurdeNichtErzeugt() {
        // arrange
        given(repositoryMock.getKapitel("SAMPLE", "master", "1.1.1")).willReturn(empty());

        // act
        Optional<TailoringAnforderung> actual = service.createAnforderung(
            "SAMPLE",
            "master",
            "1.1.1",
            "b1",
            "Dies ist eine neue Anforderung"
        );

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0)).updateKapitel(anyString(), anyString(), any());
    }

    @Test
    void createAnforderung_AnforderungNichtVorhanden_AnforderungWurdeNichtErzeugt() {
        // arrange
        Kapitel<TailoringAnforderung> kapitel = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1.1")
            .anforderungen(Collections.emptyList())
            .build();
        given(repositoryMock.getKapitel("SAMPLE", "master", "1.1.1")).willReturn(of(kapitel));

        // act
        Optional<TailoringAnforderung> actual = service.createAnforderung(
            "SAMPLE",
            "master",
            "1.1.1",
            "b1",
            "Dies ist eine neue Anforderung"
        );

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0)).updateKapitel(anyString(), anyString(), any());
    }

    @Test
    void createAnforderung_Anforderung111b1_Anforderung111b2WurdeErzeugt() {
        // arrange
        List<TailoringAnforderung> anforderungen = new ArrayList<>();
        anforderungen.add(TailoringAnforderung.builder()
            .position("a")
            .text("Anforderung 1.1.1")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("b")
            .text("Anforderung 1.1.2")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("b1")
            .text("Anforderung 1.1.2b1")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("c")
            .text("Anforderung 1.1.3")
            .ausgewaehlt(FALSE)
            .build());

        Kapitel<TailoringAnforderung> kapitel1_1_1 = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1.1")
            .anforderungen(anforderungen)
            .build();

        given(repositoryMock.getKapitel("SAMPLE", "master", "1.1.1"))
            .willReturn(of(kapitel1_1_1));
        given(repositoryMock.updateKapitel("SAMPLE", "master", kapitel1_1_1))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<TailoringAnforderung> actual = service.createAnforderung(
            "SAMPLE",
            "master",
            "1.1.1",
            "b1",
            "Dies ist eine neue Anforderung"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getPosition()).isEqualTo("b2");
        assertThat(anforderungen).hasSize(5);
        assertThat(anforderungen.get(3).getPosition()).isEqualTo("b2");
    }

    @Test
    void createAnforderung_KapitelKannNichtAktuaisiertWerden_EmptyWirdZurueckGegeben() {
        // arrange
        List<TailoringAnforderung> anforderungen = new ArrayList<>();
        anforderungen.add(TailoringAnforderung.builder()
            .position("a")
            .text("Anforderung 1.1.1")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("b")
            .text("Anforderung 1.1.2")
            .ausgewaehlt(FALSE)
            .build());
        anforderungen.add(TailoringAnforderung.builder()
            .position("c")
            .text("Anforderung 1.1.3")
            .ausgewaehlt(FALSE)
            .build());

        Kapitel<TailoringAnforderung> kapitel1_1_1 = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1.1")
            .anforderungen(anforderungen)
            .build();

        given(repositoryMock.getKapitel("SAMPLE", "master", "1.1.1"))
            .willReturn(of(kapitel1_1_1));
        given(repositoryMock.updateKapitel("SAMPLE", "master", kapitel1_1_1))
            .willReturn(empty());

        // act
        Optional<TailoringAnforderung> actual = service.createAnforderung(
            "SAMPLE",
            "master",
            "1.1.1",
            "b",
            "Dies ist eine neue Anforderung"
        );

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void handleText_AnforderungNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(empty());

        // act
        Optional<TailoringAnforderung> actual = service.handleText(
            "SAMPLE",
            "master",
            "1.1",
            "a",
            "Dies iet ein geändeter Text"
        );

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0))
            .updateAnforderung(anyString(), anyString(), anyString(), any(TailoringAnforderung.class));
    }

    @Test
    void handleText_NeuerTextKeineReferenz_AnforderungTextAktualisiert() {
        // arrange
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringAnforderung.builder()
                    .text("Der Text vor der Änderung")
                    .ausgewaehlt(TRUE)
                    .build()
            )
        );
        given(repositoryMock.updateAnforderung(anyString(), anyString(), anyString(), any()))
            .willAnswer(invocation -> of(invocation.getArgument(3)));

        // act
        Optional<TailoringAnforderung> actual = service.handleText(
            "SAMPLE",
            "master",
            "1.1",
            "a",
            "Dies iet ein geändeter Text"
        );

        // assert
        verify(repositoryMock, times(1))
            .updateAnforderung(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringAnforderung.class));
        assertThat(actual).isPresent();
        assertThat(actual.get().getText()).isEqualTo("Dies iet ein geändeter Text");
        assertThat(actual.get().getTextGeaendert()).isNotNull();
    }

    @Test
    void handleText_NeuerTextReferenz_AnforderungTextAktualisiertReferenzMod() {
        // arrange
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringAnforderung.builder()
                    .text("Der Text vor der Änderung")
                    .ausgewaehlt(TRUE)
                    .referenz(Referenz.builder().text("Referenz").build())
                    .build()
            )
        );
        given(repositoryMock.updateAnforderung(anyString(), anyString(), anyString(), any()))
            .willAnswer(invocation -> of(invocation.getArgument(3)));

        // act
        Optional<TailoringAnforderung> actual = service.handleText(
            "SAMPLE",
            "master",
            "1.1",
            "a",
            "Dies iet ein geändeter Text"
        );

        // assert
        verify(repositoryMock, times(1))
            .updateAnforderung(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringAnforderung.class));
        assertThat(actual).isPresent();
        assertThat(actual.get().getText()).isEqualTo("Dies iet ein geändeter Text");
        assertThat(actual.get().getTextGeaendert()).isNotNull();
        assertThat(actual.get().getReferenz().getGeaendert()).isTrue();
    }

    @Test
    void handleText_KeinNeuerText_AnforderungNichtAktualisiert() {
        // arrange
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringAnforderung.builder()
                    .text("Der Text vor der Änderung")
                    .ausgewaehlt(TRUE)
                    .build()
            )
        );
        given(repositoryMock.updateAnforderung(anyString(), anyString(), anyString(), any()))
            .willAnswer(invocation -> of(invocation.getArgument(3)));

        // act
        Optional<TailoringAnforderung> actual = service.handleText(
            "SAMPLE",
            "master",
            "1.1",
            "a",
            "Der Text vor der Änderung"
        );

        // assert
        verify(repositoryMock, times(0))
            .updateAnforderung(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringAnforderung.class));
        assertThat(actual).isPresent();
        assertThat(actual.get().getText()).isEqualTo("Der Text vor der Änderung");
        assertThat(actual.get().getTextGeaendert()).isNull();
    }

}
