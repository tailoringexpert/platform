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
package eu.tailoringexpert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Identifier;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Reference;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.util.Arrays.asList;
import static java.util.Locale.GERMANY;
import static org.assertj.core.api.Assertions.assertThatNoException;

@Log4j2
class StructurTest {

    FileSaver fileSaver;
    ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        this.fileSaver = new FileSaver();
        this.objectMapper = new ObjectMapper()
            .registerModules(new JavaTimeModule())
            .enable(FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(INDENT_OUTPUT)
            .disable(FAIL_ON_EMPTY_BEANS)
            .setVisibility(FIELD, ANY)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd", GERMANY));
    }

    @Test
    void doit() throws JsonProcessingException {
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .position(1)
                .chapters(asList(
                    Chapter.<BaseRequirement>builder()
                        .name("Space Debris")
                        .number("11")
                        .position(11)
                        .requirements(asList(
                            BaseRequirement.builder()
                                .position("a")
                                .reference(Reference.builder()
                                    .text("Q-ST-10C Rev.1. para. 4.1")
                                    .changed(false)
                                    .logo(Logo.builder()
                                        .name("ECSS")
                                        .url("${asset.server}/assets/arzs/${catalog.version}/catalog/ecss.png")
                                        .build())
                                    .build())
                                .identifiers(asList(
                                    Identifier.builder()
                                        .type("J") //Space Debris (junk)
                                        .level(7)
                                        .limitations(asList(
                                            "ISS"
                                        ))
                                        .build()
                                ))
                                .text("Hier kommt der <b>HTML Anforderungtext</b> rein")
                                .phases(asList(Phase.A, Phase.E))
                                .drds(asList(
                                    DRD.builder()
                                        .number("DRD-11.01")
                                        .title("Keep your environment clean")
                                        .action("AC")
                                        .deliveryDate("Wichtig ist hier das Semikolon als Trennzeichen!; MPCB; respectively summary lists for CDR; MRR; QR; AR")
                                        .build()
                                ))
                                .build())
                        )
                        .chapters(asList(
                                Chapter.<BaseRequirement>builder()
                                    .name("Unterkapitel")
                                    .number("11.1")
                                    .position(1)
                                    .requirements(asList(
                                        BaseRequirement.builder()
                                            .position("a")
                                            .text("Unteranforderung")
                                            .build())
                                    ).build()
                            )
                        )
                        .build())
                )
                .build())
            .build();


        log.info(objectMapper.writeValueAsString(catalog));
        fileSaver.accept("beispiel.json", objectMapper.writeValueAsBytes(catalog));
        log.info(catalog);

        assertThatNoException();
    }
}
