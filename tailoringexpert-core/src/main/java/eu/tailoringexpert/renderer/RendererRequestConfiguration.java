/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2023 Michael Bädorf and others
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
package eu.tailoringexpert.renderer;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

/**
 * Data object with information needed by renderer at request level.
 *
 * @author Michael Baedorf
 */
@Value
@Builder
public class RendererRequestConfiguration {

    /**
     * Id used for path.
     */
    private String id;

    /**
     * Name of the producer.
     */
    private String name;

    /**
     * System-wide template root directory.
     */
    private String templateHome;

    /**
     * Prefix for resolving external parts to include in HTML.
     */
    @Default
    private String fragmentPrefix = "";
}
