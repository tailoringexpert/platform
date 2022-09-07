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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.TailoringRequirementEntity;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringCatalogChapterEntity;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.repository.ProjectRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.transaction.Transactional;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link RequirementServiceRepository}.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Transactional
public class JPARequirementServiceRepository implements RequirementServiceRepository {

    @NonNull
    private JPARequirementServiceRepositoryMapper mapper;

    @NonNull
    private ProjectRepository projectRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringRequirement> getRequirement(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull String chapter,
        @NonNull String position) {
        ProjectEntity eProject = projectRepository.findByIdentifier(project);
        if (isNull(eProject)) {
            return empty();
        }

        Optional<TailoringEntity> oTailoring = eProject.getTailoring(tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }

        Optional<TailoringCatalogChapterEntity> oChapter = oTailoring.get()
            .getCatalog()
            .getToc()
            .getChapter(chapter);
        if (oChapter.isEmpty()) {
            return empty();
        }

        return ofNullable(mapper.toDomain(oChapter.get()
            .getRequirements()
            .stream()
            .filter(requirement -> position.equals(requirement.getPosition()))
            .findFirst()
            .orElse(null)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringRequirement> updateRequirement(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull String chapter,
        @NonNull TailoringRequirement requirement) {

        Optional<TailoringCatalogChapterEntity> oChapter = findChapter(project, tailoring, chapter);
        if (oChapter.isEmpty()) {
            return empty();
        }

        Optional<TailoringRequirementEntity> oRequirement = oChapter.get()
            .getRequirements()
            .stream()
            .filter(a -> requirement.getPosition().equals(a.getPosition()))
            .findFirst();

        if (oRequirement.isEmpty()) {
            return empty();
        }

        mapper.updateRequirement(requirement, oRequirement.get());
        return of(mapper.toDomain(oRequirement.get()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> getChapter(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull String chapter) {

        Optional<TailoringCatalogChapterEntity> oChapter = findChapter(project, tailoring, chapter);
        return ofNullable(mapper.toDomain(oChapter.orElse(null)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> updateSelected(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull Chapter<TailoringRequirement> chapter) {

        Optional<TailoringCatalogChapterEntity> oChapter = findChapter(project, tailoring, chapter.getNumber());
        if (oChapter.isEmpty()) {
            return empty();
        }

        oChapter.get().allChapters()
            .forEachOrdered(subChapter -> {
                Chapter<TailoringRequirement> domainChapter = chapter.getChapter(subChapter.getNumber());
                subChapter.getRequirements()
                    .stream()
                    .sorted(comparing(TailoringRequirementEntity::getPosition))
                    .forEachOrdered(requirement -> mapper.updateRequirement(
                        domainChapter.getRequirement(requirement.getPosition()).get(),
                        requirement)
                    );
            });
        return of(mapper.toDomain(oChapter.get()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> updateChapter(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull Chapter<TailoringRequirement> chapter) {

        Optional<TailoringCatalogChapterEntity> oChapter = findChapter(project, tailoring, chapter.getNumber());
        if (oChapter.isEmpty()) {
            return empty();
        }
        mapper.updateChapter(chapter, oChapter.get());
        return of(mapper.toDomain(oChapter.get()));
    }

    private Optional<TailoringCatalogChapterEntity> findChapter(
        String project,
        String tailoring,
        String chapter) {

        ProjectEntity eProject = projectRepository.findByIdentifier(project);
        if (isNull(eProject)) {
            return empty();
        }

        Optional<TailoringEntity> oTailoring = eProject.getTailoring(tailoring);
        if (oTailoring.isEmpty()) {
            return empty();
        }

        return oTailoring.get()
            .getCatalog()
            .getToc()
            .getChapter(chapter);
    }
}
