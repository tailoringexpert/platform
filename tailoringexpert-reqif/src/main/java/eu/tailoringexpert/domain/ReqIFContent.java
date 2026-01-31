package eu.tailoringexpert.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;

@Value
@Builder
public class ReqIFContent {

    @JsonProperty("DATATYPES")
    Collection<DatatypeDefinition> datatypes;

    @JsonProperty("SPEC-TYPES")
    Collection<SpecType> specTypes;

    @JsonProperty("SPEC-OBJECTS")
    Collection<SpecObject> specObjects;

    @JsonProperty("SPECIFICATIONS")
    Collection<Specification> specifications;

    @JsonProperty("SPEC-RELATIONS")
    Collection<SpecRelation> specRelations;

//    Collection<RelationGroup> specRelationGroups;

}
