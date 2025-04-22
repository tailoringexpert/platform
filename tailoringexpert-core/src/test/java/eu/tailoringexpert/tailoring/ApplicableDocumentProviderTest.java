package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicableDocumentProviderTest {

    ApplicableDocumentProvider provider;


    @BeforeEach
    void beforeEach() {
        this.provider = new ApplicableDocumentProvider(new DocumentNumberComparator());
    }


    @Test
    void doit() {
        // arrange
        Document q80 = Document.builder()
            .number("bh")
            .title("ECSS-Q-ST-80")
            .issue("C")
            .revision("Rev.1")
            .build();

        Document e40 = Document.builder()
            .number("m")
            .title("ECSS-E-ST-40")
            .issue("C")
            .revision("Rev.1")
            .build();

        TailoringRequirement requirement0101 = TailoringRequirement.builder()
            .selected(true)
            .applicableDocuments(Arrays.asList(
                q80
            ))
            .build();

        TailoringRequirement requirement0102 = TailoringRequirement.builder()
            .selected(true)
            .applicableDocuments(Arrays.asList(
                q80
            ))
            .build();

        TailoringRequirement requirement1101 = TailoringRequirement.builder()
            .selected(true)
            .applicableDocuments(Arrays.asList(
                q80,
                e40
            ))
            .build();

        Catalog<TailoringRequirement> catalog = Catalog.<TailoringRequirement>builder()
            .toc(Chapter.<TailoringRequirement>builder()
                .name("/")
                .chapters(of(
                        Chapter.<TailoringRequirement>builder()
                            .number("1")
                            .requirements(of(
                                requirement0101,
                                requirement0102
                            ))
                            .chapters(of(
                                Chapter.<TailoringRequirement>builder()
                                    .number("1.1")
                                    .requirements(of(
                                        requirement1101
                                    ))
                                    .build()

                            ))
                            .build()
                    )
                ).build()
            )
            .build();


        // act
        Collection<Document> actual = provider.apply(catalog);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .hasSize(2)
            .containsExactlyElementsOf(of(e40, q80));
    }

}
