package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.util.NameTransformer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;

@RequiredArgsConstructor
public class IdentifiableTagSerializer extends ValueSerializer<Identifiable> {

    @NonNull
    private ValueSerializer<Identifiable> serializer;

    private final Map<Class, String> keys = Map.of(
        DatatypeDefinitionString.class, "DATATYPE-DEFINITION-STRING",
        SpecObjectType.class, "SPEC-OBJECT-OBJECT-TYPE",
        SpecType.class, "SPEC-OBJECT-TYPE"
    );

    @Override
    public void serialize(Identifiable value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;
        writeDynamicRootName(keys.get(value.getClass()), xmlGen);
        serializeProperties(value, gen, serializers);
        writeEndObject(xmlGen);
    }

    private void writeDynamicRootName(String rootName, ToXmlGenerator xmlGen) throws JacksonException {
        xmlGen.setNextName(new QName("", rootName));
        xmlGen.writeStartObject();
        xmlGen.setNextIsAttribute(true);
    }

    private void serializeProperties(Identifiable value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        serializer.unwrappingSerializer(NameTransformer.NOP).serialize(value, gen, serializers);
    }

    private void writeEndObject(ToXmlGenerator xmlGen) throws JacksonException {
        xmlGen.writeEndObject();
    }
}
