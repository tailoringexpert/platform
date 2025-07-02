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
package eu.tailoringexpert.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class AuthExceptionHandlerAdviceTest {

    AuthExceptionHandlerAdvice advice;

    @BeforeEach
    void beforeEach() {
        this.advice = new AuthExceptionHandlerAdvice();
    }

    @Test
    void handleException_AuthenticationExceptionInput_StateUnauthorizedReturned() {
        // arrange
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        AuthenticationException exception = new BadCredentialsException("Invalid credentials");
        exception.setStackTrace(stackTrace);

        // act
        ResponseEntity<String> actual = advice.handleException(exception);

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(UNAUTHORIZED);
        assertThat(actual.getBody()).isEqualTo(Arrays.toString(stackTrace));

    }
}
