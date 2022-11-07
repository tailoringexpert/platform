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
package eu.tailoringexpert.screeningsheet;

import eu.tailoringexpert.Tenant;
import eu.tailoringexpert.domain.ScreeningSheet;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static eu.tailoringexpert.domain.ScreeningSheet.PARAMETER_PHASE;

@Log4j2
@Tenant("plattform")
public class PlattformScreeningSheetParameterProvider implements ScreeningSheetParameterProvider {

    @Override
    public Collection<ScreeningSheetParameterField> parse(InputStream is) {
        final List<PDField> fields = new ArrayList<>();
        try (PDDocument document = PDDocument.load(is)) {
            fields.addAll(document.getDocumentCatalog().getAcroForm().getFields());
        } catch (IOException e) {
            log.catching(e);
        }

        List<PDField> textfelder = filterTextfelder(fields);
        Collection<ScreeningSheetParameterField> result = new ArrayList<>();

        result.addAll(mapFields(textfelder, "Project", ScreeningSheet.PARAMETER_PROJECT));

        List<PDField> selectedParameters = filterCheckedCheckboxes(fields);
        result.addAll(mapFields(selectedParameters, PARAMETER_PHASE, "0"));
        result.addAll(mapFields(selectedParameters, PARAMETER_PHASE, "A"));
        result.addAll(mapFields(selectedParameters, PARAMETER_PHASE, "B"));
        result.addAll(mapFields(selectedParameters, PARAMETER_PHASE, "C"));
        result.addAll(mapFields(selectedParameters, PARAMETER_PHASE, "D"));
        result.addAll(mapFields(selectedParameters, PARAMETER_PHASE, "E"));
        result.addAll(mapFields(selectedParameters, PARAMETER_PHASE, "F"));

        return result;
    }

    private static List<PDField> filterTextfelder(List<PDField> fields) {
        return fields.stream()
            .filter(PDTextField.class::isInstance)
            .collect(Collectors.toList());
    }

    private List<PDField> filterCheckedCheckboxes(List<PDField> fields) {
        return fields.stream()
            .filter(PDCheckBox.class::isInstance)
            .map(PDCheckBox.class::cast)
            .filter(PDCheckBox::isChecked)
            .collect(Collectors.toList());
    }


    private Collection<ScreeningSheetParameterField> mapFields(
        List<PDField> fields,
        String category,
        String name) {
        return fields.stream()
            .filter(field -> name.equalsIgnoreCase(field.getPartialName()))
            .map(field -> ScreeningSheetParameterField.builder()
                .category(category)
                .name(field.getPartialName())
                .label(field.getValueAsString())
                .build())
            .collect(Collectors.toList());
    }

}
