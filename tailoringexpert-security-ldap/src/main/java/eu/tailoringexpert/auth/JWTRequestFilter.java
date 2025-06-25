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

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Log4j2
@RequiredArgsConstructor
public class JWTRequestFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer";

    @NonNull
    private JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHORIZATION);

        if (nonNull(authHeader) && authHeader.startsWith(BEARER)) {
            try {
                verifyAndAuthenticate(request, authHeader);
            } catch (Exception ex) {
                log.error("Authentication failed.");
            }
        }

        filterChain.doFilter(request, response);
    }

    void verifyAndAuthenticate(HttpServletRequest request, String bearer) {
        final String token = bearer.trim().substring(BEARER.length() + 1);
        final Claims claims = jwtService.getClaimsOf(token);

        if (jwtService.isTokenExpired(claims)) {
            throw log.throwing(new AuthenticationServiceException("Token expired"));
        }

        final String username = jwtService.getUserNameOf(claims);
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (nonNull(username) && isNull(authentication)) {
            List<SimpleGrantedAuthority> authorities = jwtService.extractGrantedAuthorities(claims)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
}
