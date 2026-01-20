package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.SpecType;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.ValueSerializerModifier;

public class DynamicRootNameBeanSerializerModifier extends ValueSerializerModifier {

    public ValueSerializer<?> modifySerializer(SerializationConfig config,
                                               BeanDescription.Supplier beanDesc, ValueSerializer<?> serializer) {
        if (beanDesc.getBeanClass() == DatatypeDefinitionString.class) {
            return new IdentifiableTagSerializer((ValueSerializer<Identifiable>) serializer);
        } else if (beanDesc.getBeanClass() == SpecObject.class) {
            return new IdentifiableTagSerializer((ValueSerializer<Identifiable>) serializer);
        } else if (beanDesc.getBeanClass() == SpecType.class) {
            return new IdentifiableTagSerializer((ValueSerializer<Identifiable>) serializer);
        }
        return serializer;
    }


}
