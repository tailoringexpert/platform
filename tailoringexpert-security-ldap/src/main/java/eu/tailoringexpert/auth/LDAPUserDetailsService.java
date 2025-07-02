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
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;

import java.util.Collection;
import java.util.List;

@Log4j2
public class LDAPUserDetailsService extends LdapUserDetailsService {

    Collection<String> definedRoles;
    JWTService tokenProvider;
    AuthenticationManager authenticationManager;

    public LDAPUserDetailsService(
        AuthenticationManager authenticationManager,
        LdapUserSearch userSearch,
        Collection<String> definedRoles,
        LdapAuthoritiesPopulator authoritiesPopulator,
        JWTService tokenProvider) {
        super(userSearch, authoritiesPopulator);
        this.definedRoles = definedRoles;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }


    /**
     * Authenticates the user from remote LDAP by using <code>userId</code> and <code>password</code> credentials provided.
     * Populates the {@link SecurityContextHolder} with an {@link Authentication} object.
     *
     * @param userId
     * @param password
     * @return An {@link Authentication} object populated with user detail information and a JWT token to be used in following calls.
     */
    public Authentication authenticate(@NonNull String userId, @NonNull String password) {
        UserDetails userDetails = loadUserByUsername(userId);
        Collection<? extends GrantedAuthority> grantedAuthorities = userDetails.getAuthorities();

        List<String> userRoles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(definedRoles::contains)
            .toList();

        log.info("Authentication of {} successfull! Users groups are: {}", userId, grantedAuthorities);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), password, userDetails.getAuthorities());
        authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return Authentication.builder()
            .userId(userId)
            .accessToken(tokenProvider.generateToken(userId, userRoles))
            .refreshToken(tokenProvider.generateRefreshToken(userId, userRoles))
            .build();
    }

    public Authentication refresh(String userId, String refreshToken) {
        Claims claims = tokenProvider.getClaimsOf(refreshToken);

        if (!userId.equals(tokenProvider.getUserNameOf(claims))) {
            throw log.throwing(new AuthenticationServiceException("User not owner of token"));
        }

        if (tokenProvider.isTokenExpired(claims)) {
            throw log.throwing(new AuthenticationServiceException("Refrestoken expired"));
        }

        return Authentication.builder()
            .userId(userId)
            .accessToken(tokenProvider.generateToken(userId, tokenProvider.extractGrantedAuthorities(claims)))
            .refreshToken(tokenProvider.generateRefreshToken(userId, tokenProvider.extractGrantedAuthorities(claims)))
            .build();

    }


}
