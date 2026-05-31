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

import static java.util.Objects.isNull;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.w3c.dom.Element;

import com.openhtmltopdf.extend.FSObjectDrawer;
import com.openhtmltopdf.extend.OutputDevice;
import com.openhtmltopdf.render.RenderingContext;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class WatermarkDrawer implements FSObjectDrawer {

    private static Font font;

    @Override
    public Map<Shape, String> drawObject(Element e, double x, double y, double width, double height,
            OutputDevice outputDevice, RenderingContext ctx, int dotsPerPixel) {

        outputDevice.drawWithGraphics((float) x, (float) y, (float) width / dotsPerPixel,
                (float) height / dotsPerPixel, (Graphics2D g2d) -> {

                    double realWidth = width / dotsPerPixel;
                    double realHeight = height / dotsPerPixel;

                    g2d.setFont(getWatermarkFont());
                    g2d.setPaint(Color.RED);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

                    // 1. Text-Metriken für präzise Positionierung ermitteln
                    FontMetrics metrics = g2d.getFontMetrics(getWatermarkFont());

                    // 2. Zentrierte X-Koordinate berechnen
                    String text = e.getAttribute("text");
                    float textX = (float) ((realWidth - metrics.stringWidth(text)) / 2);

                    // 3. Korrekte Y-Koordinate unter Berücksichtigung der Baseline (Ascent)
                    // berechnen
                    float textY = (float) (((realHeight - metrics.getHeight()) / 2) + metrics.getAscent());

                    // 4. Text zeichnen
                    g2d.drawString(text, textX, textY);
                });

        return Map.of();
    }

    static synchronized Font getWatermarkFont() {
        if (isNull(font)) {
            try (InputStream in = WatermarkDrawer.class.getResourceAsStream("/fonts/noto_sans/NotoSans-Bold.ttf")) {
                Font parent = Font.createFont(Font.TRUETYPE_FONT, in);
                font = parent.deriveFont(36f);
            } catch (FontFormatException | IOException e) {
                throw new ExceptionInInitializerError("Failed to load watermark font: " + e.getMessage());
            }
        }
        return font;
    }
}
