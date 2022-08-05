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
package de.baedorf.tailoringexpert.anforderung;

import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung.TailoringAnforderungBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static java.lang.Boolean.TRUE;
import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;

@Log4j2
@RequiredArgsConstructor
public class AnforderungServiceImpl implements AnforderungService {

    @NonNull
    private AnforderungServiceRepository repository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringAnforderung> handleAusgewaehlt(String projekt, String tailoring, String kapitel, String position, Boolean ausgewaehlt) {
        log.info("STARTED  | trying to set selection state of requirements of {}:{}:{}.{} to {}", projekt, tailoring, kapitel, position, ausgewaehlt);
        Optional<TailoringAnforderung> projektAnforderung = repository.getAnforderung(projekt, tailoring, kapitel, position);
        if (projektAnforderung.isPresent()) {
            log.info(projektAnforderung.get().getAusgewaehlt() + ": neu {}", ausgewaehlt);
            if (!projektAnforderung.get().getAusgewaehlt().equals(ausgewaehlt)) {
                TailoringAnforderung anforderung = handleAusgewaehlt(projektAnforderung.get(), ausgewaehlt, ZonedDateTime.now());
                Optional<TailoringAnforderung> result = repository.updateAnforderung(projekt, tailoring, kapitel, anforderung);
                log.info("FINISHED | setting selection state of requirement {}:{}:{}.{} to {}", projekt, tailoring, kapitel, position, ausgewaehlt);
                return result;
            }
            log.info("FINISHED | no change in selection state of requirements of {}:{}:{}.{}", projekt, tailoring, kapitel, position);
            return projektAnforderung;
        }
        log.info("FINISHED | trying to set selection state of requirements of {}:{}:{}.{} to {}", projekt, tailoring, kapitel, position, ausgewaehlt);
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Kapitel<TailoringAnforderung>> handleAusgewaehlt(String projekt, String tailoring, String kapitel, Boolean ausgewaehlt) {
        log.info("STARTED  | trying to set selection state of requirements of {}:{}:{} to {}", projekt, tailoring, kapitel, ausgewaehlt);
        final ZonedDateTime now = ZonedDateTime.now();
        Optional<Kapitel<TailoringAnforderung>> anforderungGruppe = repository.getKapitel(projekt, tailoring, kapitel);
        if (anforderungGruppe.isPresent()) {
            anforderungGruppe.get().allKapitel()
                .sorted(comparing(Kapitel::getNummer))
                .forEachOrdered(gruppe -> {
                    gruppe.getAnforderungen().stream().sorted(comparing(TailoringAnforderung::getPosition));
                    handleAusgewaehlt(gruppe, ausgewaehlt, now);
                });
            log.info("FINISHED | selection state of requirements of {}:{}:{} set to {}", projekt, tailoring, kapitel, ausgewaehlt);
            return repository.updateAusgewaehlt(projekt, tailoring, anforderungGruppe.get());
        }
        log.info("FINISHED | no change in selection state of {}:{}:{}.{} due to not existing chapter", projekt, tailoring, kapitel);
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringAnforderung> handleText(String projekt, String tailoring, String kapitel, String position, String text) {
        log.info("STARTED  | trying to set text of requirements of {}:{}:{}.{} to {}", projekt, tailoring, kapitel, position, text);
        Optional<TailoringAnforderung> projektAnforderung = repository.getAnforderung(projekt, tailoring, kapitel, position);

        if (projektAnforderung.isEmpty()) {
            log.info("FINISHED |  no change in text of requirements of {}:{}:{}.{} due to not existing requirement", projekt, tailoring, kapitel, position);
            return empty();
        }

        TailoringAnforderung anforderung = projektAnforderung.get();
        if (!anforderung.getText().equals(text)) {
            anforderung.setText(text);
            if (nonNull(anforderung.getReferenz())) {
                anforderung.getReferenz().setGeaendert(true);
            }
            anforderung.setTextGeaendert(ZonedDateTime.now());
            return repository.updateAnforderung(projekt, tailoring, kapitel, anforderung);
        }
        log.info("FINISHED | text of requirements of {}:{}:{}.{} set to {}", projekt, tailoring, kapitel, position, projektAnforderung.get().getText());
        return projektAnforderung;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringAnforderung> createAnforderung(String projektId, String tailoring,
                                                            String kapitelId, String position,
                                                            String text) {
        log.info("STARTED  | trying to create requirement of {}:{}:{} after {} with text {}", projektId, tailoring, kapitelId, position, text);
        Optional<Kapitel<TailoringAnforderung>> kapitel = repository.getKapitel(projektId, tailoring, kapitelId);
        if (kapitel.isEmpty()) {
            return empty();
        }

        OptionalInt anforderungPosition = kapitel.get().indexOfAnforderung(position);
        if (anforderungPosition.isEmpty()) {
            return empty();
        }

        TailoringAnforderungBuilder builder = TailoringAnforderung.builder()
            .text(text)
            .ausgewaehlt(TRUE);
        if (isBenutzerAnforderung(position)) {
            int i = parseInt(position.substring(position.length() - 1)) + 1;
            builder.position(position.substring(0, position.length() - 1) + i);
        } else {
            builder.position(position + "1");
        }
        TailoringAnforderung toCreate = builder.build();

        List<TailoringAnforderung> anforderungen = kapitel.get().getAnforderungen();
        anforderungen.add(anforderungPosition.getAsInt() + 1, toCreate);

        // nachfolgende Positionen für neue Anforderungen anpassen
        anforderungen.stream().skip(anforderungPosition.getAsInt() + 2l)
            .takeWhile(this::isBenutzerAnforderung)
            .forEach(anforderung -> {
                int i = parseInt(anforderung.getPosition().substring(position.length())) + 1;
                if (isBenutzerAnforderung(position)) {
                    anforderung.setPosition(position.substring(0, position.length() - 1) + i);
                } else {
                    anforderung.setPosition(position + i);
                }
            });

        Optional<Kapitel<TailoringAnforderung>> updatedKapitel = repository.updateKapitel(projektId, tailoring, kapitel.get());
        if (updatedKapitel.isEmpty()) {
            return empty();
        }

        log.info("FINISHED | created new requirement  {}:{}:{}.{}", projektId, tailoring, kapitelId, toCreate.getPosition());
        return updatedKapitel.get().getAnforderung(toCreate.getPosition());
    }

    private Kapitel<TailoringAnforderung> handleAusgewaehlt(Kapitel<TailoringAnforderung> gruppe, Boolean ausgewaehlt, ZonedDateTime now) {
        gruppe.getAnforderungen()
            .stream()
            .sorted(comparing(TailoringAnforderung::getPosition))
            .forEachOrdered(anforderung -> handleAusgewaehlt(anforderung, ausgewaehlt, now));
        return gruppe;
    }

    private TailoringAnforderung handleAusgewaehlt(TailoringAnforderung anforderung, Boolean ausgewaehlt, ZonedDateTime now) {
        if (!anforderung.getAusgewaehlt().equals(ausgewaehlt)) {
            anforderung.setAusgewaehlt(ausgewaehlt);
            anforderung.setAusgewaehltGeaendert(isNull(anforderung.getAusgewaehltGeaendert()) ? now : null);
        }
        return anforderung;
    }

    private boolean isBenutzerAnforderung(String position) {
        return position.matches(".\\d+");
    }

    private boolean isBenutzerAnforderung(TailoringAnforderung anforderung) {
        return isBenutzerAnforderung(anforderung.getPosition());
    }
}
