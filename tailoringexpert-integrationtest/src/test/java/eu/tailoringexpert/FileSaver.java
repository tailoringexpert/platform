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
package eu.tailoringexpert;

import lombok.extern.log4j.Log4j2;

import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import static java.nio.file.Files.newOutputStream;

@Log4j2
public class FileSaver implements BiConsumer<String, byte[]> {

    @Override
    public void accept(String dateiname, byte[] data) {
        Path path = Paths.get("target", dateiname);
        try (OutputStream out = newOutputStream(path)) {
            out.write(data);
        } catch (Exception e) {
            log.catching(e);
        }
    }
}
