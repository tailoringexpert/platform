package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import eu.tailoringexpert.domain.SpecificationType;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class SpecTypeConsumer implements BiConsumer<SpecType, ToXmlGenerator> {

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();
    private BiConsumer<AttributeDefinition, ToXmlGenerator> attributeDefinition = new AttributeDefinitionConsumer();

    @Override
    public void accept(SpecType specType, ToXmlGenerator generator) {
        if (specType instanceof SpecObjectType) {
            accept(generator, (SpecObjectType) specType);
        } else if (specType instanceof SpecificationType) {
            accept(generator, (SpecificationType) specType);
        }
    }

    private void accept(ToXmlGenerator generator, SpecificationType specType) {
        identifiable.accept(specType, generator);
    }

    private void accept(ToXmlGenerator generator, SpecObjectType specType) {
        identifiable.accept(specType, generator);
        generator.setNextIsAttribute(false);

        QName name = new QName("", "SPEC-ATTRIBUTES");
        generator.startWrappedValue(name, name);

        specType.getSpecAttributes()
            .forEach(attribute -> attributeDefinition.accept(attribute, generator));

        generator.setNextIsAttribute(false);
        generator.finishWrappedValue(name, name);
    }
}

