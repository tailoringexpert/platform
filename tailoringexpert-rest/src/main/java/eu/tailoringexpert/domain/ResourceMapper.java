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
package eu.tailoringexpert.domain;

import eu.tailoringexpert.domain.DokumentResource.DokumentResourceBuilder;
import eu.tailoringexpert.domain.DokumentZeichnungResource.DokumentZeichnungResourceBuilder;
import eu.tailoringexpert.domain.KatalogResource.KatalogResourceBuilder;
import eu.tailoringexpert.domain.KatalogVersionResource.KatalogVersionResourceBuilder;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.ProjektInformationResource.ProjektInformationResourceBuilder;
import eu.tailoringexpert.domain.ProjektResource.ProjektResourceBuilder;
import eu.tailoringexpert.domain.ScreeningSheetResource.ScreeningSheetResourceBuilder;
import eu.tailoringexpert.domain.SelektionsVektorResource.SelektionsVektorResourceBuilder;
import eu.tailoringexpert.domain.TailoringAnforderungResource.TailoringAnforderungResourceBuilder;
import eu.tailoringexpert.domain.TailoringInformationResource.TailoringInformationResourceBuilder;
import eu.tailoringexpert.domain.TailoringKatalogKapitelResource.TailoringKatalogKapitelResourceBuilder;
import eu.tailoringexpert.domain.TailoringResource.TailoringResourceBuilder;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.springframework.hateoas.server.mvc.BasicLinkBuilder.linkToCurrentMapping;

@Mapper
public abstract class ResourceMapper {

    // Resource URLs
    public static final String PROJEKTE = "projekt";
    public static final String PROJEKT_NEU = "katalog/{version}/projekt";
    public static final String PROJEKT = "projekt/{projekt}";
    public static final String PROJEKTSELEKTIONSVEKTOR = "projekt/{projekt}/selektionsvektor";
    public static final String PROJEKTSCREENINGSHEET = "projekt/{projekt}/screeningsheet";
    public static final String PROJEKTSCREENINGSHEETDATEI = "projekt/{projekt}/screeningsheet/pdf";
    public static final String ANFORDERUNG = "projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}";
    public static final String ANFORDERUNG_AUSGEWAEHLT = "projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}/ausgewaehlt/{ausgewaehlt}";
    public static final String ANFORDERUNG_TEXT = "projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}/text";
    public static final String KAPITEL_AUSGEWAEHLT = "projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/ausgewaehlt/{ausgewaehlt}";

    public static final String TAILORINGS = "projekt/{projekt}/tailoring";
    public static final String TAILORING = "projekt/{projekt}/tailoring/{tailoring}";
    public static final String TAILORINGSCREENINGSHEET = "projekt/{projekt}/tailoring/{tailoring}/screeningsheet";
    public static final String TAILORINGANFORDERUNG = "projekt/{projekt}/tailoring/{tailoring}/anforderungen/import";
    public static final String TAILORINGSCREENINGSHEETDATEI = "projekt/{projekt}/tailoring/{tailoring}/screeningsheet/pdf";
    public static final String TAILORINGSELEKTIONSVEKTOR = "projekt/{projekt}/tailoring/{tailoring}/selektionsvektor";
    public static final String TAILORINGNAME = "projekt/{projekt}/tailoring/{tailoring}/name";
    public static final String TAILORINGZEICHNUNG = "projekt/{projekt}/tailoring/{tailoring}/zeichnung";
    public static final String TAILORINGZEICHNUNGBEREICH = "projekt/{projekt}/tailoring/{tailoring}/zeichnung/{bereich}";
    public static final String TAILORINGDOKUMENT = "projekt/{projekt}/tailoring/{tailoring}/dokument";
    public static final String TAILORINGDOKUMENTKATALOG = "projekt/{projekt}/tailoring/{tailoring}/dokument/katalog";
    public static final String TAILORINGVERGLEICHDOKUMENT = "projekt/{projekt}/tailoring/{tailoring}/dokument/vergleich";
    public static final String TAILORINGDOKUMENTDOWNLOAD = "projekt/{projekt}/tailoring/{tailoring}/dokument/{name}";
    public static final String TAILORINGKATALOG = "projekt/{projekt}/tailoring/{tailoring}/katalog";
    public static final String TAILORINGKATALOGKAPITEL = "projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}";
    public static final String TAILORINGKATALOGKAPITELANFORDERUG = "projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/anforderung";
    public static final String KATALOG = "katalog";
    public static final String KATALOGVERSION = "katalog/{version}";
    public static final String KATALOGVERSIONPDFDOWNLOAD = "katalog/{version}/pdf";
    public static final String KATALOGVERSIONJSONDOWNLOAD = "katalog/{version}/json";

