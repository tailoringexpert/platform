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
package eu.tailoringexpert.requirement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.tailoringexpert.domain.ApplicableDocumentEntity;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.Document;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.repository.ApplicableDocumentRepository;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;

class JPARequirementServiceRepositoryMapperTest {

    private LogoRepository logoRepositoryMock;

    private DRDRepository drdRepositoryMock;

    private ApplicableDocumentRepository applicableDocumentRepositoryMock;

    private JPARequirementServiceRepositoryMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new JPARequirementServiceRepositoryMapperGenerated();

        this.logoRepositoryMock = mock(LogoRepository.class);
        this.drdRepositoryMock = mock(DRDRepository.class);
        this.applicableDocumentRepositoryMock = mock(ApplicableDocumentRepository.class);

        this.mapper.setLogoRepository(logoRepositoryMock);
        this.mapper.setDrdRepository(drdRepositoryMock);
        this.mapper.setApplicableDocumentRepository(applicableDocumentRepositoryMock);
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
        DRD drd = DRD.builder().number("01").build();

        DRDEntity drdEntity = DRDEntity.builder().number("01").build();
        given(drdRepositoryMock.findByNumber("01")).willReturn(drdEntity);

        // act
        DRDEntity actual = mapper.resolve(drd);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getNumber()).isEqualTo("01");
        verify(drdRepositoryMock, times(1)).findByNumber("01");
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
    void resolve_DocumentNotNull_ApplicableDocumentEntityReturned() {
        // arrange
        Document document = Document.builder().title("Sample Document").issue("1").revision("").build();

        ApplicableDocumentEntity applicableDocumentEntity = ApplicableDocumentEntity.builder().issue("1").revision("")
                .build();
        given(applicableDocumentRepositoryMock.findByTitleAndIssueAndRevision("Sample Document", "1", ""))
                .willReturn(applicableDocumentEntity);

        // act
        ApplicableDocumentEntity actual = mapper.resolve(document);

        // assert
        assertThat(actual).isNotNull();
        verify(applicableDocumentRepositoryMock, times(1)).findByTitleAndIssueAndRevision("Sample Document", "1", "");
    }
}
