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
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Log4j2
class BaseRequirementsProviderTest {

    BaseRequirementsProviderRepository serviceRepository;
    BaseRequirementsProvider provider;


    @BeforeEach
    void setup() {
        this.serviceRepository = mock(BaseRequirementsProviderRepository.class);
        this.provider = new BaseRequirementsProvider(serviceRepository);
    }

    @Test
    void apply_NullRequirementsObject_EmptyMapReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .version("8.2.2")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(of(
                        Chapter.<BaseRequirement>builder()
                            .number("1")
                            .requirements(null)
                            .build()
                    )
                )
                .build()
            )
            .build();
        given(serviceRepository.getBaseCatalog("8.2.2")).willReturn(Optional.of(catalog));

        // act
        Map<String, BaseRequirement> actual = provider.apply("8.2.2");

        // assert
        assertThat(actual)
            .isEmpty();
    }

    @Test
    void apply_NullChapterssObject_EmptyMapReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .version("8.2.2")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(null)
                .build()
            )
            .build();
        given(serviceRepository.getBaseCatalog("8.2.2")).willReturn(Optional.of(catalog));

        // act
        Map<String, BaseRequirement> actual = provider.apply("8.2.2");

        // assert
        assertThat(actual)
            .isEmpty();
    }


    @Test
    void apply_5RequirementsInChapterTree() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .version("8.2.2")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(of(
                        Chapter.<BaseRequirement>builder()
                            .number("1")
                            .requirements(asList(
                                BaseRequirement.builder()
                                    .position("a")
                                    .build())
                            )
                            .chapters(of(
                                Chapter.<BaseRequirement>builder()
                                    .number("1.1")
                                    .requirements(asList(
                                        BaseRequirement.builder()
                                            .position("a")
                                            .build())
                                    )
                                    .build()
                            ))
                            .build(),
                        Chapter.<BaseRequirement>builder()
                            .number("2")
                            .requirements(asList(
                                BaseRequirement.builder()
                                    .position("b")
                                    .build())
                            )
                            .chapters(of(
                                Chapter.<BaseRequirement>builder()
                                    .number("2.1")
                                    .chapters(of(
                                        Chapter.<BaseRequirement>builder()
                                            .number("2.1.1")
                                            .requirements(asList(
                                                BaseRequirement.builder()
                                                    .position("a")
                                                    .build())
                                            )
                                            .build()
                                    ))
                                    .requirements(asList(
                                        BaseRequirement.builder()
                                            .position("a")
                                            .build())
                                    )
                                    .build()
                            ))
                            .build()
                    )

                )
                .build()
            )
            .build();
        given(serviceRepository.getBaseCatalog("8.2.2")).willReturn(Optional.of(catalog));

        // act
        Map<String, BaseRequirement> actual = provider.apply("8.2.2");

        // assert
        assertThat(actual)
            .containsOnlyKeys("1.a", "1.1.a", "2.b", "2.1.a", "2.1.1.a");
    }


}
