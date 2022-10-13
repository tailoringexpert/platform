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

import eu.tailoringexpert.domain.FileEntity;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.DocumentSignatureEntity;
import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.repository.DokumentSigneeRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import eu.tailoringexpert.repository.TailoringRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return ofNullable(mapper.toDomain(projectRepository.findByIdentifier(project)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tailoring updateTailoring(String project, Tailoring tailoring) {
        Optional<ProjectEntity> oProjekt = findProject(project);
        if (oProjekt.isPresent()) {
            Optional<TailoringEntity> toUpdate = oProjekt.get().getTailoring(tailoring.getName());
            if (toUpdate.isPresent()) {
                mapper.updateTailoring(tailoring, toUpdate.get());
                projectRepository.flush();
                return mapper.toDomain(toUpdate.get());
            }
        }
        return tailoring;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> updateFile(String project, String tailoring, File file) {
        Optional<TailoringEntity> oTailoring = findTailoring(project, tailoring);

        if (oTailoring.isEmpty()) {
            return empty();
        }

        FileEntity toUpdate = oTailoring.get().getFiles()
            .stream()
            .filter(entity -> entity.getName().equalsIgnoreCase(file.getName()))
            .findFirst()
            .orElseGet(() -> {
                FileEntity entity = new FileEntity();
                oTailoring.get().getFiles().add(entity);
                return entity;
            });
        mapper.update(file, toUpdate);

        return ofNullable(mapper.toDomain(oTailoring.get()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> getTailoring(String project, String tailoring) {
        if (isNull(project) || isNull(tailoring)) {
            return empty();
        }
        TailoringEntity entity = projectRepository.findTailoring(project, tailoring);
        return ofNullable(mapper.toDomain(entity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ScreeningSheet> getScreeningSheet(String project, String tailoring) {
        TailoringEntity eTailoring = projectRepository.findTailoring(project, tailoring);
        if (isNull(eTailoring)) {
            return empty();
        }

        return ofNullable(mapper.toScreeningSheetParameters(eTailoring.getScreeningSheet()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<byte[]> getScreeningSheetFile(String project, String tailoring) {
        TailoringEntity eTailoring = projectRepository.findTailoring(project, tailoring);
        if (isNull(eTailoring)) {
            return empty();
        }

        return ofNullable(eTailoring.getScreeningSheet().getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DocumentSignature> updateDocumentSignature(String project, String tailoring, DocumentSignature signature) {
        TailoringEntity eTailoring = projectRepository.findTailoring(project, tailoring);
        if (isNull(eTailoring)) {
            return empty();
        }

        Optional<DocumentSignatureEntity> toUpdate = eTailoring.getSignatures()
            .stream()
            .filter(z -> z.getFaculty().equals(signature.getFaculty()))
            .findFirst();

        if (toUpdate.isEmpty()) {
            return empty();
        }

        mapper.updateDocumentSignature(signature, toUpdate.get());
        return of(mapper.toDomain(toUpdate.get()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> updateName(String project, String tailoring, String name) {
        Optional<TailoringEntity> oTailoring = findTailoring(project, tailoring);
        if (oTailoring.isPresent()) {
            oTailoring.get().setName(name);
            return of(mapper.toDomain(oTailoring.get()));
        }
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<File>> getFileList(String project, String tailoring) {
        Optional<TailoringEntity> entity = findTailoring(project, tailoring);
        if (entity.isEmpty()) {
            return empty();
        }
        return of(entity
            .map(TailoringEntity::getFiles)
            .stream()
            .flatMap(Collection::stream)
            .map(mapper::toDomain)
            .collect(Collectors.toList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> getFile(String project, String tailoring, String filename) {
        TailoringEntity eTailoring = projectRepository.findTailoring(project, tailoring);
        if (isNull(eTailoring)) {
            return empty();
        }
        Optional<FileEntity> dokument = eTailoring.getFiles()
            .stream()
            .filter(entity -> entity.getName().equalsIgnoreCase(filename))
            .findFirst();

        if (dokument.isPresent()) {
            FileEntity entity = dokument.get();
            return of(File.builder()
                .name(entity.getName())
                .data(entity.getData())
                .build()
            );
        }

        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteFile(String project, String tailoring, String filename) {
        TailoringEntity eTailoring = projectRepository.findTailoring(project, tailoring);
        if (isNull(eTailoring)) {
            return false;
        }

        Optional<FileEntity> toDelete = eTailoring.getFiles()
            .stream()
            .filter(entity -> entity.getName().equalsIgnoreCase(filename))
            .findFirst();

        if (toDelete.isEmpty()) {
            return false;
        }

        return eTailoring.getFiles().remove(toDelete.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SelectionVectorProfile> getSelectionVectorProfile() {
        return selectionVectorProfileRepository.findAll()
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DocumentSignature> getDefaultSignatures() {
        return dokumentSigneeRepository.findAll()
            .stream()
            .map(mapper::getDefaultSignatures)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteTailoring(String project, String tailoring) {
        TailoringEntity toDelete = projectRepository.findTailoring(project, tailoring);
        if (nonNull(toDelete)) {
            tailoringRepository.delete(toDelete);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> addNote(String project, String tailoring, Note note) {
        Optional<TailoringEntity> oTailoring = findTailoring(project, tailoring);
        if (oTailoring.isPresent()) {
            oTailoring.get().getNotes().add(mapper.toEntity(note));
            return of(mapper.toDomain(oTailoring.get()));
        }
        return empty();
    }

    /**
     * Loads project with provided project identifoer.
     *
     * @param project identifier of project to load
     * @return Loaded project
     */
    private Optional<ProjectEntity> findProject(String project) {
        return ofNullable(projectRepository.findByIdentifier(project));
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
