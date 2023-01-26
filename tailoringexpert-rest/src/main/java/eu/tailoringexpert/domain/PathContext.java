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
package eu.tailoringexpert.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Value
@Builder
@Getter
public class PathContext {

    String catalog;
    String project;
    String tailoring;
    String chapter;
    String requirment;
    Boolean selected;
    String note;
    String tailoringState;
    String projectState;

    public Map<String, String> parameter() {
        final HashMap<String, String> result = new HashMap<>();
        result.put("version", catalog);
        result.put("project", project);
        result.put("tailoring", tailoring);
        result.put("chapter", chapter);
        result.put("requirement", requirment);
        result.put("selected", Objects.nonNull(selected) ? selected.toString() : null);
        result.put("note", note);
        result.put("tailoringstate", tailoringState);
        result.put("projectstate", projectState);
        return result;
    }
}
