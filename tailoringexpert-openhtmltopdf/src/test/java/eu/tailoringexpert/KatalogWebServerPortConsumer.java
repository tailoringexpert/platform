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

import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Requirement;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.Reference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class KatalogWebServerPortConsumer<T extends Requirement> implements Consumer<Catalog<T>> {

    @NonNull
    int port;

    @Override
    public void accept(Catalog<T> catalog) {
        catalog.allChapters()
            .forEach(chapter -> chapter.getRequirements()
                .forEach(requirement -> {
                    String text = requirement.getText();
                    if (text.contains("http://localhost/")) {
                        requirement.setText(text.replace("http://localhost/", "http://localhost:" + port + "/"));
                    }

                    Reference reference = requirement.getReference();
                    if (nonNull(reference)) {
                        Logo logo = reference.getLogo();
                        if (nonNull(logo)) {
                            logo.setUrl(logo.getUrl().replace("localhost/", "localhost:" + port + "/"));
                        }
                    }
                })
            );
    }
}
