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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.TailoringexpertMapperConfig;
import eu.tailoringexpert.domain.*;
import eu.tailoringexpert.domain.BaseCatalogChapterEntity.BaseCatalogChapterEntityBuilder;
import eu.tailoringexpert.domain.ApplicableDocumentEntity;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.ApplicableDocumentRepository;
import eu.tailoringexpert.repository.LogoRepository;
import lombok.Setter;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.util.Objects.nonNull;

/**
 * Mapper used by {@link JPACatalogServiceRepository} to convert domain and entity objects.
 *
 * @author Michael Bädorf
 */
@Mapper(config = TailoringexpertMapperConfig.class)
public abstract class JPACatalogServiceRepositoryMapper {

    @Setter
    private LogoRepository logoRepository;

    @Setter
    private DRDRepository drdRepository;

    @Setter
    private ApplicableDocumentRepository applicableDocumentRepository;

    @Mapping(target = "validFrom", expression = "java( java.time.ZonedDateTime.now())")
    public abstract BaseCatalogEntity createCatalog(Catalog<BaseRequirement> domain);

    public abstract Catalog<BaseRequirement> createCatalog(BaseCatalogEntity entity);

    LogoEntity resolve(Logo domain) {
        return nonNull(domain) ? logoRepository.findByName(domain.getName()) : null;
    }

    @DoNotSelectForMapping
    public abstract DRDEntity createCatalog(DRD domain);

    @DoNotSelectForMapping
    public abstract ApplicableDocumentEntity createCatalog(Document domain);

    DRDEntity resolve(DRD domain) {
        return nonNull(domain) ? drdRepository.findByNumber(domain.getNumber()) : null;
    }

    ApplicableDocumentEntity resolve(Document domain)  {
        return nonNull(domain) ? applicableDocumentRepository.findByTitleAndIssueAndRevision(domain.getTitle(), domain.getIssue(), domain.getRevision()) : null;
    }

    public abstract Catalog<BaseRequirement> getCatalog(BaseCatalogEntity entity);

    public abstract CatalogVersion limitCatalogValidity(BaseCatalogVersionProjection entity);

    public abstract CatalogVersion getCatalogVersions(BaseCatalogVersionProjection entity);

    @AfterMapping
    public void addNumber(@MappingTarget BaseCatalogChapterEntityBuilder builder) {
        BaseCatalogChapterEntity entity = builder.build();
        if (nonNull(entity.getRequirements())) {
            entity.getRequirements()
                .forEach(requirement -> requirement.setNumber(entity.getNumber() + "." + requirement.getPosition()));
        }
        builder.requirements(entity.getRequirements());
    }

    @Qualifier
    @Target(METHOD)
    @Retention(CLASS)
    public @interface DoNotSelectForMapping {
    }
}
