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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.domain.Authentication;
import eu.tailoringexpert.domain.AuthenticationRefreshRequest;
import eu.tailoringexpert.domain.AuthenticationRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


class AuthenticationControllerTest {
    private static final String URL_AUTHENTICATE = "/auth/login";
    private static final String URL_REFRESH = "/auth/refresh";

    private static final String RESPONSE_FIELD_PATH_ACCESSTOKEN = "$.accessToken";
    private static final String RESPONSE_FIELD_PATH_REFRESHTOKEN = "$.refreshToken";
    private static final String RESPONSE_FIELD_PATH_USERNAME = "$.userId";

    ObjectMapper objectMapper;

    LDAPUserDetailsService serviceMock;
    AuthenticationController controller;
    MockMvc mockMvc;


    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();

        this.serviceMock = mock(LDAPUserDetailsService.class);
        this.controller = new AuthenticationController("/api", serviceMock);
        this.mockMvc = standaloneSetup(controller).build();
    }

    @SneakyThrows
    @Test
    void testPostAuthenticateWhenProperRequestShouldReturnProperAuthentication() {
        // arrange
        String accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmX2RlbW8iLCJpYXQiOjE3NTA4NjIzMTksImV4cCI6MTc1MDg2MjM3OSwiZ3JhbnRlZEF1dGhvcml0aWVzIjpbIlJPTEVfUk9MRTEiLCJST0xFX1JPTEUyIl19.PRhs_02g7AVTlu3i2X1Q4BI-LrRkJ7y9yBWG8XpWCbI";
        String refreshToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleUpoYkdjaU9pSklVekkxTmlKOS5leUp6ZFdJaU9pSm1YMlJsYlc4aUxDSnBZWFFpT2pFM05UQTROakl6TVRrc0ltVjRjQ0k2TVRjMU1EZzJNak0zT1N3aVozSmhiblJsWkVGMWRHaHZjbWwwYVdWeklqcGJJbEpQVEVWZlVrOU1SVEVpTENKU1QweEZYMUpQVEVVeUlsMTkuUFJoc18wMmc3QVZUbHUzaTJYMVE0QkktTHJSa0o3eTl5QldHOFhwV0NiSSIsImlhdCI6MTc1MDg2MjMyMCwiZXhwIjoxNzUwODYyOTIwLCJncmFudGVkQXV0aG9yaXRpZXMiOlsiUk9MRV9ST0xFMSIsIlJPTEVfUk9MRTIiXX0.C7YBvhFdICqO1r97QNpMEabJZImIgRKQygxFl_xC2gY";
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
            .userId("f_tailor")
            .password("test1234")
            .build();

        Authentication autentication = Authentication.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId("f_tailor")
            .build();

        given(serviceMock.authenticate(authRequest.getUserId(), authRequest.getPassword()))
            .willReturn(autentication);

        // act
        ResultActions actual = mockMvc.perform(post(URL_AUTHENTICATE)
            .content(objectMapper.writeValueAsString(authRequest))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8.displayName()));

        // assert
        actual
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath(RESPONSE_FIELD_PATH_ACCESSTOKEN).value(accessToken))
            .andExpect(jsonPath(RESPONSE_FIELD_PATH_REFRESHTOKEN).value(refreshToken))
            .andExpect(jsonPath(RESPONSE_FIELD_PATH_USERNAME).value("f_tailor"));
    }

    @SneakyThrows
    @Test
    void postRefreshAccessToken() {
        // arrange
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmX2RlbW8iLCJpYXQiOjE3NTA4NjIzMTksImV4cCI6MTc1MDg2MjM3OSwiZ3JhbnRlZEF1dGhvcml0aWVzIjpbIlJPTEVfUk9MRTEiLCJST0xFX1JPTEUyIl19.PRhs_02g7AVTlu3i2X1Q4BI-LrRkJ7y9yBWG8XpWCbI";
        String refreshToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleUpoYkdjaU9pSklVekkxTmlKOS5leUp6ZFdJaU9pSm1YMlJsYlc4aUxDSnBZWFFpT2pFM05UQTROakl6TVRrc0ltVjRjQ0k2TVRjMU1EZzJNak0zT1N3aVozSmhiblJsWkVGMWRHaHZjbWwwYVdWeklqcGJJbEpQVEVWZlVrOU1SVEVpTENKU1QweEZYMUpQVEVVeUlsMTkuUFJoc18wMmc3QVZUbHUzaTJYMVE0QkktTHJSa0o3eTl5QldHOFhwV0NiSSIsImlhdCI6MTc1MDg2MjMyMCwiZXhwIjoxNzUwODYyOTIwLCJncmFudGVkQXV0aG9yaXRpZXMiOlsiUk9MRV9ST0xFMSIsIlJPTEVfUk9MRTIiXX0.C7YBvhFdICqO1r97QNpMEabJZImIgRKQygxFl_xC2gY";
        AuthenticationRefreshRequest authRequest = AuthenticationRefreshRequest.builder()
            .userId("f_tailor")
            .refreshToken(refreshToken)
            .build();

        Authentication authResponse = Authentication.builder()
            .accessToken(token)
            .userId("f_tailor")
            .build();

        given(serviceMock.refresh(authRequest.getUserId(), authRequest.getRefreshToken()))
            .willReturn(authResponse);

        // act
        ResultActions actual = mockMvc.perform(post(URL_REFRESH)
            .content(objectMapper.writeValueAsString(authRequest))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8.displayName()));

        // assert
        actual
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath(RESPONSE_FIELD_PATH_ACCESSTOKEN).value(token))
            .andExpect(jsonPath(RESPONSE_FIELD_PATH_USERNAME).value("f_tailor"));
    }
}
