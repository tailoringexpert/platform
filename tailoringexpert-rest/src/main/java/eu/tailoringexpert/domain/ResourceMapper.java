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

import eu.tailoringexpert.domain.BaseCatalogVersionResource.BaseCatalogVersionResourceBuilder;
import eu.tailoringexpert.domain.DocumentSignatureResource.DocumentSignatureResourceBuilder;
import eu.tailoringexpert.domain.FileResource.FileResourceBuilder;
import eu.tailoringexpert.domain.NoteResource.NoteResourceBuilder;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.ProjectResource.ProjectResourceBuilder;
import eu.tailoringexpert.domain.ScreeningSheetResource.ScreeningSheetResourceBuilder;
import eu.tailoringexpert.domain.SelectionVectorResource.SelectionVectorResourceBuilder;
import eu.tailoringexpert.domain.TailoringCatalogChapterResource.TailoringCatalogChapterResourceBuilder;
import eu.tailoringexpert.domain.TailoringCatalogResource.TailoringCatalogResourceBuilder;
import eu.tailoringexpert.domain.TailoringRequirementResource.TailoringRequirementResourceBuilder;
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
    public static final String PROJECTS = "project";
    public static final String PROJECT_NEW = "catalog/{version}/project";
    public static final String PROJECT = "project/{project}";
    public static final String PROJECT_SELECTIONVECTOR = "project/{project}/selectionvector";
    public static final String PROJECT_SCREENINGSHEET = "project/{project}/screeningsheet";
    public static final String PROJECT_SCREENINGSHEET_PDF = "project/{project}/screeningsheet/pdf";
    public static final String TAILORINGREQUIRMENT = "project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}";
    public static final String TAILORINGREQUIRMENT_SELECTED = "project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}/selected/{selected}";
    public static final String TAILORINGREQUIRMENT_TEXT = "project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}/text";
    public static final String CHAPTER_SELECTED = "project/{project}/tailoring/{tailoring}/catalog/{chapter}/selected/{selected}";

    public static final String TAILORINGS = "project/{project}/tailoring";
    public static final String TAILORING = "project/{project}/tailoring/{tailoring}";
    public static final String TAILORING_SCREENINGSHEET = "project/{project}/tailoring/{tailoring}/screeningsheet";
    public static final String TAILORING_REQUIREMENT_IMPORT = "project/{project}/tailoring/{tailoring}/requirement/import";
    public static final String TAILORING_SCREENINGSHEET_PDF = "project/{project}/tailoring/{tailoring}/screeningsheet/pdf";
    public static final String TAILORING_SELECTIONVECTOR = "project/{project}/tailoring/{tailoring}/selectionvector";
    public static final String TAILORING_NAME = "project/{project}/tailoring/{tailoring}/name";
    public static final String TAILORING_SIGNATURE = "project/{project}/tailoring/{tailoring}/signature";
    public static final String TAILORING_SIGNATURE_FACULTY = "project/{project}/tailoring/{tailoring}/signature/{faculty}";
    public static final String TAILORING_DOCUMENT = "project/{project}/tailoring/{tailoring}/document";
    public static final String TAILORING_DOCUMENT_CATALOG = "project/{project}/tailoring/{tailoring}/document/catalog";
    public static final String TAILORING_COMPARE = "project/{project}/tailoring/{tailoring}/compare";
    public static final String TAILORING_CATALOG = "project/{project}/tailoring/{tailoring}/catalog";
    public static final String TAILORING_CATALOG_CHAPTER = "project/{project}/tailoring/{tailoring}/catalog/{chapter}";
    public static final String TAILORING_CATALOG_CHAPTER_REQUIREMENT = "project/{project}/tailoring/{tailoring}/catalog/{chapter}/requirement";
    public static final String TAILORING_ATTACHMENTS = "project/{project}/tailoring/{tailoring}/attachment";
    public static final String TAILORING_ATTACHMENT = "project/{project}/tailoring/{tailoring}/attachment/{name}";
    public static final String TAILORING_NOTES = "project/{project}/tailoring/{tailoring}/note";
    public static final String TAILORING_NOTE = "project/{project}/tailoring/{tailoring}/note/{note}";
    public static final String BASECATALOG = "catalog";
    public static final String BASECATALOG_VERSION = "catalog/{version}";
    public static final String BASECATALOG_VERSION_PDF = "catalog/{version}/pdf";
    public static final String BASECATALOG_VERSION_JSON = "catalog/{version}/json";

    public static final String SCREENINGSHEET = "screeningsheet";
    public static final String SELECTIONVECTOR_PROFILE = "selectionvector";

    // RELs
    public static final String REL_SELF = "self";
    public static final String REL_SCREENINGSHEET = "screeningsheet";
    public static final String REL_SELECTIONVECTOR = "selectionvector";
    public static final String REL_TAILORING = "tailoring";
    public static final String REL_KATALOG = "catalog";
    public static final String REL_PDF = "pdf";
    public static final String REL_JSON = "json";
    public static final String REL_TAILORINGCATALOG_DOCUMENT = "tailoringcatalog";
    public static final String REL_BASECATALOG_DOCUMENT = "basecatalog";
    public static final String REL_SIGNATURE = "signature";
    public static final String REL_KAPITEL = "chapter";
    public static final String REL_TEXT = "text";
    public static final String REL_SELECTED = "selected";
    public static final String REL_DOCUMENT = "document";
    public static final String REL_COMPARE = "compare";
    private static final String REL_NAME = "name";
    private static final String REL_IMPORT = "import";
    private static final String REL_ATTACHMENT = "attachment";
    private static final String REL_NOTE = "note";

    // Katalogversion
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, BaseCatalogVersion domain) {
        pathContext.catalog(nonNull(domain) ? domain.getVersion() : null);
    }

    @Mapping(target = "standard", expression = "java( domain.getValidUntil() == null)")
    public abstract BaseCatalogVersionResource toResource(@Context PathContextBuilder pathContext, BaseCatalogVersion domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget BaseCatalogVersionResourceBuilder resource) {
        PathContext context = pathContext.build();
        String baseUri = linkToCurrentMapping().toString();
        Map<String, String> parameter = context.parameter();
        resource.links(asList(
            linkToCurrentMapping().slash(resolveParameter(PROJECT_NEW, context.parameter())).withRel(PROJECTS),
            createLink(REL_SELF, baseUri, BASECATALOG_VERSION, parameter),
            createLink(REL_PDF, baseUri, BASECATALOG_VERSION_PDF, parameter),
            createLink(REL_JSON, baseUri, BASECATALOG_VERSION_JSON, parameter)
        ));
    }

    // ProjectInformation
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, ProjectInformation domain) {
        pathContext.project(nonNull(domain) ? domain.getIdentifier() : null);
    }

    @Mapping(target = "name", source = "identifier")
    @Mapping(target = "creationTimestamp", source = "creationTimestamp", dateFormat = "dd.MM.yyyy")
    public abstract ProjectResource toResource(@Context PathContextBuilder pathContext, ProjectInformation domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget ProjectResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, PROJECT, parameter),
            createLink(REL_SELECTIONVECTOR, baseUri, PROJECT_SELECTIONVECTOR, parameter),
            createLink(REL_SCREENINGSHEET, baseUri, PROJECT_SCREENINGSHEET, parameter),
            createLink(REL_TAILORING, baseUri, TAILORINGS, parameter)
        ));
    }

    // Project
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, Project domain) {
        pathContext.project(nonNull(domain) ? domain.getIdentifier() : null);
    }

    @Mapping(target = "name", source = "identifier")
    public abstract ProjectResource toResource(@Context PathContextBuilder pathContext, Project domain);


    // TailoringInformation
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, TailoringInformation domain) {
        pathContext.tailoring(nonNull(domain) ? domain.getName() : null);
        pathContext.catalog(nonNull(domain) ? domain.getCatalogVersion() : null);
    }

    public abstract TailoringResource toResource(@Context PathContextBuilder pathContext, TailoringInformation domain);

    @AfterMapping
    public void addLinks(@Context PathContextBuilder pathContext, @MappingTarget TailoringResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
                createLink(REL_SELF, baseUri, TAILORING, parameter),
                createLink(REL_SCREENINGSHEET, baseUri, TAILORING_SCREENINGSHEET, parameter),
                createLink(REL_SELECTIONVECTOR, baseUri, TAILORING_SELECTIONVECTOR, parameter),
                createLink(REL_SIGNATURE, baseUri, TAILORING_SIGNATURE, parameter),
                createLink(REL_DOCUMENT, baseUri, TAILORING_DOCUMENT, parameter),
                createLink(REL_TAILORINGCATALOG_DOCUMENT, baseUri, TAILORING_DOCUMENT_CATALOG, parameter),
                createLink(REL_COMPARE, baseUri, TAILORING_COMPARE, parameter),
                createLink(REL_KATALOG, baseUri, TAILORING_CATALOG, parameter),
                createLink(REL_NAME, baseUri, TAILORING_NAME, parameter),
                createLink(REL_IMPORT, baseUri, TAILORING_REQUIREMENT_IMPORT, parameter),
                createLink(REL_BASECATALOG_DOCUMENT, baseUri, BASECATALOG_VERSION_PDF, parameter),
                createLink(REL_ATTACHMENT, baseUri, TAILORING_ATTACHMENTS, parameter),
                createLink(REL_NOTE, baseUri, TAILORING_NOTES, parameter)
            )
        );
    }

    // ScreeningSheet
    public abstract ScreeningSheetResource toResource(@Context PathContextBuilder builder, ScreeningSheet screeningSheet);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget ScreeningSheetResourceBuilder resource) {
        PathContext context = pathContext.build();
        if (nonNull(context.getProject()) || nonNull(context.getTailoring())) {
            String self = isNull(context.getTailoring()) ? PROJECT_SCREENINGSHEET : TAILORING_SCREENINGSHEET;
            String datei = isNull(context.getTailoring()) ? PROJECT_SCREENINGSHEET_PDF : TAILORING_SCREENINGSHEET_PDF;

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
            .collect(Collectors.groupingBy(ScreeningSheetParameter::getCategory))
            .entrySet()
            .stream()
            .map(entry -> ScreeningSheetParameterResource.builder()
                .label(entry.getKey())
                .value(entry.getValue()
                    .stream()
                    .map(ScreeningSheetParameter::getValue)
                    .map(Objects::toString)
                    .collect(joining("; ")))
                .build()
            )
            .collect(Collectors.toList());
    }

    // Tailoring
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, Tailoring domain) {
        pathContext.tailoring(nonNull(domain) ? domain.getName() : null);
        pathContext.catalog(nonNull(domain) ? domain.getCatalog().getVersion() : null);
    }

    @Mapping(source = "domain.catalog.version", target = "catalogVersion")
    public abstract TailoringResource toResource(@Context PathContextBuilder pathContext, Tailoring domain);

    // TailoringChapter
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, Chapter<TailoringRequirement> domain) {
        if (nonNull(domain)) {
            pathContext.chapter(domain.getNumber());
            pathContext.selected(null);
        }
    }

    public abstract TailoringCatalogChapterResource toResource(@Context PathContextBuilder pathContext, Chapter<TailoringRequirement> domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget TailoringCatalogChapterResourceBuilder resource) {
        PathContext context = pathContext.build();
        pathContext.chapter(resource.build().getNumber());
        Map<String, String> parameter = context.parameter();
        parameter.put(REL_SELECTED, null);
        parameter.put(REL_KAPITEL, resource.build().getNumber());

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORING_CATALOG_CHAPTER, parameter),
            createLink("requirement", baseUri, TAILORING_CATALOG_CHAPTER_REQUIREMENT, parameter),
            createLink("selection", baseUri, CHAPTER_SELECTED, parameter))
        );
    }


    // TailoringRequirement
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, TailoringRequirement domain) {
        if (nonNull(domain)) {
            pathContext.requirment(domain.getPosition());
            pathContext.selected(!domain.getSelected());
        }
    }


    @Mapping(target = "changed", expression = "java( domain.getSelectionChanged() != null || domain.getTextChanged() != null)")
    @Mapping(target = "reference", source = "domain.reference.text")
    public abstract TailoringRequirementResource toResource(@Context PathContextBuilder pathContext, TailoringRequirement domain);


    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget TailoringRequirementResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORINGREQUIRMENT, parameter),
            createLink(REL_SELECTED, baseUri, TAILORINGREQUIRMENT_SELECTED, parameter),
            createLink(REL_TEXT, baseUri, TAILORINGREQUIRMENT_TEXT, parameter))
        );
    }

    // DocumentSignature
    public abstract DocumentSignatureResource toResource(@Context PathContextBuilder pathContext, DocumentSignature domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget DocumentSignatureResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();
        parameter.put("faculty", resource.build().getFaculty());

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORING_SIGNATURE_FACULTY, parameter))
        );
    }

    // FileResource
    public abstract FileResource toResource(@Context PathContextBuilder pathContext, File domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget FileResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();
        parameter.put("name", resource.build().getName());

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORING_ATTACHMENT, parameter))
        );
    }

    // Selectionvector
    public abstract SelectionVectorResource toResource(@Context PathContextBuilder pathContext, SelectionVector domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget SelectionVectorResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();
        String baseUri = linkToCurrentMapping().toString();
        if (nonNull(context.getTailoring())) {
            resource.links(asList(
                createLink(REL_SELF, baseUri, TAILORING_SELECTIONVECTOR, parameter))
            );
        } else if (nonNull(context.getProject())) {
            resource.links(asList(
                createLink(REL_SELF, baseUri, PROJECT_SELECTIONVECTOR, parameter))
            );
        }
    }

    // Catalog
    public abstract TailoringCatalogResource toResource(@Context PathContextBuilder pathContext, Catalog<TailoringRequirement> domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget TailoringCatalogResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORING_CATALOG, parameter))
        );
    }

    // SelectionVectorProfile
    public abstract SelectionVectorProfileResource toResource(@Context PathContextBuilder pathContext, SelectionVectorProfile domain);

    // Note
    @BeforeMapping
    protected void updatePathContext(@Context PathContextBuilder pathContext, Note domain) {
        pathContext.note(domain.getNumber().toString());
    }

    @Mapping(target = "creationTimestamp", source = "creationTimestamp", dateFormat = "dd.MM.yyyy HH:mm")
    public abstract NoteResource toResource(@Context PathContextBuilder pathContext, Note domain);

    @AfterMapping
    protected void addLinks(@Context PathContextBuilder pathContext, @MappingTarget NoteResourceBuilder resource) {
        PathContext context = pathContext.build();
        Map<String, String> parameter = context.parameter();

        String baseUri = linkToCurrentMapping().toString();
        resource.links(asList(
            createLink(REL_SELF, baseUri, TAILORING_NOTE, parameter))
        );
    }

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
