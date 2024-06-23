package eu.tailoringexpert.repository;

import eu.tailoringexpert.domain.TailoringCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access layer of {@link TailoringCatalogEntity}.
 *
 * @author Michael Bädorf
 */
public interface TailoringCatalogRepository extends JpaRepository<TailoringCatalogEntity, Long> {

    /**
     * Checks if base catalog of requested version already used in any tailoring catalog.
     *
     * @param version version of base catalog to check usage of
     * @return true, of base catalog is used
     */
    boolean existsByVersion(String version);
}
