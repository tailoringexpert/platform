package eu.tailoringexpert.domain;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

class RequirementTest {

    @Test
    void hasApplicableDocument_nullList_falseReturned() {
        // arrange
        TailoringRequirement requirement = TailoringRequirement.builder()
            .build();

        // act
        boolean actual = requirement.hasApplicableDocument();

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void hasApplicableDocument_emptyList_falseReturned() {
        // arrange
        TailoringRequirement requirement = TailoringRequirement.builder()
            .applicableDocuments(emptyList())
            .build();

        // act
        boolean actual = requirement.hasApplicableDocument();

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void hasApplicableDocument_oneDocumentList_trueReturned() {
        // arrange
        TailoringRequirement requirement = TailoringRequirement.builder()
            .applicableDocuments(of(Document.builder().build()))
            .build();

        // act
        boolean actual = requirement.hasApplicableDocument();

        // assert
        assertThat(actual).isTrue();
    }
}
