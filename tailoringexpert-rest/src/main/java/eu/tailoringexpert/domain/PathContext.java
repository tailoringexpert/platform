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

    String katalog;
    String projekt;
    String tailoring;
    String kapitel;
    String anforderung;
    Boolean ausgewaehlt;

    public Map<String, String> parameter() {
        final HashMap<String, String> result = new HashMap<>();
        result.put("version", katalog);
        result.put("projekt", projekt);
        result.put("tailoring", tailoring);
        result.put("kapitel", kapitel);
        result.put("anforderung", anforderung);
        result.put("ausgewaehlt", Objects.nonNull(ausgewaehlt) ? ausgewaehlt.toString() : null);
        return result;
    }
}
