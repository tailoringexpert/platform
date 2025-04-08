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

import eu.tailoringexpert.TailoringexpertMapperConfig;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.DocumentSigneeEntity;
import eu.tailoringexpert.domain.DocumentSignatureEntity;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.domain.NoteEntity;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheet.ScreeningSheetBuilder;
import eu.tailoringexpert.domain.ScreeningSheetEntity;
import eu.tailoringexpert.domain.ScreeningSheetParameterEntity;
import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.SelectionVectorProfileEntity;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.repository.LogoRepository;
import lombok.Setter;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.LinkedList;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

/**
 * Mapper used by {@link JPATailoringServiceRepository} to convert domain and entity objects.
 *
 * @author Michael Bädorf
 */
@Mapper(config = TailoringexpertMapperConfig.class)
public abstract class JPATailoringServiceRepositoryMapper {

    @Setter
    private LogoRepository logoRepository;

    abstract Project toDomain(ProjectEntity entity);

    abstract Tailoring toDomain(TailoringEntity entity);

    @Mapping(target = "name", ignore = true)
    @Mapping(target = "identifier", ignore = true)
    @Mapping(target = "catalog", source = "domain.catalog")
    @Mapping(target = "selectionVector", source = "domain.selectionVector")
    @Mapping(target = "state", source = "domain.state")
    abstract void updateTailoring(Tailoring domain, @MappingTarget TailoringEntity entity);

    @Mapping(target = "data", ignore = true)
    @Mapping(target = "selectionVector", source = "entity.selectionVector")
    @Mapping(target = "parameters", source = "entity.parameters")
    abstract ScreeningSheet toScreeningSheetParameters(ScreeningSheetEntity entity);

    @AfterMapping
    void toScreeningSheetParameters(ScreeningSheetEntity entity, @MappingTarget ScreeningSheetBuilder resource) {
        entity.getParameters()
            .stream()
            .filter(parameter -> ScreeningSheet.PARAMETER_PROJECT.equalsIgnoreCase(parameter.getCategory()))
            .findFirst()
            .ifPresent(parameter -> resource.project(parameter.getValue().toString()));

        resource.phases(entity.getParameters()
            .stream()
            .filter(parameter -> ScreeningSheet.PARAMETER_PHASE.equalsIgnoreCase(parameter.getCategory()))
            .map(ScreeningSheetParameterEntity::getValue)
            .map(phase -> Phase.fromString((String) phase))
            .sorted(comparing(Phase::ordinal))
            .collect(Collectors.toCollection(LinkedList::new)));
    }

    abstract void updateDocumentSignature(DocumentSignature domain, @MappingTarget DocumentSignatureEntity entity);

    abstract DocumentSignature toDomain(DocumentSignatureEntity entity);

    abstract SelectionVectorProfile toDomain(SelectionVectorProfileEntity entity);

    @Mapping(target = "applicable", constant = "false")
    abstract DocumentSignature getDefaultSignatures(DocumentSigneeEntity entity);

    abstract NoteEntity toEntity(Note domain);

    LogoEntity resolve(Logo domain) {
        return nonNull(domain) ? logoRepository.findByName(domain.getName()) : null;
    }
}
