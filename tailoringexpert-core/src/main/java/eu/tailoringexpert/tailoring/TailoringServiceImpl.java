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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.anforderung.AnforderungService;
import eu.tailoringexpert.domain.Datei;
import eu.tailoringexpert.domain.Dokument;
import eu.tailoringexpert.domain.DokumentZeichnung;
import eu.tailoringexpert.domain.Kapitel;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelektionsVektor;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.Tailoring.TailoringBuilder;
import eu.tailoringexpert.domain.TailoringAnforderung;
import eu.tailoringexpert.domain.TailoringInformation;
import eu.tailoringexpert.domain.TailoringStatus;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Log4j2
@RequiredArgsConstructor
public class TailoringServiceImpl implements TailoringService {

    @NonNull
    private TailoringServiceRepository repository;

    @NonNull
    private TailoringServiceMapper mapper;

    @NonNull
    private DokumentService dokumentService;

    @NonNull
    private AnforderungService anforderungService;

    @NonNull
    private Function<byte[], Map<String, Collection<ImportAnforderung>>> tailoringAnforderungFileReader;

    private static final String YES = "JA";
    private static final String NO = "NEIN";

    /**
     * {@inheritDoc}
     */
    @Override
    public Tailoring createTailoring(String name,
                                     String kennung,
                                     ScreeningSheet screeningSheet,
                                     SelektionsVektor anzuwendenderSelektionsVektor,
                                     Katalog<KatalogAnforderung> katalog) {
        Katalog<TailoringAnforderung> anforderungsKatalog = mapper.toTailoringKatalog(
            katalog, screeningSheet, anzuwendenderSelektionsVektor
        );

        TailoringBuilder result = Tailoring.builder()
            .name(name)
            .kennung(kennung)
            .screeningSheet(screeningSheet)
            .selektionsVektor(anzuwendenderSelektionsVektor)
            .katalog(anforderungsKatalog)
            .zeichnungen(repository.getDefaultZeichnungen())
            .status(TailoringStatus.AKTIV);

        // prüfe, ob phase(n) bereits vorhanden
        screeningSheet.getParameters()
            .stream()
            .filter(parameter -> "phase".equalsIgnoreCase(parameter.getBezeichnung()))
            .findFirst()
            .ifPresent(parameter -> result.phasen((Collection<Phase>) parameter.getWert()));


        return result.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Optional<Tailoring> addAnforderungDokument(String kuerzel, String tailoring, String dateiname, byte[] datei) {
        Optional<Tailoring> projektPhase = repository.getTailoring(kuerzel, tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        BigInteger hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(datei));
        Dokument dokument = Dokument.builder()
            .name(dateiname)
            .daten(datei)
            .hash(hash.toString(16))
            .build();
        return repository.updateAnforderungDokument(kuerzel, tailoring, dokument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Datei> createAnforderungDokument(String kuerzel, String tailoring) {
        @SuppressWarnings("PMD.PrematureDeclaration")
        final LocalDateTime erstellungsZeitpunkt = LocalDateTime.now();

        Optional<Tailoring> projektPhase = repository.getTailoring(kuerzel, tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        return dokumentService.createAnforderungDokument(projektPhase.get(), erstellungsZeitpunkt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Datei> createVergleichsDokument(String kuerzel, String tailoring) {
        @SuppressWarnings("PMD.PrematureDeclaration")
        final LocalDateTime erstellungsZeitpunkt = LocalDateTime.now();

        Optional<Tailoring> projektPhase = repository.getTailoring(kuerzel, tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        return dokumentService.createVergleichsDokument(projektPhase.get(), erstellungsZeitpunkt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Katalog<TailoringAnforderung>> getKatalog(@NonNull String projekt, @NonNull String tailoring) {
        Optional<Tailoring> projektPhase = repository.getTailoring(projekt, tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }
        return ofNullable(projektPhase.get().getKatalog());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<TailoringAnforderung>> getAnforderungen(@NonNull String projekt, @NonNull String tailoring, @NonNull String kapitel) {
        Optional<Kapitel<TailoringAnforderung>> oKapitel = getKapitel(projekt, tailoring, kapitel);
        if (oKapitel.isEmpty()) {
            return empty();
        }
        return ofNullable(oKapitel.get().getAnforderungen());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ScreeningSheet> getScreeningSheet(@NonNull String projekt, @NonNull String tailoring) {
        Optional<Tailoring> projektPhase = repository.getTailoring(projekt, tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        return ofNullable(projektPhase.get().getScreeningSheet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SelektionsVektor> getSelektionsVektor(@NonNull String projekt, @NonNull String tailoring) {
        Optional<Tailoring> projektPhase = repository.getTailoring(projekt, tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        return ofNullable(projektPhase.get().getSelektionsVektor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Kapitel<TailoringAnforderung>> getKapitel(@NonNull String projekt, @NonNull String tailoring, @NonNull String kapitel) {
        Optional<Tailoring> projektPhase = repository.getTailoring(projekt, tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        return projektPhase.get().getKatalog().getKapitel(kapitel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Collection<DokumentZeichnung>> getDokumentZeichnungen(@NonNull String projekt, @NonNull String tailoring) {
        Optional<Tailoring> projektPhase = repository.getTailoring(projekt, tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        return ofNullable(projektPhase.get().getZeichnungen());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DokumentZeichnung> updateDokumentZeichnung(@NonNull String projekt, @NonNull String tailoring, @NonNull DokumentZeichnung zeichnung) {
        return repository.updateDokumentZeichnung(projekt, tailoring, zeichnung);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringInformation> updateName(String projekt, String tailoring, @NonNull String name) {
        log.info("STARTED | updating name of {}:{} to {}", projekt, tailoring, name);

        // prüfe, ob es Phase mit neuem Namen bereits gibt
        if (tailoring.trim().equals(name.trim())) {
            log.info("FINISHED | name not changed because new name is empty");
            return empty();
        }

        Optional<Tailoring> phaseMitNeuemNamen = repository.getTailoring(projekt, name);
        if (phaseMitNeuemNamen.isPresent()) {
            log.info("FINISHED | name not changed because it already exits");
            return empty();
        }

        Optional<Tailoring> projektPhase = repository.updateName(projekt, tailoring, name);
        Optional<TailoringInformation> result = projektPhase.map(updatedPhase -> mapper.toTailoringInformation(updatedPhase));
        log.info("FINISHED | Phase name changed from {} to {}", tailoring, name);
        return result;
    }

    /**
     * @param projekt   Projekt, zum dem die Anforderung gehört
     * @param tailoring Phase des Projekts
     */
    @Override
    public void updateAusgewaehlteAnforderungen(@NonNull String projekt, @NonNull String tailoring, byte[] data) {
        log.info("STARTED | trying update requirement of {}:{} with provided file", projekt, tailoring);

        if (isNull(data) || data.length == 0) {
            log.info("FINISHED | update requirments with because of empty file");
            return;
        }

        Map<String, Collection<ImportAnforderung>> importAnforderungen = tailoringAnforderungFileReader.apply(data);
        importAnforderungen.entrySet().forEach(entry -> {
            String kapitel = entry.getKey();
            entry.getValue().forEach(anforderung -> {
                if (YES.equalsIgnoreCase(anforderung.getAnwendbar()) ||
                    NO.equalsIgnoreCase(anforderung.getAnwendbar())) {
                    boolean anwendbar = YES.equalsIgnoreCase(anforderung.getAnwendbar());
                    anforderungService.handleAusgewaehlt(projekt, tailoring, kapitel, anforderung.getPosition(), anwendbar);

                    if (nonNull(anforderung.getText()) && !anforderung.getText().trim().isEmpty()) {
                        anforderungService.handleText(projekt, tailoring, kapitel, anforderung.getPosition(), anforderung.getText());
                    }
                }
            });
        });
        log.info("FINISHED | update requirments");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> deleteTailoring(@NonNull String projekt, @NonNull String tailoring) {
        log.info("STARTED | trying to delete phase {} of project {}", tailoring, projekt);
        Optional<Tailoring> projektPhase = repository.getTailoring(projekt, tailoring);
        if (projektPhase.isEmpty()) {
            log.info("FINISHED | phase not existing. No deletion.");
            return empty();
        }

        Optional<Boolean> result = of(repository.deleteTailoring(projekt, tailoring));
        log.info("FINISHED | deleting phase {}.", result.get());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Optional<Datei> createDokumente(@NonNull String projekt, @NonNull String tailoring) {
        @SuppressWarnings("PMD.PrematureDeclaration")
        final LocalDateTime erstellungsZeitpunkt = LocalDateTime.now();

        Optional<Tailoring> projektPhase = repository.getTailoring(projekt, tailoring);
        if (projektPhase.isEmpty()) {
            return empty();
        }

        Collection<Datei> dokumente = dokumentService.createAll(projektPhase.get(), erstellungsZeitpunkt);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(os);
        dokumente.forEach(dokument -> addToZip(dokument, zip));
        zip.close();
        return of(Datei.builder()
            .docId(projekt + "-" + tailoring)
            .type("zip")
            .bytes(os.toByteArray())
            .build());
    }


    /**
     * Fügt eine Datei zum Zip hinzu.
     *
     * @param datei Hinzuzufügende Datei
     * @param zip   Zip, zu dem die Datei hinzugefügt werden soll
     */
    @SneakyThrows
    void addToZip(Datei datei, ZipOutputStream zip) {
        ZipEntry zipEntry = new ZipEntry(datei.getName());
        zip.putNextEntry(zipEntry);
        zip.write(datei.getBytes(), 0, datei.getBytes().length);
        zip.closeEntry();
    }
}
