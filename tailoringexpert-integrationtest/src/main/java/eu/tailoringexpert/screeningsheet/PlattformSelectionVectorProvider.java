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
import eu.tailoringexpert.domain.Parameter;
import eu.tailoringexpert.domain.SelectionVector;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
@Tenant("plattform")
public class PlattformSelectionVectorProvider implements SelectionVectorProvider {

    public static final String PRODUCTASSURANCE = "A";
    public static final String QUALITYASSURANCE = "Q";
    public static final String MAINTAINABILITY = "M";
    public static final String RELIABILITY = "R";
    public static final String EEE = "E";
    public static final String PMMP = "P";
    public static final String SOFTWARE = "W";
    public static final String SAFETY = "S";
    public static final String SPACEDEBRIS = "J";
    public static final String PLANETARYPROTECTION = "F";
    public static final String SPACECYBERSECURITY = "C";
    public static final String GROUNDSEGMENT = "B";

    @Override
    public SelectionVector apply(Collection<Parameter> parameterNames) {


        return SelectionVector.builder()
            .level(PRODUCTASSURANCE, 5)
            .level(QUALITYASSURANCE, 5)
            .level(EEE, 5)
            .level(PMMP, 5)
            .level(RELIABILITY, 5)
            .level(SAFETY, 5)
            .level(SOFTWARE, 5)
            .level(SPACEDEBRIS, 5)
            .level(MAINTAINABILITY, 5)
            .level(SPACECYBERSECURITY, 5)
            .level(PLANETARYPROTECTION, 5)
            .level(GROUNDSEGMENT, 5)
            .build();
    }
}
