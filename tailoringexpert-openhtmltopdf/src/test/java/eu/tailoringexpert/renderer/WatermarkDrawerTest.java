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
package eu.tailoringexpert.renderer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.w3c.dom.Element;

import com.openhtmltopdf.pdfboxout.PdfBoxFastOutputDevice;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester;
import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester.PdfCompareResult;
import com.openhtmltopdf.render.DefaultObjectDrawerFactory;
import com.openhtmltopdf.render.RenderingContext;

import lombok.extern.log4j.Log4j2;

@Log4j2
class WatermarkDrawerTest {

    WatermarkDrawer drawer;
    PdfRendererBuilder builder;

    @BeforeEach
    void beforeEach() {
        this.drawer = spy(WatermarkDrawer.class);
        DefaultObjectDrawerFactory drawerFactory = new DefaultObjectDrawerFactory();
        drawerFactory.registerDrawer("watermark", this.drawer);

        builder = new PdfRendererBuilder()
                .usePDDocument(new PDDocument())
                .useObjectDrawerFactory(drawerFactory)
                .testMode(true);
    }

    @AfterEach
    void afterEach() throws Exception {
        Field field = WatermarkDrawer.class.getDeclaredField("font");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    void getWatermarkFont_CreateFontIOException_ExceptionInInitializerErrorThrown() {
        // arrange

        // act
        Throwable actual;
        try (MockedStatic<Font> font = mockStatic(Font.class)) {
            font.when(() -> Font.createFont(anyInt(), any(InputStream.class)))
                    .thenThrow(new IOException("Failed to load watermark font"));
            actual = catchThrowable(WatermarkDrawer::getWatermarkFont);
        }

        // assert
        assertThat(actual)
                .isNotNull()
                .isInstanceOf(ExceptionInInitializerError.class)
                .hasMessage("Failed to load watermark font: Failed to load watermark font");
    }

    @Test
    void getWatermarkFont_CreateFontFontFormatException_ExceptionInInitializerErrorThrown() {
        // arrange

        // act
        Throwable actual;
        try (MockedStatic<Font> font = mockStatic(Font.class)) {
            font.when(() -> Font.createFont(anyInt(), any(InputStream.class)))
                    .thenThrow(new IOException("FontFormatException"));
            actual = catchThrowable(WatermarkDrawer::getWatermarkFont);
        }

        // assert
        assertThat(actual)
                .isNotNull()
                .isInstanceOf(ExceptionInInitializerError.class)
                .hasMessage("Failed to load watermark font: FontFormatException");
    }

    @Test
    void drawObject_TextGiven_CorrectFileGenerated() throws Exception {
        // arrange

        // act
        byte[] actual = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            builder
                    .withHtmlContent(
                            "<html><head><style>@page {size: a4 portrait;margin: 50px; }object[type=\"watermark\"] {position: fixed;display: block;width: 100%;height: 100%;transform: rotate(-45deg);z-index: 1000;left: 0; top: 0;}</style></head><body><object type=\"watermark\" text=\"ONLY FOR NORMING PURPOSES\"></object></body></html>",
                            null)
                    .toStream(os)
                    .run();
            actual = os.toByteArray();
        }

        // assert
        verify(this.drawer, times(1)).drawObject(any(Element.class), eq(1.0d), eq(1.0d),
                eq(13874.0d), eq(20450.0d), any(PdfBoxFastOutputDevice.class), any(RenderingContext.class), eq(20));

        byte[] expected = WatermarkDrawerTest.class.getResourceAsStream("/watermark.pdf").readAllBytes();
        List<PdfCompareResult> problems = PdfVisualTester.comparePdfDocuments(expected, actual,
                "drawObject_TextGiven_CorrectFileGenerated", false);
        assertThat(problems).isEmpty();

    }
}
