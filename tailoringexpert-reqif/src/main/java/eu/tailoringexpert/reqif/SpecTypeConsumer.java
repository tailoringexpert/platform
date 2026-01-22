package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import eu.tailoringexpert.domain.SpecificationType;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class SpecTypeConsumer implements BiConsumer<ToXmlGenerator, SpecType> {

    private BiConsumer<ToXmlGenerator, Identifiable> identifiable = new IdentifiableConsumer();
    private BiConsumer<ToXmlGenerator, AttributeDefinition> attributeDefinition = new AttributeDefinitionConsumer();

    @Override
    public void accept(ToXmlGenerator generator, SpecType specType) {
        if (specType instanceof SpecObjectType) {
            accept(generator, (SpecObjectType) specType);
        } else if (specType instanceof SpecificationType) {
            accept(generator, (SpecificationType) specType);
        }
    }

    private void accept(ToXmlGenerator generator, SpecificationType specType) {
        identifiable.accept(generator, specType);
    }

    private void accept(ToXmlGenerator generator, SpecObjectType specType) {
        identifiable.accept(generator, specType);
        generator.setNextIsAttribute(false);

        QName name = new QName("", "SPEC-ATTRIBUTES");
        generator.startWrappedValue(name, name);

        specType.getSpecAttributes()
            .forEach(attribute -> attributeDefinition.accept(generator, attribute));

        generator.setNextIsAttribute(false);
        generator.finishWrappedValue(name, name);
    }
}

