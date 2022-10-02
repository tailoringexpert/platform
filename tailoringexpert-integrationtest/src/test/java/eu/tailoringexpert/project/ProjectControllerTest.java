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

import eu.tailoringexpert.DBSetupRunner;
import eu.tailoringexpert.ProjectCreator;
import eu.tailoringexpert.SpringTestConfiguration;
import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.ProjectResource;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetResource;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.SelectionVectorResource;
import eu.tailoringexpert.domain.TailoringResource;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.Phase.D;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Log4j2
@SpringJUnitConfig(classes = {SpringTestConfiguration.class})
@EnableTransactionManagement
class ProjectControllerTest {

    @Autowired
    private DBSetupRunner dbSetupRunner;

    @Autowired
    private ProjectCreator projectCreator;

    @Autowired
    private ProjectController controller;

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
    void getProjects_ProjectsExists_ProjectListWitStateOKReturned() {
        // arrange
        projectCreator.get();

        // act
        ResponseEntity<CollectionModel<EntityModel<ProjectResource>>> actual = controller.getProjects();

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody().getContent()).hasSize(1);
    }

    @Test
    @DirtiesContext
    void createProject_ValidRequest_ProjectCreatedStateCreatedReturned() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        ProjectCreationRequest request = ProjectCreationRequest.builder()
            .screeningSheet(ScreeningSheet.builder()
                .phases(List.of(ZERO, A, B, C, D))
                .data(data)
                .selectionVector(selectionVector)
                .build())
            .selectionVector(selectionVector)
            .build();

        // act
        ResponseEntity<Void> actual = controller.postProject("8.2.1", request);

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);
    }

    @Test
    @DirtiesContext
    void getProject_ProjectExists_ProjectWithStateOKReturned() throws IOException {
        // arrange
        CreateProjectTO createdProject = projectCreator.get();

        // act
        ResponseEntity<EntityModel<ProjectResource>> actual = controller.getProject(createdProject.getProject());

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody().getContent().getName()).isEqualTo(createdProject.getProject());
    }

    @Test
    @DirtiesContext
    void getScreeningSheet_ProjectScreeningSheetExists_ScreeningSheetValueWithoutRawDataReturned() throws IOException {
        // arrange
        CreateProjectTO createdProject = projectCreator.get();

        // act
        ResponseEntity<EntityModel<ScreeningSheetResource>> actual = controller.getScreeningSheet(createdProject.getProject());

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(OK);

        ScreeningSheetResource resource = actual.getBody().getContent();
        assertThat(resource.getData()).isNull();
        assertThat(resource.getSelectionVector()).isNotNull();
        assertThat(resource.getParameters()).isNotNull();

    }

    @Test
    @DirtiesContext
    void getScreeningSheetFile_ProjectScreeningSheetExists_FileRawDataWithStateOKReturned() throws IOException {
        // arrange
        CreateProjectTO createdProject = projectCreator.get();

        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        // act
        ResponseEntity<byte[]> actual = controller.getScreeningSheetFile(createdProject.getProject());

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(OK);

        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody()).isEqualTo(data);
    }

    @Test
    @DirtiesContext
    void deleteProject_ProjectExists_ProjectDeletedStateNoContentReturned() throws IOException {
        // arrange
        CreateProjectTO createdProject = projectCreator.get();

        // act
        ResponseEntity<EntityModel<Void>> actual = controller.deleteProject(createdProject.getProject());

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(controller.getProject(createdProject.getProject()).getStatusCode()).isEqualTo(NOT_FOUND);

    }

    @Test
    @DirtiesContext
    void copyProject_ProjectToCopyExists_ProjectCopyiedStateCreatedReturned() throws IOException {
        // arrange
        CreateProjectTO createdProject = projectCreator.get();

        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet2.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile screeningsheet = new MockMultipartFile("datei", "screeningsheet2.pdf", "text/plain", data);

        // act
        ResponseEntity<EntityModel<ProjectResource>> actual = controller.copyProject(createdProject.getProject(), screeningsheet);

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);

        ProjectResource resource = actual.getBody().getContent();
        assertThat(resource.getName()).isEqualTo("SAMPLE2");
    }

    @Test
    @DirtiesContext
    void addNewTailoring_ProjectExists_TailoringCreatedAddedToProjectStateOKReturned() throws IOException {
        // arrange
        CreateProjectTO createdProject = projectCreator.get();

        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        ProjectCreationRequest request = ProjectCreationRequest.builder()
            .catalog("8.2.1")
            .screeningSheet(ScreeningSheet.builder()
                .data(data)
                .selectionVector(selectionVector)
                .build())
            .selectionVector(selectionVector)
            .build();

        // act
        ResponseEntity<EntityModel<Void>> actual = controller.postTailoring(createdProject.getProject(), request);

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);

        ProjectResource resource = controller.getProject(createdProject.getProject()).getBody().getContent();
        assertThat(resource.getTailorings()).hasSize(2);

        TailoringResource neuePhase = new ArrayList<TailoringResource>(resource.getTailorings()).get(1);
        assertThat(neuePhase.getName()).isEqualTo("master1");
        assertThat(neuePhase.getPhases()).containsExactly(ZERO, A, B, C, D);
    }

    @Test
    @DirtiesContext
    void getSelectionVector_ProjectExists_SelectionVectorStateOKReturned() throws IOException {
        // arrange
        CreateProjectTO createdProject = projectCreator.get();

        // act
        ResponseEntity<EntityModel<SelectionVectorResource>> actual = controller.getSelectionVector(createdProject.getProject());

        // assert
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody().getContent()).isNotNull();
    }
}
