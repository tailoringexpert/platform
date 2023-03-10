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
package eu.tailoringexpert.tailoring;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.tailoringexpert.App;
import eu.tailoringexpert.BaseCatalogImport;
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.project.CreateProjectTO;
import eu.tailoringexpert.project.ProjectService;
import eu.tailoringexpert.screeningsheet.ScreeningSheetService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.BiConsumer;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@Log4j2
@SpringJUnitConfig(classes = {App.class})
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class PlattformTailoringCatalog2JsonTest {

    @Autowired
    BaseCatalogImport baseCatalog;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TailoringService tailoringService;

    @Autowired
    private ScreeningSheetService screeningSheetService;

    private BiConsumer<String, byte[]> toFile = new FileSaver();

    @BeforeEach
    void setup() throws Exception {
        log.debug("setup started");

        TenantContext.setCurrentTenant("plattform");
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest())
        );
        baseCatalog.get();

        log.debug("setup completed");
    }

    @Test
    void tailoringcatalog2Json() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        SelectionVector selectionVector = screeningSheetService.calculateSelectionVector(data);
        CreateProjectTO project = projectService.createProject("8.2.1", data, selectionVector, null);

        // act
        Optional<Catalog<TailoringRequirement>> actual = tailoringService.getCatalog(project.getProject(), project.getTailoring());

        // assert
        assertThat(actual).isPresent();
        toFile.accept("tailoringkatalog.json", objectMapper.writeValueAsBytes(actual));
    }

}
