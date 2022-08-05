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
package de.baedorf.tailoringexpert.anforderung;

import de.baedorf.tailoringexpert.domain.DRDEntity;
import de.baedorf.tailoringexpert.domain.LogoEntity;
import de.baedorf.tailoringexpert.domain.TailoringAnforderungEntity;
import de.baedorf.tailoringexpert.domain.TailoringKatalogKapitelEntity;
import de.baedorf.tailoringexpert.domain.DRD;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Logo;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.repository.DRDRepository;
import de.baedorf.tailoringexpert.repository.LogoRepository;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import static java.util.Objects.nonNull;

@Mapper(componentModel = "jsr330")
public abstract class JPAAnforderungServiceRepositoryMapper {

    @Setter
    private LogoRepository logoRepository;

    @Setter
    private DRDRepository drdRepository;


    /**
     * Erstelluing eines neuen Domänen-Objektes mit den Daten der Entität.
     *
     * @param entity Quelle
     * @return Neu erestelltes Domänen-Objekt
     */
    abstract TailoringAnforderung toDomain(TailoringAnforderungEntity entity);

    /**
     * Aktualisierung der Entität  mit den Daten des Domänen-Objektes.
     *
     * @param domain Quelle
     * @param entity Ziele
     */
    abstract void updateAnforderung(TailoringAnforderung domain, @MappingTarget TailoringAnforderungEntity entity);

    /**
     * Erstelluing eines neuen Domänen-Objektes mit den Daten der Entität.
     *
     * @param entity Quelle
     * @return Neu erestelltes Domänen-Ibjekt
     */
    abstract Kapitel<TailoringAnforderung> toDomain(TailoringKatalogKapitelEntity entity);

    /**
     * Aktualisierung des persistenten Datenobjektes mit den Daten des domain Objektes.
     *
     * @param domain Quelle
     * @param entity Ziele
     */
    abstract void updateKapitel(Kapitel<TailoringAnforderung> domain, @MappingTarget TailoringKatalogKapitelEntity entity);

    /**
     * Ermittelt die Entitöt des im Domänen-Objekt verwendeten Logos.
     *
     * @param domain Quelle
     * @return Entität des Logs aus der Datenbank
     */
    LogoEntity resolve(Logo domain) {
        return nonNull(domain) ? logoRepository.findByName(domain.getName()) : null;
    }

    DRDEntity resolve(DRD domain) {
        return nonNull(domain) ? drdRepository.findByNummer(domain.getNummer()) : null;
    }
}
