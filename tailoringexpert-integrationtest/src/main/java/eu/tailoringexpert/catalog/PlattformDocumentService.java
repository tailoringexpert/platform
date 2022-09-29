package eu.tailoringexpert.catalog;

import eu.tailoringexpert.Tenant;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Plattform implementation of @see {@link DocumentService} for creating a base catalog document.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Log4j2
@Tenant("plattform")
public class PlattformDocumentService implements DocumentService {

    @NonNull
    private DocumentCreator catalogCreator;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createCatalog(Catalog<BaseRequirement> catalog, LocalDateTime creationTimestamp) {
        log.info("STARTED | trying to create pdf of catalog version {}", catalog.getVersion());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("DRD_DOCID", "RD-PS-01");

        String docId = String.format("PA,Safety & Sustainability-Katalog_%s", catalog.getVersion());
        File dokument = catalogCreator.createDocument(docId, catalog, placeholders);

        log.info("FINISHED | created catalog document  {}", docId);
        return ofNullable(dokument);

    }
}

