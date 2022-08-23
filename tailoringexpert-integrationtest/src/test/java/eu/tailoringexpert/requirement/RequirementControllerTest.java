package eu.tailoringexpert.requirement;

import eu.tailoringexpert.DBSetupRunner;
import eu.tailoringexpert.ProjectCreator;
import eu.tailoringexpert.SpringConfiguration;
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
@SpringJUnitConfig(classes = {SpringConfiguration.class})
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
    void createrRequirement_PredecessorRequirementA_NewRequirementA1CreatedAndAdded() throws IOException {
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
