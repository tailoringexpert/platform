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

import static eu.tailoringexpert.domain.LevelType.DEFAULT;
import static java.lang.Integer.parseInt;

import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.function.Function;

import eu.tailoringexpert.domain.Identifier;
import eu.tailoringexpert.domain.Identifier.IdentifierBuilder;
import eu.tailoringexpert.domain.LevelType;

/**
 * Class for creating a identifier of a string.
 *
 * @author Michael Bädorf
 */
public class ToIdentifierFunction implements Function<String, Identifier> {

    /**
     * Reads DRD sheet data and creates a map with its values.
     *
     * @param identifier String also possible containing multiple limitations
     * @return created Identifier object
     */
    @Override
    public Identifier apply(String identifier) {
        String trimed = identifier.trim();

        // get level as first char
        LevelType levelType = LevelType.fromString(trimed.substring(0, 1));

        IdentifierBuilder builder = Identifier.builder()
                .levelType(levelType);

        // get "startposition" after evaluating leveltype
        int startIndex = DEFAULT == levelType ? 0 : 1; // test: no level
        builder.type(trimed.substring(startIndex, startIndex + 1));

        // check if limitation exists. a limitation starts with '('
        if (!containsLimitation(trimed)) {
            builder.level(parseInt(trimed.substring(startIndex + 1).trim()));
        } else {
            // get limitation part of trimed
            builder.level(parseInt(trimed.substring(startIndex + 1, trimed.indexOf('(')).trim()));
            Collection<String> limitations = new LinkedList<>();
            builder.limitations(limitations);

            StringTokenizer stringTokenizer = new StringTokenizer(trimed.substring(trimed.indexOf('(')), "(");
            while (stringTokenizer.hasMoreTokens()) {
                String limitation = stringTokenizer.nextToken();
                limitations.add(limitation.substring(0, limitation.length() - 1));
            }
        }
        return builder.build();
    }

    boolean containsLimitation(String s) {
        return s.indexOf('(') > -1;
    }

}
