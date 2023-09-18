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

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static java.util.Collections.emptyMap;

/**
 * Create PDF Compliance Matrix.
 *
 * @author Michael Bädorf
 */
@Log4j2
public class CMRequirementsPDFDocumentCreator extends CMPDFDocumentCreator {


    public CMRequirementsPDFDocumentCreator(@NonNull HTMLTemplateEngine templateEngine,
                                            @NonNull PDFEngine pdfEngine,
                                            @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        super(templateEngine, pdfEngine, drdProvider);
    }

    /**
     * Hook fpr adding also requirements to CM instead of only adding chapters.<p>
     * The hook itself doesn't add requirements to CM.
     *
     * @param requirements requirements to add
     * @param level        level of requirements
     * @param rows         collection to add elements to
     * @param placeholders placeholder to use
     */
    @Override
    protected void addRequirements(Collection<TailoringRequirement> requirements,
                                   int level,
                                   Collection<CMElement> rows,
                                   Map<String, Object> placeholders) {
        requirements.forEach(requirement -> rows.add(CMElement.builder()
            .level(level)
            .number(xhtml(requirement.getPosition(), emptyMap()))
            .name(xhtml(requirement.getText(), placeholders))
            .requirement(true)
            .build())
        );
    }


}
