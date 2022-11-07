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

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Reference;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringState;
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
class RequirementServiceImplTest {

    private RequirementServiceRepository repositoryMock;

    private RequirementModifiablePredicate predicateMock;

    private RequirementService service;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(RequirementServiceRepository.class);
        this.predicateMock = mock(RequirementModifiablePredicate.class);
        this.service = new RequirementServiceImpl(repositoryMock, predicateMock);
    }

    @Test
    void RequirementServiceImpl_RequirementServiceRepositoryNotProvided_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> new RequirementServiceImpl(null, null));

        //assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void handleSelected_NonModifiableTailoring_EmptyReturned() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(FALSE);

        // act
        Optional<TailoringRequirement> actual = service.handleSelected("SAMPLE", "master", "1.1", "a", TRUE);

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0)).getRequirement(any(), any(), any(), any());
        verify(repositoryMock, times(0)).updateRequirement(any(), any(), any(), any());
    }


    @Test
    void handleSelected_RequirementNotSelectedBefore_StateSelected() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringRequirement.builder()
                    .selected(FALSE)
                    .build()
            )
        );
        given(repositoryMock.updateRequirement(anyString(), anyString(), anyString(), any(TailoringRequirement.class)))
            .willAnswer(invocation -> of(invocation.getArgument(3)));


        // act
        Optional<TailoringRequirement> actual = service.handleSelected("SAMPLE", "master", "1.1", "a", TRUE);

        // assert
        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(1))
            .updateRequirement(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringRequirement.class));
        assertThat(actual).isPresent();
        assertThat(actual.get().getSelected()).isTrue();
        assertThat(actual.get().getSelectionChanged()).isNotNull();
    }


    @Test
    void handleSelected_RequirementNotExist_EmptyReturned() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(empty());

        // act
        Optional<TailoringRequirement> actual = service.handleSelected("SAMPLE", "master", "1.1", "a", TRUE);

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0)).updateRequirement(any(), any(), any(), any());
    }

    @Test
    void handleSelected_RequirementSelectedChangeToNotSelected_RequirementReturned() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringRequirement.builder()
                    .selected(FALSE)
                    .selectionChanged(ZonedDateTime.now())
                    .build()
            )
        );
        given(repositoryMock.updateRequirement(anyString(), anyString(), anyString(), any()))
            .willAnswer(invocation -> of(invocation.getArgument(3)));

        // act
        Optional<TailoringRequirement> actual = service.handleSelected("SAMPLE", "master", "1.1", "a", TRUE);

        // assert
        assertThat(actual.get().getSelected()).isTrue();
        assertThat(actual.get().getSelectionChanged()).isNull();
        assertThat(actual).isPresent();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(1))
            .updateRequirement(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringRequirement.class));
    }

    @Test
    void handleSelected_NoSelectedChange_RequirementReturned() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringRequirement.builder()
                    .selected(FALSE)
                    .selectionChanged(ZonedDateTime.now())
                    .build()
            )
        );

        // act
        Optional<TailoringRequirement> actual = service.handleSelected("SAMPLE", "master", "1.1", "a", FALSE);

        // assert
        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        assertThat(actual).isPresent();
        assertThat(actual.get().getSelected()).isFalse();
        assertThat(actual.get().getSelectionChanged()).isNotNull();
    }


    @Test
    void handleSelected_NonModifiableTailoringChapter_EmptyReturned() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(FALSE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1")).willReturn(empty());

        // act
        Optional<Chapter<TailoringRequirement>> actual = service.handleSelected("SAMPLE", "master", "1", TRUE);

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0)).updateSelected(any(), any(), any());
    }


    @Test
    void handleSelected_ChapterNotExisting_EmptyReturned() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1")).willReturn(empty());

        // act
        Optional<Chapter<TailoringRequirement>> actual = service.handleSelected("SAMPLE", "master", "1", TRUE);

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0)).updateSelected(anyString(), anyString(), any(Chapter.class));
    }

    @Test
    void handleSelected_RequirementsMixedSelectedState_ChangedStateInRequirements() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1")).willReturn(of(
                Chapter.<TailoringRequirement>builder()
                    .number("1")
                    .requirements(asList(
                        TailoringRequirement.builder()
                            .text("Requirement 1")
                            .selected(FALSE)
                            .build()))
                    .chapters(asList(
                        Chapter.<TailoringRequirement>builder()
                            .number("1.1")
                            .requirements(asList(
                                TailoringRequirement.builder()
                                    .text("Requirement 1.1")
                                    .selected(TRUE)
                                    .build()))
                            .chapters(asList(
                                Chapter.<TailoringRequirement>builder()
                                    .number("1.1.1")
                                    .requirements(asList(
                                        TailoringRequirement.builder()
                                            .text("Requirement 1.1.1")
                                            .selected(FALSE)
                                            .build()))
                                    .build()))
                            .build(),
                        Chapter.<TailoringRequirement>builder()
                            .number("1.2")
                            .requirements(asList(TailoringRequirement.builder()
                                .text("Requirement 1.2")
                                .selected(TRUE)
                                .build()))
                            .build()))
                    .build()
            )
        );
        given(repositoryMock.updateSelected(anyString(), anyString(), any(Chapter.class)))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<Chapter<TailoringRequirement>> actual = service.handleSelected("SAMPLE", "master", "1", TRUE);

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().allRequirements()).allMatch(anforderung -> TRUE.equals(anforderung.getSelected()));
        assertThat(actual.get().allRequirements())
            .extracting(TailoringRequirement::getSelectionChanged)
            .filteredOn(Objects::nonNull)
            .hasSize(2);

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
    }

    @Test
    void createRequirement_NonModifiableTailoring_EmptyReturned() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(FALSE);

        // act
        Optional<TailoringRequirement> actual = service.createRequirement(
            "SAMPLE",
            "master",
            "1.1.1",
            "b",
            "Dies ist eine neue Requirement"
        );

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0)).getChapter(any(), any(), any());
        verify(repositoryMock, times(0)).updateChapter(any(), any(), any());
    }

    @Test
    void createRequirement_NewRequirement111b_Requirement111b1Created() {
        // arrange
        List<TailoringRequirement> anforderungen = new ArrayList<>();
        anforderungen.add(TailoringRequirement.builder()
            .position("a")
            .text("Requirement 1.1.1")
            .selected(FALSE)
            .build());
        anforderungen.add(TailoringRequirement.builder()
            .position("b")
            .text("Requirement 1.1.2")
            .selected(FALSE)
            .build());
        anforderungen.add(TailoringRequirement.builder()
            .position("c")
            .text("Requirement 1.1.3")
            .selected(FALSE)
            .build());

        Chapter<TailoringRequirement> chapter1_1_1 = Chapter.<TailoringRequirement>builder()
            .number("1.1.1")
            .requirements(anforderungen)
            .build();

        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1.1.1"))
            .willReturn(of(chapter1_1_1));
        given(repositoryMock.updateChapter("SAMPLE", "master", chapter1_1_1))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<TailoringRequirement> actual = service.createRequirement(
            "SAMPLE",
            "master",
            "1.1.1",
            "b",
            "Dies ist eine neue Requirement"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getPosition()).isEqualTo("b1");
        assertThat(anforderungen).hasSize(4);
        assertThat(anforderungen.get(2).getPosition()).isEqualTo("b1");

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
    }


    @Test
    void createRequirement_Requirement111b1ExistNewOneToCreate_Requirement111b1CreatedRequirement111b1ChangedTo111b2() {
        // arrange
        List<TailoringRequirement> requirements = new ArrayList<>();
        requirements.add(TailoringRequirement.builder()
            .position("a")
            .text("Requirement 1.1.1")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("b")
            .text("Requirement 1.1.2")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("b1")
            .text("Requirement 1.1.2b1")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("c")
            .text("Requirement 1.1.3")
            .selected(FALSE)
            .build());

        Chapter<TailoringRequirement> chapter1_1_1 = Chapter.<TailoringRequirement>builder()
            .number("1.1.1")
            .requirements(requirements)
            .build();

        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1.1.1"))
            .willReturn(of(chapter1_1_1));
        given(repositoryMock.updateChapter("SAMPLE", "master", chapter1_1_1))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<TailoringRequirement> actual = service.createRequirement(
            "SAMPLE",
            "master",
            "1.1.1",
            "b",
            "Dies ist eine neue zwischengeschobene neue Requirement"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getPosition()).isEqualTo("b1");
        assertThat(requirements).hasSize(5);
        assertThat(requirements.get(3).getPosition()).isEqualTo("b2");

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
    }

    @Test
    void createRequirement_Requirement111b2_Requirement111b2Created() {
        // arrange
        List<TailoringRequirement> requirements = new ArrayList<>();
        requirements.add(TailoringRequirement.builder()
            .position("a")
            .text("Requirement 1.1.1")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("b")
            .text("Requirement 1.1.2")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("b1")
            .text("Requirement 1.1.2b1")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("c")
            .text("Requirement 1.1.3")
            .selected(FALSE)
            .build());

        Chapter<TailoringRequirement> chapter1_1_1 = Chapter.<TailoringRequirement>builder()
            .number("1.1.1")
            .requirements(requirements)
            .build();

        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1.1.1"))
            .willReturn(of(chapter1_1_1));
        given(repositoryMock.updateChapter("SAMPLE", "master", chapter1_1_1))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<TailoringRequirement> actual = service.createRequirement(
            "SAMPLE",
            "master",
            "1.1.1",
            "b1",
            "Dies ist eine neue 2. neue Requirement"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getPosition()).isEqualTo("b2");
        assertThat(requirements).hasSize(5);
        assertThat(requirements.get(2).getPosition()).isEqualTo("b1");
        assertThat(requirements.get(3).getPosition()).isEqualTo("b2");
        assertThat(requirements.get(3).getText()).isEqualTo("Dies ist eine neue 2. neue Requirement");

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
    }

    @Test
    void createRequirement_RequirementAfterCustomRequirement_Requirement111b2Created() {
        // arrange
        List<TailoringRequirement> requirements = new ArrayList<>();
        requirements.add(
            TailoringRequirement.builder()
                .position("a1")
                .text("a1")
                .selected(FALSE)
                .build());
        requirements.add(
            TailoringRequirement.builder()
                .position("a2")
                .text("a2")
                .selected(FALSE)
                .build()
        );

        Chapter<TailoringRequirement> chapter1_1_1 = Chapter.<TailoringRequirement>builder()
            .number("1.1.1")
            .requirements(requirements)
            .build();

        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1.1.1"))
            .willReturn(of(chapter1_1_1));
        given(repositoryMock.updateChapter("SAMPLE", "master", chapter1_1_1))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<TailoringRequirement> actual = service.createRequirement(
            "SAMPLE",
            "master",
            "1.1.1",
            "a1",
            "Dies ist eine neue 2. neue Requirement"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getPosition()).isEqualTo("a2");
        assertThat(requirements).hasSize(3);
        assertThat(requirements.get(2).getPosition()).isEqualTo("a3");
        assertThat(requirements.get(1).getText()).isEqualTo("Dies ist eine neue 2. neue Requirement");

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
    }

    @Test
    void createRequirement_ChapterNotExisting_RequirementNotCreated() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1.1.1")).willReturn(empty());

        // act
        Optional<TailoringRequirement> actual = service.createRequirement(
            "SAMPLE",
            "master",
            "1.1.1",
            "b1",
            "Dies ist eine neue Requirement"
        );

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0)).updateChapter(any(), any(), any());
    }

    @Test
    void createRequirement_PreceedingRequirementNotExisting_RequirementNotCreated() {
        // arrange
        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("1.1.1")
            .requirements(Collections.emptyList())
            .build();
        given(repositoryMock.getChapter("SAMPLE", "master", "1.1.1")).willReturn(of(chapter));
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);

        // act
        Optional<TailoringRequirement> actual = service.createRequirement(
            "SAMPLE",
            "master",
            "1.1.1",
            "b1",
            "Dies ist eine neue Requirement"
        );

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0)).updateChapter(anyString(), anyString(), any());
    }

    @Test
    void createRequirement_Requirement111b1_Anforderung111b2WurdeErzeugt() {
        // arrange
        List<TailoringRequirement> requirements = new ArrayList<>();
        requirements.add(TailoringRequirement.builder()
            .position("a")
            .text("Requirement 1.1.1")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("b")
            .text("Requirement 1.1.2")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("b1")
            .text("Requirement 1.1.2b1")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("c")
            .text("Requirement 1.1.3")
            .selected(FALSE)
            .build());

        Chapter<TailoringRequirement> chapter1_1_1 = Chapter.<TailoringRequirement>builder()
            .number("1.1.1")
            .requirements(requirements)
            .build();

        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1.1.1"))
            .willReturn(of(chapter1_1_1));
        given(repositoryMock.updateChapter("SAMPLE", "master", chapter1_1_1))
            .willAnswer(invocation -> of(invocation.getArgument(2)));

        // act
        Optional<TailoringRequirement> actual = service.createRequirement(
            "SAMPLE",
            "master",
            "1.1.1",
            "b1",
            "Dies ist eine neue Requirement"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getPosition()).isEqualTo("b2");
        assertThat(requirements).hasSize(5);
        assertThat(requirements.get(3).getPosition()).isEqualTo("b2");

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
    }

    @Test
    void createRequirement_KapitelKannNichtAktuaisiertWerden_EmptyWirdZurueckGegeben() {
        // arrange
        List<TailoringRequirement> requirements = new ArrayList<>();
        requirements.add(TailoringRequirement.builder()
            .position("a")
            .text("Requirement 1.1.1")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("b")
            .text("Requirement 1.1.2")
            .selected(FALSE)
            .build());
        requirements.add(TailoringRequirement.builder()
            .position("c")
            .text("Requirement 1.1.3")
            .selected(FALSE)
            .build());

        Chapter<TailoringRequirement> chapter1_1_1 = Chapter.<TailoringRequirement>builder()
            .number("1.1.1")
            .requirements(requirements)
            .build();

        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getChapter("SAMPLE", "master", "1.1.1"))
            .willReturn(of(chapter1_1_1));
        given(repositoryMock.updateChapter("SAMPLE", "master", chapter1_1_1))
            .willReturn(empty());

        // act
        Optional<TailoringRequirement> actual = service.createRequirement(
            "SAMPLE",
            "master",
            "1.1.1",
            "b",
            "Dies ist eine neue Requirement"
        );

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
    }

    @Test
    void handleText_NonModifiableTailoring_EmptyReturned() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(FALSE);

        // act
        Optional<TailoringRequirement> actual = service.handleText(
            "SAMPLE",
            "master",
            "1.1",
            "a",
            "Dies iet ein geändeter Text"
        );

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0)).getRequirement(any(), any(), any(), any());
        verify(repositoryMock, times(0)).updateRequirement(any(), any(), any(), any());
    }


    @Test
    void handleText_RequirementNotExists_EmptyReturned() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(empty());

        // act
        Optional<TailoringRequirement> actual = service.handleText(
            "SAMPLE",
            "master",
            "1.1",
            "a",
            "Dies iet ein geändeter Text"
        );

        // assert
        assertThat(actual).isEmpty();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0))
            .updateRequirement(anyString(), anyString(), anyString(), any(TailoringRequirement.class));
    }

    @Test
    void handleText_NeuerTextKeineReferenz_RequirmentReturend() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringRequirement.builder()
                    .text("Der Text vor der Änderung")
                    .selected(TRUE)
                    .build()
            )
        );
        given(repositoryMock.updateRequirement(anyString(), anyString(), anyString(), any()))
            .willAnswer(invocation -> of(invocation.getArgument(3)));

        // act
        Optional<TailoringRequirement> actual = service.handleText(
            "SAMPLE",
            "master",
            "1.1",
            "a",
            "Dies iet ein geändeter Text"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getText()).isEqualTo("Dies iet ein geändeter Text");
        assertThat(actual.get().getTextChanged()).isNotNull();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(1))
            .updateRequirement(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringRequirement.class));
    }

    @Test
    void handleText_CreatedNeuerTextReferenz_AnforderungTextAktualisiertReferenzMod() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringRequirement.builder()
                    .text("Der Text vor der Änderung")
                    .selected(TRUE)
                    .reference(Reference.builder().text("Reference").build())
                    .build()
            )
        );
        given(repositoryMock.updateRequirement(anyString(), anyString(), anyString(), any()))
            .willAnswer(invocation -> of(invocation.getArgument(3)));

        // act
        Optional<TailoringRequirement> actual = service.handleText(
            "SAMPLE",
            "master",
            "1.1",
            "a",
            "Dies iet ein geändeter Text"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getText()).isEqualTo("Dies iet ein geändeter Text");
        assertThat(actual.get().getTextChanged()).isNotNull();
        assertThat(actual.get().getReference().getChanged()).isTrue();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(1))
            .updateRequirement(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringRequirement.class));
    }

    @Test
    void handleText_NoTextChange_AnforderungNichtAktualisiert() {
        // arrange
        given(predicateMock.apply("SAMPLE", "master")).willReturn(TRUE);
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(of(
                TailoringRequirement.builder()
                    .text("Der Text vor der Änderung")
                    .selected(TRUE)
                    .build()
            )
        );
        given(repositoryMock.updateRequirement(anyString(), anyString(), anyString(), any()))
            .willAnswer(invocation -> of(invocation.getArgument(3)));

        // act
        Optional<TailoringRequirement> actual = service.handleText(
            "SAMPLE",
            "master",
            "1.1",
            "a",
            "Der Text vor der Änderung"
        );

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getText()).isEqualTo("Der Text vor der Änderung");
        assertThat(actual.get().getTextChanged()).isNull();

        verify(predicateMock, times(1)).apply("SAMPLE", "master");
        verify(repositoryMock, times(0))
            .updateRequirement(eq("SAMPLE"), eq("master"), eq("1.1"), any(TailoringRequirement.class));
    }
}
