package eu.tailoringexpert.domain;

import lombok.Builder;
import lombok.Value;

import java.util.Collection;

@Value
@Builder
public class ReqIFContent {

    Collection<DatatypeDefinition> datatypes;
    Collection<SpecType> specTypes;
    Collection<SpecObject> specObjects;



//    Collection<SpecRelation> specRelations;
//    Collection<RelationGroup> specRelationGroups;

    //@JacksonXmlElementWrapper(localName = "SPEC-TYPES")
    //Collection<Specification> specifications;

}
