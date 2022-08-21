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

import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.FileEntity;
import eu.tailoringexpert.domain.DocumentSigneeEntity;
import eu.tailoringexpert.domain.DocumentSignatureEntity;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetEntity;
import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.SelectionVectorProfileEntity;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.repository.LogoRepository;
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

    abstract Project toDomain(ProjectEntity entity);

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
     * <li>Catalog</li>
     * <li>Selektionsvektor</li>
     * <li>Status</li>
     * </ul>
     * in die Entität.
     *
     * @param domain Quelle
     * @param entity Ziel
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "catalog", source = "domain.catalog")
    @Mapping(target = "selectionVector", source = "domain.selectionVector")
    @Mapping(target = "state", source = "domain.state")
    abstract void addCatalog(Tailoring domain, @MappingTarget TailoringEntity entity);

    /**
     * Erstellung einer Entität mit den Daten des Domänen-Objektes.
     *
     * @param domain Quelle
     * @param entity
     */
    abstract void update(File domain, @MappingTarget FileEntity entity);


    /**
     * ScreeningSheet mit Selektionsvektor und Parametern, aber <strong>ohne</strong> die ScreeningSheet File.
     *
     * @param entity Quelle
     * @return ScreeningSheet mit Selektionsvektor und Parametern, aber <strong>ohne</strong> die ScreeningSheet File.
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "selectionVector", source = "entity.selectionVector")
    @Mapping(target = "parameters", source = "entity.parameters")
    abstract ScreeningSheet toScreeningSheetParameters(ScreeningSheetEntity entity);

    /**
     * Übernahme der Dokumentzeichung in die Entität.
     *
     * @param domain Quelle der zu übernehmenden Daten
     * @param entity Aktualisuerungsziel
     */
    abstract void updateDocumentSignature(DocumentSignature domain, @MappingTarget DocumentSignatureEntity entity);

    /**
     * Konvertiert die persistente Dokumentzeichnung in das korrespondierende Domänenobjekt.
     *
     * @param entity Quelle
     * @return Neu erstelltes Domänenobjekt
     */
    abstract DocumentSignature toDomain(DocumentSignatureEntity entity);

    @Mapping(target = "data", ignore = true)
    abstract File toDomain(FileEntity entity);

    /**
     * Konvertiert ein persistentes SelectionVector Profil in ein Domänenobjekt.
     *
     * @param entity Quelle
     * @return Neu erstelltes Domänenobjekt
     */
    abstract SelectionVectorProfile toDomain(SelectionVectorProfileEntity entity);

    /**
     * Konvertiert einen persistenten Dokumentzeichner in eine Dokumentzeichnung.
     *
     * @param entity Quelle
     * @return Neu erstelltes Domänenobjekt
     */
    @Mapping(target = "applicable", constant = "false")
    abstract DocumentSignature getDefaultSignatures(DocumentSigneeEntity entity);

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
