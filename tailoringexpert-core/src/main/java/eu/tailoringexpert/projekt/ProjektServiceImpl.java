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
package eu.tailoringexpert.projekt;

import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.Projekt;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelektionsVektor;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.screeningsheet.ScreeningSheetService;
import eu.tailoringexpert.tailoring.TailoringService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Log4j2
@RequiredArgsConstructor
public class ProjektServiceImpl implements ProjektService {

    @NonNull
    private ProjektServiceRepository repository;

    @NonNull
    private ScreeningSheetService screeningSheetService;

    @NonNull
    private TailoringService tailoringService;

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateProjectTO createProjekt(String katalogVersion, byte[] screeningSheetData, SelektionsVektor anzuwendenderSelektionsVektor) {
        Katalog<KatalogAnforderung> katalog = repository.getKatalog(katalogVersion);
        ScreeningSheet screeningSheet = screeningSheetService.createScreeningSheet(screeningSheetData);
        Tailoring tailoring = tailoringService.createTailoring("master", "1000", screeningSheet, anzuwendenderSelektionsVektor, katalog);

        Projekt projekt = repository.createProjekt(katalogVersion, Projekt.builder()
            .screeningSheet(screeningSheet)
            .kuerzel(screeningSheet.getKuerzel())
            .tailoring(tailoring)
            .build()
        );

        log.info("Project {} with phases {} created with katalog {}", screeningSheet.getKuerzel(), tailoring.getPhasen(), katalogVersion);
        return CreateProjectTO.builder()
            .projekt(projekt.getKuerzel())
            .tailoring(tailoring.getName())
            .selektionsVektor(anzuwendenderSelektionsVektor)
            .build();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProjekt(String projekt) {
        Optional<Projekt> toDelete = repository.getProjekt(projekt);
        if (toDelete.isPresent()) {
            return repository.deleteProjekt(projekt);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> addTailoring(String projekt, String katalog, byte[] screeningSheetData, SelektionsVektor anzuwendenderSelektionsVektor) {
        log.info("STARTED  | adding tailoring to project {}", projekt);
        Optional<Projekt> oProjekt = repository.getProjekt(projekt);
        if (oProjekt.isEmpty()) {
            return empty();
        }

        Katalog<KatalogAnforderung> anwendbarerKatalog = repository.getKatalog(katalog);
        if (isNull(anwendbarerKatalog)) {
            log.error("ABORTED  | catalogue {} does not exist", katalog);
            return empty();
        }

        ScreeningSheet screeningSheet = screeningSheetService.createScreeningSheet(screeningSheetData);
        // nur hinzufüegen, wenn "richtiges" Projekt
        if (!projekt.equals(screeningSheet.getKuerzel())) {
            log.error("ABORTED  | screeningsheet defines phase of project {} instead of {}", screeningSheet.getKuerzel(), projekt);
            return empty();
        }

        Projekt addTo = oProjekt.get();
        StringBuilder phasenName = new StringBuilder("master");
        if (!addTo.getTailorings().isEmpty()) {
            phasenName.append(addTo.getTailorings().size());
        }

        Optional<String> kennung = oProjekt.get()
            .getTailorings()
            .stream()
            .map(p -> parseInt(p.getKennung()))
            .max(comparingInt(Integer::intValue))
            .map(max -> String.valueOf(max + 1));

        Tailoring tailoring = tailoringService.createTailoring(
            phasenName.toString(),
            kennung.orElse("1000"),
            screeningSheet,
            anzuwendenderSelektionsVektor,
            anwendbarerKatalog
        );

        Optional<Tailoring> result = repository.addTailoring(projekt, tailoring);
        log.info("FINISHED | adding phase {} to project {}", phasenName, projekt);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Projekt> copyProjekt(String projekt, byte[] screeningSheetData) {
        log.info("STARTED  | copyig project {}", projekt);
        Optional<Projekt> zuKopierendesProjekt = repository.getProjekt(projekt);
        if (zuKopierendesProjekt.isEmpty()) {
            log.info("Project does not exist. Aborting");
            return empty();
        }

        // annahme:
        // 1. alle phasen werden kopiert und das projekt screeningsheet gesetzt
        // 2. als screeningsheet pro phase wird das projekt screeningsheet gespeichert
        // 3. selektionvektor der phase wird aus kopierten projekt übernommen (zur info)
        // 4. KEIN neutailoring!

        Projekt projektKopie = zuKopierendesProjekt.get();

        ScreeningSheet screeningSheet = screeningSheetService.createScreeningSheet(screeningSheetData);
        projektKopie.setScreeningSheet(screeningSheet);
        projektKopie.setKuerzel(screeningSheet.getKuerzel());

        projektKopie.getTailorings()
            .forEach(projektPhase -> {
                log.debug("Copying tailoring {}", projektPhase.getName());
                projektPhase.setScreeningSheet(screeningSheet);
            });

        Optional<Projekt> result = of(repository.createProjekt(projektKopie));
        log.info("FINISHED | project {} copied to {}", projekt, screeningSheet.getKuerzel());
        return result;
    }
}
