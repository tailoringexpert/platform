package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.DatatypeDefinition;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.ReqIFContent;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import eu.tailoringexpert.domain.SpecificationType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;


public class ReqIFContentSerializer extends StdSerializer<ReqIFContent> {

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();
    private BiConsumer<Collection<SpecType>, ToXmlGenerator> specTypes = new SpecTypesConsumer();
    private BiConsumer<Collection<DatatypeDefinition>, ToXmlGenerator> datatypes = new DatatypeDefinitionsConsumer();
    private BiConsumer<Collection<SpecObject>, ToXmlGenerator> specObjects = new SpecObjectsConsumer();


    public ReqIFContentSerializer() {
        super(ReqIFContent.class);
    }

    @Override
    public void serialize(ReqIFContent value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        QName parent = new QName("", "CORE-CONTENT");
        QName name = new QName("", "REQ-IF-CONTENT");

        ToXmlGenerator generator = (ToXmlGenerator) gen;
        generator.startWrappedValue(parent, name);
        generator.writeStartObject();

        datatypes.accept(value.getDatatypes(), generator);
        specTypes.accept(value.getSpecTypes(), generator);
        specObjects.accept(value.getSpecObjects(), generator);

        generator.writeEndObject();
        generator.finishWrappedValue(parent, name);
    }


}

