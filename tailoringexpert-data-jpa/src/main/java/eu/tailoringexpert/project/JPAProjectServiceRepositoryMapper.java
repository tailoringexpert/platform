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
package eu.tailoringexpert.project;

import eu.tailoringexpert.TailoringexpertMapperConfig;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectInformation;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetEntity;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringInformation;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import lombok.Setter;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static java.util.Objects.nonNull;

/**
 * Mapper used by {@link JPAProjectServiceRepository} to convert domain and entity objects.
 *
 * @author Michael Bädorf
 */
@Mapper(config = TailoringexpertMapperConfig.class)
public abstract class JPAProjectServiceRepositoryMapper {

    @Setter
    private BaseCatalogRepository baseCatalogRepository;

    @Setter
    private LogoRepository logoRepository;

    @Setter
    private DRDRepository drdRepository;

    @Mapping(target = "screeningSheet.data", source = "entity.screeningSheet.data")
    abstract Project toDomain(ProjectEntity entity);

    abstract TailoringEntity toEntity(Tailoring domain);

    abstract Tailoring toDomain(TailoringEntity entity);

    abstract Catalog<BaseRequirement> toDomain(BaseCatalogEntity entity);

    @Mapping(target = "creationTimestamp", expression = "java( java.time.ZonedDateTime.now())")
    abstract ProjectEntity createProject(Project domain);


    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "identifier", source = "entity.identifier")
    @Mapping(target = "creationTimestamp", source = "entity.creationTimestamp")
    @Mapping(target = "tailorings", source = "entity.tailorings")
    @Mapping(target = "state", source = "entity.state")
    abstract ProjectInformation getProjectInformationen(ProjectEntity entity);

    /**
     * Erstellt ein neues Domänen-Objekt mit den Werten der Phasen.
     *
     * @param entity Quelle
     * @return Das erstellte Domänen-Objekt
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "entity.name")
    @Mapping(target = "phases", source = "entity.phases")
    @Mapping(target = "catalogVersion", source = "entity.catalog.version")
    @Mapping(target = "state", source = "entity.state")
    abstract TailoringInformation getProjectInformationen(TailoringEntity entity);

    @Mapping(target = "data", ignore = true)
    abstract ScreeningSheet getScreeningSheet(ScreeningSheetEntity entity);

    BaseCatalogEntity resolve(Catalog<BaseRequirement> domain) {
        return nonNull(domain) ? baseCatalogRepository.findByVersion(domain.getVersion()) : null;
    }

    LogoEntity resolve(Logo domain) {
        return nonNull(domain) ? logoRepository.findByName(domain.getName()) : null;
    }

    DRDEntity resolve(DRD domain) {
        return nonNull(domain) ? drdRepository.findByNumber(domain.getNumber()) : null;
    }
}
