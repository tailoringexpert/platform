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

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class TailoringexpertDOMMutatorTest {

    TailoringexpertDOMMutator mutator;

    @BeforeEach
    void beforeEach() {
        this.mutator = new TailoringexpertDOMMutator();
    }

    @Test
    void mutateDocument_ImgSrcStartsWithSlash_SrcFilenameSetted() throws Exception {
        // arrange
        Document document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        Element img = document.createElement("img");
        img.setAttribute("src", "/assets/unittest/filename.xml");
        document.appendChild(img);

        // act
        mutator.mutateDocument(document);

        // assert
        assertThat(img.getAttribute("src")).isEqualTo("filename.xml");
    }

    @Test
    void mutateDocument_ImgSrcNotStartsWithSlash_SrcFilenameUnchanged() throws Exception {
        // arrange
        Document document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        Element img = document.createElement("img");
        img.setAttribute("src", "assets/unittest/filename.xml");
        document.appendChild(img);

        // act
        mutator.mutateDocument(document);

        // assert
        assertThat(img.getAttribute("src")).isEqualTo("assets/unittest/filename.xml");
    }

    String toString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc),
            new StreamResult(new OutputStreamWriter(baos, "UTF-8")));

        return baos.toString(StandardCharsets.UTF_8);
    }

}
