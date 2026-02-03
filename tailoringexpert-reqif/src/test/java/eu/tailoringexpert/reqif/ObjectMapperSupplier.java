package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionEnumeration;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionEnumeration;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.ReqIFHeader;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecRelation;
import eu.tailoringexpert.domain.Specification;
import eu.tailoringexpert.domain.SpecificationType;
import eu.tailoringexpert.serializer.AttributeDefinitionBooleanSerializer;
import eu.tailoringexpert.serializer.AttributeDefinitionEnumerationSerializer;
import eu.tailoringexpert.serializer.AttributeDefinitionStringSerializer;
import eu.tailoringexpert.serializer.AttributeValueBooleanSerializer;
import eu.tailoringexpert.serializer.AttributeValueEnumerationSerializer;
import eu.tailoringexpert.serializer.AttributeValueStringSerializer;
import eu.tailoringexpert.serializer.DatatypeDefinitionBooleanSerializer;
import eu.tailoringexpert.serializer.DatatypeDefinitionEnumerationSerializer;
import eu.tailoringexpert.serializer.DatatypeDefinitionStringSerializer;
import eu.tailoringexpert.serializer.ReqIFHeaderSerializer;
import eu.tailoringexpert.serializer.SpecObjectSerializer;
import eu.tailoringexpert.serializer.SpecObjectTypeSerializer;
import eu.tailoringexpert.serializer.SpecRelationSerializer;
import eu.tailoringexpert.serializer.SpecificationSerializer;
import eu.tailoringexpert.serializer.SpecificationTypeSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.util.function.Supplier;

import static tools.jackson.databind.SerializationFeature.INDENT_OUTPUT;

public class ObjectMapperSupplier implements Supplier<ObjectMapper> {

    ObjectMapper mapper;

    public ObjectMapperSupplier() {
        SimpleModule reqIFModule = new SimpleModule();
        reqIFModule.addSerializer(ReqIFHeader.class, new ReqIFHeaderSerializer());
        reqIFModule.addSerializer(DatatypeDefinitionString.class, new DatatypeDefinitionStringSerializer());
        reqIFModule.addSerializer(DatatypeDefinitionBoolean.class, new DatatypeDefinitionBooleanSerializer());
        reqIFModule.addSerializer(DatatypeDefinitionEnumeration.class, new DatatypeDefinitionEnumerationSerializer());
        reqIFModule.addSerializer(AttributeDefinitionBoolean.class, new AttributeDefinitionBooleanSerializer());
        reqIFModule.addSerializer(AttributeDefinitionString.class, new AttributeDefinitionStringSerializer());
        reqIFModule.addSerializer(AttributeValueBoolean.class, new AttributeValueBooleanSerializer());
        reqIFModule.addSerializer(AttributeDefinitionEnumeration.class, new AttributeDefinitionEnumerationSerializer());
        reqIFModule.addSerializer(AttributeValueEnumeration.class, new AttributeValueEnumerationSerializer());
        reqIFModule.addSerializer(AttributeValueString.class, new AttributeValueStringSerializer());
        reqIFModule.addSerializer(SpecObject.class, new SpecObjectSerializer());
        reqIFModule.addSerializer(SpecificationType.class, new SpecificationTypeSerializer());
        reqIFModule.addSerializer(SpecObjectType.class, new SpecObjectTypeSerializer());
        reqIFModule.addSerializer(Specification.class, new SpecificationSerializer());
        reqIFModule.addSerializer(SpecRelation.class, new SpecRelationSerializer());

        this.mapper = XmlMapper.builder()
            .findAndAddModules()
            .enable(INDENT_OUTPUT)
            .addModule(reqIFModule)
            .build();
    }

    @Override
    public ObjectMapper get() {
        return this.mapper;
    }
}
