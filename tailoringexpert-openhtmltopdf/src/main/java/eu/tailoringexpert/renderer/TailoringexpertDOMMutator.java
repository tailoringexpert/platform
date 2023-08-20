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

import com.openhtmltopdf.extend.FSDOMMutator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

import static java.util.stream.IntStream.range;

/**
 * Mutator for changing elements in HTML tags.
 * Document generation is done by openhtmltopdf. Resources must be relative. If you use a basecatalog and
 * tailoringcatalog with an absolute path on the current server, the links could not be resolved.
 * This mutator will change the src attributes to the filename only. Files must be relative to the base uri of the
 * processed template.
 */
public class TailoringexpertDOMMutator implements FSDOMMutator {

    /**
     * {@inheritDoc}
     */
    @Override
    public void mutateDocument(Document document) {
        handleImg(document);
    }

    /**
     * Changes src attribute of an absolute path to the filename only.
     *
     * @param document Document to process
     */
    private void handleImg(Document document) {
        NodeList images = document.getElementsByTagName("img");
        range(0, images.getLength())
            .mapToObj(images::item)
            .map(Element.class::cast)
            .forEach(img -> {
                String src = img.getAttribute("src");
                if (src.charAt(0) == '/') {
                    img.setAttribute("src", new File(src).getName());
                }
            });
    }
}
