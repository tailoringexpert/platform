package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.Specification;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class SpecificationConsumer implements BiConsumer<Specification, ToXmlGenerator> {
  private static final QName QNAME_SPECIFICATION = new QName("SPECIFICATION");

  private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    @Override
    public void accept(Specification specification, ToXmlGenerator generator) {
        generator.startWrappedValue(QNAME_SPECIFICATION, QNAME_SPECIFICATION);
        generator.setNextIsAttribute(true);
        identifiable.accept(specification, generator);
        generator.finishWrappedValue(QNAME_SPECIFICATION, QNAME_SPECIFICATION);
    }
}
