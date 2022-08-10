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

import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.KatalogEntity;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.Projekt;
import eu.tailoringexpert.domain.ProjektEntity;
import eu.tailoringexpert.domain.ProjektInformation;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetEntity;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringInformation;
import eu.tailoringexpert.repository.KatalogRepository;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import lombok.Setter;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static java.util.Objects.nonNull;


@Mapper(componentModel = "jsr330")
public abstract class JPAProjektServiceRepositoryMapper {

    @Setter
    private KatalogRepository katalogRepository;

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
    @Mapping(target = "screeningSheet.data", source = "entity.screeningSheet.data")
    abstract Projekt toDomain(ProjektEntity entity);

    /**
     * Erstellung einer Entität  mit den Daten des Domänen-Objektes.
     *
     * @param domain Quelle
     */
    abstract TailoringEntity toEntity(Tailoring domain);

    /**
     * Erstelluing eines neuen Domänen-Objektes mit den Daten der Entität.
     *
     * @param entity Quelle
     * @return Neu erestelltes Domänen-Objekt
     */
    abstract Tailoring toDomain(TailoringEntity entity);

    /**
     * Erstelluing eines neuen Domänen-Objektes mit den Daten der Entität.
     *
     * @param entity Quelle
     * @return Neu erestelltes Domänen-Objekt
     */
    abstract Katalog<KatalogAnforderung> toDomain(KatalogEntity entity);

    /**
     * Erstellt eine "Kopie" des übergebenen Domänen-Projektes.<p>
     * IDs der ProjektPhasen werden genullt!
     *
     * @param domain Quelle
     * @return Erstellte Entität
     */
    @Mapping(target = "erstellungsZeitpunkt", expression = "java( java.time.ZonedDateTime.now())")
    abstract ProjektEntity createProjekt(Projekt domain);


    /**
     * Erstellt ein neues Domänen-Objekt mit den Werten für
     * <ul>
     * <li>Kürzel</li>
     * <li>Erstellungszeitpunkt</li>
     * <li>Katalogversion</li>
     * </ul>.
     *
     * @param entity Quelle
     * @return Das erstellte Domänen-Objekt
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "kuerzel", source = "entity.kuerzel")
    @Mapping(target = "erstellungsZeitpunkt", source = "entity.erstellungsZeitpunkt")
    @Mapping(target = "tailorings", source = "entity.tailorings")
    abstract ProjektInformation geTailoringInformationen(ProjektEntity entity);

    /**
     * Erstellt ein neues Domänen-Objekt mit den Werten der Phasen.
     *
     * @param entity Quelle
     * @return Das erstellte Domänen-Objekt
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "entity.name")
    @Mapping(target = "phasen", source = "entity.phasen")
    @Mapping(target = "katalogVersion", source = "entity.katalog.version")
    abstract TailoringInformation geTailoringInformationen(TailoringEntity entity);


    /**
     * Übernahme der berechneten ScreeningSheet Daten ohne Eingabedatei in ein neues Domänenobjekt.
     *
     * @param entity Quelle
     * @return neues Domäenobjekt <strong>ohne</strong> Eingabedatei
     */
    @Mapping(target = "data", ignore = true)
    abstract ScreeningSheet getScreeningSheet(ScreeningSheetEntity entity);

    /**
     * Ermittelt die Entitöt der Katalogversion des Domänenobjektes.
     *
     * @param domain Quelle
     * @return Entität des Katalogs aus der Datenbank
     */
    KatalogEntity resolve(Katalog<KatalogAnforderung> domain) {
        return nonNull(domain) ? katalogRepository.findByVersion(domain.getVersion()) : null;
    }

    /**
     * Ermittelt die Entitöt der im Domänen-Objekt verwendeten Logos.
     *
     * @param domain Quelle
     * @return Entität des Logs aus der Datenbank
     */
    LogoEntity resolve(Logo domain) {
        return nonNull(domain) ? logoRepository.findByName(domain.getName()) : null;
    }

    DRDEntity resolve(DRD domain) { return nonNull(domain) ? drdRepository.findByNummer(domain.getNummer()) : null; }
 }
