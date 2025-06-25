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
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;


import static io.jsonwebtoken.io.Decoders.BASE64;

@RequiredArgsConstructor
@Log4j2
public class JWTService {

    private static final String GRANTED_AUTHORITIES = "grantedAuthorities";

    @NonNull
    private String secret;

    @NonNull
    private Long jwtExpiresSeconds;

    @NonNull
    private Long jwtRefreshExpiresSeconds;


    public String generateToken(String userId, Collection<String> grantedAuthorities) {
        return Jwts.builder()
            .subject(userId)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + jwtExpiresSeconds))
            .signWith(getSignInKey())
            .claim(GRANTED_AUTHORITIES, grantedAuthorities)
            .compact();
    }

    public String generateRefreshToken(String userId, Collection<String> grantedAuthorities) {
        return Jwts.builder()
            .subject(userId)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpiresSeconds))
            .signWith(getSignInKey())
            .claim(GRANTED_AUTHORITIES, grantedAuthorities)
            .compact();
    }

    public Claims getClaimsOf(String token) {
        return Jwts
            .parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }


    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }


    public String getUserNameOf(Claims claims) {
        return claims.getSubject();
    }


    public Collection<String> extractGrantedAuthorities(Claims claims) {
        return claims.get(GRANTED_AUTHORITIES, Collection.class);
    }


    private SecretKey getSignInKey() {
        byte[] keyBytes = BASE64.decode(this.secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
