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

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

/**
 * Advice für die Behandlung von Exceptions in Tailoring RestController.
 */
@ControllerAdvice
@Log4j2
public class ExceptionHandlerAdvice {

    @ExceptionHandler(NoSuchMethodException.class)
    public ResponseEntity<String> handleException(NoSuchMethodException e) {
        log.catching(e);
        return ResponseEntity.status(BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(TailoringexpertException.class)
    public ResponseEntity<String> handleException(TailoringexpertException e) {
        log.catching(e);
        return ResponseEntity.status(PRECONDITION_FAILED).body(e.getMessage());
    }

    /**
     * Behandlung einer Exception vom Typ {@link ResourceException}.
     *
     * @param e Die zu behandelnde Exception
     * @return Antwort des Controllers auf die Exception
     */
    @ExceptionHandler(ResourceException.class)
    public ResponseEntity<String> handleException(ResourceException e) {
        log.catching(e);
        return ResponseEntity.status(e.getHttpStatus()).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.catching(e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Arrays.toString(e.getStackTrace()));
    }
}
