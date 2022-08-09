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

import eu.tailoringexpert.domain.Anforderung;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.Referenz;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class KatalogWebServerPortConsumer<T extends Anforderung> implements Consumer<Katalog<T>> {

    @NonNull
    int port;

    @Override
    public void accept(Katalog<T> katalog) {
        katalog.alleKapitel()
            .forEach(kapitel -> kapitel.getAnforderungen()
                .forEach(anforderung -> {
                    String text = anforderung.getText();
                    if (text.contains("http://localhost/")) {
                        anforderung.setText(text.replace("http://localhost/", "http://localhost:" + port + "/"));
                    }

                    Referenz referenz = anforderung.getReferenz();
                    if (nonNull(referenz)) {
                        Logo logo = referenz.getLogo();
                        if (nonNull(logo)) {
                            logo.setUrl(logo.getUrl().replace("localhost/", "localhost:" + port + "/"));
                        }
                    }
                })
            );
    }
}
