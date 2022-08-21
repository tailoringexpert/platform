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
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.TailoringRequirementEntity;
import eu.tailoringexpert.domain.TailoringCatalogChapterEntity;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import static java.util.Objects.nonNull;

@Mapper(componentModel = "jsr330")
public abstract class JPARequirementServiceRepositoryMapper {

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
    abstract TailoringRequirement toDomain(TailoringRequirementEntity entity);

    /**
     * Aktualisierung der Entität  mit den Daten des Domänen-Objektes.
     *
     * @param domain Quelle
     * @param entity Ziele
     */
    abstract void updateRequirement(TailoringRequirement domain, @MappingTarget TailoringRequirementEntity entity);

    /**
     * Erstelluing eines neuen Domänen-Objektes mit den Daten der Entität.
     *
     * @param entity Quelle
     * @return Neu erestelltes Domänen-Ibjekt
     */
    abstract Chapter<TailoringRequirement> toDomain(TailoringCatalogChapterEntity entity);

    /**
     * Aktualisierung des persistenten Datenobjektes mit den Daten des domain Objektes.
     *
     * @param domain Quelle
     * @param entity Ziele
     */
    abstract void updateChapter(Chapter<TailoringRequirement> domain, @MappingTarget TailoringCatalogChapterEntity entity);

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
        return nonNull(domain) ? drdRepository.findByNumber(domain.getNumber()) : null;
    }
}
