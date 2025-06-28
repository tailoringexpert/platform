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
import eu.tailoringexpert.domain.AuthenticationRefreshRequest;
import eu.tailoringexpert.domain.AuthenticationRequest;
import eu.tailoringexpert.domain.AuthenticationResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Log4j2
@RequiredArgsConstructor
public class AuthenticationController {

    private static final String AUTH_LOGIN = "auth/login";
    private static final String AUTH_REFRESH = "auth/refresh";

    private static final String REL_REFRESH = "refresh";

    @NonNull
    private String contextPath;

    @NonNull
    private LDAPUserDetailsService service;

    @Operation(summary = "Login")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "User successful authenticated",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = AuthenticationResource.class)))
    })
    @PostMapping(
        value = AUTH_LOGIN,
        consumes = {APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<AuthenticationResource> postAuthenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        log.info("Authentication request for user {} received!", authenticationRequest.getUserId());
        Authentication authenticate = service.authenticate(
            authenticationRequest.getUserId(),
            authenticationRequest.getPassword()
        );

        return ok()
            .body(toResource(authenticate));
    }


    @Operation(summary = "Refresh expired access token")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Access token successful refreshed",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = AuthenticationResource.class)))
    })
    @PostMapping(
        value = AUTH_REFRESH,
        consumes = {APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<AuthenticationResource> postRefreshAccessToken(@RequestBody AuthenticationRefreshRequest authRefreshRequest) {
        log.info("Authentication refresh request for user {} received!", authRefreshRequest.getUserId());

        Authentication authenticate = service.refresh(
            authRefreshRequest.getUserId(),
            authRefreshRequest.getRefreshToken()
        );

        return ok()
            .body(toResource(authenticate));
    }

    private AuthenticationResource toResource(Authentication domain) {
        return AuthenticationResource.builder()
            .userId(domain.getUserId())
            .accessToken(domain.getAccessToken())
            .refreshToken(domain.getRefreshToken())
            .links(List.of(
                Link.of(UriTemplate.of(this.contextPath + "/" + AUTH_REFRESH), REL_REFRESH))
            )
            .build();
    }

}
