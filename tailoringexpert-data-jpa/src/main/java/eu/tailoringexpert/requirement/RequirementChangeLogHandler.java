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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.domain.TailoringRequirementChangeEntity;
import eu.tailoringexpert.domain.TailoringRequirementEntity;
import eu.tailoringexpert.repository.TailoringRequirementChangeRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Handler for logging text and applicibility changes of a tailoring requirement {@link TailoringRequirementEntity}.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
public class RequirementChangeLogHandler implements BiConsumer<TailoringRequirementEntity, TailoringRequirementEntity> {

    private static final String CHANGETYPE_APPLICABILITY = "SELECTED";
    private static final String CHANGETYPE_TEXT = "TEXT";

    @NonNull
    Supplier<String> username;

    @NonNull
    TailoringRequirementChangeRepository repository;

    @Override
    public void accept(TailoringRequirementEntity original, TailoringRequirementEntity revised) {
        if (!original.getSelected().equals(revised.getSelected())) {
            repository.save(
                TailoringRequirementChangeEntity.builder()
                    .changeType(CHANGETYPE_APPLICABILITY)
                    .user(username.get())
                    .requirementId(original.getId())
                    .old(String.valueOf(original.getSelected()))
                    .changed(String.valueOf(revised.getSelected()))
                    .modificationTimestamp(ZonedDateTime.now())
                    .build()
            );
        }

        if (!original.getText().equals(revised.getText())) {
            repository.save(
                TailoringRequirementChangeEntity.builder()
                    .changeType(CHANGETYPE_TEXT)
                    .user(username.get())
                    .requirementId(original.getId())
                    .old(original.getText())
                    .changed(revised.getText())
                    .modificationTimestamp(ZonedDateTime.now())
                    .build()
            );
        }
    }
}
