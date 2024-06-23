package eu.tailoringexpert.repository;

import eu.tailoringexpert.domain.TailoringCatalogEntity;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringJUnitConfig({DBConfiguration.class})
@Transactional
class TailoringCatalogRepositoryTest {


    @Autowired
    TailoringCatalogRepository repository;

    @Test
    void existsByVersion_CatalogNotUsedInTailoringCatalogs_FalseReturned() {
        // arrange
        repository.save(TailoringCatalogEntity.builder()
                .version("8.3")
            .build());

        // act
        boolean actual = repository.existsByVersion("8.2.1");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void existsByVersion_CatalogUsedInTailoringCatalogs_TrueReturned() {
        // arrange
        repository.save(TailoringCatalogEntity.builder()
            .version("8.3")
            .build());

        // act
        boolean actual = repository.existsByVersion("8.3");

        // assert
        assertThat(actual).isTrue();
    }
}
