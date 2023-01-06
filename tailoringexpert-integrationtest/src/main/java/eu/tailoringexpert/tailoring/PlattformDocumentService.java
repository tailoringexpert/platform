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

import eu.tailoringexpert.Tenant;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static java.util.Optional.ofNullable;

@Tenant("plattform")
@RequiredArgsConstructor
public class PlattformDocumentService implements DocumentService {
    @NonNull
    private DocumentCreator requirementDocumentCreator;

    @NonNull
    private DocumentCreator cmDocumentCreator;

    @NonNull
    private DocumentCreator comparisionDocumentCreator;

    @NonNull
    private DocumentCreator drdDocumentCreator;

    @NonNull
    private DocumentCreator catalogSpreadsheetCreator;

    @NonNull
    private DocumentCreator cmSpreadsheetCreator;

    private static final String PARAMETER_PROJEKT = "PROJEKT";

    private static final String PARAMETER_DATUM = "DATUM";

    private static final String PARAMETER_DOKUMENT = "DOKUMENT";

    private static final String PARAMETER_SHOWALL = "SHOW_ALL";

    private static final String PARAMETER_SELECTIONVECTOR = "SELECTIONVECTOR";
    private static final String PARAMETER_DRD_DOCID = "${DRD_DOCID}";

    private static final String PARAMETER_KATALOG_DOCID = "KATALOG_DOCID";

    private static final String PATTERN_DATUM = "dd.MM.yyyy";

