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

import eu.tailoringexpert.domain.Datei;
import eu.tailoringexpert.domain.Dokument;
import eu.tailoringexpert.domain.DokumentEntity;
import eu.tailoringexpert.domain.DokumentZeichnung;
import eu.tailoringexpert.domain.DokumentZeichnungEntity;
import eu.tailoringexpert.domain.Projekt;
import eu.tailoringexpert.domain.ProjektEntity;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelektionsVektorProfil;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.repository.DokumentZeichnerRepository;
import eu.tailoringexpert.repository.ProjektRepository;
import eu.tailoringexpert.repository.SelektionsVektorProfilRepository;
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
    private ProjektRepository projektRepository;

    @NonNull
    private TailoringRepository tailoringRepository;

    @NonNull
    private SelektionsVektorProfilRepository selektionsVektorProfilRepository;

    @NonNull
    private DokumentZeichnerRepository dokumentZeichnerRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Projekt> getProjekt(String kuerzel) {
        return ofNullable(mapper.toDomain(projektRepository.findByKuerzel(kuerzel)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tailoring updateTailoring(String projekt, Tailoring tailoring) {
        Optional<ProjektEntity> oProjekt = findProjekt(projekt);
        if (oProjekt.isPresent()) {
            Optional<TailoringEntity> toUpdate = oProjekt.get().getTailoring(tailoring.getName());
            if (toUpdate.isPresent()) {
                mapper.addKatalog(tailoring, toUpdate.get());
                projektRepository.flush();
                return mapper.toDomain(toUpdate.get());
            }
        }
        return tailoring;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> updateAnforderungDokument(String projekt, String tailoring, Dokument dokument) {
        Optional<TailoringEntity> projektPhase = findProjektPhase(projekt, tailoring);
        if (projektPhase.isPresent()) {
            DokumentEntity toUpdate = projektPhase.get().getDokumente()
                .stream()
                .filter(entity -> entity.getName().equalsIgnoreCase(dokument.getName()))
                .findFirst()
                .orElseGet(() -> {
                    DokumentEntity entity = new DokumentEntity();
                    projektPhase.get().getDokumente().add(entity);
                    return entity;
                });
            mapper.update(dokument, toUpdate);

            return ofNullable(mapper.toDomain(projektPhase.get()));
        }
        return empty();
    }

    private Optional<TailoringEntity> findProjektPhase(String projekt, String phase) {
        if (isNull(projekt) || isNull(phase)) {
            return empty();
        }
        return ofNullable(projektRepository.findTailoring(projekt, phase));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> getTailoring(String projekt, String tailoring) {
        if (isNull(projekt) || isNull(tailoring)) {
            return empty();
        }
        TailoringEntity entity = projektRepository.findTailoring(projekt, tailoring);
        return ofNullable(mapper.toDomain(entity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ScreeningSheet> getScreeningSheet(String projekt, String tailoring) {
        TailoringEntity projektPhase = projektRepository.findTailoring(projekt, tailoring);
        if (isNull(projektPhase)) {
            return empty();
        }

        return ofNullable(mapper.toScreeningSheetParameters(projektPhase.getScreeningSheet()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<byte[]> getScreeningSheetDatei(String projekt, String tailoring) {
        TailoringEntity projektPhase = projektRepository.findTailoring(projekt, tailoring);
        if (isNull(projektPhase)) {
            return empty();
        }

        return ofNullable(projektPhase.getScreeningSheet().getData());
    }


    private Optional<ProjektEntity> findProjekt(String projekt) {
        return ofNullable(projektRepository.findByKuerzel(projekt));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DokumentZeichnung> updateDokumentZeichnung(String projekt, String tailoring, DokumentZeichnung zeichnung) {
        TailoringEntity projektPhase = projektRepository.findTailoring(projekt, tailoring);
        if (isNull(projektPhase)) {
            return empty();
        }

        Optional<DokumentZeichnungEntity> toUpdate = projektPhase.getZeichnungen()
            .stream()
            .filter(z -> z.getBereich().equals(zeichnung.getBereich()))
            .findFirst();

        if (toUpdate.isEmpty()) {
            return empty();
        }

        mapper.updateDokumentZeichnung(zeichnung, toUpdate.get());
        return of(mapper.toDomain(toUpdate.get()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> updateName(String projekt, String tailoring, String name) {
        Optional<TailoringEntity> projektPhase = findProjektPhase(projekt, tailoring);
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
    public List<Dokument> getDokumentListe(String projekt, String tailoring) {
        Optional<TailoringEntity> projektPhase = findProjektPhase(projekt, tailoring);
        if (projektPhase.isEmpty()) {
            return Collections.emptyList();
        }

        return projektPhase
            .map(TailoringEntity::getDokumente)
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
    public Optional<Datei> getDokument(String projekt, String tailoring, String name) {
        TailoringEntity projektPhase = projektRepository.findTailoring(projekt, tailoring);
        if (isNull(projektPhase)) {
            return empty();
        }
        Optional<DokumentEntity> dokument = projektPhase.getDokumente()
            .stream()
            .filter(entity -> entity.getName().equalsIgnoreCase(name))
            .findFirst();

        if (dokument.isPresent()) {
            DokumentEntity entity = dokument.get();
            int i = entity.getName().lastIndexOf(".");
            return of(Datei.builder()
                .docId(entity.getName().substring(0, i))
                .type(entity.getName().substring(i + 1))
                .bytes(entity.getDaten())
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
    public boolean deleteDokument(String projekt, String tailoring, String name) {
        TailoringEntity projektPhase = projektRepository.findTailoring(projekt, tailoring);
        if (isNull(projektPhase)) {
            return false;
        }

        Optional<DokumentEntity> toDelete = projektPhase.getDokumente()
            .stream()
            .filter(entity -> entity.getName().equalsIgnoreCase(name))
            .findFirst();

        if (toDelete.isEmpty()) {
            return false;
        }

        return projektPhase.getDokumente().remove(toDelete.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SelektionsVektorProfil> getSelektionsVektorProfile() {
        return selektionsVektorProfilRepository.findAll()
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DokumentZeichnung> getDefaultZeichnungen() {
        return dokumentZeichnerRepository.findAll()
            .stream()
            .map(mapper::getDefaultZeichnungen)
            .collect(Collectors.toList());

    }

    @Override
    public boolean deleteTailoring(String projekt, String tailoring) {
        TailoringEntity toDelete = projektRepository.findTailoring(projekt, tailoring);
        if (nonNull(toDelete)) {
            tailoringRepository.delete(toDelete);
            return true;
        }
        return false;
    }

}
