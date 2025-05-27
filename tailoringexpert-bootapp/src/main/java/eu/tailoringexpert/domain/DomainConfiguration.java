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
package eu.tailoringexpert.domain;

import eu.tailoringexpert.catalog.RequirementAlwaysSelectedPredicate;
import eu.tailoringexpert.tailoring.RequirementSelectedPredicate;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@Configuration
public class DomainConfiguration {
    @Bean
    ResourceMapper resourceMapper(@NonNull @Value("${server.servlet.context-path}") String contextPath) {
        ResourceMapperGenerated result = new ResourceMapperGenerated();
        result.setContextPath(contextPath);
        return result;
    }

    @Bean
    Comparator<Document> documentNumberComparator() {
        return new DocumentNumberComparator();
    }

    @Bean
    Predicate<TailoringRequirement> tailoringRequirementSelectedPredicate() {
        return new RequirementSelectedPredicate();
    }

    @Bean
    Function<Catalog<TailoringRequirement>, Collection<Document>> tailoringCatalogApplicableDocumentProvider(
        @NonNull Predicate<TailoringRequirement> tailoringRequirementSelectedPredicate,
        @NonNull Comparator<Document> documentNumberComparator
    ) {
        return new ApplicableDocumentProvider<>(tailoringRequirementSelectedPredicate, documentNumberComparator);
    }

    @Bean
    BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> tailoringCatalogDTDProvider(
        @NonNull BiPredicate<String, Collection<Phase>> drdApplicable,
        @NonNull Predicate<TailoringRequirement> tailoringRequirementSelectedPredicate) {
        return new DRDProvider<>(tailoringRequirementSelectedPredicate, drdApplicable);
    }

    @Bean
    Predicate<BaseRequirement> requirementAlwaysSelectedPredicate() {
        return new RequirementAlwaysSelectedPredicate<>();
    }
    @Bean
    Function<Catalog<BaseRequirement>, Collection<Document>> baseCatalogApplicableDocumentProvider(
        @NonNull Predicate<BaseRequirement> requirementAlwaysSelectedPredicate,
        @NonNull Comparator<Document> documentNumberComparator
    ) {
        return new ApplicableDocumentProvider<>(requirementAlwaysSelectedPredicate, documentNumberComparator);
    }

    @Bean
    BiFunction<Chapter<BaseRequirement>, Collection<Phase>, Map<DRD, Set<String>>> baseCatalogDRDProvider(
        @NonNull BiPredicate<String, Collection<Phase>> drdApplicable,
        @NonNull Predicate<BaseRequirement> requirementAlwaysSelectedPredicate) {
        return new DRDProvider<>(requirementAlwaysSelectedPredicate, drdApplicable);
    }
}
