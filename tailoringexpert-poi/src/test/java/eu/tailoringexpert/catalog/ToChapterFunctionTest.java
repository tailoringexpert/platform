/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2024 Michael Bädorf and others
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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.TailoringexpertException;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Identifier;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.Reference;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

@Log4j2
class ToChapterFunctionTest {

    ObjectMapper objectMapper;
    FileSaver fileSaver;

    Function<Sheet, Map<String, DRD>> toDRDMappingMock;
    Function<Sheet, Map<String, Logo>> toLogoMappingMock;


    Function<String, Identifier> toIdentifierFunctionMock;

    BiFunction<String, Map<String, Logo>, Logo> toLogoFunctionMock;

    BiFunction<String, Logo, Reference> toReferenceFunctionMock;
    BiConsumer<Chapter<BaseRequirement>, Map<String, Chapter<BaseRequirement>>> chapterConsumerMock;

    ToChapterFunction toFunction;

    @BeforeEach
    void setup() {
        this.toDRDMappingMock = mock(Function.class);
        this.toLogoMappingMock = mock(Function.class);
        this.toIdentifierFunctionMock = mock(Function.class);
        this.toLogoFunctionMock = mock(BiFunction.class);
        this.toReferenceFunctionMock = mock(BiFunction.class);
        this.chapterConsumerMock = mock(BiConsumer.class);

        this.toFunction = new ToChapterFunction(
            this.toDRDMappingMock,
            this.toLogoMappingMock,
            this.toIdentifierFunctionMock,
            this.toLogoFunctionMock,
            this.toReferenceFunctionMock,
            this.chapterConsumerMock
        );
    }

    @Test
    void apply_NoMocks_ChapterCreated() throws Exception {
        // arrange
        Workbook wb = null;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.xlsx")) {
            assert nonNull(is);
            wb = WorkbookFactory.create(is);
        }

        ToChapterFunction noMocksToFunction = new ToChapterFunction(
            new ToDRDMappingFunction(),
            new ToLogoMappingFunction(),
            new ToIdentifierFunction(),
            new ToLogoFunction(),
            new ToReferenceFunction(),
            new BuildingChapterConsumer()
        );

        // act
        Chapter<BaseRequirement> actual = noMocksToFunction.apply(wb.getSheetAt(0));

        // assert
        log.debug(actual);
        assertThat(actual).isNotNull();

    }

    @Test
    void apply_NoMocksChapterWithoutPhases_TailoringExceptionThrown() throws Exception {
        // arrange
        Workbook wb = null;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog_requirement_without_phase.xlsx")) {
            assert nonNull(is);
            wb = WorkbookFactory.create(is);
        }

        ToChapterFunction noMocksToFunction = new ToChapterFunction(
            new ToDRDMappingFunction(),
            new ToLogoMappingFunction(),
            new ToIdentifierFunction(),
            new ToLogoFunction(),
            new ToReferenceFunction(),
            new BuildingChapterConsumer()
        );

        // act
        Workbook finalWb = wb;
        Throwable actual = catchThrowable(() -> noMocksToFunction.apply(finalWb.getSheetAt(0)));

        // assert
        assertThat(actual)
            .isInstanceOf(TailoringexpertException.class)
            .hasMessage("Could not convert worksheet chapter row 3");
    }

}
