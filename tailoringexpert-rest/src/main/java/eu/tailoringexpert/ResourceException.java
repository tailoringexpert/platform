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


import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception for using in RESTController.
 *
 * @author Michael Bädof
 */
public class ResourceException extends RuntimeException {

    private static final long serialVersionUID = -1920260812248865649L;

    @Getter
    private final HttpStatus httpStatus;

    /**
     * Creates RuntimeException with provided message.
     * <p>
     * Der Auslöser wird nicht initialisiert und kann ggf. mittels {@link #initCause} gesetzt werden.
     *
     * @param httpStatus http state to return
     * @param message    text to use. Text will be used as {@link #getMessage()}.
     */
    public ResourceException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
