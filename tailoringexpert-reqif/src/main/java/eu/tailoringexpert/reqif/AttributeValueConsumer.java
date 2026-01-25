package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeValue;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AttributeValueConsumer implements BiConsumer<AttributeValue, ToXmlGenerator> {

    private Map<Class, String> attribute2Xml = Map.ofEntries(
        Map.entry(AttributeValueString.class, "ATTRIBUTE-VALUE-STRING")
    );


    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    @Override
    public void accept(AttributeValue attributeValue, ToXmlGenerator generator) {
        new AttributeValueConsumerRegistry()
            .apply(attributeValue)
            .accept(attributeValue, generator);
//        QName name = new QName(attribute2Xml.get(attributeValue.getClass()));
//        generator.startWrappedValue(name, name);
//        generator.setNextIsAttribute(true);
//
//        if (AttributeValueString.class.equals(attributeValue.getClass())) {
//            accept((AttributeValueString) attributeValue, generator);
//        }
//
//        generator.finishWrappedValue(name, name);

    }

    private void accept(AttributeValueString valueString, ToXmlGenerator generator) {
        generator.setNextIsAttribute(true);
        generator.writeStringProperty("THE-VALUE", valueString.getTheValue());

        generator.setNextIsAttribute(false);
        generator.startWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));
        generator.writeStringProperty("ATTRIBUTE-DEFINITION-STRING-REF", valueString.getDefinition().getType().getIdentifier());

        generator.finishWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));
    }


    static class AttributeValueConsumerRegistry implements Function<AttributeValue, BiConsumer<AttributeValue,ToXmlGenerator>> {

        BiConsumer<AttributeValue, ToXmlGenerator> attributeValueString = (value, generator) -> {
            QName name = new QName("ATTRIBUTE-VALUE-STRING");
            generator.startWrappedValue(name, name);

            generator.setNextIsAttribute(true);
            generator.writeStringProperty("THE-VALUE", ((AttributeValueString) value).getTheValue());

            generator.setNextIsAttribute(false);
            generator.startWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));
            generator.writeStringProperty("ATTRIBUTE-DEFINITION-STRING-REF", value.getDefinition().getType().getIdentifier());

            generator.finishWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));

            generator.finishWrappedValue(name, name);
        };

        BiConsumer<AttributeValue, ToXmlGenerator> attributeValueEnumeration = (value, generator) -> {
            QName name = new QName("ATTRIBUTE-VALUE-ENUMERATION");
            generator.startWrappedValue(name, name);



            generator.setNextIsAttribute(false);
            generator.startWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));
            generator.writeStringProperty("ATTRIBUTE-DEFINITION-ENUMERATION-REF", value.getDefinition().getType().getIdentifier());

            generator.finishWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));

            generator.finishWrappedValue(name, name);
        };


//
//         <ATTRIBUTE-VALUE-ENUMERATION>
//                            <VALUES>
//                                <ENUM-VALUE-REF>v-ecss-q-st-80d software product assurance</ENUM-VALUE-REF>
//                            </VALUES>
//                            <DEFINITION>
//                                <ATTRIBUTE-DEFINITION-ENUMERATION-REF>a-type</ATTRIBUTE-DEFINITION-ENUMERATION-REF>
//                            </DEFINITION>
//                        </ATTRIBUTE-VALUE-ENUMERATION>

        private Map<Class<?>, BiConsumer<AttributeValue, ToXmlGenerator>> impl = Map.ofEntries(
            Map.entry(AttributeValueString.class, attributeValueString),
            Map.entry(AttributeValue)
        );


        @Override
        public BiConsumer<AttributeValue, ToXmlGenerator> apply(AttributeValue attributeValue) {
            return impl.get(attributeValue.getClass());
        }
    }
}
