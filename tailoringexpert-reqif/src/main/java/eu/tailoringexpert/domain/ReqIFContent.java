package eu.tailoringexpert.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
public class ReqIFContent {

    Collection<DatatypeDefinition> datatypes;
    Collection<SpecType> specTypes;
    Collection<SpecObject> specObjects;



//    Collection<SpecRelation> specRelations;
//    Collection<RelationGroup> specRelationGroups;

    //@JacksonXmlElementWrapper(localName = "SPEC-TYPES")
    //Collection<Specification> specifications;

}