    public static final String SCREENINGSHEET = "screeningsheet";
    public static final String SELEKTIONSVEKTORPROFILE = "selektionsvektor";

    // RELs
    public static final String REL_SELF = "self";
    public static final String REL_SCREENINGSHEET = "screeningsheet";
    public static final String REL_SELEKTIONSVEKTOR = "selektionsvektor";
    public static final String REL_TAILORING = "tailoring";
    public static final String REL_KATALOG = "katalog";
    public static final String REL_PDF = "pdf";
    public static final String REL_JSON = "json";
    public static final String REL_KATALOGDOKUMENT = "katalogdokument";
    public static final String REL_KATALOGDEFINITIONDOKUMENT = "katalogdefinitiondokument";
    public static final String REL_ZEICHNUNG = "zeichnung";
    public static final String REL_KAPITEL = "kapitel";
    public static final String REL_TEXT = "text";
    public static final String REL_AUSGEWAEHLT = "ausgewaehlt";
    public static final String REL_DOKUMENT = "dokument";
    public static final String REL_VERGLEICH = "vergleich";
    private static final String REL_NAME = "name";
    private static final String REL_IMPORT = "import";


    // Katalogversion
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, KatalogVersion domain) {
        pathContext.katalog(nonNull(domain) ? domain.getVersion() : null);
    }

    @Mapping(target = "standard", expression = "java( domain.getGueltigBis() == null)")
    public abstract KatalogVersionResource toResource(@Context PathContextBuilder pathContext, KatalogVersion domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget KatalogVersionResourceBuilder resource) {
        PathContext context = pathContext.build();
        String baseUri = linkToCurrentMapping().toString();
        Map<String, String> parameter = context.parameter();
        resource.links(asList(
            linkToCurrentMapping().slash(resolveParameter(PROJEKT_NEU, context.parameter())).withRel(PROJEKTE),
            createLink(REL_SELF, baseUri, KATALOGVERSION, parameter),
            createLink(REL_PDF, baseUri, KATALOGVERSIONPDFDOWNLOAD, parameter),
            createLink(REL_JSON, baseUri, KATALOGVERSIONJSONDOWNLOAD, parameter)
        ));
    }

    // ProjektInformation
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, ProjektInformation domain) {
        pathContext.projekt(nonNull(domain) ? domain.getKuerzel() : null);
    }

    @Mapping(target = "name", source = "kuerzel")
    @Mapping(target = "erstellungsZeitpunkt", dateFormat = "dd.MM.yyyy")
    public abstract ProjektInformationResource toResource(@Context PathContextBuilder pathContext, ProjektInformation domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget ProjektInformationResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, PROJEKT, parameter),
            createLink(REL_SELEKTIONSVEKTOR, baseUri, PROJEKTSELEKTIONSVEKTOR, parameter),
            createLink(REL_SCREENINGSHEET, baseUri, PROJEKTSCREENINGSHEET, parameter),
            createLink(REL_TAILORING, baseUri, TAILORINGS, parameter))
        );
    }

    // TailoringInformation
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, TailoringInformation domain) {
        pathContext.tailoring(nonNull(domain) ? domain.getName() : null);
        pathContext.katalog(nonNull(domain) ? domain.getKatalogVersion() : null);
    }

    public abstract TailoringInformationResource toResource(@Context PathContextBuilder pathContext, TailoringInformation domain);

    @AfterMapping
    public void addLinks(@Context PathContextBuilder pathContext, @MappingTarget TailoringInformationResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORING, parameter),
            createLink(REL_SCREENINGSHEET, baseUri, TAILORINGSCREENINGSHEET, parameter),
            createLink(REL_SELEKTIONSVEKTOR, baseUri, TAILORINGSELEKTIONSVEKTOR, parameter),
            createLink(REL_ZEICHNUNG, baseUri, TAILORINGZEICHNUNG, parameter),
            createLink(REL_DOKUMENT, baseUri, TAILORINGDOKUMENT, parameter),
            createLink(REL_KATALOGDOKUMENT, baseUri, TAILORINGDOKUMENTKATALOG, parameter),
            createLink(REL_VERGLEICH, baseUri, TAILORINGVERGLEICHDOKUMENT, parameter),
            createLink(REL_KATALOG, baseUri, TAILORINGKATALOG, parameter),
            createLink(REL_NAME, baseUri, TAILORINGNAME, parameter),
            createLink(REL_IMPORT, baseUri, TAILORINGANFORDERUNG, parameter),
            createLink(REL_KATALOGDEFINITIONDOKUMENT, baseUri, KATALOGVERSIONPDFDOWNLOAD, parameter))
        );
    }

    // ScreeningSheet
    public abstract ScreeningSheetResource toResource(@Context PathContextBuilder builder, ScreeningSheet screeningSheet);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget ScreeningSheetResourceBuilder resource) {
        PathContext context = pathContext.build();
        if (nonNull(context.getProjekt()) || nonNull(context.getTailoring())) {
            String self = isNull(context.getTailoring()) ? PROJEKTSCREENINGSHEET : TAILORINGSCREENINGSHEET;
            String datei = isNull(context.getTailoring()) ? PROJEKTSCREENINGSHEETDATEI : TAILORINGSCREENINGSHEETDATEI;

            Map<String, String> parameter = context.parameter();
            String baseUri = linkToCurrentMapping().toString();
            resource.links(asList(
                createLink(REL_SELF, baseUri, self, parameter),
                createLink("datei", baseUri, datei, parameter))
            );
        }
    }

    @SuppressWarnings({"java:S1172"})
    public List<ScreeningSheetParameterResource> toResource(@Context PathContextBuilder builder,
                                                            List<ScreeningSheetParameter> parameters) {
        if (isNull(parameters)) {
            return null;
        }

        return parameters.stream()
            .collect(Collectors.groupingBy(ScreeningSheetParameter::getBezeichnung))
            .entrySet()
            .stream()
            .map(entry -> ScreeningSheetParameterResource.builder()
                .bezeichnung(entry.getKey())
                .wert(entry.getValue()
                    .stream()
                    .map(ScreeningSheetParameter::getWert)
                    .map(Objects::toString)
                    .collect(joining("; ")))
                .build()
            )
            .collect(Collectors.toList());
    }

    // Projekt
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, Projekt domain) {
        pathContext.projekt(nonNull(domain) ? domain.getKuerzel() : null);
    }

    @Mapping(target = "name", source = "kuerzel")
    public abstract ProjektResource toResource(@Context PathContextBuilder pathContext, Projekt domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget ProjektResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, PROJEKT, parameter),
            createLink("copy", baseUri, PROJEKT, parameter))
        );
    }

    // Tailoring
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, Tailoring domain) {
        pathContext.tailoring(nonNull(domain) ? domain.getName() : null);
    }

    public abstract TailoringResource toResource(@Context PathContextBuilder pathContext, Tailoring domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget TailoringResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORING, parameter),
            createLink(REL_SCREENINGSHEET, baseUri, TAILORINGSCREENINGSHEET, parameter),
            createLink(REL_SELEKTIONSVEKTOR, baseUri, TAILORINGSELEKTIONSVEKTOR, parameter),
            createLink(REL_ZEICHNUNG, baseUri, TAILORINGZEICHNUNG, parameter),
            createLink(REL_DOKUMENT, baseUri, TAILORINGDOKUMENT, parameter),
            createLink(REL_KATALOG, baseUri, TAILORINGKATALOG, parameter),
            createLink(REL_NAME, baseUri, TAILORINGNAME, parameter))
        );
    }

    // TailoringKatalogKapitel
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, Kapitel<TailoringAnforderung> domain) {
        if (nonNull(domain)) {
            pathContext.kapitel(domain.getNummer());
            pathContext.ausgewaehlt(null);
        }
    }

    public abstract TailoringKatalogKapitelResource toResource(@Context PathContextBuilder pathContext, Kapitel<TailoringAnforderung> domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget TailoringKatalogKapitelResourceBuilder resource) {
        PathContext context = pathContext.build();
        pathContext.kapitel(resource.build().getNummer());
        Map<String, String> parameter = context.parameter();
        parameter.put(REL_AUSGEWAEHLT, null);
        parameter.put(REL_KAPITEL, resource.build().getNummer());

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORINGKATALOGKAPITEL, parameter),
            createLink("anforderungen", baseUri, TAILORINGKATALOGKAPITELANFORDERUG, parameter),
            createLink("selektion", baseUri, KAPITEL_AUSGEWAEHLT, parameter))
        );
    }


    // TailoringAnforderung
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, TailoringAnforderung domain) {
        if (nonNull(domain)) {
            pathContext.anforderung(domain.getPosition());
            pathContext.ausgewaehlt(!domain.getAusgewaehlt());
        }
    }


    @Mapping(target = "geaendert", expression = "java( domain.getAusgewaehltGeaendert() != null || domain.getTextGeaendert() != null)")
    @Mapping(target = "referenz", source = "domain.referenz.text")
    public abstract TailoringAnforderungResource toResource(@Context PathContextBuilder pathContext, TailoringAnforderung domain);


    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget TailoringAnforderungResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, ANFORDERUNG, parameter),
            createLink(REL_AUSGEWAEHLT, baseUri, ANFORDERUNG_AUSGEWAEHLT, parameter),
            createLink(REL_TEXT, baseUri, ANFORDERUNG_TEXT, parameter))
        );
    }

    // DokumentZeichnung
    public abstract DokumentZeichnungResource toResource(@Context PathContextBuilder pathContext, DokumentZeichnung domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget DokumentZeichnungResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();
        parameter.put("bereich", resource.build().getBereich());

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORINGZEICHNUNGBEREICH, parameter))
        );
    }

    // DokumentResource
    public abstract DokumentResource toResource(@Context PathContextBuilder pathContext, Dokument domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget DokumentResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();
        parameter.put("name", resource.build().getName());

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_DOKUMENT, baseUri, TAILORINGDOKUMENTDOWNLOAD, parameter))
        );
    }

    // Selektionsvektor
    public abstract SelektionsVektorResource toResource(@Context PathContextBuilder pathContext, SelektionsVektor domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget SelektionsVektorResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();
        String baseUri = linkToCurrentMapping().toString();
        if (nonNull(context.getTailoring())) {
            resource.links(asList(
                createLink(REL_SELF, baseUri, TAILORINGSELEKTIONSVEKTOR, parameter))
            );
        } else if (nonNull(context.getProjekt())) {
            resource.links(asList(
                createLink(REL_SELF, baseUri, PROJEKTSELEKTIONSVEKTOR, parameter))
            );
        }
    }

    // Katalog
    public abstract KatalogResource toResource(@Context PathContextBuilder pathContext, Katalog<TailoringAnforderung> domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget KatalogResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORINGKATALOG, parameter))
        );
    }

    // SelektionsVektorProfil
    public abstract SelektionsVektorProfilResource toResource(@Context PathContextBuilder pathContext, SelektionsVektorProfil domain);

    private String resolveParameter(String path, Map<String, String> parameter) {
        String result = path;
        for (Map.Entry<String, String> entry : parameter.entrySet()) {
            String value = entry.getValue();
            if (nonNull(value)) {
                result = result.replaceAll("\\{" + entry.getKey() + "\\}", value);
            }
        }
        return result;

    }

    public Link createLink(String rel, String baseUri, String path, Map<String, String> parameter) {
        return Link.of(baseUri + "/" + UriTemplate.of(resolveParameter(path, parameter)), rel);
    }
}
