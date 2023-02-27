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
import eu.tailoringexpert.domain.TailoringRequirementEntity;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringCatalogChapterEntity;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.repository.ProjectRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

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
@Log4j2
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
        log.traceEntry(() -> project, () -> tailoring, () -> chapter, () -> position);

        Optional<TailoringCatalogChapterEntity> oChapter = findChapter(project, tailoring, chapter);
        if (oChapter.isEmpty()) {
            log.traceExit();
            return empty();
        }

        Optional<TailoringRequirement> result = ofNullable(mapper.toDomain(oChapter.get()
            .getRequirements()
            .stream()
            .filter(requirement -> position.equals(requirement.getPosition()))
            .findFirst()
            .orElse(null)));

        log.traceExit();
        return result;
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
        log.traceEntry(() -> project, () -> tailoring, () -> chapter, requirement::getPosition);

        Optional<TailoringCatalogChapterEntity> oChapter = findChapter(project, tailoring, chapter);
        if (oChapter.isEmpty()) {
            log.traceExit();
            return empty();
        }

        Optional<TailoringRequirementEntity> oRequirement = oChapter.get()
            .getRequirements()
            .stream()
            .filter(a -> requirement.getPosition().equals(a.getPosition()))
            .findFirst();

        if (oRequirement.isEmpty()) {
            log.traceExit();
            return empty();
        }

        mapper.updateRequirement(requirement, oRequirement.get());
        Optional<TailoringRequirement> result = of(mapper.toDomain(oRequirement.get()));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> getChapter(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull String chapter) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter);

        Optional<TailoringCatalogChapterEntity> oChapter = findChapter(project, tailoring, chapter);
        Optional<Chapter<TailoringRequirement>> result = ofNullable(mapper.toDomain(oChapter.orElse(null)));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> updateSelected(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull Chapter<TailoringRequirement> chapter) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter);

        Optional<TailoringCatalogChapterEntity> oChapter = findChapter(project, tailoring, chapter.getNumber());
        if (oChapter.isEmpty()) {
            log.traceExit();
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
        Optional<Chapter<TailoringRequirement>> result = of(mapper.toDomain(oChapter.get()));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> updateChapter(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull Chapter<TailoringRequirement> chapter) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter);

        Optional<TailoringCatalogChapterEntity> oChapter = findChapter(project, tailoring, chapter.getNumber());
        if (oChapter.isEmpty()) {
            log.traceExit();
            return empty();
        }
        mapper.updateChapter(chapter, oChapter.get());
        Optional<Chapter<TailoringRequirement>> result = of(mapper.toDomain(oChapter.get()));

        log.traceExit();
        return result;
    }

    /**
     * Load a requested tailoring catalog chapter.
     *
     * @param project   project tailoring belongs to
     * @param tailoring tailoring chapter belongs to
     * @param chapter   requested chapter
     * @return if exists the (fullqualified) chapter, otherwise empty
     */
    private Optional<TailoringCatalogChapterEntity> findChapter(
        String project,
        String tailoring,
        String chapter) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter);

        TailoringEntity eTailoring = projectRepository.findTailoring(project, tailoring);
        if (isNull(eTailoring)) {
            log.traceExit();
            return empty();
        }

        Optional<TailoringCatalogChapterEntity> result = eTailoring
            .getCatalog()
            .getToc()
            .getChapter(chapter);

        log.traceExit();
        return result;
    }
}
