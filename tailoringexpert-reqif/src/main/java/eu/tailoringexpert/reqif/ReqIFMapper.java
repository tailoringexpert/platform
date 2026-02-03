package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.ReqIFContent;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.Map.of;

public class ReqIFMapper {
    // specttypes
    SpecObjectType specTypeNormative = SpecObjectType.builder()
        .identifier("st-normative")
        .longName("Normative Statement")
        .specAttributes(List.of(
            AttributeDefinitionString.builder()
                .identifier("a-document-code")
                .build()
        ))
        .build();

    SpecObjectType
        specTypeChapter = SpecObjectType.builder()
        .identifier("type_chapter")
        .longName("Chapter")
        .specAttributes(List.of(
            AttributeDefinitionString.builder()
                .identifier("attr_name")
                .longName("Name of the chapter")
                .type(DatatypeDefinitionString.builder()
                    .maxLength(200)
                    .identifier("dt-string")
                    .longName("String")
                    .build())
                .build()
        ))
        .build();


    private Function<TailoringRequirement, AttributeValueString> requirementText = new ToRequirementValue();
    private Function<TailoringRequirement, AttributeValueBoolean> selected = new ToSelectedValue();
    private Function<String, AttributeValueString> chapter = new ToChapterHeaderValue();
    private Function<Integer, AttributeValueEnumeration> kind = new ToKindValue();

    ReqIFContent doit(Tailoring tailoring, Map<String, Object> placeholders) {
        Collection<SpecObject> specObjects = new LinkedList<>();

        tailoring.getCatalog().getToc().getChapters()
            .forEach(chapter -> {
                    addChapter(chapter, 1, specObjects, placeholders);
                }
            );

        return ReqIFContent.builder()
            .specObjects(specObjects)
            .build();

    }

    void addChapter(Chapter<TailoringRequirement> chapter, int level, Collection<
        SpecObject> specObjects, Map<String, Object> placeholders) {


//        <SPEC-OBJECT IDENTIFIER="obj_1" LAST-CHANGE="2023-10-27T10:00:00Z">
//            <TYPE>
//            <SPEC-OBJECT-TYPE-REF>type_headline</SPEC-OBJECT-TYPE-REF>
//            </TYPE>
//            <VALUES>
//            <ATTRIBUTE-VALUE-STRING THE-VALUE="1. Einleitung">
//            <DEFINITION><ATTRIBUTE-DEFINITION-STRING-REF>attr_title</ATTRIBUTE-DEFINITION-STRING-REF></DEFINITION>
//            </ATTRIBUTE-VALUE-STRING>
//            </VALUES>
//            </SPEC-OBJECT>

        specObjects.add(SpecObject.builder()
            .identifier("so-" + chapter.getNumber() + " " + chapter.getName())
            .type(specTypeChapter)
            .values(List.of(
                this.chapter.apply(chapter.getNumber() + " " + chapter.getName())
            ))
            .build());
        //.text(templateEngine.toXHTML(chapter.getNumber() + " " + chapter.getName(), emptyMap()))

        chapter.getRequirements()
            .forEach(requirement -> addRequirement(requirement, chapter.getNumber(), specObjects));
        final AtomicInteger nextLevel = new AtomicInteger(level + 1);
        chapter.getChapters()
            .forEach(subChapter -> addChapter(subChapter, nextLevel.get(), specObjects, placeholders));
    }

    void addRequirement(TailoringRequirement requirement, String
        chapterNumber, Collection<SpecObject> specObjects) {
        specObjects.add(SpecObject.builder()
            .identifier("so-" + chapterNumber + requirement.getPosition())
            .type(specTypeNormative)
            .values(List.of(
                requirementText.apply(requirement),
                kind.apply(0),
                selected.apply(requirement)
            ))
            .build());

    }

}
