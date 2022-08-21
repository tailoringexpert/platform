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
        ProjectEntity byKuerzel = projectRepository.findByIdentifier(project);
        if (isNull(byKuerzel)) {
            return empty();
        }

        Optional<TailoringEntity> projektPhase = byKuerzel.getTailoring(tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        Optional<TailoringCatalogChapterEntity> gruppe = projektPhase.get()
            .getCatalog()
            .getToc()
            .getChapter(chapter);
        if (gruppe.isEmpty()) {
            return empty();
        }

        return ofNullable(mapper.toDomain(gruppe.get()
            .getRequirements()
            .stream()
            .filter(anforderung -> position.equals(anforderung.getPosition()))
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

        Optional<TailoringCatalogChapterEntity> gruppe = findKapitel(project, tailoring, chapter);
        if (gruppe.isEmpty()) {
            return empty();
        }

        Optional<TailoringRequirementEntity> eAnforderung = gruppe.get()
            .getRequirements()
            .stream()
            .filter(a -> requirement.getPosition().equals(a.getPosition()))
            .findFirst();

        if (eAnforderung.isEmpty()) {
            return empty();
        }

        mapper.updateRequirement(requirement, eAnforderung.get());
        return of(mapper.toDomain(eAnforderung.get()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> getChapter(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull String chapter) {

        Optional<TailoringCatalogChapterEntity> anforderungGruppe = findKapitel(project, tailoring, chapter);
        return ofNullable(mapper.toDomain(anforderungGruppe.orElse(null)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> updateSelected(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull Chapter<TailoringRequirement> gruppe) {

        Optional<TailoringCatalogChapterEntity> root = findKapitel(project, tailoring, gruppe.getNumber());
        if (root.isEmpty()) {
            return empty();
        }

        root.get().allChapters()
            .forEachOrdered(entityGruppe -> {
                Chapter<TailoringRequirement> domainGruppe = gruppe.getChapter(entityGruppe.getNumber());
                entityGruppe.getRequirements()
                    .stream()
                    .sorted(comparing(TailoringRequirementEntity::getPosition))
                    .forEachOrdered(anforderung -> mapper.updateRequirement(
                        domainGruppe.getRequirement(anforderung.getPosition()).get(),
                        anforderung)
                    );
            });
        return of(mapper.toDomain(root.get()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> updateChapter(
        @NonNull String project,
        @NonNull String tailoring,
        @NonNull Chapter<TailoringRequirement> gruppe) {

        Optional<TailoringCatalogChapterEntity> kapitel = findKapitel(project, tailoring, gruppe.getNumber());
        if (kapitel.isEmpty()) {
            return empty();
        }
        mapper.updateChapter(gruppe, kapitel.get());
        return of(mapper.toDomain(kapitel.get()));
    }

    private Optional<TailoringCatalogChapterEntity> findKapitel(
        String projekt,
        String phase,
        String kapitel) {

        ProjectEntity byKuerzel = projectRepository.findByIdentifier(projekt);
        if (isNull(byKuerzel)) {
            return empty();
        }

        Optional<TailoringEntity> projektPhase = byKuerzel.getTailoring(phase);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        return projektPhase.get()
            .getCatalog()
            .getToc()
            .getChapter(kapitel);
    }
}
