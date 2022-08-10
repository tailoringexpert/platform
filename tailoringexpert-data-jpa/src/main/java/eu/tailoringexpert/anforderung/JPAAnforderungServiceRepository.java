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
package eu.tailoringexpert.anforderung;

import eu.tailoringexpert.domain.ProjektEntity;
import eu.tailoringexpert.domain.TailoringAnforderungEntity;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringKatalogKapitelEntity;
import eu.tailoringexpert.domain.Kapitel;
import eu.tailoringexpert.domain.TailoringAnforderung;
import eu.tailoringexpert.repository.ProjektRepository;
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
public class JPAAnforderungServiceRepository implements AnforderungServiceRepository {

    @NonNull
    private JPAAnforderungServiceRepositoryMapper mapper;

    @NonNull
    private ProjektRepository projektRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringAnforderung> getAnforderung(
        @NonNull String projekt,
        @NonNull String tailoring,
        @NonNull String kapitel,
        @NonNull String position) {
        ProjektEntity byKuerzel = projektRepository.findByKuerzel(projekt);
        if (isNull(byKuerzel)) {
            return empty();
        }

        Optional<TailoringEntity> projektPhase = byKuerzel.getTailoring(tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        Optional<TailoringKatalogKapitelEntity> gruppe = projektPhase.get()
            .getKatalog()
            .getToc()
            .getKapitel(kapitel);
        if (gruppe.isEmpty()) {
            return empty();
        }

        return ofNullable(mapper.toDomain(gruppe.get()
            .getAnforderungen()
            .stream()
            .filter(anforderung -> position.equals(anforderung.getPosition()))
            .findFirst()
            .orElse(null)));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringAnforderung> updateAnforderung(
        @NonNull String projekt,
        @NonNull String tailoring,
        @NonNull String kapitel,
        @NonNull TailoringAnforderung anforderung) {

        Optional<TailoringKatalogKapitelEntity> gruppe = findKapitel(projekt, tailoring, kapitel);
        if (gruppe.isEmpty()) {
            return empty();
        }

        Optional<TailoringAnforderungEntity> eAnforderung = gruppe.get()
            .getAnforderungen()
            .stream()
            .filter(a -> anforderung.getPosition().equals(a.getPosition()))
            .findFirst();

        if (eAnforderung.isEmpty()) {
            return empty();
        }

        mapper.updateAnforderung(anforderung, eAnforderung.get());
        return
            of(mapper.toDomain(eAnforderung.get()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Kapitel<TailoringAnforderung>> getKapitel(
        @NonNull String projekt,
        @NonNull String tailoring,
        @NonNull String kapitel) {

        Optional<TailoringKatalogKapitelEntity> anforderungGruppe = findKapitel(projekt, tailoring, kapitel);
        return ofNullable(mapper.toDomain(anforderungGruppe.orElse(null)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Kapitel<TailoringAnforderung>> updateAusgewaehlt(
        @NonNull String projekt,
        @NonNull String tailoring,
        @NonNull Kapitel<TailoringAnforderung> gruppe) {

        Optional<TailoringKatalogKapitelEntity> root = findKapitel(projekt, tailoring, gruppe.getNummer());
        if (root.isEmpty()) {
            return empty();
        }

        root.get().alleKapitel()
            .forEachOrdered(entityGruppe -> {
                Kapitel<TailoringAnforderung> domainGruppe = gruppe.getKapitel(entityGruppe.getNummer());
                entityGruppe.getAnforderungen()
                    .stream()
                    .sorted(comparing(TailoringAnforderungEntity::getPosition))
                    .forEachOrdered(anforderung -> mapper.updateAnforderung(
                        domainGruppe.getAnforderung(anforderung.getPosition()).get(),
                        anforderung)
                    );
            });
        return of(mapper.toDomain(root.get()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Kapitel<TailoringAnforderung>> updateKapitel(
        @NonNull String projekt,
        @NonNull String tailoring,
        @NonNull Kapitel<TailoringAnforderung> gruppe) {

        Optional<TailoringKatalogKapitelEntity> kapitel = findKapitel(projekt, tailoring, gruppe.getNummer());
        if (kapitel.isEmpty()) {
            return empty();
        }
        mapper.updateKapitel(gruppe, kapitel.get());
        return of(mapper.toDomain(kapitel.get()));
    }

    private Optional<TailoringKatalogKapitelEntity> findKapitel(
        String projekt,
        String phase,
        String kapitel) {

        ProjektEntity byKuerzel = projektRepository.findByKuerzel(projekt);
        if (isNull(byKuerzel)) {
            return empty();
        }

        Optional<TailoringEntity> projektPhase = byKuerzel.getTailoring(phase);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        return projektPhase.get()
            .getKatalog()
            .getToc()
            .getKapitel(kapitel);
    }
}
