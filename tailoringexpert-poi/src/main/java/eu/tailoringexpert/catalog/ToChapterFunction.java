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

import eu.tailoringexpert.TailoringexpertException;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Document;
import eu.tailoringexpert.domain.Identifier;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Reference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Collections.list;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

/**
 * Class for creating a root chapter of a base catalogue.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Log4j2
public class ToChapterFunction implements Function<Sheet, Chapter<BaseRequirement>> {

    @NonNull
    Function<Sheet, Map<String, DRD>> toDRDMapping;
    @NonNull
    Function<Sheet, Map<String, Logo>> toLogoMapping;
    @NonNull
    Function<Sheet, Map<String, Document>> toDocumentMapping;
    @NonNull
    Function<String, Identifier> toIdentifier;
    @NonNull
    BiFunction<String, Map<String, Logo>, Logo> toLogo;
    @NonNull
    BiFunction<String, Logo, Reference> toReference;

    @NonNull
    BiConsumer<Chapter<BaseRequirement>, Map<String, Chapter<BaseRequirement>>> rootConsumer;

    /**
     * Reads requirement sheet data and creates a valid root chapter.
     *
     * @param sheet sheet to read requirement data from
     * @return root chapter with full hierarchy
     */
    @Override
    public Chapter<BaseRequirement> apply(Sheet sheet) {
        Map<String, Logo> logos = toLogoMapping.apply(sheet.getWorkbook().getSheet("LOGO"));
        Map<String, DRD> drds = toDRDMapping.apply(sheet.getWorkbook().getSheet("DRD"));
        Map<String, Document> documents = toDocumentMapping.apply(sheet.getWorkbook().getSheet("AD"));

        Map<String, Chapter<BaseRequirement>> chapters = new TreeMap<>();
        Chapter<BaseRequirement> current = new Chapter<>();

        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            while (isHeader(row)) {
                String number = row.getCell(0, CREATE_NULL_AS_BLANK).getStringCellValue();
                current = createChapter(row);
                chapters.put(number, current);
                row = rowIterator.next();
            }

            current.getRequirements().add(createRequirement(row, logos, documents, drds));
        }

        Chapter<BaseRequirement> result = new Chapter<>();
        result.setName("/");
        result.setPosition(1);
        result.setChapters(new LinkedList<>());

        rootConsumer.accept(result, chapters);

        return result;
    }

    private boolean isHeader(Row row) {
        return row.getCell(2, CREATE_NULL_AS_BLANK).getStringCellValue().isBlank();
    }

    private int getPosition(String number) {
        int index = number.lastIndexOf('.');
        return (index == -1) ?
            parseInt(number) :
            parseInt(number.substring(index + 1));
    }


    private Chapter<BaseRequirement> createChapter(Row row) {
        try {
            Chapter<BaseRequirement> result = new Chapter<>();
            String number = row.getCell(0, CREATE_NULL_AS_BLANK).getStringCellValue();
            result.setName(row.getCell(1, CREATE_NULL_AS_BLANK).getStringCellValue());
            result.setNumber(number);
            result.setPosition(getPosition(number));
            result.setRequirements(new LinkedList<>());
            result.setChapters(new LinkedList<>());
            return result;
        } catch (Exception nfe) {
            throw new TailoringexpertException("Could not convert worksheet chapter row " + (row.getRowNum() + 1));

        }
    }

    private BaseRequirement createRequirement(Row row, Map<String, Logo> logos, Map<String, Document> documents, Map<String, DRD> drds) {
        Logo logo = toLogo.apply(row.getCell(5, CREATE_NULL_AS_BLANK).getStringCellValue(), logos);
        Reference reference = toReference.apply(row.getCell(4, CREATE_NULL_AS_BLANK).getStringCellValue(), logo);

        return BaseRequirement.builder()
            .text(row.getCell(1, CREATE_NULL_AS_BLANK).getStringCellValue())
            .position(row.getCell(0, CREATE_NULL_AS_BLANK).getStringCellValue())
            .phases(stream(row.getCell(2, CREATE_NULL_AS_BLANK).getStringCellValue().split("\n"))
                .map(Phase::fromString)
                .collect(toCollection(LinkedList::new)))
            .identifiers(list(new StringTokenizer(row.getCell(3, CREATE_NULL_AS_BLANK).getStringCellValue().trim(), "\n"))
                .stream()
                .map(String.class::cast)
                .map(token -> toIdentifier.apply(token))
                .collect(toCollection(LinkedList::new)))
            .drds(list(new StringTokenizer(row.getCell(6, CREATE_NULL_AS_BLANK).getStringCellValue().trim(), "\n"))
                .stream()
                .map(String.class::cast)
                .map(drds::get)
                .collect(collectingAndThen(toCollection(LinkedList::new), d -> !d.isEmpty() ? d : null)))
            .applicableDocuments(list(new StringTokenizer(row.getCell(7, CREATE_NULL_AS_BLANK).getStringCellValue().trim(), "\n"))
                .stream()
                .map(String.class::cast)
                .map(documents::get)
                .collect(collectingAndThen(toCollection(LinkedList::new), d -> !d.isEmpty() ? d : null)))
            .reference(reference)
            .build();
    }

}
