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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.DBSetupRunner;
import eu.tailoringexpert.ProjectCreator;
import eu.tailoringexpert.SpringTestConfiguration;
import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.TailoringCatalogChapterResource;
import eu.tailoringexpert.domain.TailoringRequirementResource;
import eu.tailoringexpert.project.CreateProjectTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Log4j2
@SpringJUnitConfig(classes = {SpringTestConfiguration.class})
@EnableTransactionManagement
class RequirementControllerTest {

    @Autowired
    private DBSetupRunner dbSetupRunner;

    @Autowired
    private ProjectCreator projektCreator;

    @Autowired
    private RequirementController controller;


    @BeforeEach
    void setup() throws Exception {
        log.debug("setup started");

        TenantContext.setCurrentTenant("plattform");
        dbSetupRunner.run();

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest())
        );

        log.debug("setup completed");
    }

    @Test
    @DirtiesContext
    void updateChapterRequirementsState_ValidChapter_AllRequirementsDeselected() throws IOException {
        // arrange
        CreateProjectTO createdProjekt = projektCreator.get();


        // act
        ResponseEntity<EntityModel<TailoringCatalogChapterResource>> actual = controller.updateChapterRequirementsState(
            "SAMPLE",
            createdProjekt.getTailoring(),
            "1.4",
            FALSE
        );

        // assert
        assertThat(actual.getStatusCode()).isEqualByComparingTo(OK);

        TailoringCatalogChapterResource resource = actual.getBody().getContent();
        assertThat(allRequirements(resource)).isNotEmpty()
            .allMatch(requirment -> !requirment.getSelected());
    }

    @Test
    @DirtiesContext
    void updateChapterRequirementsState_RequirementSelected_RequirementChangedToDeselected() throws IOException {
        // arrange
        CreateProjectTO createdProjekt = projektCreator.get();

        // act
        ResponseEntity<EntityModel<TailoringRequirementResource>> actual = controller.updateChapterRequirementsState(
            "SAMPLE",
            createdProjekt.getTailoring(),
            "1.4",
            "a",
            FALSE
        );

        // assert
        assertThat(actual.getStatusCode()).isEqualByComparingTo(OK);

        TailoringRequirementResource resource = actual.getBody().getContent();
        assertThat(resource.getSelected()).isFalse();
    }


    @Test
    @DirtiesContext
    void updateRequirementText_NewTextGiven_RequirementTextUpdated() throws IOException {
        // arrange
        CreateProjectTO createdProjekt = projektCreator.get();

        // act
        ResponseEntity<EntityModel<TailoringRequirementResource>> actual = controller.updateRequirementText(
            "SAMPLE",
            createdProjekt.getTailoring(),
            "1.4",
            "a",
            "Dies ist ein neuer Text"
        );

        // assert
        assertThat(actual.getStatusCode()).isEqualByComparingTo(OK);
        TailoringRequirementResource resource = actual.getBody().getContent();
        assertThat(resource.getText()).isEqualTo("Dies ist ein neuer Text");
    }

    @Test
    @DirtiesContext
    void createRequirement_PredecessorRequirementA_NewRequirementA1CreatedAndAdded() throws IOException {
        // arrange
        CreateProjectTO createdProjekt = projektCreator.get();

        // act
        ResponseEntity<EntityModel<TailoringRequirementResource>> actual = controller.createrRequirement(
            "SAMPLE",
            createdProjekt.getTailoring(),
            "1.4",
            "a",
            "Dies ist eine neue Requirement"
        );

        // assert
        assertThat(actual.getStatusCode()).isEqualByComparingTo(CREATED);

        TailoringRequirementResource resource = actual.getBody().getContent();
        assertThat(resource.getPosition()).isEqualTo("a1");
        assertThat(resource.getText()).isEqualTo("Dies ist eine neue Requirement");
    }


    public Stream<TailoringCatalogChapterResource> allChapters(TailoringCatalogChapterResource chapter) {
        return Stream.concat(
            of(chapter),
            nonNull(chapter.getChapters()) ? chapter.getChapters().stream().flatMap(g -> allChapters(g)) : Stream.empty());

    }

    public Stream<TailoringRequirementResource> allRequirements(TailoringCatalogChapterResource chapter) {
        return allChapters(chapter)
            .flatMap(h -> h.getRequirements().stream());
    }
}
