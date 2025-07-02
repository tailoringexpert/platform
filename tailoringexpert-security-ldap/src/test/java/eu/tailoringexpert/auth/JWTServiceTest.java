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
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Date;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@Log4j2
class JWTServiceTest {

    JWTService service;

    @BeforeEach
    void beforeEach() {
        this.service = spy(new JWTService("Test1234Test1234Test1234Test1234Test1234Test1234", 60000L, 600000L));
    }

    @Test
    void extractGrantedAuthorities() {
        // arrange
        Claims claims = Jwts.claims()
            .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
            .build();

        // act
        Collection<String> actual = service.extractGrantedAuthorities(claims);

        // assert
        assertThat(actual).
            hasSize(2)
            .containsOnly("ROLE_ROLE1", "ROLE_ROLE2");
    }

    @Test
    void generateRefreshToken() {
        // arrange
        String token = service.generateToken("f_demo", of("ROLE_ROLE1", "ROLE_ROLE2"));

        // act
        String actual = service.generateRefreshToken(token, of("ROLE_ROLE1", "ROLE_ROLE2"));

        // assert
        assertThat(actual).isNotNull()
            .isNotEqualTo(token);
    }

    @Test
    void isTokenExpired_TokenExpire_TrueReturned() {
        //arrange
        Claims claims = Jwts.claims()
            .subject("f_demo")
            .expiration(new Date(System.currentTimeMillis() - 60000))
            .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
            .build();

        // act
        boolean actual = service.isTokenExpired(claims);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void isTokenExpired_TokenValid_FalseReturned() {
        //arrange
        Claims claims = Jwts.claims()
            .subject("f_demo")
            .expiration(new Date(System.currentTimeMillis() + 60000))
            .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
            .build();

        // act
        boolean actual = service.isTokenExpired(claims);

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void getClaimsOf() {
        // arrange
        String token = service.generateToken("f_demo", of("ROLE_ROLE1"));

        // act
        Claims actual = service.getClaimsOf(token);

        // assert
        assertThat(actual.getSubject()).isEqualTo("f_demo");
    }

    @Test
    void getUserNameOf_UserExists_NameReturned() {
        //arrange
        Claims claims = Jwts.claims()
            .subject("f_demo")
            .expiration(new Date(System.currentTimeMillis() + 60000))
            .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
            .build();

        // act
        String actual = service.getUserNameOf(claims);

        // assert
        assertThat(actual)
            .isNotNull()
            .isEqualTo("f_demo");
    }

    @Test
    void getUserNameOf_UserNotExists_NullReturned() {
        //arrange
        Claims claims = Jwts.claims()
            .expiration(new Date(System.currentTimeMillis() + 60000))
            .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
            .build();

        // act
        String actual = service.getUserNameOf(claims);

        // assert
        assertThat(actual)
            .isNull();
    }
}
