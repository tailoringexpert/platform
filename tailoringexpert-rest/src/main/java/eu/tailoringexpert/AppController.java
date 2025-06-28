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

import eu.tailoringexpert.domain.ResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

import static eu.tailoringexpert.domain.ResourceMapper.*;
import static org.springframework.hateoas.CollectionModel.empty;

/**
 * REST-Controller for providing main rels of platform.
 *
 * @author Michael Bädorf
 */
@Tag(name = "App Controller", description = "Main controller to retrieve top level resource urls")
@Log4j2
@RestController
@RequiredArgsConstructor
public class AppController {

    @NonNull
    private ResourceMapper mapper;

    @Operation(summary = "Retrieve main urls of application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Links to to level resources",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CollectionModel.class))))
    })
    @GetMapping(value = "/", produces = {"application/hal+json"})
    public <T> ResponseEntity<CollectionModel<T>> getLinks() {
        log.traceEntry();
        Map<String, String> parameter = Collections.emptyMap();
        ResponseEntity<CollectionModel<T>> result = ResponseEntity
            .ok()
            .body(empty(
                    mapper.createLink("login", AUTH_LOGIN, parameter),
                    mapper.createLink("catalog", BASECATALOG, parameter),
                    mapper.createLink("project", PROJECTS, parameter),
                    mapper.createLink("screeningsheet", SCREENINGSHEET, parameter),
                    mapper.createLink("selectionvector", SELECTIONVECTOR_PROFILE, parameter),
                    mapper.createLink("catalogconversion", BASECATALOG_CONVERT_EXCEL, parameter),
                    mapper.createLink("catalogpreview", BASECATALOG_PREVIEW_PDF, parameter)
                )
            );

        log.traceExit();
        return result;
    }
}
