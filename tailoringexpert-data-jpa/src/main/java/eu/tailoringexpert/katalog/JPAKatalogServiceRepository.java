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
package eu.tailoringexpert.katalog;

import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.Kapitel;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.KatalogEntity;
import eu.tailoringexpert.repository.KatalogRepository;
import eu.tailoringexpert.repository.DRDRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Log4j2
@RequiredArgsConstructor
public class JPAKatalogServiceRepository implements KatalogServiceRepository {

    @NonNull
    private JPAKatalogServiceRepositoryMapper mapper;

    @NonNull
    private KatalogRepository katalogRepository;

    @NonNull
    private DRDRepository drdRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(cacheNames = {KatalogRepository.CACHE_KATALOGKLISTE})
    @Transactional
    public Optional<Katalog<KatalogAnforderung>> createKatalog(Katalog<KatalogAnforderung> katalog, ZonedDateTime gueligAb) {
        if (isNull(katalog)) {
            return empty();
        }

        int anzahlBeendeteKatalogGueltigkeiten = katalogRepository.setGueltigBisFuerNichtGesetztesGueltigBis(gueligAb);
        log.info("Anzahl ungültig gesetzter Kataloge: " + anzahlBeendeteKatalogGueltigkeiten);


        Collection<DRD> drds = apply(katalog.getToc());
        drds.forEach(domain -> {
            DRDEntity entity = drdRepository.findByNummer(domain.getNummer());
            if (isNull(entity)) {
                drdRepository.save(mapper.createKatalog(domain));
            }
        });

        KatalogEntity toSave = mapper.createKatalog(katalog);
        return ofNullable(mapper.createKatalog(katalogRepository.save(toSave)));


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Katalog<KatalogAnforderung>> getKatalog(String version) {
        KatalogEntity entity = katalogRepository.findByVersion(version);
        return ofNullable(mapper.getKatalog(entity));
    }

    private Collection<DRD> apply(Kapitel<KatalogAnforderung> gruppe) {
        return gruppe.allAnforderungen()
            .map(KatalogAnforderung::getDrds)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    }

}
