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
package eu.tailoringexpert.renderer;

import com.openhtmltopdf.extend.FSDOMMutator;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import eu.tailoringexpert.domain.File;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static java.lang.String.format;

/**
 * Engine for creating PDF output of HTML input.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Log4j2
public class PDFEngine {

    @NonNull FSDOMMutator domMutator;

    @NonNull
    private RendererRequestConfigurationSupplier requestConfigurationSupplier;

    /**
     * Creates PDF using provided HTML String.
     *
     * @param docId      docid to use in PDF file
     * @param html       HTML to use as input
     * @param pathSuffix Path relarive to defined <strong>baseUri</strong>. Will be used for relative addressing of images
     * @return Die erzeugte "PA" File
     */
    public File process(@NonNull String docId, @NonNull String html, @NonNull String pathSuffix) {
        log.traceEntry(() -> docId);

        try (PDDocument document = new PDDocument()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();

            addColorProfile(builder);

            RendererRequestConfiguration configuration = requestConfigurationSupplier.get();
            String baseUri = new java.io.File(
                format("%s/%s/%s",
                    configuration.getTemplateHome(),
                    pathSuffix,
                    "non-existing-base-uri-file.html")).toURI()
                .toString();
            log.info("using baseuri: " + baseUri);
            builder
                .withUri(baseUri)
                .withHtmlContent(html, baseUri)
                .addDOMMutator(domMutator)
                .withProducer(configuration.getName())
                .usePDDocument(document)
                .toStream(os)
                .run();

            File result = File.builder()
                .name(docId + ".pdf")
                .data(os.toByteArray())
                .build();

            log.traceExit();
            return result;
        } catch (Exception e) {
            log.catching(e);
        }

        log.traceExit();
        return null;
    }

    /**
     * Add colorprofile to builder.
     *
     * @param builder builder to add profile to
     * @return provided builder
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
