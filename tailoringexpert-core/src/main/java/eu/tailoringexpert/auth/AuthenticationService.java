/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2025 Michael Bädorf and others
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

import eu.tailoringexpert.domain.Authentication;

public interface AuthenticationService {

    /**
     * Tries to authenticate a user with provided password.
     *
     * @param userId   id of user to authenticate
     * @param password password of user
     * @return user authentication if successful
     */
    Authentication authenticate(String userId, String password);


    /**
     * Refresged access token of user.
     *
     * @param userId user to get new access token
     * @param token  refresh token of user
     * @return user authentication if successful
     */
    Authentication refresh(String userId, String token);
}
