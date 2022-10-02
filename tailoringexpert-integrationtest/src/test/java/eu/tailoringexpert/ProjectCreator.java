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

import eu.tailoringexpert.project.CreateProjectTO;
import eu.tailoringexpert.project.ProjectService;
import eu.tailoringexpert.screeningsheet.ScreeningSheetService;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static java.nio.file.Files.newInputStream;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class ProjectCreator implements Supplier<CreateProjectTO> {

    @NonNull
    private ProjectService projectService;

    @NonNull
    private ScreeningSheetService screeningSheetService;

    @Getter
    private String screeningSheetResourceName = "src/test/resources/screeningsheet.pdf";

    @SneakyThrows
    @Override
    public CreateProjectTO get() {
        byte[] data;
        try (InputStream is = newInputStream(Paths.get(screeningSheetResourceName))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        return projectService.createProject("8.2.1", data, screeningSheetService.calculateSelectionVector(data), null);
    }
}