    private static final String FORMAT_BASIS_DATEINAME = "%s-AR-ZS-DLR-%s-DV-%s";

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createRequirementDocument(Tailoring tailoring, LocalDateTime creationTimestamp) {
        return createTailoringRequirementDocument(tailoring, creationTimestamp, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createComparisonDocument(Tailoring tailoring, LocalDateTime creationTimestamp) {
        Map<String, Object> platzhalter = new HashMap<>();
        platzhalter.put(PARAMETER_PROJEKT, tailoring.getScreeningSheet().getProject());
        platzhalter.put(PARAMETER_DATUM, creationTimestamp.format(DateTimeFormatter.ofPattern(PATTERN_DATUM)));

        String docId = String.format("%s-AR-ZS-DLR-%s-DV-Tailoring-Diffs",
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier());
        File result = comparisionDocumentCreator.createDocument(docId, tailoring, platzhalter);

        return ofNullable(result);

    }

    @Override
    public Collection<File> createAll(Tailoring tailoring, LocalDateTime creationTimestamp) {
        Collection<File> result = new LinkedList<>();

        createTailoringRequirementDocument(tailoring, creationTimestamp, false).ifPresent(result::add);
        createComparisonDocument(tailoring, creationTimestamp).ifPresent(result::add);
        createDRDDokument(tailoring, creationTimestamp).ifPresent(result::add);
        createCMDokument(tailoring, creationTimestamp).ifPresent(result::add);
        createCMSpreadsheetDokument(tailoring, creationTimestamp).ifPresent(result::add);
        createTailoringRequirementDokument(tailoring, creationTimestamp).ifPresent(result::add);

        return result;
    }

    Optional<File> createDRDDokument(Tailoring tailoring, LocalDateTime currentTime) {
        Map<String, Object> platzhalter = new HashMap<>();
        platzhalter.put(PARAMETER_PROJEKT, tailoring.getScreeningSheet().getProject());
        platzhalter.put(PARAMETER_DATUM, currentTime.format(DateTimeFormatter.ofPattern(PATTERN_DATUM)));
        platzhalter.put(PARAMETER_DOKUMENT, String.format("%s-AR-ZS-DLR-%s-DV",
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier()));

        String docId = String.format(FORMAT_BASIS_DATEINAME,
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier(),
            "DRD");
        File result = drdDocumentCreator.createDocument(docId, tailoring, platzhalter);

        return ofNullable(result);
    }

    Optional<File> createCMDokument(Tailoring tailoring, LocalDateTime currentTime) {
        String docId = String.format(FORMAT_BASIS_DATEINAME,
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier(),
            "CM");

        String katalogDocId = String.format(FORMAT_BASIS_DATEINAME,
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier(),
            "Product Assurance Safety Sustainability Requirements");

        String drdDocId = String.format(FORMAT_BASIS_DATEINAME,
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier(),
            "DRD");

        Map<String, Object> platzhalter = new HashMap<>();
        platzhalter.put(PARAMETER_PROJEKT, tailoring.getScreeningSheet().getProject());
        platzhalter.put(PARAMETER_DATUM, currentTime.format(DateTimeFormatter.ofPattern(PATTERN_DATUM)));
        platzhalter.put(PARAMETER_DOKUMENT, docId);
        platzhalter.put(PARAMETER_KATALOG_DOCID, katalogDocId);
        platzhalter.put("DRD_DOCID", drdDocId);

        File result = cmDocumentCreator.createDocument(docId, tailoring, platzhalter);

        return ofNullable(result);
    }

    Optional<File> createCMSpreadsheetDokument(Tailoring tailoring, LocalDateTime currentTime) {
        Map<String, Object> platzhalter = new HashMap<>();
        platzhalter.put(PARAMETER_PROJEKT, tailoring.getScreeningSheet().getProject());
        platzhalter.put(PARAMETER_DATUM, currentTime.format(DateTimeFormatter.ofPattern(PATTERN_DATUM)));

        String docId = String.format(FORMAT_BASIS_DATEINAME,
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier(),
            "CM");

        File result = cmSpreadsheetCreator.createDocument(docId, tailoring, platzhalter);

        return ofNullable(result);

    }

    Optional<File> createTailoringRequirementDokument(Tailoring tailoring, LocalDateTime currentTime) {
        Map<String, Object> platzhalter = new HashMap<>();
        platzhalter.put(PARAMETER_PROJEKT, tailoring.getScreeningSheet().getProject());
        platzhalter.put(PARAMETER_DATUM, currentTime.format(DateTimeFormatter.ofPattern(PATTERN_DATUM)));

        String docId = String.format(FORMAT_BASIS_DATEINAME,
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier(),
            "CONFIG");

        File result = catalogSpreadsheetCreator.createDocument(docId, tailoring, platzhalter);

        return ofNullable(result);

    }

    Optional<File> createTailoringRequirementDocument(Tailoring tailoring, LocalDateTime currentTime, boolean internal) {
        String docId = String.format(FORMAT_BASIS_DATEINAME,
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier(),
            "Product Assurance Safety Sustainability Requirements");

        String drdDocId = String.format(FORMAT_BASIS_DATEINAME,
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier(),
            "DRD");

        Map<String, Object> platzhalter = new HashMap<>();
        platzhalter.put(PARAMETER_PROJEKT, tailoring.getScreeningSheet().getProject());
        platzhalter.put(PARAMETER_DATUM, currentTime.format(DateTimeFormatter.ofPattern(PATTERN_DATUM)));
        platzhalter.put(PARAMETER_DRD_DOCID, drdDocId);
        platzhalter.put(PARAMETER_SHOWALL, Boolean.toString(internal));

        platzhalter.put(PARAMETER_DOKUMENT, String.format("%s-AR-ZS-DLR-%s-DV",
            tailoring.getScreeningSheet().getProject(),
            tailoring.getIdentifier())
        );

        List<DocumentSelectionVector> selectionVector = tailoring.getSelectionVector().getLevels()
            .entrySet()
            .stream().map(entry -> DocumentSelectionVector.builder().type(entry.getKey()).level(entry.getValue()).build())
            .toList();
        platzhalter.put(PARAMETER_SELECTIONVECTOR, selectionVector);

        File result = requirementDocumentCreator.createDocument(docId, tailoring, platzhalter);

        return ofNullable(result);
    }
}
