package eu.tailoringexpert.tailoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Creates a PDF document containing text or applicability diferences between
 * two tailoring.
 * <p>
 * A HTML template named <code>tailoringdiffs.html</code> must be available in
 * the template folder of the tailoring version.
 * +
 * 
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class TailoringsDiffPDFDocumentCreator {

    @NonNull
    BiFunction<TailoringRequirement, TailoringRequirement, Optional<TailoringRequirementDiff>> diffProvider;

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    public File createDocument(Tailoring base,
            Tailoring compare, Map<String, Object> placeholders) {
        log.traceEntry(base.getCatalog().getVersion());
        Map<String, Object> parameter = new HashMap<>(placeholders);

        Map<String, List<TailoringRequirementDiff>> diffs = new LinkedHashMap<>();
        parameter.put("diffs", diffs);

        apply(base.getCatalog().getToc(), compare.getCatalog().getToc(), diffs);

        // dokument generieren
        String html = templateEngine.process(base.getCatalog().getVersion() + "/tailoringdiffs", parameter);
        File result = pdfEngine.process("docId", html, base.getCatalog().getVersion() + "/tailoringdiffs");

        log.traceExit();
        return result;
    }

    private void apply(Chapter<TailoringRequirement> base,
            Chapter<TailoringRequirement> compare,
            Map<String, List<TailoringRequirementDiff>> diffs) {
        diffs.computeIfAbsent(base.getNumber() + " " + base.getName(), key -> new ArrayList<>())
                .addAll(compare.getRequirements()
                        .stream()
                        .map(requirement -> base.getRequirement(requirement.getPosition())
                                .flatMap(baseReq -> diffProvider.apply(requirement, baseReq)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList());

        Optional.ofNullable(compare.getChapters())
                .ifPresent(subChapters -> subChapters.forEach(subChapter -> apply(
                        subChapter,
                        base.getChapter(subChapter.getNumber()),
                        diffs)));

        diffs.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

}
