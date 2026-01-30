package eu.tailoringexpert.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Value;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collection;

@Value
@Builder
public class ReqIFContent {

    @JsonProperty("DATATYES")
    Collection<DatatypeDefinition> datatypes;

    @JsonProperty("SPEC-TYPES")
    Collection<SpecType> specTypes;

    @JsonProperty("SPEC-OBJECTS")
    Collection<SpecObject> specObjects;


//    Collection<SpecRelation> specRelations;
//    Collection<RelationGroup> specRelationGroups;
//    Collection<Specification> specifications;

}
