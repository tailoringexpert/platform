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

import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.requirement.RequirementService;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.Tailoring.TailoringBuilder;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringInformation;
import eu.tailoringexpert.domain.TailoringState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link TailoringService}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class TailoringServiceImpl implements TailoringService {

    @NonNull
    private TailoringServiceRepository repository;

    @NonNull
    private TailoringServiceMapper mapper;

    @NonNull
    private DocumentService documentService;

    @NonNull
    private RequirementService requirementService;

    @NonNull
    private Function<byte[], Map<String, Collection<ImportRequirement>>> tailoringAnforderungFileReader;

    private static final String YES = "JA";
    private static final String NO = "NEIN";

    /**
     * {@inheritDoc}
     */
    @Override
    public Tailoring createTailoring(String name,
                                     String identifier,
                                     ScreeningSheet screeningSheet,
                                     SelectionVector applicableSelectionVector,
                                     Catalog<BaseRequirement> catalog) {
        Catalog<TailoringRequirement> tailoringCatalog = mapper.toTailoringCatalog(
            catalog, screeningSheet, applicableSelectionVector
        );

        TailoringBuilder result = Tailoring.builder()
            .name(name)
            .identifier(identifier)
            .screeningSheet(screeningSheet)
            .selectionVector(applicableSelectionVector)
            .catalog(tailoringCatalog)
            .signatures(repository.getDefaultSignatures())
            .state(TailoringState.ACTIVE);

        // prüfe, ob phase(n) bereits vorhanden
        screeningSheet.getParameters()
            .stream()
            .filter(parameter -> ScreeningSheet.PHASE.equalsIgnoreCase(parameter.getCategory()))
            .findFirst()
            .ifPresent(parameter -> result.phases((Collection<Phase>) parameter.getValue()));


        return result.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Optional<Tailoring> addFile(String project, String tailoring, String filename, byte[] data) {
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }


        BigInteger hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(data));
        File file = File.builder()
            .name(filename)
            .data(data)
            .hash(hash.toString(16))
            .build();
        return repository.updateFile(project, tailoring, file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createRequirementDocument(String project, String tailoring) {
        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime creationTimestamp = LocalDateTime.now();

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }

        return documentService.createRequirementDocument(oTailoring.get(), creationTimestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createComparisonDocument(String project, String tailoring) {
        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime creationTimestamp = LocalDateTime.now();

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }

        return documentService.createComparisonDocument(oTailoring.get(), creationTimestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Catalog<TailoringRequirement>> getCatalog(@NonNull String project, @NonNull String tailoring) {
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }
        return ofNullable(oTailoring.get().getCatalog());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<TailoringRequirement>> getRequirements(@NonNull String project, @NonNull String tailoring, @NonNull String chapter) {
        Optional<Chapter<TailoringRequirement>> oChapter = getChapter(project, tailoring, chapter);
        if (oChapter.isEmpty()) {
            return empty();
        }
        return ofNullable(oChapter.get().getRequirements());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ScreeningSheet> getScreeningSheet(@NonNull String project, @NonNull String tailoring) {
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }

        return ofNullable(oTailoring.get().getScreeningSheet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SelectionVector> getSelectionVector(@NonNull String project, @NonNull String tailoring) {
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }

        return ofNullable(oTailoring.get().getSelectionVector());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> getChapter(@NonNull String project, @NonNull String tailoring, @NonNull String chapter) {
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }

        return oTailoring.get().getCatalog().getChapter(chapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Collection<DocumentSignature>> getDocumentSignatures(@NonNull String project, @NonNull String tailoring) {
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }

        return ofNullable(oTailoring.get().getSignatures());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DocumentSignature> updateDocumentSignature(@NonNull String project, @NonNull String tailoring, @NonNull DocumentSignature signature) {
        return repository.updateDocumentSignature(project, tailoring, signature);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringInformation> updateName(String project, String tailoring, @NonNull String name) {
        log.info("STARTED | updating name of {}:{} to {}", project, tailoring, name);

        // prüfe, ob es Phase mit neuem Namen bereits gibt
        if (tailoring.trim().equals(name.trim())) {
            log.info("FINISHED | name not changed because new name is empty");
            return empty();
        }

        Optional<Tailoring> tailoringWithNewName = repository.getTailoring(project, name);
        if (tailoringWithNewName.isPresent()) {
            log.info("FINISHED | name not changed because it already exits");
            return empty();
        }

        Optional<Tailoring> oTailoring = repository.updateName(project, tailoring, name);
        Optional<TailoringInformation> result = oTailoring.map(updatedPhase -> mapper.toTailoringInformation(updatedPhase));
        log.info("FINISHED | Phase name changed from {} to {}", tailoring, name);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateImportedRequirements(@NonNull String project, @NonNull String tailoring, byte[] data) {
        log.info("STARTED | trying update requirement of {}:{} with provided file", project, tailoring);

        if (isNull(data) || data.length == 0) {
            log.info("FINISHED | update requirments with because of empty file");
            return;
        }

        Map<String, Collection<ImportRequirement>> importRequirements = tailoringAnforderungFileReader.apply(data);
        importRequirements.entrySet().forEach(entry -> {
            String chapter = entry.getKey();
            entry.getValue().forEach(requirement -> {
                if (YES.equalsIgnoreCase(requirement.getApplicable()) || NO.equalsIgnoreCase(requirement.getApplicable())) {
                    boolean selected = YES.equalsIgnoreCase(requirement.getApplicable());
                    requirementService.handleSelected(project, tailoring, chapter, requirement.getPosition(), selected);

                    if (nonNull(requirement.getText()) && !requirement.getText().trim().isEmpty()) {
                        requirementService.handleText(project, tailoring, chapter, requirement.getPosition(), requirement.getText());
                    }
                }
            });
        });
        log.info("FINISHED | update requirments");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> deleteTailoring(@NonNull String project, @NonNull String tailoring) {
        log.info("STARTED | trying to delete tailoring {} of project {}", tailoring, project);
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("FINISHED | tailoring not existing. No deletion.");
            return empty();
        }

        Optional<Boolean> result = of(repository.deleteTailoring(project, tailoring));
        log.info("FINISHED | deleting tailoring {}.", result.get());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringInformation> addNote(String project, String tailoring, String note) {
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("FINISHED | tailoring not existing. Not adding.");
            return empty();
        }

        Collection<Note> notes = oTailoring.get().getNotes();
        Optional<Tailoring> updatedTailoring = repository.addNote(project, tailoring, Note.builder()
            .number(nonNull(notes) ? notes.size() + 1 : 1)
            .text(note)
            .creationTimestamp(ZonedDateTime.now())
            .build());

        Optional<TailoringInformation> result = updatedTailoring.map(t -> mapper.toTailoringInformation(t));
        log.info("FINISHED | addNote tailoring {}.", result.get());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Note> getNote(String project, String tailoring, Integer note) {
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("FINISHED | tailoring not existing. Not adding.");
            return empty();
        }

        Optional<Note> result = oTailoring.get().getNotes().stream()
            .filter(n -> note.equals(n.getNumber()))
            .findFirst();

        log.info("FINISHED | getNote tailoring {}.", result.isPresent() ? result.get() : "does not exists");
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Collection<Note>> getNotes(String project, String tailoring) {
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("FINISHED | tailoring not existing. Not adding.");
            return empty();
        }

        Optional<Collection<Note>> result = ofNullable(oTailoring.get().getNotes());

        log.info("FINISHED | getNote tailoring {}.", result.isPresent() ? result.get() : "does not exists");
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Optional<File> createDocuments(@NonNull String project, @NonNull String tailoring) {
        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime erstellungsZeitpunkt = LocalDateTime.now();

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }

        Collection<File> documents = documentService.createAll(oTailoring.get(), erstellungsZeitpunkt);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(os);
        documents.forEach(dokument -> addToZip(dokument, zip));
        zip.close();
        return of(File.builder()
            .name(project + "-" + tailoring + ".zip")
            .data(os.toByteArray())
            .build());
    }

    /**
     * Add file to zip.
     *
     * @param file file to add
     * @param zip  Zip, to add file to
     */
    @SneakyThrows
    void addToZip(File file, ZipOutputStream zip) {
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zip.putNextEntry(zipEntry);
        zip.write(file.getData(), 0, file.getData().length);
        zip.closeEntry();
    }
}
