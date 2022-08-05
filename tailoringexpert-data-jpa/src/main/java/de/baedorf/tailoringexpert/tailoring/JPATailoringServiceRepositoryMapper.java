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
package de.baedorf.tailoringexpert.tailoring;

import de.baedorf.tailoringexpert.domain.Dokument;
import de.baedorf.tailoringexpert.domain.DokumentEntity;
import de.baedorf.tailoringexpert.domain.DokumentZeichnerEntity;
import de.baedorf.tailoringexpert.domain.DokumentZeichnung;
import de.baedorf.tailoringexpert.domain.DokumentZeichnungEntity;
import de.baedorf.tailoringexpert.domain.Logo;
import de.baedorf.tailoringexpert.domain.LogoEntity;
import de.baedorf.tailoringexpert.domain.Projekt;
import de.baedorf.tailoringexpert.domain.ProjektEntity;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.ScreeningSheetEntity;
import de.baedorf.tailoringexpert.domain.SelektionsVektorProfil;
import de.baedorf.tailoringexpert.domain.SelektionsVektorProfilEntity;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringEntity;
import de.baedorf.tailoringexpert.repository.LogoRepository;
import lombok.Setter;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static java.util.Objects.nonNull;


@Mapper(componentModel = "jsr330")
public abstract class JPATailoringServiceRepositoryMapper {

    @Setter
    private LogoRepository logoRepository;

    /**
     * Erstelluing eines neuen Domänen-Objektes mit den Daten der Entität.
     *
     * @param entity Quelle
     * @return Neu erestelltes Domänen-Objekt
     */

    abstract Projekt toDomain(ProjektEntity entity);

    /**
     * Erstelluing eines neuen Domänen-Objektes mit den Daten der Entität.
     *
     * @param entity Quelle
     * @return Neu erestelltes Domänen-Objekt
     */
    abstract Tailoring toDomain(TailoringEntity entity);

    /**
     * Übernahme der Werte für
     * <ul>
     * <li>Katalog</li>
     * <li>Selektionsvektor</li>
     * <li>Status</li>
     * </ul>
     * in die Entität.
     *
     * @param domain Quelle
     * @param entity Ziel
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "katalog", source = "domain.katalog")
    @Mapping(target = "selektionsVektor", source = "domain.selektionsVektor")
    @Mapping(target = "status", source = "domain.status")
    abstract void addKatalog(Tailoring domain, @MappingTarget TailoringEntity entity);

    /**
     * Erstellung einer Entität mit den Daten des Domänen-Objektes.
     *
     * @param domain Quelle
     * @param entity
     */
    abstract void update(Dokument domain, @MappingTarget DokumentEntity entity);


    /**
     * ScreeningSheet mit Selektionsvektor und Parametern, aber <strong>ohne</strong> die ScreeningSheet Datei.
     *
     * @param entity Quelle
     * @return ScreeningSheet mit Selektionsvektor und Parametern, aber <strong>ohne</strong> die ScreeningSheet Datei.
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "selektionsVektor", source = "entity.selektionsVektor")
    @Mapping(target = "parameters", source = "entity.parameters")
    abstract ScreeningSheet toScreeningSheetParameters(ScreeningSheetEntity entity);

    /**
     * Übernahme der Dokumentzeichung in die Entität.
     *
     * @param domain Quelle der zu übernehmenden Daten
     * @param entity Aktualisuerungsziel
     */
    abstract void updateDokumentZeichnung(DokumentZeichnung domain, @MappingTarget DokumentZeichnungEntity entity);

    /**
     * Konvertiert die persistente Dokumentzeichnung in das korrespondierende Domänenobjekt.
     *
     * @param entity Quelle
     * @return Neu erstelltes Domänenobjekt
     */
    abstract DokumentZeichnung toDomain(DokumentZeichnungEntity entity);

    @Mapping(target = "daten", ignore = true)
    abstract Dokument toDomain(DokumentEntity entity);

    /**
     * Konvertiert ein persistentes SelektionsVektor Profil in ein Domänenobjekt.
     *
     * @param entity Quelle
     * @return Neu erstelltes Domänenobjekt
     */
    abstract SelektionsVektorProfil toDomain(SelektionsVektorProfilEntity entity);

    /**
     * Konvertiert einen persistenten Dokumentzeichner in eine Dokumentzeichnung.
     *
     * @param entity Quelle
     * @return Neu erstelltes Domänenobjekt
     */
    @Mapping(target = "anwendbar", constant = "false")
    abstract DokumentZeichnung getDefaultZeichnungen(DokumentZeichnerEntity entity);

    /**
     * Ermittelt die Entitöt des im Domänen-Objekt verwendeten Logos.
     *
     * @param domain Quelle
     * @return Entität des Logs aus der Datenbank
     */
    LogoEntity resolve(Logo domain) {
        return nonNull(domain) ? logoRepository.findByName(domain.getName()) : null;
    }
}
