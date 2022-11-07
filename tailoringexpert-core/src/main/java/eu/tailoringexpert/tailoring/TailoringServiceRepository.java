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

import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling (peristent) data used by @see {@link TailoringService}.
 *
 * @author Michael Bädorf
 */
public interface TailoringServiceRepository {

    /**
     * Load a project using its ideentifier.
     *
     * @param project project identifier
     * @return loaded project
     */
    Optional<Project> getProject(String project);

    /**
     * Update persistent tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring to update
     * @return updated tailoring
     */
    Tailoring updateTailoring(String project, Tailoring tailoring);

    /**
     * Update file in tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @param file      file to update in tailoring
     * @return updated tailoring
     */
    Optional<Tailoring> updateFile(String project, String tailoring, File file);

    /**
     * Load tailoring by name.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @return loaded tailoring
     */
    Optional<Tailoring> getTailoring(String project, String tailoring);

    /**
     * Load screeningsheet (data) of tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @return loaded screeningsheet of tailoring
     */
    Optional<ScreeningSheet> getScreeningSheet(String project, String tailoring);

    /**
     * Load screeningsheet file of tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @return screeningsheet file of tailoring
     */
    Optional<byte[]> getScreeningSheetFile(String project, String tailoring);

    /**
     * Update document signature of tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @param signature signature to update
     * @return updated signature
     */
    Optional<DocumentSignature> updateDocumentSignature(String project, String tailoring, DocumentSignature signature);

    /**
     * Change name of tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailorings current name
     * @param name      New name des tailoring
     * @return Im Falle der Aktualisierung das neue Tailoring, sonst empty
     */
    Optional<Tailoring> updateName(String project, String tailoring, String name);

    /**
     * Load filelist of tailoring.
     * <p>
     * Raw-Data (bytearray) will <strong>NOT</strong> be loaded!
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @return List of files without file data
     */
    Optional<List<File>> getFileList(String project, String tailoring);

    /**
     * Load a file.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @param filename  name of file to load
     * @return Rohdaten der File
     */
    Optional<File> getFile(String project, String tailoring, String filename);

    /**
     * Delete a file.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @param filename  name of file to delete
     * @return true, if file deleted
     */
    boolean deleteFile(String project, String tailoring, String filename);

    /**
     * Load defined selectionvector profiles.
     *
     * @return List of all selectionvector profiles
     */
    Collection<SelectionVectorProfile> getSelectionVectorProfile();

    /**
     * Load all defined default document signatures.
     *
     * @return all defined default document signatures
     */
    Collection<DocumentSignature> getDefaultSignatures();

    /**
     * Delete a tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring to delete
     * @return true, if deleted
     */
    boolean deleteTailoring(String project, String tailoring);

    /**
     * Add note to tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring to add note to
     * @param note      Note to add
     * @return In case successful adding tailoring, otherwise empty
     */
    Optional<Tailoring> addNote(String project, String tailoring, Note note);

    /**
     * Checks if a tailoring belongs to a dedicated project.
     *
     * @param project identifier of project
     * @param name    name of tailoring to check
     * @return true if name of the tailoring is part of project tailorings
     */
    boolean existsTailoring(String project, String name);

    /**
     * Set state of tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring to add note to
     * @param state     state to set
     * @return In case successful setting state tailoring, otherwise empty
     */
    Optional<Tailoring> setState(String project, String tailoring, TailoringState state);


}


