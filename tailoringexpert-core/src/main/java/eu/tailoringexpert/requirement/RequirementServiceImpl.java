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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringRequirement.TailoringRequirementBuilder;
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
public class RequirementServiceImpl implements RequirementService {

    @NonNull
    private RequirementServiceRepository repository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringRequirement> handleSelected(String project, String tailoring, String chapter, String position, Boolean selected) {
        log.info("STARTED  | trying to set selection state of requirements of {}:{}:{}.{} to {}", project, tailoring, chapter, position, selected);
        Optional<TailoringRequirement> projektAnforderung = repository.getRequirement(project, tailoring, chapter, position);
        if (projektAnforderung.isPresent()) {
            log.info(projektAnforderung.get().getSelected() + ": neu {}", selected);
            if (!projektAnforderung.get().getSelected().equals(selected)) {
                TailoringRequirement anforderung = handleSelected(projektAnforderung.get(), selected, ZonedDateTime.now());
                Optional<TailoringRequirement> result = repository.updateRequirement(project, tailoring, chapter, anforderung);
                log.info("FINISHED | setting selection state of requirement {}:{}:{}.{} to {}", project, tailoring, chapter, position, selected);
                return result;
            }
            log.info("FINISHED | no change in selection state of requirements of {}:{}:{}.{}", project, tailoring, chapter, position);
            return projektAnforderung;
        }
        log.info("FINISHED | trying to set selection state of requirements of {}:{}:{}.{} to {}", project, tailoring, chapter, position, selected);
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> handleSelected(String project, String tailoring, String chapter, Boolean selected) {
        log.info("STARTED  | trying to set selection state of requirements of {}:{}:{} to {}", project, tailoring, chapter, selected);
        final ZonedDateTime now = ZonedDateTime.now();
        Optional<Chapter<TailoringRequirement>> anforderungGruppe = repository.getChapter(project, tailoring, chapter);
        if (anforderungGruppe.isPresent()) {
            anforderungGruppe.get().allChapters()
                .sorted(comparing(Chapter::getNumber))
                .forEachOrdered(gruppe -> {
                    gruppe.getRequirements().stream().sorted(comparing(TailoringRequirement::getPosition));
                    handleSelected(gruppe, selected, now);
                });
            log.info("FINISHED | selection state of requirements of {}:{}:{} set to {}", project, tailoring, chapter, selected);
            return repository.updateSelected(project, tailoring, anforderungGruppe.get());
        }
        log.info("FINISHED | no change in selection state of {}:{}:{}.{} due to not existing chapter", project, tailoring, chapter);
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringRequirement> handleText(String project, String tailoring, String chapter, String position, String text) {
        log.info("STARTED  | trying to set text of requirements of {}:{}:{}.{} to {}", project, tailoring, chapter, position, text);
        Optional<TailoringRequirement> projektAnforderung = repository.getRequirement(project, tailoring, chapter, position);

        if (projektAnforderung.isEmpty()) {
            log.info("FINISHED |  no change in text of requirements of {}:{}:{}.{} due to not existing requirement", project, tailoring, chapter, position);
            return empty();
        }

        TailoringRequirement anforderung = projektAnforderung.get();
        if (!anforderung.getText().equals(text)) {
            anforderung.setText(text);
            if (nonNull(anforderung.getReference())) {
                anforderung.getReference().setChanged(true);
            }
            anforderung.setTextChanged(ZonedDateTime.now());
            return repository.updateRequirement(project, tailoring, chapter, anforderung);
        }
        log.info("FINISHED | text of requirements of {}:{}:{}.{} set to {}", project, tailoring, chapter, position, projektAnforderung.get().getText());
        return projektAnforderung;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringRequirement> createRequirement(String project, String tailoring,
                                                            String chapter, String position,
                                                            String text) {
        log.info("STARTED  | trying to create requirement of {}:{}:{} after {} with text {}", project, tailoring, chapter, position, text);
        Optional<Chapter<TailoringRequirement>> kapitel = repository.getChapter(project, tailoring, chapter);
        if (kapitel.isEmpty()) {
            return empty();
        }

        OptionalInt anforderungPosition = kapitel.get().indexOfRequirement(position);
        if (anforderungPosition.isEmpty()) {
            return empty();
        }

        TailoringRequirementBuilder builder = TailoringRequirement.builder()
            .text(text)
            .selected(TRUE);
        if (isBenutzerAnforderung(position)) {
            int i = parseInt(position.substring(position.length() - 1)) + 1;
            builder.position(position.substring(0, position.length() - 1) + i);
        } else {
            builder.position(position + "1");
        }
        TailoringRequirement toCreate = builder.build();

        List<TailoringRequirement> anforderungen = kapitel.get().getRequirements();
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

        Optional<Chapter<TailoringRequirement>> updatedKapitel = repository.updateChapter(project, tailoring, kapitel.get());
        if (updatedKapitel.isEmpty()) {
            return empty();
        }

        log.info("FINISHED | created new requirement  {}:{}:{}.{}", project, tailoring, chapter, toCreate.getPosition());
        return updatedKapitel.get().getRequirement(toCreate.getPosition());
    }

    private Chapter<TailoringRequirement> handleSelected(Chapter<TailoringRequirement> gruppe, Boolean ausgewaehlt, ZonedDateTime now) {
        gruppe.getRequirements()
            .stream()
            .sorted(comparing(TailoringRequirement::getPosition))
            .forEachOrdered(anforderung -> handleSelected(anforderung, ausgewaehlt, now));
        return gruppe;
    }

    private TailoringRequirement handleSelected(TailoringRequirement anforderung, Boolean ausgewaehlt, ZonedDateTime now) {
        if (!anforderung.getSelected().equals(ausgewaehlt)) {
            anforderung.setSelected(ausgewaehlt);
            anforderung.setSelectionChanged(isNull(anforderung.getSelectionChanged()) ? now : null);
        }
        return anforderung;
    }

    private boolean isBenutzerAnforderung(String position) {
        return position.matches(".\\d+");
    }

    private boolean isBenutzerAnforderung(TailoringRequirement anforderung) {
        return isBenutzerAnforderung(anforderung.getPosition());
    }
}
