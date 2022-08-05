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
package de.baedorf.tailoringexpert.katalog;

import de.baedorf.tailoringexpert.domain.DRD;
import de.baedorf.tailoringexpert.domain.DRDEntity;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;
import de.baedorf.tailoringexpert.domain.KatalogEntity;
import de.baedorf.tailoringexpert.domain.KatalogKapitelEntity;
import de.baedorf.tailoringexpert.domain.Logo;
import de.baedorf.tailoringexpert.domain.LogoEntity;
import de.baedorf.tailoringexpert.domain.Phase;
import de.baedorf.tailoringexpert.repository.DRDRepository;
import de.baedorf.tailoringexpert.repository.LogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPAKatalogServiceRepositoryMapperTest {

    private LogoRepository logoRepositoryMock;
    private DRDRepository drdRepositoryMock;
    private JPAKatalogServiceRepositoryMapper mapper;

    @BeforeEach
    void setup() {
        this.logoRepositoryMock = Mockito.mock(LogoRepository.class);
        this.drdRepositoryMock = Mockito.mock(DRDRepository.class);
        this.mapper = new JPAKatalogServiceRepositoryMapperImpl();
        this.mapper.setLogoRepository(logoRepositoryMock);
        this.mapper.setDrdRepository(drdRepositoryMock);
    }

    @Test
    void createKatalog_KatalogNull_NullWirdZurueckGegeben() {
        // arrange
        Katalog<KatalogAnforderung> katalog = null;

        // act
        KatalogEntity actual = mapper.createKatalog(katalog);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void createKatalog_KatalogTocNull_KatalogDefinitionEntityOhneTocWirdErzeugt() {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .version("8.2.1")
            .toc(null)
            .build();

        // act
        KatalogEntity actual = mapper.createKatalog(katalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
        assertThat(actual.getGueltigAb()).isNotNull();
        assertThat(actual.getToc()).isNull();
    }

    @Test
    void createKatalog_Katalog_KatalogDefinitionEntityWirdErzeugt() {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .version("8.2.1")
            .toc(Kapitel.<KatalogAnforderung>builder()
                .anforderungen(Arrays.asList(
                    KatalogAnforderung.builder()
                        .position("a")
                        .text("Anforderung")
                        .phasen(Arrays.asList(Phase.ZERO))
                        .build()
                ))
                .build())
            .build();

        // act
        KatalogEntity actual = mapper.createKatalog(katalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
        assertThat(actual.getGueltigAb()).isNotNull();
        assertThat(actual.getToc()).isNotNull();
        assertThat(actual.getToc().getAnforderungen()).hasSize(1);
    }

    @Test
    void toDomain_KatalogDefinitionEntityNull_NullWirdZureuckGegeben() {
        // arrange
        KatalogEntity katalog = null;

        // act
        Katalog<KatalogAnforderung> actual = mapper.createKatalog(katalog);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toDomain_KatalogDefinitionEntityKeinToc_KatalogOhneTocWirdErzeugt() {
        // arrange
        KatalogEntity katalog = KatalogEntity.builder()
            .version("8.2.1")
            .build();

        // act
        Katalog<KatalogAnforderung> actual = mapper.createKatalog(katalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc()).isNull();
        assertThat(actual.getVersion()).isEqualTo(katalog.getVersion());
    }

    @Test
    void toDomain_KatalogDefinitionEntity_KatalogWirdErzeugt() {
        // arrange
        KatalogEntity katalog = KatalogEntity.builder()
            .version("8.2.1")
            .toc(KatalogKapitelEntity.builder().build())
            .build();

        // act
        Katalog<KatalogAnforderung> actual = mapper.createKatalog(katalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc()).isNotNull();
        assertThat(actual.getVersion()).isEqualTo(katalog.getVersion());
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

    @Test
    void resolve_NullDRD_NullWirdZurueckgegeben() {
        // arrange
        DRD drd = null;

        // act
        DRDEntity actual = mapper.resolve(drd);

        // assert
        assertThat(actual).isNull();
        verify(drdRepositoryMock, times(0)).findByNummer(any());
    }

    @Test
    void resolve_DRDUebergeben_ExistierendesDRDEntityWirdZurueckgegeben() {
        // arrange
        DRD drd = DRD.builder().nummer("drd-47.11").build();

        DRDEntity drdEntity = DRDEntity.builder().id(12l).build();
        given(drdRepositoryMock.findByNummer("drd-47.11")).willReturn(drdEntity);

        // act
        DRDEntity actual = mapper.resolve(drd);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        verify(drdRepositoryMock, times(1)).findByNummer("drd-47.11");
    }

}
