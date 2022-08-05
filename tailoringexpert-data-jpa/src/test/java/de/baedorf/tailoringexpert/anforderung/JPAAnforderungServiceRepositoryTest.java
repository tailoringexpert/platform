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
import de.baedorf.tailoringexpert.domain.ProjektEntity;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.domain.TailoringAnforderungEntity;
import de.baedorf.tailoringexpert.domain.TailoringEntity;
import de.baedorf.tailoringexpert.domain.TailoringKatalogEntity;
import de.baedorf.tailoringexpert.domain.TailoringKatalogKapitelEntity;
import de.baedorf.tailoringexpert.repository.ProjektRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static de.baedorf.tailoringexpert.domain.Phase.E;
import static de.baedorf.tailoringexpert.domain.Phase.F;
import static de.baedorf.tailoringexpert.domain.Phase.ZERO;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Log4j2
class JPAAnforderungServiceRepositoryTest {

    ProjektRepository projektRepositoryMock;
    JPAAnforderungServiceRepositoryMapper mapperMock;
    JPAAnforderungServiceRepository repository;

    @BeforeEach
    void setup() {
        this.projektRepositoryMock = mock(ProjektRepository.class);
        this.mapperMock = mock(JPAAnforderungServiceRepositoryMapper.class);
        this.repository = new JPAAnforderungServiceRepository(
            this.mapperMock,
            this.projektRepositoryMock
        );
    }

