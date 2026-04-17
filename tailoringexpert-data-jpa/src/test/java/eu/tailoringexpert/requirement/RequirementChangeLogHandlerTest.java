/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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

import eu.tailoringexpert.domain.TailoringRequirementChangeEntity;
import eu.tailoringexpert.domain.TailoringRequirementEntity;
import eu.tailoringexpert.repository.TailoringRequirementChangeRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
class RequirementChangeLogHandlerTest {

    Supplier<String> usernameMock;
    TailoringRequirementChangeRepository tailoringRequirementRepositoryMock;
    RequirementChangeLogHandler logHandler;

    @BeforeEach
    void beforeEach() {
        this.usernameMock = mock(Supplier.class);
        this.tailoringRequirementRepositoryMock = mock(TailoringRequirementChangeRepository.class);

        this.logHandler = new RequirementChangeLogHandler(
            this.usernameMock,
            this.tailoringRequirementRepositoryMock
        );
    }

    @Test
    void accept_TextChanged_TailoringRequirementTextChangeCreated() {
        // arrange
        TailoringRequirementEntity original = TailoringRequirementEntity.builder()
            .id(1L)
            .text("old")
            .selected(FALSE)
            .build();
        TailoringRequirementEntity revised = TailoringRequirementEntity.builder()
            .id(1L)
            .text("new")
            .selected(FALSE)
            .build();

        ZonedDateTime now =
            ZonedDateTime.of(2020, 12, 1, 8, 0, 0, 0, ZoneId.systemDefault());

        // act
        try (MockedStatic<ZonedDateTime> dateTimeMock = mockStatic(ZonedDateTime.class)) {
            dateTimeMock.when(ZonedDateTime::now).thenReturn(now);
            logHandler.accept(original, revised);
        }

        // assert
        verify(tailoringRequirementRepositoryMock, times(1)).save(TailoringRequirementChangeEntity.builder()
            .changeType("TEXT")
            .requirementId(1L)
            .modificationTimestamp(now)
            .old("old")
            .changed("new")
            .build()
        );
    }

    @Test
    void accept_StatetChanged_TailoringRequirementStateChangeCreated() {
        // arrange
        TailoringRequirementEntity original = TailoringRequirementEntity.builder()
            .id(1L)
            .text("old")
            .selected(FALSE)
            .build();
        TailoringRequirementEntity revised = TailoringRequirementEntity.builder()
            .id(1L)
            .text("old")
            .selected(TRUE)
            .build();

        ZonedDateTime now =
            ZonedDateTime.of(2020, 12, 1, 8, 0, 0, 0, ZoneId.systemDefault());

        // act
        try (MockedStatic<ZonedDateTime> dateTimeMock = mockStatic(ZonedDateTime.class)) {
            dateTimeMock.when(ZonedDateTime::now).thenReturn(now);
            logHandler.accept(original, revised);
        }

        // assert
        verify(tailoringRequirementRepositoryMock, times(1)).save(TailoringRequirementChangeEntity.builder()
            .requirementId(1L)
            .changeType("SELECTED")
            .modificationTimestamp(now)
            .old(String.valueOf(FALSE))
            .changed(String.valueOf(TRUE))
            .build()
        );
    }
}
