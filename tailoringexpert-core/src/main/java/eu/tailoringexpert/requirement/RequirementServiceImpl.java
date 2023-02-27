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
import java.util.Scanner;

import static java.lang.Boolean.TRUE;
import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;

/**
 * Implementation of {@link RequirementService}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class RequirementServiceImpl implements RequirementService {

    @NonNull
    private RequirementServiceRepository repository;

    @NonNull
    private RequirementModifiablePredicate modifiablePredicate;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringRequirement> handleSelected(String project, String tailoring, String chapter, String position, Boolean selected) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter, () -> position, () -> selected);

        if (!modifiablePredicate.test(project, tailoring)) {
            log.error("not updated requirement because of invalid/non given tailoring state");
            log.traceExit();
            return empty();
        }

        Optional<TailoringRequirement> tailoringRequirement = repository.getRequirement(project, tailoring, chapter, position);
        if (tailoringRequirement.isPresent()) {
            log.info(tailoringRequirement.get().getSelected() + ": neu {}", selected);
            if (!tailoringRequirement.get().getSelected().equals(selected)) {
                TailoringRequirement requirement = handleSelected(tailoringRequirement.get(), selected, ZonedDateTime.now());
                Optional<TailoringRequirement> result = repository.updateRequirement(project, tailoring, chapter, requirement);
                log.info("selection state changed to {}", selected);
                log.traceExit();
                return result;
            }
            log.info("no change in selection state");
            log.traceExit();
            return tailoringRequirement;
        }
        log.traceExit();
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Chapter<TailoringRequirement>> handleSelected(String project, String tailoring, String chapter, Boolean selected) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter, () -> selected);

        if (!modifiablePredicate.test(project, tailoring)) {
            log.error("not updated requirement because of invalid/non given tailoring state");
            log.traceExit();
            return empty();
        }

        final ZonedDateTime now = ZonedDateTime.now();
        Optional<Chapter<TailoringRequirement>> tailoringChapter = repository.getChapter(project, tailoring, chapter);
        if (tailoringChapter.isPresent()) {
            tailoringChapter.get().allChapters()
                .sorted(comparing(Chapter::getNumber))
                .forEachOrdered(subChapter -> {
                    subChapter.getRequirements().stream().sorted(comparing(TailoringRequirement::getPosition));
                    handleSelected(subChapter, selected, now);
                });
            log.info("selection state of requirements of {}:{}:{} set to {}", project, tailoring, chapter, selected);
            log.traceExit();
            return repository.updateSelected(project, tailoring, tailoringChapter.get());
        }

        log.traceExit();
        return empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringRequirement> handleText(String project, String tailoring, String chapter, String position, String text) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter, () -> position, () -> text);

        if (!modifiablePredicate.test(project, tailoring)) {
            log.error("requirement not updated because of invalid/non given tailoring state");
            log.traceExit();
            return empty();
        }

        Optional<TailoringRequirement> requirement = repository.getRequirement(project, tailoring, chapter, position);

        if (requirement.isEmpty()) {
            log.error("no change in text of requirements");
            log.traceExit();
            return empty();
        }

        TailoringRequirement tailoringRequirement = requirement.get();
        if (!tailoringRequirement.getText().equals(text)) {
            tailoringRequirement.setText(text);
            if (nonNull(tailoringRequirement.getReference())) {
                tailoringRequirement.getReference().setChanged(true);
            }
            tailoringRequirement.setTextChanged(ZonedDateTime.now());
            log.traceExit();
            return repository.updateRequirement(project, tailoring, chapter, tailoringRequirement);
        }

        log.traceExit();
        return requirement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringRequirement> createRequirement(String project, String tailoring,
                                                            String chapter, String position,
                                                            String text) {
        log.traceEntry(() -> project, () -> tailoring, () -> chapter, () -> position, () -> text);

        if (!modifiablePredicate.test(project, tailoring)) {
            log.error("requirement not updated requirement because of invalid/non given tailoring state");
            log.traceExit();
            return empty();
        }

        Optional<Chapter<TailoringRequirement>> oChapter = repository.getChapter(project, tailoring, chapter);
        if (oChapter.isEmpty()) {
            log.traceExit();
            return empty();
        }

        OptionalInt requirementPosition = oChapter.get().indexOfRequirement(position);
        if (requirementPosition.isEmpty()) {
            log.traceExit();
            return empty();
        }

        TailoringRequirementBuilder builder = TailoringRequirement.builder()
            .text(text)
            .selected(TRUE);
        if (isCustomRequirement(position)) {
            int i = parseInt(position.substring(position.length() - 1)) + 1;
            builder.position(position.substring(0, position.length() - 1) + i);
        } else {
            builder.position(position + "1");
        }
        TailoringRequirement toCreate = builder.build();

        List<TailoringRequirement> requirements = oChapter.get().getRequirements();
        requirements.add(requirementPosition.getAsInt() + 1, toCreate);

        // nachfolgende Positionen für neue Anforderungen anpassen
        requirements.stream().skip(requirementPosition.getAsInt() + 2l)
            .takeWhile(this::isCustomRequirement)
            .forEach(requirement -> {
                int i = new Scanner(requirement.getPosition()).useDelimiter("\\D+").nextInt() + 1;
                if (isCustomRequirement(position)) {
                    requirement.setPosition(position.substring(0, position.length() - 1) + i);
                } else {
                    requirement.setPosition(position + i);
                }
            });

        Optional<Chapter<TailoringRequirement>> updateChapter = repository.updateChapter(project, tailoring, oChapter.get());
        if (updateChapter.isEmpty()) {
            log.traceExit();
            return empty();
        }

        log.traceExit();
        return updateChapter.get().getRequirement(toCreate.getPosition());
    }

    private Chapter<TailoringRequirement> handleSelected(Chapter<TailoringRequirement> chapter, Boolean selected, ZonedDateTime now) {
        chapter.getRequirements()
            .stream()
            .sorted(comparing(TailoringRequirement::getPosition))
            .forEachOrdered(requirement -> handleSelected(requirement, selected, now));
        return chapter;
    }

    private TailoringRequirement handleSelected(TailoringRequirement requirement, Boolean selected, ZonedDateTime now) {
        if (!requirement.getSelected().equals(selected)) {
            requirement.setSelected(selected);
            requirement.setSelectionChanged(isNull(requirement.getSelectionChanged()) ? now : null);
        }
        return requirement;
    }

    private boolean isCustomRequirement(String position) {
        return position.matches(".\\d+");
    }

    private boolean isCustomRequirement(TailoringRequirement requirment) {
        return isCustomRequirement(requirment.getPosition());
    }
}