    @Test
    void getAnforderung_ProjektNichtVorhanden_EmptyErgebniss() {
        // arrange
        when(projektRepositoryMock.findByKuerzel("SAMPLE")).thenReturn(null);

        // act
        Optional<TailoringAnforderung> actual = repository.getAnforderung("SAMPLE", "master1", "1.2.1", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getAnforderung_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getAnforderung(null, "master1", "1.2.1", "b"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderung_PhaseNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getAnforderung("DUMMY", null, "1.2.1", "b"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderung_KapitelNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getAnforderung("DUMMY", "master", null, "b"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderung_PositionNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getAnforderung("DUMMY", "master", "1.2.1", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderung_PhaseNichtVorhanden_EmptyErgebniss() {
        // arrange
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder().build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        // act
        Optional<TailoringAnforderung> actual = repository.getAnforderung("SAMPLE", "master1", "1.2.1", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getAnforderung_KapitelNichtVorhanden_EmptyErgebniss() {
        // arrange
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                TailoringKatalogKapitelEntity.builder()
                                    .nummer("1")
                                    .kapitel(asList(
                                        TailoringKatalogKapitelEntity.builder()
                                            .nummer("1.1")
                                            .kapitel(asList(
                                                TailoringKatalogKapitelEntity.builder()
                                                    .nummer("1.1.1")
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        // act
        Optional<TailoringAnforderung> actual = repository.getAnforderung("SAMPLE", "master", "1.1.2", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getAnforderung_AnforderungNichtVorhanden_EmptyErgebniss() {
        // arrange
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                TailoringKatalogKapitelEntity.builder()
                                    .nummer("1")
                                    .kapitel(asList(
                                        TailoringKatalogKapitelEntity.builder()
                                            .nummer("1.1")
                                            .anforderungen(asList(
                                                TailoringAnforderungEntity.builder()
                                                    .position("a")
                                                    .build()
                                            ))
                                            .kapitel(asList(
                                                TailoringKatalogKapitelEntity.builder()
                                                    .nummer("1.1.1")
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        // act
        Optional<TailoringAnforderung> actual = repository.getAnforderung("SAMPLE", "master", "1.1", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getAnforderung() {
        // arrange
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                TailoringKatalogKapitelEntity.builder()
                                    .nummer("1")
                                    .kapitel(asList(
                                        TailoringKatalogKapitelEntity.builder()
                                            .nummer("1.1")
                                            .kapitel(asList(
                                                TailoringKatalogKapitelEntity.builder()
                                                    .nummer("1.1.1")
                                                    .build()
                                            ))
                                            .build(),
                                        TailoringKatalogKapitelEntity.builder()
                                            .nummer("1.2")
                                            .kapitel(asList(
                                                TailoringKatalogKapitelEntity.builder()
                                                    .nummer("1.2.1")
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build(),
                TailoringEntity.builder()
                    .id(3L)
                    .name("master1")
                    .phase(E)
                    .phase(F)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                TailoringKatalogKapitelEntity.builder()
                                    .nummer("1")
                                    .kapitel(asList(
                                        TailoringKatalogKapitelEntity.builder()
                                            .nummer("1.1")
                                            .kapitel(asList(
                                                TailoringKatalogKapitelEntity.builder()
                                                    .nummer("1.1.1")
                                                    .build()
                                            ))
                                            .build(),
                                        TailoringKatalogKapitelEntity.builder()
                                            .nummer("1.2")
                                            .kapitel(asList(
                                                TailoringKatalogKapitelEntity.builder()
                                                    .nummer("1.2.1")
                                                    .anforderungen(asList(
                                                        TailoringAnforderungEntity.builder()
                                                            .position("a")
                                                            .build(),
                                                        TailoringAnforderungEntity.builder()
                                                            .position("b")
                                                            .build()))
                                                    .build()
                                            ))
                                            .build()))
                                    .build()))
                            .build())
                        .build())
                    .build()))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        // act
        Optional<TailoringAnforderung> actual = repository.getAnforderung("SAMPLE", "master1", "1.2.1", "b");

        // assert
        assertThat(actual).isNotNull();
    }


    @Test
    void updateAnforderung_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateAnforderung(null, "master1", "1.2.1", TailoringAnforderung.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAnforderung_PhaseNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateAnforderung("DUMMY", null, "1.2.1", TailoringAnforderung.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAnforderung_KapitelNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateAnforderung("DUMMY", "master", null, TailoringAnforderung.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAnforderung_AnforderungNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateAnforderung("DUMMY", "master", "1.2.1", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAnforderung_GruppeNichtVorhanden_EmptyErgebnis() {
        when(projektRepositoryMock.findByKuerzel("SAMPLE")).thenReturn(null);

        TailoringAnforderung anforderung = TailoringAnforderung.builder()
            .position("a")
            .build();

        // act
        Optional<TailoringAnforderung> actual = repository.updateAnforderung("SAMPLE", "master", "1.1", anforderung);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateAnforderung_AnforderungNichtVorhanden_EmptyErgebnis() {
        // arrange
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                TailoringKatalogKapitelEntity.builder()
                                    .nummer("1")
                                    .kapitel(asList(
                                        TailoringKatalogKapitelEntity.builder()
                                            .nummer("1.1")
                                            .anforderungen(asList(
                                                TailoringAnforderungEntity.builder()
                                                    .position("a")
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);


        TailoringAnforderung anforderung = TailoringAnforderung.builder()
            .position("b")
            .build();

        // act
        Optional<TailoringAnforderung> actual = repository.updateAnforderung("SAMPLE", "master", "1.1", anforderung);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateAnforderung_AnforderungVorhanden_WerteWurdenUebernommen() {
        // arrange
        TailoringAnforderungEntity anforderungToUpdate = TailoringAnforderungEntity.builder()
            .position("a")
            .build();
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                TailoringKatalogKapitelEntity.builder()
                                    .nummer("1")
                                    .kapitel(asList(
                                        TailoringKatalogKapitelEntity.builder()
                                            .nummer("1.1")
                                            .anforderungen(asList(
                                                anforderungToUpdate
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        TailoringAnforderung anforderung = TailoringAnforderung.builder()
            .position("a")
            .build();
        given(mapperMock.toDomain(anforderungToUpdate))
            .willReturn(anforderung);

        // act
        Optional<TailoringAnforderung> actual = repository.updateAnforderung("SAMPLE", "master", "1.1", anforderung);

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, timeout(1))
            .updateAnforderung(anforderung, anforderungToUpdate);
    }


    @Test
    void getKapitel_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getKapitel(null, "master", "1.2.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_PhaseNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getKapitel("DUMMY", null, "1.2.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_KapitelNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getKapitel("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_KapitelVorhanden_DomaenenobjektWirdZurueckGegeben() {

        // arrange
        TailoringKatalogKapitelEntity gruppe = TailoringKatalogKapitelEntity.builder()
            .nummer("1")
            .kapitel(asList(
                TailoringKatalogKapitelEntity.builder()
                    .nummer("1.1")
                    .build()
            ))
            .build();
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        given(mapperMock.toDomain(gruppe))
            .willReturn(Kapitel.<TailoringAnforderung>builder().build());

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = repository.getKapitel("SAMPLE", "master", "1.1");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void getKapitel_KapitelNichtVorhanden_EmptyErgebnis() {
        // arrange
        TailoringKatalogKapitelEntity gruppe = TailoringKatalogKapitelEntity.builder()
            .nummer("1")
            .kapitel(asList(
                TailoringKatalogKapitelEntity.builder()
                    .nummer("1.1")
                    .build()
            ))
            .build();
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        given(mapperMock.toDomain(gruppe))
            .willReturn(Kapitel.<TailoringAnforderung>builder().build());

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = repository.getKapitel("SAMPLE", "master", "1.2");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateKapitel_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateKapitel(null, "master", Kapitel.<TailoringAnforderung>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateKapitel_PhaseNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateKapitel("DUMMY", null, Kapitel.<TailoringAnforderung>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateKapitel_KapitelNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateKapitel("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }


    @Test
    void updateKapitel_KapitelNichtVorhanden_EmptyErgebnis() {
        // arrange
        TailoringKatalogKapitelEntity gruppe = TailoringKatalogKapitelEntity.builder()
            .nummer("1")
            .kapitel(asList(
                TailoringKatalogKapitelEntity.builder()
                    .nummer("1.1")
                    .build()
            ))
            .build();
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        Kapitel<TailoringAnforderung> kapitel = Kapitel.<TailoringAnforderung>builder()
            .nummer("2")
            .build();

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = repository.updateKapitel("SAMPLE", "master", kapitel);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateKapitel_KapitelVorhanden_WerteUebernommen() {
        // arrange
        TailoringKatalogKapitelEntity kapitelToUpdate = TailoringKatalogKapitelEntity.builder()
            .nummer("1")
            .kapitel(asList(
                TailoringKatalogKapitelEntity.builder()
                    .nummer("1.1")
                    .build()
            ))
            .build();
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                kapitelToUpdate)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        Kapitel<TailoringAnforderung> kapitel = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1")
            .build();

        given(mapperMock.toDomain(kapitelToUpdate))
            .willReturn(kapitel);

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = repository.updateKapitel("SAMPLE", "master", kapitel);

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, times(1))
            .updateKapitel(kapitel, kapitelToUpdate);
    }

    @Test
    void updateAusgewaehlt_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateAusgewaehlt(null, "master", Kapitel.<TailoringAnforderung>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAusgewaehlt_PhasetNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act

        Throwable actual = catchThrowable(() -> repository.updateAusgewaehlt("DUMMY", null, Kapitel.<TailoringAnforderung>builder().build()));
        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAusgewaehlt_GruppeNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateAusgewaehlt("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }


    @Test
    void updateAusgewaehlt_KapitelNichtVorhanden_EmptyErgebnis() {
        // arrange
        TailoringKatalogKapitelEntity gruppe = TailoringKatalogKapitelEntity.builder()
            .nummer("1")
            .kapitel(asList(
                TailoringKatalogKapitelEntity.builder()
                    .nummer("1.1")
                    .build()
            ))
            .build();
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        Kapitel<TailoringAnforderung> kapitel = Kapitel.<TailoringAnforderung>builder()
            .nummer("2")
            .build();

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = repository.updateAusgewaehlt("SAMPLE", "master", kapitel);

        // assert
        assertThat(actual).isEmpty();

    }

    @Test
    void updateAusgewaehlt_KapitelVorhanden_AuswahlUebernommen() {
        // arrange
        TailoringAnforderungEntity anforderungAToUpdate = TailoringAnforderungEntity.builder()
            .position("a")
            .ausgewaehlt(Boolean.TRUE)
            .build();
        TailoringAnforderungEntity anforderungBToUpdate = TailoringAnforderungEntity.builder()
            .position("b")
            .ausgewaehlt(Boolean.FALSE)
            .build();

        TailoringKatalogKapitelEntity gruppe = TailoringKatalogKapitelEntity.builder()
            .nummer("1")
            .kapitel(asList(
                TailoringKatalogKapitelEntity.builder()
                    .nummer("1.1")
                    .anforderungen(asList(
                        anforderungAToUpdate,
                        anforderungBToUpdate
                    ))
                    .build()
            ))
            .build();
        ProjektEntity projekt = ProjektEntity.builder()
            .kuerzel("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .katalog(TailoringKatalogEntity.builder()
                        .toc(TailoringKatalogKapitelEntity.builder()
                            .kapitel(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        TailoringAnforderung anforderungA = TailoringAnforderung.builder()
            .position("a")
            .ausgewaehlt(Boolean.TRUE)
            .build();
        TailoringAnforderung anforderungB = TailoringAnforderung.builder()
            .position("b")
            .ausgewaehlt(Boolean.TRUE)
            .build();
        Kapitel<TailoringAnforderung> kapitel = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1")
            .anforderungen(asList(
                anforderungA,
                anforderungB
            ))
            .build();

        given(mapperMock.toDomain(gruppe))
            .willReturn(kapitel);

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = repository.updateAusgewaehlt("SAMPLE", "master", kapitel);

        // assert
        assertThat(actual).isNotEmpty();

        verify(mapperMock, times(1))
            .updateAnforderung(anforderungA, anforderungAToUpdate);

        verify(mapperMock, times(1))
            .updateAnforderung(anforderungB, anforderungBToUpdate);

    }
}
