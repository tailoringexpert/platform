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
package eu.tailoringexpert.projekt;

import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.KatalogEntity;
import eu.tailoringexpert.domain.Projekt;
import eu.tailoringexpert.domain.ProjektEntity;
import eu.tailoringexpert.domain.ProjektInformation;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.repository.KatalogRepository;
import eu.tailoringexpert.repository.ProjektRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Transactional
public class JPAProjektServiceRepository implements ProjektServiceRepository {

    public static final String CACHE_KATALOG = "ProjektServiceRepository#Katalog";

    @NonNull
    private JPAProjektServiceRepositoryMapper mapper;

    @NonNull
    private ProjektRepository projektRepository;

    @NonNull
    private KatalogRepository katalogRepository;


    /**
     * {@inheritDoc}
     */
    @Cacheable(CACHE_KATALOG)
    @Override
    public Katalog<KatalogAnforderung> getKatalog(String version) {
        return mapper.toDomain(getKatalogDefinition(version));
    }

    private KatalogEntity getKatalogDefinition(String version) {
        return katalogRepository.findByVersion(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Projekt createProjekt(String katalogVersion, Projekt projekt) {
        ProjektEntity entity = projektRepository.save(mapper.createProjekt(projekt));

        KatalogEntity katalogDefinition = katalogRepository.findByVersion(katalogVersion);
        entity.getTailorings().get(0).setBasisKatalog(katalogDefinition);

        return mapper.toDomain(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Projekt createProjekt(Projekt projekt) {
        ProjektEntity result = mapper.createProjekt(projekt);
        result = projektRepository.save(result);
        return mapper.toDomain(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProjekt(String projekt) {
        Long deletedProjekte = projektRepository.deleteByKuerzel(projekt);
        return deletedProjekte.intValue() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Projekt> getProjekt(String projekt) {
        return ofNullable(mapper.toDomain(projektRepository.findByKuerzel(projekt)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> addTailoring(String projektKuerzel, Tailoring tailoring) {
        ProjektEntity projekt = projektRepository.findByKuerzel(projektKuerzel);
        TailoringEntity eProjektPhase = mapper.toEntity(tailoring);

        KatalogEntity katalogDefinition = katalogRepository.findByVersion(tailoring.getKatalog().getVersion());
        eProjektPhase.setBasisKatalog(katalogDefinition);

        projekt.setTailorings(isNull(projekt.getTailorings()) ? new ArrayList<>() : new ArrayList<>(projekt.getTailorings()));
        projekt.getTailorings().add(eProjektPhase);

        projektRepository.flush();
        return ofNullable(mapper.toDomain(eProjektPhase));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ProjektInformation> getProjektInformationen() {
        return projektRepository.findAll()
            .stream()
            .map(mapper::geTailoringInformationen)
            .collect(toList());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProjektInformation> getProjektInformation(String kuerzel) {
        return ofNullable(mapper.geTailoringInformationen(projektRepository.findByKuerzel(kuerzel)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<byte[]> getScreeningSheetDatei(String projekt) {
        ProjektEntity entity = projektRepository.findByKuerzel(projekt);
        if (isNull(entity)) {
            return empty();
        }

        return Optional.of(entity.getScreeningSheet().getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ScreeningSheet> getScreeningSheet(String projekt) {
        ProjektEntity entity = projektRepository.findByKuerzel(projekt);
        if (isNull(entity)) {
            return empty();
        }

        return ofNullable(mapper.getScreeningSheet(entity.getScreeningSheet()));
    }


}
