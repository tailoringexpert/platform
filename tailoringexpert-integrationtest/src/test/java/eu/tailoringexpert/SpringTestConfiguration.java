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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.catalog.CatalogService;
import eu.tailoringexpert.project.ProjectService;
import eu.tailoringexpert.screeningsheet.ScreeningSheetService;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.Rollback;

import java.io.File;

@Configuration
@PropertySource({
    "classpath:application.properties",
    "classpath:application-test.properties"
})
@Import({
    LiquibaseAutoConfiguration.class
})
@Rollback
@Log4j2
public class SpringTestConfiguration {

    static {
        System.setProperty("liquibase.secureParsing", "false");
    }

    @Bean
    ProjectCreator projectCreator(@NonNull ProjectService projectService,
                                  @NonNull ScreeningSheetService screeningSheetService) {
        return new ProjectCreator(projectService, screeningSheetService);
    }

    @Bean
    BaseCatalogImport baseCatalogImport(
        @NonNull ObjectMapper objectMapper,
        @NonNull CatalogService catalogService) {
        return new BaseCatalogImport(objectMapper, catalogService);
    }
}
