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
    private TailoringDeletablePredicate deletablePredicate;
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
                                     String note,
                                     Catalog<BaseRequirement> catalog) {
        log.traceEntry(() -> name, () -> identifier, screeningSheet::getParameters, () -> applicableSelectionVector, catalog::getVersion);

        Catalog<TailoringRequirement> tailoringCatalog = mapper.toTailoringCatalog(
            catalog, screeningSheet, applicableSelectionVector
        );

        TailoringBuilder tailoringBuilder = Tailoring.builder()
            .name(name)
            .identifier(identifier)
            .screeningSheet(screeningSheet)
            .selectionVector(applicableSelectionVector)
            .catalog(tailoringCatalog)
            .signatures(repository.getDefaultSignatures())
            .state(TailoringState.CREATED)
            .phases(screeningSheet.getPhases());

        Tailoring result = tailoringBuilder
            .notes(nonNull(note) ? List.of(Note.builder()
                .number(1)
                .text(note)
                .creationTimestamp(ZonedDateTime.now())
                .build()) : null)
            .build();
        log.traceExit();
        return result;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Optional<Tailoring> addFile(String project, String tailoring, String filename, byte[] data) {
        log.traceEntry(() -> project, () -> tailoring, () -> filename);

        if (!repository.existsTailoring(project, tailoring)) {
            log.error("Tailoring does not exists");
            log.traceExit();
            return empty();
        }

        BigInteger hash = new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(data));
        File file = File.builder()
            .name(filename)
            .data(data)
            .hash(hash.toString(16))
            .build();

        log.traceExit();
        return repository.updateFile(project, tailoring, file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createRequirementDocument(String project, String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime creationTimestamp = LocalDateTime.now();

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.error("Tailoring does not exists");
            log.traceExit();
            return empty();
        }

        log.traceExit();
        return documentService.createRequirementDocument(oTailoring.get(), creationTimestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createComparisonDocument(String project, String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime creationTimestamp = LocalDateTime.now();

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.error("Tailoring does not exists");
            log.traceExit();
            return empty();
        }

        log.traceExit();
        return documentService.createComparisonDocument(oTailoring.get(), creationTimestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Catalog<TailoringRequirement>> getCatalog(@NonNull String project, @NonNull String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.error("Tailoring does not exists");
            log.traceExit();
            return empty();
        }

        Optional<Catalog<TailoringRequirement>> result = ofNullable(oTailoring.get().getCatalog());
        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<TailoringRequirement>> getRequirements(@NonNull String project, @NonNull String tailoring, @NonNull String chapter) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter);

        Optional<Chapter<TailoringRequirement>> oChapter = getChapter(project, tailoring, chapter);
        if (oChapter.isEmpty()) {
            log.info("Chapter does not exists");
            return log.traceExit(empty());
        }

        return log.traceExit(ofNullable(oChapter.get().getRequirements()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ScreeningSheet> getScreeningSheet(@NonNull String project, @NonNull String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("Tailoring does not exists");
            return log.traceExit(empty());
        }

        return log.traceExit(ofNullable(oTailoring.get().getScreeningSheet()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SelectionVector> getSelectionVector(@NonNull String project, @NonNull String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("Tailoring does not exists");
            return log.traceExit(empty());
        }

        return log.traceExit(ofNullable(oTailoring.get().getSelectionVector()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> getChapter(@NonNull String project, @NonNull String tailoring, @NonNull String chapter) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter);

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("Tailoring does not exists");
            return log.traceExit(empty());
        }

        return log.traceExit(oTailoring.get().getCatalog().getChapter(chapter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Collection<DocumentSignature>> getDocumentSignatures(@NonNull String project, @NonNull String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("Tailoring does not exists");
            return log.traceExit(empty());
        }

        return log.traceExit(ofNullable(oTailoring.get().getSignatures()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DocumentSignature> updateDocumentSignature(@NonNull String project, @NonNull String tailoring, @NonNull DocumentSignature signature) {
        log.traceEntry(() -> project, () -> tailoring, () -> signature);
        return log.traceExit(repository.updateDocumentSignature(project, tailoring, signature));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringInformation> updateName(String project, String tailoring, @NonNull String name) {
        log.traceEntry(() -> project, () -> tailoring, () -> name);

        // prüfe, ob es Phase mit neuem Namen bereits gibt
        if (tailoring.trim().equals(name.trim())) {
            log.info("Name not changed because new name is empty");
            return log.traceExit(empty());
        }

        Optional<Tailoring> tailoringWithNewName = repository.getTailoring(project, name);
        if (tailoringWithNewName.isPresent()) {
            log.info("Name not changed because it already exits");
            return log.traceExit(empty());
        }

        Optional<Tailoring> oTailoring = repository.updateName(project, tailoring, name);
        Optional<TailoringInformation> result = oTailoring.map(updatedPhase -> mapper.toTailoringInformation(updatedPhase));
        return log.traceExit(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateImportedRequirements(@NonNull String project, @NonNull String tailoring, byte[] data) {
        log.traceEntry(() -> project, () -> tailoring);

        if (isNull(data) || data.length == 0) {
            log.info("No import of requriments because of empty file");
            log.traceExit();
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

        log.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> deleteTailoring(@NonNull String project, @NonNull String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        Optional<Tailoring> toDelete = repository.getTailoring(project, tailoring);
        if (toDelete.isEmpty()) {
            log.info("Tailoring not exists. No deletion.");
            return log.traceExit(empty());
        }

        if (!deletablePredicate.test(project, tailoring)) {
            log.info("Tailoring not deleted because of state " + toDelete.get().getState());
            return log.traceExit(of(Boolean.FALSE));
        }

        boolean result = repository.deleteTailoring(project, tailoring);
        return log.traceExit(of(result));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Note> addNote(String project, String tailoring, String note) {
        log.traceEntry(() -> project, () -> tailoring, () -> note);
        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("Tailoring not exists. Note not added.");
            return log.traceExit(empty());
        }

        Collection<Note> notes = oTailoring.get().getNotes();
        Note noteToAdd = Note.builder()
            .number(nonNull(notes) ? notes.size() + 1 : 1)
            .text(note)
            .creationTimestamp(ZonedDateTime.now())
            .build();

        Optional<Tailoring> updatedTailoring = repository.addNote(project, tailoring, noteToAdd);
        if (updatedTailoring.isEmpty()) {
            return log.traceExit("Note not added", empty());
        }

        return log.traceExit(of(noteToAdd));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Note> getNote(String project, String tailoring, Integer note) {
        log.traceEntry(() -> tailoring, () -> project);

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("Tailoring not exist. Note not added.");
            return log.traceExit(empty());
        }

        Optional<Note> result = oTailoring.get().getNotes().stream()
            .filter(n -> note.equals(n.getNumber()))
            .findFirst();

        return log.traceExit(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringInformation> updateState(String project, String tailoring, TailoringState state) {
        log.traceEntry(state::name, () -> project, () -> tailoring);

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("Tailoring not existing. Not adding.");
            return log.traceExit(empty());
        }
        // no "downgrade": e.g. RELEASED -> AGREED
        if (state.isBefore(oTailoring.get().getState())) {
            log.info("Tailoring downgrade of states not supported");
            return log.traceExit(of(mapper.toTailoringInformation(oTailoring.get())));
        }

        Optional<Tailoring> updatedTailoring = repository.setState(project, tailoring, state);
        if (updatedTailoring.isEmpty()) {
            log.info("Failed setting state");
            return log.traceExit(empty());
        }

        return log.traceExit(of(mapper.toTailoringInformation(updatedTailoring.get())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Collection<Note>> getNotes(String project, String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("Tailoring does not exists.");
            return log.traceExit(empty());
        }

        Optional<Collection<Note>> result = ofNullable(oTailoring.get().getNotes());
        return log.traceExit(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Optional<File> createDocuments(@NonNull String project, @NonNull String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime erstellungsZeitpunkt = LocalDateTime.now();

        Optional<Tailoring> oTailoring = repository.getTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.info("Tailoring does not exists.");
            return log.traceExit(empty());
        }

        Collection<File> documents = documentService.createAll(oTailoring.get(), erstellungsZeitpunkt);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(os);
        documents.forEach(dokument -> addToZip(dokument, zip));
        zip.close();

        log.traceExit();
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
