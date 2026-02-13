/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class BaseRequirementsProvider implements Function<String, Map<String, BaseRequirement>> {

    @NonNull
    private BaseRequirementsProviderRepository serviceRepository;

    @Override
    public Map<String, BaseRequirement> apply(String version) {
        Map<String, BaseRequirement> result = new HashMap<>();

        Optional<Catalog<BaseRequirement>> catalog = serviceRepository.getBaseCatalog(version);
        catalog.orElse(
                Catalog.<BaseRequirement>builder()
                    .toc(Chapter.<BaseRequirement>builder().build())
                    .build()
            )
            .getToc().allChapters()
            .forEach(chapter -> accept(chapter, result));

        return result;
    }

    /**
     * Add chapter and all requirement to rows object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter             chapter evaluate
     * @param requirementsMapping collection to add elements to
     */
    void accept(Chapter<BaseRequirement> chapter, Map<String, BaseRequirement> requirementsMapping) {
        ofNullable(chapter.getRequirements())
            .orElse(List.of())
            .forEach(requirement -> requirementsMapping.put(chapter.getNumber() + "." + requirement.getPosition(), requirement));
    }

}
