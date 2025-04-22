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
package eu.tailoringexpert.repository;

import eu.tailoringexpert.domain.DocumentEntity;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@Log4j2
@SpringJUnitConfig(classes = {DBConfiguration.class})
@Transactional
class DocumentRepositoryTest {
    @Autowired
    DocumentRepository repository;

    @Test
    void findByTitle_DocumentExist_DocumentEntityReturned() {
        // arrange
        repository.save(DocumentEntity.builder()
            .title("ECSS-Q-ST-80C")
            .issue("C")
            .revision("Rev.1")
            .build());


        // act
        DocumentEntity actual = repository.findByTitle("ECSS-Q-ST-80C");

        // assert
        assertThat(actual).isNotNull();
    }
}
