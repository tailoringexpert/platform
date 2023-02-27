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

import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.SelectionVectorProfileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class JPADocumentServiceRepositoryMapperTest {

    private JPADocumentServiceRepositoryMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new JPADocumentServiceRepositoryMapperImpl();
    }

    @Test
    void getSelectionVectorProfiles_ProfileNull_NullReturned() {
        // arrange
        SelectionVectorProfileEntity entity = null;

        // act
        SelectionVectorProfile actual = mapper.getSelectionVectorProfiles(entity);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getSelectionVectorProfiles_ProfileProvided_CreatedProfileReturned() {
        // arrange
        SelectionVectorProfileEntity entity = SelectionVectorProfileEntity.builder()
            .name("PROFILE01")
            .levels(Map.ofEntries(
                entry("W", 10),
                entry("Q", 5)
            ))
            .build();

        // act
        SelectionVectorProfile actual = mapper.getSelectionVectorProfiles(entity);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getLevels()).hasSize(2);
        assertThat(actual.getLevels()).containsEntry("Q", 5);
        assertThat(actual.getLevels()).containsEntry("W", 10);
    }
}
