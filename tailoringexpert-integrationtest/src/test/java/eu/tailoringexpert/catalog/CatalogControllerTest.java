package eu.tailoringexpert.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.LiquibaseRunner;
import eu.tailoringexpert.SpringConfiguration;
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
@SpringJUnitConfig(classes = {SpringConfiguration.class})
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
        ResponseEntity actual = controller.importCatalog(catalog);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);
    }


    @Test
    @DirtiesContext
    void getCatalogs_ExistingsCatalogs_CatalogListReturned() throws IOException {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = newInputStream(get("src/test/resources/basecatalog.json"))) {
            assert nonNull(is);

            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }
        controller.importCatalog(catalog);

        // act
        ResponseEntity<CollectionModel<EntityModel<BaseCatalogVersionResource>>> actual = controller.getCatalogs();

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody().getContent()).hasSize(1);
    }

}


