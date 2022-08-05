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
package de.baedorf.tailoringexpert.renderer;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import de.baedorf.tailoringexpert.domain.Datei;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import static java.lang.String.format;
import static org.apache.pdfbox.io.MemoryUsageSetting.setupTempFileOnly;

/**
 * Erzeugung der PDF Datei aus HTML Content.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Log4j2
public class PDFEngine {

    @NonNull
    private String ersteller;

    /**
     * Hauptpfad im Dateisystem für die Auflösung von relativen Resourcen.
     */
    @NonNull
    @Getter
    private String baseUri;

    /**
     * Erzeugt ein PDF aus dem übergebenen HTML String.
     *
     * @param docId      Name der zu erstellenden PDF Datei
     * @param html       HTML für die Erzeugung der PDF Datei
     * @param pfadSuffix Pfad zum Verzeichnis unterhalb der definierten <strong>baseUri</strong>. Wird für die relative Referenzierung von Bildern benötigt.
     * @return Die erzeugte "PA" Datei
     */
    public Datei process(@NonNull String docId, @NonNull String html, @NonNull String pfadSuffix) {
        try (PDDocument document = new PDDocument(setupTempFileOnly())) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();

            addColorProfile(builder);

            builder
                .withHtmlContent(html, new File(format("%s/%s/", baseUri, pfadSuffix)).toURI().toString())
                .withProducer(ersteller)
                .usePDDocument(document)
                .toStream(os)
                .run();

            return Datei.builder()
                .docId(docId)
                .type("pdf")
                .bytes(os.toByteArray())
                .build();
        } catch (Exception e) {
            log.catching(e);
        }
        return null;
    }

    /**
     * Fügt das Farbprofil dem Builder hinzu.
     *
     * @param builder Builder, zu dem das Farbprofil hinzugefügt werden sollen
     * @return Der übergabene Builder
     */
    @SneakyThrows
    private PdfRendererBuilder addColorProfile(PdfRendererBuilder builder) {
        try (InputStream colorProfile = getClass().getResourceAsStream("/colorspaces/sRGB.icc")) {
            byte[] colorProfileBytes = IOUtils.toByteArray(colorProfile);
            builder.useColorProfile(colorProfileBytes);
            return builder;
        }
    }

}
