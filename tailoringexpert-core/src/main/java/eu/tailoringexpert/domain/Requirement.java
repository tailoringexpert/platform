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

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

import static java.util.Objects.nonNull;

@Data
public abstract class Requirement implements Serializable {
    private static final long serialVersionUID = 7854311335886291270L;

    /**
     * Text of the requirement.
     */
    private String text;

    /**
     * Position (in chapter) of requirement.
     */
    private String position;

    /**
     * Requirement origin.
     */
    private Reference reference;

    /**
     * List of all referenced applicable documents.
     */
    private Collection<Document> applicableDocuments;

    /**
     * List of DRDs requirement shall be part of.
     */
    private Collection<DRD> drds;


    protected Requirement(String text, String position, Reference reference, Collection<Document> applicableDocuments, Collection<DRD> drds) {
        this.text = text;
        this.position = position;
        this.reference = reference;
        this.applicableDocuments = applicableDocuments;
        this.drds = drds;
    }

    /**
     * Checks if requirement has references to any DRD.
     *
     * @return true if requirement has references to DRDs otherwise false
     */
    public boolean hasDRD() {
        return nonNull(getDrds()) && !getDrds().isEmpty();
    }

    /**
     * Checks if requirement has references to any applicable document.
     *
     * @return true if requirement has references to applicable document otherwise false
     */
    public boolean hasApplicableDocument() { return  nonNull(getApplicableDocuments()) && !getApplicableDocuments().isEmpty();}
}
