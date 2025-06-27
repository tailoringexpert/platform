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
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.ResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import static eu.tailoringexpert.domain.ResourceMapper.AUTH_LOGIN;
import static eu.tailoringexpert.domain.ResourceMapper.AUTH_REFRESH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Log4j2
@RequiredArgsConstructor
public class AuthenticationController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private AuthenticationService authenticationService;

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
        PathContext.PathContextBuilder pathContext = PathContext.builder();

        Authentication authenticate = authenticationService.authenticate(
            authenticationRequest.getUserId(),
            authenticationRequest.getPassword()
        );
        return ok()
            .body(mapper.toResource(pathContext, authenticate));
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
        PathContext.PathContextBuilder pathContext = PathContext.builder();

        Authentication authenticate = authenticationService.refresh(
            authRefreshRequest.getUserId(),
            authRefreshRequest.getRefreshToken()
        );
        return ok()
            .body(mapper.toResource(pathContext, authenticate));
    }
}
