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
package eu.tailoringexpert.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.LiquibaseRunner;
import eu.tailoringexpert.SpringTestConfiguration;
import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.BaseCatalogVersionResource;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Log4j2
@SpringJUnitConfig(classes = {SpringTestConfiguration.class})
@EnableTransactionManagement
class CatalogControllerTest {

    @Autowired
    LiquibaseRunner liquibase;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CatalogController controller;

    @BeforeEach
    void setup() throws Exception {
        log.debug("setup started");

        TenantContext.setCurrentTenant("plattform");
        liquibase.dropAll();
        liquibase.runChangelog("db-tailoringexpert-plattform-install.xml", "db-tailoringexpert-plattform-update.xml");

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest())
        );

        log.debug("setup completed");
    }

    @Test
    @DirtiesContext
    void importCatalog_NewVersion_CatalogImported() throws IOException {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = newInputStream(get("src/test/resources/basecatalog.json"))) {
            assert nonNull(is);

            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }

        // act
        ResponseEntity actual = controller.postCatalog(catalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);
    }


    @Test
    @DirtiesContext
    void getCatalogs_CatalogsExists_CatalogListReturned() throws IOException {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = newInputStream(get("src/test/resources/basecatalog.json"))) {
            assert nonNull(is);

            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }
        controller.postCatalog(catalog);

        // act
        ResponseEntity<CollectionModel<EntityModel<BaseCatalogVersionResource>>> actual = controller.getCatalogs();

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody().getContent()).hasSize(1);
    }

}


