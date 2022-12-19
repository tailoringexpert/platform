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
package eu.tailoringexpert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHandlerAdviceTest {

    ExceptionHandlerAdvice advice;

    @BeforeEach
    void beforeEach() {
        this.advice = new ExceptionHandlerAdvice();
    }

    @Test
    void handleException_ExceptionInput_State500Returned() {
        // arrange
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Exception exception = new Exception();
        exception.setStackTrace(stackTrace);

        // act
        ResponseEntity<String> actual = advice.handleException(exception);

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(actual.getBody()).isEqualTo(Arrays.toString(stackTrace));

    }

    @Test
    void handleException_ResourceExceptionInput_ProvidedStateReturned() {
        // arrange
        ResourceException exception = new ResourceException(HttpStatus.NOT_FOUND, "Die Resource konnte nicht gefunden werden");

        // act
        ResponseEntity<String> actual = advice.handleException(exception);

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(actual.getBody()).isEqualTo("Die Resource konnte nicht gefunden werden");

    }

    @Test
    void handleException_TailoringExceptionInput_State412Returned() {
        // arrange
        TailoringexpertException exception = new TailoringexpertException("Something unexpected happened");

        // act
        ResponseEntity<String> actual = advice.handleException(exception);

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.PRECONDITION_FAILED);
        assertThat(actual.getBody()).isEqualTo("Something unexpected happened");
    }

    @Test
    void handleException_NoSuchMethodExceptionInput_State400Returned() {
        // arrange
        NoSuchMethodException exception = new NoSuchMethodException("Method not implemented");

        // act
        ResponseEntity<String> actual = advice.handleException(exception);

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(actual.getBody()).isEqualTo("Method not implemented");
    }
}
