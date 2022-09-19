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

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringInformation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface for working on/with tailorings.
 *
 * @author Michael Bädorf
 */
public interface TailoringService {

    /**
     * Create a new tailoring.<p>
     * A tailoring con consist of multiple phases.
     *
     * @param name                      Tailoring name
     * @param identifier                DocID identifier
     * @param screeningSheet            screeningsheet of tailoring
     * @param applicableSelectionVector selection vector to use of selecting requirements
     * @param catalog                   Base catalog to use for creating the tailoring
     * @return Create tailoring
     */
    Tailoring createTailoring(String name, String identifier, ScreeningSheet screeningSheet, SelectionVector applicableSelectionVector, Catalog<BaseRequirement> catalog);

    /**
     * Add a file to tailoring
     *
     * @param project   project identidier
     * @param tailoring tailoring, file to add to
     * @param name      name of file to add
     * @param data      Raw-data of file
     * @return Tailoring, with added file
     */
    Optional<Tailoring> addFile(String project, String tailoring, String name, byte[] data);

    /**
     * Create a requirements document of tailoring.
     *
     * @param project   Project identifier
     * @param tailoring Tailoring to create document of
     * @return Created document file
     */
    Optional<File> createRequirementDocument(String project, String tailoring);

    /**
     * Create a new document for camparing automatic and manual tailoring.
     *
     * @param project   Project identifier
     * @param tailoring Tailoring to create comparison document of
     * @return Created document file
     */
    Optional<File> createComparisonDocument(String project, String tailoring);

    /**
     * Create all documents of tialoring.<p>
     * Documents will be provided in {@code Zip-File}.
     *
     * @param project   Project identifier
     * @param tailoring Tailoring to create all documents of
     * @return Created zip-file containing all created documents
     */
    Optional<File> createDocuments(String project, String tailoring);

    /**
     * Get requirement catalog of tailoring.
     *
     * @param project   Project identifier
     * @param tailoring Tailoring, to get catalog of
     * @return Requirement catalog of tailoring
     */
    Optional<Catalog<TailoringRequirement>> getCatalog(String project, String tailoring);

    /**
     * Get requirements of requests tailoring catalog chapter.
     *
     * @param project   Project identifier
     * @param tailoring tailoring name
     * @param chapter   Chapter, to get requirements of
     * @return Alle requirements of chapter
     */
    Optional<List<TailoringRequirement>> getRequirements(String project, String tailoring, String chapter);

    /**
     * Get screeningsheet of tailoring.
     *
     * @param project   Project identifier
     * @param tailoring Tailoring name
     * @return ScreeningSheet of tailoring
     */
    Optional<ScreeningSheet> getScreeningSheet(String project, String tailoring);

    /**
     * Get applied selection vector of tailoring.
     *
     * @param project   Project identifier
     * @param tailoring Tailoring name
     * @return applied selection vector of tailoring
     */
    Optional<SelectionVector> getSelectionVector(String project, String tailoring);

    /**
     * Get chapter of tailoring catalog.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @param chapter   number of chapter to get
     * @return all chapter data
     */
    Optional<Chapter<TailoringRequirement>> getChapter(String project, String tailoring, String chapter);

    /**
     * Get list of all document signatures of tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @return all document signature of tailoring
     */
    Optional<Collection<DocumentSignature>> getDocumentSignatures(String project, String tailoring);

    /**
     * Update document signature of tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @param signature signature to update
     * @return updates signature
     */
    Optional<DocumentSignature> updateDocumentSignature(String project, String tailoring, DocumentSignature signature);

    /**
     * Change name of tailoring.
     *
     * @param project   project identidier
     * @param tailoring current name of tailoring
     * @param name      new name of tailoring
     * @return in case of successful change new tailoring, otherwise empty
     */
    Optional<TailoringInformation> updateName(String project, String tailoring, String name);

    /**
     * Import requirements contained in an Excel data byte arrays.
     *
     * @param project   project identifier
     * @param tailoring tailoring, to import requirements to
     * @param data      Excel ByteArray containing entries of Chapter, Position und State
     */
    void updateImportedRequirements(String project, String tailoring, byte[] data);

    /**
     * Delete a tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailorig to delete
     * @return true, if deleted
     */
    Optional<Boolean> deleteTailoring(String project, String tailoring);

    /**
     * Add note to tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailorig to add note to
     * @param note      note/text to add
     * @return in case of successful add tailoring, otherwise empty
     */
    Optional<TailoringInformation> addNote(String project, String tailoring, String note);

    /**
     * Get all notes of project tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring to get notes of
     * @return
     */
    Optional<Collection<Note>> getNotes(String project, String tailoring);

    /**
     * Get a note of project tailoring identified by its number.
     *
     * @param project   project identifier
     * @param tailoring tailoring to get note of
     * @param note      note/text number to get
     * @return
     */
    Optional<Note> getNote(String project, String tailoring, Integer note);

}
