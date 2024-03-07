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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.File;

import java.util.Collection;
import java.util.Optional;

/**
 * Interface for file operations.
 *
 * @author Michael Bädorf
 */
public interface AttachmentService {

    /**
     * Gets a file of the specified tailoring.
     *
     * @param project   name of the project to get file of
     * @param tailoring tailoring of the tailoring to get file of
     * @param filename  name of the file to get
     * @return file if existing, otherwise empty
     */
    Optional<File> load(String project, String tailoring, String filename);

    /**
     * Gets the list of files of the specified tailoring.
     *
     * @param project   name of the project to get file of
     * @param tailoring tailoring of the tailoring to get file of
     * @return collections files
     */
    Collection<File> list(String project, String tailoring);

    Optional<File> save(String project, String tailoring, File file);

    boolean delete(String project, String tailoring,  String filename);
}
