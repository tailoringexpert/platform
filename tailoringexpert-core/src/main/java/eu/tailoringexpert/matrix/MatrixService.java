/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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
package eu.tailoringexpert.matrix;

import java.util.Collection;
import java.util.Optional;

import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.MatrixFile;
import eu.tailoringexpert.domain.MatrixFileMeta;

/**
 * Service for management of matrix files.
 *
 * @author Michael Bädorf
 */
public interface MatrixService {

    /**
     * Get a list of all available matrices
     * 
     * @return list of all matrices
     */
    Collection<MatrixFileMeta> list();

    Optional<File> get(String name);

    /**
     * Saves the provided matrix file
     * 
     * @param file matrix file to be saved
     * @return saved matrix file
     */
    MatrixFileMeta save(MatrixFile file);

    /**
     * Deletes a matrix file.
     * 
     * @param name of matrix file to be deleted
     * @return true is file has been deleted
     */
    boolean delete(String name);

}
