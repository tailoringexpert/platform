package eu.tailoringexpert.domain;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonNaming;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Collection;

@Value
@Builder

class ReqIFContent {

    @JacksonXmlElementWrapper(localName = "DATATYPES")
    Collection<DatatypeDefinition> datatypes;

    @JacksonXmlElementWrapper(localName = "SPEC-TYPES")
    Collection<SpecType> specTypes;

    @JacksonXmlElementWrapper(localName = "SPEC-OBJECTS")
    Collection<SpecObject> specObjects;
//    Collection<SpecRelation> specRelations;
//    Collection<Specification> specifications;
//    Collection<RelationGroup> specRelationGroups;
//    Collection<String> test;
}
