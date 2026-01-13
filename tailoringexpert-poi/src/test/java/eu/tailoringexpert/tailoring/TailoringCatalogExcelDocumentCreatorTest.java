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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.BiConsumer;

import static eu.tailoringexpert.domain.Phase.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Log4j2
class TailoringCatalogExcelDocumentCreatorTest {

    TailoringCatalogExcelDocumentCreator creator;
    JsonMapper objectMapper;
    BiConsumer<String, byte[]> fileSaver = (dateiName, data) -> {
        try {
            Path path = Paths.get("target", dateiName);
            OutputStream out = Files.newOutputStream(path);
            out.write(data);
            out.close();
        } catch (Exception e) {
            log.catching(e);
        }
    };

    @BeforeEach
    void beforeEach() {
        this.objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
            .build();

        this.creator = new TailoringCatalogExcelDocumentCreator();

    }


    @Test
    void createDocument_TailoringCatalogNull_NullReturned() {
        // arrange

        // act
        File actual = creator.createDocument("4711", Tailoring.builder().build(), emptyMap());

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void createDocument_ValidTailoringCatalog_NameIsDocIdXslx() throws Exception {
        // arrange
        Catalog<TailoringRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringkatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructParametricType(Catalog.class, TailoringRequirement.class)
            );
        }

        Tailoring tailoring = Tailoring.builder()
            .name("ut")
            .catalog(catalog)
            .signatures(emptyList())
            .phases(Arrays.asList(ZERO, A, B, C, D, E, F))
            .build();

        // act
        File actual = creator.createDocument("42", tailoring, emptyMap());

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo("42.xlsx");
        fileSaver.accept(actual.getName(), actual.getData());
    }

    @Test
    void createDocument_ValidTailoringCatalog_WorkbookWith2SheetsCreated() throws Exception {
        // arrange
        Catalog<TailoringRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringkatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructParametricType(Catalog.class, TailoringRequirement.class)
            );
        }

        Tailoring tailoring = Tailoring.builder()
            .name("ut")
            .catalog(catalog)
            .signatures(emptyList())
            .phases(Arrays.asList(ZERO, A, B, C, D, E, F))
            .build();

        // act
        File actual = creator.createDocument("42", tailoring, emptyMap());

        // assert
        assertThat(actual).isNotNull();
        try (ByteArrayInputStream is = new ByteArrayInputStream(actual.getData());
             Workbook workbook = WorkbookFactory.create(is)) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
            assertThat(workbook.getSheetAt(0).getSheetName()).isEqualTo("ut-8.2.1-IMPORT");
            assertThat(workbook.getSheetAt(1).getSheetName()).isEqualTo("ut-8.2.1-EXPORT");
        }
    }

    @Test
    void applyValidationToColumn_XSSFDataValidation_ValidationAddedToSheet() {
        // arrange
        Sheet sheet = new XSSFWorkbook().createSheet();
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue("HEADER");
        row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue("YES");

        // act
        creator.applyValidationToColumn(sheet, 0);

        // assert
        assertThat(sheet.getDataValidations()).hasSize(1);
        assertThat(sheet.getDataValidations().get(0).getShowErrorBox()).isTrue();
        assertThat(sheet.getDataValidations().get(0).getSuppressDropDownArrow()).isFalse();
    }
}
