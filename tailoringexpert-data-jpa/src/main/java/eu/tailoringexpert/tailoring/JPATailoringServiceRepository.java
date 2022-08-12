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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

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
        Optional<ProjectEntity> oProjekt = findProjekt(project);
        if (oProjekt.isPresent()) {
            Optional<TailoringEntity> toUpdate = oProjekt.get().getTailoring(tailoring.getName());
            if (toUpdate.isPresent()) {
                mapper.addCatalog(tailoring, toUpdate.get());
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

    private Optional<TailoringEntity> findTailoring(String projekt, String tailoring) {
        if (isNull(projekt) || isNull(tailoring)) {
            return empty();
        }
        return ofNullable(projectRepository.findTailoring(projekt, tailoring));
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
        TailoringEntity projektPhase = projectRepository.findTailoring(project, tailoring);
        if (isNull(projektPhase)) {
            return empty();
        }

        return ofNullable(mapper.toScreeningSheetParameters(projektPhase.getScreeningSheet()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<byte[]> getScreeningSheetFile(String project, String tailoring) {
        TailoringEntity projektPhase = projectRepository.findTailoring(project, tailoring);
        if (isNull(projektPhase)) {
            return empty();
        }

        return ofNullable(projektPhase.getScreeningSheet().getData());
    }


    private Optional<ProjectEntity> findProjekt(String projekt) {
        return ofNullable(projectRepository.findByIdentifier(projekt));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DocumentSignature> updateDocumentSignature(String project, String tailoring, DocumentSignature signature) {
        TailoringEntity projektPhase = projectRepository.findTailoring(project, tailoring);
        if (isNull(projektPhase)) {
            return empty();
        }

        Optional<DocumentSignatureEntity> toUpdate = projektPhase.getSignatures()
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
        Optional<TailoringEntity> projektPhase = findTailoring(project, tailoring);
        if (projektPhase.isPresent()) {
            projektPhase.get().setName(name);
            return of(mapper.toDomain(projektPhase.get()));
        }
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<File> getFileList(String project, String tailoring) {
        Optional<TailoringEntity> entity = findTailoring(project, tailoring);
        if (entity.isEmpty()) {
            return Collections.emptyList();
        }

        return entity
            .map(TailoringEntity::getFiles)
            .stream()
            .flatMap(Collection::stream)
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<File> getFile(String project, String tailoring, String name) {
        TailoringEntity projektPhase = projectRepository.findTailoring(project, tailoring);
        if (isNull(projektPhase)) {
            return empty();
        }
        Optional<FileEntity> dokument = projektPhase.getFiles()
            .stream()
            .filter(entity -> entity.getName().equalsIgnoreCase(name))
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
     *
     * @return
     */
    @Override
    public boolean deleteFile(String project, String tailoring, String name) {
        TailoringEntity projektPhase = projectRepository.findTailoring(project, tailoring);
        if (isNull(projektPhase)) {
            return false;
        }

        Optional<FileEntity> toDelete = projektPhase.getFiles()
            .stream()
            .filter(entity -> entity.getName().equalsIgnoreCase(name))
            .findFirst();

        if (toDelete.isEmpty()) {
            return false;
        }

        return projektPhase.getFiles().remove(toDelete.get());
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

    @Override
    public boolean deleteTailoring(String project, String tailoring) {
        TailoringEntity toDelete = projectRepository.findTailoring(project, tailoring);
        if (nonNull(toDelete)) {
            tailoringRepository.delete(toDelete);
            return true;
        }
        return false;
    }

}
