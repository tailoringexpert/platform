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
package de.baedorf.tailoringexpert.screeningsheet;

import de.baedorf.tailoringexpert.domain.PathContext;
import de.baedorf.tailoringexpert.domain.ResourceMapper;
import de.baedorf.tailoringexpert.domain.ScreeningSheetResource;
import de.baedorf.tailoringexpert.domain.TailoringResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static de.baedorf.tailoringexpert.domain.ResourceMapper.SCREENINGSHEET;
import static java.util.Optional.ofNullable;

@Log
@RequiredArgsConstructor
@Tag(name = "ScreeningSheet Controller", description = "Erstellung von ScreeningSheets")
@RestController
public class ScreeningSheetController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private ScreeningSheetService screeningSheetService;

    @Operation(summary = "Parsen des überhebenen Screeningsheets")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Screeningsheet wurde ausgewertet",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringResource.class)))
    })
    @PostMapping(value = SCREENINGSHEET, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<ScreeningSheetResource>> createScreeningSheet(
        @RequestPart("datei") MultipartFile datei) throws IOException {

        return ofNullable(screeningSheetService.createScreeningSheet(datei.getBytes()))
            .map(screeningSheet -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(PathContext.builder(), screeningSheet))))
            .orElseGet(() -> ResponseEntity.notFound().build());


    }

}
