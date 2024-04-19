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

import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.DocumentSignatureEntity;
import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringCatalogProjection;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringState;
import eu.tailoringexpert.repository.DokumentSigneeRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import eu.tailoringexpert.repository.TailoringRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link TailoringServiceRepository}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@Transactional
@RequiredArgsConstructor
public class JPATailoringServiceRepository implements TailoringServiceRepository {

    @NonNull
    private JPATailoringServiceRepositoryMapper mapper;

    @NonNull
    private ProjectRepository projectRepository;

    @NonNull
    private TailoringRepository tailoringRepository;

    @NonNull
    private SelectionVectorProfileRepository selectionVectorProfileRepository;

    @NonNull
    private DokumentSigneeRepository dokumentSigneeRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Project> getProject(String project) {
        log.traceEntry(() -> project);
        Optional<Project> result = ofNullable(mapper.toDomain(projectRepository.findByIdentifier(project)));
        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tailoring updateTailoring(String project, Tailoring tailoring) {
        log.traceEntry(() -> project, tailoring::getName);

        TailoringEntity toUpdate = projectRepository.findTailoring(project, tailoring.getName());
        if (nonNull(toUpdate)) {
            mapper.updateTailoring(tailoring, toUpdate);
            projectRepository.flush();
            return mapper.toDomain(toUpdate);
        }

        log.traceExit();
        return tailoring;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> getTailoring(String project, String tailoring) {
        log.traceEntry();

        if (isNull(project) || isNull(tailoring)) {
            return empty();
        }
        TailoringEntity entity = projectRepository.findTailoring(project, tailoring);
        Optional<Tailoring> result = ofNullable(mapper.toDomain(entity));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ScreeningSheet> getScreeningSheet(String project, String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        TailoringEntity eTailoring = projectRepository.findTailoring(project, tailoring);
        if (isNull(eTailoring)) {
            log.traceExit();
            return empty();
        }
        Optional<ScreeningSheet> result = ofNullable(mapper.toScreeningSheetParameters(eTailoring.getScreeningSheet()));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<byte[]> getScreeningSheetFile(String project, String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        TailoringEntity eTailoring = projectRepository.findTailoring(project, tailoring);
        if (isNull(eTailoring)) {
            log.traceExit();
            return empty();
        }
        Optional<byte[]> result = ofNullable(eTailoring.getScreeningSheet().getData());

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DocumentSignature> updateDocumentSignature(String project, String tailoring, DocumentSignature signature) {
        log.traceEntry(() -> project, () -> tailoring, () -> signature);

        TailoringEntity eTailoring = projectRepository.findTailoring(project, tailoring);
        if (isNull(eTailoring)) {
            log.traceExit();
            return empty();
        }

        Optional<DocumentSignatureEntity> toUpdate = eTailoring.getSignatures()
            .stream()
            .filter(z -> z.getFaculty().equals(signature.getFaculty()))
            .findFirst();

        if (toUpdate.isEmpty()) {
            log.traceExit();
            return empty();
        }

        mapper.updateDocumentSignature(signature, toUpdate.get());
        Optional<DocumentSignature> result = of(mapper.toDomain(toUpdate.get()));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> updateName(String project, String tailoring, String name) {
        log.traceEntry(() -> project, () -> tailoring, () -> name);

        Optional<TailoringEntity> oTailoring = findTailoring(project, tailoring);
        if (oTailoring.isPresent()) {
            oTailoring.get().setName(name);
            Optional<Tailoring> result = of(mapper.toDomain(oTailoring.get()));
            log.traceExit();
            return result;
        }

        log.traceExit();
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SelectionVectorProfile> getSelectionVectorProfile() {
        log.traceEntry();

        List<SelectionVectorProfile> result = selectionVectorProfileRepository.findAll()
            .stream()
            .map(mapper::toDomain)
            .toList();

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DocumentSignature> getDefaultSignatures() {
        log.traceEntry();

        List<DocumentSignature> result = dokumentSigneeRepository.findAll()
            .stream()
            .map(mapper::getDefaultSignatures)
            .toList();

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteTailoring(String project, String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        TailoringEntity toDelete = projectRepository.findTailoring(project, tailoring);
        if (isNull(toDelete)) {
            return log.traceExit(false);
        }

        tailoringRepository.delete(toDelete);
        return log.traceExit(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> addNote(String project, String tailoring, Note note) {
        log.traceEntry(() -> project, () -> tailoring, () -> note);

        Optional<TailoringEntity> oTailoring = findTailoring(project, tailoring);

        if (oTailoring.isEmpty()) {
            log.traceExit();
            return empty();
        }

        oTailoring.get().getNotes().add(mapper.toEntity(note));
        Optional<Tailoring> result = of(mapper.toDomain(oTailoring.get()));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsTailoring(String project, String name) {
        log.traceEntry(() -> project, () -> name);
        return log.traceExit(projectRepository.existsTailoring(project, name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> setState(String project, String tailoring, TailoringState state) {
        log.traceEntry(() -> project, () -> tailoring, () -> state);

        Optional<TailoringEntity> oTailoring = findTailoring(project, tailoring);
        if (oTailoring.isEmpty()) {
            log.traceExit();
            return empty();
        }

        oTailoring.get().setState(state);
        Optional<Tailoring> result = of(mapper.toDomain(oTailoring.get()));

        log.traceExit();
        return result;

    }

    @Override
    public Optional<Catalog<TailoringRequirement>> getCatalog(String project, String tailoring) {
        log.traceEntry();

        if (isNull(project) || isNull(tailoring)) {
            return empty();
        }
        TailoringCatalogProjection projection = projectRepository.findTailoringCatalog(project, tailoring);
        Optional<Catalog<TailoringRequirement>> result = ofNullable(mapper.getCatalog(projection));

        log.traceExit();
        return result;

    }

    /**
     * Loads tailoring of a project.
     *
     * @param project   identifier of project tailoring belongs to
     * @param tailoring name of tailoring to load
     * @return Loaded tailoring
     */
    private Optional<TailoringEntity> findTailoring(String project, String tailoring) {
        if (isNull(project) || isNull(tailoring)) {
            return empty();
        }
        return ofNullable(projectRepository.findTailoring(project, tailoring));
    }
}
